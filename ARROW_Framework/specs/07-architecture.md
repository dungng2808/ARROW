# 07. Kiến trúc code

**Mục đích:** mô tả cách tổ chức code C# của ARROW Framework: các project, thư mục, lớp chính của từng khối, cách các phần gọi nhau, cách chạy song song, lưu trạng thái, chạy tiếp, và thứ tự viết code.

## 1. Nguyên tắc

1. **Bốn khối = bốn thư mục, mỗi khối một interface** khớp đúng input/output trong spec của khối. Pipeline chỉ nối các interface; không khối nào gọi thẳng vào bên trong khối khác.
2. **Tách logic thuần khỏi I/O.** Mọi quyết định (chọn JDK, dựng prompt, tách mã, phân loại lỗi, signature, so sánh checkpoint, thuật toán Fixed/Adaptive, công thức báo cáo, quy tắc N) nằm trong `Arrow.Core`, không chạy tiến trình, không gọi mạng, nên unit test được bằng ví dụ trong spec. Mọi việc chạm vào máy (git, Maven/Gradle, Java, HTTP, file) nằm trong `Arrow.Infrastructure`.
3. **Kết quả là dữ liệu, không phải exception.** Task hỏng, candidate lỗi, lỗi hạ tầng đều trả về dưới dạng `Outcome` có mã lỗi; exception chỉ dùng cho lỗi lập trình hoặc lỗi cấu hình làm dừng cả run.
4. **Mọi thứ ghi ra đĩa ngay khi xong** (ghi nguyên tử), để dừng bất cứ lúc nào cũng chạy tiếp được.
5. **Một chỗ gọi model** (`ILlmClient`), **một chỗ chạy build** (`IBuildRunner`), **một chỗ đọc báo cáo test** (`TestReportReader`): Fixed, Adaptive, EvoSuite, khối 4 đều dùng chung, bảo đảm cùng điều kiện so sánh.

## 2. Các project và phụ thuộc

```
                    ┌──────────────────────┐
                    │     Arrow.Console    │  lệnh, màn hình (Spectre.Console), RunHost
                    └──────────┬───────────┘
                               │
                    ┌──────────▼───────────┐
                    │ Arrow.Infrastructure │  4 khối (phần I/O), Pipeline, Git, Maven/Gradle,
                    │                      │  LLM client, java-helper, công cụ khối 4, RunStore
                    └──────────┬───────────┘
                               │
                    ┌──────────▼───────────┐
                    │      Arrow.Core      │  model dữ liệu, cấu hình, logic thuần của 4 khối
                    └──────────────────────┘

 tools/java-helper (Java, JavaParser)  ◄── gọi qua tiến trình từ Arrow.Infrastructure
 tests/Arrow.Core.Tests                ──► Arrow.Core                     (unit)
 tests/Arrow.Infrastructure.Tests      ──► Arrow.Infrastructure, Console  (integration, end-to-end)
```

Phụ thuộc một chiều: Console → Infrastructure → Core. Core không tham chiếu project nào.

## 3. Thư mục và lớp chính

