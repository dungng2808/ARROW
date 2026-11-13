# Kiểm tra bộ bàn giao team-r1 — 2026-11-14

- Normalize lại toàn bộ Classes2Test hiện có: **362414 input → 85819 CUT,
  0 rejection**. Pool hash khớp plan; tạo được shard local 12873 CUT cho `may-toi`.
- Normalize lại GitBug replay hiện có: **202 input → 202 CUT, 0 rejection**;
  tạo được shard local 28 CUT cho `may-toi`.
- CAT-LM: đối chiếu đủ 119 normalized task của index đã có và prefix archive;
  tạo shard 47 CUT cho `dung`. Hash prefix Java của archive Java-only hiện tại
  khớp bản dùng để khóa plan, không phụ thuộc đường dẫn archive của máy nhận.
- So 100 assignment record với shard .NET/Python cũ: khớp cả evidence SHA-256.
- 6 test công cụ đều đạt: chia đủ/không trùng/không cắt repo, deterministic;
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
