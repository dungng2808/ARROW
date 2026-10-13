# ARROW Framework

**Trạng thái:** spec đã chốt (14/10/2026). Code: **xong bước C1** (khung 3 tầng, Models, toàn bộ Data Access, nhập/xuất console, kiểm tra bộ prompt) và **C2** (java-helper: công cụ Java dùng JavaParser, chạy thường trú, cùng client C# và trình chạy tiến trình). Các bước C3–C10 theo spec 07 Mục 11.

## Build và chạy

Cần .NET SDK 8 trở lên; java-helper cần JDK 17 (`machine.local.json` → `jdk_homes`, hoặc `ARROW/Java-version/runtime/jdk-17`) và Maven. Từ thư mục `ARROW_Framework`:

| Việc | Lệnh |
| --- | --- |
| Build | `dotnet build ArrowFramework.sln` |
| Test | `dotnet test ArrowFramework.sln` |
| Kiểm tra bộ prompt | `dotnet run --project src/Arrow.UI -- prompts check` |
| Trợ giúp | `dotnet run --project src/Arrow.UI -- --help` |
| Build java-helper (tự chạy khi jar chưa có hoặc cũ hơn mã nguồn) | `mvn -q -B -ntp -DskipTests package` trong `tools/java-helper` |
| Test java-helper | `mvn -q -B -ntp test` trong `tools/java-helper` |

Code bằng C# console (.NET 8), kiến trúc 3 tầng (UI / Service / Data Access), theo đúng các spec dưới đây.

ARROW Framework chạy quy trình ARROW trên các CUT của manifest, gồm 4 khối:

| Khối | Spec | Input | Output |
| --- | --- | --- | --- |
| 1. Repository-Aware | [specs/01-repository-aware.md](specs/01-repository-aware.md) | Bản ghi manifest (repository, commit, CUT) | Repository Metadata M (6 thông tin), baseline |
| 2. LLM-Based Test Generation | [specs/02-llm-test-generation.md](specs/02-llm-test-generation.md) | M, mã CUT, quy trình prompting, model; hoặc yêu cầu sửa từ khối 3 | Candidate Unit Test T |
| 3. Adaptive Repair Strategy | [specs/03-adaptive-repair.md](specs/03-adaptive-repair.md) | T, M, baseline, cơ chế sửa, N | Kết quả xác minh, log từng vòng, Final Test Suite |
| 4. Automated Test Suite Assessment | [specs/04-test-suite-assessment.md](specs/04-test-suite-assessment.md) | Bộ test VALID của ARROW và EvoSuite; kết quả khối 1–3 | Coverage, mutation, test smell; báo cáo RQ1–RQ3 |

Tổng quan (luồng dữ liệu, cấu hình, thư mục run, lệnh, kiến trúc code, các điểm chờ xác nhận): [specs/00-overview.md](specs/00-overview.md).

| Spec khác | Nội dung |
| --- | --- |
| [specs/05-open-questions.md](specs/05-open-questions.md) | Các điểm đã chốt (Q1–Q8), giải thích chi tiết |
| [specs/06-console-ui.md](specs/06-console-ui.md) | Tầng UI: nhập bằng `ReadLine`, xuất bằng `WriteLine`; menu, cài đặt, model, kiểm tra môi trường, theo dõi khi chạy, chi tiết task, báo cáo, cần chạy lại, sàng lọc |
| [specs/07-architecture.md](specs/07-architecture.md) | Kiến trúc 3 tầng UI / Service / Data Access qua interface: project, interface từng tầng, luồng gọi, song song, chạy tiếp, thứ tự viết code |

Thiết kế Adaptive Repair gốc: [../Adaptive repair.md](../Adaptive%20repair.md). Quy tắc chung của dự án: [../CLAUDE.md](../CLAUDE.md).

## Điểm chờ xác nhận

Giải thích chi tiết từng điểm: [specs/05-open-questions.md](specs/05-open-questions.md).

| # | Điểm | Trạng thái | Spec liên quan |
| --- | --- | --- | --- |
| Q1 | Nhãn model trong ảnh: "Selected LLM (commercial API / open-weight model)" | Chốt | 00, 02 |
| Q2 | Fixed Repair trong khối 3, EvoSuite trong khối 4 | Chốt | 03, 04 |
| Q3 | Bộ đại diện ARROW ở RQ3 (một bộ mỗi task mỗi model); báo riêng từng model, từng dataset | Chốt | 04 |
| Q4 | M đầy đủ cho mọi prompt sửa | Chốt | 02, 03 |
| Q5 | Mức 3 không tiến triển thì về mức 2; prompt mức 3 có danh sách đã thử | Chốt | 03 |
| Q6 | Quy tắc chọn N của CLAUDE.md, chạy pilot sau khi code | Chốt | 04 |
| Q7 | Mức 2 của UNKNOWN: khối lỗi build đầy đủ + cấu hình build | Chốt | 03 |
| Q8 | Timeout: thử 3 lần, vẫn lỗi thì ghi cần chạy lại riêng task | Chốt | 01, 03, 04 |
