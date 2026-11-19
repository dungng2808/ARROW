# Thiết kế Thực nghiệm 1 (RQ1a)

Cập nhật 18/11/2026. Chạy trên tool `arrow` (ARROW_Developer_CLI) bản mới nhất của Chính.

## 1. TN1 làm gì

So **3 cách viết prompt** khi cho mô hình sinh test, xem cách nào cho nhiều test chạy được nhất. **Không sửa test**: sinh một lần, kiểm một lần.

| Cách viết prompt | Mô hình được đưa gì |
|---|---|
| `zero_shot` | Chỉ mã nguồn lớp cần test |
| `few_shot` | Mã nguồn + 2 ví dụ test mẫu (JUnit 4, JUnit 5) |
| `zero_shot_project_aware` | Mã nguồn + thông tin dự án (Java, framework test, thư viện, đường dẫn…) |

Mỗi cách chạy với **2 mô hình**: GPT-6 Luna và Qwen3-Coder-30B.

## 2. Đầu vào

| Đầu vào | Lấy từ đâu | Ghi chú |
|---|---|---|
| **Danh sách lớp** | `ARROW/ARROW_dataset/final/`: `classes2test_eval.jsonl` (200), `catlm_eval.jsonl` (36), `gitbug_java_eval.jsonl` (32) | Tag `dataset-v1`; kiểm `SHA256SUMS.txt` trước khi chạy |
| **Repo của từng lớp** | Trường `repo_url` và `checkout_sha` trong manifest | GitBug-Java dùng commit **sau khi sửa lỗi** |
| **3 prompt sinh test** | **Bộ prompt của nhóm `team-v1`**: `prompts/override/generation/` (tiếng Anh, gửi mô hình); bản tiếng Việt ở `prompts/vi/`. Nạp vào tool bằng `prompts.override_dir` | Phần chung giống hệt giữa 3 cách; tool ghi sha256 vào `run.json`, `tn1_run.py` ghi vào file lock |
| **2 ví dụ Few-shot** | Chép nguyên văn từ prompt `few_shot` của tool (JUnit 4: `DefaultJwtService`; JUnit 5: `ReadOptions`) | Repo của 2 ví dụ đã bị loại khỏi dataset |
| **2 mô hình** | GPT-6 Luna (TeamoRouter); Qwen3-Coder-30B (Ollama trên máy Mac) | temperature 0, max_tokens 16.384, seed cố định, tắt suy luận; Qwen num_ctx 40.960 |
| **Tool** | `arrow` bản đã chốt (một commit cho mọi máy) | Ghi commit vào file lock |
| **Môi trường** | JDK 8/11/17/21, Maven, Gradle; 2 file bản sao Google ở `ARROW_dataset/env/` | Giống nhau trên mọi máy |

**Khối lượng:** 268 lớp × 3 cách viết prompt × 2 mô hình = **1.608 bộ test** (Classes2Test 1.200, CAT-LM 216, GitBug-Java 192). Mỗi cấu hình chạy 1 lần.

## 3. Tool đã có gì

| Cần | Trạng thái | Cách dùng |
|---|---|---|
| Chọn cách viết prompt | ✅ | `arrow config set prompts.strategy <tên> --home <thư mục>` |
| Tắt sửa, tắt đo chất lượng | ✅ | `arrow gen ... --no-repair --no-metrics` |
| Kiểm 5 bước | ✅ | Có sẵn |
| Dùng chung baseline cho 6 lần chạy của một lớp | ✅ | Có sẵn: tool tự dùng lại baseline đã đo trong cùng ARROW_HOME |
| Kiểm tra lại trước khi ghi REGRESSION | ✅ | Đúng quy tắc báo cáo (mục 6), **mặc định bật** (`verification.confirm_regression = true`). Không dùng `--no-confirm-regression` |
| Mọi máy cùng một bản tool | ⚠ | Nhờ Chính phát hành một bản có tag |

## 4. Chuẩn bị máy

Mỗi máy chạy cần:
1. `arrow` đúng bản đã chốt; `arrow doctor` báo **SẴN SÀNG**.
2. Đủ JDK 8, 11, 17, 21; Maven, Gradle, git.
3. Chép 2 file bản sao Google trong `ARROW/ARROW_dataset/env/`.
4. Cấu hình mô hình:
   - **GPT-6 Luna** (qua TeamoRouter): temperature 0, max_tokens 16.384, seed cố định, tắt suy luận.
   - **Qwen3-Coder-30B** (qua Ollama, **chỉ máy Mac**): num_ctx 40.960, temperature 0, max_tokens 16.384, seed cố định.
   - Thử bằng `arrow models test`.

## 5. Cách chạy

### Bước 1: Chạy khói
Chạy 2–5 lớp pilot (`final/classes2test_pilot.jsonl`) với đủ 6 cấu hình. Xem chạy có lỗi không, file kết quả có đúng không. **Không dùng số liệu.**

### Bước 2: Chạy TN1
Với **mỗi lớp**, chạy lần lượt 6 cấu hình trong **cùng một ARROW_HOME**:

```
cho mỗi cách viết prompt (3 cách):
    arrow config set prompts.strategy <cách> --home <home>
    cho mỗi mô hình (2 mô hình):
        arrow gen <lớp> --repo <repo_url> --ref <checkout_sha> \
            --model <mô hình> --no-repair --no-metrics \
            --yes --json --home <home>
```

Quy tắc:
- **6 cấu hình của một lớp chạy trên cùng một máy, cùng một ARROW_HOME.** Như vậy chúng dùng chung baseline, nên so sánh công bằng.
- **Không chạy 2 lớp cùng lúc trên cùng một ARROW_HOME**, vì `prompts.strategy` là cấu hình chung của home. Muốn chạy song song thì mỗi worker dùng một home riêng.
- Chia lớp cho các máy; mỗi máy ghi lại loại máy và số worker.
- Phần Qwen chỉ chạy trên máy Mac.
- Bị ngắt thì chạy tiếp, bỏ qua lớp đã xong.

### Bước 3: Chạy lại lỗi hạ tầng
Bộ test bị INFRA (mất mạng, quá giờ…) thì chạy lại **đúng cấu hình đó** sau khi cả lô xong. Chạy lại vẫn INFRA thì loại khỏi mẫu số của lớp đó và báo cáo số lượng.

## 6. Kiểm tra lại REGRESSION (quy tắc trong báo cáo)

```
Bước 5 → test cũ X fail
  └─ Chạy riêng X 3 lần (không có test mới)
       ├─ fail ít nhất 1 lần → nghi flaky, không tính
       └─ đạt cả 3 lần → chạy lại module CÓ test mới 1 lần
                           ├─ X fail → REGRESSION
                           └─ X đạt  → không tính
```

Tool đã làm đúng sơ đồ trên (Chính, 18/11, commit `6ab167a`). Thêm hai điểm:
- Test cũ biến mất khỏi báo cáo, hoặc chính test mới fail khi chạy cùng module → REGRESSION ngay, không kiểm lại.
- Giới hạn đã biết: test cũ chỉ đạt khi chạy chung module (cần test khác chạy trước) sẽ luôn fail khi chạy riêng, nên bị xếp "nghi chập chờn". Các trường hợp nghi chập chờn được ghi tên trong `verify.json` (`module_flaky`) để kiểm lại.

## 7. Đầu ra

### File dữ liệu

| File | Nội dung | Dùng cho |
|---|---|---|
| `tn1_candidates.csv` | Mỗi bộ test một dòng (cột ở bảng dưới) | Mọi bảng kết quả |
| `tn1.lock.json` | Bản tool (commit), cấu hình, tham số 2 mô hình, tag Ollama và num_ctx, sha256 manifest, máy, số worker | Chứng minh điều kiện chạy |
| `logs/load.csv` | Tải máy, ghi mỗi 60 giây | Kiểm điều kiện chạy |
| `tn2_pool.jsonl` | Bộ test **có code, không INFRA, chưa VALID**, kèm mã c0 | Rút 180 bộ cho TN2 (90 / 48 / 42) |
| `runs/` của `arrow` | Log gốc, prompt, phản hồi mô hình, `usage.json`, `verify.json` | Kiểm lại từng bộ test |

### Cột của `tn1_candidates.csv`

| Nhóm | Cột |
|---|---|
| Định danh | dataset, task_id, repo, commit, lớp, mô hình, cách viết prompt, run_id, máy |
| Dự án | build tool, JDK, framework test |
| Sinh test | có ra code không (INVALID_OUTPUT nếu không), token prompt, token đầu ra, thời gian gọi mô hình, `model_id` trả về |
| Kiểm test | trạng thái dừng (COMPILE, UNKNOWN, DISCOVERY, RUNTIME, ASSERTION, REGRESSION, VALID hoặc INFRA), số phương thức test đã chạy, số test nghi flaky (`module_flaky`), thời gian build |
| Tính sẵn | qua bước 1…5 (0/1), biên dịch được (CSR), thực thi được (ESR), VALID |

## 8. Cách tính chỉ số

**Qua từng bước:** bộ test qua bước k nếu nó không dừng ở bước k hoặc trước đó.

| Bước | Dừng ở đây nếu |
|---|---|
| 1. Biên dịch | không ra code, COMPILE, **UNKNOWN** |
| 2. Được tìm thấy | DISCOVERY |
| 3. Chạy được | RUNTIME |
| 4. Test đạt | ASSERTION |
| 5. Không hỏng test cũ | REGRESSION |

- **CSR** = số bộ test qua bước 1 / tổng số bộ test. UNKNOWN **không** tính là biên dịch được.
- **ESR (chỉ số chính)** = số bộ test có **ít nhất 1 phương thức test đã chạy** / tổng số bộ test.
- Mẫu số gồm cả bộ test không ra code; không gồm INFRA.

## 9. Thống kê

Làm riêng cho từng dataset và từng mô hình:
- **Cochran's Q:** so 3 cách viết prompt cùng lúc.
- **McNemar:** so từng cặp (Zero-shot – Few-shot, Zero-shot – Project-aware, Few-shot – Project-aware).
- **Holm:**
  - **Họ chính:** McNemar trên ESR, 3 cặp × 2 mô hình = 6 phép.
  - **Họ phụ:** CSR và các bước còn lại.
