# pilot_tool: công cụ chạy pilot của ARROW

**Trạng thái:** đang code; mốc M1, M2, M3 xong (13/10/2026), tiến độ chi tiết ở `PROGRESS.md`. Tài liệu này mô tả tool làm gì và làm như thế nào; code viết theo tài liệu này. Khi thiết kế thay đổi, sửa tài liệu trước rồi mới sửa code.

Tài liệu liên quan: `ARCHITECTURE.md` (kiến trúc code), `CLI_DASHBOARD.md` (dashboard trong terminal), `SETUP.md` (cài đặt máy, chia shard, gom kết quả), `specs/` (spec chi tiết để code; các điểm chờ xác nhận ở `specs/00-INDEX.md`).

## 0. Bắt đầu nhanh (mốc M1, M2 đã có)

Cần .NET SDK 8 trở lên (máy chỉ có SDK mới hơn vẫn được). Từ thư mục `ARROW/pilot_tool`:

| Việc | Lệnh |
| --- | --- |
| Build | `dotnet build ArrowRunner.sln` |
| Mở menu (lần đầu tự vào cài đặt 8 bước) | `dotnet run --project src/Arrow.Cli` |
| Danh sách lệnh | `dotnet run --project src/Arrow.Cli -- --help` |
| Thử dashboard với job giả (P tạm dừng, R tiếp tục, S dừng) | `dotnet run --project src/Arrow.Cli -- run --demo` |
| Chạy shard của máy (M2: bước S0 chuẩn bị) | `dotnet run --project src/Arrow.Cli -- run` |
| Chạy lại task lỗi hạ tầng | `dotnet run --project src/Arrow.Cli -- run --rerun-infra` |
| Unit test | `dotnet test tests/Arrow.Core.Tests` |
| Integration test (Maven, Gradle thật trên `tests/fixtures`, cần `machine.local.json`) | `dotnet test tests/Arrow.Infrastructure.Tests` |

Chép đúng phần lệnh; không chép kèm chú thích (zsh trên macOS mặc định không coi `#` là chú thích).

**Cài lệnh `arrow`** (một lần trên mỗi máy, sau đó gõ `arrow` ở thư mục nào cũng được; sau `git pull` không cần cài lại):

| Hệ điều hành | Lệnh (chạy trong `ARROW/pilot_tool`) |
| --- | --- |
| macOS, Linux | `sh install/install-arrow.sh` |
| Windows | `powershell -ExecutionPolicy Bypass -File install\install-arrow.ps1` |

Sau đó mở terminal mới: `arrow` (menu), `arrow --help`, `arrow run --demo`.

**Cài LiteLLM** (chương trình trung gian để gọi model; một lần trên mỗi máy; cài vào `ARROW/litellm`, không commit; phiên bản 1.91.0 theo Report 2):

| Hệ điều hành | Lệnh (chạy trong `ARROW/pilot_tool`) |
| --- | --- |
| macOS, Linux | `sh install/install-litellm.sh` |
| Windows | `powershell -ExecutionPolicy Bypass -File install\install-litellm.ps1` |

Cần Python 3.10 trở lên (macOS: `brew install python@3.12`; Windows: python.org).

M1 có: menu, `setup`, `doctor`, `shard plan`, `control`, `runs`, `status`, dashboard trong terminal với job giả, log của tool (`pilot_tool/logs/arrow-<ngày>.log`, thêm `--verbose` để ghi chi tiết; khi gặp lỗi, gửi file này cho người điều phối). M2 có: bước S0 chạy thật (`arrow run`): tải đúng commit vào mirror, tạo workspace riêng, dò module giống preflight, chọn JDK theo mức Java khai báo, build baseline, chạy test của module 2 lần, trích 6 thông tin repository; ghi `task.json`, `baseline.json`, `repo_context.json` trong `runs/<run_id>/tasks/…`. Sinh test và sửa test có từ M5, M6. Thư mục repository (mặc định `ARROW/repo`) phải không có dấu cách và chữ có dấu; nếu đường dẫn ARROW có dấu cách thì đổi tên thư mục cha hoặc chọn `~/arrow-repo`.

