# 07. Kiến trúc code: 3 tầng

**Mục đích:** mô tả cách tổ chức code C# (.NET 8, console) của ARROW Framework theo **kiến trúc 3 tầng**: UI, Service, Data Access. Các tầng nói chuyện với nhau **chỉ qua interface**.

## 1. Ba tầng

```
┌───────────────────────────────────────────────────────────────┐
│ Tầng UI (Arrow.UI)                                            │
│ Nhập bằng Console.ReadLine, xuất bằng Console.WriteLine       │
│ Menu, màn hình S1–S10, đọc tham số dòng lệnh                  │
└──────────────────────────────┬────────────────────────────────┘
                               │ gọi interface dịch vụ (IPipelineService, IReportService…)
┌──────────────────────────────▼────────────────────────────────┐
│ Tầng Service (Arrow.Service)                                  │
│ Thực thi logic: 4 khối, thuật toán Fixed/Adaptive, công thức  │
│ báo cáo, lập lịch job; gọi công cụ ngoài (git, Maven/Gradle,  │
│ Java, model, JaCoCo/PIT/tsDetect/EvoSuite) qua interface      │
└──────────────────────────────┬────────────────────────────────┘
                               │ gọi interface repository (IRunRepository, IConfigRepository…)
┌──────────────────────────────▼────────────────────────────────┐
│ Tầng Data Access (Arrow.DataAccess)                           │
│ Đọc, ghi dữ liệu của framework ra file: cấu hình, manifest,   │
│ prompt, ledger, trạng thái, kết quả từng khối, cache, báo cáo,│
│ log                                                           │
└───────────────────────────────────────────────────────────────┘

Arrow.Models: thư viện dùng chung chỉ chứa class dữ liệu (record, enum), không có logic; cả ba tầng đều dùng.
```

| Tầng | Làm | Không làm |
| --- | --- | --- |
| **UI** | Hiện thông tin, hỏi người dùng, đọc tham số dòng lệnh, gọi Service, in kết quả | Đọc/ghi file, chạy tiến trình, gọi model, chứa quy tắc nghiệp vụ |
| **Service** | Mọi logic: phân tích repository, dựng prompt, xác minh, sửa, đo, tính chỉ số; điều phối công cụ ngoài; lập lịch, tạm dừng, chạy tiếp | Đọc/ghi trực tiếp file dữ liệu của framework (luôn qua repository của Data Access); in ra màn hình |
| **Data Access** | Đọc, ghi file dữ liệu (JSON, JSONL, CSV, Markdown, YAML, text), ghi nguyên tử, khóa khi ghi đồng thời, che khóa API trong log | Logic nghiệp vụ, chạy tiến trình |

**Ranh giới về file:** Data Access giữ **dữ liệu của framework** (mọi thứ trong `experiments/`, `prompts/`, `runs/`, `logs/`, `machine.local.json`, `litellm.config.local.yaml`). **Mã nguồn của repository Java** và **workspace để build** (`repo/mirrors`, `repo/ws`) là môi trường làm việc của công cụ build, nên do tầng Service quản lý qua `IWorkspaceManager` (thư mục External). Báo cáo test XML mà Maven/Gradle sinh ra trong workspace cũng do Service đọc (`ITestReportReader`); kết quả đã phân tích thì Service ghi qua Data Access.

**Hướng phụ thuộc:** UI → Service → Data Access; cả ba → Models. UI **không** tham chiếu Data Access: Service cung cấp `ServiceFactory.Create(paths)` tự tạo các repository và trả về các interface dịch vụ cho UI.

## 2. Các project

```
ARROW_Framework/
  ArrowFramework.sln
  Directory.Build.props              .NET 8, nullable, cảnh báo là lỗi
  src/
    Arrow.Models/                    class dữ liệu dùng chung
    Arrow.DataAccess/                tầng Data Access
    Arrow.Service/                   tầng Service
    Arrow.UI/                        tầng UI (project console, file chạy arrow)
  tools/java-helper/                 công cụ Java (JavaParser) Service gọi qua tiến trình
  prompts/                           template prompt, ví dụ few-shot (Data Access đọc)
  experiments/                       cấu hình thực nghiệm (Data Access đọc/ghi)
  tests/
    Arrow.DataAccess.Tests/          đọc/ghi file trên thư mục tạm
    Arrow.Service.Tests/             logic thuần + Service với repository và công cụ giả
    Arrow.IntegrationTests/          Maven/Gradle thật trên fixture, chạy toàn luồng với model giả
    fixtures/                        dự án Java mẫu
```

