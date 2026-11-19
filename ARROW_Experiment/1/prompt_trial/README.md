# prompt_trial — thử để tinh chỉnh prompt (19/11/2026)

Thư mục này dùng để **tinh chỉnh bộ prompt sinh test** trước khi chạy thực nghiệm. **Không phải kết quả thực nghiệm**, không đưa số liệu ở đây vào báo cáo.

- Thử trên **10 lớp ngoài `dataset-v1`** (đã qua sàng lọc, repo không trùng tập đánh giá), 3 cách viết prompt, mô hình GPT-6 Luna, không sửa test.
- Sửa prompt **tối đa 2 lượt rồi chốt** bản `team-v1` (trong `../prompts/override/`), **trước** khi chạy khói, pilot và TN1. Sau đó không sửa nữa.
- Chạy lại cả bản trước và bản đã chốt (lượt 1b, 3b) để kiểm: chênh lệch giữa hai bản nằm trong mức dao động khi chạy lại cùng một prompt.

Chi tiết và lý do chốt: [`ket_qua_thu_19-11.md`](ket_qua_thu_19-11.md).

| Nội dung | File |
|---|---|
| Tóm tắt các lượt, lỗi tìm thấy, kết luận | `ket_qua_thu_19-11.md` |
| Bảng (tạo bằng `tn1_collect.py` + `tn1_analyze.py`) | `bang_luot1.md`, `bang_luot1b.md`, `bang_luot2.md`, `bang_luot3.md`, `bang_luot3b.md` |
| Kết quả từng lần chạy, test sinh ra (`c0/`), log | `results/` (lượt 1), `results_luot2/`, `results_luot3/`, `results_luot1b/`, `results_luot3b/` |
| Script chạy thử | `run_trial.py` (`python3 run_trial.py <thư mục kết quả> [thư mục prompt]`) |
| Chuyển kết quả thử sang dạng của `tn1_collect.py` | `convert_to_tn1.py` |
| Dựng lại bộ prompt lượt 1 (sha256 khớp từng file) | `prompts_luot1/build_override_luot1.py` |

| Lượt | Bộ prompt | Thay đổi so với lượt trước |
|---|---|---|
| 1, 1b | trước khi chốt | — |
| 2 | trung gian | câu về null; ngoại lệ của lớp khác |
| 3, 3b | **`team-v1` (đã chốt)** | viết lại 2 câu trên cho cụ thể hơn |