## 1. Mục đích

Pilot là lượt chạy thử trên mẫu **tách khỏi tập đánh giá**, trước thực nghiệm chính, theo Report 2 (Mục 3). **Pilot chỉ dùng GPT-6 Luna** (chốt 12/10/2026); Qwen3-Coder-30B không chạy trong pilot. Pilot có hai phần:

| Phần | Câu hỏi cần trả lời | Dữ liệu |
| --- | --- | --- |
| **Phần 1: môi trường, công cụ, chi phí** | Repository có dựng lại được không? GPT-6 Luna qua LiteLLM có sẵn sàng không? Bộ xác minh 5 bước, EvoSuite, JaCoCo, PIT, tsDetect có chạy được trên output không? Mỗi bước tốn bao nhiêu thời gian, token, tiền, dung lượng? Khối lượng thực nghiệm chính có vừa với kế hoạch (308 ngày công, 14 tuần, hạn mức API) không? | 50 class Classes2Test + 9 CUT GitBug-Java |
| **Phần 2: chọn số vòng sửa tối đa N** | Sau bao nhiêu vòng thì sửa thêm gần như không còn ca thành công mới? | Ứng viên thất bại của 50 class Classes2Test |

Pilot **không** trả lời RQ nào. Pilot không so sánh Fixed với Adaptive, và kết quả pilot không gộp vào kết quả chính.

## 2. Đầu vào

| Đầu vào | Đường dẫn | Ghi chú |
| --- | --- | --- |
| Manifest pilot Classes2Test | `evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl` | 50 class / 50 repository, không trùng repository với tập đánh giá |
| Manifest pilot GitBug-Java | `evaluation_manifest/pilot/gitbug_java_pilot_9_seed42.jsonl` | 9 CUT, khác bug với CUT đánh giá |
| JDK 8, 11, 17, 21 | `Java-version/runtime/` | Chọn theo JDK preflight đã build thành công (trường `java_version` của manifest) |
| Cấu hình tool | `pilot_tool/appsettings.local.json` (không commit; mẫu `appsettings.example.json`) | Địa chỉ LiteLLM proxy và khóa của proxy, model, đường dẫn JDK, timeout, số worker |
| Cấu hình LiteLLM proxy | `pilot_tool/litellm.config.local.yaml` (không commit; mẫu `litellm.config.example.yaml`) | Model và **khóa API của GPT-6 Luna, lưu trực tiếp trong file** |
| Prompt | thư mục prompt của ARROW core (có version và hash) | Zero-shot, Few-shot, Zero-shot-project-aware; prompt sửa của Fixed và Adaptive |

Tool **chỉ đọc** manifest. Mọi thay đổi trên repository (ghi test sinh ra) diễn ra trong workspace riêng, không đụng vào bản clone gốc.

## 3. Các bước xử lý

```
manifest pilot
 └─ S0 Chuẩn bị      : clone @checkout_sha → baseline (compile + test suite của module) → repo_context (6 thông tin)
     ├─ S1 Sinh test  : 2 model × 3 prompt → ứng viên ban đầu → xác minh 5 bước          (phần 1)
     │   └─ S2 Sửa    : ứng viên thất bại × {Fixed, Adaptive}, tới N_max = 10 vòng          (phần 2, chỉ Classes2Test)
     ├─ S3 Hệ thống so sánh: EvoSuite (cấu hình mặc định) → xác minh 5 bước               (phần 1)
     ├─ S4 Chất lượng : JaCoCo, PIT, tsDetect trên bộ test hợp lệ                          (phần 1)
     └─ S5 Báo cáo    : chi phí và thời gian (phần 1); đường cộng dồn và N (phần 2)
```

### S0. Chuẩn bị repository

