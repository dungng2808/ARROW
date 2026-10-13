# 07. Báo cáo pilot và chọn N

`arrow report --run-id <id> [--experiment <file>]` chạy trên run đã gom (`merge`) hoặc một run đơn; `arrow run` tự chạy bước này (S5) khi mọi bước trước đã chạy xong. Cấu hình thực nghiệm phải có cùng sha256 với `run_config.lock.json` (run gom: `machines/*.json`). Chỉ đọc dữ liệu trong `runs/<id>/`, không gọi model hay build.

## 1. Phần 1: môi trường, công cụ, chi phí

### 1.1. `part1_cost.csv`

Mỗi dòng một bước (`clone`, `baseline`, `llm_generate`, `build_verify`, `llm_repair`, `build_repair`, `evosuite`, `jacoco`, `pit`, `tsdetect`):

| Cột | Ý nghĩa |
| --- | --- |
| `step`, `dataset` | |
| `count` | Số lần chạy |
| `median_seconds`, `p90_seconds`, `max_seconds`, `total_seconds` | Thời gian (không gồm thời gian tạm dừng) |
| `prompt_tokens`, `completion_tokens`, `cost_usd` | Chỉ các bước gọi model; `cache_hit` không tính tiền |

### 1.2. `part1_infra_errors.csv`

`task_id, dataset, stage, reason, attempts, last_message` cho mọi job `INFRA`. Thêm bảng đếm theo `reason`.

### 1.3. Kết quả bước sinh test (chỉ để kiểm tra pipeline)

Số ứng viên theo trạng thái và theo quy trình prompting, theo dataset. Chi tiết từng ứng viên ở `part1_generation.csv` (dataset, task, model, quy trình, trạng thái, output không hợp lệ, error signature, số assertion, số test pass, model id). Đây **không** phải kết quả RQ1 (pilot không trả lời RQ); báo cáo ghi rõ điều này.

### 1.4. Ước tính cho thực nghiệm chính

Từ thời gian trung vị và chi phí trung bình mỗi ứng viên của pilot (GPT-6 Luna), ước tính cho phần GPT-6 Luna của thực nghiệm chính:

| Hạng mục | Số lượng |
| --- | --- |
| RQ1 | 200 × 3 + 32 × 3 = 696 ứng viên |
| RQ2 | Tập con 120 ứng viên Classes2Test (phần của GPT-6 Luna) + mọi ứng viên GitBug-Java thất bại, × 2 cơ chế × tối đa N vòng |
| RQ3 | Adaptive cho mọi ứng viên thất bại còn lại; EvoSuite, JaCoCo, PIT, tsDetect cho 232 CUT |

Ghi rõ: phần Qwen3-Coder-30B **không** ước tính được từ pilot (pilot chỉ chạy GPT-6 Luna).

## 2. Phần 2: chọn N (chỉ Classes2Test)

### 2.1. Dữ liệu

- Tập ứng viên: mọi ứng viên Classes2Test của pilot có `state ≠ VALID` và `≠ INFRA` sau bước sinh (của **mọi máy**, sau `merge`).
- Mỗi ứng viên có hai chuỗi sửa (Fixed, Adaptive), mỗi chuỗi tối đa 10 vòng. Tổng số chuỗi `M = 2 × số ứng viên`.
- Chuỗi có job `INFRA` chưa giải quyết bị loại khỏi tính toán và liệt kê riêng.
- Chỉ tính N khi **mọi** chuỗi còn lại đã VALID hoặc chạy đủ 10 vòng; ngược lại báo `N chưa tính được` và số chuỗi còn thiếu.

### 2.2. Đường cộng dồn chung

`S(k)` = số chuỗi đạt VALID ở vòng ≤ k, tính trên cả hai cơ chế gộp lại, k = 1…10. Xuất `part2_cumulative.csv`: `round, successes_cumulative, share_of_round10`.

Báo cáo pilot **không** xuất số liệu tách theo Fixed và Adaptive (Report 2: pilot không xem so sánh hai cơ chế). Số liệu tách vẫn còn trong log và chỉ được dùng ở thực nghiệm chính.

### 2.3. Quy tắc `share95` `[D8]`

```
if S(10) = 0: N = 10   (không có ca nào sửa được; ghi chú riêng)
else: N = max(3, min{ k ∈ [1, 10] : S(k) ≥ 0.95 × S(10) })
```

### 2.4. Quy tắc `report2` (Report 2 hiện tại)

```
N = min{ k ∈ [5, 10] : S(k−1) = S(k−2) và S(k) = S(k−1) }   # vòng k−1 và k không thêm ca nào
nếu không có k thỏa: N = 10
```

(`S(0) = 0`; "vòng k−1 và vòng k không có thêm ứng viên sửa thành công" nghĩa là `S(k−1) − S(k−2) = 0` và `S(k) − S(k−1) = 0`.)

### 2.5. Phân tích độ nhạy

Ngoài N theo quy tắc đang dùng, báo cáo luôn in thêm: N theo `report2`, N theo `share95` với ngưỡng 90%, 95%, 99%. Chỉ quy tắc ghi trong cấu hình thực nghiệm (`n_rule`) được dùng làm N chính thức.

Cách tính thời gian của bước `clone`: thời lượng job S0 trừ tổng thời gian các lệnh build của job (gồm cả tạo workspace); `baseline`: tổng thời gian các lần chạy test của module ở baseline. Ước tính 1.4 dùng trung vị theo bước (lấy dataset lớn nhất) và chi phí trung bình mỗi lượt gọi; RQ2 là cận trên (mọi chuỗi chạy đủ N vòng).

## 3. `pilot_report.md`

Các mục: thông tin run (experiment, các máy, model id, endpoint host theo máy, thời gian), phần 1 (bảng 1.1, 1.2, 1.3, 1.4), phần 2 (bảng cộng dồn, N và phân tích độ nhạy), danh sách ngoại lệ đã khai báo (cờ build, EvoSuite runtime, plugin PIT), các hạn chế (chỉ GPT-6 Luna; N áp dụng cho cả Qwen).

## 4. Kiểm thử

Dựng dữ liệu giả cho `S(k)` và kiểm tra cả hai quy tắc, gồm ví dụ trong `ARROW/pilot_tool/README.md`: `S = 40, 58, 66, 70, 72, 73, 73, 73, 74, 74` → `share95` cho N = 5, `report2` cho N = 8. Trường hợp `S(10) = 0`; chuỗi chưa đủ vòng → không tính N.
