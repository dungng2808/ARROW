# Tiến độ pilot_tool

Bằng chứng cho từng mốc của `ARCHITECTURE.md` Mục 16: lệnh đã chạy, kết quả, run, commit. Máy chạy: Dung-mac (macOS arm64, 14 nhân, 48 GB, JDK 8/11/17/21, Maven 3.9.16, Gradle 9.6.1). Mỗi mục ghi theo thứ tự thời gian; commit hash của một mục nằm ở mục ngay sau (commit chứa chính file này).

## 0. Dữ liệu pilot v2 (13/10/2026)

- Rút 50 CUT dự bị (`evaluation_manifest/pilot/classes2test_pilot_reserve_50_seed42.*`, sha256 `5a76e632…`), chạy S0 cho 50 CUT pilot v1 + 50 dự bị + 9 GitBug-Java: run `pilot-screening` (109 task: 101 READY, 8 INFRA) và `pilot-screening-fix` (4 task chạy lại sau khi sửa lỗi đọc `classname` của Surefire).
- `python scripts/select_pilot_after_s0.py … --name classes2test_pilot_50_seed42_v2` → `OK 50 CUTs: kept 39, replaced 11, reserve left 27; sha256=b44a9d9a…`. Tiêu chí và danh sách thay: `evaluation_manifest/pilot/README.md`, `classes2test_pilot_50_seed42_v2.meta.json`.
- `experiments/pilot-v1.json` trỏ sang v2; `arrow shard plan --experiment experiments/pilot-v1.json --owners "Dung-mac=Dũng-mac,…,Han=Hán" --plan-id pilot-6 --out ../shards/pilot-6 --coordinator Dung-mac -y` → 6 shard, 59 CUT (50 Classes2Test, 9 GitBug-Java).
- Sửa trong lúc sàng lọc: JVM của mọi bước build chạy với `-Duser.language=en -Duser.country=US -Duser.timezone=UTC` (specs/02 Mục 3 bước 6; jitterted-ensembler hết 3 test fail giả); `TestReportParser` lấy tên class từ `testsuite` khi `classname` là tên method (amforeas, cucable); tên test JUnit 5 của Gradle bỏ `()` cuối.

## 1. Nợ M2: fixtures và integration test

- `tests/fixtures/`: `sample-maven-junit4` (1 module, Java 8), `sample-maven-multimodule-junit5` (core + app, Java 17, `-pl app -am`, module app có `<includes>` riêng), `sample-gradle-junit5` (Java 17). Mỗi fixture có 1 test luôn fail (`BrokenTest`) và 1 test fail ở lần chạy đầu của workspace (`FlakyTest`).
- `tests/Arrow.Infrastructure.Tests/PrepareIntegrationTests.cs`: S0 thật trên 3 fixture (fixture được commit thành repository git tạm, tải qua `file://`).
- `dotnet build ArrowRunner.sln` → 0 warning, 0 error. `dotnet test ArrowRunner.sln` → Core 50/50, Infrastructure 3/3 pass.

Commit: `79315db` (M1, M2, nợ M2, pilot v2), đã push lên `main`.

## 2. M3: xác minh 5 bước

- Code: `Arrow.Core/Verify` (`VerificationState`, `VerifyResult` = `verify.json`, `FailureClassifier`: lỗi javac Maven/Gradle, loại lỗi javac cố định, RUNTIME/ASSERTION, regression, signature theo specs/03 Mục 6, D11; `CodeHasher`), `Arrow.Infrastructure/Verify` (`FiveStepVerifier`, `CandidateRunner`: workspace sạch mỗi ứng viên, build lại 1 lần khi timeout/lỗi công cụ/lỗi tải dependency rồi INFRA, `-Dsurefire.includesFile` hoặc init script Gradle khi module không chạy test sinh ra), `Arrow.Infrastructure/Java/JavaAnalyzer` + `tools/java-analyzer` (JavaParser: bỏ comment theo AST, đếm assertion, method chứa dòng lỗi). S0 thêm hậu tố `_ArrowTest2` khi tên trùng file sẵn có.
- `dotnet test tests/Arrow.Infrastructure.Tests` → 14/14 pass (`VerifierIntegrationTests`: lỗi compile trong method → `COMPILE | cannot find symbol | method` + method lỗi; lỗi import → `COMPILE | package does not exist | -`; không có `@Test` và mọi method `@Ignore` → DISCOVERY; NPE từ CUT → `RUNTIME | NullPointerException | Calculator.labelLength`; assertion sai giá trị → ASSERTION (không phải COMPILE); `assertThrows` không ném → ASSERTION; ghi file dùng chung → `REGRESSION | existing tests fail | SharedStoreTest`; test đúng → VALID với 2 assertion; module có `<includes>` riêng → VALID, `module_run_includes_target=false`; Gradle VALID và COMPILE).
- `dotnet test tests/Arrow.Core.Tests` → 76/76 pass (thêm `FailureClassifierTests`, `CodeHasherTests`).
- `dotnet build ArrowRunner.sln` → 0 warning, 0 error.

