# Pilot chọn N: kết quả

Máy: dung · chain: 188 (dùng 188; lỗi hạ tầng 0; c0 kiểm lại đã VALID 0)

## N = 10

Quy tắc B, ngưỡng 1% của S(12) = 81 → 0.81 chain. Không có k nào trong 5–10 thỏa → N = 10 (chạm biên: ghi là giới hạn).

| k | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| S(k) | 51 | 65 | 69 | 70 | 74 | 76 | 79 | 79 | 80 | 81 | 81 | 81 |
| Δ(k) | 51 | 14 | 4 | 1 | 4 | 2 | 3 | 0 | 1 | 1 | 0 | 0 |
| RSR@k | 27.1% | 34.6% | 36.7% | 37.2% | 39.4% | 40.4% | 42.0% | 42.0% | 42.6% | 43.1% | 43.1% | 43.1% |

**Tỷ lệ mất khi dừng ở N:** ℓ = (81 − 81) / 81 = 0.0%, cận trên 95% (Clopper–Pearson) 4.5%.

## Kiểm công bằng (ngưỡng 5%)

Số chain còn sửa thành công ở vòng N+1..12, trên số chain thành công của chính nhóm đó. Không so sánh tỷ lệ thành công giữa các cơ chế.

| Nhóm | Thành công ở vòng N+1..12 | Trên tổng thành công | Tỷ lệ | Giới hạn? |
|---|---|---|---|---|
| cơ chế F+ | 0 | 18 | 0.0% | không |
| cơ chế F− | 0 | 19 | 0.0% | không |
| cơ chế F2 | 0 | 25 | 0.0% | không |
| cơ chế A | 0 | 19 | 0.0% | không |
| mô hình gpt6luna | 0 | 49 | 0.0% | không |
| mô hình qwen3coder-or | 0 | 32 | 0.0% | không |

## Độ nhạy theo ngưỡng (chỉ minh họa, N dùng ngưỡng 1%)

| Ngưỡng | N | Chain bị mất | ℓ | Cận trên 95% |
|---|---|---|---|---|
| 1% | 10 | 0 | 0.0% | 4.5% |
| 2% | 9 | 1 | 1.2% | 6.7% |
| 5% | 5 | 7 | 8.6% | 17.0% |

## Thời gian và token một vòng sửa (để ước giờ máy TN2)

| Mô hình sửa | Số vòng | Trung vị (s) | p90 (s) | Gọi mô hình trung vị (s) | Build trung vị (s) | Token vào/ra trung bình | Số vòng TB mỗi chain nếu N = 10 |
|---|---|---|---|---|---|---|---|
| gpt6luna | 225 | 11.7 | 34.8 | 6.0 | 6.0 | 4057.3 / 745.7 | 3.38 |
| qwen3coder-or | 1226 | 6.5 | 19.1 | 3.8 | 2.5 | 4676.1 / 1208.0 | 8.08 |

## Thời gian từng công đoạn

_Theo mô hình · máy: máy khác nhau thì cấu hình và số worker khác nhau, chỉ so các mô hình trên cùng một máy._

**Sinh c0** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 (clone, phân tích, baseline) | gọi mô hình sinh | kiểm c0 (tổng build) | biên dịch | chạy test đích | chạy test module | tổng |
|---|---|---|---|---|---|---|---|
| gpt6luna · dung | 4.3 / 13.9 (n=36) | 6.0 / 12.2 (n=36) | 5.0 / 9.2 (n=36) | 2.0 / 2.9 (n=36) | 2.1 / 3.3 (n=25) | 2.1 / 4.1 (n=21) | 18.2 / 27.5 (n=36) |
| qwen3coder-or · dung | 3.6 / 4.6 (n=36) | 9.3 / 39.4 (n=36) | 2.4 / 5.8 (n=35) | 1.9 / 2.9 (n=35) | 2.2 / 3.5 (n=9) | 2.1 / 2.3 (n=3) | 16.2 / 47.8 (n=36) |

**Sửa: mỗi chain** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 | kiểm lại c0 | khối 3 (kiểm + mọi vòng sửa) | tổng chain |
|---|---|---|---|---|
| gpt6luna · dung | 4.0 / 14.5 (n=60) | 2.2 / 6.5 (n=60) | 25.8 / 190.2 (n=60) | 30.4 / 232.3 (n=60) |
| qwen3coder-or · dung | 4.2 / 12.2 (n=128) | 2.3 / 4.4 (n=128) | 85.1 / 180.2 (n=128) | 90.3 / 194.5 (n=128) |

**Sửa: mỗi vòng** (giây, trung vị / p90; n = số lần đo)

| Mô hình | gọi mô hình sửa | build (tổng) | biên dịch | chạy test đích | chạy test module | tổng vòng |
|---|---|---|---|---|---|---|
| gpt6luna · dung | 6.0 / 15.5 (n=225) | 6.0 / 9.9 (n=225) | 2.8 / 3.2 (n=211) | 3.1 / 3.9 (n=170) | 4.5 / 118.3 (n=49) | 11.7 / 34.8 (n=225) |
| qwen3coder-or · dung | 3.8 / 15.8 (n=1226) | 2.5 / 5.8 (n=1226) | 2.3 / 3.1 (n=1002) | 2.8 / 3.5 (n=330) | 2.0 / 2.5 (n=32) | 6.5 / 19.1 (n=1226) |

## Kiểm tra tính hợp lệ

- Chain không thành công nhưng chạy chưa đủ 12 vòng: 0
- Prompt sửa dùng: repair.system ['team-v1:cf5e5868c02d']; repair.user.level1 ['team-v1:f085fe3b62aa']; repair.user.level2 ['team-v1:8150e6a250a7']; repair.user.level3 ['team-v1:b45dc39ad007']
