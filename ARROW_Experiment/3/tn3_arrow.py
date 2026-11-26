#!/usr/bin/env python3
"""TN3, nhánh ARROW, trên phần lớp của một máy. Thiết kế: cac_thuc_nghiem.md, mục "Adaptive Repair cho TN3"; arrow_TN3.md.

Không sinh test mới. Mỗi lớp × mỗi mô hình lấy bộ Zero-shot-project-aware (PA) của TN1 (tn3_arrow.jsonl, đã đóng băng):
    measure         (VALID ngay ở TN1)  arrow gen --c0 <bộ TN1> --no-repair, có đo         → source = initial
    repair          (chưa VALID)        arrow gen --c0 <bộ TN1> --rounds 10, cơ chế adaptive, cùng mô hình đã sinh c0,
                                        không dừng sớm, có đo                               → source = adaptive
    invalid_output  không chạy; ghi một dòng INVALID_OUTPUT (tính là thất bại, không sinh lại, không sửa)
JaCoCo/PIT/tsDetect do arrow đo khi bộ cuối VALID. Chạy trên repo gốc (--repo/--ref) như TN1, TN2.
Kết quả: results/<máy>/arrow_runs.jsonl (mỗi (lớp, mô hình) một dòng), logs/arrow/

    python3 tn3_arrow.py --machine dung --workers 3 --arrow ~/.local/share/arrow-dev/build-tn2/arrow
    Chạy khói:  --task <task_id> [--model gpt6luna]      Chạy tiếp: chạy lại đúng lệnh; --retry-infra cho dòng INFRA
    Một nhà cung cấp đang lỗi: --model gpt6luna (chỉ chạy mô hình đó), sau đó chạy lại không có --model
    Chạy lại 1 bộ và ghi đè kết quả thật: --redo <task_id> [--model qwen3coder-or]
"""
import argparse          # tham số dòng lệnh
import os                # đường dẫn
import subprocess        # gọi "arrow config set"

import tn3_common as c   # thư viện chung TN3
import tn3_lists         # đọc danh sách đóng băng

SYSTEM = "arrow"                     # tên hệ thống ghi vào mỗi dòng kết quả
N_ROUNDS = 10                        # số vòng sửa tối đa (N chọn ở pilot)
MECHANISM = "adaptive"               # cơ chế sửa Adaptive (nhánh A)
# Trạng thái TN1 hợp lệ để báo cho arrow (--c0-state): bộ đang hỏng ở bước nào
C0_STATES = {"COMPILE", "UNKNOWN", "DISCOVERY", "RUNTIME", "ASSERTION", "REGRESSION"}


def key_of(rec):
    """Khóa của một dòng kết quả: (lớp, mô hình). Dùng để biết bộ nào đã chạy xong."""
    return (rec["task_id"], rec["model"])


def arrow_cfg(arrow, home, key, value):
    """Đặt một cấu hình của arrow trong home của worker: arrow config set <key> <value> --home <home>."""
    subprocess.run([arrow, "config", "set", key, value, "--home", home, "--plain"],
                   check=True, stdout=subprocess.DEVNULL, stdin=subprocess.DEVNULL)


