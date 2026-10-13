# Spec chi tiết của pilot_tool

**Trạng thái:** bản nháp 1 (12/10/2026). Đây là spec để code. Mỗi file mô tả đủ để viết code và test cho một phần. Khi spec và tài liệu tổng quan (`README.md`, `ARCHITECTURE.md`, `CLI_DASHBOARD.md`, `SETUP.md`) khác nhau, **spec thắng**; sửa tài liệu tổng quan cho khớp.

| File | Phần | Dùng cho mốc |
| --- | --- | --- |
| `01-config-and-data.md` | Cấu hình thực nghiệm, cấu hình máy, shard, thư mục run, ledger, file điều khiển, bundle export | M1 |
| `02-prepare-and-build.md` | S0: tải repository, chọn JDK, lệnh Maven/Gradle, baseline, trích 6 thông tin repository | M2 |
| `03-verification.md` | Xác minh 5 bước, trạng thái, phân loại lỗi, error signature, code hash, đếm assertion | M3, M4 |
| `04-llm-and-prompts.md` | Gọi model qua LiteLLM, lấy mã từ response, nội dung các prompt sinh test và sửa test | M5 |
| `05-repair.md` | Thuật toán Fixed Repair và Adaptive Repair (bản thống nhất), checkpoint, mức, log từng vòng | M6 |
| `06-evosuite-and-quality.md` | EvoSuite, JaCoCo, PIT, tsDetect | M7 |
| `07-report-and-n.md` | Báo cáo phần 1, đường cộng dồn, quy tắc chọn N | M8 |
| `08-logging.md` | Log của tool, log thực nghiệm, log build: ở đâu, định dạng, giữ và xuất | M1–M8 |

Quy ước: `MUST` là bắt buộc, `SHOULD` là nên làm. Nhãn `[Dn]` trong các spec trỏ tới quyết định ở bảng dưới; mọi quyết định D1–D12 đã được người dùng chốt ngày 12/10/2026 theo giá trị trong bảng.

## Các điểm đã chốt (12/10/2026)

| Mã | Điểm | Mặc định trong spec | Ở file |
| --- | --- | --- | --- |
| D1 | Tên class test sinh ra | `<TênCUT>_ArrowTest`, cùng package với CUT, trong test path của module | 03 |
| D2 | Bỏ qua plugin kiểm tra định dạng, giấy phép, coverage tối thiểu của chính repository khi build | Có (danh sách cờ ở `02` Mục 4.3); áp dụng như nhau cho mọi hệ thống, ghi vào cấu hình thực nghiệm. JaCoCo của tool chạy riêng ở bước đo chất lượng | 02 |
| D3 | Prompt quy trình thông thường có cho biết test framework không | **Không**: zero-shot và few-shot chỉ có mã CUT, tên class test và package; framework, Java version, dependencies, cấu trúc, test path chỉ có ở zero-shot-project-aware. Bước sửa import JUnit 4↔5 của code (áp dụng cho mọi quy trình) vẫn giữ theo bản chuẩn | 04 |
| D4 | Ví dụ few-shot | 2 cặp (CUT + test người viết) **cố định cho mọi CUT**, không chọn theo framework của CUT; lấy từ pool Classes2Test ngoài 200 repository đánh giá và 50 repository pilot | 04 |
| D6 | Nội dung mức 2 của trạng thái UNKNOWN | Khối lỗi của goal/task kèm `Caused by`; cấu hình bước test (Surefire/compiler hoặc task `test` của Gradle); test engine trên classpath và Java version; dấu hiệu nhận ra trong log (`04` Mục 6.2) | 04 |
| D8 | Quy tắc chọn N | `share95` (≥ 95% số ca thành công của 10 vòng, 3 ≤ N ≤ 10); `report2` vẫn được cài | 07 |
| D9 | Ngân sách EvoSuite | Cấu hình mặc định của EvoSuite 1.2.0 (`search_budget` 60 giây, criterion mặc định), seed cố định 42 | 06 |
| D10 | Timeout khi chạy test (bước 2–4) | Là lỗi hạ tầng như build timeout: build lại 1 lần, vẫn lỗi thì `INFRA`. Test tự đặt `@Timeout` thì vẫn là lỗi của test | 03 |
| D11 | Error signature của UNKNOWN | `UNKNOWN \| <plugin id trong "Failed to execute goal", hoặc dòng ERROR đầu tiên đã chuẩn hóa> \| -` | 03 |
| D12 | Câu chữ prompt sửa và cách hiển thị các lần đã thử | Câu chữ tiếng Anh trong `04` Mục 6; lần đã thử chỉ ghi kết quả và signature, không kèm mã | 04 |

## Đã giải quyết theo bản chuẩn (12/10/2026)

