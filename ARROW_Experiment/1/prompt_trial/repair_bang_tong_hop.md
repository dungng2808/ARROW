# Thử prompt sửa test: bảng tổng hợp 2 lượt (21/11/2026)

22 c0 chưa VALID (lượt 3 của prompt_trial, GPT sinh) trên 8 lớp ngoài dataset-v1; tối đa 5 vòng, không dừng sớm. Chỉ để chọn prompt, không phải kết quả thực nghiệm.

## 1. Tỷ lệ sửa thành công

| Mô hình sửa | Cơ chế | Bản prompt | VALID / 22 | Tỷ lệ | Wilson 95% | Vòng thành công TB | Vòng trả mã trùng | Token vào TB/vòng | Token ra TB/vòng |
|---|---|---|---|---|---|---|---|---|---|
| GPT-6 Luna | F+ | Cũ (L1) | 16 | 73% | 52–87% | 1.81 | 5/59 (8%) | 3477 | 773 |
| GPT-6 Luna | F+ | Mới v1 (L1) | 16 | 73% | 52–87% | 1.75 | 8/58 (14%) | 4149 | 735 |
| GPT-6 Luna | F+ | Mới v2 (L2) | 18 | 82% | 61–93% | 1.94 | 4/55 (7%) | 4156 | 770 |
| GPT-6 Luna | A | Cũ (L1) | 15 | 68% | 47–84% | 1.87 | 5/63 (8%) | 4122 | 794 |
| GPT-6 Luna | A | Mới v1 (L1) | 17 | 77% | 57–90% | 2.00 | 12/59 (20%) | 4647 | 773 |
| GPT-6 Luna | A | Mới v2 (L2) | 19 | 86% | 67–95% | 2.00 | 5/53 (9%) | 4586 | 764 |
| Qwen3-Coder | F+ | Cũ (L1) | 10 | 45% | 27–65% | 1.60 | 31/76 (41%) | 3556 | 1058 |
| Qwen3-Coder | F+ | Cũ chạy lại (L2) | 12 | 55% | 35–73% | 2.08 | 26/75 (35%) | 3550 | 1105 |
| Qwen3-Coder | F+ | Mới v1 (L1) | 10 | 45% | 27–65% | 1.50 | 30/75 (40%) | 4257 | 817 |
| Qwen3-Coder | F+ | Mới v2 (L2) | 10 | 45% | 27–65% | 1.50 | 34/75 (45%) | 4264 | 818 |
| Qwen3-Coder | A | Cũ (L1) | 12 | 55% | 35–73% | 1.67 | 24/70 (34%) | 3926 | 974 |
| Qwen3-Coder | A | Cũ chạy lại (L2) | 11 | 50% | 31–69% | 1.73 | 28/74 (38%) | 3983 | 1065 |
| Qwen3-Coder | A | Mới v1 (L1) | 9 | 41% | 23–61% | 1.22 | 38/76 (50%) | 4593 | 801 |
| Qwen3-Coder | A | Mới v2 (L2) | 8 | 36% | 20–57% | 1.50 | 41/82 (50%) | 4673 | 816 |

## 2. Gộp hai cơ chế (44 chain mỗi ô)

| Mô hình sửa | Cũ (L1) | Cũ chạy lại (L2) | Mới v1 (L1) | Mới v2 (L2) |
|---|---|---|---|---|
| GPT-6 Luna | 31/44 (70%) | — | 33/44 (75%) | 37/44 (84%) |
| Qwen3-Coder | 22/44 (50%) | 23/44 (52%) | 19/44 (43%) | 18/44 (41%) |

## 3. So theo cặp (cùng c0, cùng mô hình, cùng cơ chế)

