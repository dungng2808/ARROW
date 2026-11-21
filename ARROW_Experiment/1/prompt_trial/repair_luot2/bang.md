| Mô hình sửa | Cơ chế | Bản prompt | Chain | VALID | Tỷ lệ | Vòng thành công TB | Token vào TB mỗi vòng | Lỗi hạ tầng |
|---|---|---|---|---|---|---|---|---|
| gpt6luna | fixed_plus | moi2 | 22 | 18 | 82% | 1.94 | 4156 | 0 |
| gpt6luna | adaptive | moi2 | 22 | 19 | 86% | 2.00 | 4586 | 0 |
| qwen3coder-or | fixed_plus | cu | 22 | 12 | 55% | 2.08 | 3550 | 0 |
| qwen3coder-or | fixed_plus | moi2 | 22 | 10 | 45% | 1.50 | 4264 | 0 |
| qwen3coder-or | adaptive | cu | 22 | 11 | 50% | 1.73 | 3983 | 0 |
| qwen3coder-or | adaptive | moi2 | 22 | 8 | 36% | 1.50 | 4673 | 0 |

| Mô hình sửa | Cặp | Chỉ moi2 VALID | Chỉ cu VALID | Cả hai | Cả hai không |
|---|---|---|---|---|---|
| qwen3coder-or | 44 | 2 | 7 | 16 | 19 |
| (cả hai) | 44 | 2 | 7 | 16 | 19 |