def run_one(item, home, ctx):
    """Chạy một bộ ARROW (một lớp × một mô hình) rồi ghi một dòng vào arrow_runs.jsonl."""
    tid, model, action = item["task_id"], item["model"], item["action"]
    cl = ctx["classes"][tid]                                            # thông tin lớp (repo, commit, FQN)
    task = {k: cl[k] for k in ("task_id", "dataset", "repo_url", "checkout_sha", "class_fqn")}
    rec = dict(task, system=SYSTEM, model=model, action=action, machine=ctx["machine"], tool_commit=ctx["commit"],
               tn1_result=item.get("tn1_result"),                       # trạng thái bộ test ở TN1
               tn1_total_s=item.get("tn1_total_s"), tn1_llm_s=item.get("tn1_llm_s"),   # thời gian TN1 (cho EvoSuite cùng ngân sách)
               c0_path=item.get("c0_path"),
               source={"measure": "initial", "repair": "adaptive"}.get(action))      # cột source của bảng RQ3
    if action == "invalid_output":                                      # TN1 không có file test → không chạy gì
        rec.update(status="DONE", result="INVALID_OUTPUT", final_state="INVALID_OUTPUT",
                   tn1_invalid_reason=item.get("tn1_invalid_reason"), finished_at=c.now_iso())
        c.append_jsonl(ctx["runs_path"], rec, c.JSONL_LOCK)
        print(f"[DONE] {tid} {model}: INVALID_OUTPUT (không chạy)", flush=True)
        return
    if action == "missing_tn1":                                         # TN1 thiếu dòng (không nên xảy ra) → INFRA
        rec.update(status="INFRA", result="MISSING_TN1", finished_at=c.now_iso())
        c.append_jsonl(ctx["runs_path"], rec, c.JSONL_LOCK)
        return
    c0 = os.path.join(c.TN1_DIR, item["c0_path"])                       # file test c0 của TN1
    if c.sha256_file(c0) != item["c0_sha256"]:                          # phải đúng bản đã đóng băng
        raise SystemExit(f"File c0 khác bản đóng băng: {c0}. Dừng.")
    args = ["gen", task["class_fqn"],
            "--repo", task["repo_url"], "--ref", task["checkout_sha"],   # chạy trên repo gốc, đúng commit
            "--c0", c0,                                                 # dùng bộ test TN1, không sinh mới
            "--c0-model", model,                                        # mô hình đã sinh c0
            "--model", model,                                           # mô hình sửa = mô hình sinh (không sửa chéo)
            "--yes"]
    if action == "measure":
        args.append("--no-repair")                                      # VALID sẵn: chỉ xác minh + đo
    else:
        args += ["--rounds", str(N_ROUNDS)]                             # sửa tối đa 10 vòng
        if item.get("tn1_result") in C0_STATES:
            args += ["--c0-state", item["tn1_result"]]                  # báo arrow bộ đang hỏng ở bước nào (khỏi xác minh lại c0)
    logs = os.path.join(ctx["root"], "logs", SYSTEM)
    data, code, seconds = c.run_arrow(ctx["arrow"], args, home, os.path.join(logs, f"{tid}__{model}"), 6 * 60 * 60)   # tối đa 6 giờ
    res = c.parse_gen(data, code, seconds, task)
    rec.update(res)
    if res.get("run_dir"):
        # Kiểm arrow đã chạy đúng cấu hình (đọc lại run.json); sai thì đánh BAD_CONFIG để không lẫn kết quả
        cfg = (c.read_json(os.path.join(res["run_dir"], "run.json")) or {}).get("config") or {}
        bad = []
        if action == "repair":
            if cfg.get("mechanism") != MECHANISM:
                bad.append(f"mechanism={cfg.get('mechanism')}")
            if cfg.get("rounds") != N_ROUNDS:
                bad.append(f"rounds={cfg.get('rounds')}")
            if cfg.get("early_stop_p") is not None:                     # phải không dừng sớm
                bad.append(f"early_stop_p={cfg.get('early_stop_p')}")
        if cfg.get("model") not in (None, model):
            bad.append(f"model={cfg.get('model')}")
        if bad:
            rec.update(status="BAD_CONFIG", detail=", ".join(bad))
        # thời gian sửa (block3/final.json): dùng cho bảng chi phí và EvoSuite cùng ngân sách
        fin = c.read_json(os.path.join(c.target_dir(res["run_dir"], task["class_fqn"]), "block3", "final.json")) or {}
        rec.update(repair_seconds=fin.get("repair_seconds"), repair_llm_seconds=fin.get("llm_seconds"),
                   repair_build_seconds=fin.get("build_seconds"), final_round=fin.get("final_round"))
        m = res.get("metrics") or {}                                    # chỉ số đo (có khi bộ cuối VALID)
        rec.update(line=m.get("line"), branch=m.get("branch"), method=m.get("method"), mutation=m.get("mutation"),
                   test_strength=m.get("test_strength"), smell_density=m.get("smell_density"))
    rec["final_state"] = rec.get("result")
    rec["finished_at"] = c.now_iso()
    c.base.mark_fatal(rec)                         # hết tiền / mô hình không dùng được → STOPPED, dừng cả lô như TN1, TN2
    c.append_jsonl(ctx["runs_path"], rec, c.JSONL_LOCK)                 # ghi SAU mark_fatal để dòng STOPPED được chạy lại
    print(f"[{rec['status']}] {tid} {model} {action}: {rec.get('result')} (vòng {rec.get('rounds_run')}, "
          f"mut {rec.get('mutation')})", flush=True)


