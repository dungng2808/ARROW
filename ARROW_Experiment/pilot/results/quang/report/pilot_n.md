# Pilot chọn N: kết quả

Máy: quang · chain: 240 (dùng 240; lỗi hạ tầng 0; c0 kiểm lại đã VALID 0)

## N = 10

Quy tắc B, ngưỡng 1% của S(12) = 92 → 0.92 chain. Không có k nào trong 5–10 thỏa → N = 10 (chạm biên: ghi là giới hạn).

| k | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| S(k) | 40 | 49 | 59 | 67 | 74 | 80 | 81 | 85 | 87 | 90 | 91 | 92 |
| Δ(k) | 40 | 9 | 10 | 8 | 7 | 6 | 1 | 4 | 2 | 3 | 1 | 1 |
| RSR@k | 16.7% | 20.4% | 24.6% | 27.9% | 30.8% | 33.3% | 33.8% | 35.4% | 36.2% | 37.5% | 37.9% | 38.3% |

**Tỷ lệ mất khi dừng ở N:** ℓ = (92 − 90) / 92 = 2.2%, cận trên 95% (Clopper–Pearson) 7.6%.

## Kiểm công bằng (ngưỡng 5%)

Số chain còn sửa thành công ở vòng N+1..12, trên số chain thành công của chính nhóm đó. Không so sánh tỷ lệ thành công giữa các cơ chế.

| Nhóm | Thành công ở vòng N+1..12 | Trên tổng thành công | Tỷ lệ | Giới hạn? |
|---|---|---|---|---|
| cơ chế F+ | 1 | 22 | 4.5% | không |
| cơ chế F− | 0 | 19 | 0.0% | không |
| cơ chế F2 | 0 | 28 | 0.0% | không |
| cơ chế A | 1 | 23 | 4.3% | không |
| mô hình gpt6luna | 2 | 74 | 2.7% | không |
| mô hình qwen3coder-or | 0 | 18 | 0.0% | không |

## Độ nhạy theo ngưỡng (chỉ minh họa, N dùng ngưỡng 1%)

| Ngưỡng | N | Chain bị mất | ℓ | Cận trên 95% |
|---|---|---|---|---|
| 1% | 10 | 2 | 2.2% | 7.6% |
| 2% | 10 | 2 | 2.2% | 7.6% |
| 5% | 8 | 7 | 7.6% | 15.1% |

## Thời gian và token một vòng sửa (để ước giờ máy TN2)

| Mô hình sửa | Số vòng | Trung vị (s) | p90 (s) | Gọi mô hình trung vị (s) | Build trung vị (s) | Token vào/ra trung bình | Số vòng TB mỗi chain nếu N = 10 |
|---|---|---|---|---|---|---|---|
| gpt6luna | 707 | 22.3 | 41.2 | 7.3 | 7.3 | 5657.4 / 985.9 | 5.61 |
| qwen3coder-or | 1370 | 10.7 | 34.4 | 3.5 | 6.5 | 5927.1 / 2164.9 | 8.98 |

## Thời gian từng công đoạn

_Theo mô hình · máy: máy khác nhau thì cấu hình và số worker khác nhau, chỉ so các mô hình trên cùng một máy._

**Sinh c0** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 (clone, phân tích, baseline) | gọi mô hình sinh | kiểm c0 (tổng build) | biên dịch | chạy test đích | chạy test module | tổng |
|---|---|---|---|---|---|---|---|
| gpt6luna · quang | 17.8 / 186.3 (n=39) | 6.8 / 20.8 (n=39) | 12.0 / 38.2 (n=39) | 5.4 / 14.3 (n=39) | 4.0 / 6.5 (n=23) | 7.9 / 26.5 (n=11) | 61.1 / 214.0 (n=39) |
| qwen3coder-or · quang | 10.7 / 58.4 (n=39) | 3.7 / 12.2 (n=39) | 8.3 / 21.8 (n=36) | 6.3 / 15.8 (n=36) | 5.3 / 10.6 (n=6) | 16.5 / 25.8 (n=4) | 26.0 / 95.2 (n=39) |

**Sửa: mỗi chain** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 | kiểm lại c0 | khối 3 (kiểm + mọi vòng sửa) | tổng chain |
|---|---|---|---|---|
| gpt6luna · quang | 9.9 / 126.5 (n=112) | 7.6 / 22.7 (n=112) | 145.2 / 363.3 (n=112) | 170.9 / 481.8 (n=112) |
| qwen3coder-or · quang | 9.8 / 116.8 (n=128) | 6.6 / 15.9 (n=128) | 128.7 / 408.9 (n=128) | 171.9 / 466.3 (n=128) |

**Sửa: mỗi vòng** (giây, trung vị / p90; n = số lần đo)

| Mô hình | gọi mô hình sửa | build (tổng) | biên dịch | chạy test đích | chạy test module | tổng vòng |
|---|---|---|---|---|---|---|
| gpt6luna · quang | 7.3 / 22.8 (n=707) | 7.3 / 27.9 (n=707) | 6.2 / 15.9 (n=638) | 3.8 / 11.4 (n=485) | 11.4 / 35.7 (n=74) | 22.3 / 41.2 (n=707) |
| qwen3coder-or · quang | 3.5 / 21.0 (n=1370) | 6.5 / 19.9 (n=1370) | 6.0 / 16.8 (n=1152) | 3.3 / 8.3 (n=302) | 4.7 / 22.8 (n=18) | 10.7 / 34.4 (n=1370) |

## Kiểm tra tính hợp lệ

- Chain không thành công nhưng chạy chưa đủ 12 vòng: 0
- Prompt sửa dùng: repair.system ['team-v1:cf5e5868c02d']; repair.user.level1 ['team-v1:f085fe3b62aa']; repair.user.level2 ['team-v1:8150e6a250a7']; repair.user.level3 ['team-v1:b45dc39ad007']