1. Clone repository, checkout đúng `checkout_sha` của manifest.
2. Chọn JDK theo JDK preflight đã dùng. Nếu module nhắm Java 8 nhưng dùng `--release` thì dùng JDK 11 (cùng quy tắc với `preflight_tool/scripts/rerun_fixed_probe.py`).
3. Đặt `JAVA_HOME` rồi mới chạy `mvn -v` hoặc `gradlew --version` để ghi phiên bản build tool. (Cột `build_tool_version` của manifest preflight không dùng được: luôn là `stdout` hoặc `timeout after 60s`.)
4. **Baseline:** compile và chạy toàn bộ test suite của module **trước khi** thêm test sinh ra; ghi danh sách test đã fail sẵn. Chạy baseline hai lần; test cho kết quả khác nhau giữa hai lần được đánh dấu flaky và không tính vào regression.
5. Trích 6 thông tin repository (build tool, Java version, dependencies, test framework, project structure, source/test path) vào `repo_context.json`. Dependencies lấy từ danh sách đã resolve (`mvn dependency:list`, `gradle dependencies`).

Lỗi ở S0 là **lỗi hạ tầng**: ghi lý do, không gọi model, không tính là thất bại của ARROW.

### S1. Sinh test ban đầu (giống thực nghiệm 1, repair tắt)

- Mỗi CUT × GPT-6 Luna (`reasoning_effort: none`, qua LiteLLM) × 3 prompt = 3 ứng viên. Tổng: 50 × 3 = 150 ứng viên Classes2Test, 9 × 3 = 27 ứng viên GitBug-Java.
- Ghi test vào test path đúng package; tên class test theo quy ước chốt ở Mục 8.
- **Xác minh 5 bước** (một bộ xác minh dùng chung cho mọi hệ thống): compile → discovery → execution → target test pass → không có regression mới trong module. Test suite của module chỉ chạy khi test mục tiêu đã pass.
- Trạng thái theo chuỗi `COMPILE → UNKNOWN → DISCOVERY → RUNTIME → ASSERTION → REGRESSION → VALID`; timeout và lỗi công cụ là `INFRA`, nằm ngoài chuỗi.

### S2. Sửa test để chọn N (phần 2, chỉ Classes2Test)

- Đầu vào: mọi ứng viên S1 của Classes2Test không đạt VALID. Mỗi cơ chế nhận một bản sao độc lập của cùng ứng viên và cùng trạng thái repository.
- Fixed Repair và Adaptive Repair chạy theo thiết kế đã chốt (CLAUDE.md, `draft/Adaptive repair.md`), **không dừng sớm**, tối đa `N_max = 10` vòng; dừng sớm hơn chỉ khi đạt VALID.
- Ghi log từng vòng (Mục 5).
- Tính N theo quy tắc chốt trước (Mục 4) trên **tỷ lệ cộng dồn chung của hai cơ chế**. Báo cáo pilot **không** in kết quả so sánh Fixed với Adaptive; số liệu theo từng cơ chế vẫn được lưu trong log nhưng không tổng hợp.

### S3. Hệ thống so sánh

- EvoSuite 1.2.0 cấu hình mặc định trên cùng CUT; ghi ngân sách tìm kiếm nhưng không quy đổi sang ngân sách của ARROW. EvoSuite runtime được thêm ở scope test, chỉ cho lượt chạy EvoSuite.
- Test do EvoSuite sinh ra đi qua **cùng bộ xác minh 5 bước** như ARROW.
- Mục tiêu ở pilot: xác nhận bộ xác minh và bộ đo xử lý được output của từng hệ thống, đo thời gian. Danh sách hệ thống so sánh được chốt sau pilot.

### S4. Chất lượng

Trên bộ test hợp lệ: JaCoCo 0.8.12 (line, branch, method coverage của CUT), PIT 1.17.4 (mutation score, chỉ chạy test sinh ra), tsDetect 2.2 (test smell). Ở pilot chỉ cần xác nhận công cụ chạy được và đo thời gian.

### S5. Báo cáo

- **Phần 1:** bảng thời gian trung vị và p90 của từng bước (clone, baseline, gọi model, build, xác minh, EvoSuite, JaCoCo, PIT, tsDetect); token và chi phí API của GPT-6 Luna; dung lượng đĩa; tỷ lệ lỗi hạ tầng và nguyên nhân; ước tính thời gian và chi phí cho phần GPT-6 Luna của thực nghiệm chính (600 + 96 ứng viên RQ1, cùng RQ2, RQ3). Phần Qwen của thực nghiệm chính không ước tính được từ pilot.
- **Phần 2:** bảng số ca sửa thành công cộng dồn theo vòng (chung hai cơ chế), N tính theo quy tắc, và N tương ứng nếu ngưỡng là 90% và 99% (phân tích độ nhạy).

