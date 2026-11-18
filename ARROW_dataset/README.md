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

**4. Ra danh sách cuối cùng bằng `finalize.py`, rồi commit** trước khi gọi mô hình lần đầu:

```bash
python3 finalize.py
```

- Lớp PASS thì giữ; lớp bị loại hoặc còn ERROR sau các lượt chạy lại thì bỏ.
- Classes2Test (tập đánh giá và pilot): lớp bị bỏ được thay bằng lớp PASS kế tiếp trong tập dự phòng, theo đúng thứ tự file.
- CAT-LM và GitBug-Java: không thay (baseline chạy cả module nên lớp cùng repo hỏng y như nhau).

Kết quả ở `final/`: `classes2test_eval` 200, `classes2test_pilot` 50, `catlm_eval` 36, `gitbug_java_eval` 32 (mỗi cái một `.jsonl` làm manifest và một `.csv`), `excluded.csv` (lớp bị loại và lý do), `SHA256SUMS.txt`.

## Môi trường

Khi screen (17–18/11/2026), Maven Central và JCenter trả lỗi `429 Too Many Requests`, nên Maven và Gradle được cấu hình tải Maven Central qua bản sao của Google (nội dung giống hệt). Hai file cấu hình nằm ở `env/`; **mọi máy chạy screen hoặc thực nghiệm phải dùng chúng**:

```bash
cp env/settings.xml ~/.m2/settings.xml
mkdir -p ~/.gradle/init.d && cp env/google-mirror.gradle ~/.gradle/init.d/
```

## Lưu ý

Nếu chạy lại preflight thì danh sách lớp đầu vào thay đổi, nên phải làm lại từ bước 2.