def main():
    ap = argparse.ArgumentParser(description="TN3: nhánh ARROW trên phần lớp của một máy.")
    ap.add_argument("--machine", required=True, help="tên máy: dung / chinh / kieuanh / quang / han")
    ap.add_argument("--part", help="chạy phần của máy khác (mặc định = --machine)")
    ap.add_argument("--workers", type=int, default=3, help="số luồng song song")
    ap.add_argument("--arrow", default="arrow", help="đường dẫn file arrow 203fb8e")
    ap.add_argument("--base-home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")))
    ap.add_argument("--task", action="append", help="chạy khói: chỉ lớp này (lặp lại được)")
    ap.add_argument("--model", action="append", help="chỉ chạy mô hình này (khi một nhà cung cấp đang lỗi)")
    ap.add_argument("--redo", action="append", help="CHẠY LẠI lớp này và XÓA SẠCH kết quả, log, bộ test của lần chạy cũ (lặp lại được)")
    ap.add_argument("--limit", type=int, help="chạy khói: N lớp đầu của phần")
    ap.add_argument("--retry-infra", action="store_true", help="chạy lại các dòng lỗi hạ tầng")
    ap.add_argument("--homes-dir", help="nơi đặt home của worker (mặc định results/<máy>/homes)")
    a = ap.parse_args()

    smoke = bool(a.task or a.limit)          # --model chỉ lọc mô hình, không tính là chạy khói
    root = c.out_root(a.machine, smoke)      # results/<máy> hoặc results/<máy>-smoke
    os.makedirs(root, exist_ok=True)
    version, commit = c.check_arrow(a.arrow)                            # phải là arrow 203fb8e
    classes, arrow_items = tn3_lists.load_frozen()                      # danh sách đã đóng băng (kiểm sha256)
    mine = {cl["task_id"]: cl for cl in c.select_classes(classes, a.part or a.machine, a.redo or a.task, a.limit)}
    base_cfg = c.base.load_config(a.base_home)                          # cấu hình arrow gốc (~/.arrow/config.json)
    models = sorted({x["model"] for x in arrow_items})
    model_info = c.base.check_models(base_cfg, models)                  # 2 mô hình phải khai báo trong arrow
    # Bộ prompt sửa phải là bộ của nhóm (giống TN1, TN2)
    override = (base_cfg.get("prompts") or {}).get("override_dir")
    same = lambda x, y: os.path.normcase(os.path.realpath(x)) == os.path.normcase(os.path.realpath(y))
    if not override or not same(override, c.base.PROMPT_DIR):
        raise SystemExit(f"ARROW_HOME gốc chưa dùng bộ prompt của nhóm. Chạy:\n"
                         f"  arrow config set prompts.override_dir \"{c.base.PROMPT_DIR}\"")
    prompt_sha = {}                                                     # sha256 từng file prompt, để chứng minh prompt không đổi
    for r, _, files in os.walk(c.base.PROMPT_DIR):
        for name in sorted(files):
            if name.endswith(".md"):
                p = os.path.join(r, name)
                prompt_sha[os.path.relpath(p, c.base.PROMPT_DIR).replace(os.sep, "/")] = c.sha256_file(p)
    c.lock_invocation(root, "tn3_arrow.py", {"tool_version": version, "tool_commit": commit, "machine": a.machine,
                                             "part": a.part or a.machine, "workers": a.workers, "models": model_info,
                                             "rounds": N_ROUNDS, "mechanism": MECHANISM, "early_stop_p": None,
                                             "prompt_sha256": prompt_sha})
    runs_path = os.path.join(root, "arrow_runs.jsonl")
    if a.redo:   # --redo: xóa dòng cũ của (lớp, mô hình) cần chạy lại → bên dưới coi là chưa làm
        logs = os.path.join(root, "logs", SYSTEM)
        n = c.redo_remove(runs_path, lambda r: r["task_id"] in a.redo and (not a.model or r["model"] in a.model),
                          lambda r: [os.path.join(logs, f"{r['task_id']}__{r['model']}.*")])   # log .json/.err cũ
        print(f"[redo] đã xóa sạch {n} kết quả cũ (dòng, log, thư mục run)")
    latest = c.latest_by(runs_path, key_of)                             # dòng mới nhất của mỗi bộ đã chạy
    items = [x for x in arrow_items if x["task_id"] in mine and (not a.model or x["model"] in a.model)]
    todo = []
    for x in items:
        rec = latest.get(key_of(x))
        # cần chạy: chưa có dòng; hoặc dòng STOPPED (dừng vì hết tiền / mô hình lỗi); hoặc --retry-infra và dòng không DONE
        if rec is None or rec.get("status") == "STOPPED" or (a.retry_infra and rec.get("status") != "DONE"):
            todo.append(x)
    print(f"TN3 ARROW · máy {a.machine} · {len(todo)}/{len(items)} bộ cần chạy · {a.workers} worker")
    if not todo:
        return
    # repair (lâu) chạy trước, measure sau; trong mỗi nhóm xếp theo lớp rồi mô hình
    todo.sort(key=lambda x: ({"repair": 0, "measure": 1}.get(x["action"], 2), x["task_id"], x["model"]))
    c.base.keep_awake()                                                 # chặn máy ngủ khi đang chạy
    pool = c.HomePool(a.base_home, a.homes_dir or c.default_homes_root(a.machine, smoke), "arrow", a.workers)
    for h in list(pool.q.queue):                                        # đặt cơ chế sửa cho mọi home
        arrow_cfg(a.arrow, h, "repair.mechanism", MECHANISM)
        arrow_cfg(a.arrow, h, "repair.early_stop_p", "null")            # không dừng sớm
    ctx = {"arrow": a.arrow, "commit": commit, "machine": a.machine, "root": root, "runs_path": runs_path, "classes": mine}
    c.run_jobs(todo, a.workers, lambda x, home: run_one(x, home, ctx), pool)
    if c.base.STOP.is_set():                                            # bị dừng vì hết tiền / mô hình lỗi
        c.base.notify_stop(root, " ".join(["python3"] + __import__("sys").argv))   # ghi DA_DUNG.txt kèm lệnh chạy lại
    print("Xong. Kết quả:", runs_path)


if __name__ == "__main__":
    main()
