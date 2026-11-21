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
| qwen3coder-or | Zero-shot | 200 | 3/200 (1.5%) | 75/200 (37.5% [31.1–44.4]) | 75/200 (37.5%) | 53/200 (26.5%) | 32/200 (16.0%) | 32/200 (16.0% [11.6–21.7]) | 72/200 (36.0% [29.7–42.9]) |
| qwen3coder-or | Few-shot | 200 | 3/200 (1.5%) | 74/200 (37.0% [30.6–43.9]) | 74/200 (37.0%) | 58/200 (29.0%) | 41/200 (20.5%) | 40/200 (20.0% [15.0–26.1]) | 71/200 (35.5% [29.2–42.3]) |
| qwen3coder-or | Project-aware | 200 | 4/200 (2.0%) | 80/200 (40.0% [33.5–46.9]) | 79/200 (39.5%) | 59/200 (29.5%) | 40/200 (20.0%) | 39/200 (19.5% [14.6–25.5]) | 76/200 (38.0% [31.6–44.9]) |

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
| qwen3coder-or | ESR | 200 | 0.84 (0.657) | Zero-shot – Few-shot | 12 / 11 | +0.5 [-4.2; +5.2] | 1.000 | 1.000 | chính |
| qwen3coder-or | ESR | 200 | 0.84 (0.657) | Zero-shot – Project-aware | 17 / 21 | -2.0 [-8.0; +4.0] | 0.627 | 1.000 | chính |
| qwen3coder-or | ESR | 200 | 0.84 (0.657) | Few-shot – Project-aware | 17 / 22 | -2.5 [-8.6; +3.6] | 0.522 | 1.000 | chính |
| qwen3coder-or | CSR (bước 1) | 200 | 1.22 (0.545) | Zero-shot – Few-shot | 12 / 11 | +0.5 [-4.2; +5.2] | 1.000 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 200 | 1.22 (0.545) | Zero-shot – Project-aware | 17 / 22 | -2.5 [-8.6; +3.6] | 0.522 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 200 | 1.22 (0.545) | Few-shot – Project-aware | 17 / 23 | -3.0 [-9.2; +3.2] | 0.430 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 200 | 0.84 (0.657) | Zero-shot – Few-shot | 12 / 11 | +0.5 [-4.2; +5.2] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 200 | 0.84 (0.657) | Zero-shot – Project-aware | 17 / 21 | -2.0 [-8.0; +4.0] | 0.627 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 200 | 0.84 (0.657) | Few-shot – Project-aware | 17 / 22 | -2.5 [-8.6; +3.6] | 0.522 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 200 | 1.35 (0.510) | Zero-shot – Few-shot | 7 / 12 | -2.5 [-6.8; +1.8] | 0.359 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 200 | 1.35 (0.510) | Zero-shot – Project-aware | 14 / 20 | -3.0 [-8.7; +2.7] | 0.392 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 200 | 1.35 (0.510) | Few-shot – Project-aware | 19 / 20 | -0.5 [-6.6; +5.6] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 200 | 4.29 (0.117) | Zero-shot – Few-shot | 5 / 14 | -4.5 [-8.7; -0.3] | 0.064 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 200 | 4.29 (0.117) | Zero-shot – Project-aware | 6 / 14 | -4.0 [-8.3; +0.3] | 0.115 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 200 | 4.29 (0.117) | Few-shot – Project-aware | 15 / 14 | +0.5 [-4.8; +5.8] | 1.000 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 200 | 3.56 (0.168) | Zero-shot – Few-shot | 5 / 13 | -4.0 [-8.1; +0.1] | 0.096 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 200 | 3.56 (0.168) | Zero-shot – Project-aware | 6 / 13 | -3.5 [-7.7; +0.7] | 0.167 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 200 | 3.56 (0.168) | Few-shot – Project-aware | 14 / 13 | +0.5 [-4.6; +5.6] | 1.000 | 1.000 | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 83 | 73 | 0 | 0 | 19 | 25 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 82 | 77 | 0 | 1 | 19 | 21 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 104 | 46 | 0 | 1 | 23 | 26 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Zero-shot | 32 | 122 | 0 | 0 | 22 | 21 | 0 | 3 | 0 | 0 |
| qwen3coder-or | Few-shot | 40 | 123 | 0 | 0 | 16 | 17 | 1 | 3 | 0 | 0 |
| qwen3coder-or | Project-aware | 39 | 116 | 0 | 1 | 20 | 19 | 1 | 4 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 2301 / 1772 | 593 / 505 | 4.8 | - |
| gpt6luna | Few-shot | 3662 / 3132 | 557 / 470 | 4.7 | - |
| gpt6luna | Project-aware | 2649 / 2148 | 618 / 508 | 4.8 | - |
| qwen3coder-or | Zero-shot | 2292 / 1776 | 1156 / 826 | 3.7 | 0 |
| qwen3coder-or | Few-shot | 3650 / 3134 | 1027 / 783 | 3.5 | 0 |
| qwen3coder-or | Project-aware | 2646 / 2150 | 1357 / 898 | 3.8 | 0 |

