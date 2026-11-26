# Hướng dẫn chạy TN3 trên từng máy

Đặc tả dễ hiểu: [dac_ta_TN3.md](dac_ta_TN3.md). **Không chạy TN3 cùng lúc với TN2 trên cùng máy** (EvoSuite tốn CPU,
TN2 đo thời gian).

| Máy | `<máy>` | Số lớp |
|---|---|---:|
| Dung | `dung` | 107 |
| Chính | `chinh` | 38 |
| Kiều Anh | `kieuanh` | 41 |
| Quang | `quang` | 41 |
| Hán | `han` | 41 |

## 1. Chuẩn bị (một lần)

1. `git pull` repo ARROW. Kiểm danh sách: `cd ARROW_Experiment/3 && python3 tn3_lists.py --check` (phải ĐÚNG cả hai file).
2. Dùng **arrow bản 203fb8e** như TN2 (macOS: `~/.local/share/arrow-dev/build-tn2/arrow`; Windows: `C:\tools\arrow-203\arrow.exe`).
   Dưới đây gọi là `<arrow>`.
3. ARROW_HOME gốc (`~/.arrow`) như TN2:
   - hai mô hình `gpt6luna`, `qwen3coder-or` (Qwen ghim Amazon Bedrock);
   - `prompts.override_dir` trỏ tới `ARROW_Experiment/1/prompts/override`;
   - JDK 8, 11, 17 khai trong `java.jdk_homes`.

   Kiểm: `<arrow> models test gpt6luna` và `<arrow> models test qwen3coder-or` đều OK.
4. **Maven ≥ 3.9** (`mvn` trên PATH; kiểm `mvn -v`), `~/.m2/settings.xml` và `~/.gradle/init.d/google-mirror.gradle` như mọi
   thực nghiệm. Plugin ChatUniTest chạy bằng Maven của máy (wrapper `mvnw` cũ của dự án làm plugin lỗi).
5. Lần đầu, `tn3_evosuite.py` tự tải EvoSuite 1.2.0 (kiểm sha256) và cài runtime, JUnit 4.12 vào `~/.m2`.

## 2. Thứ tự chạy

```bash
cd ARROW_Experiment/3

# (a) Chạy khói từng nhánh (kết quả ở results/<máy>-smoke/, không dùng trong báo cáo)
python3 tn3_arrow.py       --machine <máy> --arrow <arrow> --limit 1
python3 tn3_evosuite.py    --machine <máy> --arrow <arrow> --limit 1 --seeds 1
python3 tn3_chatunitest.py --machine <máy> --arrow <arrow> --smoke          # 2 lớp Maven × 2 mô hình

# (b) Chạy thật: (b1) và (b2) chạy cùng lúc được
python3 tn3_arrow.py    --machine <máy> --workers 3 --arrow <arrow>          # (b1) ARROW, cả hai mô hình
python3 tn3_evosuite.py --machine <máy> --workers 3 --arrow <arrow>          # (b2) EvoSuite, seed 1–5

# (c) Sau khi (b1) xong: EvoSuite cấu hình cùng thời gian với ARROW
python3 tn3_evosuite.py --machine <máy> --workers 3 --arrow <arrow> --budget

# (d) ChatUniTest (phần Qwen chạy sau khi đã nạp tiền OpenRouter)
python3 tn3_chatunitest.py --machine <máy> --workers 2 --arrow <arrow>

# (e) Cuối cùng: phát hiện lỗi thật (chỉ lớp GitBug-Java của máy)
python3 tn3_phat_hien_loi.py --machine <máy> --workers 2 --arrow <arrow>
```

- **Bị ngắt / tắt máy:** chạy lại đúng lệnh cũ; phần đã xong được bỏ qua. Lỗi hạ tầng: thêm `--retry-infra`.
- **Hết tiền hoặc mô hình lỗi:** script dừng cả lô, ghi `results/<máy>/DA_DUNG.txt` kèm lệnh chạy lại; sửa xong chạy lại đúng lệnh.
- **Một nhà cung cấp đang lỗi:** `tn3_arrow.py --model gpt6luna` (hoặc `qwen3coder-or`) để chạy riêng mô hình còn dùng
  được; sau đó chạy lại không có `--model`.
- **Windows:** home và thư mục làm việc tự đặt ở `C:\arrow-homes\tn3-<máy>`, `C:\arrow-work\tn3-<máy>` (đường dẫn ngắn).

## 2b. Chạy lại đúng một việc và xóa sạch lần chạy cũ (`--redo`)

Dùng khi một kết quả sai (vd. sửa lỗi script giữa chừng). `--redo` **xóa sạch mọi dấu vết của lần chạy cũ** của đúng
việc đó (dòng trong `*_runs.jsonl`, log trong `logs/`, bộ test trong `suites/`, thư mục run cũ của arrow trong `homes/`;
ChatUniTest: cả các lượt gọi mô hình cũ trong `proxy_calls.jsonl`), rồi chạy lại và ghi kết quả mới. Không giữ bản cũ nào:

```bash
python3 tn3_arrow.py         --machine <máy> --arrow <arrow> --redo <task_id> [--model qwen3coder-or]
python3 tn3_evosuite.py      --machine <máy> --arrow <arrow> --redo <task_id> [--seeds 3]   [--budget]
python3 tn3_chatunitest.py   --machine <máy> --arrow <arrow> --redo <task_id> [--model gpt6luna]
python3 tn3_phat_hien_loi.py --machine <máy> --arrow <arrow> --redo <task_id> [--system evosuite]
```

- Không ghi `--model` / `--seeds` / `--system` thì chạy lại **mọi** mô hình / seed / hệ thống của lớp đó.
- `--redo` lặp lại được: `--redo A --redo B`. Bộ test và log mới ghi đè file cùng tên.
- Chạy lại kết quả ARROW của một lớp thì nên chạy lại cả EvoSuite `--budget` của lớp đó (ngân sách lấy theo thời gian ARROW).
- Log dùng chung của cả lớp (`inspect`, `build_orig.log`, `apply_*.log`) không xóa; lần chạy lại ghi đè.
- `tn3.lock.json` vẫn ghi lại lệnh `--redo` đã chạy (giờ, nguyên dòng lệnh).

## 3. Nộp kết quả

Commit thư mục `results/<máy>/` (trừ `homes/`, `work/`, đã có trong `.gitignore`):
`arrow_runs.jsonl`, `evosuite_runs.jsonl`, `chatunitest_runs.jsonl`, `proxy_calls.jsonl`, `phat_hien_loi.jsonl`,
`tn3.lock.json`, `suites/`, `logs/`. Báo Dung khi xong từng bước.

## 4. Gom và phân tích (Dung, sau khi 5 máy đã commit kết quả)

```bash
git pull
python3 tn3_collect.py      # gom 5 máy → results/_all/ (arrow.csv, evosuite.csv, chatunitest.csv, phat_hien_loi.csv,
                            #   lop.csv mỗi lớp một dòng, kiem_tra.md: thiếu gì, lỗi hạ tầng, lệch tool, cờ đặc biệt)
python3 tn3_analyze.py      # → results/_all/bao_cao/rq3.md + b1…b12 (.csv): các bảng RQ3
```

- Xem trước khi chưa đủ 5 máy: `python3 tn3_collect.py --allow-missing`.
- `kiem_tra.md` còn dòng thiếu hoặc INFRA thì báo máy đó chạy lại (`--retry-infra`) trước khi dùng số liệu.