Commit: `15378b5` (M3), đã push lên `main`.

## 3. M4: java-analyzer

- `tools/java-analyzer` (Maven, JavaParser 3.26.2, Java 11, jar shade), lệnh: `analyze_test`, `strip_comments`, `count_assertions`, `list_test_methods`, `method_at_line`, `cut_api`, `method_source`, `called_methods`, `splice_methods`. `JavaAnalyzer` (.NET) tự build jar bằng Maven khi chưa có hoặc mã nguồn mới hơn jar; chạy bằng JDK 17 (rồi 21, 11).
- `dotnet test tests/Arrow.Infrastructure.Tests --filter AnalyzerTests` → 6/6 pass: đếm assertion đúng từng quy tắc của specs/03 Mục 8 (JUnit, AssertJ, Mockito `verify`, `assertThrows`, `@Test(expected)`, `fail` trong `try`, RestAssured `.then().statusCode().body()` = 2, `.then().assertThat()` không tính, `.extract()` không tính, assertion tầm thường không tính); bỏ comment theo AST giữ nguyên `"http://…/*…*/"` và text block; mã không parse được thì bỏ comment theo lexer; ghép method mức 3: thay method lỗi, bỏ method lỗi không có trong response, thêm import/field/helper mới, không thêm test mới, method khác giữ từng ký tự; API của CUT và method bị gọi.
- `dotnet test ArrowRunner.sln` → Core 76/76, Infrastructure 20/20. Build 0 warning.

Commit: `960e346` (M4), đã push lên `main`.

## 4. M5: model, prompt, S1 sinh test

- Code: `Arrow.Core/Prompts` (template engine `{{x}}`/`#if`/`#each`, biến thiếu → lỗi; `PromptLibrary`, hash từng template vào `run_config.lock.json`; khối `repo_context`), `Arrow.Core/Generation/OutputNormalizer` (specs/04 Mục 3), `Arrow.Core/Llm`, `Arrow.Infrastructure/Llm` (`LiteLlmClient` thử lại 20/60/120 giây, 400/401/403/404 → tạm dừng run; `CachingLlmClient` `llm_cache/`; `LlmGateway` giới hạn đồng thời, rpm, kiểm tra model id, `--max-llm-calls`; `FakeLlmClient`; `LiteLlmProxy` tự bật/tắt proxy), `Arrow.Infrastructure/Generation` (`GenerateService` S1, `FewShotSelector`), `prompts/` (câu chữ của specs/04 Mục 4, 6). Lệnh mới: `arrow run --experiment … --stages … --fake-llm … --max-llm-calls …`, `arrow fewshot select`.
- Few-shot: `dotnet run --project src/Arrow.Cli -- fewshot select` → JUnit 4 `166116543_f6b6c10d343d5bb1` (conduit-on-kubernetes, `DefaultJwtService`), JUnit 5 `48880766_962a909932e57aca` (tablesaw, `ReadOptions`); pool 4.805 bản ghi; chi tiết specs/04 Mục 5.
- Test: Core 96/96 (`TemplateEngineTests`, `OutputNormalizerTests`, `LlmRequestTests`); Infrastructure 27/27 (`LlmClientTests`: 429 rồi 200, 401 tạm dừng, 5xx hết lượt gọi lại, sai model id, cache hit không gọi HTTP, giới hạn lượt gọi; `GenerateEndToEndTests`: `arrow run --stages prepare,generate --fake-llm` trên 3 fixture → VALID, ASSERTION, COMPILE (output không hợp lệ, không build), chạy lại không thêm dòng ledger).
- Smoke với GPT-6 Luna thật: `arrow run --experiment experiments/smoke-v1.json --tasks 107165228_0924785415323560,159422409_9d630c4556038ca7 --stages prepare,generate --max-llm-calls 6 --run-id smoke-gpt6 --rerun-infra` (run `runs/smoke-gpt6`).
  - Lần 1: LiteLLM 1.91.0 trả 400 `UnsupportedParamsError: reasoning_effort` → run tự tạm dừng đúng thiết kế; lộ lỗi ghi `control.json` khi hai job cùng yêu cầu tạm dừng (đã sửa: tên file tạm riêng, yêu cầu tạm dừng sau khi ghi ledger). Sửa cấu hình: `allowed_openai_params: ["reasoning_effort"]` (00-INDEX P5).
  - Lần 2: 3 lượt gọi thật thành công, model id `gpt-6-luna` (prompt/completion token: 970/656, 1272/884, 440/270): jpa2ddl `zero_shot` → COMPILE (`cannot find symbol | class`), `zero_shot_project_aware` → VALID; `159422409` `zero_shot` → COMPILE. Lượt thứ 4: HTTP 400 **"Your TeamoRouter wallet balance is insufficient"** → run tạm dừng, đã dừng hẳn. **Smoke còn dở: cần nạp tiền ví TeamoRouter** rồi chạy lại đúng lệnh trên (3 job còn lại; job đã xong không gọi lại).
