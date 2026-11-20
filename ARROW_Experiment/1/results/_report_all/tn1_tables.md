# Kết quả Thực nghiệm 1 (RQ1a)

**ĐỦ DỮ LIỆU.**

- File đầu vào: 6 · máy: chinh-qwen, dung, dung-qwen, dung-qwen-fix, han-qwen, quang-qwen · mô hình: gpt6luna, qwen3coder-or
- Bản tool: d0f2b5c4c7e3

## Bảng 0. Mức đủ dữ liệu

| Dataset | Mô hình | Dự kiến | Đã có | Tính vào mẫu số | Lỗi hạ tầng/bị chặn | Còn thiếu |
|---|---|---|---|---|---|---|
| classes2test | gpt6luna | 600 | 600 | 600 | 0 | 0 |
| classes2test | qwen3coder-or | 600 | 600 | 600 | 0 | 0 |
| catlm | gpt6luna | 108 | 108 | 108 | 0 | 0 |
| catlm | qwen3coder-or | 108 | 108 | 108 | 0 | 0 |
| gitbug-java | gpt6luna | 96 | 96 | 96 | 0 | 0 |
| gitbug-java | qwen3coder-or | 96 | 96 | 96 | 0 | 0 |

## Dataset: classes2test

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 200 | 0/200 (0.0%) | 127/200 (63.5% [56.6–69.9]) | 127/200 (63.5%) | 108/200 (54.0%) | 83/200 (41.5%) | 83/200 (41.5% [34.9–48.4]) | 126/200 (63.0% [56.1–69.4]) |
| gpt6luna | Few-shot | 200 | 0/200 (0.0%) | 123/200 (61.5% [54.6–68.0]) | 122/200 (61.0%) | 103/200 (51.5%) | 82/200 (41.0%) | 82/200 (41.0% [34.4–47.9]) | 121/200 (60.5% [53.6–67.0]) |
| gpt6luna | Project-aware | 200 | 0/200 (0.0%) | 154/200 (77.0% [70.7–82.3]) | 153/200 (76.5%) | 130/200 (65.0%) | 104/200 (52.0%) | 104/200 (52.0% [45.1–58.8]) | 152/200 (76.0% [69.6–81.4]) |
| qwen3coder-or | Zero-shot | 200 | 2/200 (1.0%) | 76/200 (38.0% [31.6–44.9]) | 76/200 (38.0%) | 53/200 (26.5%) | 33/200 (16.5%) | 33/200 (16.5% [12.0–22.3]) | 73/200 (36.5% [30.1–43.4]) |
| qwen3coder-or | Few-shot | 200 | 3/200 (1.5%) | 73/200 (36.5% [30.1–43.4]) | 73/200 (36.5%) | 57/200 (28.5%) | 40/200 (20.0%) | 39/200 (19.5% [14.6–25.5]) | 70/200 (35.0% [28.7–41.8]) |
| qwen3coder-or | Project-aware | 200 | 5/200 (2.5%) | 82/200 (41.0% [34.4–47.9]) | 81/200 (40.5%) | 60/200 (30.0%) | 38/200 (19.0%) | 37/200 (18.5% [13.7–24.5]) | 78/200 (39.0% [32.5–45.9]) |

### Bảng 2. Kiểm định