Thư viện ngoài: **không** dùng thư viện giao diện. Chỉ .NET BCL (`System.Text.Json`, `System.Diagnostics.Process`, `HttpClient`) và xUnit cho test.

## 3. Arrow.Models (dữ liệu dùng chung)

```
Arrow.Models/
  Common/     Ids (TaskKey, CandidateKey, ChainKey, CallId), Outcome, ErrorCodes, VerificationState, ErrorGroup
  Config/     ExperimentConfig, ModelConfig, PriceConfig, MachineConfig, LiteLlmRoute, RunLock
  Manifest/   TaskRecord, BuildAttempt
  Block1/     RepositoryMetadata (M), Baseline, PairedTestResult, WorkspaceInfo, RepositoryAnalysis
  Block2/     GenerateRequest, RepairRequest, LlmRequest, LlmResponse, LlmUsage, CandidateTest
  Block3/     SuiteFiles, VerifyResult, Diagnosis, RoundDecision, FinalTestSuite
  Block4/     CoverageResult, MutationResult, SmellResult, QualityOutcome, ReportModel
  Run/        RunRequest, RunStatus, RunEvent, LedgerEntry, ControlCommand, RerunItem, TaskDetail, ScreeningRow
```

Tên trường JSON là snake_case (spec 00 Mục 4); các record này là hình dạng dữ liệu của mọi file JSON trong spec 01–04.

## 4. Tầng Service

### 4.1. Interface dịch vụ cho UI (UI chỉ gọi những interface này)

| Interface | Màn hình dùng | Việc |
| --- | --- | --- |
| `IPipelineService` | S5 | `Start(RunRequest, IProgress<RunEvent>) → RunHandle`; `Pause`, `Resume`, `Stop`; `GetSummary()` (bảng tổng khi gõ `t`) |
| `IScreeningService` | S10 | Chạy chỉ khối 1 cho một manifest, trả bảng tiêu chí B, ghi `screening.csv` |
| `IRunQueryService` | S1, S6, S7, S9 | `ListRuns`, `GetStatus`, `GetTaskDetail`, `GetRerunList`, `SendControl(runId, pause/resume/stop)` |
| `IReportService` | S8 | Tạo báo cáo cho một hoặc nhiều run, trả bản tóm tắt để UI in |
| `ISetupService` | S2 | Dò JDK/Maven/Gradle/Git, kiểm tra thư mục repository, đọc/lưu cấu hình máy |
| `IModelConfigService` | S3 | Xem model, lưu provider và khóa, đặt giá, thêm/bỏ model, gọi thử |
| `IDoctorService` | S4 | Kiểm tra môi trường, trả từng dòng qua callback; sửa các lỗi tự sửa được |

### 4.2. Interface của 4 khối (Service dùng nội bộ, khớp spec 01–04)

| Khối | Interface | Input → Output |
| --- | --- | --- |
| 1 | `IRepositoryAnalyzer` | `TaskRecord` → `RepositoryAnalysis` (M, baseline, snapshot, tiêu chí B) |
| 2 | `ITestGenerator` | `GenerateRequest` / `RepairRequest` → `CandidateTest` |
| 3 | `IVerifier` | `SuiteFiles` → `VerifyResult` (5 bước) |
| 3 | `IFailureDiagnoser` | `VerifyResult` → `Diagnosis` |
| 3 | `IRepairPolicy` (`AdaptiveRepairPolicy`, `FixedRepairPolicy`) | lịch sử vòng → `RepairRequest` tiếp theo, `RoundDecision` |
| 3 | `IRepairRunner` | c0 + cơ chế → `FinalTestSuite` (vòng lặp gọi khối 2) |
| 4 | `IComparisonGenerator` (EvoSuite), `IQualityTool` (JaCoCo, PIT, tsDetect), `IReportBuilder` | bộ test VALID → số đo → báo cáo |

