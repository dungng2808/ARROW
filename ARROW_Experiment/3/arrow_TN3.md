# TN3, nhánh ARROW: lấy bộ test ARROW cho RQ3

Nguồn thiết kế: `Sua/ban_phan_bien/cac_thuc_nghiem.md`, mục "Adaptive Repair cho TN3" và "Thực nghiệm 3".
Tổng quan TN3: [README.md](README.md).

## 1. Mục đích

Tạo **một bộ test ARROW cho mỗi lớp, mỗi mô hình** (2 × 268 = 536 bộ) để so với EvoSuite và ChatUniTest. Nhánh này
**không sinh test mới**:

- Chỉ dùng bộ **Zero-shot-project-aware** của TN1.
- Bộ đã VALID ngay ở TN1 → đo lại (`source = initial`).
- Bộ chưa VALID → sửa bằng **Adaptive Repair** ngay trong TN3 (`source = adaptive`).
- **Không lấy gì từ TN2** (chốt 17/11/2026), kể cả với candidate nằm trong tập con 180 của TN2. Nhờ vậy TN3 chỉ phụ thuộc TN1.
- Candidate Zero-shot và Few-shot của TN1 **không** dùng ở đây.

```
TN1: bộ Zero-shot-project-aware của (lớp, mô hình)
 ├─ VALID ngay              → đo lại                                   source = initial
 ├─ chưa VALID, có file c0  → Adaptive Repair, N = 10, cùng mô hình    source = adaptive
 └─ INVALID_OUTPUT (không có file test) → không sinh lại, không sửa, tính là thất bại
```

## 2. Đầu vào

| Đầu vào | Lấy từ đâu |
|---|---|
| Danh sách 268 lớp | Manifest cuối `dataset-v1` (`ARROW_dataset/final/`) |
| Bộ test c0 của TN1 | `ARROW_Experiment/1/results/<thư mục>/c0/<task_id>__<mô hình>__zero_shot_project_aware.java`; chỉ lấy dòng TN1 được tính (`counted = 1`) |
| Trạng thái của c0 ở TN1 | `tn1_runs.jsonl` (`result`), dùng cho `--c0-state` khi sửa |
| Thời gian TN1 | `tn1_runs.jsonl`, dùng cho cấu hình EvoSuite cùng ngân sách |

Thứ tự ưu tiên thư mục kết quả TN1 khi một (lớp, mô hình) có nhiều dòng (như danh sách TN2):
`dung`, `dung-qwen`, `dung-qwen-fix`, `quang-qwen`, `han-qwen`, `chinh-qwen`. Danh sách bộ ARROW phải ghi thành file
`.jsonl` kèm sha256 của từng file c0, và commit **trước** khi chạy.

## 3. Phân loại 536 bộ Zero-shot-project-aware (PA) của TN1

Chỉ gồm bộ PA: dòng `strategy = zero_shot_project_aware`, `counted = 1` trong `tn1_candidates.csv` của TN1
(khớp bảng RQ1a, dòng "Project-aware"). Không có bộ Zero-shot hay Few-shot.

| Dataset | Mô hình | Đo lại (VALID ngay) | Cần sửa | INVALID_OUTPUT |
|---|---|---:|---:|---:|
| Classes2Test | GPT-6 Luna | 104 | 96 | 0 |
| Classes2Test | Qwen3-Coder | 39 | 157 | 4 |
| CAT-LM | GPT-6 Luna | 29 | 7 | 0 |
| CAT-LM | Qwen3-Coder | 14 | 22 | 0 |
| GitBug-Java | GPT-6 Luna | 6 | 26 | 0 |
| GitBug-Java | Qwen3-Coder | 3 | 27 | 2 |
| **Tổng** | | **195** | **335** | **6** |

- 335 bộ cần sửa, theo trạng thái ở TN1: COMPILE 205, ASSERTION 65, RUNTIME 59, DISCOVERY 3, UNKNOWN 2, REGRESSION 1.
- Theo mô hình: GPT-6 Luna 139 đo lại / 129 sửa; Qwen 56 đo lại / 206 sửa / 6 INVALID_OUTPUT.
- **6 INVALID_OUTPUT** đều của Qwen. Ở TN1, câu trả lời không thành được file test Java, nên arrow không ghi `candidate.java`
  (chỉ còn `block2/response.json`):
  - 5 bộ `parse_error`: Qwen viết quá dài (khoảng 1.500–1.700 dòng, có lúc lặp vô tận), chạm `max_tokens` 16.384
    (`finish_reason: length`) → file bị cắt giữa chừng.
  - 1 bộ `multiple_classes` (`org.asn1s.api.util.NRxUtils`): thêm một lớp phụ thứ hai ở mức ngoài cùng.

  Cơ chế sửa cần một file đọc được và có đúng một lớp, nên 6 bộ này không sửa được. Ghi một dòng `INVALID_OUTPUT` và
  tính là thất bại. Không sinh lại, vì như vậy là một lần sinh mới, không còn là bộ của TN1.