```
ARROW_Framework/
  ArrowFramework.sln
  Directory.Build.props            .NET 8, nullable, warnings as errors
  src/
    Arrow.Core/
      Common/        Json, Hashing, Ids (TaskKey, CandidateKey, ChainKey), ArrowException, Outcome, ErrorCodes
      Config/        ExperimentConfig, MachineConfig, ConfigLoader (kiểm tra sha256 manifest), RunLock
      Manifest/      TaskRecord, ManifestReader
      Block1/        RepositoryMetadata (M), Baseline, JavaVersionReader, ManifestJavaVersionParser, JdkSelector,
                     BuildRootRule (từ lần biên dịch test thành công trong build_attempts), MavenModuleSelector,
                     TestFrameworkDetector, PathCalculator, MetadataPromptRenderer
      Block2/        LlmRequest, LlmResponse, LlmUsage, CacheKey, PromptBuilder (3 quy trình + sửa),
                     PromptTemplates, CodeExtractor, NameNormalizer, CandidateTest
      Block3/        SuiteFiles (test_fqn, files, extra_test_classpath), VerificationState, VerifyResult, JavacErrorParser, TestResultClassifier,
                     SignatureBuilder, Diagnosis, CheckpointComparer, IRepairPolicy,
                     AdaptiveRepairPolicy, FixedRepairPolicy, RepairRequest, RoundDecision, FinalTestSuite
      Block4/        CoverageResult, MutationResult, SmellResult, JacocoCsvParser, PitXmlParser,
                     TsDetectCsvParser, Rq1Calculator, Rq2Calculator, NRule, PSensitivity,
                     Rq3Calculator, ReportModel
    Arrow.Infrastructure/
      Processes/     ProcessRunner (timeout, giết cả cây tiến trình, gom output), ToolLog
      Storage/       RunStore (thư mục run), AtomicFile, Ledger, EventLog, StatusTracker
      Build/         BuildRunner (Maven/Gradle, JAVA_HOME, JAVA_TOOL_OPTIONS, cờ chung),
                     BuildCommands, TestReportReader (Surefire/Gradle XML)
      Git/           GitClient, MirrorManager, WorkspaceManager (checkout gốc, snapshot đã biên dịch, bản sao mới cho mỗi lần chạy test)
      JavaHelper/    JavaHelperClient (tiến trình java-helper chạy lâu, giao tiếp JSON từng dòng)
      Block1/        RepositoryAnalyzer : IRepositoryAnalyzer, BuildRootLocator, EffectivePomReader, GradleProbe,
                     BaselineRunner, PairedTestScreener (tiêu chí B)
      Block2/        TestGenerator : ITestGenerator, LiteLlmClient, ReplayingLlmClient, LlmGateway,
                     FakeLlmClient, LiteLlmProxy (tự bật/tắt proxy), LiteLlmConfigFile
      Block3/        Verifier : IVerifier, FailureDiagnoser : IFailureDiagnoser,
                     ExtraContextBuilder (thông tin bổ sung mức 2), RepairRunner : IRepairRunner
      Block4/        EvoSuiteGenerator : IComparisonGenerator, JacocoTool, PitTool, TsDetectTool : IQualityTool,
                     ReportBuilder : IReportBuilder, ReportWriter (CSV, report.md), ToolsCatalog (tải, sha256)
      Pipeline/      RunPipeline, JobGraph, Scheduler (2 hàng đợi), ControlGate (tạm dừng/dừng), ResumePlanner
      Doctor/        DoctorService
    Arrow.Console/
      Program.cs     đăng ký lệnh (Spectre.Console.Cli)
      Commands/      Run, Screen, Status, Show, Report, RerunList, Control, Setup, Models, Doctor, FewShot
      Screens/       MenuScreen (S1), SetupScreen (S2), ModelScreen (S3), DoctorScreen (S4),
                     LiveRunScreen (S5), StatusScreen (S6), TaskScreen (S7), ReportScreen (S8), RerunScreen (S9), ScreenScreen (S10)
      Ui/            Theme, PlainPrinter, KeyListener
  tools/java-helper/   pom.xml, src/…  (JavaParser 3.26, đóng gói 1 jar)
  prompts/             generation/*.md, repair/*.md, fewshot/examples.json
  experiments/         pilot-v1.json, smoke-v1.json
  tests/
    Arrow.Core.Tests/
    Arrow.Infrastructure.Tests/
    fixtures/          sample-maven-junit4, sample-maven-multimodule-junit5, sample-gradle-junit5
```

## 4. Interface của từng khối

