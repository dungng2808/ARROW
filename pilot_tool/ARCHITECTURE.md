# Kiến trúc code của pilot_tool

**Trạng thái:** đang code; M1–M4 xong (13/10/2026), tiến độ chi tiết ở `PROGRESS.md`. `README.md` mô tả tool làm gì; tài liệu này mô tả code được tổ chức và viết như thế nào. Sửa tài liệu trước, sửa code sau.

## 1. Kiểu kiến trúc

**Modular monolith, chia lớp theo hướng ports and adapters.** Một solution .NET 8, các project tách theo trách nhiệm; phần logic nghiệp vụ (trạng thái xác minh, quyết định của Adaptive Repair, so sánh checkpoint, quy tắc chọn N) là code thuần, không gọi tiến trình, không đọc file, nên kiểm thử được bằng unit test mà không cần Maven, Java hay API.

```
                ┌──────────────────────────────────────────┐
                │ Arrow.Cli (Spectre.Console)              │
                │  lệnh chạy + dashboard trong terminal    │
                └──────┬───────────────────────────────────┘
                       │  gọi use case, đọc trạng thái
                ┌──────▼───────────────────────────────────┐
                │ Arrow.Core                               │
                │  Application: Stage, Scheduler, Ledger,  │
                │               ControlGate, RunStore      │
                │  Domain:      model, VerificationState,  │
                │               RepairPolicy, Checkpoint,  │
                │               NRule (code thuần)         │
                │  Ports:       interface ra ngoài         │
                └──────┬───────────────────────────────────┘
                       │  cài đặt interface
                ┌──────▼───────────────────────────────────┐
                │ Arrow.Infrastructure                     │
                │  Git, JDK, Maven, Gradle, ProcessRunner, │
                │  LiteLLM client, JavaAnalyzer (jar),     │
                │  EvoSuite, JaCoCo, PIT, tsDetect, file IO│
                └──────────────────────────────────────────┘
```

Quy tắc phụ thuộc: `Cli` phụ thuộc `Core` và `Infrastructure`; `Infrastructure` phụ thuộc `Core` (để cài đặt interface); `Core` không phụ thuộc project nào khác. Nhờ vậy đổi cách gọi model, đổi build tool hay thay LLM thật bằng LLM giả để kiểm thử chỉ đụng tới `Infrastructure`.

## 2. Cấu trúc solution

```
pilot_tool/
  ArrowRunner.sln
  Directory.Build.props          net8.0, Nullable=enable, TreatWarningsAsErrors, RollForward
  .gitignore                     chặn file *.local.* (chứa khóa API), runs/, bin/, obj/
  appsettings.example.json       mẫu cấu hình; bản thật appsettings.local.json không commit
  experiments/                   cấu hình thực nghiệm dùng chung (commit), ví dụ pilot-20261015.json
  machine.local.json             cấu hình riêng của máy, do `setup` tạo (không commit)
  litellm.config.example.yaml    mẫu cấu hình LiteLLM proxy; bản thật litellm.config.local.yaml
                                 chứa khóa API, không commit
  prompts/                       template prompt, có version; hash ghi vào run_config.lock.json
    generation/{zero_shot,few_shot,zero_shot_project_aware}.md
    repair/fixed.md
    repair/adaptive/{compile,discovery,runtime,assertion,regression}.level{1,2,3}.md
    fewshot/{junit4,junit5,testng}.json
  src/
    Arrow.Core/
      Domain/                    record, enum, logic thuần
      Application/               stage, scheduler, ledger, control, timing
      Ports/                     interface
    Arrow.Infrastructure/
      Git/ Jdk/ Build/ Process/ Llm/ JavaAnalysis/ Baselines/ Quality/ Storage/
    Arrow.Cli/
      Commands/                  prepare, generate, repair, evosuite, quality, report, run,
                                 status, show, runs, control, setup, doctor, shard, export, merge
      Menu/                      menu chính và các bước của setup (SETUP.md)
      Dashboard/                 màn hình trực tiếp, phím tắt, bộ đọc trạng thái run
  tools/java-analyzer/           project Maven nhỏ, build ra java-analyzer.jar (JavaParser)
  tests/
    Arrow.Core.Tests/            unit test logic thuần
    Arrow.Infrastructure.Tests/  integration test trên repository mẫu
    fixtures/{sample-maven-junit4,sample-maven-multimodule-junit5,sample-gradle-junit5}/
  runs/                          output (trong .gitignore)
```