### 4.3. Interface công cụ ngoài (thư mục `External/`)

| Interface | Bản thật | Bản giả cho test |
| --- | --- | --- |
| `IProcessRunner` | Chạy tiến trình, timeout, giết cả cây tiến trình, thử lại lỗi hạ tầng tối đa 3 lần | Trả output định sẵn |
| `IGitClient` | git fetch, worktree | Fixture git cục bộ |
| `IWorkspaceManager` | Mirror, checkout gốc, `compiled_snapshot`, bản sao mới cho mỗi lần chạy test, xóa | Thư mục tạm |
| `IBuildRunner` | Dựng và chạy lệnh Maven/Gradle (build root, bộ chọn module, cờ chung, JDK) | Kết quả định sẵn |
| `ITestReportReader` | Đọc `TEST-*.xml` của Surefire/Gradle trong workspace | XML mẫu |
| `IJavaHelper` | Tiến trình java-helper chạy lâu, giao tiếp JSON từng dòng | Kết quả định sẵn |
| `ILlmClient` | `LiteLlmClient` (HTTP tới proxy) bọc bởi `ReplayingLlmClient` (phát lại khi chạy tiếp, dùng `ILlmCacheRepository`) và `LlmGateway` (giới hạn, kiểm tra model id) | `FakeLlmClient` (luật: prompt chứa X thì trả Y) |
| `ILiteLlmProxy` | Bật/tắt proxy LiteLLM | Không bật gì |
| `IToolsCatalog` | Tải, kiểm tra sha256 của JaCoCo/PIT/tsDetect/EvoSuite | Thư mục công cụ giả |

### 4.4. Thư mục của Arrow.Service

```
Arrow.Service/
  Interfaces/       interface dịch vụ (4.1), interface khối (4.2), interface công cụ ngoài (4.3)
  App/              PipelineService, ScreeningService, RunQueryService, ReportService,
                    SetupService, ModelConfigService, DoctorService
  Block1/           RepositoryAnalyzer, BuildRootRule, JdkSelector, ManifestJavaVersionParser,
                    EffectivePomReader, GradleProbe, TestFrameworkDetector, PathCalculator,
                    BaselineRunner, PairedTestScreener, MetadataPromptRenderer
  Block2/           TestGenerator, PromptBuilder, CodeExtractor, NameNormalizer
  Block3/           Verifier, JavacErrorParser, TestResultClassifier, SignatureBuilder, FailureDiagnoser,
                    CheckpointComparer, AdaptiveRepairPolicy, FixedRepairPolicy, ExtraContextBuilder, RepairRunner
  Block4/           EvoSuiteGenerator, SuitePreparer (ghi + biên dịch + classpath), JacocoTool, PitTool, TsDetectTool,
                    JacocoCsvParser, PitXmlParser, TsDetectCsvParser, Rq1Calculator, Rq2Calculator, Rq3Calculator,
                    NRule, PSensitivity, ReportBuilder
  Pipeline/         JobGraph, Scheduler (2 hàng đợi), ControlGate, ResumePlanner, JobRunner
  External/         ProcessRunner, GitClient, WorkspaceManager, BuildRunner, TestReportReader, JavaHelperClient,
                    LiteLlmClient, ReplayingLlmClient, LlmGateway, LiteLlmProxy, ToolsCatalog
  ServiceFactory.cs tạo repository của Data Access, công cụ ngoài, rồi các service; trả interface dịch vụ cho UI
```

Logic thuần (không I/O: `PromptBuilder`, `CodeExtractor`, `JavacErrorParser`, `TestResultClassifier`, `SignatureBuilder`, `CheckpointComparer`, hai policy sửa, `JdkSelector`, `BuildRootRule`, các calculator, `NRule`) là các class không phụ thuộc gì ngoài Models, nên unit test trực tiếp bằng ví dụ trong spec.

