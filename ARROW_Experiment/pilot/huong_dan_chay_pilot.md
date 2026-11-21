# Pilot chọn N — hướng dẫn chạy (dành cho agent)

> **Người dùng:** đưa file này cho agent và nói: *"Làm theo file này cho máy của tôi, tôi là <Quang | Hán | Chính>."* Máy phải là máy **đã chạy TN1 Qwen** (đã có `arrow` bản `d0f2b5c`, JDK, cấu hình và khóa OpenRouter). Agent sẽ cần bạn: **tự nhập khóa TeamoRouter** (Dung gửi riêng) bằng `arrow secrets set teamo`, và **đăng nhập GitHub** nếu máy chưa có.
>
> **Agent:** bạn đang chạy trong repo `ARROW` người dùng mở sẵn. Mọi đường dẫn tính từ thư mục gốc đó. Làm lần lượt Bước 1–7, kiểm "Kết quả mong đợi" sau mỗi bước. Gặp tình huống không có trong file thì **dừng và báo người dùng**.

## Mục đích

Pilot chỉ để chọn **N** (số vòng sửa tối đa của TN2). Mỗi máy:
1. **gen**: sinh test (c0) cho phần lớp pilot của mình bằng 3 cách viết prompt × **2 mô hình (GPT-6 Luna và Qwen3-Coder)**, không sửa (giống TN1, cùng bản `arrow` `d0f2b5c`);
2. **repair**: mỗi c0 chưa VALID được sửa bằng 4 cơ chế (F+, F−, F2, A), **bằng chính mô hình đã sinh ra nó**, tối đa 12 vòng, bằng `arrow` bản `203fb8e`.

Dung gộp kết quả mọi máy để tính N.

## Quy tắc bắt buộc

1. **Không đọc, in, ghi hay gõ khóa API.**
2. **Không sửa** file nào trong repo (`pilot/*.py`, `1/*.py`, `1/prompts/`, `ARROW_dataset/`). Script lỗi thì dừng và báo.
3. Không đổi cấu hình mô hình do `setup_tn1.py` đặt.
4. Chỉ chạy **đúng `--machine` và `--shard`** của người dùng trong bảng dưới.
5. Không xóa `ARROW_Experiment/pilot/results/<M>/` trước khi nộp xong. **Không xóa thư mục homes** (`C:\arrow-homes\pilot-<M>-...`) cho tới khi **Dung xác nhận đã gộp xong**: số liệu từng vòng sửa chỉ nằm trong đó.
6. Khi nộp: **chỉ commit `ARROW_Experiment/pilot/results/<M>/`**, push thẳng lên `main`, **không bao giờ** `--force`.

## Phân công

`--shard` của pilot chia **đều** 50 lớp (xếp theo sha256, 13/13/12/12), khác cách chia của TN1.

| Người | `<M>` | `<S>` | Số lớp pilot | Số dòng gen mong đợi (lớp × 3 prompt × 2 mô hình) | Thư mục homes |
|---|---|---|---|---|---|
| Quang | `quang` | `1/4` | 13 | 78 | (mặc định, ổ C) |
| Hán | `han` | `2/4` | 13 | 78 | (mặc định, ổ C) |
| Chính | `chinh` | `3/4` | 12 | 72 | `D:\arrow-homes\pilot-chinh` |
| Dung (macOS) | `dung` | `4/4` | 12 | 72 | (mặc định) |

Với Chính, thêm `--homes-dir D:\arrow-homes\pilot-chinh` vào **mọi** lệnh `pilot_run.py` bên dưới. Người khác không thêm.

---

## Bước 1. Cập nhật repo

```powershell
git switch main
git pull origin main
```
**Kết quả mong đợi:** có thư mục `ARROW_Experiment/pilot/` với `pilot_run.py`, và `ARROW_Experiment/1/prompts/override/repair/`.

## Bước 2. Kiểm cấu hình TN1 và thêm GPT