| Khối | Interface (Core định nghĩa model, Infrastructure triển khai) | Input → Output |
| --- | --- | --- |
| 1 | `IRepositoryAnalyzer.AnalyzeAsync(TaskRecord, RunContext)` | manifest → `RepositoryAnalysis` (M, baseline, workspace, outcome) |
| 2 | `ITestGenerator.GenerateAsync(GenerateRequest)` / `RepairAsync(RepairRequest)` | M + CUT + quy trình / yêu cầu sửa → `CandidateTest` (T) |
| 3 | `IVerifier.VerifyAsync(CandidateTest, TaskContext)` | T → `VerifyResult` (5 bước) |
| 3 | `IFailureDiagnoser.Diagnose(VerifyResult, …)` | kết quả → `Diagnosis` (trạng thái, signature, method lỗi, khối lỗi) |
| 3 | `IRepairPolicy` (`AdaptiveRepairPolicy`, `FixedRepairPolicy`) | lịch sử vòng → `RepairRequest` tiếp theo, `RoundDecision` |
| 3 | `IRepairRunner.RunChainAsync(c0, mechanism, TaskContext)` | c0 → `FinalTestSuite` (gọi khối 2 mỗi vòng) |
| 4 | `IComparisonGenerator`, `IQualityTool`, `IReportBuilder` | bộ test VALID → JaCoCo/PIT/tsDetect → báo cáo |

`RepairRunner` là chỗ duy nhất thể hiện **vòng lặp nét đứt** của hình: mỗi vòng hỏi `IRepairPolicy` lấy `RepairRequest` → gọi `ITestGenerator.RepairAsync` (khối 2) → `IVerifier` → `IFailureDiagnoser` → `IRepairPolicy.Apply`.

## 5. Luồng chạy một run

```
arrow-fw run
  └─ RunCommand
       ├─ ConfigLoader: đọc experiment (kiểm sha256 manifest), machine.local.json, litellm config
       ├─ RunStore.Open(run_id): tạo hoặc mở thư mục run; RunLock ghi hoặc so run.lock.json
       ├─ ResumePlanner: đọc ledger → danh sách job đã xong
       ├─ JobGraph.Build(tasks, experiment): tạo mọi job và quan hệ phụ thuộc
       ├─ RunHost: chạy RunPipeline ở nền + LiveRunScreen (hoặc PlainPrinter)
       │    └─ RunPipeline
       │         └─ Scheduler lấy job sẵn sàng → chạy → ghi ledger → mở khóa job phụ thuộc
       └─ kết thúc: status DONE / STOPPED / PAUSED, in tóm tắt
```

**Đồ thị job của một task** (một model; nhiều model thì nhân bản nhánh khối 2–3):

```
B1 analyze
  ├─► B2 generate (zero_shot) ──► B3 verify c0 ──┬─► B3 chain fixed ─────┐
  ├─► B2 generate (zero_shot_project_aware) ─► …  └─► B3 chain adaptive ──┤
  ├─► B2 generate (few_shot) ──► …                                         ├─► B4 measure (mỗi bộ VALID)
  └─► B4 evosuite generate ──► B3 verify evosuite ────────────────────────┘
B4 report: chạy riêng bằng `arrow-fw report` (hoặc tự chạy khi mọi job của run xong)
```

**Một vòng sửa** (trong job `B3 chain`):

```
RepairRunner           IRepairPolicy        ITestGenerator (khối 2)     IVerifier        IFailureDiagnoser
    │ NextRequest() ───────►│                        │                       │                  │
    │◄── RepairRequest ─────│                        │                       │                  │
    │ RepairAsync(request) ─────────────────────────►│ prompt → LLM → tách mã │                  │
    │◄────────────────────────────── CandidateTest ──│                       │                  │
    │ (Adaptive: hash trùng? → bỏ qua build)          │                       │                  │
    │ VerifyAsync(ct) ──────────────────────────────────────────────────────►│ 5 bước           │
    │◄──────────────────────────────────────────────────────── VerifyResult ─│                  │
    │ Diagnose(result) ─────────────────────────────────────────────────────────────────────────►│
    │◄──────────────────────────────────────────────────────────────────────────── Diagnosis ───│
    │ Apply(outcome) ──────►│ checkpoint, rollback, mức, đã thử                                    │
    │◄── RoundDecision ─────│                                                                      │
    │ ghi round_k/*.json; VALID hoặc hết N → FinalTestSuite                                         │
```

## 6. Lập lịch và song song