| Mô hình | Chỉ số | Số lớp ghép cặp | Cochran's Q (p) | Cặp so sánh | b / c | Chênh lệch [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | ESR | 200 | 25.57 (<0.001) | Zero-shot – Few-shot | 18 / 13 | +2.5 [-2.9; +7.9] | 0.473 | 1.000 | chính |
| gpt6luna | ESR | 200 | 25.57 (<0.001) | Zero-shot – Project-aware | 13 / 39 | -13.0 [-19.8; -6.2] | <0.001 | 0.002 | chính |
| gpt6luna | ESR | 200 | 25.57 (<0.001) | Few-shot – Project-aware | 8 / 39 | -15.5 [-21.9; -9.1] | <0.001 | <0.001 | chính |
| gpt6luna | CSR (bước 1) | 200 | 25.46 (<0.001) | Zero-shot – Few-shot | 18 / 14 | +2.0 [-3.5; +7.5] | 0.597 | 1.000 | phụ |
| gpt6luna | CSR (bước 1) | 200 | 25.46 (<0.001) | Zero-shot – Project-aware | 13 / 40 | -13.5 [-20.4; -6.6] | <0.001 | 0.008 | phụ |
| gpt6luna | CSR (bước 1) | 200 | 25.46 (<0.001) | Few-shot – Project-aware | 9 / 40 | -15.5 [-22.0; -9.0] | <0.001 | <0.001 | phụ |
| gpt6luna | Bước 2 | 200 | 25.57 (<0.001) | Zero-shot – Few-shot | 18 / 13 | +2.5 [-2.9; +7.9] | 0.473 | 1.000 | phụ |
| gpt6luna | Bước 2 | 200 | 25.57 (<0.001) | Zero-shot – Project-aware | 13 / 39 | -13.0 [-19.8; -6.2] | <0.001 | 0.011 | phụ |
| gpt6luna | Bước 2 | 200 | 25.57 (<0.001) | Few-shot – Project-aware | 8 / 39 | -15.5 [-21.9; -9.1] | <0.001 | <0.001 | phụ |
| gpt6luna | Bước 3 | 200 | 17.94 (<0.001) | Zero-shot – Few-shot | 20 / 15 | +2.5 [-3.3; +8.3] | 0.500 | 1.000 | phụ |
| gpt6luna | Bước 3 | 200 | 17.94 (<0.001) | Zero-shot – Project-aware | 13 / 35 | -11.0 [-17.6; -4.4] | 0.002 | 0.052 | phụ |
| gpt6luna | Bước 3 | 200 | 17.94 (<0.001) | Few-shot – Project-aware | 14 / 41 | -13.5 [-20.5; -6.5] | <0.001 | 0.010 | phụ |
| gpt6luna | Bước 4 | 200 | 12.68 (0.002) | Zero-shot – Few-shot | 22 / 21 | +0.5 [-5.9; +6.9] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 4 | 200 | 12.68 (0.002) | Zero-shot – Project-aware | 15 / 36 | -10.5 [-17.3; -3.7] | 0.005 | 0.101 | phụ |
| gpt6luna | Bước 4 | 200 | 12.68 (0.002) | Few-shot – Project-aware | 15 / 37 | -11.0 [-17.9; -4.1] | 0.003 | 0.076 | phụ |
| gpt6luna | VALID (bước 5) | 200 | 12.68 (0.002) | Zero-shot – Few-shot | 22 / 21 | +0.5 [-5.9; +6.9] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 200 | 12.68 (0.002) | Zero-shot – Project-aware | 15 / 36 | -10.5 [-17.3; -3.7] | 0.005 | 0.101 | phụ |
| gpt6luna | VALID (bước 5) | 200 | 12.68 (0.002) | Few-shot – Project-aware | 15 / 37 | -11.0 [-17.9; -4.1] | 0.003 | 0.076 | phụ |
| qwen3coder-or | ESR | 200 | 1.96 (0.375) | Zero-shot – Few-shot | 12 / 9 | +1.5 [-3.0; +6.0] | 0.664 | 1.000 | chính |
| qwen3coder-or | ESR | 200 | 1.96 (0.375) | Zero-shot – Project-aware | 17 / 22 | -2.5 [-8.6; +3.6] | 0.522 | 1.000 | chính |
| qwen3coder-or | ESR | 200 | 1.96 (0.375) | Few-shot – Project-aware | 16 / 24 | -4.0 [-10.2; +2.2] | 0.268 | 1.000 | chính |
| qwen3coder-or | CSR (bước 1) | 200 | 2.47 (0.291) | Zero-shot – Few-shot | 12 / 9 | +1.5 [-3.0; +6.0] | 0.664 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 200 | 2.47 (0.291) | Zero-shot – Project-aware | 17 / 23 | -3.0 [-9.2; +3.2] | 0.430 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 200 | 2.47 (0.291) | Few-shot – Project-aware | 16 / 25 | -4.5 [-10.7; +1.7] | 0.211 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 200 | 1.96 (0.375) | Zero-shot – Few-shot | 12 / 9 | +1.5 [-3.0; +6.0] | 0.664 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 200 | 1.96 (0.375) | Zero-shot – Project-aware | 17 / 22 | -2.5 [-8.6; +3.6] | 0.522 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 200 | 1.96 (0.375) | Few-shot – Project-aware | 16 / 24 | -4.0 [-10.2; +2.2] | 0.268 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 200 | 1.61 (0.447) | Zero-shot – Few-shot | 8 / 12 | -2.0 [-6.4; +2.4] | 0.503 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 200 | 1.61 (0.447) | Zero-shot – Project-aware | 13 / 20 | -3.5 [-9.1; +2.1] | 0.296 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 200 | 1.61 (0.447) | Few-shot – Project-aware | 18 / 21 | -1.5 [-7.6; +4.6] | 0.749 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 200 | 2.36 (0.307) | Zero-shot – Few-shot | 6 / 13 | -3.5 [-7.7; +0.7] | 0.167 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 200 | 2.36 (0.307) | Zero-shot – Project-aware | 7 / 12 | -2.5 [-6.8; +1.8] | 0.359 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 200 | 2.36 (0.307) | Few-shot – Project-aware | 15 / 13 | +1.0 [-4.2; +6.2] | 0.851 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 200 | 1.81 (0.405) | Zero-shot – Few-shot | 6 / 12 | -3.0 [-7.1; +1.1] | 0.238 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 200 | 1.81 (0.405) | Zero-shot – Project-aware | 7 / 11 | -2.0 [-6.1; +2.1] | 0.481 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 200 | 1.81 (0.405) | Few-shot – Project-aware | 14 / 12 | +1.0 [-4.0; +6.0] | 0.845 | 1.000 | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 83 | 73 | 0 | 0 | 19 | 25 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 82 | 77 | 0 | 1 | 19 | 21 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 104 | 46 | 0 | 1 | 23 | 26 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Zero-shot | 33 | 122 | 0 | 0 | 23 | 20 | 0 | 2 | 0 | 0 |
| qwen3coder-or | Few-shot | 39 | 124 | 0 | 0 | 16 | 17 | 1 | 3 | 0 | 0 |
| qwen3coder-or | Project-aware | 37 | 113 | 0 | 1 | 21 | 22 | 1 | 5 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 2301 / 1772 | 593 / 505 | 4.8 | - |
| gpt6luna | Few-shot | 3662 / 3132 | 557 / 470 | 4.7 | - |
| gpt6luna | Project-aware | 2649 / 2148 | 618 / 508 | 4.8 | - |
| qwen3coder-or | Zero-shot | 2292 / 1776 | 1074 / 820 | 3.4 | 0 |
| qwen3coder-or | Few-shot | 3650 / 3134 | 1017 / 780 | 3.3 | 0 |
| qwen3coder-or | Project-aware | 2646 / 2150 | 1378 / 908 | 3.6 | 0 |