```powershell
arrow --version                       # phải có: commit d0f2b5c
cd ARROW_Experiment\1
python setup_tn1.py --gpt
cd ..\..
```
`--gpt` thêm mô hình `gpt6luna` (GPT-6 Luna qua TeamoRouter, temperature 0, seed 42, reasoning_effort none: đúng cấu hình TN1 trên máy Dung). **Không** tự đặt cấu hình GPT bằng tay.

**Kết quả mong đợi:** toàn `[OK]`, test được cả `qwen3coder-or` lẫn `gpt6luna`, kết thúc bằng `Máy đã sẵn sàng`.
- `Chưa có khóa TeamoRouter` → nhờ người dùng **tự** chạy `arrow secrets set teamo` (khóa Dung gửi riêng), rồi chạy lại lệnh trên.
- `Chưa có khóa OpenRouter` → người dùng tự chạy `arrow secrets set openrouter`, rồi chạy lại.
- Lỗi khác → làm lại các bước tương ứng trong `ARROW_Experiment/1/huong_dan_chay_TN1.md` (Bước 0–5), rồi quay lại đây.

## Bước 3. Cài thêm `arrow` bản `203fb8e` (cho bước repair)

Bản này đặt ở thư mục **riêng** `C:\tools\arrow-203`, **không** thêm vào PATH (lệnh `arrow` trong PATH vẫn phải là bản `d0f2b5c`). Tải từ GitHub Release `arrow-203fb8e` của repo:
```powershell
$zip = "$env:TEMP\arrow-0.2.0-203fb8e-win-x64.zip"
curl.exe -L -o $zip https://github.com/dungng2808/ARROW/releases/download/arrow-203fb8e/arrow-0.2.0-203fb8e-win-x64.zip
(Get-FileHash $zip -Algorithm SHA256).Hash   # phải là 4B040A523D56A4C1FFFE7715898A403E49018A43070EBC5D63E378D350A1BDC7
New-Item -ItemType Directory -Force C:\tools\arrow-203 | Out-Null
Expand-Archive $zip -DestinationPath C:\tools\arrow-203 -Force
C:\tools\arrow-203\arrow.exe --version
```
Không tải được (mạng chặn GitHub) → hỏi người dùng file zip Dung gửi, rồi làm từ dòng `Get-FileHash` với đường dẫn đó.
**Kết quả mong đợi:** dòng cuối có `commit 203fb8e`. Hash sai → dừng, báo người dùng.

Không có zip: build như Cách B ở Bước 3 của `huong_dan_chay_TN1.md`, nhưng dùng commit `203fb8e` và thư mục ra `C:\tools\arrow-203`; **không** thêm vào PATH.

## Bước 4. Chạy khói (khoảng 5–10 phút)

Chạy trong `ARROW_Experiment\pilot`:
```powershell
cd ARROW_Experiment\pilot
python pilot_run.py gen    --machine <M> --models gpt6luna,qwen3coder-or --shard <S> --workers 2 --limit 2
python pilot_run.py repair --machine <M> --models gpt6luna,qwen3coder-or --workers 2 --limit 1 --arrow C:\tools\arrow-203\arrow.exe
```
**Kết quả mong đợi:**
- Lệnh gen: 12 dòng `DONE` (2 lớp × 3 prompt × 2 mô hình), rồi dòng `gen: 12 bộ test ... ; <k> bộ vào tn2_pool.jsonl`.
- Nếu `k = 0` (cả 2 lớp đều VALID, không có gì để sửa): chạy lại lệnh gen với `--limit 4`, rồi chạy lệnh repair.
- Lệnh repair: 4 dòng `DONE` (1 c0 × 4 cơ chế), **không** có `BAD_CONFIG`, `INFRA` hay `Traceback`.