## Dataset: catlm

_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); không có ý nghĩa thống kê không có nghĩa là không có khác biệt._

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 36 | 0/36 (0.0%) | 17/36 (47.2% [32.0–63.0]) | 16/36 (44.4%) | 15/36 (41.7%) | 14/36 (38.9%) | 14/36 (38.9% [24.8–55.1]) | 16/36 (44.4% [29.5–60.4]) |
| gpt6luna | Few-shot | 36 | 0/36 (0.0%) | 18/36 (50.0% [34.5–65.5]) | 17/36 (47.2%) | 15/36 (41.7%) | 13/36 (36.1%) | 13/36 (36.1% [22.5–52.4]) | 17/36 (47.2% [32.0–63.0]) |
| gpt6luna | Project-aware | 36 | 0/36 (0.0%) | 33/36 (91.7% [78.2–97.1]) | 33/36 (91.7%) | 31/36 (86.1%) | 29/36 (80.6%) | 29/36 (80.6% [65.0–90.2]) | 33/36 (91.7% [78.2–97.1]) |
| qwen3coder-or | Zero-shot | 36 | 0/36 (0.0%) | 12/36 (33.3% [20.2–49.7]) | 11/36 (30.6%) | 9/36 (25.0%) | 6/36 (16.7%) | 6/36 (16.7% [7.9–31.9]) | 11/36 (30.6% [18.0–46.9]) |
| qwen3coder-or | Few-shot | 36 | 2/36 (5.6%) | 12/36 (33.3% [20.2–49.7]) | 11/36 (30.6%) | 10/36 (27.8%) | 8/36 (22.2%) | 8/36 (22.2% [11.7–38.1]) | 11/36 (30.6% [18.0–46.9]) |
| qwen3coder-or | Project-aware | 36 | 0/36 (0.0%) | 24/36 (66.7% [50.3–79.8]) | 23/36 (63.9%) | 18/36 (50.0%) | 14/36 (38.9%) | 14/36 (38.9% [24.8–55.1]) | 23/36 (63.9% [47.6–77.5]) |

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
| qwen3coder-or | ESR | 36 | 24.00 (<0.001) | Zero-shot – Few-shot | 0 / 0 | +0.0 [+0.0; +0.0] | 1.000 | 1.000 | chính |
| qwen3coder-or | ESR | 36 | 24.00 (<0.001) | Zero-shot – Project-aware | 0 / 12 | -33.3 [-48.7; -17.9] | <0.001 | 0.002 | chính |
| qwen3coder-or | ESR | 36 | 24.00 (<0.001) | Few-shot – Project-aware | 0 / 12 | -33.3 [-48.7; -17.9] | <0.001 | 0.002 | chính |
| qwen3coder-or | CSR (bước 1) | 36 | 24.00 (<0.001) | Zero-shot – Few-shot | 0 / 0 | +0.0 [+0.0; +0.0] | 1.000 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 36 | 24.00 (<0.001) | Zero-shot – Project-aware | 0 / 12 | -33.3 [-48.7; -17.9] | <0.001 | 0.010 | phụ |
| qwen3coder-or | CSR (bước 1) | 36 | 24.00 (<0.001) | Few-shot – Project-aware | 0 / 12 | -33.3 [-48.7; -17.9] | <0.001 | 0.010 | phụ |
| qwen3coder-or | Bước 2 | 36 | 24.00 (<0.001) | Zero-shot – Few-shot | 0 / 0 | +0.0 [+0.0; +0.0] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 36 | 24.00 (<0.001) | Zero-shot – Project-aware | 0 / 12 | -33.3 [-48.7; -17.9] | <0.001 | 0.010 | phụ |
| qwen3coder-or | Bước 2 | 36 | 24.00 (<0.001) | Few-shot – Project-aware | 0 / 12 | -33.3 [-48.7; -17.9] | <0.001 | 0.010 | phụ |
| qwen3coder-or | Bước 3 | 36 | 13.27 (0.001) | Zero-shot – Few-shot | 0 / 1 | -2.8 [-8.1; +2.6] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 36 | 13.27 (0.001) | Zero-shot – Project-aware | 1 / 10 | -25.0 [-41.1; -8.9] | 0.012 | 0.188 | phụ |
| qwen3coder-or | Bước 3 | 36 | 13.27 (0.001) | Few-shot – Project-aware | 1 / 9 | -22.2 [-37.8; -6.6] | 0.021 | 0.322 | phụ |
| qwen3coder-or | Bước 4 | 36 | 9.45 (0.009) | Zero-shot – Few-shot | 0 / 2 | -5.6 [-13.0; +1.9] | 0.500 | 1.000 | phụ |
| qwen3coder-or | Bước 4 | 36 | 9.45 (0.009) | Zero-shot – Project-aware | 1 / 9 | -22.2 [-37.8; -6.6] | 0.021 | 0.322 | phụ |
| qwen3coder-or | Bước 4 | 36 | 9.45 (0.009) | Few-shot – Project-aware | 2 / 8 | -16.7 [-33.0; -0.3] | 0.109 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 36 | 9.45 (0.009) | Zero-shot – Few-shot | 0 / 2 | -5.6 [-13.0; +1.9] | 0.500 | 1.000 | phụ |
| qwen3coder-or | VALID (bước 5) | 36 | 9.45 (0.009) | Zero-shot – Project-aware | 1 / 9 | -22.2 [-37.8; -6.6] | 0.021 | 0.322 | phụ |
| qwen3coder-or | VALID (bước 5) | 36 | 9.45 (0.009) | Few-shot – Project-aware | 2 / 8 | -16.7 [-33.0; -0.3] | 0.109 | 1.000 | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 14 | 19 | 0 | 1 | 1 | 1 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 13 | 18 | 0 | 1 | 2 | 2 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 29 | 3 | 0 | 0 | 2 | 2 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Zero-shot | 6 | 24 | 0 | 1 | 2 | 3 | 0 | 0 | 0 | 0 |
| qwen3coder-or | Few-shot | 8 | 22 | 0 | 1 | 1 | 2 | 0 | 2 | 0 | 0 |
| qwen3coder-or | Project-aware | 14 | 12 | 0 | 1 | 5 | 4 | 0 | 0 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 1331 / 1164 | 183 / 75 | 2.2 | - |
| gpt6luna | Few-shot | 2692 / 2525 | 178 / 80 | 1.9 | - |
| gpt6luna | Project-aware | 1564 / 1410 | 202 / 95 | 2.2 | - |
| qwen3coder-or | Zero-shot | 1329 / 1164 | 380 / 202 | 1.9 | 0 |
| qwen3coder-or | Few-shot | 2687 / 2522 | 756 / 167 | 1.9 | 0 |
| qwen3coder-or | Project-aware | 1563 / 1410 | 408 / 214 | 2.2 | 0 |