## 3. Mô hình dữ liệu chính (`Arrow.Core/Domain`)

Dùng `record` bất biến, serialize bằng `System.Text.Json` (snake_case để khớp JSON của preflight).

| Kiểu | Nội dung |
| --- | --- |
| `ManifestRecord` | `TaskId`, `RepoUrl`, `CheckoutSha`, `ModulePath`, `ClassPath`, `ClassFqn`, `TestClassPath`, `BuildTool`, `TestingFramework`, `JavaVersion`, `Dataset` |
| `RepoContext` | 6 thông tin repository: build tool, Java version, dependencies (đã resolve), test framework, project structure, source/test path |
| `Candidate` | `CandidateId`, `TaskId`, `Model`, `PromptStrategy`, `Round`, `JavaSource`, `CodeHash`, `ParentCandidateId` |
| `VerificationState` | enum theo chuỗi: `Compile < Unknown < Discovery < Runtime < Assertion < Regression < Valid`; `Infra` nằm ngoài chuỗi |
| `FailureGroup` / `FailureSubtype` | `Build`/`Runtime`/`Assertion` và loại con (COMPILE_METHOD, COMPILE_CLASS, DISCOVERY, EXCEPTION, REGRESSION, VALUE, EXPECTED_EXCEPTION) |
| `VerificationResult` | trạng thái, kết quả từng bước (5 bước), test method chạy/pass/fail, test có sẵn bị fail mới, thông báo lỗi đã rút gọn, `ErrorSignature`, thời gian build |
| `RepairRound` | các trường log ở mục 11 bảng Adaptive Repair (CLAUDE.md): trạng thái, signature, hash, mức, `BaseRound`, rollback, mã trùng, `Improved`, `CheckpointRound`, số method pass, số assertion, thời gian LLM, thời gian build |
| `JobKey` | `(Dataset, TaskId, Stage, Model, Prompt, Mechanism, Round)`: khóa duy nhất trong ledger |

## 4. Interface ra ngoài (`Arrow.Core/Ports`)

| Interface | Việc | Cài đặt trong Infrastructure |
| --- | --- | --- |
| `IManifestReader` | Đọc JSONL manifest, kiểm tra trường bắt buộc và checksum trong `.meta.json` | `JsonlManifestReader` |
| `IRepositoryProvider` | Clone mirror một lần mỗi repository, tạo workspace riêng mỗi job (`git worktree add --detach <sha>`), xóa workspace | `GitRepositoryProvider` |
| `IJdkResolver` | Chọn JDK theo `java_version` của manifest; Java 8 + `--release` → JDK 11 | `JdkResolver` |
| `IBuildTool` | Lệnh compile, test-compile, chạy test mục tiêu, chạy test suite của module; đọc báo cáo test | `MavenBuildTool`, `GradleBuildTool` |
| `IProcessRunner` | Chạy tiến trình có timeout, ghi log, dừng cả cây tiến trình | `ProcessRunner` |
| `IVerifier` | Xác minh 5 bước một ứng viên | `FiveStepVerifier` (dùng `IBuildTool`) |
| `ILlmClient` | Gửi prompt, nhận text, token, độ trễ | `LiteLlmClient` + `CachingLlmClient` (bọc ngoài) |
| `IPromptRenderer` | Dựng prompt từ template và dữ liệu | `TemplatePromptRenderer` |
| `IJavaAnalyzer` | API của CUT, đếm assertion, liệt kê test method, tìm method lỗi, ghép method viết lại vào class | `JavaAnalyzerProcess` (gọi jar) |
| `ITestGenerator` (hệ thống so sánh) | Sinh bộ test bằng công cụ khác | `EvoSuiteGenerator` |
| `IQualityMeter` | Coverage, mutation, smell | `JacocoMeter`, `PitMeter`, `TsDetectMeter` |
| `IRunStore` | Đường dẫn và ghi file trong `runs/<run_id>/`, ghi nguyên tử | `FileRunStore` |
| `IClock` | Thời gian hiện tại (thay được khi kiểm thử) | `SystemClock` |

## 5. Luồng xử lý