## Dataset: catlm

_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); không có ý nghĩa thống kê không có nghĩa là không có khác biệt._

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 36 | 0/36 (0.0%) | 17/36 (47.2% [32.0–63.0]) | 16/36 (44.4%) | 15/36 (41.7%) | 14/36 (38.9%) | 14/36 (38.9% [24.8–55.1]) | 16/36 (44.4% [29.5–60.4]) |
| gpt6luna | Few-shot | 36 | 0/36 (0.0%) | 18/36 (50.0% [34.5–65.5]) | 17/36 (47.2%) | 15/36 (41.7%) | 13/36 (36.1%) | 13/36 (36.1% [22.5–52.4]) | 17/36 (47.2% [32.0–63.0]) |
| gpt6luna | Project-aware | 36 | 0/36 (0.0%) | 33/36 (91.7% [78.2–97.1]) | 33/36 (91.7%) | 31/36 (86.1%) | 29/36 (80.6%) | 29/36 (80.6% [65.0–90.2]) | 33/36 (91.7% [78.2–97.1]) |
| qwen3coder-or | Zero-shot | 36 | 0/36 (0.0%) | 13/36 (36.1% [22.5–52.4]) | 12/36 (33.3%) | 11/36 (30.6%) | 8/36 (22.2%) | 8/36 (22.2% [11.7–38.1]) | 12/36 (33.3% [20.2–49.7]) |
| qwen3coder-or | Few-shot | 36 | 0/36 (0.0%) | 11/36 (30.6% [18.0–46.9]) | 10/36 (27.8%) | 8/36 (22.2%) | 8/36 (22.2%) | 8/36 (22.2% [11.7–38.1]) | 10/36 (27.8% [15.8–44.0]) |
| qwen3coder-or | Project-aware | 36 | 1/36 (2.8%) | 23/36 (63.9% [47.6–77.5]) | 22/36 (61.1%) | 18/36 (50.0%) | 14/36 (38.9%) | 14/36 (38.9% [24.8–55.1]) | 22/36 (61.1% [44.9–75.2]) |