## 4. Quy tắc chọn N (phải chốt trước khi chạy S2)

Quy tắc hiện ghi trong Report 2: N là vòng k nhỏ nhất (5 ≤ k ≤ 10) mà cả vòng k − 1 và vòng k không có thêm ứng viên sửa thành công trên tỷ lệ cộng dồn chung; không có vòng nào thỏa thì N = 10.

Quy tắc đang đề xuất thay thế (**chưa chốt**): N là vòng nhỏ nhất mà số ca sửa thành công cộng dồn chung đạt ≥ 95% số ca thành công sau 10 vòng, với 3 ≤ N ≤ 10.

Tool cài cả hai quy tắc, chọn bằng cấu hình (`n_rule = "report2" | "share95"`). Quy tắc dùng thật phải được ghi vào Report 2 và CLAUDE.md **trước** khi chạy S2; giá trị `n_rule` được khóa trong `run_config.lock.json`.

## 5. Đầu ra và log

```
pilot_tool/runs/<run_id>/                     (không commit; chỉ commit báo cáo tổng hợp)
  run_config.lock.json      commit của code, hash prompt, model id, phiên bản công cụ, seed, N_max, n_rule
  ledger.jsonl              trạng thái từng job (khóa: dataset, task_id, bước, model, prompt, cơ chế, vòng)
  tasks/<dataset>/<task_id>/
    baseline.json  repo_context.json
    gen/<model>/<prompt>/            prompt.txt  response.raw  cand_0.java  verify_0.json  usage.json
    repair/<model>/<prompt>/<fixed|adaptive>/round_<k>/
                                     prompt.txt  response.raw  cand.java  verify.json  decision.json
    evosuite/  quality/<system>/
  reports/
    part1_cost.csv  part1_infra_errors.csv  part2_cumulative.csv  pilot_report.md
```

`decision.json` mỗi vòng sửa ghi đủ các trường ở mục 11 của bảng Adaptive Repair trong CLAUDE.md: trạng thái, nhóm và loại con lỗi, error signature, code hash, mức strategy, `base_round`, rollback, mã trùng, `improved`, `checkpoint_round`, số test method pass, số assertion, thời gian gọi model và thời gian build.

Tool chạy tiếp được sau khi bị dừng: job đã có trong `ledger.jsonl` thì bỏ qua; mỗi response của model lưu theo hash của prompt, nên chạy lại không gọi model lần hai cho cùng prompt. Chỉ job lỗi hạ tầng được chạy lại (`--rerun-status INFRA`).

## 6. Công nghệ và cấu trúc (chốt 12/10/2026)

| Thành phần | Lựa chọn | Lý do |
| --- | --- | --- |
| Ngôn ngữ, runtime | **.NET 8 (LTS)**, C# | Chạy trên macOS (arm64) và Windows (x64) |
| Phạm vi | Toàn bộ phần chạy thực nghiệm (pilot và thực nghiệm chính) viết bằng .NET | Pilot và thực nghiệm chính chạy cùng một code. Preflight giữ nguyên bằng Python (đã chạy xong, manifest đã đóng băng) |
| ARROW core | Viết mới theo thiết kế đã chốt (CLAUDE.md, `draft/Adaptive repair.md`) | Code Python cũ ở `ARROW-paper/ARROW/src` chỉ để tham khảo cách gọi Maven/Gradle |
| Gọi model | Qua **LiteLLM proxy** (`litellm --config litellm.config.yaml`, HTTP chuẩn OpenAI) | Giữ đúng mô tả "gọi qua LiteLLM" trong báo cáo; Qwen qua Ollama sau này dùng cùng đường gọi |
| Phân tích Java | Một jar nhỏ dùng **JavaParser** (`tools/java-analyzer`), .NET gọi qua tiến trình | Đếm assertion, tìm method lỗi cho mức 3, trích API của CUT; .NET không có parser Java tốt |
| Dashboard | **Dashboard trong terminal** (Spectre.Console), nằm ngay trong CLI; phím tắt tạm dừng, tiếp tục, dừng hẳn (Mục 6b). Chi tiết màn hình: `CLI_DASHBOARD.md` (chốt 12/10/2026) | Một chương trình duy nhất, ít code, chạy được qua SSH; chi tiết từng task xem trong `runs/` hoặc báo cáo |