## 5. Tầng Data Access

### 5.1. Interface repository

| Interface | File | Việc |
| --- | --- | --- |
| `IConfigRepository` | `experiments/*.json`, `machine.local.json`, `litellm.config.local.yaml` | Đọc cấu hình (kiểm sha256 manifest), ghi cấu hình máy và LiteLLM, sửa mục `models` của cấu hình thực nghiệm (giữ thứ tự khóa) |
| `IManifestRepository` | `evaluation_manifest/**/*.jsonl` | Đọc bản ghi `TaskRecord`, kiểm sha256 |
| `IPromptRepository` | `prompts/**` | Đọc template, ví dụ few-shot, tính sha256 template |
| `IRunRepository` | `runs/<run>/run.lock.json`, `ledger.jsonl`, `events.jsonl`, `status.json`, `control.json` | Tạo/mở run, ghi và so khóa cấu hình, thêm dòng ledger, đọc ledger, ghi sự kiện, ghi/đọc trạng thái, ghi/đọc lệnh điều khiển |
| `ITaskResultRepository` | `runs/<run>/tasks/<dataset>/<task>/block1..4/**` | Ghi/đọc kết quả từng khối: M, baseline, workspace info, outcome; prompt, response, usage, candidate; verify, decision, final; jacoco, pit, tsdetect; log của từng lệnh |
| `ILlmCacheRepository` | `runs/<run>/llm_cache/` | `TryGet(key)`, `Put(key, entry)` (entry gồm response và thời gian, token, chi phí gốc) |
| `IReportRepository` | `runs/<run>/reports/` | Ghi CSV (`rq1_verification.csv`, `rq2_*.csv`, `rq3_*.csv`, `needs_rerun.csv`, `screening.csv`…) và `report.md` |
| `ILogRepository` | `logs/arrow-<ngày>.log` | Ghi log của tool, che khóa API, `master_key`, `api_base` đầy đủ trước khi ghi |

### 5.2. Cách cài đặt

```
Arrow.DataAccess/
  Interfaces/   các interface ở 5.1
  Files/        FileConfigRepository, FileManifestRepository, FilePromptRepository, FileRunRepository,
                FileTaskResultRepository, FileLlmCacheRepository, FileReportRepository, FileLogRepository
  Common/       AtomicFile (ghi <file>.<guid>.tmp rồi đổi tên), JsonFile (System.Text.Json, snake_case, UTF-8, LF),
                JsonlAppender (thêm dòng có khóa), CsvWriter, YamlLiteLlm (đọc/ghi định dạng cố định của file LiteLLM), Redactor
```

- Mọi lần ghi đi qua `AtomicFile`; ghi nối vào JSONL và ghi `status.json` có khóa theo file (bài học bản cũ: Windows từ chối `File.Move` khi hai luồng cùng ghi).
- Đường dẫn trong file luôn dùng `/` và tương đối với gốc run hoặc gốc repository.
- Kiểm thử bằng thư mục tạm, không cần Java hay mạng.

## 6. Tầng UI

```
Arrow.UI/
  Program.cs          UTF-8, đọc tham số dòng lệnh, gọi ServiceFactory.Create, mở menu hoặc màn hình theo lệnh
  CommandLine.cs      tách lệnh và tùy chọn (`run --run-id R --tasks a,b`), không dùng thư viện
  ConsoleInput.cs     Choose, Ask, AskInt, Confirm, AskSecret (đều bằng ReadLine)
  ConsoleOutput.cs    Title, Line, Table, Status, Blank (WriteLine); có khóa để luồng nền và luồng chính không in chen nhau
  Screens/            MenuScreen (S1), SetupScreen (S2), ModelScreen (S3), DoctorScreen (S4), RunScreen (S5),
                      StatusScreen (S6), TaskScreen (S7), ReportScreen (S8), RerunScreen (S9), ScreeningScreen (S10)
```

Mỗi màn hình là một class nhận interface dịch vụ qua constructor, có một hàm `Run()`: hỏi bằng `ConsoleInput`, gọi Service, in bằng `ConsoleOutput`. Không màn hình nào chứa quy tắc nghiệp vụ.

