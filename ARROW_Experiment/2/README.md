# Thực nghiệm 2 (RQ1b, RQ2): cơ chế sửa

Thiết kế: `Sua/ban_phan_bien/cac_thuc_nghiem.md`, mục "Thực nghiệm 2". Hướng dẫn chạy cho từng máy: [huong_dan_chay_TN2.md](huong_dan_chay_TN2.md).

## Đã chốt trước khi chạy (22/11/2026)

| Mục | Giá trị |
|---|---|
| Tập con | 179 candidate (Classes2Test 90, CAT-LM 47, GitBug-Java 42); 15 / 8 / 7 mỗi ô mô hình sinh × chiến lược; ô CAT-LM · GPT · Zero-shot-PA chỉ có 7 (`shortfall_by_cell`) |
| Cách chọn | trong mỗi ô xếp theo `sha256("arrow-tn2-subset-v1\|<dataset>\|<task_id>\|<model>\|<strategy>")`, lấy các candidate đầu |
| Mô hình sửa | cả hai (GPT-6 Luna, Qwen3-Coder) cho mọi candidate: chain cùng mô hình và sửa chéo ngang nhau |
| Nhánh | F+, F−, F2, A |
| N | 10 (pilot, quy tắc B ngưỡng 1%); không dừng sớm |
| Lặp | 3 lần trên cùng c0 đã đóng băng; **lần 1 là lần đo chính** (mọi kiểm định), lần 2 và 3 chỉ để báo độ biến thiên (trung bình, min–max của RSR từng nhánh) |
| Kiểm lại REGRESSION | có sẵn trong tool (chạy riêng 3 lần, rồi chạy lại module), giống TN1 và pilot |
| Tool, prompt | `arrow` `203fb8e`, prompt sửa `team-v1` v3 (giống pilot) |
| Chia máy | theo tốc độ đo ở pilot: Dung 82, Chính 33, Kiều Anh 30, Quang 17, Hán 17 candidate |

## Các file

| File | Ai chạy | Làm gì |
|---|---|---|
| `tn2_subset.py` | Dung, **một lần** | Chọn tập con, chia máy, ghi `tn2_subset.jsonl` (+ `.sha256`, `tn2_subset.md`). `--check` để kiểm lại |
| `tn2_subset.jsonl` | — | Danh sách đóng băng: candidate, máy, c0 (đường dẫn + sha256), thứ hạng trong ô |
| `tn2_run.py` | mỗi máy | Chạy 24 chain cho mỗi candidate của máy mình → `results/<máy>/tn2_runs.jsonl` |
| `tn2_collect.py` | mỗi máy, trước khi nộp | Gom từ homes/ ra `results/<máy>/report/` (chain, từng vòng gồm cả quay lui/checkpoint, mã cuối) và kiểm ĐẠT |
| `tn2_analyze.py`, `tn2_glmer.R` | Dung, sau khi đủ máy | (sẽ viết) Gộp và phân tích |
