# Kết quả Thực nghiệm 1 (RQ1a)

**⚠ CHƯA ĐỦ DỮ LIỆU — bảng dưới chỉ là kết quả tạm, chưa dùng cho báo cáo.**

- File đầu vào: 1 · máy: trial_luot3b · mô hình: gpt6luna
- Bản tool: d0f2b5c

## Bảng 0. Mức đủ dữ liệu

| Dataset | Mô hình | Dự kiến | Đã có | Tính vào mẫu số | Lỗi hạ tầng/bị chặn | Còn thiếu |
|---|---|---|---|---|---|---|
| classes2test | gpt6luna | 600 | 30 | 30 | 0 | 570 |
| catlm | gpt6luna | 108 | 0 | 0 | 0 | 108 |
| gitbug-java | gpt6luna | 96 | 0 | 0 | 0 | 96 |

## Dataset: classes2test

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 10 | 0/10 (0.0%) | 7/10 (70.0% [39.7–89.2]) | 7/10 (70.0%) | 4/10 (40.0%) | 2/10 (20.0%) | 2/10 (20.0% [5.7–51.0]) | 7/10 (70.0% [39.7–89.2]) |
| gpt6luna | Few-shot | 10 | 0/10 (0.0%) | 5/10 (50.0% [23.7–76.3]) | 5/10 (50.0%) | 5/10 (50.0%) | 3/10 (30.0%) | 3/10 (30.0% [10.8–60.3]) | 5/10 (50.0% [23.7–76.3]) |
| gpt6luna | Project-aware | 10 | 0/10 (0.0%) | 9/10 (90.0% [59.6–98.2]) | 9/10 (90.0%) | 6/10 (60.0%) | 3/10 (30.0%) | 3/10 (30.0% [10.8–60.3]) | 9/10 (90.0% [59.6–98.2]) |

### Bảng 2. Kiểm định

| Mô hình | Chỉ số | Số lớp ghép cặp | Cochran's Q (p) | Cặp so sánh | b / c | Chênh lệch [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | ESR | 10 | 6.00 (0.050) | Zero-shot – Few-shot | 2 / 0 | +20.0 [-4.8; +44.8] | 0.500 | 1.000 | chính |
| gpt6luna | ESR | 10 | 6.00 (0.050) | Zero-shot – Project-aware | 0 / 2 | -20.0 [-44.8; +4.8] | 0.500 | 1.000 | chính |
| gpt6luna | ESR | 10 | 6.00 (0.050) | Few-shot – Project-aware | 0 / 4 | -40.0 [-70.4; -9.6] | 0.125 | 0.375 | chính |
| gpt6luna | CSR (bước 1) | 10 | 6.00 (0.050) | Zero-shot – Few-shot | 2 / 0 | +20.0 [-4.8; +44.8] | 0.500 | 1.000 | phụ |
| gpt6luna | CSR (bước 1) | 10 | 6.00 (0.050) | Zero-shot – Project-aware | 0 / 2 | -20.0 [-44.8; +4.8] | 0.500 | 1.000 | phụ |
| gpt6luna | CSR (bước 1) | 10 | 6.00 (0.050) | Few-shot – Project-aware | 0 / 4 | -40.0 [-70.4; -9.6] | 0.125 | 1.000 | phụ |
| gpt6luna | Bước 2 | 10 | 6.00 (0.050) | Zero-shot – Few-shot | 2 / 0 | +20.0 [-4.8; +44.8] | 0.500 | 1.000 | phụ |
| gpt6luna | Bước 2 | 10 | 6.00 (0.050) | Zero-shot – Project-aware | 0 / 2 | -20.0 [-44.8; +4.8] | 0.500 | 1.000 | phụ |
| gpt6luna | Bước 2 | 10 | 6.00 (0.050) | Few-shot – Project-aware | 0 / 4 | -40.0 [-70.4; -9.6] | 0.125 | 1.000 | phụ |
| gpt6luna | Bước 3 | 10 | 1.50 (0.472) | Zero-shot – Few-shot | 1 / 2 | -10.0 [-43.4; +23.4] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 3 | 10 | 1.50 (0.472) | Zero-shot – Project-aware | 1 / 3 | -20.0 [-57.2; +17.2] | 0.625 | 1.000 | phụ |
| gpt6luna | Bước 3 | 10 | 1.50 (0.472) | Few-shot – Project-aware | 0 / 1 | -10.0 [-28.6; +8.6] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 4 | 10 | 0.40 (0.819) | Zero-shot – Few-shot | 1 / 2 | -10.0 [-43.4; +23.4] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 4 | 10 | 0.40 (0.819) | Zero-shot – Project-aware | 1 / 2 | -10.0 [-43.4; +23.4] | 1.000 | 1.000 | phụ |
| gpt6luna | Bước 4 | 10 | 0.40 (0.819) | Few-shot – Project-aware | 2 / 2 | +0.0 [-39.2; +39.2] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 10 | 0.40 (0.819) | Zero-shot – Few-shot | 1 / 2 | -10.0 [-43.4; +23.4] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 10 | 0.40 (0.819) | Zero-shot – Project-aware | 1 / 2 | -10.0 [-43.4; +23.4] | 1.000 | 1.000 | phụ |
| gpt6luna | VALID (bước 5) | 10 | 0.40 (0.819) | Few-shot – Project-aware | 2 / 2 | +0.0 [-39.2; +39.2] | 1.000 | 1.000 | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 2 | 3 | 0 | 0 | 3 | 2 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 3 | 5 | 0 | 0 | 0 | 2 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 3 | 1 | 0 | 0 | 3 | 3 | 0 | 0 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 2433 / 1959 | 844 / 880 | 7.3 | - |
| gpt6luna | Few-shot | 3794 / 3320 | 728 / 634 | 6.3 | - |
| gpt6luna | Project-aware | 2742 / 2213 | 821 / 616 | 6.1 | - |

