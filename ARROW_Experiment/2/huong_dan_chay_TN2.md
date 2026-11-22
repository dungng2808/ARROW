# TN2 (RQ1b, RQ2) — hướng dẫn chạy (dành cho agent)

> **Người dùng:** đưa file này cho agent và nói: *"Làm theo file này cho máy của tôi, tôi là <Chính | Kiều Anh | Quang | Hán>."* Agent sẽ cần bạn: **tự nhập khóa** (TeamoRouter do Dung gửi riêng, OpenRouter của bạn) nếu máy chưa có, và **đăng nhập GitHub** nếu máy chưa có.
>
> **Agent:** bạn đang chạy trong repo `ARROW` người dùng mở sẵn. Mọi đường dẫn tính từ thư mục gốc đó. Làm lần lượt Bước 1–7, kiểm "Kết quả mong đợi" sau mỗi bước. Gặp tình huống không có trong file thì **dừng và báo người dùng**.

## Mục đích

TN2 so các cơ chế sửa test. Dung đã chọn và đóng băng **179 candidate** (c0 thất bại của TN1) trong `ARROW_Experiment/2/tn2_subset.jsonl`, và giao mỗi candidate cho một máy. Với mỗi candidate của máy mình, script chạy:

**2 mô hình sửa** (GPT-6 Luna, Qwen3-Coder) × **4 nhánh** (F+, F−, F2, A) × **3 lần lặp** = **24 chain**, tối đa **10 vòng** sửa mỗi chain, bằng `arrow` bản `203fb8e` (giống pilot).

Script chạy hết **lần 1** của mọi candidate trước (lần đo chính), rồi mới lần 2, lần 3.

## Quy tắc bắt buộc

1. **Không đọc, in, ghi hay gõ khóa API.**
2. **Không sửa** file nào trong repo (`2/*`, `pilot/*.py`, `1/*.py`, `1/prompts/`, `ARROW_dataset/`). Đặc biệt **không** chạy `tn2_subset.py` (chỉ Dung chạy một lần). Script lỗi thì dừng và báo.
3. Không đổi cấu hình mô hình do `setup_tn1.py` đặt.
4. Chỉ chạy **đúng `--machine`** của người dùng trong bảng dưới.
5. **Không xóa thư mục homes** (`C:\arrow-homes\tn2-<M>`) cho tới khi **Dung xác nhận đã gộp xong**: số liệu từng vòng sửa chỉ nằm trong đó.
6. Khi nộp: **chỉ commit `ARROW_Experiment/2/results/<M>/`**, push thẳng lên `main`, **không bao giờ** `--force`.

## Phân công

Chia theo tốc độ đo ở pilot (Quang = Hán theo yêu cầu của Dung). Mỗi candidate có 24 chain.

| Người | `<M>` | Candidate | Chain | `--workers` | Ước thời gian | Thư mục homes |
|---|---|---|---|---|---|---|
| Dung (macOS) | `dung` | 82 | 1.968 | 6 | khoảng 8–9 giờ | (mặc định) |
| Chính | `chinh` | 33 | 792 | 4 | khoảng 8–9 giờ | `D:\arrow-homes\tn2-chinh` |
| Kiều Anh | `kieuanh` | 30 | 720 | 4 | khoảng 8–9 giờ (ước) | (mặc định, ổ C) |
| Quang | `quang` | 17 | 408 | 4 | khoảng 6 giờ | (mặc định, ổ C) |
| Hán | `han` | 17 | 408 | 4 | khoảng 14 giờ (nên chạy qua đêm) | (mặc định, ổ C) |

Với Chính, thêm `--homes-dir D:\arrow-homes\tn2-chinh` vào **mọi** lệnh `tn2_run.py` bên dưới. Người khác không thêm.

---

## Bước 1. Cập nhật repo

```powershell
git switch main
git pull origin main
```
**Kết quả mong đợi:** có `ARROW_Experiment/2/tn2_run.py`, `tn2_collect.py`, `tn2_subset.jsonl`, `tn2_subset.jsonl.sha256`.

## Bước 2. Máy đã có sẵn cấu hình chưa

```powershell
C:\tools\arrow-203\arrow.exe --version
```
- In ra dòng có `commit 203fb8e` (máy **đã chạy pilot**: Chính, Quang, Hán) → sang **Bước 3**.
- Báo không tìm thấy (máy **mới**: Kiều Anh) → cài đặt trước:
  1. làm **Bước 0–5** của `ARROW_Experiment/1/huong_dan_chay_TN1.md` (cài JDK, Maven, Gradle, `arrow` bản `d0f2b5c`, chạy `setup_tn1.py`, người dùng tự nhập khóa OpenRouter). **Không** làm Bước 6 trở đi của file đó;
  2. làm **Bước 2 và Bước 3** của `ARROW_Experiment/pilot/huong_dan_chay_pilot.md` (thêm GPT bằng `setup_tn1.py --gpt`, người dùng tự nhập khóa TeamoRouter; cài `arrow` bản `203fb8e` vào `C:\tools\arrow-203`);
  3. quay lại đây, sang Bước 3.