## 4. Quy tắc sửa (giữ cố định)

| Mục | Giá trị |
|---|---|
| Cơ chế | **A (Adaptive)** |
| Số vòng tối đa | **N = 10** (chọn ở pilot, quy tắc B, ngưỡng 1%) |
| Mô hình sửa | **cùng mô hình đã sinh c0** (không sửa chéo như TN2) |
| Dừng sớm | **không** (`early_stop_p = null`) |
| Bộ prompt | bộ của nhóm (`ARROW_Experiment/1/prompts/override`), giống TN1 và TN2 |
| Tham số mô hình | temperature 0, max_tokens 16.384, tắt suy luận, không đặt top-p |
| Qwen | qua OpenRouter, **ghim Amazon Bedrock**, `allow_fallbacks: false` (như TN1, TN2) |
| Repo | chạy trên repo gốc, đúng `checkout_sha` của TN1 |
| Tool | **cùng một commit** trên mọi máy (bản dựng thử dùng arrow `203fb8e`, bản của pilot và TN2) |

## 5. Cách chạy bằng arrow CLI

**Cấu hình home** (mỗi worker một home riêng, chép từ `~/.arrow`):

```bash
arrow config set prompts.override_dir "<ARROW_Experiment/1/prompts/override>"
arrow config set repair.mechanism adaptive  --home <home>
arrow config set repair.early_stop_p null   --home <home>
arrow models test gpt6luna && arrow models test qwen3coder-or
```

**Bộ VALID ngay ở TN1 (đo lại):**

```bash
arrow gen <FQN> --repo <repo_url> --ref <checkout_sha> \
    --c0 <file c0 TN1> --c0-model <mô hình> --model <mô hình> --no-repair --yes --json --home <home>
```

**Bộ chưa VALID (sửa):**

```bash
arrow gen <FQN> --repo <repo_url> --ref <checkout_sha> \
    --c0 <file c0 TN1> --c0-model <mô hình> --model <mô hình> \
    --rounds 10 --c0-state <trạng thái TN1> --yes --json --home <home>
```

- `--c0`: dùng bộ test có sẵn, không sinh mới. `--c0-state`: trạng thái ở TN1 (COMPILE, ASSERTION…).
- Trước khi chạy, kiểm sha256 của file c0 khớp danh sách đã commit.
- Sau khi chạy, đọc lại `runs/<id>/run.json` để kiểm arrow đã dùng đúng: `mechanism = adaptive`, `rounds = 10`,
  `early_stop_p = null`, đúng mô hình. Sai thì ghi `BAD_CONFIG` và chạy lại.
- Hết tiền hoặc mô hình không dùng được: dừng cả lô (như TN1, TN2), ghi `STOPPED`, chạy lại sau.
- Lỗi hạ tầng: thử lại tối đa 3 lần, cách 30 giây; sau đó ghi INFRA và đưa vào danh sách chạy lại.

Việc 17 trong bảng việc của thiết kế (sửa `rq3_only`, spec R03 §6.1) là một cách làm khác. Bản dựng thử gọi
`arrow gen --c0` cho từng bộ, nên không phải sửa tool.

## 6. Xác minh và đo (arrow tự làm)

- **5 bước:** biên dịch → được phát hiện → thực thi ≥ 1 phương thức → test đích đạt → không hồi quy trong module.
- **Trước khi ghi REGRESSION:** chạy riêng mỗi test có sẵn bị fail 3 lần trên bản không có test mới. Fail ≥ 1 lần thì
  là `suspected_flaky`, không tính.
- **Khi bộ cuối VALID**, đo trên bộ đó:
  - JaCoCo 0.8.12 (dòng, nhánh, phương thức);
  - PIT 1.17.4 `DEFAULTS` (điểm đột biến, test strength);
  - tsDetect (mật độ smell).