### 5.1. Stage và job

Mỗi bước S0–S5 trong README là một `IStage` gồm hai việc: liệt kê job cần làm (từ manifest và output đã đóng băng của bước trước) và xử lý một job. Một job là đơn vị nhỏ nhất được ghi vào ledger, ví dụ "sinh test cho task X bằng GPT-6 Luna với prompt zero-shot" hoặc "một vòng Adaptive Repair cho ứng viên Y".

```
for job in stage.EnumerateJobs():
    if ledger.IsDone(job.Key): continue          # chạy tiếp sau khi dừng
    await controlGate.WaitIfPausedAsync()        # nút tạm dừng
    if controlGate.StopRequested: break          # nút dừng hẳn
    result = await stage.RunAsync(job)           # bên trong: gọi model / build / xác minh
    runStore.WriteOutputs(job, result)           # ghi file trước
    ledger.Append(job.Key, result.Status)        # rồi mới đánh dấu xong
```

Ghi output trước, ghi ledger sau: nếu máy tắt giữa chừng, job chưa có trong ledger sẽ được chạy lại và ghi đè output dở.

### 5.2. Sửa test (S2)

`RepairRunner` chạy một ứng viên thất bại với một `IRepairPolicy`:

```
interface IRepairPolicy
    RepairStep NextStep(RepairHistory history)           // prompt nào, sửa từ bản nào, mức mấy
    RepairDecision Decide(RepairHistory history, Candidate c, VerificationResult r)
```

- `FixedRepairPolicy`: luôn prompt mức 1, luôn sửa từ bản mới nhất, không dùng hash, signature, checkpoint; mã trùng vẫn build lại.
- `AdaptiveRepairPolicy`: đúng thuật toán mục 4 của `draft/Adaptive repair.md` (checkpoint, rollback, leo mức 1→2→3, mã trùng không build, prompt lấy lỗi của checkpoint).
- `CheckpointComparer`: so sánh theo thứ tự bước xác minh → số test method pass → số assertion → vòng sớm hơn.

Hai policy là code thuần, nhận lịch sử và trả quyết định. Các ví dụ trong `draft/Adaptive repair.md` (vòng 1 RUNTIME, vòng 2 COMPILE → rollback, vòng 3 ASSERTION…) được viết thành unit test trước khi viết policy.

`RepairRunner` không dừng sớm (P không bật), chạy tới VALID hoặc hết `N_max`. Mỗi vòng là một job riêng trong ledger, nên tạm dừng giữa các vòng được và chạy tiếp đúng vòng kế tiếp.

### 5.3. Xác minh 5 bước

```
compile (test-compile của module, đã có test sinh ra)        → lỗi: Compile / Unknown
discovery (chạy riêng test class; runner có thấy test không) → lỗi: Discovery
execution (ít nhất một test method thật sự chạy)             → lỗi: Runtime
target pass (mọi test method của class sinh ra pass)         → lỗi: Runtime / Assertion
module regression (chạy test suite module, so với baseline)  → lỗi: Regression
```

Trạng thái lấy từ báo cáo XML (Surefire `TEST-*.xml`, Gradle `test-results`) trước, log console sau. `FailureClassifier` (code thuần) phân loại nhóm, loại con; `ErrorSignatureNormalizer` bỏ số dòng, đường dẫn, tên biến, giá trị; `CodeHasher` tính SHA-256 trên mã đã bỏ comment và khoảng trắng thừa, xuống dòng LF.

## 6. Chạy song song

- Hai hàng đợi dùng `System.Threading.Channels`: **hàng LLM** (số luồng theo hạn mức API của GPT-6 Luna) và **hàng build** (2–3 luồng mỗi máy). Một job sinh test đi qua hàng LLM rồi sang hàng build.
- Khóa theo repository cho thao tác git (clone mirror, `worktree add/remove`), vì git không cho hai thao tác cùng lúc trên một mirror.
- Mỗi job build có workspace riêng, nên hai job cùng repository không ghi đè nhau.
- Cache Maven (`~/.m2`) và Gradle dùng chung. Nếu gặp lỗi tranh chấp khi tải dependency, cấu hình cho phép tách `maven.repo.local` theo worker.
- Mọi hàm bất đồng bộ nhận `CancellationToken`; nút dừng hẳn không hủy bước đang chạy mà chờ bước đó xong (README Mục 6b).

