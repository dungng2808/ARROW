# Kiểm tra bộ bàn giao team-r1 — 2026-11-14

- Normalize lại toàn bộ Classes2Test hiện có: **362414 input → 85819 CUT,
  0 rejection**. Pool hash khớp plan; đã kiểm shard 12873 CUT ở phân công đầu
  cho `quang`, nay chuyển nguyên tập task đó sang `chinh` ở revision 2.
- Normalize lại GitBug replay hiện có: **202 input → 202 CUT, 0 rejection**;
  đã kiểm shard local 28 CUT tương ứng phần hiện chuyển sang `chinh`.
- CAT-LM: đối chiếu đủ 119 normalized task của index đã có và prefix archive;
  tạo shard 47 CUT cho `dung`. Hash prefix Java của archive Java-only hiện tại
  khớp bản dùng để khóa plan, không phụ thuộc đường dẫn archive của máy nhận.
- So 100 assignment record với shard .NET/Python cũ: khớp cả evidence SHA-256.
- 9 test công cụ đều đạt: chia đủ/không trùng/không cắt repo, deterministic;
  CAT-LM đổi path local vẫn khớp nhưng đổi HEAD/archive bị phát hiện; C2T đổi
  evidence/GitBug đổi commit bị phát hiện; task trùng bị chặn; export/aggregate
  kiểm coverage và phát hiện file kết quả bị sửa.
- Bước publish đã kiểm tra trường hợp đang stage file khác: chặn trước add,
  commit và push, giữ nguyên staging của người dùng.
- Plan hash dùng JSON canonical, không lệch do CRLF/LF; Git attributes giữ nguyên
  byte output đã checksum khi chuyển giữa Windows và Mac.
- Đã parse cú pháp PowerShell của lệnh thành viên Windows và `bash -n` phần lệnh
  riêng của Dũng. Các file Python parse được, Markdown/Vietnamese UTF-8 hợp lệ.
- Đây là kiểm tra **chuẩn bị/chia việc/tổng hợp**, chưa chạy full preflight trên
  86140 CUT và chưa có output thực nghiệm mới để công bố.
- Không có Mac trong phiên kiểm tra: build dylib ARM64, smoke parser + SQLite và
  throughput/worker ceiling trên máy Dũng vẫn là gate bắt buộc trước run thật.
- Workers khởi đầu/trần hiệu chỉnh là ước lượng vận hành theo CPU/RAM; chưa có
  benchmark xác nhận tối đa ổn định trên từng máy. Calibration được ghi trong
  hướng dẫn, có thể đổi workers qua resume.
- Pilot cũ và source dataset được giữ nguyên; các index/shard tạm để kiểm plan
  không phải run thật. Plan đã xuất độc lập, không cần giữ pilot để dùng plan.

## Điều chỉnh phân công và vị trí tài liệu

- Bộ bàn giao đã chuyển về `Shards/`, gồm 5 file agent ở ngoài cùng và hai thư
  mục `_shared/`, `_tools/`; hướng dẫn/script dùng đường dẫn mới.
- Revision 2 hoán đổi đúng hai danh sách task `quang` ↔ `chinh` trong cả ba
  dataset: Quang 8582/18/12, Chính 12873/28/18. Không trùng/thiếu task, pool SHA
  không đổi, ba thành viên còn lại giữ nguyên danh sách.
- Shard tồn tại được so với phân công hiện tại mỗi lần gọi lệnh; checkpoint cũ
  khác danh sách task bị chặn trước resume. Không tự ghi đè hoặc xóa run cũ.

## Danh sách định danh đọc được cho từng người

- Đã xuất 15 CSV (5 người × 3 dataset), mỗi dòng có task_id, repository, class/test
  path, commit pinned nếu có và vị trí trong pool. Mọi CSV được đối chiếu đúng
  tập task_id của người đó trong plan revision 2, không tự chia lại mẫu.
- Kiểm đủ 86140 task trên ba dataset, không trùng/thiếu giữa 5 người; mỗi repo
  trong cùng dataset chỉ thuộc một người. `assignment-files.json` ghi checksum.
- Ngay đầu mỗi file MD có liên kết ba CSV và ba ví dụ định danh thực tế; agent
  không phải suy ra phân công từ số lượng hoặc nạp toàn CSV vào context.

## Định danh thành viên Quang

- File bàn giao là `03-QUANG.md`, mã thành viên `quang`. Tên hiển thị, khóa plan,
  thư mục CSV, đường dẫn run/cache/output và branch trong hướng dẫn cùng dùng
  định danh này. Các task_id, pool SHA và toàn bộ byte CSV giữ nguyên.
