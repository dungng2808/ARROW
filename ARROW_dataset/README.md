# ARROW_dataset

Danh sách các lớp Java (CUT) dùng để chạy thực nghiệm ARROW.

Hiện thư mục mới có hai script chọn, **chưa có dataset**.

## Cần chọn bao nhiêu lớp

| Dataset | Số lớp |
|---|---|
| Classes2Test | 200 lớp (+ 100 lớp dự phòng) |
| Classes2Test cho pilot | 50 lớp (+ 25 lớp dự phòng) |
| CAT-LM | tối đa 59 lớp |
| GitBug-Java | tối đa 33 lớp |

## Chọn thế nào

- Mỗi repo chỉ lấy **1 lớp**, và lớp đó phải có **1 lớp test** do người viết.
- Các dataset không dùng chung repo nào.
- Thứ tự chọn tính bằng `sha256` từ một chuỗi cố định (`SALT` trong script) và mã của lớp. Không dùng seed. Chạy lại bao nhiêu lần cũng ra đúng kết quả cũ.

## Các bước

**1. Sửa script nếu cần, rồi commit.** Thường chỉ cần đổi `SALT`. Commit trước khi chạy để chứng minh là đã chốt cách chọn từ trước.

**2. Chạy script chọn** (một lệnh cho cả ba dataset):

```bash
cd ARROW/ARROW_dataset
python3 select_datasets.py ../Prelight_Net/results/team-r1 candidates
```

Kết quả ở `candidates/`: `classes2test_eval`, `classes2test_eval_reserve`, `classes2test_pilot`, `classes2test_pilot_reserve`, `catlm_eval`, `gitbug_java_eval` (mỗi cái một `.jsonl` và một `.csv`), kèm `SHA256SUMS.txt`.

**3. Kiểm từng lớp bằng `screen.py`.** Script gọi `arrow inspect` cho từng lớp; lớp nào có test của người viết không chạy đạt cả 2 lần thì loại.

```bash
python3 screen.py candidates/*.jsonl --out screen --workers 6
```

Mỗi lớp ra `PASS`, `EXCLUDED` (loại) hoặc `ERROR` (lỗi phía mình, chạy lại bằng `--retry-errors`). Kết quả ở `screen/screen_results.jsonl`. Bị ngắt thì chạy lại đúng lệnh cũ.
- Classes2Test: lấy lớp dự phòng thay vào.
- CAT-LM và GitBug-Java: không có repo dự phòng, nên mất lớp đó.

**4. Lưu danh sách cuối cùng và commit** trước khi gọi mô hình lần đầu:

```bash
shasum -a 256 <các file danh sách cuối cùng> > SHA256SUMS.txt
git add ARROW_dataset && git commit
```

## Lưu ý

Nếu chạy lại preflight thì danh sách lớp đầu vào thay đổi, nên phải làm lại từ bước 2.
