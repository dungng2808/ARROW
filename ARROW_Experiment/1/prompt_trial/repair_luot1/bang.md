| Mô hình sửa | Cơ chế | Bản prompt | Chain | VALID | Tỷ lệ | Vòng thành công TB | Token vào TB mỗi vòng | Lỗi hạ tầng |
|---|---|---|---|---|---|---|---|---|
| gpt6luna | fixed_plus | cu | 22 | 16 | 73% | 1.81 | 3477 | 0 |
| gpt6luna | fixed_plus | moi | 22 | 16 | 73% | 1.75 | 4149 | 0 |
| gpt6luna | adaptive | cu | 22 | 15 | 68% | 1.87 | 4122 | 0 |
| gpt6luna | adaptive | moi | 22 | 17 | 77% | 2.00 | 4647 | 0 |
| qwen3coder-or | fixed_plus | cu | 22 | 10 | 45% | 1.60 | 3556 | 0 |
| qwen3coder-or | fixed_plus | moi | 22 | 10 | 45% | 1.50 | 4257 | 0 |
| qwen3coder-or | adaptive | cu | 22 | 12 | 55% | 1.67 | 3926 | 0 |
| qwen3coder-or | adaptive | moi | 22 | 9 | 41% | 1.22 | 4593 | 0 |

| Mô hình sửa | Cặp | Chỉ moi VALID | Chỉ cu VALID | Cả hai | Cả hai không |
|---|---|---|---|---|---|
| gpt6luna | 44 | 5 | 3 | 28 | 8 |
| qwen3coder-or | 44 | 3 | 6 | 16 | 19 |
| (cả hai) | 88 | 8 | 9 | 44 | 27 |