- **Hai hàng đợi** giới hạn bằng `SemaphoreSlim`: `build` (`workers.build`, mọi lệnh Maven/Gradle/Java/EvoSuite/PIT) và `llm` (`workers.llm`, mọi lượt gọi model). Một job giữ hàng đợi chỉ trong lúc làm đúng việc đó (chain sửa nhả `build` khi đang chờ model và ngược lại), nên chain của nhiều task đan xen nhau.
- **Ưu tiên job sâu hơn**: job khối 3–4 của task đã bắt đầu được chạy trước job khối 1 của task mới, để task xong sớm và workspace được xóa sớm (đỡ tốn đĩa).
- **Thứ tự Fixed/Adaptive xen kẽ** theo chỉ số task (lẻ: Fixed trước) như AR Mục 8.
- **Giới hạn gọi model**: `LlmGateway` giữ `rpm_limit` lượt mỗi phút và `--max-llm-calls`.
- **Tạm dừng / dừng**: `ControlGate` được kiểm tra trước mỗi bước (mỗi lệnh build, mỗi lượt gọi model). Tạm dừng: không bắt đầu bước mới. Dừng: hủy `CancellationToken`, tiến trình con bị giết cả cây.
- **Khóa ghi file**: mọi lần ghi `status.json`, ledger, events đi qua một khóa trong `StatusTracker`/`Ledger` (bài học bản cũ: Windows từ chối `File.Move` khi hai luồng cùng ghi).
- **Tải công cụ một lần**: tải file công cụ (ví dụ `junit-platform-launcher`) dùng khóa theo tên file, job khác chờ thay vì tải song song (bài học bản cũ: "file đang được tiến trình khác dùng" trên Windows).

## 7. Lưu trữ và chạy tiếp

| File | Ghi khi | Dùng để |
| --- | --- | --- |
| `run.lock.json` | Lần đầu tạo run | So cấu hình khi chạy tiếp (sha256 experiment, manifest, prompt; commit tool; JDK) |
| `ledger.jsonl` | Mỗi job xong (một dòng: job, outcome, mã lỗi, `needs_rerun`) | Chạy tiếp: bỏ qua job đã có |
| `events.jsonl` | Mỗi sự kiện đáng chú ý | Khung "Sự kiện" của S5, chẩn đoán |
| `status.json` | Mỗi 2 s (heartbeat) | S6 ở terminal khác; biết run còn sống |
| `tasks/…` | Theo spec 00 Mục 4 | Kết quả của từng khối |
| `llm_cache/` | Mỗi lượt gọi model thành công (khóa = `call_id` + request, kèm thời gian, token, chi phí gốc) | Phát lại khi chạy tiếp; không bao giờ dùng cho lượt gọi của chain/vòng khác |

**Chạy tiếp:** `ResumePlanner` đọc ledger. **Dòng ledger là việc ghi cuối cùng của mọi job**, chỉ ghi khi mọi file đầu ra của job đã lưu đủ (lớp `JobRunner` bảo đảm thứ tự: chạy → ghi đầu ra → ghi ledger). Job có dòng → bỏ qua. Job không có dòng (đang chạy dở khi dừng) → xóa đầu ra dở dang và chạy lại từ đầu job (chain sửa: từ vòng 1; lượt gọi model đã có trong `llm_cache/` được phát lại với số liệu gốc). Job `INFRA` chỉ chạy lại khi có `--rerun-infra`.

**Ghi nguyên tử:** ghi `<file>.<guid>.tmp` rồi đổi tên; tên `.tmp` có guid để hai luồng không đụng nhau.

## 8. Lớp gọi model (khối 2)

```
TestGenerator ──► LlmGateway ──► ReplayingLlmClient ──► LiteLlmClient ──HTTP──► LiteLLM proxy ──► provider
                  (giới hạn,     (khóa = call_id +        (gọi lại 20/60/120 s
                   kiểm model id, request; có thì phát     khi 429/5xx; lỗi
                   đếm lượt)      lại với số liệu gốc)    cấu hình → tạm dừng run)
```

Test thay `LiteLlmClient` bằng `FakeLlmClient` (luật: nếu prompt chứa X thì trả Y), không cần proxy, không tốn tiền. `LiteLlmProxy` bật proxy khi cần gọi model lần đầu và tắt khi run xong; khi bật xóa các biến môi trường gây lỗi (`DEBUG`), đặt `PYTHONUTF8=1` (bài học bản cũ trên Windows).

