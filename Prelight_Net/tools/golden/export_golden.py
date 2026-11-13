"""Xuất oracle bằng hàm Python gốc, chỉ đọc dataset/mirror/log có sẵn.

Không clone, fetch, checkout, Maven/Gradle hay cài package. Dữ liệu đầy đủ luôn
ở .work/golden-full; tests/golden giữ tập cố định <= 50 MB nếu cần thu nhỏ.
"""
from __future__ import annotations

import argparse
import base64
import csv
import dataclasses
import hashlib
import importlib.metadata
import itertools
import json
import os
import random
import subprocess
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[2]
ARROW = ROOT.parent
PYTHON_ROOT = ARROW / "preflight_tool"
sys.path[:0] = [str(PYTHON_ROOT), str(PYTHON_ROOT / "scripts")]
from preflight import ingest, java_ast, policy, revision, runner, util
from preflight.models import BuildAttempt, PreflightStatus, RevisionStatus, RunMode
import rerun_fixed_probe as v2
from export_unicode_tables import unicode_cases
from sanitize_records import sanitize_payload


def safe_output(path: Path) -> Path:
    path = path.resolve()
    if not path.is_relative_to(ROOT) or path.is_relative_to(ROOT / "Dataset") or path.is_relative_to(ROOT / "file.md"):
        raise ValueError(f"Output phải nằm trong Prelight_Net và ngoài Dataset/file.md: {path}")
    return path


def serialize(value):
    if dataclasses.is_dataclass(value):
        return {k: serialize(v) for k, v in dataclasses.asdict(value).items()}
    if isinstance(value, set):
        return sorted(value)
    if isinstance(value, (tuple, list)):
        return [serialize(v) for v in value]
    if isinstance(value, dict):
        return {k: serialize(v) for k, v in value.items()}
    return value