## 7. Ghi kết quả: mỗi (lớp, mô hình) một dòng

| Nhóm | Trường |
|---|---|
| Định danh | `task_id`, `dataset`, `repo_url`, `checkout_sha`, `class_fqn`, `model`, `machine`, `tool_commit` |
| Nguồn | `action` (measure / repair / invalid_output), `source` (initial / adaptive), `tn1_result`, `tn1_total_s`, `c0_path` |
| Kết quả | `final_state` (VALID … COMPILE, hoặc INVALID_OUTPUT), `rounds_run`, `success_round`, `stop_reason` |
| Chi phí | số lượt gọi mô hình, token prompt, token đầu ra, `repair_seconds`, `repair_llm_seconds`, `repair_build_seconds` |
| Chất lượng | `line`, `branch`, `method`, `mutation`, `test_strength`, `smell_density` |
| Kiểm cấu hình | `status` (DONE / INFRA / STOPPED / BAD_CONFIG), `run_dir` |

Lưu kèm: `tn3.lock.json` (tool, mô hình, tham số, sha256 bộ prompt, sha256 danh sách), log của từng lần chạy arrow.

## 8. Chia máy

Chia 268 lớp thành **10 phần: Dung 4 phần (107 lớp), 6 phần còn lại (161 lớp) chia cho 4 máy, Chính ít hơn một chút: Chính 38, Kiều Anh, Quang, Hán mỗi máy 41** (chốt 26/11/2026). Xếp lớp theo thứ tự `sha256` cố định rồi giao lần lượt cho máy đang thiếu nhiều nhất so với tỷ lệ, nên mỗi máy nhận đủ ba bộ dữ liệu. Cả ba hệ thống của một lớp chạy trên cùng máy.

| Máy | Lớp | Bộ ARROW | Đo lại | Cần sửa | INVALID_OUTPUT |
|---|---:|---:|---:|---:|---:|
| Dung | 107 | 214 | 83 | 128 | 3 |
| Chính | 38 | 76 | 28 | 47 | 1 |
| Kiều Anh | 41 | 82 | 31 | 50 | 1 |
| Quang | 41 | 82 | 28 | 54 | 0 |
| Hán | 41 | 82 | 25 | 56 | 1 |
| **Tổng** | **268** | **536** | **195** | **335** | **6** |

## 9. Kết quả dùng ở đâu

- Bảng RQ3 (từng dataset, từng mô hình): bộ ARROW với cột `source` (initial / adaptive), để người đọc biết bộ nào VALID
  ngay, bộ nào qua sửa. INVALID_OUTPUT tính vào mẫu số tỷ lệ qua từng bước.
- So sánh chính: ARROW – EvoSuite, ARROW – ChatUniTest (cùng mô hình); chỉ số chính là điểm đột biến.
- Bảng chi phí ARROW – ChatUniTest: lượt gọi, token, thời gian mỗi lớp.
- EvoSuite cùng ngân sách: `search_budget` = trung vị qua 2 mô hình của (thời gian TN1 + thời gian sửa ở TN3) của lớp đó.
  Vì vậy nhánh ARROW của một lớp phải xong **trước** cấu hình phụ của EvoSuite.
- Phát hiện lỗi thật (GitBug-Java): mọi bộ ARROW VALID trên GitBug-Java.

## 10. Tham khảo từ bản dựng thử (đã xóa 26/11/2026)

Bản dựng thử chạy phần GPT-6 Luna của máy Dung (123 bộ), chỉ để tham khảo, **không dùng làm kết quả**:

| Loại | Số bộ | VALID |
|---|---:|---:|
| Đo lại | 62 | 62 |
| Sửa (Adaptive, N = 10) | 61 | 47 (77%); còn lại RUNTIME 8, ASSERTION 3, DISCOVERY 2, COMPILE 1 |

Script và kết quả ở `~/ARROW_backups/tn3-ban-dung-truoc-20261126/` trên máy Dung (`tn3_arrow.py`, `tn3_lists.py`,
`tn3_arrow.jsonl`, `results/dung/arrow_runs.jsonl`).

Chi phí Qwen ước theo giá Amazon Bedrock ($0,15 / $0,60 mỗi triệu token vào / ra) và lượng token thật ở TN2: khoảng
$0,00066 mỗi lượt gọi → 206 bộ Qwen cần sửa tối đa khoảng $1–1,5.