## 9. Lớp build và tiến trình

- `ProcessRunner`: chạy tiến trình với timeout, gom stdout+stderr, giết cả cây tiến trình khi timeout hoặc bị hủy; trả mã thoát, thời gian, có timeout không. Có cơ chế thử lại tối đa 3 lần cho lỗi hạ tầng (Q8), chờ 30 s giữa các lần.
- `BuildRunner`: dựng lệnh Maven/Gradle (wrapper nếu có), đặt `JAVA_HOME`, `PATH`, `JAVA_TOOL_OPTIONS` (locale `en_US`, múi giờ UTC, UTF-8), xóa `JDK_JAVA_OPTIONS`/`MAVEN_ARGS` của người dùng; ghi log từng lệnh (spec 00 Mục 6).
- `WorkspaceManager`: mirror (`git fetch --depth 1 <sha>`); checkout gốc của task (không build, không chạy test trong đó); `compiled_snapshot` (bản sao sau khi biên dịch, chưa chạy test); **bản sao mới của snapshot cho mỗi lần chạy test** (baseline, mỗi lần xác minh của khối 3, mỗi công cụ của khối 4), xóa ngay sau khi đọc kết quả. Sao chép bằng copy-on-write (APFS `clonefile`, reflink) khi có, nếu không thì copy thường. Xóa snapshot khi mọi job của task xong.
- `TestReportReader`: đọc `TEST-*.xml` của Surefire và Gradle (JUnit 4, JUnit 5, TestNG; mọi module trong reactor) thành danh sách `(test id, pass/fail/error/skip, exception type, message, trace)`; xử lý tên class/method khác nhau giữa hai công cụ.
- `JavacErrorParser` (Core): biểu thức nhận đường dẫn macOS, `/D:/…`, `D:\…` (spec 03 Mục 3.2).

## 10. java-helper

Công cụ Java nhỏ (JavaParser) chạy như **một tiến trình sống lâu** cho mỗi worker, nhận lệnh JSON từng dòng qua stdin và trả JSON từng dòng (tránh tốn 0,5–1 s khởi động JVM mỗi lần gọi):

| Lệnh | Dùng ở | Việc |
| --- | --- | --- |
| `parse` | Khối 2 | Kiểm tra cú pháp, đếm class top-level, tên class, package |
| `rename_class` | Khối 2 | Đổi tên class và tham chiếu |
| `strip_comments_hash` | Khối 2, 3 | Mã không comment/khoảng trắng thừa để tính hash |
| `count_assertions` | Khối 3 | Đếm assertion (AR Mục 5.2) |
| `list_test_methods` | Khối 3, 4 | Danh sách test method |
| `method_at_line` | Khối 3 | Method chứa một dòng (COMPILE → method lỗi) |
| `splice_methods` | Khối 2 (mức 3) | Ghép method viết lại vào checkpoint |
| `class_api` | Khối 3 (mức 2) | Chữ ký constructor/method/field của một class (từ mã nguồn hoặc jar) |
| `method_source` | Khối 3 (mức 2) | Mã nguồn method theo tên |
| `called_methods` | Khối 3 (mức 2) | Các method của CUT mà một test method gọi |

Jar được build từ `tools/java-helper` bằng Maven khi chưa có hoặc mã nguồn mới hơn jar; chạy bằng JDK 17.

## 11. Màn hình (Arrow.Console)

- `RunHost` chạy `RunPipeline` trong một `Task` nền; `LiveRunScreen` vẽ lại từ `StatusTracker.Snapshot()` mỗi 0,5 s (Spectre `LiveDisplay`); `KeyListener` đọc phím P/R/S/Q và gọi `ControlGate`.
- Không phải terminal (chuyển hướng output) hoặc `--plain` → `PlainPrinter` in sự kiện từng dòng.
- `arrow-fw control --run-id R pause|resume|stop` ghi file `control.json` trong thư mục run; `ControlGate` của run đang chạy đọc file này mỗi 2 s (điều khiển từ terminal khác).
- Màn hình chỉ đọc model dữ liệu của Core; không chứa logic quyết định.

