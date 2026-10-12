# 01. Cấu hình và định dạng dữ liệu

Mọi file JSON dùng UTF-8, không BOM, xuống dòng LF, tên trường `snake_case`. JSONL: mỗi dòng một object. Mọi file do tool ghi phải ghi **nguyên tử** (ghi `<file>.tmp` rồi đổi tên), trừ `ledger.jsonl` (chỉ ghi thêm, flush sau mỗi dòng). Hash là SHA-256 hex chữ thường của **byte** của file.

## 1. Cấu hình thực nghiệm (`experiments/<experiment_id>.json`, commit)

```json
{
  "schema_version": 1,
  "experiment_id": "pilot-20261015",
  "kind": "pilot",
  "manifests": [
    {"dataset": "classes2test", "path": "../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl",
     "sha256": "687fab7c…", "repair": true},
    {"dataset": "gitbug-java",  "path": "../evaluation_manifest/pilot/gitbug_java_pilot_9_seed42.jsonl",
     "sha256": "6754b1a5…", "repair": false}
  ],
  "models": [
    {"name": "gpt-6-luna", "expected_model_id_prefix": "gpt-6-luna",
     "params": {"reasoning_effort": "none", "temperature": 0, "max_tokens": 8192, "seed": 42}}
  ],
  "generation_strategies": ["zero_shot", "few_shot", "zero_shot_project_aware"],
  "prompt_set": {"dir": "prompts", "version": "v1"},
  "repair": {"mechanisms": ["fixed", "adaptive"], "n_max": 10, "early_stop": false},
  "n_rule": "share95",
  "build": {
    "step_timeout_seconds": 900, "tool_version_timeout_seconds": 120,
    "maven_extra_flags": ["-Dcheckstyle.skip=true", "…"], "gradle_extra_flags": ["--no-daemon", "-x", "checkstyleMain", "…"],
    "baseline_runs": 2
  },
  "comparison_systems": [{"name": "evosuite", "version": "1.2.0", "seed": 42, "config": "default"}],
  "quality": {"jacoco": "0.8.12", "pit": "1.17.4", "tsdetect": "2.2"},
  "test_class_suffix": "_ArrowTest"
}
```

- `experiment_sha256` = hash của file này; ghi vào phân công shard và `run_config.lock.json`.
- Khi đọc: MUST kiểm tra `sha256` của từng manifest; lệch thì dừng với lỗi `MANIFEST_HASH_MISMATCH`.
- `repair: false` nghĩa là dataset đó không chạy S2 (GitBug-Java trong pilot).
- Danh sách cờ build đầy đủ: `02-prepare-and-build.md` Mục 4.3.

## 2. Cấu hình máy (`machine.local.json`, không commit)

```json
{
  "schema_version": 1,
  "machine_name": "Dung-mac",
  "os": "osx-arm64",
  "arrow_root": "/Users/dungng/FPT/Ki 9/SET490/DoAnARROW/ARROW",
  "experiment": "experiments/pilot-20261015.json",
  "shard": "../shards/pilot-6/shard-01.json",
  "work_dir": "/Users/dung/arrow-repo",
  "runs_dir": "runs",
  "repo_policy": "commit_only_delete_after",
  "keep_failed_workspaces": false,
  "jdk_homes": {"8": "…/jdk-8/Contents/Home", "11": "…", "17": "…", "21": "…"},
  "maven_path": "/opt/homebrew/bin/mvn", "gradle_path": "/opt/homebrew/bin/gradle", "git_path": "/opt/homebrew/bin/git",
  "llm": {"proxy_mode": "auto_start", "proxy_url": "http://127.0.0.1:4000",
          "proxy_key_file": "litellm.config.local.yaml", "key_alias": "dung-mac", "rpm_limit": 60},
  "workers": {"build": 3, "llm": 2},
  "last_doctor": {"at": "2026-10-15T08:55:00Z", "status": "READY"}
}
```

- `repo_policy`: `commit_only_delete_after` | `commit_only_keep` | `full_history_keep`.
- Khóa API **không** có trong file này. `litellm.config.local.yaml` chứa khóa và `api_base` (endpoint) của máy:

```yaml
model_list:
  - model_name: gpt-6-luna
    litellm_params:
      model: openai/gpt-6-luna
      api_base: https://api.openai.com/v1
      api_key: sk-…            # khóa của máy này
general_settings:
  master_key: sk-local-…       # khóa bảo vệ proxy, sinh ngẫu nhiên khi setup
```

## 3. Phân công shard (`shards/<plan_id>/`, commit)

`plan.json`:

```json
{
  "schema_version": 1, "plan_id": "pilot-6", "experiment_sha256": "…",
  "manifests_sha256": {"classes2test": "687fab7c…", "gitbug-java": "6754b1a5…"},
  "coordinator": "Dung-mac",
  "shards": [{"shard_id": "shard-01", "owner": "Dung-mac", "file": "shard-01.json", "sha256": "…",
              "task_count": 10, "repository_count": 10, "estimated_build_seconds": 400}]
}
```

`shard-XX.json`:

```json
{"schema_version": 1, "plan_id": "pilot-6", "shard_id": "shard-01", "owner": "Dung-mac", "experiment_sha256": "…",
 "tasks": [{"dataset": "classes2test", "task_id": "51700398_fc0e38f96f8a8eb5", "repo_url": "https://github.com/ymanvieu/trading"}]}
```

Thuật toán `shard plan` (tất định):

1. Đọc mọi manifest của cấu hình thực nghiệm; gom task theo `repo_url`.
2. Trọng số repository = tổng `duration_seconds` của preflight các task của nó (thiếu thì dùng trung vị).
3. Sắp repository theo (trọng số giảm dần, `repo_url` tăng dần).
4. Lần lượt gán mỗi repository cho shard có (số task, tổng trọng số, `shard_id`) nhỏ nhất: **chia đều số task trước**, rồi mới cân bằng thời gian. Thời gian preflight gồm cả thời gian clone nên chỉ dùng để cân bằng phụ (lần thử đầu cân bằng theo thời gian trước làm một shard chỉ nhận 1 CUT).
5. Trong shard, task sắp theo (`dataset`, `task_id`). Owner gán theo thứ tự `--owners`.

## 4. Thư mục run (`runs/<run_id>/`, không commit)

`run_id` mặc định: `<plan_id>-<shard_id>-<machine_name>` (ví dụ `pilot-6-shard-01-Dung-mac`); run không chia shard: `<experiment_id>-<machine_name>`.

```
runs/<run_id>/
  run_config.lock.json
  ledger.jsonl
  control.json            ghi bởi phím tắt / lệnh control
  status.json             ghi bởi runner, đọc bởi dashboard
  events.jsonl            dòng sự kiện cho dashboard và --plain
  llm_cache/<sha256>.json
  tasks/<dataset>/<task_id>/
    task.json             bản ghi manifest + repo_context + đường dẫn workspace
    baseline.json
    repo_context.json
    gen/<model>/<strategy>/{prompt.json, response.json, usage.json, normalization.json, cand_0.java, verify_0.json}
    repair/<model>/<strategy>/excluded.json          lý do c0 bị loại khỏi bước sửa (nếu có)
    repair/<model>/<strategy>/<mechanism>/summary.json
    repair/<model>/<strategy>/<mechanism>/<attempt_id>/round_<k>/{prompt.json, response.json, usage.json, cand.java, normalization.json, verify.json, decision.json}
    evosuite/{tests/…, verify.json, evosuite_stats.json}
    quality/<system>/{jacoco.json, pit.json, tsdetect.json}
    logs/<job>__<step>.log, logs/reports/   (xem 08-logging.md)
  reports/
```

### 4.1. `run_config.lock.json`

Ghi lần đầu khi tạo run. Các lần chạy tiếp phải có cùng giá trị ở các trường đánh dấu `*`, lệch thì từ chối với `RUN_CONFIG_MISMATCH`.