class Exporter:
    def __init__(self, folder: Path, limit: int):
        self.folder, self.limit = folder, limit
        self.handles, self.counts = {}, {}
        self.sources = {}
        folder.mkdir(parents=True, exist_ok=True)

    def add(self, name, input_, function):
        if self.limit and self.counts.get(name, 0) >= self.limit:
            return
        if name not in self.handles:
            self.handles[name] = (self.folder / f"{name}.jsonl").open("w", encoding="utf-8", newline="\n")
        row = {"input": input_}
        try:
            row["output"] = serialize(function())
        except Exception as error:
            row["error"] = type(error).__name__
        self.handles[name].write(json.dumps(row, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n")
        self.counts[name] = self.counts.get(name, 0) + 1

    def close(self):
        for handle in self.handles.values():
            handle.close()


def write_files(root, files):
    root.mkdir(parents=True, exist_ok=True)
    for name, text in files.items():
        path = root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8", newline="\n")


def export_tasks(ex):
    paths = sorted((ARROW / "preflight_manifest").glob("0*-*/class_candidates.jsonl"))
    for path in paths:
        ex.sources[path.relative_to(ARROW).as_posix()] = util.sha256_file(path)
        with path.open(encoding="utf-8") as handle:
            for line in handle:
                row = json.loads(line)
                args = [row[key] for key in ("project_id", "repo_url", "class_path", "class_name")]
                ex.add("task_id", args, lambda: util.stable_task_id(*args))
    for args in [["1", "url", "a/X.java", "X"], ["dữ liệu", "https://example.test/é", "x/Đ.java", "Đ"], ["1", "a\0b", "c", "D"]]:
        ex.add("task_id", args, lambda: util.stable_task_id(*args))


def export_records(ex, rng, work):
    dataset = ROOT / "Dataset/classes2test-main/dataset"
    folders = sorted(p for p in dataset.iterdir() if p.is_dir())
    rng.shuffle(folders)
    raw_paths = []
    # Chọn theo seed nhưng không nạp hàng trăm nghìn JSON vào RAM.
    for folder in folders:
        paths = sorted(folder.glob("*.json"))
        rng.shuffle(paths)
        raw_paths.extend(paths[:20])
        if len(raw_paths) >= max(10000, ex.limit):
            break
    parameters = []
    edge_texts = ['String s = "https://a/*x*/"; // x\nint n=1;', '"a b"', "x\x1cy\x1dz\x1e\x1fq", '"\\\"//"; /* unfinished', "'\\\\' // end", '"unclosed', "/*broken", "// end", "\u00a0\u2028\u2029"]
    for text in edge_texts:
        ex.add("normalize_java", text, lambda: util.normalize_java(text))
    for path in raw_paths[:10000 if not ex.limit else ex.limit]:
        payload = sanitize_payload(path.read_bytes())
        raw = json.loads(payload.decode("utf-8-sig"))
        for section in ("focal_method", "test_case"):
            text = str((raw.get(section) or {}).get("body") or "")
            ex.add("normalize_java", text, lambda: util.normalize_java(text))
        parameters.append(str((raw.get("focal_method") or {}).get("parameters") or "()"))
        parameters.extend(str(method.get("parameters") or "()") for method in (raw.get("focal_class") or {}).get("methods", []))
        target = work / "records" / path.parent.name / path.name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        ex.add("record_identity", {"project_id": path.parent.name, "name": path.name, "bytes_base64": base64.b64encode(payload).decode()}, lambda: ingest._record_identity(target))
    template = {"repository": {"url": " https://example.test/repo "}, "focal_class": {"file": "src/main/java/X.java", "identifier": " X "}, "test_class": {"file": "src/test/java/XTest.java"}}
    bad_records = [b"{bad", b"\x80\x81", b"[]", b"null", b"true", b"{}", b'"hello"', b"\xef\xbb\xbf" + json.dumps(template).encode(), b"\xef\xbb\xbf\xef\xbb\xbf{}"]
    for field, unsafe in itertools.product(("focal_class", "test_class"), ("../X.java", "src/../X.java", "C:/X.java", "C:X.java", "\\\\server\\share\\X.java", "/X.java", "src/\0X.java")):
        row = json.loads(json.dumps(template)); row[field]["file"] = unsafe
        bad_records.append(json.dumps(row).encode())
    for i, payload in enumerate(bad_records):
        target = work / "records/edge" / f"{i}.json"; target.parent.mkdir(parents=True, exist_ok=True); target.write_bytes(payload)
        ex.add("record_identity", {"project_id": "edge", "name": target.name, "bytes_base64": base64.b64encode(payload).decode()}, lambda: ingest._record_identity(target))
    edges = ["()", "(boolean b, byte b2, short s, int i, long l, float f, double d, char c)", "(@NotNull final String s, int[] a, boolean... varargs)", "(Map<String,List<Integer>> value, T v, K[] k, E1 e)", "(Outer.Inner inner, Inner[] rest)", "(@ĐặcBiệt final String name)", "(@A² String x)", "(@A\u0301 String x)"]
    parameters = edges + parameters
    # Dữ liệu thật có nhiều method trong mỗi class: đủ 20.000 chuỗi tham số.
    for p in parameters[:max(20000, ex.limit)]:
        ex.add("primitive_arguments", p, lambda: java_ast.primitive_arguments(p))
        ex.add("split_parameters", p, lambda: v2._split_parameters(p))
        if p.count("<") != p.count(">"):
            continue  # Hàm Python gốc treo với generic chưa đóng.
        ex.add("cast_arguments", {"parameters": p, "outer": "Outer", "nested": ["Inner"]}, lambda: v2._cast_arguments(p, "Outer", {"Inner"}))
        for chunk in v2._split_parameters(p):
            ex.add("erased_type", chunk, lambda: v2._erased_type(chunk))


def git(mirror, *args):
    result = subprocess.run(["git", "-c", f"safe.directory={mirror.as_posix()}", "-c", "core.quotepath=false", "-C", str(mirror), *args], capture_output=True, check=True, timeout=120)
    return result.stdout


def export_targets(ex, work):
    samples = []
    names = ["pom.xml", "build.gradle", "build.gradle.kts"]
    for mirror in sorted((ARROW / "repo/mirrors").glob("*.git")):
        # Các cache này có FETCH_HEAD nhưng không nhất thiết có refs/heads.
        fetched = mirror / "FETCH_HEAD"
        revisions = [line.split()[0] for line in fetched.read_text(encoding="utf-8").splitlines()] if fetched.is_file() else []
        listing = git(mirror, "rev-list", "--objects", "--all", *revisions, "--", *names, *[f"**/{name}" for name in names]).decode("utf-8", "replace")
        objects = []
        for line in listing.splitlines():
            oid, _, name = line.partition(" ")
            if name.rsplit("/", 1)[-1] in names:
                objects.append((oid, name))
        # Đọc object bằng một process, chỉ đọc bare mirror.
        process = subprocess.run(["git", "-c", f"safe.directory={mirror.as_posix()}", "-C", str(mirror), "cat-file", "--batch"], input="".join(oid + "\n" for oid, _ in objects).encode(), capture_output=True, check=True, timeout=120)
        data, offset = process.stdout, 0
        for oid, name in objects:
            end = data.index(b"\n", offset); header = data[offset:end].split(); size = int(header[-1]); offset = end + 1
            content = data[offset:offset + size]; offset += size + 1
            if header[1] != b"blob": continue
            samples.append(({name.rsplit("/", 1)[-1]: content.decode("utf-8", "replace")}, "maven" if name.endswith("pom.xml") else "gradle", {"mirror": mirror.name, "oid": oid, "path": name}))
            if len(samples) >= (ex.limit or 3000): break
        if len(samples) >= (ex.limit or 3000): break
    # Bổ sung file build còn sót, nếu mirror không đủ.
    if len(samples) < (ex.limit or 3000):
        for workspace in sorted((PYTHON_ROOT / "runs").glob("*/workspaces")):
            walk_root = "\\\\?\\" + str(workspace.resolve()) if os.name == "nt" else str(workspace)
            for directory, folders, files in os.walk(walk_root, followlinks=False):
                folders.sort()
                for name in sorted(set(files).intersection(names)):
                    path = Path(directory) / name
                    try: content = path.read_text(encoding="utf-8", errors="replace")
                    except OSError: continue
                    samples.append(({name: content}, "maven" if name == "pom.xml" else "gradle", {"workspace": str(path)}))
                    if len(samples) >= (ex.limit or 3000): break
                if len(samples) >= (ex.limit or 3000): break
            if len(samples) >= (ex.limit or 3000): break
    # Ca biên v1/v2, ưu tiên release và giới hạn ba thư mục cha.
    synthetic = [({"pom.xml": f"<project><{key}>{value}</{key}></project>"}, "maven", {}) for key, value in itertools.product(("maven.compiler.release", "maven.compiler.target", "java.version"), ("8", "1.8", "17", "${java.version}", "\x1c21\x1f"))]
    synthetic += [({"build.gradle.kts": text}, "gradle", {}) for text in ("sourceCompatibility = 1.8", "targetCompatibility = JavaVersion.VERSION_1_8", "sourceCompatibility = '21'", "options.release.set(8)", "sourceCompatibility = 8\noptions.release = 8")]
    synthetic += [({}, "maven", {}), ({"module/pom.xml": "<java.version>8</java.version>", "pom.xml": "<release>8</release>"}, "maven", {"module": "module"})]
    for i, (files, tool, origin) in enumerate(synthetic + samples):
        target = work / "targets" / str(i); write_files(target, files)
        module = target / origin.get("module", ".")
        input_ = {"files": files, "module": origin.get("module", "."), "tool": tool}
        for profile, function in (("v1", runner._java_target), ("v2", v2._java_target)):
            ex.add("java_target", {**input_, "profile": profile}, lambda function=function: function(module, tool))
        ex.add("uses_release_flag", input_, lambda: v2._uses_release_flag(module))
    return len(samples)


def export_failures(ex):
    logs = PYTHON_ROOT / "runs/quang-shard-03-20260925T131230Z/logs"
    count = 0
    for task_dir in sorted(logs.iterdir()):
        for path in sorted(task_dir.rglob("*.log")):
            text = path.read_text(encoding="utf-8", errors="replace")
            for stage in ("main_compile", "test_compile"):
                attempt = BuildAttempt(stage, "", [], 1, "TIMEOUT after " in text, 0, str(path))
                ex.add("failure_status", {"text": text, "stage": stage, "timed_out": attempt.timed_out}, lambda: runner._failure_status(attempt, stage))
            count += 1
            if ex.limit and count >= ex.limit: return count
    # Ca ưu tiên marker/timeout và file log mất.
    return count


def export_combinations(ex, work):
    statuses = list(PreflightStatus)
    for observed in [[s] for s in statuses] + list(itertools.combinations(statuses, 2)) + [["UNKNOWN"], []]:
        ex.add("primary_status", list(observed), lambda: policy.select_primary_status(observed))
    for status, mode, rev in itertools.product(statuses, RunMode, RevisionStatus):
        ex.add("eligibility", [status, mode, rev], lambda: policy.derive_eligibility(status, mode, rev))
    frameworks = ["", "org.junit.jupiter.api.Test", "org.junit.Test", "junit.framework.TestCase", "org.testng.annotations.Test"]
    for source, test in itertools.product(frameworks, repeat=2):
        ex.add("framework", [source, test], lambda: runner._framework(source, test))
    for source_set, kind, top, abstract, generated in itertools.product(("production", "test", "unknown"), ("class", "interface", "annotation", "enum", "record"), (True, False), (True, False), (True, False)):
        java = java_ast.JavaClass("acme", "X", kind, {"abstract"} if abstract else {"public"}, set(), [], [], source_set=source_set, tags={"GENERATED_SOURCE", "LOW_LOGIC"} if generated else {"LOW_LOGIC"}, is_top_level=top)
        ex.add("class_policy", serialize(java), lambda: runner._class_policy(java))
    for framework, kind, static, package, profile, overloaded in itertools.product(("junit4", "junit5", "testng", "unknown"), ("method", "constructor"), (True, False), ("", "acme"), ("v1", "v2"), (True, False)):
        api = java_ast.JavaApi(kind, "X" if kind == "constructor" else "run", "(Inner value, T v)", static, "signature")
        java = java_ast.JavaClass(package, "X", "class", {"public"}, set(), [api, dataclasses.replace(api, parameters="(String value, K v)")] if overloaded else [api], [])
        source = "import acme.Inner;\nimport java.util.*;\nimport static acme.Util.run;\nclass X { class Inner {} }"
        v2._probe_phase.source = source
        ex.add("probe_source", {"java": serialize(java), "api": serialize(api), "class_name": "Probe", "framework": framework, "profile": profile, "source": source}, lambda: (runner._probe_source if profile == "v1" else v2._probe_source)(java, api, "Probe", framework))
    for relative in ("src/main/java/X.java", "proj/src/main/java/X.java", "src\\main\\java\\X.java", "X.java", "", "/etc/passwd", "../secret.java", "src/../secret.java", "C:X.java", "C:/X.java", "\\\\server\\share\\X.java", "src/\0X.java", "./src//main/java/X.java"):
        workspace = work / "paths"; workspace.mkdir(exist_ok=True)
        source = workspace / "src/main/java/X.java"; source.parent.mkdir(parents=True, exist_ok=True); source.write_text("class X {}", encoding="utf-8")
        value = {"relative": relative, "files": ["src/main/java/X.java"]}
        ex.add("path_candidates", value, lambda: [path.relative_to(workspace).as_posix() for path in runner._path_candidates(workspace, relative)])
        ex.add("resolve_path", value, lambda: (path.relative_to(workspace).as_posix() if (path := runner._resolve_path(workspace, relative)) else None))
    for class_path in ("src/main/java/acme/X.java", "a/src/main/java/X.java", "a/SRC/MAIN/JAVA/X.java", "custom/X.java", "src/main/kotlin/X.kt"):
        workspace = work / "paths"
        ex.add("test_source_root", {"class_file": class_path, "module_root": "module"}, lambda: runner._test_source_root(workspace / class_path, workspace / "module").relative_to(workspace).as_posix())
    # Oracle chính xác cho whitespace và các thao tác chuỗi.
    for code in list(range(0x100)) + [0x1680, 0x2000, 0x2007, 0x2028, 0x2029, 0x202f, 0x205f, 0x3000, 0xfeff]:
        ex.add("isspace", code, lambda: chr(code).isspace())
    for text, chars, maximum in itertools.product((" a b ", "a..b..", "\x1c\u00a0 text \x1f", "", "a a a"), (" ", ".", ";", "a"), (-1, 0, 1, 2)):
        for op in ("strip", "removeprefix", "rstrip", "split", "rsplit"):
            args = {"text": text, "chars": chars, "max_split": maximum, "operation": op}
            ex.add("pystr", args, lambda: getattr(text, op)(*([] if op == "strip" else [chars, maximum] if op in ("split", "rsplit") else [chars])))
    for word in ("A", "Đ", "²", "a\u0301", "a\u200c", "\u2160", "_", "$", "中"):
        for pattern in (r"\w+", r"\bpublic\b", r"@[\w.]+(?:\([^)]*\))?", r"\b(?:class|interface)\s+(\w+)"):
            text = f"@{word} public class {word} {{}}"
            import re
            ex.add("regex", {"pattern": pattern, "text": text}, lambda: re.findall(pattern, text))
    for text in ("Could not resolve dependency", "could not transfer artifact", "could not find artifact", "unable to resolve dependency", "read timed out", "connection timed out", "unknown host", "release version", "source option", "target option", "invalid target release", "unsupported class file major version", "java_home", "no such file or directory could not resolve", "EXECUTABLE_NOT_FOUND", "is not recognized", "ordinary error", ""):
        path = work / "failure.log"; path.write_text(text, encoding="utf-8")
        for stage, timed_out in itertools.product(("main_compile", "test_compile", "probe_compile"), (False, True)):
            attempt = BuildAttempt(stage, "", [], 1, timed_out, 0, str(path))
            ex.add("failure_status", {"text": text, "stage": stage, "timed_out": timed_out}, lambda: runner._failure_status(attempt, stage))
    for value in ({"z": 1, "a": "dữ liệu\n\x1c\u2028", "float": 0.0}, {"x": [True, None, "quote\"", 1.0, 1e-5, 1e16, -0.0]}, {"e": 1.2345678901234567e-20}):
        for compact, indent in ((False, None), (True, None), (False, 2)):
            ex.add("json_format", {"value": value, "compact": compact, "indented": indent is not None}, lambda: json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":") if compact else None, indent=indent))