## Bước 3. Kiểm hai mô hình và tăng tốc Windows

```powershell
arrow models test gpt6luna
arrow models test qwen3coder-or
```
**Kết quả mong đợi:** cả hai in `OK`. Lỗi khóa → người dùng **tự** chạy `arrow secrets set teamo` (GPT, khóa Dung gửi) hoặc `arrow secrets set openrouter` (Qwen), rồi kiểm lại.

**Tăng tốc build (nên làm, nhất là máy Hán: ở pilot máy Hán build chậm gấp 5–6 lần máy cùng cấu hình).** Nhờ người dùng tự làm (cần quyền quản trị):
- Windows Security → Virus & threat protection → Manage settings → Exclusions → Add an exclusion → Folder: thêm `C:\arrow-homes` (Chính: `D:\arrow-homes`), `%USERPROFILE%\.m2`, `%USERPROFILE%\.gradle`.
- Settings → System → Power: chọn **Best performance** (Hiệu năng cao nhất). **Cắm sạc** suốt lúc chạy.

## Bước 4. Chạy khói (khoảng 15–30 phút)

Chạy trong `ARROW_Experiment\2`:
```powershell
cd ARROW_Experiment\2
python tn2_run.py --machine <M> --workers 1 --limit 1 --arrow C:\tools\arrow-203\arrow.exe
python tn2_collect.py --machine <M> --smoke
```
**Kết quả mong đợi:**
- Lệnh 1: dòng đầu có `commit 203fb8e`, dòng `Máy <M> · phần <M>: 1/<số> candidate · lần [1] · ... · còn 8 chain`, rồi **8 dòng `DONE`** (1 candidate × 2 mô hình sửa × 4 nhánh), **không** có `BAD_CONFIG`, `INFRA` hay `Traceback`.
- Lệnh 2: in `ĐẠT`.