| So sánh | Mô hình | Cặp | Chỉ vế trái VALID | Chỉ vế phải VALID | p (McNemar chính xác) |
|---|---|---|---|---|---|
| Cũ chạy lại (L2) vs Cũ (L1) (mức nhiễu khi chạy lại) | Qwen3-Coder | 44 | 3 | 2 | 1.00 |
| Mới v1 (L1) vs Cũ (L1) | GPT-6 Luna | 44 | 5 | 3 | 0.73 |
| Mới v1 (L1) vs Cũ (L1) | Qwen3-Coder | 44 | 3 | 6 | 0.51 |
| Mới v2 (L2) vs Cũ (L1) | GPT-6 Luna | 44 | 7 | 1 | 0.07 |
| Mới v2 (L2) vs Cũ (L1) | Qwen3-Coder | 44 | 3 | 7 | 0.34 |
| Mới v2 (L2) vs Cũ chạy lại (L2) | Qwen3-Coder | 44 | 2 | 7 | 0.18 |
| Mới v2 (L2) vs Mới v1 (L1) | GPT-6 Luna | 44 | 5 | 1 | 0.22 |
| Mới v2 (L2) vs Mới v1 (L1) | Qwen3-Coder | 44 | 1 | 2 | 1.00 |

## 4. Theo trạng thái c0 (gộp hai cơ chế; VALID/chain)

| Trạng thái c0 | Mô hình | Cũ (L1) | Cũ chạy lại (L2) | Mới v1 (L1) | Mới v2 (L2) |
|---|---|---|---|---|---|
| COMPILE | GPT-6 Luna | 12/18 | — | 13/18 | 17/18 |
| COMPILE | Qwen3-Coder | 8/18 | 10/18 | 7/18 | 7/18 |
| RUNTIME | GPT-6 Luna | 7/12 | — | 7/12 | 7/12 |
| RUNTIME | Qwen3-Coder | 3/12 | 2/12 | 2/12 | 2/12 |
| ASSERTION | GPT-6 Luna | 12/14 | — | 13/14 | 13/14 |
| ASSERTION | Qwen3-Coder | 11/14 | 11/14 | 10/14 | 9/14 |

## 5. Theo lớp (gộp hai cơ chế; VALID/chain)

| Lớp | Mô hình | Cũ (L1) | Cũ chạy lại (L2) | Mới v1 (L1) | Mới v2 (L2) |
|---|---|---|---|---|---|
| ANNIndex | GPT-6 Luna | 4/6 | — | 4/6 | 5/6 |
| ANNIndex | Qwen3-Coder | 2/6 | 2/6 | 3/6 | 2/6 |
| CompositeParser | GPT-6 Luna | 0/4 | — | 2/4 | 4/4 |
| CompositeParser | Qwen3-Coder | 0/4 | 0/4 | 0/4 | 0/4 |
| CryptoUtil | GPT-6 Luna | 6/6 | — | 6/6 | 6/6 |
| CryptoUtil | Qwen3-Coder | 5/6 | 6/6 | 0/6 | 1/6 |
| DataExpressionSource | GPT-6 Luna | 1/6 | — | 1/6 | 2/6 |
| DataExpressionSource | Qwen3-Coder | 0/6 | 0/6 | 0/6 | 0/6 |
| DeweyVersion | GPT-6 Luna | 2/4 | — | 2/4 | 2/4 |
| DeweyVersion | Qwen3-Coder | 1/4 | 0/4 | 0/4 | 0/4 |
| PathUtils | GPT-6 Luna | 6/6 | — | 6/6 | 6/6 |
| PathUtils | Qwen3-Coder | 2/6 | 3/6 | 4/6 | 4/6 |
| ViterbiAlgorithm | GPT-6 Luna | 6/6 | — | 6/6 | 6/6 |
| ViterbiAlgorithm | Qwen3-Coder | 6/6 | 6/6 | 6/6 | 6/6 |
| WithJsonPath | GPT-6 Luna | 6/6 | — | 6/6 | 6/6 |
| WithJsonPath | Qwen3-Coder | 6/6 | 6/6 | 6/6 | 5/6 |

Lỗi hạ tầng: 0/308 chain.
