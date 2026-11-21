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

## Thử prompt SỬA test (21/11/2026)

So bộ prompt sửa nhúng trong tool (`v2-draft`) với bộ mới của nhóm (`team-v1`, bản gốc tiếng Việt ở `../prompts/vi/repair/`). Dùng 22 c0 chưa VALID của lượt 3 (8 lớp ngoài dataset-v1, **không** dùng lớp pilot), sửa bằng F+ và A, mỗi chain tối đa 5 vòng, với cả GPT-6 Luna và Qwen3-Coder. Script: `repair_trial.py`. Bảng đầy đủ: `repair_bang_tong_hop.md` (lượt 1–2) và mục dưới.

| Lượt | Bản | Thay đổi | GPT VALID/44 | Qwen VALID/44 |
|---|---|---|---|---|
| 1 | cũ `v2-draft` | — | 31 | 22 (chạy lại ở lượt 2: 23) |
| 1 | mới v1 | prompt hệ thống viết lại theo team-v1; hướng dẫn theo loại lỗi chuyển vào `failure_summaries` | 33 | 19 |
| 2 | mới v2 | danh sách lần sửa thất bại dời xuống cuối; dặn "phải khác mã cũ" | 37 | 18 |
| 3 | **mới v3 (chốt)** | sửa câu mô tả RUNTIME (sai kiểu ngoại lệ) và ASSERTION (giá trị "but was", thông báo ngoại lệ của method khác) | 33 | 21 |

- Mức nhiễu khi chạy lại cùng bản cũ với Qwen: 3 và 2 cặp lệch. Mọi bản có tổng 52–55/88, nên tỷ lệ sửa thành công không khác rõ giữa các bản.
- Chốt v3 vì không làm mô hình nào kém đi so với bản cũ (GPT 6–4, Qwen 4–5 cặp lệch), số vòng trả mã trùng của Qwen về mức bản cũ, và sửa được các lỗi thiết kế của bản cũ (quy tắc mâu thuẫn, F− bị nhắc tới cấu hình nó không thấy, thiếu quy tắc Java 8 / JUnit 4, AssertionError từ `assert` của lớp cần test, sai kiểu ngoại lệ).
- Thử **3 lượt**, nhiều hơn mức 2 lượt dự kiến: lượt 3 chỉ sửa 2 câu mô tả lỗi sau khi phân tích lớp CryptoUtil (Qwen kẹt vì câu mô tả lỗi hướng sai), và các câu sửa là quy tắc chung đã có trong team-v1.
- Cả 3 bản thử đều ghi phiên bản `team-v1` trong file; phân biệt bằng sha256. Bản lượt 1 và lượt 2 lưu ở `repair_luot1/prompt_moi/`, `repair_luot2/prompt_moi2/`.

Bộ prompt sửa đã chốt (`../prompts/override/repair/`, sinh bằng `../prompts/build_override.py`):

| File | sha256 |
|---|---|
| `repair/system.md` | `cf5e5868c02d5197a4d161408e24e94b558b4aeb801518188b83ed9f688b46d3` |
| `repair/failure_summaries.md` | `fe188b8ae5c3341848deccd48848cc374ac112ebc1a639e647f6f23e30794689` |
| `repair/user/level1.md` | `f085fe3b62aaecb54e062f4ee33310567a77544ca06342d931091583ec4ebeef` |
| `repair/user/level2.md` | `8150e6a250a7aef8f6c3d8d1ff25faf21a05063bc513bbb18086c26b365cb67b` |
| `repair/user/level3.md` | `b45dc39ad0074d84b45397921045f116bfcb69e093cbb16acedefd6cd1b308a5` |
