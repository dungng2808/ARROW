"""Cập nhật một bộ phân công máy (shards/<plan>/) theo cấu hình thực nghiệm hiện tại.

Dùng khi manifest hoặc cấu hình thực nghiệm đổi (ví dụ thay một CUT trong pilot) mà vẫn giữ nguyên cách chia máy:
thay task cũ bằng task mới ở đúng shard chứa task cũ, ghi lại experiment_sha256, manifests_sha256 và sha256 của từng file.
Kiểm tra: mọi task của thực nghiệm nằm ở đúng một shard; mọi task trong shard có trong thực nghiệm; mỗi repository chỉ ở một shard.

Chạy từ ARROW/:
    python ARROW_Framework/scripts/update_shard_plan.py --plan-dir shards/pilot-6 \
        --experiment ARROW_Framework/experiments/pilot-v1.json --replace OLD_TASK=NEW_TASK

sha256 của cấu hình và manifest tính như framework (Hashing.Sha256TextFile): UTF-8, CRLF đổi thành LF.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from collections import Counter
from pathlib import Path


def text_sha256(path: Path) -> str:
    return hashlib.sha256(path.read_text(encoding="utf-8").replace("\r\n", "\n").encode("utf-8")).hexdigest()


def dump(data: dict) -> str:
    return json.dumps(data, indent=2, ensure_ascii=False) + "\n"


def write(path: Path, content: str) -> None:
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write(content)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--plan-dir", required=True, type=Path)
    parser.add_argument("--experiment", required=True, type=Path)
    parser.add_argument("--replace", action="append", default=[], metavar="OLD=NEW")
    args = parser.parse_args()

    experiment = json.loads(args.experiment.read_text(encoding="utf-8"))
    experiment_sha = text_sha256(args.experiment)
    tasks: dict[str, dict] = {}
    manifests_sha = {}
    for ref in experiment["manifests"]:
        path = (args.experiment.parent / ref["path"]).resolve()
        sha = text_sha256(path)
        if sha != ref["sha256"]:
            print(f"ERROR {path}: sha256 {sha} khác cấu hình {ref['sha256']}", file=sys.stderr)
            return 1
        manifests_sha[ref["dataset"]] = sha
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.strip():
                record = json.loads(line)
                tasks[record["task_id"]] = {"dataset": ref["dataset"], "task_id": record["task_id"], "repo_url": record["repo_url"]}

    replacements = {}
    for item in args.replace:
        old, _, new = item.partition("=")
        if new not in tasks:
            print(f"ERROR task mới {new} không có trong thực nghiệm", file=sys.stderr)
            return 1
        replacements[old] = new

    plan_path = args.plan_dir / "plan.json"
    plan = json.loads(plan_path.read_text(encoding="utf-8"))
    seen: Counter[str] = Counter()
    repo_shard: dict[str, str] = {}
    for entry in plan["shards"]:
        shard_path = args.plan_dir / entry["file"]
        shard = json.loads(shard_path.read_text(encoding="utf-8"))
        shard["tasks"] = [tasks[replacements.get(t["task_id"], t["task_id"])] if replacements.get(t["task_id"], t["task_id"]) in tasks else t
                          for t in shard["tasks"]]
        shard["experiment_sha256"] = experiment_sha
        for task in shard["tasks"]:
            if task["task_id"] not in tasks:
                print(f"ERROR {entry['file']}: task {task['task_id']} không có trong thực nghiệm", file=sys.stderr)
                return 1
            seen[task["task_id"]] += 1
            owner = repo_shard.setdefault(task["repo_url"], entry["shard_id"])
            if owner != entry["shard_id"]:
                print(f"ERROR repository {task['repo_url']} nằm ở cả {owner} và {entry['shard_id']}", file=sys.stderr)
                return 1
        content = dump(shard)
        write(shard_path, content)
        entry["sha256"] = hashlib.sha256(content.encode("utf-8")).hexdigest()
        entry["task_count"] = len(shard["tasks"])
        entry["repository_count"] = len({t["repo_url"] for t in shard["tasks"]})
        entry["tasks_by_dataset"] = dict(sorted(Counter(t["dataset"] for t in shard["tasks"]).items()))

    missing = sorted(set(tasks) - set(seen))
    duplicated = sorted(t for t, n in seen.items() if n > 1)
    if missing or duplicated:
        print(f"ERROR thiếu {missing}, trùng {duplicated}", file=sys.stderr)
        return 1

    plan["experiment_id"] = experiment["experiment_id"]
    plan["experiment_sha256"] = experiment_sha
    plan["manifests_sha256"] = manifests_sha
    write(plan_path, dump(plan))
    print(f"OK {len(plan['shards'])} shard, {len(tasks)} task; experiment_sha256={experiment_sha}; thay {replacements}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
