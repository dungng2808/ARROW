# TN1 Qwen — bổ sung cột thời gian (dành cho agent)

> **Người dùng:** đưa file này cho agent và nói: *"Làm theo file này cho máy của tôi, tôi là <Quang | Hán | Chính>."* Không cần khóa API, không chạy lại TN1, khoảng 2 phút.
>
> **Agent:** bạn đang chạy trong repo `ARROW` người dùng mở sẵn. Mọi đường dẫn tính từ thư mục gốc đó. Làm lần lượt Bước 1–4. Gặp tình huống không có trong file thì **dừng và báo người dùng**.

## Mục đích

`tn1_collect.py` (bản 21/11/2026) có thêm cột thời gian từng bước kiểm c0: biên dịch, chạy test đích, chạy test module. Số liệu này nằm trong thư mục `homes` trên máy đã chạy TN1, không có trên Git, nên mỗi máy phải tự gom lại rồi nộp `tn1_candidates.csv`.

## Quy tắc bắt buộc

1. **Không sửa** file nào trong repo, **không xóa** thư mục `homes` hay `results/<M>/`.
2. Không chạy `tn1_run.py`, không gọi `arrow`.
3. Khi nộp: **chỉ commit `results/<M>/tn1_candidates.csv`**, push thẳng lên `main`, **không bao giờ** dùng `--force`.

| Người | `<M>` | Thư mục homes (đã dùng khi chạy TN1) |
|---|---|---|
| Quang | `quang-qwen` | `C:\arrow-homes\quang-qwen` |
| Hán | `han-qwen` | `C:\arrow-homes\han-qwen` |
| Chính | `chinh-qwen` | `D:\arrow-homes\chinh-qwen` |

## Bước 1. Cập nhật repo

```powershell
git switch main
git pull origin main
```
**Kết quả mong đợi:** có file `ARROW_Experiment/1/kiem_bo_sung_thoi_gian.py`.

## Bước 2. Kiểm thư mục homes còn không

```powershell
Test-Path "<thư mục homes trong bảng>\w1\runs"
```
**Kết quả mong đợi:** `True`. Nếu `False` (đã xóa) → **dừng**, báo người dùng: "Máy <M> không còn homes, không bổ sung được thời gian từng bước." Không làm tiếp.

## Bước 3. Gom lại và kiểm

Chạy trong `ARROW_Experiment/1`:
```powershell
python tn1_collect.py --machine <M>
python kiem_bo_sung_thoi_gian.py <M>
```
**Kết quả mong đợi:**
- Lệnh 1 in `<M>: <số> bộ test (...)` với số bằng lúc nộp TN1 (Quang 189, Hán 195, Chính 177).
- Lệnh 2 in **`ĐẠT`**, và dòng "Số dòng có thời gian từng bước" có `step_compile_s` lớn hơn 0.
- `git status --short` chỉ có `results/<M>/tn1_candidates.csv` bị sửa. Nếu thấy cả `results/<M>/tn2_pool.jsonl` bị sửa → **dừng**, báo người dùng (không nộp).

In `KHÔNG ĐẠT` → **dừng**, không nộp, chép nguyên phần in ra gửi người dùng.

## Bước 4. Nộp

```powershell
git commit -m "TN1 Qwen: <M> bổ sung thời gian từng bước" -- results/<M>/tn1_candidates.csv
git show --name-only --format= HEAD          # phải đúng 1 dòng: ARROW_Experiment/1/results/<M>/tn1_candidates.csv
git pull --rebase --autostash origin main
git push origin main
```
- `git show` có dòng khác → dừng, hỏi người dùng (không push).
- Push bị từ chối vì máy khác vừa push → chạy lại `pull --rebase` rồi `push`, tối đa 3 lần.
- `pull --rebase` báo `CONFLICT` → `git rebase --abort`, dừng, báo người dùng.

**Báo cáo cho người dùng** (để gửi Dung):
```
TN1 Qwen · máy <M> · bổ sung thời gian
- Kiểm: ĐẠT, step_compile_s có <số> dòng
- Đã push: commit <mã> (git log -1 --format=%h)
```
Sau khi Dung xác nhận đã nhận, có thể xóa thư mục homes cho đỡ đầy ổ.