## Dataset: catlm

_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); không có ý nghĩa thống kê không có nghĩa là không có khác biệt._

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 0 | - | - | - | - | - | - | - |
| gpt6luna | Few-shot | 0 | - | - | - | - | - | - | - |
| gpt6luna | Project-aware | 0 | - | - | - | - | - | - | - |

### Bảng 2. Kiểm định

| Mô hình | Chỉ số | Số lớp ghép cặp | Cochran's Q (p) | Cặp so sánh | b / c | Chênh lệch [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | ESR | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | chính |
| gpt6luna | ESR | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | chính |
| gpt6luna | ESR | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | chính |
| gpt6luna | CSR (bước 1) | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | CSR (bước 1) | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | CSR (bước 1) | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 2 | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 2 | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 2 | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 3 | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 3 | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 3 | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 4 | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 4 | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 4 | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | VALID (bước 5) | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | VALID (bước 5) | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | VALID (bước 5) | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | - | - | - | - |
| gpt6luna | Few-shot | - | - | - | - |
| gpt6luna | Project-aware | - | - | - | - |

## Dataset: gitbug-java

_Ít lớp nên lực thống kê thấp (CI 95% khoảng ±16–17 điểm %); không có ý nghĩa thống kê không có nghĩa là không có khác biệt._

### Bảng 1. Kết quả RQ1a

| Mô hình | Cách viết prompt | Số bộ test | Không ra code | Qua bước 1 (CSR) | Qua bước 2 | Qua bước 3 | Qua bước 4 | VALID | ESR |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 0 | - | - | - | - | - | - | - |
| gpt6luna | Few-shot | 0 | - | - | - | - | - | - | - |
| gpt6luna | Project-aware | 0 | - | - | - | - | - | - | - |

### Bảng 2. Kiểm định

| Mô hình | Chỉ số | Số lớp ghép cặp | Cochran's Q (p) | Cặp so sánh | b / c | Chênh lệch [CI 95%] | p McNemar | p sau Holm | Họ |
|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | ESR | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | chính |
| gpt6luna | ESR | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | chính |
| gpt6luna | ESR | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | chính |
| gpt6luna | CSR (bước 1) | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | CSR (bước 1) | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | CSR (bước 1) | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 2 | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 2 | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 2 | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 3 | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 3 | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 3 | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 4 | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 4 | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | Bước 4 | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | VALID (bước 5) | 0 | mô tả (<10 cặp) | Zero-shot – Few-shot | 0 / 0 | - | - | - | phụ |
| gpt6luna | VALID (bước 5) | 0 | mô tả (<10 cặp) | Zero-shot – Project-aware | 0 / 0 | - | - | - | phụ |
| gpt6luna | VALID (bước 5) | 0 | mô tả (<10 cặp) | Few-shot – Project-aware | 0 / 0 | - | - | - | phụ |

### Bảng 3. Bộ test dừng ở đâu

| Mô hình | Cách viết prompt | VALID | COMPILE | UNKNOWN | DISCOVERY | RUNTIME | ASSERTION | REGRESSION | INVALID_OUTPUT | Không tính (hạ tầng/bị chặn) | Có test cũ nghi chập chờn |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gpt6luna | Zero-shot | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| gpt6luna | Few-shot | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| gpt6luna | Project-aware | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

### Bảng 4. Token và thời gian gọi mô hình

| Mô hình | Cách viết prompt | Token prompt (TB / trung vị) | Token đầu ra (TB / trung vị) | Thời gian gọi (trung vị, giây) | Prompt có thể vượt num_ctx (ước tính) |
|---|---|---|---|---|---|
| gpt6luna | Zero-shot | - | - | - | - |
| gpt6luna | Few-shot | - | - | - | - |
| gpt6luna | Project-aware | - | - | - | - |

## Bảng 5. Framework test

| Dataset | Số lớp | JUnit 4 | JUnit 5 | TestNG |
|---|---|---|---|---|
| classes2test | 10 | 6 | 3 | 1 |

## Bảng 6. JDK × framework × build tool (dùng cho TN3)

| Dataset | Build tool | JDK | JUnit 4 | JUnit 5 + vintage | JUnit 5 không vintage | TestNG |
|---|---|---|---|---|---|---|
| classes2test | maven | 8 | 3 | 0 | 2 | 1 |
| classes2test | maven | 11 | 2 | 0 | 0 | 0 |
| classes2test | maven | 17 | 1 | 0 | 0 | 0 |
| classes2test | gradle | 8 | 0 | 0 | 1 | 0 |

## Ghi chú

- Mẫu số: bộ test đã chạy xong, kể cả không ra code; không gồm lỗi hạ tầng và lớp bị chặn ở khối 1.
- CSR: qua bước biên dịch; UNKNOWN không tính là biên dịch được. ESR (chỉ số chính): có ≥ 1 phương thức test đã chạy.
- Kiểm định chỉ dùng lớp có đủ 3 cách viết prompt; McNemar chính xác; Holm: họ chính = ESR, họ phụ = các chỉ số còn lại.
- Cột "Prompt có thể vượt num_ctx" là ước tính thô (4 ký tự ≈ 1 token).