### Bảng 2. Kiểm định

| Mô hình | Chỉ số | Số lớp ghép cặp | Cochran's Q (p) | Cặp so sánh | b / c | Chênh lệch [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | ESR | 36 | 28.74 (<0.001) | Zero-shot – Few-shot | 0 / 1 | -2.8 [-8.1; +2.6] | 1.000 | 1.000 | chính |
| gpt6luna | ESR | 36 | 28.74 (<0.001) | Zero-shot – Project-aware | 1 / 18 | -47.2 [-65.3; -29.2] | <0.001 | <0.001 | chính |
| gpt6luna | ESR | 36 | 28.74 (<0.001) | Few-shot – Project-aware | 1 / 17 | -44.4 [-62.4; -26.5] | <0.001 | <0.001 | chính |
| gpt6luna | CSR (bước 1) | 36 | 26.78 (<0.001) | Zero-shot – Few-shot | 0 / 1 | -2.8 [-8.1; +2.6] | 1.000 | 1.000 | phụ |
| gpt6luna | CSR (bước 1) | 36 | 26.78 (<0.001) | Zero-shot – Project-aware | 1 / 17 | -44.4 [-62.4; -26.5] | <0.001 | 0.004 | phụ |
| gpt6luna | CSR (bước 1) | 36 | 26.78 (<0.001) | Few-shot – Project-aware | 1 / 16 | -41.7 [-59.5; -23.8] | <0.001 | 0.006 | phụ |
| gpt6luna | Bước 2 | 36 | 28.74 (<0.001) | Zero-shot – Few-shot | 0 / 1 | -2.8 [-8.1; +2.6] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 2 | 36 | 28.74 (<0.001) | Zero-shot – Project-aware | 1 / 18 | -47.2 [-65.3; -29.2] | <0.001 | 0.002 | phụ |
| gpt6luna | Bước 2 | 36 | 28.74 (<0.001) | Few-shot – Project-aware | 1 / 17 | -44.4 [-62.4; -26.5] | <0.001 | 0.004 | phụ |
| gpt6luna | Bước 3 | 36 | 28.44 (<0.001) | Zero-shot – Few-shot | 1 / 1 | +0.0 [-7.7; +7.7] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 3 | 36 | 28.44 (<0.001) | Zero-shot – Project-aware | 1 / 17 | -44.4 [-62.4; -26.5] | <0.001 | 0.004 | phụ |
| gpt6luna | Bước 3 | 36 | 28.44 (<0.001) | Few-shot – Project-aware | 0 / 16 | -44.4 [-60.7; -28.2] | <0.001 | <0.001 | phụ |
| gpt6luna | Bước 4 | 36 | 25.37 (<0.001) | Zero-shot – Few-shot | 2 / 1 | +2.8 [-6.6; +12.2] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 4 | 36 | 25.37 (<0.001) | Zero-shot – Project-aware | 1 / 16 | -41.7 [-59.5; -23.8] | <0.001 | 0.006 | phụ |
| gpt6luna | Bước 4 | 36 | 25.37 (<0.001) | Few-shot – Project-aware | 1 / 17 | -44.4 [-62.4; -26.5] | <0.001 | 0.004 | phụ |
| gpt6luna | VALID (bước 5) | 36 | 25.37 (<0.001) | Zero-shot – Few-shot | 2 / 1 | +2.8 [-6.6; +12.2] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 36 | 25.37 (<0.001) | Zero-shot – Project-aware | 1 / 16 | -41.7 [-59.5; -23.8] | <0.001 | 0.006 | phụ |
| gpt6luna | VALID (bước 5) | 36 | 25.37 (<0.001) | Few-shot – Project-aware | 1 / 17 | -44.4 [-62.4; -26.5] | <0.001 | 0.004 | phụ |
| qwen3coder-or | ESR | 36 | 17.71 (<0.001) | Zero-shot – Few-shot | 2 / 0 | +5.6 [-1.9; +13.0] | 0.500 | 1.000 | chính |
| qwen3coder-or | ESR | 36 | 17.71 (<0.001) | Zero-shot – Project-aware | 1 / 11 | -27.8 [-44.3; -11.2] | 0.006 | 0.019 | chính |
| qwen3coder-or | ESR | 36 | 17.71 (<0.001) | Few-shot – Project-aware | 1 / 13 | -33.3 [-50.5; -16.1] | 0.002 | 0.007 | chính |
| qwen3coder-or | CSR (bước 1) | 36 | 17.71 (<0.001) | Zero-shot – Few-shot | 2 / 0 | +5.6 [-1.9; +13.0] | 0.500 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 36 | 17.71 (<0.001) | Zero-shot – Project-aware | 1 / 11 | -27.8 [-44.3; -11.2] | 0.006 | 0.114 | phụ |
| qwen3coder-or | CSR (bước 1) | 36 | 17.71 (<0.001) | Few-shot – Project-aware | 1 / 13 | -33.3 [-50.5; -16.1] | 0.002 | 0.037 | phụ |
| qwen3coder-or | Bước 2 | 36 | 17.71 (<0.001) | Zero-shot – Few-shot | 2 / 0 | +5.6 [-1.9; +13.0] | 0.500 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 36 | 17.71 (<0.001) | Zero-shot – Project-aware | 1 / 11 | -27.8 [-44.3; -11.2] | 0.006 | 0.114 | phụ |
| qwen3coder-or | Bước 2 | 36 | 17.71 (<0.001) | Few-shot – Project-aware | 1 / 13 | -33.3 [-50.5; -16.1] | 0.002 | 0.037 | phụ |
| qwen3coder-or | Bước 3 | 36 | 12.15 (0.002) | Zero-shot – Few-shot | 3 / 0 | +8.3 [-0.7; +17.4] | 0.250 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 36 | 12.15 (0.002) | Zero-shot – Project-aware | 2 / 9 | -19.4 [-36.3; -2.5] | 0.065 | 0.981 | phụ |
| qwen3coder-or | Bước 3 | 36 | 12.15 (0.002) | Few-shot – Project-aware | 1 / 11 | -27.8 [-44.3; -11.2] | 0.006 | 0.114 | phụ |
| qwen3coder-or | Bước 4 | 36 | 6.00 (0.050) | Zero-shot – Few-shot | 2 / 2 | +0.0 [-10.9; +10.9] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 36 | 6.00 (0.050) | Zero-shot – Project-aware | 2 / 8 | -16.7 [-33.0; -0.3] | 0.109 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 36 | 6.00 (0.050) | Few-shot – Project-aware | 2 / 8 | -16.7 [-33.0; -0.3] | 0.109 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 36 | 6.00 (0.050) | Zero-shot – Few-shot | 2 / 2 | +0.0 [-10.9; +10.9] | 1.000 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 36 | 6.00 (0.050) | Zero-shot – Project-aware | 2 / 8 | -16.7 [-33.0; -0.3] | 0.109 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 36 | 6.00 (0.050) | Few-shot – Project-aware | 2 / 8 | -16.7 [-33.0; -0.3] | 0.109 | 1.000 | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 14 | 19 | 0 | 1 | 1 | 1 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 13 | 18 | 0 | 1 | 2 | 2 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 29 | 3 | 0 | 0 | 2 | 2 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Zero-shot | 8 | 23 | 0 | 1 | 1 | 3 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Few-shot | 8 | 25 | 0 | 1 | 2 | 0 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Project-aware | 14 | 12 | 0 | 1 | 4 | 4 | 0 | 1 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 1331 / 1164 | 183 / 75 | 2.2 | - |
| gpt6luna | Few-shot | 2692 / 2525 | 178 / 80 | 1.9 | - |
| gpt6luna | Project-aware | 1564 / 1410 | 202 / 95 | 2.2 | - |
| qwen3coder-or | Zero-shot | 1329 / 1164 | 369 / 198 | 1.7 | 0 |
| qwen3coder-or | Few-shot | 2687 / 2522 | 341 / 186 | 1.7 | 0 |
| qwen3coder-or | Project-aware | 1563 / 1410 | 836 / 184 | 1.8 | 0 |

