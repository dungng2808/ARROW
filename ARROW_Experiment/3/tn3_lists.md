# TN3: danh sách đóng băng

Sinh bởi `tn3_lists.py` (tn3-v2), 2026-11-25T19:59:00+00:00.

## Chia máy

| Máy | Lớp | Classes2Test | CAT-LM | GitBug-Java | EvoSuite chạy được (JDK ≤ 11) | Maven |
|---|---:|---:|---:|---:|---:|---:|
| dung | 107 | 80 | 14 | 13 | 100 | 102 |
| chinh | 38 | 28 | 6 | 4 | 32 | 34 |
| kieuanh | 41 | 31 | 5 | 5 | 39 | 38 |
| quang | 41 | 31 | 5 | 5 | 38 | 39 |
| han | 41 | 30 | 6 | 5 | 37 | 39 |

## Bộ ARROW (Zero-shot-project-aware của TN1)

| Dataset | Mô hình | measure | repair | invalid_output | missing_tn1 |
|---|---|---:|---:|---:|---:|
| classes2test | gpt6luna | 104 | 96 | 0 | 0 |
| classes2test | qwen3coder-or | 39 | 157 | 4 | 0 |
| catlm | gpt6luna | 29 | 7 | 0 | 0 |
| catlm | qwen3coder-or | 14 | 22 | 0 | 0 |
| gitbug-java | gpt6luna | 6 | 26 | 0 | 0 |
| gitbug-java | qwen3coder-or | 3 | 27 | 2 | 0 |

## JDK × framework × build tool (JDK ARROW dùng ở TN1)

| Dataset | Build | JDK | JUnit 4 | JUnit 5 + vintage | JUnit 5 không vintage | TestNG | EvoSuite chạy được |
|---|---|---|---:|---:|---:|---:|---:|
| classes2test | maven | 8 | 131 | 5 | 17 | 6 | 159 |
| classes2test | maven | 11 | 17 | 0 | 3 | 2 | 22 |
| classes2test | maven | 17 | 8 | 0 | 2 | 0 | 0 |
| classes2test | gradle | 8 | 6 | 0 | 1 | 0 | 7 |
| classes2test | gradle | 17 | 1 | 0 | 1 | 0 | 0 |
| catlm | maven | 8 | 14 | 1 | 15 | 0 | 30 |
| catlm | maven | 11 | 0 | 1 | 2 | 0 | 3 |
| catlm | maven | 17 | 2 | 0 | 1 | 0 | 0 |
| gitbug-java | maven | 8 | 9 | 0 | 3 | 0 | 12 |
| gitbug-java | maven | 11 | 1 | 0 | 6 | 0 | 7 |
| gitbug-java | maven | 17 | 1 | 0 | 5 | 0 | 0 |
| gitbug-java | gradle | 8 | 2 | 0 | 2 | 0 | 4 |
| gitbug-java | gradle | 11 | 2 | 0 | 0 | 0 | 2 |
| gitbug-java | gradle | 17 | 0 | 0 | 1 | 0 | 0 |

| Dataset | Lớp | EvoSuite chạy được | Maven (ChatUniTest) |
|---|---:|---:|---:|
| classes2test | 200 | 188 | 191 |
| catlm | 36 | 33 | 36 |
| gitbug-java | 32 | 25 | 25 |