- **Wilson 95%** cho mọi tỷ lệ; độ lớn hiệu ứng là chênh lệch tỷ lệ.
- Phép so sánh nào có dưới 10 cặp thì chỉ mô tả.

## 10. Các bảng kết quả

Mọi bảng làm **riêng cho từng dataset** (Classes2Test, CAT-LM, GitBug-Java). CAT-LM và GitBug-Java ghi chú lực thống kê thấp (CI 95% khoảng ±16–17 điểm %).

**Bảng 1. Kết quả RQ1a** (bảng chính)

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID (qua bước 5) | **ESR** |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| GPT-6 Luna | Zero-shot | | | | | | | | |
| | Few-shot | | | | | | | | |
| | Project-aware | | | | | | | | |
| Qwen3-Coder-30B | Zero-shot | | | | | | | | |
| | Few-shot | | | | | | | | |
| | Project-aware | | | | | | | | |

Mỗi ô ghi số lượng và tỷ lệ kèm Wilson 95%.

**Bảng 2. Kiểm định**

| Mô hình | Chỉ số | Cochran's Q (p) | Cặp so sánh | Chênh lệch tỷ lệ [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|
| GPT-6 Luna | ESR | | Zero-shot – Few-shot | | | | chính |
| | | | Zero-shot – Project-aware | | | | chính |
| | | | Few-shot – Project-aware | | | | chính |
| | CSR, bước 2–5 | | (như trên) | | | | phụ |
| Qwen3-Coder-30B | (như trên) | | | | | | |

**Bảng 3. Bộ test dừng ở đâu**

| Mô hình | Cách viết prompt | Không ra code | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | VALID | INFRA (loại) |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|

Kèm số trường hợp REGRESSION được đổi thành "nghi flaky".

**Bảng 4. Độ dài prompt và token**

| Mô hình | Cách viết prompt | Token prompt (trung bình / trung vị) | Token đầu ra (trung bình / trung vị) | Thời gian gọi (trung vị) |
|---|---|---|---|---|

Với Qwen: thêm số lần prompt vượt num_ctx.

**Bảng 5. Framework test của từng dataset** (giải thích hạn chế của ví dụ Few-shot)

| Dataset | JUnit 4 | JUnit 5 | TestNG |
|---|---:|---:|---:|
| Classes2Test | 163 | 29 | 8 |
| CAT-LM | 16 | 20 | 0 |
| GitBug-Java | 15 | 17 | 0 |

(Số theo preflight; kiểm lại bằng `metadata.json` sau khi chạy.)

**Bảng 6. JDK × framework × build tool** (dùng cho TN3)

| Build tool | JDK | JUnit 4 | JUnit 5 + vintage | JUnit 5 không vintage | TestNG |
|---|---|---:|---:|---:|---:|
| Maven | 8 / 11 / 17 / 21 | | | | |
| Gradle | 8 / 11 / 17 / 21 | | | | |

Lập ngay sau khi chạy TN1, từ `block1/metadata.json`; cột vintage đọc từ `pom.xml` hoặc `build.gradle`.

## 11. Script cần viết

| Script | Làm gì |
|---|---|
| `tn1_run.py` | Chạy theo mục 5; ghi file lock (bản tool, cấu hình, checksum dataset); chạy tiếp được khi bị ngắt |
| `tn1_collect.py` | Gom kết quả thành `tn1_candidates.csv` và bảng JDK × framework |
| `tn1_analyze.py` | Tính CSR, ESR, kiểm định, ra bảng RQ1a |

## 12. Còn phải chốt

| Việc | Đề xuất |
|---|---|
| Bản tool cho mọi máy | Nhờ Chính phát hành một bản có tag |
| ~~Tên tham số tắt suy luận của GPT-6 Luna~~ | ✅ 19/11: `reasoning_effort: "none"` (giá trị sai → HTTP 400, nên provider có nhận tham số). Cấu hình máy Dung: model `gpt6luna` = `gpt-6-luna` trên TeamoRouter, `temperature 0`, `max_tokens 16384`, `seed` cố định, `reasoning_effort none`; `arrow models test gpt6luna` OK |
| ~~Cấu hình Qwen~~ | ✅ 19/11: bản Ollama `qwen3-coder-arrow:30b-q8-ctx40k` tạo từ `env/Modelfile.qwen3coder` (gốc `qwen3-coder:30b-a3b-q8_0`; num_ctx 40960; temperature 0; bỏ top_p/top_k/repeat_penalty của bản gốc). Model `qwen3coder` trong `~/.arrow` (provider `ollama`, seed cố định, max_tokens 16384). 36 GB, 100% GPU. Chạy khói 2 lớp × 3 cách: OK; mỗi lần gọi 27–44 s; **2 worker** (RAM chỉ còn ~9% trống) |
| Đếm prompt Qwen bị cắt vì vượt num_ctx | Đếm lại token bằng tokenizer của Qwen (Ollama cắt mà không báo) |
| Nhánh tùy chọn Few-shot + thông tin dự án | Làm nếu còn thời gian |