```
pilot_tool/
  ArrowRunner.sln
  src/
    Arrow.Core/        đọc manifest, chọn JDK, chạy Maven/Gradle, xác minh 5 bước, gọi LLM,
                       Fixed + Adaptive Repair, ledger, log từng vòng, file điều khiển
    Arrow.Infrastructure/  git, JDK, Maven/Gradle, LiteLLM, JavaParser, EvoSuite, JaCoCo, PIT, tsDetect
    Arrow.Cli/         lệnh chạy + dashboard trong terminal: prepare | generate | repair | evosuite |
                       quality | report | run | status | show | runs | control
  tests/Arrow.Core.Tests/
  tools/java-analyzer/ jar JavaParser
```

Chạy được như nhau trên macOS và Windows: gọi `mvnw` hoặc `mvnw.cmd`, `gradlew` hoặc `gradlew.bat` theo hệ điều hành; đường dẫn dùng `Path.Combine`; khi timeout thì dừng cả cây tiến trình; Windows bật long paths. Có thể xuất bản file chạy sẵn cho `osx-arm64` và `win-x64` (`dotnet publish -r <rid>`).

### 6a. Cách chạy (dự kiến)

```text
# Lần đầu trên mỗi máy: arrow setup (chọn shard, thư mục, JDK, khóa API) rồi arrow doctor. Xem SETUP.md.
litellm --config litellm.config.local.yaml                  # chỉ cần khi không để tool tự bật LiteLLM proxy
dotnet run --project src/Arrow.Cli -- prepare  --manifest ../evaluation_manifest/pilot/classes2test_pilot_50_seed42.jsonl --run-id pilot-c2t-<ngày>
dotnet run --project src/Arrow.Cli -- generate --run-id pilot-c2t-<ngày>
dotnet run --project src/Arrow.Cli -- repair   --run-id pilot-c2t-<ngày> --n-max 10
dotnet run --project src/Arrow.Cli -- evosuite --run-id pilot-c2t-<ngày>
dotnet run --project src/Arrow.Cli -- quality  --run-id pilot-c2t-<ngày>
dotnet run --project src/Arrow.Cli -- report   --run-id pilot-c2t-<ngày>
dotnet run --project src/Arrow.Cli -- run      --run-id pilot-c2t-<ngày>   # chạy lần lượt mọi bước, hiện dashboard
dotnet run --project src/Arrow.Cli -- status   --run-id pilot-c2t-<ngày>   # xem tổng quan một run (đang chạy hay đã xong)
```

GitBug-Java chạy cùng các lệnh với manifest pilot GitBug-Java, bỏ bước `repair`.

### 6b. Tạm dừng, tiếp tục, dừng hẳn

Có hai cách điều khiển, cùng tác dụng: **phím tắt trong dashboard terminal đang chạy** (`P` tạm dừng, `R` tiếp tục, `S` hoặc `Ctrl+C` dừng hẳn) và **lệnh `control`** từ terminal khác (`dotnet run --project src/Arrow.Cli -- control --run-id <id> pause|resume|stop`). Cả hai đều ghi vào file `runs/<run_id>/control.json` (`{"command": "pause" | "resume" | "stop", "requested_at": ...}`); CLI đọc file trước mỗi job và giữa các vòng sửa. Cách này chạy giống nhau trên macOS và Windows và không mở cổng mạng. Đóng cửa sổ terminal thì lần chạy dừng; chạy lại cùng `run_id` thì tiếp tục từ `ledger.jsonl`.