## 12. Cấu hình, log, lỗi

- `ConfigLoader` kiểm tra cấu hình khi khởi động: sha256 manifest, đủ model trong `litellm.config.local.yaml`, JDK cần có, thư mục không dấu cách. Lỗi → thông báo có mã và cách sửa, không chạy.
- Log của tool: `logs/arrow-<ngày>.log` (mức Info; `--verbose` thêm Debug), qua `ToolLog` (ghi có khóa, che khóa API/`master_key` trước khi ghi).
- Mã lỗi: hằng trong `ErrorCodes` (Core), tiền tố theo khối (`B1_…`, `B2_…`, `B3_…`, `B4_…`), danh sách khớp các spec 01–04.

## 13. Thư viện

| Thư viện | Phiên bản | Dùng cho |
| --- | --- | --- |
| .NET SDK | 8 (roll forward lên bản mới hơn) | Toàn bộ |
| Spectre.Console, Spectre.Console.Cli | 0.49 | Màn hình, lệnh |
| xUnit | 2.9 | Test |
| JavaParser (trong java-helper) | 3.26 | Phân tích mã Java |
| LiteLLM (Python, ngoài .NET) | 1.91.0 | Proxy gọi model |
| JaCoCo / PIT (+ junit5 plugin) / tsDetect / EvoSuite | 0.8.12 / 1.17.4 (+1.2.1) / 2.2 / 1.2.0 | Khối 4 |

Không dùng thư viện JSON ngoài: `System.Text.Json` (snake_case). Không dùng ORM hay cơ sở dữ liệu: mọi kết quả là file JSON/CSV trong thư mục run.

## 14. Thứ tự viết code (mỗi bước có test, build không cảnh báo, test pass)

| Bước | Nội dung | Xong khi |
| --- | --- | --- |
| C1 | Solution, Core/Common, Config, Manifest, RunStore, Ledger, ProcessRunner, ToolLog | Unit test cấu hình, ledger, ghi nguyên tử |
| C2 | java-helper (Java) + JavaHelperClient | Test từng lệnh trên mã mẫu |
| C3 | Khối 1: build root, RepositoryAnalyzer, snapshot, Baseline, tiêu chí B, lệnh `screen` | Integration trên 4 fixture (gồm build root là thư mục con, module phụ thuộc module anh em): M đủ 6 thông tin, baseline đúng, tiêu chí B đúng; chạy `screen` trên manifest pilot v2: kết quả khớp sàng lọc pilot v2 (50/50 PASS) hoặc liệt kê rõ CUT khác biệt |
| C4 | Khối 3 phần xác minh: SuiteFiles, Verifier, JavacErrorParser, TestReportReader, Diagnoser | Fixture với candidate cố ý sai ra đủ 6 trạng thái; một bộ kiểu EvoSuite (`*_ESTest` + scaffolding) được xác minh đúng |
| C5 | Khối 2: PromptBuilder, CodeExtractor, LLM stack, FakeLlmClient | Unit test prompt 3 quy trình; HTTP giả |
| C6 | Khối 3 phần sửa: Adaptive/Fixed policy, RepairRunner, ExtraContextBuilder | Ví dụ spec 03 Mục 7, 14, 15 thành unit test; e2e một chain với model giả |
| C7 | Khối 4: chuẩn bị bộ test (ghi + biên dịch + classpath), EvoSuite, JaCoCo, PIT, tsDetect, ReportBuilder | Đo được trên 3 fixture (gồm đa module với `-am`); PIT chỉ có mutant của CUT; báo cáo từ dữ liệu mẫu |
| C8 | Pipeline, Scheduler, ResumePlanner, ControlGate | E2E `run` 4 khối với model giả; dừng giữa chừng rồi chạy tiếp |
| C9 | Màn hình S1–S9, setup, models, doctor | Chạy tay từng màn hình; `--plain` |
| C10 | Smoke với model thật (2 CUT, giới hạn lượt gọi) | Chạy qua 4 khối, có báo cáo |
