# Pilot chọn N

Pilot **chỉ dùng để chọn N**, số vòng sửa tối đa cho Thực nghiệm 2. Không so sánh các cơ chế sửa, và không gộp số liệu pilot vào kết quả chính.

## Thiết kế

| Thành phần | Giá trị |
|---|---|
| Lớp | 50 lớp `classes2test_pilot` (dataset-v1), không trùng repo với tập đánh giá |
| Sinh c0 | 3 cách viết prompt × 2 mô hình, không sửa; cùng script và bản tool với TN1 (`d0f2b5c`) |
| Sửa | Mọi c0 chưa VALID × 4 cơ chế (F+ `fixed_plus`, F− `fixed_minus`, F2 `fixed_l2`, A `adaptive`) |
| Mô hình sửa | Trùng mô hình đã sinh c0 (pilot không sửa chéo) |
| Số vòng | 12, không dừng sớm (`repair.early_stop_p = null`); bản tool `203fb8e` |
| Quy tắc chọn N | Quy tắc B, ngưỡng 1% (chốt 18/11/2026), xem `pilot_analyze.py` |

Quy tắc B: S(k) là số chain sửa thành công ở vòng k hoặc sớm hơn, gộp 4 cơ chế và 2 mô hình. N là vòng k nhỏ nhất trong khoảng 5–10 mà ở cả vòng k − 1 và vòng k, số chain sửa thêm đều dưới 1% của S(12). Nếu không có vòng nào thỏa thì N = 10.

Hai loại chain bị loại khỏi mẫu số:
- chain lỗi hạ tầng;
- chain có c0 đã VALID khi kiểm lại (`success_round = 0`).

## Chạy

```bash
# 1. Sinh c0 (arrow bản d0f2b5c trong PATH)
python3 pilot_run.py gen    --machine dung --models gpt6luna,qwen3coder-or --workers 4

# 2. Sửa bằng 4 cơ chế (arrow bản 203fb8e)
python3 pilot_run.py repair --machine dung --models gpt6luna,qwen3coder-or --workers 4 \
    --arrow ~/.local/share/arrow-dev/build-tn2/arrow

# 3. Tính N
python3 pilot_analyze.py
```

- **Chạy khói:** thêm `--limit 1` vào bước 1 và bước 2. Kết quả ghi vào `results/<máy>-smoke/`; xem bằng `python3 pilot_analyze.py --dir results/<máy>-smoke`.
- **Bị ngắt:** chạy lại đúng lệnh cũ; script bỏ qua phần đã xong.
- **Lỗi hạ tầng:** chạy lại với `--retry-infra`.
- **Đổi số worker khi đang chạy:** ghi số mới vào `results/<máy>/repair/workers.txt`.

## Đầu ra

| File | Nội dung |
|---|---|
| `results/<máy>/gen/` | Kết quả sinh c0 theo dạng TN1 (`tn1_runs.jsonl`, `tn1_candidates.csv`), cộng `tn2_pool.jsonl` và `c0/` là các c0 cần sửa |
| `results/<máy>/repair/pilot_runs.jsonl` | Mỗi chain một dòng. Script tự kiểm cơ chế, số vòng và dừng sớm theo `run.json`; nếu sai thì ghi `BAD_CONFIG` |
| `results/<máy>/repair/pilot.lock.json` | Bản tool, mô hình, sha256 của bộ prompt và dataset trong từng lần gọi |
| `reports/pilot_n.md` | N, bảng S(k), tỷ lệ mất ℓ kèm cận trên Clopper–Pearson, kiểm công bằng 5% theo cơ chế và mô hình, độ nhạy theo ngưỡng, thời gian một vòng sửa |
| `reports/pilot_n.csv`, `pilot_chains.csv`, `pilot_rounds.csv` | Số liệu chi tiết |

## Trước khi chạy pilot thật

1. Ghi quy tắc B và ngưỡng 1% vào RP2 Mục 3, rồi commit trước lần gọi mô hình đầu tiên.
2. ✅ Prompt sửa đã chốt 21/11/2026: bản `team-v1` (v3) trong `../1/prompts/override/repair/`, nạp qua `prompts.override_dir`; sha256 và kết quả thử ở `../1/prompt_trial/README.md`. Script ghi sha256 của mọi file prompt vào `pilot.lock.json` và dừng nếu prompt đổi giữa chừng.
3. Chạy khói xong mới chạy thật.