## 7. Lưu trữ và chạy tiếp

- `runs/<run_id>/run_config.lock.json` ghi lúc bắt đầu: commit của code, hash từng template prompt, model id, tham số suy luận, phiên bản JDK, Maven, Gradle, EvoSuite, JaCoCo, PIT, tsDetect, `N_max`, `n_rule`, cờ build. Chạy tiếp mà cấu hình khác → từ chối, trừ khi dùng `run_id` mới.
- `ledger.jsonl` chỉ ghi thêm, mỗi dòng một job xong: khóa, trạng thái, thời gian, đường dẫn output. Mỗi dòng ghi xong thì flush.
- Mọi file JSON ghi theo kiểu ghi file tạm rồi đổi tên, để không có file ghi dở.
- `CachingLlmClient` lưu mỗi response theo `SHA-256(model + tham số + toàn bộ message)`; chạy lại không gọi API lần hai cho cùng prompt. Response đầu tiên là bản chính thức.
- Lỗi hạ tầng (API 429/5xx, timeout mạng, build timeout, lỗi git) được thử lại có giới hạn với backoff; vẫn lỗi thì ghi `INFRA` và không tính là thất bại của ARROW.

## 8. Tạm dừng, tiếp tục, dừng hẳn

- `ControlGate` đọc `runs/<run_id>/control.json` (theo dõi file và đọc lại mỗi 2 giây, vì theo dõi file trên macOS không phải lúc nào cũng báo).
- `WaitIfPausedAsync()` được gọi trước mỗi job; trạng thái `PAUSED`/`RUNNING`/`STOPPING` ghi vào `runs/<run_id>/status.json` để lệnh `status` và `runs` hiển thịboard hiển thị.
- `TimingRecorder` chỉ đo thời gian gọi model và thời gian build của từng job; khoảng tạm dừng ghi riêng, không cộng vào thời gian sửa.
- Lệnh `control --run-id <id> pause|resume|stop` của CLI ghi đúng file đó, dùng từ một terminal khác.
- Trong lúc chạy, CLI đọc phím từ console (`P` tạm dừng, `R` tiếp tục, `S` dừng hẳn) và cũng ghi vào `control.json`; `Ctrl+C` được bắt (`Console.CancelKeyPress`) và xử lý như `S`, không ngắt ngang bước đang chạy. Khi stdin không phải terminal (chạy nền, chuyển hướng), CLI bỏ qua đọc phím và chỉ theo dõi `control.json`.

## 9. Gọi model

- `LiteLlmClient` gọi LiteLLM proxy qua HTTP chuẩn OpenAI (`POST /v1/chat/completions`); địa chỉ proxy, tên model và khóa của proxy lấy từ `appsettings.local.json`.
- **Khóa API lưu trong file local, không dùng biến môi trường:** khóa GPT-6 Luna ghi trực tiếp trong `litellm.config.local.yaml` (`model_list[].litellm_params.api_key`); nếu proxy bật `master_key` thì khóa đó ghi trong `appsettings.local.json` (`Llm:ProxyKey`) để tool gửi kèm. Hai file khớp mẫu `*.local.*` trong `pilot_tool/.gitignore`, không bao giờ được commit.
- Khóa không được ghi vào log, `run_config.lock.json`, `ledger.jsonl` hay bất kỳ file nào trong `runs/`. `run_config.lock.json` chỉ ghi tên model và địa chỉ proxy. Trước khi commit, kiểm tra không có chuỗi khóa (`sk-…`) trong thay đổi.
- Tham số cho GPT-6 Luna: `reasoning_effort: none`, temperature 0, `max_tokens` theo cấu hình. Ghi lại model id do API trả về, token vào, token ra, độ trễ.
- Lấy mã Java từ response: bỏ markdown fence, kiểm tra cân ngoặc và đúng tên class; output không hợp lệ được ghi nhận như một ứng viên lỗi (trong sửa test: tính một vòng, không build, lên mức).

## 10. Phân tích Java (`tools/java-analyzer`)