**Ghi lại** tổng thời gian chạy khói (để Dung chỉnh phần chia nếu máy nhanh/chậm khác ước tính). Kết quả chạy khói nằm ở `results\<M>-smoke\`, **không nộp**. Lỗi → dừng, gửi Dung phần in ra.

## Bước 5. Chạy thật

Chạy nền. Windows:
```powershell
New-Item -ItemType Directory -Force results\<M> | Out-Null
Start-Process python -ArgumentList "-u tn2_run.py --machine <M> --workers 4 --arrow C:\tools\arrow-203\arrow.exe" -RedirectStandardOutput results\<M>\run.log -RedirectStandardError results\<M>\run.err -WindowStyle Hidden
```
macOS (Dung): `caffeinate -i python3 -u tn2_run.py --machine dung --workers 6 --arrow ~/.local/share/arrow-dev/build-tn2/arrow > results/dung/run.log 2> results/dung/run.err &`

Nhắc người dùng: **cắm sạc, không gập máy, không tắt máy** (script tự giữ máy không ngủ).

Theo dõi mỗi 15–30 phút:
```powershell
Get-Content results\<M>\run.log -Tail 3
```
- Mỗi dòng `[đã xong/tổng] lần <k> DONE ...` là một chain xong.
- Dòng `Kết quả: ...` ở cuối = chạy xong.
- **Worker:** máy ì → ghi `3` vào `results\<M>\workers.txt`; CPU rảnh → `5`.
- Script dừng giữa chừng (mất điện, khởi động lại máy) → chạy lại **đúng lệnh đó**; nó tự bỏ qua phần đã xong.
- `run.err` có `Traceback` → dừng, gửi Dung file `run.log` và `run.err`.
- **Hết tiền / sai khóa:** script **tự dừng**: không mở việc mới, chờ chain đang chạy xong, hiện hộp thoại cảnh báo, in khối `!!!! ĐÃ DỪNG: ...` cuối log và ghi file `results\<M>\DA_DUNG.txt`. Kiểm nhanh: `Test-Path results\<M>\DA_DUNG.txt`. Khi đó:
  1. báo người dùng đúng mô hình bị dừng: `qwen3coder-or` → nạp tiền **OpenRouter**; `gpt6luna` → báo **Dung** (TeamoRouter dùng chung);
  2. người dùng nạp xong thì kiểm: `arrow models test <mô hình>` phải in `OK`;
  3. chạy lại **đúng lệnh ở Bước 5**. Phần chưa xong, kể cả chain bị dừng (`STOPPED`), tự chạy tiếp.

## Bước 6. Chạy lại lỗi hạ tầng (nếu có)

Cuối `run.log` có `Còn N chain không xong` → chạy lại (không nền):
```powershell
python tn2_run.py --machine <M> --workers 4 --retry-infra --arrow C:\tools\arrow-203\arrow.exe
```
Lặp tối đa 2 lần. Còn sót thì ghi vào báo cáo. `BAD_CONFIG` → **không** chạy lại, báo Dung.

## Bước 7. Gom, kiểm và nộp

Gom số liệu từng vòng sửa vào `results\<M>\report\` (homes không lên Git, nên phải gom tại máy):
```powershell
python tn2_collect.py --machine <M>
```
**Kết quả mong đợi:** dòng `Máy <M>: <số> chain (<số> DONE ...)` với số chain = số trong bảng phân công, và dòng cuối **`ĐẠT`**. `KHÔNG ĐẠT` → làm theo dòng `[X]` (thường là chạy lại Bước 5 hoặc 6), gom lại; vẫn không đạt thì dừng, gửi Dung phần in ra.

Nộp (chạy trong `ARROW_Experiment\2`):
```powershell
git add results/<M>
git commit -m "TN2: <M>" -- results/<M>
git show --name-only --format= HEAD          # MỌI dòng phải bắt đầu bằng ARROW_Experiment/2/results/<M>/
git pull --rebase --autostash origin main
git push origin main
```
- `git show` có dòng khác → dừng, hỏi người dùng (không push).
- Push bị từ chối vì máy khác vừa push → chạy lại `pull --rebase` rồi `push`, tối đa 3 lần.
- `pull --rebase` báo `CONFLICT` → `git rebase --abort`, dừng, báo người dùng.

(`homes` tự bị bỏ qua nhờ `.gitignore`. **Chỉ** xóa `C:\arrow-homes\tn2-<M>` sau khi Dung xác nhận đã gộp xong.)

**Báo cáo cho người dùng** (để gửi Dung):
```
TN2 · máy <M>
- Chạy khói: <số> phút cho 8 chain
- Chạy thật: <số> chain; dòng "Kết quả: ..." cuối run.log; thời gian chạy <giờ>
- tn2_collect: ĐẠT / KHÔNG ĐẠT
- Lỗi hạ tầng còn lại: <số>
- Đã push: commit <mã>
- Ghi chú: worker đã dùng, đã thêm loại trừ Defender chưa, sự cố nếu có
```

## Lỗi thường gặp

| Thông báo | Cách xử lý |
|---|---|
| `Bản tool là '...', cần 203fb8e` | Thiếu `--arrow C:\tools\arrow-203\arrow.exe`, hoặc làm lại Bước 3 của hướng dẫn pilot |
| `tn2_subset.jsonl khác bản đã đóng băng` | `git checkout -- tn2_subset.jsonl` rồi `git pull`; vẫn lỗi thì báo Dung |
| `File c0 bị đổi so với lúc đóng băng` / `Thiếu file c0` | `git checkout -- ..\1\results` rồi `git pull`; vẫn lỗi thì báo Dung |
| `Không có phần '<M>' trong tập con` | Sai tên `--machine`: xem bảng phân công |
| `Bộ prompt ... khác các lần chạy trước` | `git checkout -- ..\1\prompts` rồi chạy lại; vẫn lỗi thì báo Dung |
| `ARROW_HOME gốc chưa dùng bộ prompt của nhóm` | Chạy lại `python ..\1\setup_tn1.py --gpt` |
| `Mô hình ... chưa cấu hình` | Chạy lại `python ..\1\setup_tn1.py --gpt` |
| `BAD_CONFIG` trong `run.log` | Dừng, báo Dung (không chạy tiếp) |
| `ĐÃ DỪNG: HẾT TIỀN / HẾT HẠN MỨC` hoặc `ĐÃ DỪNG: LỖI CẤU HÌNH MÔ HÌNH` | Script đã tự dừng; làm theo mục "Hết tiền / sai khóa" ở Bước 5 |
| `B2_LLM_UNAVAILABLE` (429, 5xx) | Không dừng cả lô; các chain đó ghi lỗi hạ tầng → Bước 6 |
| `Filename too long` | Bật đường dẫn dài (Bước 1 của hướng dẫn TN1) |

## Phiên bản

- Repo: nhánh `main` của `dungng2808/ARROW`
- Tool: `arrow 0.2.0` commit `203fb8e` (GitHub Release `arrow-203fb8e`, zip sha256 `4B040A52…1BDC7`)
- Mô hình: `gpt6luna` (TeamoRouter) và `qwen3coder-or` (OpenRouter, Amazon Bedrock), cấu hình do `setup_tn1.py --gpt` đặt
- Prompt sửa: `team-v1` (v3), giống pilot
- N = 10 (pilot, quy tắc B ngưỡng 1%); 3 lần lặp, lần 1 là lần đo chính
- Tập con: `ARROW_Experiment/2/tn2_subset.jsonl` (179 candidate, chuỗi xếp `arrow-tn2-subset-v1`); dataset tag `dataset-v1`