def export_revision_maps(ex, work):
    good = {"task_id": "task", "checkout_sha": "a" * 40, "revision_provenance": "dataset_record", "revision_verification_status": "UPSTREAM_PINNED", "evidence_ref": "dataset:1"}
    sets = [[good], [good, good], [good, {**good, "checkout_sha": "b" * 40}], [good, {**good, "evidence_ref": "other"}], [good, {**good, "revision_provenance": "upstream_metadata"}], [{**good, "checkout_sha": "ABCDEF0123" * 4}], []]
    for key, values in {"checkout_sha": ["z" * 40, "a" * 39, "a" * 41, "a" * 39 + "\n", "ａ" * 40], "revision_provenance": ["manual", "git_history"], "revision_verification_status": ["CONTENT_MATCHED", "UNVERIFIED"], "evidence_ref": ["", " \x1c \x1f "], "task_id": [""]}.items():
        sets.extend([[{**good, key: value}] for value in values])
    for i, rows in enumerate(sets):
        for extension in ("csv", "jsonl"):
            path = work / f"map-{i}.{extension}"
            if extension == "csv":
                with path.open("w", encoding="utf-8-sig", newline="") as handle:
                    writer = csv.DictWriter(handle, fieldnames=list(good)); writer.writeheader(); writer.writerows(rows)
            else: path.write_text("".join(json.dumps(row, ensure_ascii=False) + "\n" for row in rows), encoding="utf-8")
            ex.add("revision_map", {"extension": extension, "text": path.read_text(encoding="utf-8")}, lambda: revision.load_revision_map(path))
    ex.add("revision_map", None, lambda: revision.load_revision_map(None))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--limit", type=int, default=0)
    parser.add_argument("--out", type=Path, default=ROOT / "tests/golden")
    args = parser.parse_args()
    if args.limit < 0: parser.error("--limit phải >= 0")
    output = safe_output(args.out)
    full = safe_output(ROOT / ".work/golden-full")
    work = safe_output(ROOT / ".work/golden_inputs")
    work.mkdir(parents=True, exist_ok=True)
    ex = Exporter(full, args.limit)
    try:
        export_tasks(ex)
        export_records(ex, random.Random(args.seed), work)
        real_build_files = export_targets(ex, work)
        export_combinations(ex, work)
        for input_, output_ in unicode_cases():
            ex.add("regex_unicode", input_, lambda: output_)
        real_logs = export_failures(ex)
        export_revision_maps(ex, work)
    finally:
        ex.close()
    manifest = {"schema_version": 1, "seed": args.seed, "limit": args.limit, "python_version": sys.version.split()[0], "tree_sitter": importlib.metadata.version("tree-sitter"), "tree_sitter_java": importlib.metadata.version("tree-sitter-java"), "sources": ex.sources,
                "credential_redaction": "Octopus API-key-like strings replaced in generated payloads before Python oracle evaluation",
                "real_build_files": real_build_files, "real_logs": real_logs, "files": {}}
    for name in sorted(ex.counts):
        path = full / f"{name}.jsonl"
        manifest["files"][path.name] = {"count": ex.counts[name], "sha256": util.sha256_file(path), "bytes": path.stat().st_size}
    (full / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, sort_keys=True, indent=2) + "\n", encoding="utf-8", newline="\n")
    output.mkdir(parents=True, exist_ok=True)
    subset = sum(row["bytes"] for row in manifest["files"].values()) > 50 * 1024 * 1024 and output != full
    retained = json.loads(json.dumps(manifest)); retained["subset"] = subset
    if output != full:
        for name, stats in manifest["files"].items():
            source, target = full / name, output / name
            if not subset: target.write_bytes(source.read_bytes())
            else:
                # Reservoir cố định, mỗi hàm tối đa 1.000 mẫu; giữ metadata tập đầy đủ.
                rng, reservoir = random.Random(args.seed), []
                with source.open(encoding="utf-8") as handle:
                    for i, line in enumerate(handle):
                        if i < 1000: reservoir.append((i, line))
                        elif (slot := rng.randrange(i + 1)) < 1000: reservoir[slot] = (i, line)
                target.write_text("".join(line for _, line in sorted(reservoir)), encoding="utf-8", newline="\n")
            retained["files"][name] = {"count": min(stats["count"], 1000) if subset else stats["count"], "sha256": util.sha256_file(target), "bytes": target.stat().st_size, "full_count": stats["count"], "full_sha256": stats["sha256"]}
        (output / "manifest.json").write_text(json.dumps(retained, ensure_ascii=False, sort_keys=True, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(json.dumps({"counts": ex.counts, "real_build_files": real_build_files, "real_logs": real_logs, "subset": subset}, sort_keys=True))


if __name__ == "__main__":
    main()