| Trường | Ví dụ |
| --- | --- |
| `run_id`, `created_at` | |
| `experiment_sha256` `*`, `shard_sha256` `*` | |
| `tool_version` `*`, `tool_git_commit` `*`, `tool_git_dirty` | `0.1.0`, `a1b2c3…`, `false` |
| `prompt_hashes` `*` | `{"generation/zero_shot.md": "…", …}` |
| `machine_name`, `os`, `cpu_count`, `ram_gb` | |
| `jdk_versions` | `{"8": "1.8.0_504", …}` |
| `maven_version`, `gradle_version`, `git_version` | đo sau khi đặt `JAVA_HOME` |
| `llm_endpoint_host`, `llm_key_alias` | `api.openai.com`, `dung-mac` (không ghi khóa) |
| `build_flags` `*`, `n_max` `*`, `n_rule` `*` | `build_flags` gồm `maven`, `gradle`, `gradle_excluded_tasks`, `jvm` (tham số `JAVA_TOOL_OPTIONS`, `02` Mục 3 bước 6) |

### 4.2. `ledger.jsonl`

```json
{"job": {"dataset": "classes2test", "task_id": "…", "stage": "generate", "model": "gpt-6-luna",
         "strategy": "few_shot", "mechanism": null, "round": 0},
 "status": "DONE", "outcome": "COMPILE", "started_at": "…", "ended_at": "…",
 "llm_seconds": 9.1, "build_seconds": 38.4, "attempt": 1, "outputs": ["gen/gpt-6-luna/few_shot/verify_0.json"]}
```

- `stage`: `prepare` | `generate` | `repair` | `evosuite` | `quality`.
- `status`: `DONE` (có kết quả, kể cả kết quả thất bại của ứng viên) | `INFRA` (lỗi hạ tầng sau khi đã thử lại) | `SKIPPED` (ví dụ ứng viên đã VALID thì không sửa).
- Khóa job là `job` (6 trường + `round`). Một job đã có dòng `DONE` hoặc `SKIPPED` thì không chạy lại. Job `INFRA` chỉ chạy lại khi có `--rerun-status INFRA`; dòng mới được thêm, dòng cũ giữ nguyên, bản mới nhất có hiệu lực.
- Dòng `{"pause": {"from": "…", "to": "…"}}` ghi mỗi khoảng tạm dừng.

### 4.3. `control.json` và `status.json`

```json
{"command": "pause", "requested_at": "2026-10-15T09:40:00Z", "requested_by": "hotkey"}
```

`command`: `pause` | `resume` | `stop`. `requested_by`: `hotkey` | `cli` | `menu`.

```json
{"state": "PAUSED", "stage": "generate", "since": "…", "running_jobs": [], "done": 98, "total": 150,
 "elapsed_seconds": 6130, "paused_seconds": 312, "pid": 81234, "host": "Dung-mac", "heartbeat_at": "…"}
```

`state`: `RUNNING` | `PAUSING` | `PAUSED` | `STOPPING` | `STOPPED` | `DONE` | `FAILED`. Runner ghi `heartbeat_at` mỗi 5 giây; dashboard coi run là "không còn chạy" khi heartbeat cũ hơn 30 giây mà `state` vẫn là `RUNNING`.

### 4.4. `events.jsonl`

```json
{"at": "…", "level": "info", "stage": "generate", "task_id": "…", "strategy": "few_shot",
 "outcome": "COMPILE", "message": "cannot find symbol: method getTotal", "llm_seconds": 9.1, "build_seconds": 38.4}
```

## 5. Bundle export (`pilot_results/<plan_id>/<shard_id>-<machine_name>.zip`)

Nội dung: `runs/<run_id>/` trừ `llm_cache/` (đã có bản sao trong `response.json`), trừ workspace; log build: giữ đủ log của bước lỗi, bước đạt chỉ giữ 200 dòng cuối. Thêm `bundle_manifest.json` (danh sách file + hash, `run_id`, `experiment_sha256`, `shard_sha256`, số job theo trạng thái). Không có khóa API, không có `api_base` đầy đủ. Kích thước > 50 MB thì cảnh báo; > 95 MB thì từ chối và gợi ý bỏ log.

`merge` MUST kiểm tra: cùng `experiment_sha256` và `prompt_hashes`; mỗi `shard_id` của plan có đúng một bundle; hợp các task bằng đúng tập task của plan; không task nào xuất hiện hai lần; model id trả về (trong `usage.json`) cùng một giá trị trên mọi máy. Lỗi nào cũng dừng và in danh sách cụ thể.
