# Pilot chọn N: kết quả

Máy: chinh · chain: 180 (dùng 180; lỗi hạ tầng 0; c0 kiểm lại đã VALID 0)

## N = 10

Quy tắc B, ngưỡng 1% của S(12) = 88 → 0.88 chain. Không có k nào trong 5–10 thỏa → N = 10 (chạm biên: ghi là giới hạn).

| k | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| S(k) | 49 | 70 | 76 | 79 | 79 | 84 | 86 | 87 | 88 | 88 | 88 | 88 |
| Δ(k) | 49 | 21 | 6 | 3 | 0 | 5 | 2 | 1 | 1 | 0 | 0 | 0 |
| RSR@k | 27.2% | 38.9% | 42.2% | 43.9% | 43.9% | 46.7% | 47.8% | 48.3% | 48.9% | 48.9% | 48.9% | 48.9% |

**Tỷ lệ mất khi dừng ở N:** ℓ = (88 − 88) / 88 = 0.0%, cận trên 95% (Clopper–Pearson) 4.1%.

## Kiểm công bằng (ngưỡng 5%)

Số chain còn sửa thành công ở vòng N+1..12, trên số chain thành công của chính nhóm đó. Không so sánh tỷ lệ thành công giữa các cơ chế.

| Nhóm | Thành công ở vòng N+1..12 | Trên tổng thành công | Tỷ lệ | Giới hạn? |
|---|---|---|---|---|
| cơ chế F+ | 0 | 20 | 0.0% | không |
| cơ chế F− | 0 | 18 | 0.0% | không |
| cơ chế F2 | 0 | 27 | 0.0% | không |
| cơ chế A | 0 | 23 | 0.0% | không |
| mô hình gpt6luna | 0 | 61 | 0.0% | không |
| mô hình qwen3coder-or | 0 | 27 | 0.0% | không |

## Độ nhạy theo ngưỡng (chỉ minh họa, N dùng ngưỡng 1%)

| Ngưỡng | N | Chain bị mất | ℓ | Cận trên 95% |
|---|---|---|---|---|
| 1% | 10 | 0 | 0.0% | 4.1% |
| 2% | 9 | 0 | 0.0% | 4.1% |
| 5% | 5 | 9 | 10.2% | 18.5% |

## Thời gian và token một vòng sửa (để ước giờ máy TN2)

| Mô hình sửa | Số vòng | Trung vị (s) | p90 (s) | Gọi mô hình trung vị (s) | Build trung vị (s) | Token vào/ra trung bình | Số vòng TB mỗi chain nếu N = 10 |
|---|---|---|---|---|---|---|---|
| gpt6luna | 325 | 21.2 | 49.8 | 11.6 | 7.5 | 5927.0 / 1191.5 | 3.59 |
| qwen3coder-or | 961 | 12.1 | 36.7 | 2.8 | 7.0 | 5309.1 / 1259.9 | 8.15 |

## Thời gian từng công đoạn

_Theo mô hình · máy: máy khác nhau thì cấu hình và số worker khác nhau, chỉ so các mô hình trên cùng một máy._

**Sinh c0** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 (clone, phân tích, baseline) | gọi mô hình sinh | kiểm c0 (tổng build) | biên dịch | chạy test đích | chạy test module | tổng |
|---|---|---|---|---|---|---|---|
| gpt6luna · chinh | 15.2 / 76.6 (n=36) | 6.3 / 17.7 (n=36) | 14.2 / 35.0 (n=36) | 4.6 / 13.9 (n=36) | 3.9 / 8.6 (n=28) | 8.3 / 120.3 (n=16) | 47.9 / 135.8 (n=36) |
| qwen3coder-or · chinh | 8.4 / 22.1 (n=36) | 7.6 / 27.2 (n=36) | 10.8 / 19.9 (n=36) | 4.4 / 13.9 (n=36) | 3.3 / 4.6 (n=14) | 5.5 / 114.7 (n=11) | 31.6 / 85.7 (n=36) |

**Sửa: mỗi chain** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 | kiểm lại c0 | khối 3 (kiểm + mọi vòng sửa) | tổng chain |
|---|---|---|---|---|
| gpt6luna · chinh | 18.9 / 53.2 (n=80) | 9.3 / 26.0 (n=80) | 92.8 / 296.6 (n=80) | 115.3 / 315.4 (n=80) |
| qwen3coder-or · chinh | 14.6 / 28.0 (n=100) | 6.8 / 22.4 (n=100) | 104.3 / 327.2 (n=100) | 126.7 / 364.8 (n=100) |

**Sửa: mỗi vòng** (giây, trung vị / p90; n = số lần đo)

| Mô hình | gọi mô hình sửa | build (tổng) | biên dịch | chạy test đích | chạy test module | tổng vòng |
|---|---|---|---|---|---|---|
| gpt6luna · chinh | 11.6 / 20.2 (n=325) | 7.5 / 31.1 (n=325) | 4.3 / 18.8 (n=293) | 3.7 / 13.0 (n=187) | 10.4 / 27.6 (n=61) | 21.2 / 49.8 (n=325) |
| qwen3coder-or · chinh | 2.8 / 9.1 (n=961) | 7.0 / 24.1 (n=961) | 5.1 / 20.1 (n=780) | 5.7 / 15.2 (n=360) | 14.1 / 26.2 (n=27) | 12.1 / 36.7 (n=961) |

## Kiểm tra tính hợp lệ

- Chain không thành công nhưng chạy chưa đủ 12 vòng: 0
- Prompt sửa dùng: repair.system ['team-v1:cf5e5868c02d']; repair.user.level1 ['team-v1:f085fe3b62aa']; repair.user.level2 ['team-v1:8150e6a250a7']; repair.user.level3 ['team-v1:b45dc39ad007']