- Project Maven nhỏ, Java 11, JavaParser; build ra một jar có đủ dependency.
- Chạy như tiến trình sống lâu, mỗi worker một tiến trình, nhận và trả JSON từng dòng qua stdin/stdout, để không tốn thời gian khởi động JVM cho mỗi lần gọi.
- Lệnh: `cut_api` (constructor, method của CUT và class liên quan), `count_assertions` (`specs/03` Mục 8, gồm RestAssured), `strip_comments` (bỏ comment theo AST để tính code hash), `list_test_methods`, `method_at_line` (method chứa một dòng lỗi biên dịch), `called_methods` (method của CUT mà một test method gọi), `splice_methods` (mức 3: thay các method lỗi trong checkpoint bằng bản trong response, thêm import/field/helper mới, bỏ method lỗi không còn trong response; `specs/05` Mục 5.3).

## 11. Dashboard trong terminal

Chi tiết từng màn hình và từng lệnh: `CLI_DASHBOARD.md`. Về code:

- Dùng `Spectre.Console` (`AnsiConsole.Live` + `Layout`, `Table`, `BarChart`), làm mới 2 lần mỗi giây; không thêm thư viện TUI khác.
- Màn hình chỉ **đọc** trạng thái qua `RunStatusReader` (ledger, `status.json`, các file trong `runs/`); nó không gọi trực tiếp stage hay scheduler. Nhờ vậy cùng một màn hình dùng được cho run đang chạy (lệnh `run`) và run đã xong (lệnh `status`).
- Đọc phím bằng `Console.KeyAvailable`/`ReadKey(intercept: true)` trong một vòng riêng; phím chỉ ghi `control.json`.
- Khi stdout không phải terminal hoặc có cờ `--plain`, không vẽ màn hình mà in từng dòng sự kiện (phù hợp chạy nền, ghi log).
- Với run có tiền tố `pilot-`, màn hình **không** hiển thị số liệu tách theo Fixed và Adaptive; chỉ hiển thị số liệu chung hai cơ chế.

## 12. Chạy trên macOS và Windows