Kết quả chạy khói nằm ở `results\<M>-smoke\`, **không nộp**. Lỗi → dừng, gửi Dung phần in ra.

## Bước 5. Chạy thật

Chạy nền, lần lượt **gen rồi repair** (repair cần kết quả gen). Windows:
```powershell
New-Item -ItemType Directory -Force results\<M> | Out-Null
Start-Process python -ArgumentList "-u pilot_run.py gen --machine <M> --models gpt6luna,qwen3coder-or --shard <S> --workers 4" -RedirectStandardOutput results\<M>\gen.log -RedirectStandardError results\<M>\gen.err -WindowStyle Hidden -Wait
Test-Path results\<M>\gen\DA_DUNG.txt      # True → bước gen đã dừng: làm theo mục "Hết tiền / sai khóa" bên dưới, CHƯA chạy repair
Start-Process python -ArgumentList "-u pilot_run.py repair --machine <M> --models gpt6luna,qwen3coder-or --workers 4 --arrow C:\tools\arrow-203\arrow.exe" -RedirectStandardOutput results\<M>\repair.log -RedirectStandardError results\<M>\repair.err -WindowStyle Hidden
```
macOS (Dung): như trên với `caffeinate -i python3 -u pilot_run.py ...` và `--arrow ~/.local/share/arrow-dev/build-tn2/arrow`.

**Thời gian:** gen khoảng 30–60 phút; repair khoảng 5–8 giờ (chain chưa sửa được chạy đủ 12 vòng). Nên chạy qua đêm. Nhắc người dùng: **cắm sạc, không gập máy, không tắt máy**.

Theo dõi mỗi 10–15 phút:
```powershell
Get-Content results\<M>\repair.log -Tail 3
```
- Dòng `Máy <M> · <số> c0 · ... · còn <số> chain` ở đầu cho biết tổng số chain.
- Dòng `Kết quả: ...` ở cuối = chạy xong.
- **Worker:** máy ì → ghi `3` vào `results\<M>\repair\workers.txt`; CPU rảnh → `5`.
- Script dừng giữa chừng → chạy lại **đúng lệnh đó**; nó tự bỏ qua phần đã xong.
- `gen.err` hoặc `repair.err` có `Traceback` → dừng, gửi Dung file `.log` và `.err`.
- **Hết tiền / sai khóa:** script **tự dừng**: không mở việc mới, chờ việc đang chạy xong, hiện hộp thoại cảnh báo, in khối `!!!! ĐÃ DỪNG: HẾT TIỀN / HẾT HẠN MỨC (<mô hình>)` cuối log và ghi file `DA_DUNG.txt` (trong `results\<M>\gen\` hoặc `results\<M>\repair\`; file tự xóa khi chạy lại). Kiểm nhanh: `Test-Path results\<M>\repair\DA_DUNG.txt`. Khi thấy một trong các dấu hiệu đó:
  1. báo người dùng đúng mô hình bị dừng: `qwen3coder-or` → nạp tiền **OpenRouter**; `gpt6luna` → báo **Dung** (TeamoRouter dùng chung);
  2. người dùng nạp xong thì kiểm: `arrow models test <mô hình>` phải in `OK`;
  3. chạy lại **đúng lệnh vừa dừng** (gen hoặc repair). Phần chưa xong, kể cả các lần bị dừng (`STOPPED`), tự chạy tiếp, không cần `--retry-infra`. Nếu bước gen bị dừng thì chạy lại gen cho xong **rồi mới** chạy repair.

## Bước 6. Chạy lại lỗi hạ tầng (nếu có)

Cuối `gen.log` có `Còn N bộ test lỗi hạ tầng` → chạy lại lệnh gen (không nền) với `--retry-infra`, **rồi** chạy lại lệnh repair (để sửa thêm các c0 mới).
Cuối `repair.log` có `Còn N chain không xong` → chạy lại lệnh repair với `--retry-infra`.
Mỗi loại lặp tối đa 2 lần. Còn sót thì ghi vào báo cáo.

## Bước 7. Kiểm và nộp

```powershell
python ..\1\tn1_collect.py --dir results\<M>\gen
```
**Kết quả mong đợi:** `gen: <số> bộ test`, với số bằng "Số dòng gen mong đợi" trong bảng phân công. Số chain trong `results\<M>\repair\pilot_runs.jsonl` (không tính dòng lặp do chạy lại) = 4 × số dòng của `results\<M>\gen\tn2_pool.jsonl`.

Gom số liệu từng vòng sửa của máy này vào `results\<M>\report\` (homes không lên Git, nên phải gom tại máy):
```powershell
python pilot_analyze.py --dir results\<M>
```
**Kết quả mong đợi:** in bảng `## N = ...` (N của riêng máy này, **không** dùng), và thư mục `results\<M>\report\` có `pilot_chains.csv`, `pilot_rounds.csv`, `pilot_gen_timing.csv`, `pilot_n.md`.

