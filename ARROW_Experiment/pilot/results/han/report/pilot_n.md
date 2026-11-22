# Pilot chọn N: kết quả

Máy: han · chain: 196 (dùng 196; lỗi hạ tầng 0; c0 kiểm lại đã VALID 0)

## N = 8

Quy tắc B, ngưỡng 1% của S(12) = 53 → 0.53 chain. Thỏa lần đầu tại k = 8.

| k | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| S(k) | 23 | 38 | 43 | 46 | 46 | 49 | 49 | 49 | 50 | 52 | 52 | 53 |
| Δ(k) | 23 | 15 | 5 | 3 | 0 | 3 | 0 | 0 | 1 | 2 | 0 | 1 |
| RSR@k | 11.7% | 19.4% | 21.9% | 23.5% | 23.5% | 25.0% | 25.0% | 25.0% | 25.5% | 26.5% | 26.5% | 27.0% |

**Tỷ lệ mất khi dừng ở N:** ℓ = (53 − 49) / 53 = 7.5%, cận trên 95% (Clopper–Pearson) 18.2%.

## Kiểm công bằng (ngưỡng 5%)

Số chain còn sửa thành công ở vòng N+1..12, trên số chain thành công của chính nhóm đó. Không so sánh tỷ lệ thành công giữa các cơ chế.

| Nhóm | Thành công ở vòng N+1..12 | Trên tổng thành công | Tỷ lệ | Giới hạn? |
|---|---|---|---|---|
| cơ chế F+ | 1 | 12 | 8.3% | **có** |
| cơ chế F− | 2 | 15 | 13.3% | **có** |
| cơ chế F2 | 1 | 14 | 7.1% | **có** |
| cơ chế A | 0 | 12 | 0.0% | không |
| mô hình gpt6luna | 4 | 34 | 11.8% | **có** |
| mô hình qwen3coder-or | 0 | 19 | 0.0% | không |

## Độ nhạy theo ngưỡng (chỉ minh họa, N dùng ngưỡng 1%)

| Ngưỡng | N | Chain bị mất | ℓ | Cận trên 95% |
|---|---|---|---|---|
| 1% | 8 | 4 | 7.5% | 18.2% |
| 2% | 8 | 4 | 7.5% | 18.2% |
| 5% | 8 | 4 | 7.5% | 18.2% |

## Thời gian và token một vòng sửa (để ước giờ máy TN2)

| Mô hình sửa | Số vòng | Trung vị (s) | p90 (s) | Gọi mô hình trung vị (s) | Build trung vị (s) | Token vào/ra trung bình | Số vòng TB mỗi chain nếu N = 8 |
|---|---|---|---|---|---|---|---|
| gpt6luna | 608 | 50.6 | 82.1 | 6.4 | 43.8 | 4149.0 / 608.2 | 5.67 |
| qwen3coder-or | 1247 | 31.6 | 58.9 | 3.5 | 27.7 | 6669.5 / 1224.8 | 7.03 |

## Thời gian từng công đoạn

_Theo mô hình · máy: máy khác nhau thì cấu hình và số worker khác nhau, chỉ so các mô hình trên cùng một máy._

**Sinh c0** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 (clone, phân tích, baseline) | gọi mô hình sinh | kiểm c0 (tổng build) | biên dịch | chạy test đích | chạy test module | tổng |
|---|---|---|---|---|---|---|---|
| gpt6luna · han | 31.5 / 255.4 (n=39) | 6.4 / 12.9 (n=39) | 42.5 / 104.6 (n=38) | 11.8 / 48.1 (n=38) | 9.9 / 17.2 (n=26) | 21.6 / 147.1 (n=19) | 99.8 / 368.2 (n=39) |
| qwen3coder-or · han | 19.7 / 63.6 (n=39) | 3.5 / 10.7 (n=39) | 32.9 / 63.5 (n=39) | 14.6 / 44.9 (n=39) | 9.2 / 16.5 (n=14) | 25.1 / 147.3 (n=9) | 62.9 / 154.0 (n=39) |

**Sửa: mỗi chain** (giây, trung vị / p90; n = số lần đo)

| Mô hình | khối 1 | kiểm lại c0 | khối 3 (kiểm + mọi vòng sửa) | tổng chain |
|---|---|---|---|---|
| gpt6luna · han | 58.3 / 251.2 (n=76) | 37.3 / 55.6 (n=76) | 475.4 / 848.3 (n=76) | 578.1 / 957.0 (n=76) |
| qwen3coder-or · han | 32.0 / 144.6 (n=120) | 22.9 / 51.4 (n=120) | 317.1 / 714.3 (n=120) | 377.2 / 802.4 (n=120) |

**Sửa: mỗi vòng** (giây, trung vị / p90; n = số lần đo)

| Mô hình | gọi mô hình sửa | build (tổng) | biên dịch | chạy test đích | chạy test module | tổng vòng |
|---|---|---|---|---|---|---|
| gpt6luna · han | 6.4 / 14.0 (n=608) | 43.8 / 76.0 (n=608) | 27.8 / 56.5 (n=539) | 15.0 / 53.2 (n=345) | 26.1 / 189.4 (n=35) | 50.6 / 82.1 (n=608) |
| qwen3coder-or · han | 3.5 / 6.0 (n=1247) | 27.7 / 53.2 (n=1247) | 17.2 / 49.4 (n=1052) | 14.0 / 17.3 (n=415) | 9.9 / 44.1 (n=19) | 31.6 / 58.9 (n=1247) |

## Kiểm tra tính hợp lệ

- Chain không thành công nhưng chạy chưa đủ 12 vòng: 0
- Prompt sửa dùng: repair.system ['team-v1:cf5e5868c02d']; repair.user.level1 ['team-v1:f085fe3b62aa']; repair.user.level2 ['team-v1:8150e6a250a7']; repair.user.level3 ['team-v1:b45dc39ad007']