**S5 khi đang chạy:** `RunScreen` gọi `IPipelineService.Start(request, progress)`; pipeline chạy ở luồng nền và báo `RunEvent` qua `IProgress<RunEvent>`, `RunScreen` in mỗi sự kiện một dòng (qua `ConsoleOutput`, có khóa). Luồng chính lặp `Console.ReadLine()` và gọi `Pause`/`Resume`/`Stop`/`GetSummary` theo chữ người dùng gõ (spec 06 Mục 7).

## 7. Luồng gọi giữa các tầng

### 7.1. Chạy một run (`arrow run`)

```
UI: RunScreen
  │ hỏi run id, tập task (ReadLine)
  │ IPipelineService.Start(request, progress)
  ▼
Service: PipelineService
  │ IConfigRepository.LoadExperiment / LoadMachine        ── Data Access đọc file cấu hình
  │ IManifestRepository.ReadTasks                         ── Data Access đọc manifest
  │ IRunRepository.OpenOrCreate + WriteOrVerifyLock       ── Data Access ghi run.lock.json
  │ ResumePlanner(IRunRepository.ReadLedger)              ── job nào đã xong
  │ JobGraph.Build → Scheduler chạy job:
  │    JobRunner: gọi service của khối → ITaskResultRepository.Save… → IRunRepository.AppendLedger (cuối cùng)
  │ báo RunEvent qua progress                             ── UI in một dòng
  ▼
UI: in tóm tắt khi xong
```

### 7.2. Một vòng sửa (trong job "chain" của khối 3)

```
RepairRunner (Service)
  ├─ IRepairPolicy.NextRequest()                     → RepairRequest
  ├─ ITestGenerator.RepairAsync(request)  (khối 2)    → ILlmClient (Replaying → Gateway → LiteLLM)
  │       ReplayingLlmClient: ILlmCacheRepository.TryGet/Put   (Data Access)
  ├─ Adaptive: mã trùng? → bỏ qua build
  ├─ IVerifier.VerifyAsync(SuiteFiles)                → IWorkspaceManager (bản sao mới), IBuildRunner, ITestReportReader
  ├─ IFailureDiagnoser.Diagnose(result)               → Diagnosis
  ├─ IRepairPolicy.Apply(outcome)                     → RoundDecision
  └─ ITaskResultRepository.SaveRound(prompt, response, usage, candidate, verify, decision)   (Data Access)
VALID hoặc hết N → ITaskResultRepository.SaveFinal(final) → IRunRepository.AppendLedger (cuối cùng)
```

## 8. Chạy song song, tạm dừng, chạy tiếp (trong tầng Service)

- **Hai hàng đợi** (`SemaphoreSlim`): `build` (`workers.build`, mọi lệnh Maven/Gradle/Java/EvoSuite/PIT) và `llm` (`workers.llm`). Job giữ hàng đợi chỉ trong lúc làm đúng việc đó.
- **Ưu tiên job sâu hơn** (khối 3–4 của task đã bắt đầu chạy trước khối 1 của task mới) để workspace được xóa sớm.
- **Fixed/Adaptive xen kẽ** theo chỉ số task (AR Mục 8).
- **ControlGate** được kiểm tra trước mỗi bước: tạm dừng = không bắt đầu bước mới; dừng = hủy `CancellationToken`, giết cây tiến trình. Lệnh từ terminal khác đọc qua `IRunRepository.ReadControl` mỗi 2 giây.
- **Chạy tiếp:** dòng ledger là việc ghi **cuối cùng** của mọi job (`JobRunner` bảo đảm: chạy → lưu đầu ra qua `ITaskResultRepository` → `AppendLedger`). Job có dòng ledger → bỏ qua; job chưa có → xóa đầu ra dở dang, chạy lại từ đầu job (chain: từ vòng 1, lượt gọi model đã có trong cache được phát lại với số liệu gốc). Job `INFRA` chỉ chạy lại khi có `--rerun-infra`.
- **Heartbeat:** `PipelineService` ghi `status.json` qua `IRunRepository` mỗi 2 giây (S6 ở terminal khác đọc).