Nộp (chạy trong `ARROW_Experiment\pilot`):
```powershell
git add results/<M>
git commit -m "Pilot: <M> shard <S>" -- results/<M>
git show --name-only --format= HEAD          # MỌI dòng phải bắt đầu bằng ARROW_Experiment/pilot/results/<M>/
git pull --rebase --autostash origin main
git push origin main
```
- `git show` có dòng khác → dừng, hỏi người dùng (không push).
- Push bị từ chối vì máy khác vừa push → chạy lại `pull --rebase` rồi `push`, tối đa 3 lần.
- `pull --rebase` báo `CONFLICT` → `git rebase --abort`, dừng, báo người dùng.
(`homes` tự bị bỏ qua nhờ `.gitignore`. **Chỉ** xóa thư mục homes của pilot sau khi Dung xác nhận đã gộp xong.)

**Báo cáo cho người dùng** (để gửi Dung):
```
Pilot · máy <M> · shard <S>
- Gen: <số> / <mong đợi> bộ test; <số> c0 vào tn2_pool
- Repair: <số> chain; dòng "Kết quả: ..." cuối repair.log
- Lỗi hạ tầng còn lại: gen <số>, repair <số>
- Đã push: commit <mã>
- Ghi chú: worker đã dùng, sự cố nếu có
```

## Lỗi thường gặp

| Thông báo | Cách xử lý |
|---|---|
| `Bản tool là '...', cần 203fb8e` | Thiếu `--arrow C:\tools\arrow-203\arrow.exe` ở lệnh repair, hoặc làm lại Bước 3 |
| `Sai bản arrow` / `khác bản đã chốt d0f2b5c` ở lệnh gen | Lệnh `arrow` trong PATH không phải `d0f2b5c`: kiểm lại, **không** đưa `C:\tools\arrow-203` vào PATH |
| `Chưa có ...tn2_pool.jsonl. Chạy bước gen trước.` | Chạy gen xong mới chạy repair |
| `Bộ prompt ... khác các lần chạy trước` | `git checkout -- ..\1\prompts` rồi chạy lại; vẫn lỗi thì báo Dung |
| `ARROW_HOME gốc chưa dùng bộ prompt của nhóm` | Chạy lại `python ..\1\setup_tn1.py` |
| `BAD_CONFIG` trong `repair.log` | Dừng, báo Dung (không chạy tiếp) |
| `ĐÃ DỪNG: HẾT TIỀN / HẾT HẠN MỨC` hoặc `ĐÃ DỪNG: LỖI CẤU HÌNH MÔ HÌNH` | Script đã tự dừng; làm theo mục "Hết tiền / sai khóa" ở Bước 5 |
| `B2_LLM_UNAVAILABLE` (429, 5xx: nhà cung cấp quá tải) | Không dừng cả lô; các lần đó ghi lỗi hạ tầng → Bước 6 (`--retry-infra`) |
| `Filename too long` | Bật đường dẫn dài (Bước 1 của hướng dẫn TN1) |

## Phiên bản

- Repo: nhánh `main` của `dungng2808/ARROW`
- Tool: sinh c0 `arrow 0.2.0` commit `d0f2b5c`; sửa commit `203fb8e` (GitHub Release `arrow-203fb8e`, zip sha256 `4B040A52…1BDC7`)
- Mô hình: `gpt6luna` (TeamoRouter) và `qwen3coder-or` (OpenRouter, Amazon Bedrock), cấu hình do `setup_tn1.py --gpt` đặt
- Prompt: sinh test `team-v1`; sửa test `team-v1` (v3), sha256 ở `ARROW_Experiment/1/prompt_trial/README.md`
- Quy tắc chọn N: quy tắc B, ngưỡng 1% (commit `9ef4c8c`)
- Dataset: tag `dataset-v1`, `classes2test_pilot` (50 lớp)
