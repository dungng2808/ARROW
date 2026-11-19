# Thử prompt team-v1 với GPT-6 Luna (19/11/2026)

Chỉ để tìm lỗi của prompt. **Không phải kết quả thực nghiệm**, không đưa số vào báo cáo.

- 10 lớp đã qua sàng lọc nhưng **không thuộc `dataset-v1`** (repo cũng không trùng), × 3 cách viết prompt = 30 lần. Không sửa (`--no-repair`).
- Script: `run_trial.py`. Kết quả: `results/trial_runs.jsonl`, test sinh ra ở `results/c0/`.

## Kết quả

| Cách | VALID | Biên dịch được | Có test chạy |
|---|---|---|---|
| Zero-shot | 3 | 8 | 7 |
| Few-shot | 4 | 7 | 7 |
| Project-aware | 5 | 9 | 9 |

## Định dạng

Cả 30 file đều đúng định dạng: có dòng package, đúng một lớp public, không có ```, kết thúc bằng `}`. Không mock, không sleep, không giờ hệ thống, không số ngẫu nhiên. Test có dùng file thì dùng file tạm và xoá (quy tắc mới được làm theo).

Vi phạm: 1 file dùng reflection (DataExpressionSource, Few-shot).

## Lỗi lặp lại do prompt (có thể sửa)

1. **Truyền null cho tham số bắt buộc** (DataExpressionSource, cả 3 cách): mã nguồn gọi `checkNotNull` nhưng test vẫn truyền null để tạo đối tượng. Câu "truyền null nếu mã nguồn có xử lý null" bị hiểu sai.
2. **Đoán kiểu và thông báo của ngoại lệ do lớp khác ném** (CryptoUtil, cả 3 cách): đoán `NullPointerException` nhưng `Preconditions.checkNotNull` của dự án ném `IllegalArgumentException`; đoán sai nội dung thông báo.
3. **Tự cài đặt interface của dự án mà không thấy mã nguồn** (CompositeParser, Few-shot và Project-aware): viết lớp ẩn danh `new Parser() {…}` sai method → không biên dịch.

## Lỗi không sửa bằng prompt

- Chọn JUnit 4 trong dự án JUnit 5 (Zero-shot, Few-shot: WithJsonPath, PathUtils, CompositeParser). Đây chính là điều RQ1a đo, không được sửa.
- Tính sai giá trị mong đợi (ANNIndex, DeweyVersion), đoán sai chuỗi `toString` (WithJsonPath). Lỗi suy luận của mô hình.

## Lượt 2 (thêm câu về null và ngoại lệ của lớp khác)

Cùng 10 lớp. Kết quả: `results_luot2/`; bảng của `tn1_collect.py` + `tn1_analyze.py`: `bang_luot1.md`, `bang_luot2.md`.

| Cách | VALID lượt 1 → 2 | Biên dịch được | Có test chạy |
|---|---|---|---|
| Zero-shot | 3 → 2 | 8 → 8 | 7 → 7 |
| Few-shot | 4 → 3 | 7 → 6 | 7 → 6 |
| Project-aware | 5 → 4 | 9 → 9 | 9 → 9 |

- 10/30 lần đổi kết quả, kể cả ở lớp không liên quan tới 2 câu mới (ViterbiAlgorithm, NumberFormatter, DeweyVersion). Mô hình trả lời khác giữa hai lần chạy, nên so tổng số giữa hai lượt trên 10 lớp không nói được prompt nào tốt hơn; chỉ xem lỗi đích có hết không.
- **Câu null: chưa có tác dụng.** DataExpressionSource Zero-shot và Project-aware vẫn truyền null vào tham số mà mã nguồn gọi `checkNotNull` (Few-shot hết lỗi, thành VALID).
- **Câu ngoại lệ: chưa có tác dụng.** CryptoUtil Zero-shot và Project-aware vẫn đoán `NullPointerException`, dù method khai báo `throws IllegalArgumentException`. Mô hình không coi `checkNotNull` (import tĩnh) là "lớp khác".

## Lượt 3 (sửa câu chữ lần cuối, câu 16–17 trong `prompts/vi/README.md`)

Kết quả: `results_luot3/`, bảng: `bang_luot3.md`.

| Cách | VALID lượt 1 / 2 / 3 | Biên dịch được | Có test chạy |
|---|---|---|---|
| Zero-shot | 3 / 2 / 2 | 8 / 8 / 6 | 7 / 7 / 6 |
| Few-shot | 4 / 3 / 3 | 7 / 6 / 6 | 7 / 6 / 6 |
| Project-aware | 5 / 4 / 3 | 9 / 9 / 9 | 9 / 9 / 9 |

- **Null (DataExpressionSource):** Project-aware và Few-shot không truyền null thẳng nữa nhưng chuyển sang dùng reflection để tạo tham số (vẫn hỏng); Zero-shot vẫn truyền null. Lớp này cần một đối tượng `ValueExpression` mà mô hình không biết cách tạo, nên khó với mọi cách viết.
- **Ngoại lệ (CryptoUtil):** Few-shot hết đoán `NullPointerException` nhưng vẫn kiểm nội dung thông báo; Zero-shot vẫn đoán `NullPointerException`; Project-aware lỗi biên dịch khác (ép int sang byte).
- Kết quả dao động mạnh giữa các lượt ở cả những lớp không liên quan (ViterbiAlgorithm, DeweyVersion, ANNIndex, CompositeParser), nên không so tổng giữa các lượt.
- Định dạng câu trả lời vẫn đúng ở cả 90 lần chạy của 3 lượt.

## Chốt (19/11/2026)

Theo kế hoạch: sửa tối đa 2 lượt rồi chốt dù kết quả thế nào, để không chỉnh prompt cho khớp 10 lớp thử. Bộ prompt dùng cho chạy khói, pilot, TN1, TN2, TN3 là bản sau lượt 3 (`team-v1`). Lỗi còn lại để vòng sửa (TN2, Adaptive Repair ở TN3) xử lý.

## Kiểm dao động: chạy lại y nguyên (lượt 1b, 3b)

- Lượt 1b: chạy lại bản prompt lượt 1 (dựng lại bằng `prompts_luot1/build_override_luot1.py`; sha256 khớp từng file với `run.json` của lượt 1).
- Lượt 3b: chạy lại bản đã chốt. Bảng: `bang_luot1b.md`, `bang_luot3b.md`.

| Bản prompt | Lần chạy | VALID | ESR | CSR |
|---|---|---|---|---|
| Lượt 1 | 1 | 12 | 23 | 24 |
| Lượt 1 | 1b | 9 | 22 | 22 |
| Đã chốt | 3 | 8 | 21 | 21 |
| Đã chốt | 3b | 8 | 21 | 21 |

- **Cùng một prompt, chạy lại vẫn đổi nhiều:** bản lượt 1 đổi 7/30 kết quả (VALID 12 → 9); bản đã chốt đổi 11/30 (mất 4 VALID, được 4, tổng vẫn 8).
- Mức đổi giữa hai bản prompt (9–11/30) ngang mức đổi khi chạy lại cùng một prompt (7–11/30).
- Gộp 2 lần chạy mỗi bản: 6 ô bản lượt 1 tốt hơn, 2 ô bản đã chốt tốt hơn (kiểm định dấu p = 0,29), phần lớn do một lớp (ViterbiAlgorithm). Số test trung bình mỗi bộ không khác (8,5–9,6).
- **Kết luận:** chênh lệch 12 → 8 nằm trong mức dao động ngẫu nhiên; không có bằng chứng bản nào tốt hơn. Giữ bản đã chốt.