## Dataset: gitbug-java

_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); không có ý nghĩa thống kê không có nghĩa là không có khác biệt._

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 32 | 0/32 (0.0%) | 9/32 (28.1% [15.6–45.4]) | 9/32 (28.1%) | 8/32 (25.0%) | 4/32 (12.5%) | 4/32 (12.5% [5.0–28.1]) | 9/32 (28.1% [15.6–45.4]) |
| gpt6luna | Few-shot | 32 | 0/32 (0.0%) | 11/32 (34.4% [20.4–51.7]) | 11/32 (34.4%) | 8/32 (25.0%) | 5/32 (15.6%) | 5/32 (15.6% [6.9–31.8]) | 11/32 (34.4% [20.4–51.7]) |
| gpt6luna | Project-aware | 32 | 0/32 (0.0%) | 20/32 (62.5% [45.3–77.1]) | 20/32 (62.5%) | 16/32 (50.0%) | 6/32 (18.8%) | 6/32 (18.8% [8.9–35.3]) | 20/32 (62.5% [45.3–77.1]) |
| qwen3coder-or | Zero-shot | 32 | 2/32 (6.2%) | 5/32 (15.6% [6.9–31.8]) | 5/32 (15.6%) | 4/32 (12.5%) | 2/32 (6.2%) | 2/32 (6.2% [1.7–20.1]) | 5/32 (15.6% [6.9–31.8]) |
| qwen3coder-or | Few-shot | 32 | 2/32 (6.2%) | 5/32 (15.6% [6.9–31.8]) | 4/32 (12.5%) | 3/32 (9.4%) | 2/32 (6.2%) | 2/32 (6.2% [1.7–20.1]) | 4/32 (12.5% [5.0–28.1]) |
| qwen3coder-or | Project-aware | 32 | 2/32 (6.2%) | 12/32 (37.5% [22.9–54.7]) | 12/32 (37.5%) | 7/32 (21.9%) | 3/32 (9.4%) | 3/32 (9.4% [3.2–24.2]) | 12/32 (37.5% [22.9–54.7]) |

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
| qwen3coder-or | ESR | 32 | 9.50 (0.009) | Zero-shot – Few-shot | 2 / 1 | +3.1 [-7.4; +13.7] | 1.000 | 1.000 | chính |
| qwen3coder-or | ESR | 32 | 9.50 (0.009) | Zero-shot – Project-aware | 1 / 8 | -21.9 [-38.6; -5.1] | 0.039 | 0.154 | chính |
| qwen3coder-or | ESR | 32 | 9.50 (0.009) | Few-shot – Project-aware | 2 / 10 | -25.0 [-44.4; -5.6] | 0.039 | 0.154 | chính |
| qwen3coder-or | CSR (bước 1) | 32 | 7.54 (0.023) | Zero-shot – Few-shot | 2 / 2 | +0.0 [-12.2; +12.2] | 1.000 | 1.000 | phụ |
| qwen3coder-or | CSR (bước 1) | 32 | 7.54 (0.023) | Zero-shot – Project-aware | 1 / 8 | -21.9 [-38.6; -5.1] | 0.039 | 0.926 | phụ |
| qwen3coder-or | CSR (bước 1) | 32 | 7.54 (0.023) | Few-shot – Project-aware | 3 / 10 | -21.9 [-42.6; -1.1] | 0.092 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 32 | 9.50 (0.009) | Zero-shot – Few-shot | 2 / 1 | +3.1 [-7.4; +13.7] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 2 | 32 | 9.50 (0.009) | Zero-shot – Project-aware | 1 / 8 | -21.9 [-38.6; -5.1] | 0.039 | 0.926 | phụ |
| qwen3coder-or | Bước 2 | 32 | 9.50 (0.009) | Few-shot – Project-aware | 2 / 10 | -25.0 [-44.4; -5.6] | 0.039 | 0.926 | phụ |
| qwen3coder-or | Bước 3 | 32 | 3.25 (0.197) | Zero-shot – Few-shot | 2 / 1 | +3.1 [-7.4; +13.7] | 1.000 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 32 | 3.25 (0.197) | Zero-shot – Project-aware | 1 / 4 | -9.4 [-22.7; +3.9] | 0.375 | 1.000 | phụ |
| qwen3coder-or | Bước 3 | 32 | 3.25 (0.197) | Few-shot – Project-aware | 2 / 6 | -12.5 [-29.3; +4.3] | 0.289 | 1.000 | phụ |
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
| qwen3coder-or | Few-shot | 2 | 24 | 1 | 1 | 1 | 1 | 0 | 2 | 0 | 0 |
| qwen3coder-or | Project-aware | 3 | 17 | 1 | 0 | 5 | 4 | 0 | 2 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 3553 / 2382 | 740 / 682 | 5.6 | - |
| gpt6luna | Few-shot | 4914 / 3743 | 696 / 594 | 5.3 | - |
| gpt6luna | Project-aware | 3821 / 2694 | 806 / 704 | 5.6 | - |
| qwen3coder-or | Zero-shot | 3584 / 2376 | 2360 / 1266 | 4.7 | 0 |
| qwen3coder-or | Few-shot | 4942 / 3734 | 2337 / 1139 | 3.8 | 0 |
| qwen3coder-or | Project-aware | 3859 / 2683 | 2437 / 1268 | 6.6 | 0 |

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