| Mã cũ | Điểm | Theo `ARROW/Adaptive repair.md` |
| --- | --- | --- |
| D5 | Prompt sửa có kèm 6 thông tin repository không | **Không.** Mức 1 (và Fixed) = lỗi + mã test + source focal class; mức 2 của COMPILE mới thêm dependencies và Java version (AR 3.1) |
| D7 | Mức 3 có được xóa test method không | LLM trả cả class; ARROW ghép các method lỗi vào checkpoint; method lỗi không còn trong response thì bị bỏ; method bị bỏ = có trong c0 mà không còn ở bản cuối (AR 3.1) |

## Chờ xác nhận (tool tự quyết khi code, 13/10/2026)

| Mã | Điểm | Tool đang làm | Ở file |
| --- | --- | --- | --- |
| P1 | Bước chuẩn hóa tên class chỉ nói `public class`; test JUnit 5 thường không public | Không có `public class` thì lấy class cấp cao nhất không public; vẫn phải đúng một class tên `<CUT>_ArrowTest` | 04 Mục 3 |
| P2 | Test kiểu JUnit 5 (class/method không public) bị đổi import sang JUnit 4 | Không tự thêm `public`; JUnit 4 báo `InvalidTestClassError` → RUNTIME (đúng spec, không sửa thêm) | 04 Mục 3 |
| P3 | JUnit 4 báo class không có `@Test` bằng test case `initializationError` "No runnable methods" | Coi là DISCOVERY, không phải RUNTIME | 03 Mục 2 |
| P4 | Few-shot: điều kiện "chạy độc lập, không cần dịch vụ ngoài" | Lọc theo danh sách dấu hiệu trong mã test (không chạy test) | 04 Mục 5 |
| P5 | LiteLLM 1.91.0 từ chối `reasoning_effort` với model `gpt-6-luna` (`UnsupportedParamsError`) | `litellm.config.local.yaml` thêm `allowed_openai_params: ["reasoning_effort"]` cho model; không dùng `drop_params` vì sẽ bỏ tham số | 04 Mục 1 |
| P6 | Ghi nguyên tử khi nhiều luồng cùng ghi một file (`control.json`) | Tên file tạm riêng `<file>.<pid>.<n>.tmp` | 01 |
| P7 | PIT: spec ghi Maven plugin và plugin Gradle `info.solidsoft.pitest` 1.15.0; plugin Gradle không chạy trên Gradle 9 | PIT bản dòng lệnh 1.17.4 cho cả Maven và Gradle, classpath test lấy từ build tool, không sửa POM; JUnit 5 thêm plugin 1.2.1 và `junit-platform-launcher` cùng phiên bản platform | 06 Mục 2.2 |
| P9 | JaCoCo: spec ghi `jacoco-maven-plugin:0.8.12` / plugin Gradle; project tự cấu hình JaCoCo làm đụng agent | Agent 0.8.12 của tool gắn qua `JAVA_TOOL_OPTIONS`, JaCoCo của project tắt bằng `-Djacoco.skip=true`, báo cáo bằng `jacococli`, cho cả Maven và Gradle | 06 Mục 2.1 |
| P11 | EvoSuite: spec ghi chạy bằng JDK 11 cho task Java 8 và 11 | Chạy bằng đúng JDK của task nếu ≤ 11 (EvoSuite 1.2.0 chạy được trên JDK 8); task 17/21 thử bằng JDK 11 | 06 Mục 1 |
| P10 | Sửa tool giữa pilot: run cũ khóa commit tool (`RUN_CONFIG_MISMATCH`) | `arrow run --import-from <run cũ>` tạo run mới, chép kết quả S0–S2 và cache model, ghi nguồn vào `imported.json`, chạy lại các bước sau | 01 Mục 4.1 |
| P8 | EvoSuite: test sinh ra dùng JUnit 4; module chỉ có JUnit 5 sẽ COMPILE/DISCOVERY | Giữ như spec (không thêm vintage); runtime EvoSuite thêm vào scope test chỉ ở job EvoSuite và job đo bộ test EvoSuite | 06 Mục 1 |

## Nguồn của thiết kế

- **`ARROW/Adaptive repair.md`** (viết tắt "AR" trong spec) là nguồn chuẩn cho thuật toán sửa, khớp với bảng Adaptive Repair trong `ARROW/CLAUDE.md`. Nhóm không dùng lại code cũ (`src/repair_v2` không có); tool .NET là bản triển khai mới theo AR và các spec này. Chỗ AR không nói rõ, spec quyết định và ghi thành điểm chờ xác nhận (D6, D10–D12).
- Report 2 (Mục 2.2, 2.3, 3, 7.1) cho định nghĩa 5 bước xác minh, pilot, RQ.
- Code Python cũ (`ARROW-paper/ARROW/src`) và `preflight_tool` cho các lệnh Maven/Gradle đã chạy được.