| Lệnh | CLI làm gì |
| --- | --- |
| Tạm dừng | Làm nốt bước đang chạy (một lượt gọi model, một lần build, hoặc một vòng sửa), rồi không bắt đầu job mới; trạng thái `PAUSED` |
| Tiếp tục | Chạy tiếp từ job kế tiếp |
| Dừng hẳn | Làm nốt bước đang chạy rồi thoát; chạy lại cùng `run_id` thì tiếp tục từ `ledger.jsonl` |

Không có lệnh ngắt ngay giữa một lần build hay một lượt gọi model, để không sinh kết quả dở dang. **Thời gian tạm dừng không tính vào số liệu:** thời gian sửa chỉ cộng thời gian gọi model và thời gian build của từng vòng; mỗi khoảng tạm dừng được ghi riêng (`pauses` trong `ledger.jsonl`).

## 7. Nguyên tắc thiết kế

1. **Dùng chung code với thực nghiệm chính.** Bộ xác minh, gọi model, prompt, Fixed Repair và Adaptive Repair là module của ARROW core; pilot_tool chỉ điều phối. Thực nghiệm chính chạy đúng code đã chạy pilot (khác manifest, khác `run_id`), để không bị hỏi "pilot chạy code khác".
2. **Đầu vào của mỗi bước là output đã đóng băng của bước trước**, kèm hash; không tính lại.
3. **Hai hàng đợi worker:** gọi model (GPT-6 Luna vài luồng, giới hạn theo hạn mức API) và build (2–3 luồng mỗi máy). Thiết kế vẫn hỗ trợ Qwen (1 luồng vì Ollama trên Mac) để dùng lại cho thực nghiệm chính.
4. **Workspace riêng cho mỗi job,** xóa sau khi xong; cache Maven/Gradle dùng chung.
5. **Chỉ được sửa test sinh ra.** Không sửa mã nguồn production, dependencies, cấu hình build hay test sẵn có. Cờ bỏ qua plugin định dạng (checkstyle, spring-javaformat, spotless) và jacoco khi build là thay đổi lệnh build, không phải sửa file; nếu dùng thì áp dụng như nhau cho mọi hệ thống và ghi trong `run_config.lock.json`.
6. **Pilot không xem so sánh hai cơ chế.**

## 7a. Hệ quả của việc chỉ dùng GPT-6 Luna

- N chọn từ ứng viên thất bại của GPT-6 Luna được áp dụng cho cả hai model trong thực nghiệm chính. Qwen có thể cần nhiều vòng sửa hơn, nên N có thể chưa tối ưu cho Qwen; đây là hạn chế phải ghi trong báo cáo.
- Pilot không kiểm tra Ollama, LiteLLM với Qwen, không đo thời gian một lượt gọi Qwen và không kiểm tra output Qwen bị cắt. Các việc này phải được kiểm tra riêng trước khi chạy phần Qwen của thực nghiệm chính, và không dùng để chỉnh N.
- Trong thực nghiệm chính, log từng vòng cho phép vẽ đường cộng dồn riêng cho Qwen; nếu ở vòng N Qwen vẫn còn tăng rõ thì báo cáo như một hạn chế, không chỉnh lại N sau khi thấy kết quả.
- Report 2 (Mục 3) đang ghi pilot kiểm tra "the two models"; cần sửa cho khớp.

## 8. Việc phải chốt trước khi viết code

| # | Việc | Ghi chú |
| --- | --- | --- |
| 1 | ~~Code ARROW core ở đâu~~ | **Đã chốt:** viết mới bằng .NET theo thiết kế (Mục 6) |
| 2 | Tên class test sinh ra | Đề xuất `<CUT>_ArrowTest` cùng package, để không ghi đè test người viết (cần cho regression và cho tham chiếu RQ3) |
| 3 | Quy tắc chọn N | `report2` hay `share95` (Mục 4) |
| 4 | Có bỏ qua plugin định dạng khi build không | Nếu có: cờ cụ thể, áp dụng cho mọi hệ thống |
| 5 | Máy chạy | Pilot chỉ gọi API GPT-6 Luna nên chạy được trên máy bất kỳ có JDK và Maven/Gradle; Windows cần bật long paths |
| 6 | Ví dụ few-shot | Lấy ngoài tập đánh giá và ngoài pilot, mỗi test framework một bộ |