## Bảng 7. Thời gian từng công đoạn (giây, trung vị / p90)

_Đo khi chạy song song nhiều worker, máy khác nhau thì cấu hình và số worker khác nhau: so thời gian giữa các mô hình chỉ trên cùng một máy. n = số bộ test có số đo (bước sau không chạy nếu bước trước lỗi; "-" = máy chưa nộp số liệu từng bước)._

| Mô hình | Máy | Số bộ test | Khối 1: clone, phân tích, baseline | Gọi mô hình | Kiểm c0 (tổng) | ↳ biên dịch | ↳ chạy test đích | ↳ chạy test module | Tổng một lần sinh |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | dung | 804 | 5.5 / 38.7 (n=804) | 4.4 / 9.5 (n=804) | 5.3 / 15.9 (n=804) | 2.2 / 4.8 (n=804) | 2.3 / 4.1 (n=512) | 3.8 / 20.6 (n=340) | 18.9 / 59.9 (n=804) |
| qwen3coder-or | chinh-qwen | 177 | 18.8 / 207.3 (n=177) | 3.5 / 12.8 (n=177) | 12.2 / 50.7 (n=175) | 5.8 / 26.1 (n=175) | 6.5 / 11.2 (n=72) | 12.6 / 43.9 (n=46) | 51.3 / 267.8 (n=177) |
| qwen3coder-or | dung-qwen | 243 | 5.5 / 25.4 (n=243) | 3.9 / 21.3 (n=243) | 3.0 / 7.5 (n=235) | 2.0 / 4.1 (n=235) | 2.2 / 3.6 (n=79) | 3.1 / 11.4 (n=26) | 16.4 / 59.4 (n=243) |
| qwen3coder-or | dung-qwen-fix | 3 | 3.5 / 8.4 (n=3) | 3.0 / 3.0 (n=3) | 2.3 / 4.1 (n=3) | 2.2 / 2.2 (n=3) | 2.3 / 2.3 (n=1) | - | 9.1 / 15.7 (n=3) |
| qwen3coder-or | han-qwen | 192 | 27.9 / 147.2 (n=192) | 3.1 / 8.7 (n=192) | 15.0 / 60.0 (n=187) | 9.8 / 29.8 (n=187) | 9.4 / 24.1 (n=72) | 17.7 / 68.6 (n=39) | 61.7 / 201.6 (n=192) |
| qwen3coder-or | quang-qwen | 189 | 15.6 / 135.6 (n=189) | 4.0 / 23.7 (n=189) | 10.2 / 30.3 (n=186) | 5.6 / 17.9 (n=186) | 5.4 / 9.5 (n=75) | 8.1 / 32.3 (n=37) | 46.6 / 169.6 (n=189) |
| qwen3coder-or | (mọi máy) | 804 | 14.0 / 132.1 (n=804) | 3.6 / 17.1 (n=804) | 8.5 / 36.8 (n=786) | 5.0 / 20.1 (n=786) | 5.4 / 12.8 (n=299) | 10.5 / 56.1 (n=148) | 35.9 / 184.4 (n=804) |

## Ghi chú

- Mẫu số: bộ test đã chạy xong, kể cả không ra code; không gồm lỗi hạ tầng và lớp bị chặn ở khối 1.
- CSR: qua bước biên dịch; UNKNOWN không tính là biên dịch được. ESR (chỉ số chính): có ≥ 1 phương thức test đã chạy.
- Kiểm định chỉ dùng lớp có đủ 3 cách viết prompt; McNemar chính xác; Holm: họ chính = ESR, họ phụ = các chỉ số còn lại.
- Cột "Prompt có thể vượt num_ctx" là ước tính thô (4 ký tự ≈ 1 token).
