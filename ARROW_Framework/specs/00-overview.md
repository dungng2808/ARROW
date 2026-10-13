# 00. Tổng quan ARROW Framework

**Trạng thái:** spec chờ duyệt (13/10/2026). Chưa có code. Code C# console viết sau khi spec được duyệt.

ARROW Framework là công cụ dòng lệnh (C# console, .NET 8) chạy toàn bộ quy trình ARROW trên một tập CUT (class under test) lấy từ manifest: phân tích repository, sinh unit test bằng LLM, xác minh và sửa test, đánh giá chất lượng bộ test. Framework gồm **4 khối**, mỗi khối có một file spec riêng:

| Khối | Spec | Việc chính | Đầu ra chính |
| --- | --- | --- | --- |
| 1. Repository-Aware | `01-repository-aware.md` | Tải đúng commit, phân tích 6 thông tin cấu hình, chạy baseline | Repository Metadata **M**, baseline |
| 2. LLM-Based Test Generation | `02-llm-test-generation.md` | Dựng prompt, chọn quy trình prompting, gọi model qua lớp trừu tượng, tách mã test | Candidate Unit Test **T** |
| 3. Adaptive Repair Strategy | `03-adaptive-repair.md` | Biên dịch và chạy test (5 bước xác minh), chẩn đoán lỗi, quyết định sửa tiếp, chốt bộ test | Final Test Suite **S** và log từng vòng |
| 4. Automated Test Suite Assessment | `04-test-suite-assessment.md` | Đo coverage (JaCoCo), mutation (PIT), test smell (tsDetect), tổng hợp báo cáo | Unified Metrics, báo cáo |

## 1. Luồng dữ liệu

```
manifest (CUT) ──► [Khối 1] ──M, baseline──► [Khối 2] ──T──► [Khối 3] ──S (VALID)──► [Khối 4] ──► báo cáo
                                                ▲              │
                                                └─RepairRequest┘   (vòng sửa, nét đứt trong hình)
```

1. Khối 1 nhận một bản ghi manifest, tạo workspace đúng commit, trả về **M** (metadata repository) và **baseline** (test sẵn có pass ổn định).
2. Khối 2 nhận M và CUT, sinh **T** cho mỗi tổ hợp (model × quy trình prompting).
3. Khối 3 xác minh T qua 5 bước. T đạt VALID thì chuyển sang khối 4. T không đạt thì chẩn đoán lỗi; nếu còn được sửa, thuật toán sửa tạo **RepairRequest** gửi ngược lại khối 2 (cùng lớp gọi model) để nhận T mới, rồi xác minh lại. Hết lượt sửa thì chốt bộ test.
4. Khối 4 đo chất lượng các bộ test VALID và tổng hợp mọi số liệu (xác minh, sửa, chất lượng, chi phí) thành báo cáo.

Ảnh framework chỉ là cái nhìn tổng quan. Spec này bổ sung những phần thực nghiệm cần mà ảnh không vẽ: **Fixed Repair** (đối chứng của RQ2) chạy cùng vòng lặp của khối 3, và **EvoSuite** (hệ thống so sánh của RQ3) sinh bộ test được xác minh bằng khối 3 rồi đo bằng khối 4.

## 2. Khái niệm và định danh

| Khái niệm | Định nghĩa | Định danh |
| --- | --- | --- |
| Task | Một CUT trong manifest | `task_id` (lấy từ manifest), kèm `dataset` |
| Candidate | Một test do khối 2 sinh cho một task | `(task_id, model, strategy)` |
| Chain | Quá trình sửa một candidate bằng một cơ chế | `(task_id, model, strategy, mechanism)`; `mechanism` ∈ `fixed`, `adaptive` |
| Round | Một vòng sửa trong chain | `round` = 1…N; `round 0` là candidate ban đầu (c0) |
| Suite | Bộ test cuối cùng đem đo ở khối 4 | `suite_id` = `<model>__<strategy>__<mechanism>` hoặc `<model>__<strategy>__initial` (VALID ngay lần sinh) hoặc `evosuite` |
| Run | Một lần chạy framework trên một tập task | `run_id` (người dùng đặt hoặc tự sinh `run-<yyyyMMdd-HHmm>`) |

### 2.1. Thuật ngữ

| Thuật ngữ | Nghĩa | Ví dụ |
| --- | --- | --- |
| CUT | *Class under test*: class cần sinh test | `OrderService` |
| FQN | *Fully qualified name*: tên đầy đủ của class gồm package | `com.acme.shop.OrderService` |
| Manifest | File `.jsonl` liệt kê các CUT của dataset, mỗi dòng một CUT (repository, commit, đường dẫn) | `evaluation_manifest/pilot/classes2test_pilot_50_seed42_v2.jsonl` |
| Revision status | Mức chắc chắn của commit, trường `revision_verification_status`: `UPSTREAM_PINNED` (đúng commit dataset ghi) hoặc `CONTENT_MATCHED` (preflight tìm commit có nội dung khớp). Trường `revision_provenance` cho biết commit lấy từ đâu: `dataset_record` hoặc `git_history` | Classes2Test: `git_history` + `CONTENT_MATCHED` |
| Build root | Thư mục chạy Maven/Gradle (có file build); có thể là thư mục con của repository. `module_path` của manifest tính từ đây | `3rdParty/libsodium-jni-2.0.1` |
| Snapshot | Bản sao workspace đã biên dịch, chưa chạy test; mỗi lần chạy test dùng một bản sao mới của nó | |
| Mirror | Bản sao git của repository trên máy, dùng chung cho mọi run | `repo/mirrors/github.com/acme/shop.git` |
| Workspace | Thư mục mã nguồn ở đúng commit để build; mỗi lần chạy test dùng một bản sao mới của snapshot | `repo/ws/<run>/<task>/…` |
| Baseline | Kết quả chạy test sẵn có của module **trước khi** thêm test sinh ra | 42 test pass ở cả 2 lần chạy |
| M | Repository Metadata: 6 thông tin cấu hình của khối 1 | build tool Maven, Java 8, JUnit 4… |
| T, c0 | Candidate Unit Test; c0 là bản do khối 2 sinh lần đầu (chưa sửa) | `OrderService_ArrowTest.java` |
| Chain | Quá trình sửa một candidate bằng một cơ chế (Fixed hoặc Adaptive), tối đa N vòng | |
| Checkpoint | Bản tốt nhất tới hiện tại trong chain Adaptive; vòng sau sửa từ bản này | |
| Error signature | Lỗi đã chuẩn hóa (bỏ số dòng, tên, giá trị) để nhận ra lỗi lặp | `COMPILE \| cannot find symbol \| method` |
| Code hash | SHA-256 của mã test sau khi bỏ comment và khoảng trắng; để nhận ra mã trùng | `sha256:9f3c…` |
| INFRA | Lỗi hạ tầng: không phải lỗi của test (timeout build, mạng, công cụ) | Không tải được dependency |
| EXCLUDED | Task không dùng được, bị loại kèm lý do | Repository không còn commit |

Ví dụ xuyên suốt cả 4 khối dùng một CUT minh họa (số liệu giả định): repository `https://github.com/acme/shop`, module `shop-core`, CUT `com.acme.shop.OrderService`, Maven, Java 8, JUnit 4 + Mockito. Mỗi spec 01–04 có mục "Ví dụ" theo CUT này.

**Trạng thái xác minh** (định nghĩa đầy đủ ở spec 03): `COMPILE < UNKNOWN < DISCOVERY < RUNTIME < ASSERTION < REGRESSION < VALID`. `INFRA` (timeout, lỗi công cụ, lỗi mạng) và `EXCLUDED` (task không dùng được) nằm ngoài chuỗi.

## 3. Cấu hình

Ba file, tách phần dùng chung với phần riêng của từng máy.

### 3.1. `experiments/<tên>.json` (dùng chung, commit)

```json
{
  "experiment_id": "pilot-v1",
  "manifests": [
    { "dataset": "classes2test", "path": "../evaluation_manifest/pilot/classes2test_pilot_50_seed42_v2.jsonl", "sha256": "…", "repair": true },
    { "dataset": "gitbug-java",  "path": "../evaluation_manifest/pilot/gitbug_java_pilot_9_seed42.jsonl", "sha256": "…", "repair": false }
  ],
  "models": [
    { "name": "gpt-6-luna", "expected_model_id_prefix": "gpt-6-luna",
      "params": { "reasoning_effort": "none", "temperature": 0, "max_tokens": 8192, "seed": 42 },
      "price_usd_per_million": { "input": 0.05, "output": 0.25 } }
  ],
  "strategies": ["zero_shot", "zero_shot_project_aware", "few_shot"],
  "repair": { "mechanisms": ["fixed", "adaptive"], "n": 10, "early_stop_p": null },
  "verification": { "baseline_runs": 2, "step_timeout_seconds": 900, "infra_attempts": 3 },
  "screening": { "require_paired_test_pass": true },
  "assessment": { "tools": ["jacoco", "pit", "tsdetect"], "comparison_systems": ["evosuite"] },
  "prompts": { "dir": "prompts", "version": "v1" },
  "generated_test_suffix": "_ArrowTest"
}
```

- `sha256` của manifest được kiểm tra khi đọc; lệch thì dừng (`MANIFEST_MISMATCH`).
- `repair: false` ở một manifest: candidate của dataset đó chỉ xác minh, không sửa (ví dụ GitBug-Java ở pilot).
- File này quyết định kết quả thực nghiệm. Run ghi sha256 của file vào `run.lock.json`; chạy tiếp một run với file đã đổi thì từ chối (`RUN_CONFIG_MISMATCH`).

### 3.2. `machine.local.json` (riêng từng máy, không commit)

```json
{
  "machine_name": "Dung-mac",
  "jdk_homes": { "8": "…", "11": "…", "17": "…", "21": "…" },
  "maven_path": "/opt/homebrew/bin/mvn",
  "gradle_path": "/opt/homebrew/bin/gradle",
  "git_path": "/opt/homebrew/bin/git",
  "work_dir": "../repo",
  "tools_dir": "../tools",
  "llm": { "proxy_url": "http://127.0.0.1:4000", "proxy_mode": "auto_start", "proxy_config_file": "litellm.config.local.yaml", "rpm_limit": 60 },
  "workers": { "build": 4, "llm": 2 }
}
```

### 3.3. `litellm.config.local.yaml` (riêng từng máy, không commit)

Mỗi model trong `experiments/*.json` có một mục trong `model_list` (provider, endpoint, khóa API). Chi tiết ở spec 02 Mục 4.4. Khóa API chỉ nằm ở file này, không bao giờ ghi vào log hay kết quả.

## 4. Thư mục của một run

```
runs/<run_id>/
  run.lock.json            cấu hình đã khóa: sha256 experiment, manifest, prompt; commit của tool; JDK, Maven, Gradle; máy
  ledger.jsonl             mỗi bước xong một dòng (để chạy tiếp sau khi dừng)
  events.jsonl             sự kiện để theo dõi (bắt đầu/kết thúc bước, lỗi)
  llm_cache/               lượt gọi model đã hoàn thành, khóa = định danh lượt gọi + request; chỉ để phát lại khi chạy tiếp (spec 02 Mục 4.4)
  tasks/<dataset>/<task_id>/
    block1/                metadata.json (M), baseline.json, workspace.json, logs/
    block2/<model>/<strategy>/   prompt.json, response.json, usage.json, candidate.java, candidate.json
    block3/<model>/<strategy>/
      verify_c0.json       kết quả xác minh candidate ban đầu
      <mechanism>/round_<k>/     request.json, prompt.json, response.json, candidate.java, verify.json, decision.json
      <mechanism>/final.json     Final Test Suite của chain
    block3/evosuite/       bộ test EvoSuite và kết quả xác minh
    block4/<suite_id>/     jacoco.json, pit.json, tsdetect.json, logs/
  reports/                 CSV và report.md của khối 4
```

Mọi file JSON: UTF-8 không BOM, xuống dòng LF, tên trường snake_case. Ghi file theo kiểu nguyên tử (ghi `.tmp` rồi đổi tên).

## 5. Thực thi

### 5.1. Lệnh

| Lệnh | Việc |
| --- | --- |
| `arrow-fw run [--experiment F] [--run-id R] [--tasks FILE\|ID,…] [--blocks 1,2,3,4]` | Chạy các khối cho các task; mặc định cả 4 khối, mọi task của experiment |
| `arrow-fw analyze …` | Chỉ khối 1 |
| `arrow-fw screen --manifest F --run-id R` | Chỉ khối 1 cho mọi CUT của manifest, ghi `reports/screening.csv` (tiêu chí B) làm đầu vào cho script chọn mẫu (spec 01 Mục 6.3) |
| `arrow-fw generate …` | Khối 2 (cần khối 1 đã xong) |
| `arrow-fw repair …` | Khối 3 (cần khối 2 đã xong) |
| `arrow-fw assess …` | Khối 4 phần đo (cần khối 3 đã xong) |
| `arrow-fw report --run-id R [--run-id R2 …]` | Khối 4 phần báo cáo; nhiều run thì gộp (dùng khi nhiều máy chạy các phần task khác nhau) |
| `arrow-fw doctor` | Kiểm tra môi trường: JDK, Maven/Gradle, Git, LiteLLM, công cụ khối 4, gọi thử từng model |
| `arrow-fw status --run-id R` | Tiến độ: số job xong/tổng theo khối, số INFRA, số EXCLUDED |
| `arrow-fw show --run-id R --task T` | Chi tiết một task: kết quả từng khối, từng vòng sửa |
| `arrow-fw rerun-list --run-id R` | Danh sách job cần chạy lại riêng, kèm lệnh |
| `arrow-fw control --run-id R pause\|resume\|stop` | Điều khiển run đang chạy từ terminal khác |
| `arrow-fw setup` / `arrow-fw models` | Cài đặt máy; model, provider, khóa API, giá |
| `arrow-fw` (không kèm lệnh) | Menu chính |

Màn hình của từng lệnh (menu, cài đặt, theo dõi trực tiếp khi chạy, chi tiết task, báo cáo…) mô tả ở **spec 06**. Khi chạy: P tạm dừng, R tiếp tục, S hoặc Ctrl+C dừng an toàn (việc đang chạy dở bị hủy, việc đã ghi ledger được giữ, chạy lại cùng lệnh thì tiếp tục). `--plain` in từng dòng thay cho màn hình.

### 5.2. Thứ tự và song song

- Đơn vị lập lịch là **job**: `(task, khối, model, strategy, mechanism)`. Một job chỉ chạy khi job nó phụ thuộc đã xong (khối 1 → khối 2 → xác minh c0 → chain sửa → khối 4).
- Hai hàng đợi giới hạn song song: `workers.build` (mọi lệnh Maven/Gradle/Java) và `workers.llm` (lượt gọi model). Một chain sửa chiếm lần lượt hai hàng đợi theo từng vòng.
- Mỗi job build trong **workspace riêng** (bản sao của mirror ở đúng commit), không dùng chung thư mục build.
- Fixed và Adaptive của cùng một candidate chạy **xen kẽ** theo thứ tự task (task lẻ: Fixed trước; task chẵn: Adaptive trước), như `Adaptive repair.md` Mục 8.

### 5.3. Chạy tiếp và chạy lại

- Job ghi một dòng `ledger.jsonl` khi xong (kể cả kết quả lỗi). **Dòng ledger là việc ghi cuối cùng của job**, chỉ ghi sau khi mọi file đầu ra của job đã lưu đủ (ví dụ khối 1: metadata, snapshot, baseline, tiêu chí B; spec 01 Mục 6.4). Chạy lại cùng `run_id` bỏ qua job đã có dòng ledger; job chưa có dòng thì làm lại từ đầu job (xóa đầu ra dở dang).
- Lỗi hạ tầng (timeout, dependency, công cụ) được thử lại tự động, tối đa `verification.infra_attempts` = 3 lần thử. Vẫn lỗi thì ghi ledger với `outcome = INFRA`, `needs_rerun = true` và lý do; báo cáo có danh sách **"Cần chạy lại riêng"** kèm lệnh cho từng task: `arrow-fw run --run-id R --tasks <task_id> --rerun-infra`. `--rerun-infra` không kèm `--tasks` thì chạy lại mọi job INFRA của run.
- Một chain sửa dừng giữa chừng (Ctrl+C) được chạy lại **từ đầu chain**; các lượt gọi model đã có trong `llm_cache/` không tốn tiền lại.

### 5.4. Nhiều máy

Không có cơ chế chia shard riêng. Mỗi máy chạy cùng experiment với `--tasks <file danh sách task_id>`, rồi `arrow-fw report --run-id A --run-id B …` gộp kết quả. Báo cáo kiểm tra mọi run cùng sha256 experiment và prompt, task không trùng nhau.

## 6. Log và mã lỗi

- Log của tool: `logs/arrow-<yyyyMMdd>.log` (mọi lệnh, lỗi, cảnh báo). Log từng lệnh build: `tasks/…/logs/<bước>.log`, dòng đầu ghi lệnh, thư mục, JDK; dòng cuối ghi mã thoát, thời gian, có timeout không. Log lớn hơn 5 MB chỉ giữ 2 MB đầu và 2 MB cuối.
- Không ghi khóa API, `master_key`, hay `api_base` đầy đủ vào bất kỳ log hay file kết quả nào; chỉ ghi tên máy chủ của endpoint.
- Mã lỗi viết hoa, có tiền tố theo khối: `B1_CLONE_FAILED`, `B2_INVALID_OUTPUT`, `B3_TIMEOUT`, `B4_PIT_FAILED`… Danh sách đầy đủ trong từng spec.

## 7. Kiến trúc code C#

Chi tiết ở **spec 07**. Tóm tắt: ba project phụ thuộc một chiều `Arrow.Console → Arrow.Infrastructure → Arrow.Core`.

| Project | Chứa |
| --- | --- |
| `Arrow.Core` | Model dữ liệu, cấu hình, **logic thuần** của 4 khối (chọn JDK, dựng prompt, tách mã, phân loại lỗi, signature, checkpoint, Fixed/Adaptive, công thức báo cáo, quy tắc N). Không chạy tiến trình, không gọi mạng |
| `Arrow.Infrastructure` | Phần chạm vào máy của 4 khối (git, Maven/Gradle, java-helper, gọi model, JaCoCo/PIT/tsDetect/EvoSuite), Pipeline lập lịch job, lưu trạng thái run |
| `Arrow.Console` | Lệnh dòng lệnh và màn hình (spec 06) |
| `tools/java-helper` | Công cụ Java (JavaParser): phân tích, đếm assertion, ghép method… |

- Mỗi khối có **interface** khớp đúng input/output trong spec của khối (ví dụ `IRepositoryAnalyzer`, `ITestGenerator`, `IVerifier`, `IRepairRunner`, `IQualityTool`). Pipeline chỉ nối các interface.
- Gọi model chỉ qua `ILlmClient`; test dùng `FakeLlmClient` đọc luật từ file JSON, không tốn tiền.

## 8. Kiểm thử của framework

| Mức | Nội dung |
| --- | --- |
| Unit | Phân tích file build (Java version, test framework), dựng prompt, tách mã từ response, phân loại lỗi và signature, so sánh checkpoint, thuật toán Fixed/Adaptive theo ví dụ của spec 03, quy tắc N, công thức chỉ số khối 4 |
| Integration | Khối 1 và khối 3 trên 3 fixture với Maven/Gradle thật; khối 4 đo một test VALID trên mỗi fixture |
| End-to-end | `arrow-fw run` trên fixture với `FakeLlmClient`: đủ 4 khối, ra báo cáo |
| Smoke model thật | 2 CUT, giới hạn số lượt gọi, chạy tay khi cần |

## 9. Chờ xác nhận

Giải thích chi tiết từng điểm (bối cảnh, vấn đề, ảnh hưởng tới RQ, các phương án kèm ưu nhược, đề xuất, phải sửa gì nếu chọn khác) ở **spec 05** (`05-open-questions.md`). Spec 00–04 đang viết theo phương án đề xuất.

| # | Điểm | Quyết định (14/10/2026) |
| --- | --- | --- |
| Q1 | Ảnh ghi "Fine-Tuned Open-Source Code Model" | **Chốt:** nhãn "Selected LLM (commercial API / open-weight model)", không fine-tune |
| Q2 | Ảnh không vẽ Fixed Repair và EvoSuite | **Chốt:** Fixed là một cơ chế của khối 3; EvoSuite sinh ở khối 4, xác minh bằng khối 3 |
| Q3 | Bộ test nào đại diện ARROW ở RQ3 | **Chốt:** một bộ mỗi task mỗi model (`zero_shot_project_aware` VALID ngay, nếu không thì sau Adaptive); báo riêng từng model, từng dataset |
| Q4 | Prompt sửa có kèm metadata M không | **Chốt:** M đầy đủ cho mọi prompt sửa, cả Fixed và Adaptive |
| Q5 | Adaptive kẹt ở mức 3 khi mã trùng | **Chốt:** mức 3 không tiến triển thì về mức 2; prompt mức 3 có danh sách đã thử |
| Q6 | Quy tắc chọn N | **Chốt:** quy tắc của CLAUDE.md (5 ≤ k ≤ 10), chạy pilot `n = 10` sau khi code xong |
| Q7 | Prompt mức 2 cho UNKNOWN | **Chốt:** khối lỗi build đầy đủ và cấu hình build |
| Q8 | Timeout | **Chốt:** thử tối đa 3 lần; vẫn timeout → INFRA, ghi rõ cần chạy lại riêng task kèm lệnh |

## 10. Lịch sử thay đổi spec

| Ngày | Thay đổi | Lý do |
| --- | --- | --- |
| 13/10/2026 | Bản đầu spec 00–05 | |
| 14/10/2026 | Chốt Q1–Q8 (spec 05); thêm spec 06 (màn hình), 07 (kiến trúc) | Quyết định của nhóm |
| 14/10/2026 | Spec 01: đọc đúng `revision_verification_status` (CONTENT_MATCHED/UPSTREAM_PINNED) và `revision_provenance` (git_history/dataset_record); tách major từ `java_version` dạng `openjdk version "…"` | Review: đọc nhầm trường manifest |
| 14/10/2026 | Spec 01: thêm build root và module root; `module_path` tính từ build root (lấy từ `build_attempts` của preflight) | Review: mẫu `118477931` có build root `3rdParty/libsodium-jni-2.0.1`, gốc repository không có pom; 23/59 mẫu pilot có `module_path = "."` |
| 14/10/2026 | Spec 01, 03, 04: mọi lệnh Maven chạy từ build root với `-pl <m> -am`, kể cả chạy test | Review: chạy test thiếu `-am` thì module anh em chưa có artifact |
| 14/10/2026 | Spec 02: cache khóa theo `call_id` (lượt gọi cụ thể), chỉ để phát lại khi chạy tiếp, giữ thời gian và chi phí gốc | Review: cache chung theo nội dung làm cơ chế chạy sau được thời gian 0 và làm Fixed lặp output lỗi |
| 14/10/2026 | Spec 01, 03, 04: snapshot sau biên dịch; mỗi lần chạy test (baseline, mỗi vòng xác minh, mỗi lần đo) trên bản sao mới | Review: vòng trước có thể làm hỏng workspace của vòng sau |
| 14/10/2026 | Spec 04: phạm vi đo dùng mẫu chính xác `<FQN>` và `<FQN>$*` cho PIT và JaCoCo | Review: `<FQN>*` khớp nhầm class khác |
| 14/10/2026 | Spec 04: RQ3 ghép cặp theo từng chỉ số, báo `n_pairs` riêng | Review: công cụ lỗi độc lập làm hai bên so trên hai tập task khác nhau |
| 14/10/2026 | Spec 01: tiêu chí B (test người viết chạy và pass ổn định), lệnh `screen`, `require_paired_test_pass` | Review: thống nhất với cách chọn pilot v2 |
| 14/10/2026 | Spec 01, 02, 03, 04: xử lý TestNG (2/50 CUT pilot) | Kiểm tra thêm khi review |
| 14/10/2026 | Spec 01: build root và bộ chọn module lấy từ **lần biên dịch test thành công** trong `build_attempts` (chế độ reactor hoặc standalone), không lấy lần thử đầu | Review lần 2: tập đánh giá có 2 CUT (`103728471_…`, `9580168_…`) mà lệnh reactor thất bại, chỉ build riêng module thành công |
| 14/10/2026 | Spec 01, 04: `dependency:list` / `build-classpath` chạy **cùng một lần gọi Maven** với `test-compile` | Review lần 2 (kiểm chứng bằng fixture): gọi riêng thì thiếu artifact module anh em |
| 14/10/2026 | Spec 03, 04: lệnh chạy test lấy tên từ `SuiteFiles.test_fqn` (`…_ArrowTest`, `…_ESTest`); mô tả `SuiteFiles` gồm scaffolding, runtime EvoSuite | Review lần 2: cố định `_ArrowTest` làm bộ EvoSuite bị xếp DISCOVERY |
| 14/10/2026 | Spec 04: mỗi bản sao để đo phải ghi bộ test và **biên dịch test** trước khi chạy PIT | Review lần 2: snapshot tạo trước khi có bộ test, PIT không tự biên dịch |
| 14/10/2026 | Spec 00, 01, 07: ledger là việc ghi cuối cùng của job; khối 1 chỉ ghi sau baseline và tiêu chí B | Review lần 2: ghi OK sớm làm chạy tiếp thiếu baseline |