- Cột "Xong khi" của M5 (sinh và xác minh 177 ứng viên pilot): để lại cho lần chạy pilot thật.

Commit: `f02b2d6` (M5), đã push lên `main`.

## 5. M6: Fixed Repair, Adaptive Repair, S2, quy tắc N

- Code: `Arrow.Core/Repair` (`CheckpointComparer`, `FixedRepairPolicy`, `AdaptiveRepairPolicy` theo specs/05 Mục 4–5, dựng lại từ `decision.json`; `RepairSummary`; `RepairPrompts`: khối lỗi mức 1, phần thuần của mức 2 cho DISCOVERY, ASSERTION, REGRESSION, UNKNOWN [D6], COMPILE), `Arrow.Core/Report/NRules` (`share95`, `report2`, cộng dồn), `Arrow.Infrastructure/Repair` (`RepairService`: c0 dựng lại và build lại một lần cho hai cơ chế, loại khỏi bước sửa có `excluded.json`, mỗi vòng ghi prompt/response/usage/normalization/cand/verify/decision và một dòng ledger `round=t`, lỗi hạ tầng mở attempt mới; `RepairContextBuilder`: API thật của CUT và class liên quan, method bị gọi, mock, expected/actual, cấu hình bước test), stage S2 với thứ tự xen kẽ.
- Test: Core 108/108 (`RepairPolicyTests`: kịch bản 5 vòng của specs/05 Mục 9, Tie khác signature giữ mức, output không hợp lệ tính một vòng, mức 3 lùi về 2 cho DISCOVERY và khi không xác định được method, Fixed sửa từ bản mới nhất và build lại mã trùng, dựng lại từ sự kiện cho kết quả giống chạy liền, bộ đếm của summary; `NRuleTests`: ví dụ README → `share95` N = 5, `report2` N = 8, S(10) = 0 → 10). Infrastructure 28/28, trong đó `RepairEndToEndTests` (`arrow run --stages prepare,generate,repair --fake-llm`, n_max 3): RUNTIME — Fixed build lại cùng mã 3 vòng không thành công, Adaptive vòng 1 mã trùng → mức 2 (có mã method bị gọi) → VALID vòng 2; ASSERTION — Fixed 3 vòng output không hợp lệ, Adaptive: không hợp lệ → mức 2 → lặp signature → mức 3 ghép `adds`, giữ `divides` của checkpoint → VALID vòng 3; COMPILE (Gradle) cả hai VALID vòng 1; thứ tự xen kẽ đúng; GitBug-Java không có job sửa.
- S2 với model thật: chưa chạy (ví TeamoRouter hết tiền, xem Việc còn mở).

Commit: `a951443` (M6), đã push lên `main`.

## 6. M7: EvoSuite, JaCoCo, PIT, tsDetect

