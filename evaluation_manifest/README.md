# Evaluation Manifest: Classes2Test (Frozen Cohort 200 Classes)

Thư mục này chứa tập dữ liệu thực nghiệm chuẩn (frozen evaluation cohort) gồm **200 focal classes từ 200 repositories khác nhau**, được chọn ngẫu nhiên có kiểm soát từ dataset Classes2Test sau khi vượt qua toàn bộ quy trình preflight.

---

## 1. Tiêu chí và quy tắc chọn mẫu (Sampling Rules)

Tập đánh giá được chọn theo đặc tả tại `preflight.md` và mã nguồn `preflight_tool/scripts/sample_manifest.py` theo các tiêu chuẩn nghiêm ngặt:

1. **Pool đầu vào đã qua kiểm chứng kỹ thuật (`technical_eligible`):**
   - Chỉ lấy các class từ kết quả preflight của cả 5 shards (`preflight_manifest/*/technical_eligible_manifest.jsonl`).
   - Tổng pool: **6.342 classes** (thuộc **1.324 repositories**) đạt trạng thái `preflight_status == ELIGIBLE`.
   - Tất cả đều có `revision_verification_status == CONTENT_MATCHED`: preflight tìm được trong lịch sử repository một commit mà phần thân focal method và test case khớp với bản ghi Classes2Test sau khi bỏ chú thích và chuẩn hóa khoảng trắng. Commit này tái lập được nhưng không bảo đảm là commit tác giả dataset đã dùng; các file khác như cấu hình build và dependencies có thể khác.

2. **Đảm bảo tính đa dạng và độc lập (Repository Diversity):**
   - Mỗi repository chỉ được chọn tối đa **đúng 1 focal class** (`unique_repositories == 200`).
   - Loại trừ hoàn toàn nguy cơ thiên lệch do các repository lớn có nhiều class pass preflight.

3. **Tính tất định và khả năng tái lập (Strict Reproducibility):**
   - Danh sách repositories và task ID đều được sắp xếp theo thứ tự từ điển (`sorted()`) trước khi đưa vào bộ sinh số ngẫu nhiên.
   - Sử dụng bộ sinh số ngẫu nhiên cố định với `seed = 42` (`python random.Random(42)`). Kết quả không phụ thuộc vào thứ tự file trên đĩa hay hệ điều hành thực thi.

4. **Tuyệt đối không rò rỉ độ đo mục tiêu (No Target-Metric Leakage):**
   - Quá trình chọn mẫu hoàn toàn mù đối với các chỉ số chất lượng đầu ra (coverage, mutation score, test smells). Không sử dụng bất kỳ cột kết quả thử nghiệm nào để lọc class.

---

## 2. Thống kê đặc trưng tập 200 Classes (Seed 42)

| Đặc trưng | Phân bổ | Số lượng | Tỷ lệ (%) |
|---|---|---|---|
| **Tổng số class** | - | **200** | 100.0% |
| **Số repository duy nhất** | - | **200** | 100.0% |
| **Phân bổ theo Shard** | Shard 01 (Dũng) | 43 | 21.5% |
| | Shard 02 (Kiều Anh) | 33 | 16.5% |
| | Shard 03 (Quang) | 42 | 21.0% |
| | Shard 04 (Chính) | 38 | 19.0% |
| | Shard 05 (Hán) | 44 | 22.0% |
| **Build Tool** | Maven | 183 | 91.5% |
| | Gradle | 17 | 8.5% |
| **Test Framework** | JUnit 4 | 166 | 83.0% |
| | JUnit 5 | 26 | 13.0% |
| | TestNG | 8 | 4.0% |
| **Java Version** | Java 17 | 161 | 80.5% |
| | Java 8 | 38 | 19.0% |
| | Java 11 | 1 | 0.5% |
| **API Count (Methods + Constructors)** | Trung bình: **9.7** | Min: **2** | Max: **91** |

---

## 3. Cấu trúc các file

- [`classes2test_200_seed42.jsonl`](./classes2test_200_seed42.jsonl): Danh sách 200 bản ghi chi tiết dạng JSON Lines, giữ nguyên vẹn toàn bộ metadata (AST, build commands, probed API, module path, commit SHA, source paths).
- [`classes2test_200_seed42.meta.json`](./classes2test_200_seed42.meta.json): File chứng minh nguồn gốc (provenance), ghi nhận SHA-256 của từng file pool đầu vào, checksum của file output, seed, và danh sách 200 task ID được chọn.
- [`classes2test_200_seed42.csv`](./classes2test_200_seed42.csv): Bản phẳng (flattened CSV) gồm các trường quan trọng, thuận tiện tra cứu nhanh trên GitHub hoặc nhập vào công cụ thống kê.

---

## 4. Lệnh tái lập (Reproduce)

Từ thư mục `preflight_tool`, chạy lệnh sau để tái tạo chính xác file manifest:

```bash
python scripts/sample_manifest.py \
    --pool-glob "../preflight_manifest/*/technical_eligible_manifest.jsonl" \
    --size 200 \
    --seed 42 \
    --output-dir ../evaluation_manifest
```

Output SHA-256 kỳ vọng:
`7e88b9265d37c2ca31263fcdf689c47900c50b6e5e3692cbfddcffeac93263fe`