## Dataset: gitbug-java

_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); không có ý nghĩa thống kê không có nghĩa là không có khác biệt._

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 32 | 0/32 (0.0%) | 9/32 (28.1% [15.6–45.4]) | 9/32 (28.1%) | 8/32 (25.0%) | 4/32 (12.5%) | 4/32 (12.5% [5.0–28.1]) | 9/32 (28.1% [15.6–45.4]) |
| gpt6luna | Few-shot | 32 | 0/32 (0.0%) | 11/32 (34.4% [20.4–51.7]) | 11/32 (34.4%) | 8/32 (25.0%) | 5/32 (15.6%) | 5/32 (15.6% [6.9–31.8]) | 11/32 (34.4% [20.4–51.7]) |
| gpt6luna | Project-aware | 32 | 0/32 (0.0%) | 20/32 (62.5% [45.3–77.1]) | 20/32 (62.5%) | 16/32 (50.0%) | 6/32 (18.8%) | 6/32 (18.8% [8.9–35.3]) | 20/32 (62.5% [45.3–77.1]) |
| qwen3coder-or | Zero-shot | 32 | 2/32 (6.2%) | 5/32 (15.6% [6.9–31.8]) | 5/32 (15.6%) | 4/32 (12.5%) | 2/32 (6.2%) | 2/32 (6.2% [1.7–20.1]) | 5/32 (15.6% [6.9–31.8]) |
| qwen3coder-or | Few-shot | 32 | 2/32 (6.2%) | 4/32 (12.5% [5.0–28.1]) | 3/32 (9.4%) | 2/32 (6.2%) | 2/32 (6.2%) | 2/32 (6.2% [1.7–20.1]) | 3/32 (9.4% [3.2–24.2]) |
| qwen3coder-or | Project-aware | 32 | 2/32 (6.2%) | 11/32 (34.4% [20.4–51.7]) | 11/32 (34.4%) | 7/32 (21.9%) | 3/32 (9.4%) | 3/32 (9.4% [3.2–24.2]) | 11/32 (34.4% [20.4–51.7]) |