- Công cụ: `arrow tools install` → 16 file trong `ARROW/tools/`, đúng SHA-256 (nguồn và hash: SETUP.md); `arrow doctor` kiểm tra từng công cụ và đề nghị tải khi thiếu.
- Code: `Arrow.Core/Quality` (model `jacoco.json`, `pit.json`, `tsdetect.json`, `evosuite_stats.json`; đọc CSV của JaCoCo, PIT, tsDetect), `Arrow.Infrastructure/Quality` (`QualityToolkit`: lệnh build tùy ý, classpath test Maven/Gradle, init script Gradle, chạy Java có log, thêm dependency `system` vào POM; `EvoSuiteService` S3; `QualityService` S4), stage S3 và S4 (đo mọi bộ test VALID: ARROW sau sinh, sau sửa từng cơ chế, EvoSuite).
- Sai lệch đã ghi (00-INDEX P7, P8; specs/06 Mục 2.2): PIT chạy bản dòng lệnh cho cả Maven và Gradle vì plugin Gradle 1.15.0 không chạy trên Gradle 9; JUnit 5 cần thêm `junit-platform-launcher` cùng phiên bản.
- Test: Core 111/111 (`QualityParserTests`); Infrastructure 32/32, trong đó `QualityIntegrationTests`: Maven JUnit 4 (JaCoCo đúng 9 dòng của `Calculator`, PIT có mutation score, tsDetect đọc được), Maven nhiều module JUnit 5, Gradle JUnit 5; EvoSuite 1.2.0 (JDK 11) sinh `Calculator_ESTest` đạt VALID qua bộ xác minh 5 bước và đo được bằng cùng bộ đo.

Commit: `3728e24` (M7), đã push lên `main`.

## 7. M8: báo cáo, export, merge, show

- Code: `Arrow.Core/Report/PilotReport` (bảng thời gian/chi phí trung vị, p90; chọn N chung hai cơ chế, chỉ tính khi mọi chuỗi xong, độ nhạy `report2`, `share90/95/99`; CSV), `Arrow.Infrastructure/Report` (`ReportService` S5 → `reports/part1_cost.csv`, `part1_infra_errors.csv`, `part1_generation.csv`, `part2_cumulative.csv`, `pilot_report.md`; `BundleService` export (bỏ `llm_cache/`, log bước đạt giữ 200 dòng cuối, `bundle_manifest.json` có hash từng file, cảnh báo > 50 MB, từ chối > 95 MB) và merge (cùng experiment và prompt hash, mỗi shard đúng một gói, hợp task bằng plan, không task trùng, model id giống nhau)). Lệnh `arrow report`, `show`, `export`, `merge`; `arrow run` chạy S5 ở cuối.
- Test: Core 114/114 (`PilotReportTests`); Infrastructure 37/37, trong đó `FullPipelineEndToEndTests` (`arrow run` đủ S0→S5 với `--fake-llm`, n_max 3, trên 3 fixture): EvoSuite VALID trên fixture JUnit 4 (task Java 17 → `UNSUPPORTED_JDK` đúng specs/06), mọi bộ test VALID (ARROW sau sinh, sau sửa, EvoSuite) có JaCoCo/PIT/tsDetect `OK`, `part1_cost.csv` có đủ 10 bước, `part2_cumulative.csv` = `1,2 / 2,3 / 3,4`, `pilot_report.md` ghi **N theo `share95` = 3** (độ nhạy `report2` = 3); `export` không chứa `llm_cache/`; `merge` gom lại cho cùng N; `merge` từ chối plan có shard thiếu gói. `LoggingTests` (specs/08 Mục 7): che khóa, dòng ledger ghi dở và file tạm sót lại bị bỏ qua, log > 5 MB giữ đầu/cuối, log bước đạt trong gói chỉ giữ 200 dòng cuối.
- `dotnet build ArrowRunner.sln` → 0 warning, 0 error.

## 8. Smoke với GPT-6 Luna thật (điều kiện 3, 7)

| Run | Lệnh | Kết quả |
| --- | --- | --- |
| `runs/smoke-gpt6` | `arrow run --experiment experiments/smoke-v1.json --tasks 107165228_0924785415323560,159422409_9d630c4556038ca7 --stages prepare,generate --max-llm-calls 6 --run-id smoke-gpt6 --rerun-infra` | S0: 2 READY. S1: 3 lượt gọi thật thành công, model id `gpt-6-luna` (jpa2ddl `zero_shot` → COMPILE, `zero_shot_project_aware` → VALID; `159422409` `zero_shot` → COMPILE); lượt 4: HTTP 400 `enterprise_insufficient_balance` → run tự tạm dừng, đã dừng |
| `runs/smoke-gpt6-b` (13/10 10:19) | như trên, `--stages prepare,generate,repair --max-llm-calls 36` (run mới vì commit tool đổi → `RUN_CONFIG_MISMATCH`) | S0: 2 READY; lượt gọi đầu: HTTP 400 `enterprise_insufficient_balance` (`source: enterprise_wallet`) → tạm dừng, đã dừng; 0 lượt bị tính |
| `runs/smoke-gpt6-c` (13/10 10:27) | như trên | như `smoke-gpt6-b` |
| `runs/smoke-gpt6-d` (13/10 10:30) | như trên | như `smoke-gpt6-b` |
| `runs/smoke-gpt6-e` (13/10 10:37) | như trên | HTTP 400 `routing_strategy_resource_unavailable`: "No channel is currently available for this routing mode. Switch to premium routing or retry later" (`routing_strategy: low_price`) → tạm dừng, đã dừng |
| `runs/smoke-gpt6-f` (13/10 10:39, sau 2 phút) | như trên | lại `enterprise_insufficient_balance` → tạm dừng, đã dừng |

