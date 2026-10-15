# ARROW Framework

**Trạng thái:** spec đã chốt (14/10/2026). Code: **xong C1–C9** theo spec 07 Mục 11:
C1 (khung 3 tầng, Data Access, console), C2 (java-helper), C3 (khối 1, lệnh `screen`), C4 (khối 3 phần xác minh),
C5 (khối 2: dựng prompt 3 quy trình và prompt sửa 3 mức, tách và chuẩn hóa mã, lớp gọi model Replaying → Gateway → LiteLLM, model giả),
C6 (khối 3 phần sửa: Fixed và Adaptive Repair, checkpoint, rollback, strategy switching, thông tin bổ sung theo trạng thái, `RepairRunner`),
C7 (khối 4: chuẩn bị bộ test, JaCoCo, PIT, tsDetect, EvoSuite, báo cáo RQ1–RQ3, chọn N, độ nhạy P, cần chạy lại),
C8 (`run` toàn luồng: lập lịch, hàng đợi build/model, tạm dừng/dừng/chạy tiếp theo ledger, heartbeat, điều khiển từ terminal khác),
C9 (màn hình S1–S10 và mọi lệnh). **C10** (smoke với model thật) còn chờ khóa API và LiteLLM của máy chạy; smoke với model giả trên 2 CUT thật đã chạy.

## Build và chạy

Hướng dẫn từng bước cho thành viên chạy pilot (cài đặt, chạy theo shard, gửi kết quả): [HUONG_DAN_CHAY.md](HUONG_DAN_CHAY.md).

Cần .NET SDK 8 trở lên; java-helper cần JDK 17 (`machine.local.json` → `jdk_homes`, hoặc `ARROW/Java-version/runtime/jdk-17`) và Maven. Khối 1 cần git, Maven (Gradle nếu repository không có `gradlew`) và các JDK 8/11/17/21 (tìm theo cùng thứ tự); repository được tải vào `work_dir` (mặc định `../repo`). Integration test của khối 1 chạy Maven/Gradle thật trên `tests/fixtures/`. Từ thư mục `ARROW_Framework`:

| Việc | Lệnh |
| --- | --- |
| Build | `dotnet build ArrowFramework.sln` |
| Test | `dotnet test ArrowFramework.sln` |
| Kiểm tra bộ prompt | `dotnet run --project src/Arrow.UI -- prompts check` |
| Trợ giúp | `dotnet run --project src/Arrow.UI -- --help` |
| Sàng lọc CUT (chỉ khối 1, tiêu chí B) | `dotnet run --project src/Arrow.UI -- screen --manifest ../evaluation_manifest/pilot/classes2test_pilot_50_seed42_v2.jsonl --run-id screen-pilot-v2` |
| Menu chính | `dotnet run --project src/Arrow.UI` |
| Kiểm tra môi trường / tải công cụ khối 4 | `dotnet run --project src/Arrow.UI -- doctor` / `-- tools install` |
| Chạy 4 khối (model thật qua LiteLLM) | `dotnet run --project src/Arrow.UI -- run --experiment experiments/pilot-v1.json --run-id pilot-1` |
| Chạy thử không tốn tiền (model giả) | `dotnet run --project src/Arrow.UI -- run --run-id smoke --tasks <id1>,<id2> --fake-llm` |
| Báo cáo, trạng thái, chi tiết task | `-- report --run-id R` · `-- status --run-id R` · `-- show --run-id R --task T` · `-- rerun-list --run-id R` |
| Điều khiển run đang chạy | `-- control --run-id R pause\|resume\|stop` |
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