### Bảng 2. Kiểm định

| Mô hình | Chỉ số | Số lớp ghép cặp | Cochran's Q (p) | Cặp so sánh | b / c | Chênh lệch [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | ESR | 32 | 15.85 (<0.001) | Zero-shot – Few-shot | 0 / 2 | -6.2 [-14.6; +2.1] | 0.500 | 1.000 | chính |
| gpt6luna | ESR | 32 | 15.85 (<0.001) | Zero-shot – Project-aware | 0 / 11 | -34.4 [-50.8; -17.9] | <0.001 | 0.006 | chính |
| gpt6luna | ESR | 32 | 15.85 (<0.001) | Few-shot – Project-aware | 2 / 11 | -28.1 [-47.9; -8.3] | 0.022 | 0.112 | chính |
| gpt6luna | CSR (bước 1) | 32 | 15.85 (<0.001) | Zero-shot – Few-shot | 0 / 2 | -6.2 [-14.6; +2.1] | 0.500 | 1.000 | phụ |
| gpt6luna | CSR (bước 1) | 32 | 15.85 (<0.001) | Zero-shot – Project-aware | 0 / 11 | -34.4 [-50.8; -17.9] | <0.001 | 0.029 | phụ |
| gpt6luna | CSR (bước 1) | 32 | 15.85 (<0.001) | Few-shot – Project-aware | 2 / 11 | -28.1 [-47.9; -8.3] | 0.022 | 0.602 | phụ |
| gpt6luna | Bước 2 | 32 | 15.85 (<0.001) | Zero-shot – Few-shot | 0 / 2 | -6.2 [-14.6; +2.1] | 0.500 | 1.000 | phụ |
| gpt6luna | Bước 2 | 32 | 15.85 (<0.001) | Zero-shot – Project-aware | 0 / 11 | -34.4 [-50.8; -17.9] | <0.001 | 0.029 | phụ |
| gpt6luna | Bước 2 | 32 | 15.85 (<0.001) | Few-shot – Project-aware | 2 / 11 | -28.1 [-47.9; -8.3] | 0.022 | 0.602 | phụ |
| gpt6luna | Bước 3 | 32 | 11.64 (0.003) | Zero-shot – Few-shot | 1 / 1 | +0.0 [-8.7; +8.7] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 3 | 32 | 11.64 (0.003) | Zero-shot – Project-aware | 1 / 9 | -25.0 [-42.3; -7.7] | 0.021 | 0.602 | phụ |
| gpt6luna | Bước 3 | 32 | 11.64 (0.003) | Few-shot – Project-aware | 1 / 9 | -25.0 [-42.3; -7.7] | 0.021 | 0.602 | phụ |
| gpt6luna | Bước 4 | 32 | 1.00 (0.607) | Zero-shot – Few-shot | 1 / 2 | -3.1 [-13.7; +7.4] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 4 | 32 | 1.00 (0.607) | Zero-shot – Project-aware | 1 / 3 | -6.2 [-18.3; +5.8] | 0.625 | 1.000 | phụ |
| gpt6luna | Bước 4 | 32 | 1.00 (0.607) | Few-shot – Project-aware | 2 / 3 | -3.1 [-16.8; +10.5] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 32 | 1.00 (0.607) | Zero-shot – Few-shot | 1 / 2 | -3.1 [-13.7; +7.4] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 32 | 1.00 (0.607) | Zero-shot – Project-aware | 1 / 3 | -6.2 [-18.3; +5.8] | 0.625 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 32 | 1.00 (0.607) | Few-shot – Project-aware | 2 / 3 | -3.1 [-16.8; +10.5] | 1.000 | 1.000 | phụ |
| qwen3coder-or | ESR | 32 | 8.00 (0.018) | Zero-shot – Few-shot | 3 / 1 | +6.2 [-5.8; +18.3] | 0.625 | 1.000 | chính |
| qwen3coder-or | ESR | 32 | 8.00 (0.018) | Zero-shot – Project-aware | 2 / 8 | -18.8 [-37.0; -0.5] | 0.109 | 0.328 | chính |
| qwen3coder-or | ESR | 32 | 8.00 (0.018) | Few-shot – Project-aware | 2 / 10 | -25.0 [-44.4; -5.6] | 0.039 | 0.154 | chính |
| qwen3coder-or | CSR (bước 1) | 32 | 6.14 (0.046) | Zero-shot – Few-shot | 3 / 2 | +3.1 [-10.5; +16.8] | 1.000 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 32 | 6.14 (0.046) | Zero-shot – Project-aware | 2 / 8 | -18.8 [-37.0; -0.5] | 0.109 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 32 | 6.14 (0.046) | Few-shot – Project-aware | 3 / 10 | -21.9 [-42.6; -1.1] | 0.092 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 32 | 8.00 (0.018) | Zero-shot – Few-shot | 3 / 1 | +6.2 [-5.8; +18.3] | 0.625 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 32 | 8.00 (0.018) | Zero-shot – Project-aware | 2 / 8 | -18.8 [-37.0; -0.5] | 0.109 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 32 | 8.00 (0.018) | Few-shot – Project-aware | 2 / 10 | -25.0 [-44.4; -5.6] | 0.039 | 0.926 | phụ |
| qwen3coder-or | Bước 3 | 32 | 3.80 (0.150) | Zero-shot – Few-shot | 3 / 1 | +6.2 [-5.8; +18.3] | 0.625 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 32 | 3.80 (0.150) | Zero-shot – Project-aware | 2 / 5 | -9.4 [-25.3; +6.5] | 0.453 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 32 | 3.80 (0.150) | Few-shot – Project-aware | 2 / 7 | -15.6 [-33.2; +1.9] | 0.180 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 32 | 0.40 (0.819) | Zero-shot – Few-shot | 1 / 1 | +0.0 [-8.7; +8.7] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 32 | 0.40 (0.819) | Zero-shot – Project-aware | 1 / 2 | -3.1 [-13.7; +7.4] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 32 | 0.40 (0.819) | Few-shot – Project-aware | 2 / 3 | -3.1 [-16.8; +10.5] | 1.000 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 32 | 0.40 (0.819) | Zero-shot – Few-shot | 1 / 1 | +0.0 [-8.7; +8.7] | 1.000 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 32 | 0.40 (0.819) | Zero-shot – Project-aware | 1 / 2 | -3.1 [-13.7; +7.4] | 1.000 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 32 | 0.40 (0.819) | Few-shot – Project-aware | 2 / 3 | -3.1 [-16.8; +10.5] | 1.000 | 1.000 | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 4 | 22 | 1 | 0 | 1 | 4 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 5 | 20 | 1 | 0 | 3 | 3 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 6 | 11 | 1 | 0 | 4 | 10 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Zero-shot | 2 | 24 | 1 | 0 | 1 | 2 | 0 | 2 | 0 | 0 |
| qwen3coder-or | Few-shot | 2 | 25 | 1 | 1 | 1 | 0 | 0 | 2 | 0 | 0 |
| qwen3coder-or | Project-aware | 3 | 18 | 1 | 0 | 4 | 4 | 0 | 2 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 3553 / 2382 | 740 / 682 | 5.6 | - |
| gpt6luna | Few-shot | 4914 / 3743 | 696 / 594 | 5.3 | - |
| gpt6luna | Project-aware | 3821 / 2694 | 806 / 704 | 5.6 | - |
| qwen3coder-or | Zero-shot | 3584 / 2376 | 2316 / 1257 | 4.0 | 0 |
| qwen3coder-or | Few-shot | 4942 / 3734 | 2269 / 1158 | 3.8 | 0 |
| qwen3coder-or | Project-aware | 3859 / 2683 | 2342 / 1268 | 3.9 | 0 |

