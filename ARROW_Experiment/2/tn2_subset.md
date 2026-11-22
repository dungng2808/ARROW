# TN2: tập con đóng băng

- Chuỗi xếp: `sha256("arrow-tn2-subset-v1|<dataset>|<task_id>|<model>|<strategy>")`
- Số lấy mỗi ô: Classes2Test 15, CAT-LM 8, GitBug-Java 7
- Tổng: **179** candidate × 2 mô hình sửa × 4 nhánh × 3 lần = **4296** chain
- shortfall_by_cell: `{"catlm|gpt6luna|zero_shot_project_aware": 1}`

## Số lấy mỗi ô

| Dataset | Mô hình sinh | Chiến lược | Có | Cần | Lấy |
|---|---|---|---|---|---|
| classes2test | gpt6luna | zero_shot | 117 | 15 | 15 |
| classes2test | gpt6luna | few_shot | 118 | 15 | 15 |
| classes2test | gpt6luna | zero_shot_project_aware | 96 | 15 | 15 |
| classes2test | qwen3coder-or | zero_shot | 165 | 15 | 15 |
| classes2test | qwen3coder-or | few_shot | 157 | 15 | 15 |
| classes2test | qwen3coder-or | zero_shot_project_aware | 157 | 15 | 15 |
| catlm | gpt6luna | zero_shot | 22 | 8 | 8 |
| catlm | gpt6luna | few_shot | 23 | 8 | 8 |
| catlm | gpt6luna | zero_shot_project_aware | 7 | 8 | 7 |
| catlm | qwen3coder-or | zero_shot | 30 | 8 | 8 |
| catlm | qwen3coder-or | few_shot | 26 | 8 | 8 |
| catlm | qwen3coder-or | zero_shot_project_aware | 22 | 8 | 8 |
| gitbug-java | gpt6luna | zero_shot | 28 | 7 | 7 |
| gitbug-java | gpt6luna | few_shot | 27 | 7 | 7 |
| gitbug-java | gpt6luna | zero_shot_project_aware | 26 | 7 | 7 |
| gitbug-java | qwen3coder-or | zero_shot | 28 | 7 | 7 |
| gitbug-java | qwen3coder-or | few_shot | 28 | 7 | 7 |
| gitbug-java | qwen3coder-or | zero_shot_project_aware | 27 | 7 | 7 |

## Chia máy

| Máy | Candidate | Chain | Classes2Test | CAT-LM | GitBug-Java |
|---|---|---|---|---|---|
| dung | 82 | 1968 | 41 | 22 | 19 |
| chinh | 33 | 792 | 17 | 8 | 8 |
| kieuanh | 30 | 720 | 15 | 8 | 7 |
| quang | 17 | 408 | 9 | 4 | 4 |
| han | 17 | 408 | 8 | 5 | 4 |