| Vấn đề | Cách xử lý |
| --- | --- |
| Tên wrapper | `mvnw`/`gradlew` trên macOS, `mvnw.cmd`/`gradlew.bat` trên Windows; không có wrapper thì dùng `mvn`/`gradle` trong PATH |
| Đường dẫn | Chỉ dùng `Path.Combine`, không nối chuỗi với `/` hay `\` |
| Dừng tiến trình | `Process.Kill(entireProcessTree: true)` |
| Xuống dòng | Chuẩn hóa về LF trước khi tính hash và so sánh; file test ghi bằng LF |
| Đường dẫn dài trên Windows | `git config core.longpaths true` cho mirror và workspace; hướng dẫn bật LongPathsEnabled |
| Xóa workspace trên Windows | Thử lại khi file đang bị khóa (Maven/Gradle daemon); Gradle chạy với `--no-daemon` |
| Quyền chạy wrapper trên macOS | Kiểm tra và đặt quyền thực thi cho `mvnw`/`gradlew` sau khi checkout |
| JDK | Đường dẫn JDK theo hệ điều hành trong `appsettings.local.json`, giống `Java-version/config.local.toml` |

## 13. Thư viện dùng

| Thư viện | Dùng cho |
| --- | --- |
| Microsoft.Extensions.Hosting, DependencyInjection, Options, Configuration | Khởi tạo, DI, cấu hình |
| Spectre.Console, Spectre.Console.Cli | Lệnh CLI và hiển thị tiến độ trong terminal |
| Serilog (Console, File) | Log của tool |
| System.Text.Json, System.Threading.Channels | Có sẵn trong .NET |
| xUnit, FluentAssertions | Kiểm thử |
| JavaParser (trong jar Java) | Phân tích mã Java |

## 14. Quy ước code

- `Nullable` bật, cảnh báo coi như lỗi, namespace theo file.
- Dữ liệu là `record` bất biến; không dùng biến static giữ trạng thái.
- Mọi I/O là `async` và nhận `CancellationToken`.
- Logic quyết định (trạng thái, phân loại lỗi, policy sửa, so sánh checkpoint, quy tắc N) nằm trong `Domain`, không gọi I/O.
- Tên class, method, biến bằng tiếng Anh; chú thích và tài liệu bằng tiếng Việt.

## 15. Kiểm thử

| Loại | Nội dung | Cần gì |
| --- | --- | --- |
| Unit (`Arrow.Core.Tests`) | Thứ tự trạng thái, phân loại lỗi, chuẩn hóa signature, hash, so sánh checkpoint, Fixed và Adaptive policy (theo ví dụ trong `draft/Adaptive repair.md`), quy tắc N (cả `report2` và `share95`), ControlGate | Không cần gì ngoài .NET |
| Integration (`Arrow.Infrastructure.Tests`) | Chạy Maven và Gradle thật trên 3 repository mẫu trong `tests/fixtures`: test đúng → VALID; lỗi compile, test không được phát hiện, exception, assertion, phá test có sẵn → đúng trạng thái | JDK, Maven, Gradle |
| End-to-end | Chạy cả pipeline trên repository mẫu với `FakeLlmClient` trả response soạn sẵn; kiểm tra ledger, chạy tiếp sau khi dừng, tạm dừng/tiếp tục | Không tốn tiền API |
| Smoke với model thật | 1–2 class pilot, gọi GPT-6 Luna thật | Khóa API, LiteLLM proxy |

Test chạy được trên cả macOS và Windows; nếu dùng GitHub Actions thì chạy ma trận `macos-latest` và `windows-latest`.

## 16. Thứ tự làm

| Mốc | Nội dung | Xong khi |
| --- | --- | --- |
| M1 | Khung solution, cấu hình thực nghiệm và cấu hình máy, menu chính, `setup`, `doctor`, `shard plan`, `ManifestRecord` + đọc manifest, `RunStore`, ledger, `ControlGate`, lệnh `control`, lệnh `runs` và khung dashboard terminal với phím điều khiển | Đọc được 2 manifest pilot; tạm dừng/tiếp tục một stage giả hoạt động trên macOS và Windows |
| M2 | S0: git mirror + worktree, `JdkResolver`, `ProcessRunner`, `MavenBuildTool`, `GradleBuildTool`, baseline 2 lần, `RepoContext` | S0 chạy xong 59 CUT pilot; lỗi hạ tầng có lý do |
| M3 | `FiveStepVerifier`, `FailureClassifier`, signature, hash; integration test trên repository mẫu | Mọi trạng thái của chuỗi xác minh được tái hiện đúng trên repository mẫu |
| M4 | `java-analyzer.jar` và `IJavaAnalyzer` | Đếm assertion và ghép method đúng trên bộ test mẫu |
| M5 | `LiteLlmClient` + cache, template prompt, S1 sinh test | Sinh và xác minh 177 ứng viên pilot (smoke trên 2 class trước) |
| M6 | `FixedRepairPolicy`, `AdaptiveRepairPolicy`, `RepairRunner`, S2, quy tắc N | Unit test policy pass; S2 chạy được với `FakeLlmClient` rồi với GPT-6 Luna |
| M7 | S3 EvoSuite, S4 JaCoCo, PIT, tsDetect qua cùng bộ xác minh | Có số đo trên các bộ test hợp lệ của pilot |
| M8 | `export`, `merge`, S5 báo cáo, dashboard terminal đầy đủ (lệnh `show`, vòng sửa, đường cộng dồn) | `pilot_report.md` và các CSV của README Mục 3 |

Code chuyển từ phần đã làm bằng Python: cách chọn lệnh Maven multi-module (`-pl <module> -am`, fallback khi module không thuộc reactor) và lệnh Gradle theo project path lấy từ `ARROW-paper/ARROW/src/build_runner.py`; quy tắc chọn JDK và cờ bỏ qua plugin lấy từ `preflight_tool/scripts/rerun_fixed_probe.py`.

## 17. Việc còn chờ chốt ảnh hưởng tới code

| Việc (README Mục 8) | Ảnh hưởng | Cần trước mốc |
| --- | --- | --- |
| Tên class test sinh ra | `rename_class`, đường dẫn ghi test, chạy riêng test mục tiêu | M3 |
| Bỏ qua plugin định dạng khi build | Cờ trong `MavenBuildTool`, ghi vào `run_config.lock.json` | M3 |
| Ví dụ few-shot | `prompts/fewshot/*.json` | M5 |
| Quy tắc chọn N | `n_rule` mặc định; cả hai quy tắc đều được cài | Trước khi chạy S2 thật |