## Bảng 5. Framework test

| Dataset | Số lớp | JUnit 4 | JUnit 5 | TestNG |
|---|---|---|---|---|
| classes2test | 200 | 163 | 29 | 8 |
| catlm | 36 | 16 | 20 | 0 |
| gitbug-java | 32 | 15 | 17 | 0 |

## Bảng 6. JDK × framework × build tool (dùng cho TN3)

| Dataset | Build tool | JDK | JUnit 4 | JUnit 5 + vintage | JUnit 5 không vintage | TestNG |
|---|---|---|---|---|---|---|
| classes2test | maven | 8 | 131 | 5 | 17 | 6 |
| classes2test | maven | 11 | 17 | 0 | 3 | 2 |
| classes2test | maven | 17 | 8 | 0 | 2 | 0 |
| classes2test | gradle | 8 | 6 | 0 | 1 | 0 |
| classes2test | gradle | 17 | 1 | 0 | 1 | 0 |
| catlm | maven | 8 | 14 | 1 | 15 | 0 |
| catlm | maven | 11 | 0 | 1 | 2 | 0 |
| catlm | maven | 17 | 2 | 0 | 1 | 0 |
| gitbug-java | maven | 8 | 9 | 0 | 3 | 0 |
| gitbug-java | maven | 11 | 1 | 0 | 6 | 0 |
| gitbug-java | maven | 17 | 1 | 0 | 5 | 0 |
| gitbug-java | gradle | 8 | 2 | 0 | 2 | 0 |
| gitbug-java | gradle | 11 | 2 | 0 | 0 | 0 |
| gitbug-java | gradle | 17 | 0 | 0 | 1 | 0 |

## Ghi chú

- Mẫu số: bộ test đã chạy xong, kể cả không ra code; không gồm lỗi hạ tầng và lớp bị chặn ở khối 1.
- CSR: qua bước biên dịch; UNKNOWN không tính là biên dịch được. ESR (chỉ số chính): có ≥ 1 phương thức test đã chạy.
- Kiểm định chỉ dùng lớp có đủ 3 cách viết prompt; McNemar chính xác; Holm: họ chính = ESR, họ phụ = các chỉ số còn lại.
- Cột "Prompt có thể vượt num_ctx" là ước tính thô (4 ký tự ≈ 1 token).