| `runs/smoke-20261013-1049` (13/10 10:49, menu "Chạy smoke với model thật", `workers.llm` = 4) | `arrow run --experiment experiments/smoke-v1.json --tasks … --stages prepare,generate,repair --max-llm-calls 36` | 14 lượt gọi thật thành công (`gpt-6-luna`); 2 job INFRA `enterprise_insufficient_balance` lẻ tẻ trong khi các lượt song song vẫn thành công; S2: 7/8 nhánh VALID |
| **`runs/smoke-20261013-1104`** (13/10 11:04, menu, `workers.llm` = 2) | như trên | **S0 2/2 READY; S1 6/6 có kết quả (1 VALID, 3 COMPILE trong đó 1 output không hợp lệ thiếu `package`, 1 RUNTIME… xem `part1_generation.csv`); S2 8/8 nhánh không lỗi hạ tầng, đều VALID (Fixed và Adaptive VALID vòng 1, Adaptive `159422409` zero_shot VALID vòng 2); 0 job INFRA.** 13 lượt gọi thật, 15.032 token vào, 6.791 token ra, model id `gpt-6-luna`. `arrow report --run-id smoke-20261013-1104` → `reports/pilot_report.md`, N (`share95`, n_max 2) = 3, `report2` = 2 |

**Smoke S1 + S2 với model thật: đạt** (run `smoke-20261013-1104`). Tổng lượt gọi thật: 3 + 14 + 13 = 30 / 40.

Nguyên nhân các lỗi trước (kiểm tra bằng API `GET /v1/billing/balance`, `/v1/billing/costs`, `/v1/usage` của TeamoRouter): số dư của khóa còn 0,3609 USD, mỗi lượt gọi chỉ khoảng 0,0002 USD, không có lỗi 429 hay header rate limit. Lỗi `enterprise_insufficient_balance` và `routing_strategy_resource_unavailable` (`low_price`) xuất hiện khi có nhiều lượt gọi cùng lúc (`workers.llm` = 4); với `workers.llm` = 2 không còn lỗi. Request trả lỗi 400 vẫn bị TeamoRouter tính tiền (22 request, 0,0053 USD): nên báo hỗ trợ TeamoRouter. Khuyến nghị cho pilot: `workers.llm` ≤ 2 trên mọi máy.

## Trạng thái

**Code M1–M8 và test tự động đã xong; smoke với GPT-6 Luna thật qua S1 và S2; sẵn sàng chạy pilot theo shard.**

## Việc còn mở

- TeamoRouter từ chối khi nhiều lượt gọi cùng lúc: đặt `workers.llm` ≤ 2 trên mọi máy trước khi chạy pilot; báo hỗ trợ TeamoRouter việc request lỗi vẫn bị tính tiền (trace_id trong `logs/litellm-20261013.log`).
- Cột "Xong khi" của M5 (177 ứng viên), M6 (S2 với GPT-6 Luna trên pilot), M8 (`pilot_report.md` của pilot thật): để lại cho lần chạy pilot theo shard `shards/pilot-6` (mỗi máy `arrow run`, `arrow export`; người điều phối `arrow merge`).
- Giá token GPT-6 Luna (`price_usd_per_million`) chưa điền: cột chi phí của báo cáo là 0 cho tới khi điền (token vẫn đủ).
- Tiêu chí M1 trên Windows (tạm dừng/tiếp tục trên Windows): chưa kiểm được trên máy này.
- Các máy khác cần: `git pull`, `arrow tools install`, sửa `litellm.config.local.yaml` thêm `allowed_openai_params` (hoặc nhập lại khóa), `arrow doctor`.
- `evaluation_manifest/gitbug_java_32_seed42.*` và thay đổi trong `evaluation_manifest/README.md` chưa commit: ngoài phạm vi goal, để người dùng commit.
- Tập đánh giá 200 class chưa sàng lọc S0 theo tiêu chí B (người dùng sẽ chọn lại sau).
- Điểm chờ xác nhận P1–P8: `specs/00-INDEX.md`.