## 9. Ghép các tầng (ServiceFactory)

`ServiceFactory.Create(ToolPaths paths, Options options)`:
1. Tạo repository của Data Access (`FileConfigRepository`, `FileRunRepository`…).
2. Tạo công cụ ngoài (`ProcessRunner`, `BuildRunner`, `LiteLlmClient` hoặc `FakeLlmClient` khi `--fake-llm`…).
3. Tạo service của 4 khối, rồi service cho UI, truyền phụ thuộc qua constructor (không dùng thư viện DI).
4. Trả về một object `ArrowServices` chỉ gồm các interface ở Mục 4.1.

Test thay bước 1 bằng repository trong bộ nhớ và bước 2 bằng công cụ giả.

## 10. Kiểm thử theo tầng

| Tầng | Test | Cần gì |
| --- | --- | --- |
| Data Access | Đọc/ghi từng loại file, ghi nguyên tử, ghi đồng thời từ nhiều luồng, che khóa API trong log, giữ thứ tự khóa khi sửa `models` | Thư mục tạm |
| Service: logic thuần | Mọi ví dụ trong spec 01–05 (chọn build root, JDK, prompt 3 quy trình, tách mã, phân loại lỗi, signature, ví dụ Fixed/Adaptive spec 03 Mục 7, 14, 15, công thức RQ, quy tắc N) | Không cần gì |
| Service: điều phối | `PipelineService`, `RepairRunner` với repository trong bộ nhớ, `FakeLlmClient`, `IBuildRunner` giả | Không cần Java |
| Tích hợp | Khối 1, 3, 4 với Maven/Gradle thật trên fixture; toàn luồng `run` với model giả; dừng giữa chừng rồi chạy tiếp | JDK, Maven, Gradle |
| UI | Chạy `arrow` với input chuyển hướng (`echo "4\n…" \| arrow`), so output với mẫu | File chạy đã build |

## 11. Thứ tự viết code (mỗi bước có test, build không cảnh báo, test pass)

| Bước | Nội dung | Xong khi |
| --- | --- | --- |
| C1 | Solution 4 project + 3 project test; Models; Data Access (mọi repository); `ConsoleInput`/`ConsoleOutput` | Test Data Access pass |
| C2 | java-helper (Java) + `IJavaHelper` | Test từng lệnh trên mã mẫu |
| C3 | Service khối 1: build root, JDK, phân tích, snapshot, baseline, tiêu chí B; `IScreeningService` | Integration trên fixture (gồm build root thư mục con, module không thuộc reactor); `screen` trên pilot v2 khớp sàng lọc pilot (50/50 PASS) hoặc liệt kê khác biệt |
| C4 | Service khối 3 phần xác minh: `SuiteFiles`, Verifier, phân loại lỗi, Diagnoser | Fixture với candidate cố ý sai ra đủ 6 trạng thái; một bộ kiểu EvoSuite xác minh đúng |
| C5 | Service khối 2: prompt, tách mã, `ILlmClient` (Replaying, Gateway, LiteLLM, Fake) | Unit test prompt 3 quy trình; HTTP giả |
| C6 | Service khối 3 phần sửa: hai policy, `RepairRunner`, `ExtraContextBuilder` | Ví dụ spec 03 Mục 7, 14, 15 thành unit test; một chain với model giả |
| C7 | Service khối 4: chuẩn bị bộ test, EvoSuite, JaCoCo, PIT, tsDetect, báo cáo | Đo được trên fixture; PIT chỉ có mutant của CUT; báo cáo từ dữ liệu mẫu |
| C8 | `PipelineService`, Scheduler, ResumePlanner, ControlGate, `IRunQueryService` | Toàn luồng `run` 4 khối với model giả; dừng rồi chạy tiếp |
| C9 | Tầng UI: S1–S10, `CommandLine`, `ServiceFactory` | Chạy tay từng màn hình; test UI với input chuyển hướng |
| C10 | Smoke với model thật (2 CUT, giới hạn lượt gọi) | Qua 4 khối, có báo cáo |
