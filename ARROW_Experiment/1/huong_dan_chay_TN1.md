# TN1 Qwen — hướng dẫn chạy (dành cho agent)

> **Người dùng:** đưa file này cho agent (Claude Code, Codex, …) và nói: *"Làm theo file này cho máy của tôi, tôi là <Quang | Hán | Chính | Dung>."* Agent sẽ hỏi bạn đúng 2 việc: **nhập khóa OpenRouter** và **đăng nhập GitHub** (nếu máy chưa có).
>
> **Agent:** bạn đang chạy trong repo `ARROW` người dùng mở sẵn (thư mục gốc chứa `ARROW_Experiment`, `ARROW_dataset`, `ARROW_Developer_CLI`). Mọi đường dẫn trong file **tính từ thư mục gốc đó**. Làm lần lượt từ Bước 0 tới Bước 9. Sau mỗi bước, kiểm đúng "Kết quả mong đợi" rồi mới sang bước sau. Gặp tình huống không có trong file thì **dừng và báo người dùng**, không tự sáng tạo cách khác.

## Mục đích

Chạy Thực nghiệm 1 (RQ1a) với mô hình **Qwen3-Coder-30B qua OpenRouter** trên một phần dataset: sinh unit test cho các lớp Java bằng 3 cách viết prompt, kiểm test qua 5 bước, rồi nộp kết quả lên GitHub. Bốn máy chia nhau 268 lớp.

## Quy tắc bắt buộc (agent không được vi phạm)

1. **Không đọc, in, ghi hay gõ khóa API.** Khóa chỉ do người dùng tự nhập bằng `arrow secrets set openrouter` trong terminal của họ.
2. **Không sửa** bất cứ file nào trong repo: `prompts/`, `tn1_run.py`, `tn1_collect.py`, `tn1_analyze.py`, `setup_tn1.py`, `ARROW_dataset/`. Script lỗi thì dừng và báo, **không tự sửa code**.
3. **Không đổi cấu hình model** do `setup_tn1.py` đặt (model, nhà cung cấp, temperature, max_tokens).
4. Chỉ chạy **đúng phần (`--shard`) và tên máy (`--machine`)** của người dùng trong bảng dưới.
5. Không xóa `results/<tên máy>/` trước khi nộp xong.
6. Không push lên `main`. Chỉ push nhánh `tn1/<tên máy>`.

## Phân công

| Người | Hệ điều hành | `--machine` | `--shard` | Số lớp | Số dòng kết quả mong đợi |
|---|---|---|---|---|---|
| Quang | Windows | `quang-qwen` | `1/4` | 63 | 189 |
| Hán | Windows | `han-qwen` | `2/4` | 65 | 195 |
| Dung | macOS | `dung-qwen` | `3/4` | 81 | 243 |
| Chính | Windows | `chinh-qwen` | `4/4` | 59 | 177 |

Dưới đây `<M>` = `--machine`, `<S>` = `--shard` của người dùng.

---

## Bước 0. Kiểm máy

Windows dùng **PowerShell**. Kiểm: ≥ 4 nhân CPU, ≥ 16 GB RAM, ≥ 20 GB ổ trống.
```powershell
(Get-CimInstance Win32_ComputerSystem).NumberOfLogicalProcessors
[math]::Round((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory/1GB)
Get-PSDrive C | Select-Object Free
```
Thiếu thì báo người dùng (vẫn chạy được nhưng dùng `--workers 2` ở Bước 7).

## Bước 1. Cài phần mềm

Cần: Git, Python ≥ 3.10, JDK **8, 11, 17, 21** (đủ cả 4), Maven **3.9.x**. Cái nào đã có thì bỏ qua.

**Windows:**
```powershell
winget install -e --id Git.Git
winget install -e --id Python.Python.3.12
winget install -e --id EclipseAdoptium.Temurin.8.JDK
winget install -e --id EclipseAdoptium.Temurin.11.JDK
winget install -e --id EclipseAdoptium.Temurin.17.JDK
winget install -e --id EclipseAdoptium.Temurin.21.JDK
# Maven 3.9.16
New-Item -ItemType Directory -Force C:\tools | Out-Null
Invoke-WebRequest https://archive.apache.org/dist/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip -OutFile $env:TEMP\maven.zip
Expand-Archive $env:TEMP\maven.zip -DestinationPath C:\tools -Force
[Environment]::SetEnvironmentVariable("Path", [Environment]::GetEnvironmentVariable("Path","User") + ";C:\tools\apache-maven-3.9.16\bin", "User")
# Bật đường dẫn dài cho git
git config --global core.longpaths true
```
Bật đường dẫn dài của Windows cần quyền admin. Thử lệnh dưới; báo lỗi quyền thì **nhờ người dùng** mở PowerShell "Run as administrator" chạy giúp:
```powershell
New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name LongPathsEnabled -Value 1 -PropertyType DWORD -Force
```
**macOS:** `brew install git python maven`, JDK 8/11/17/21 (Temurin).

Mở **cửa sổ PowerShell mới** (để PATH mới có hiệu lực).

**Kết quả mong đợi:** `git --version`, `python --version` (≥ 3.10), `mvn -v` (3.9.x) đều chạy được.

## Bước 2. Cập nhật repo đang mở

Ở thư mục gốc của repo:
```powershell
git status --short
git fetch origin
git switch main
git pull origin main
```
- `git status` có file đã sửa trong `ARROW_Experiment/` hoặc `ARROW_dataset/` → **dừng, hỏi người dùng** (không tự xóa thay đổi của họ).
- `git switch`/`git pull` báo xung đột hoặc lỗi đăng nhập → dừng, nhờ người dùng xử lý.

**Kết quả mong đợi:** có `ARROW_Experiment/1/` chứa `tn1_run.py`, `setup_tn1.py`, `prompts/override/`.

## Bước 3. Cài `arrow` (đúng bản commit `d0f2b5c`)

**Cách A (ưu tiên): file zip Dung gửi** `arrow-0.2.0-d0f2b5c-win-x64.zip`. Hỏi người dùng đường dẫn file, rồi:
```powershell
(Get-FileHash <đường dẫn zip> -Algorithm SHA256).Hash   # phải là 93DF3932DF12AC25C5238E80B94F64CB55C06896EBA62CA898D7786981571910
New-Item -ItemType Directory -Force C:\tools\arrow | Out-Null
Expand-Archive <đường dẫn zip> -DestinationPath C:\tools\arrow -Force
[Environment]::SetEnvironmentVariable("Path", [Environment]::GetEnvironmentVariable("Path","User") + ";C:\tools\arrow", "User")
```
Hash sai → dừng, báo người dùng.

**Cách B (không có zip): build từ mã nguồn ở đúng commit `d0f2b5c`** (cần .NET SDK 10 và JDK 17). Phải build từ commit `d0f2b5c`, không phải commit hiện tại, vì `arrow --version` ghi commit lúc build và script kiểm đúng `d0f2b5c`:
```powershell
winget install -e --id Microsoft.DotNet.SDK.10
git worktree add C:\arrow-src d0f2b5c        # bản chép repo ở commit d0f2b5c, không đụng thư mục đang mở
cd C:\arrow-src\ARROW_Developer_CLI
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-17*" | Select-Object -First 1).FullName
mvn -q -f tools/java-helper/pom.xml package
dotnet publish src/Arrow.Cli -c Release -r win-x64 --self-contained -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true -p:DebugType=none -o C:\tools\arrow
Rename-Item C:\tools\arrow\Arrow.Cli.exe arrow.exe
[Environment]::SetEnvironmentVariable("Path", [Environment]::GetEnvironmentVariable("Path","User") + ";C:\tools\arrow", "User")
```
Build xong quay về thư mục gốc repo và xóa bản chép: `git worktree remove C:\arrow-src --force`. macOS: Dung tự có bản `arrow`.

Mở PowerShell mới. **Kết quả mong đợi:** `arrow --version` in ra chuỗi có `commit d0f2b5c`.

## Bước 4. Cài đặt bằng script

```powershell
cd ARROW_Experiment\1
python setup_tn1.py
```
Script tự đặt: JDK (tự tìm), bộ prompt nhóm, nhà cung cấp OpenRouter, model `qwen3coder-or`, bản sao Maven Central; rồi chạy `arrow doctor`.

- `[X] Không tìm thấy JDK <n>` → tìm thư mục JDK đó (`Get-ChildItem "C:\Program Files\Eclipse Adoptium"`), chạy lại với `--jdk<n> "<thư mục>"`.
- `[X] ... settings.xml đã có sẵn nhưng chưa có bản sao Google` → báo người dùng (file Maven riêng của họ), không tự ghi đè.
- `[X] Chưa có khóa OpenRouter` → sang Bước 5.

## Bước 5. Người dùng nhập khóa OpenRouter

**Agent không làm bước này.** Nói với người dùng:

> Mở một cửa sổ PowerShell, gõ `arrow secrets set openrouter`, dán khóa OpenRouter khi được hỏi (chữ không hiện ra), Enter. Xong báo tôi.

Khóa lấy từ Dung (hoặc khóa của chính bạn, nhớ đặt giới hạn chi tiêu). Sau đó agent chạy lại:
```powershell
python setup_tn1.py
```
**Kết quả mong đợi:** dòng cuối `[OK] Máy đã sẵn sàng.` Nếu `[X] Model chưa trả lời đúng` → báo người dùng kiểm khóa và số dư OpenRouter.

## Bước 6. Chạy khói (khoảng 3 phút)

```powershell
python tn1_run.py --machine <M> --models qwen3coder-or --pilot --limit 1
```
**Kết quả mong đợi:** 3 dòng `[i/3] DONE ...` (kết quả VALID/COMPILE/… đều được), **không có `INFRA`**, dòng cuối `Kết quả: ...`.
Có `INFRA` hoặc script báo lỗi → dừng, gửi người dùng nội dung lỗi và các file `results\<M>-smoke\logs\*.err` để chuyển cho Dung.

## Bước 7. Chạy thật (khoảng 1–2 giờ)

Chạy nền, ghi log ra file (`-u`: ghi log ngay, không đợi). Windows:
```powershell
New-Item -ItemType Directory -Force results\<M> | Out-Null
Start-Process python -ArgumentList "-u tn1_run.py --machine <M> --models qwen3coder-or --shard <S> --workers 4" -RedirectStandardOutput results\<M>\run.log -RedirectStandardError results\<M>\run.err -WindowStyle Hidden
```
macOS: `caffeinate -i python3 -u tn1_run.py --machine <M> --models qwen3coder-or --shard <S> --workers 4 > results/<M>/run.log 2>&1 &`

Theo dõi mỗi 5–10 phút:
```powershell
Get-Content results\<M>\run.log -Tail 3
```
- Mỗi dòng `[i/tổng] DONE ...` là một lần xong. Trên Windows, bản chép các dự án nằm ở `C:\arrow-homes\<M>\` (đường dẫn ngắn để tránh lỗi 260 ký tự); cần khoảng 10–20 GB trống ở ổ C. Dòng `Kết quả: ...` ở cuối = chạy xong.
- Script tự giữ máy Windows không ngủ. Nhắc người dùng: **cắm sạc, không gập máy, không tắt máy**.
- **Worker:** máy ì hoặc RAM gần hết → ghi `3` vào `results\<M>\workers.txt`; CPU rảnh nhiều → ghi `5`. Script tự đổi, không cần chạy lại.
- Script dừng giữa chừng (mất mạng, máy khởi động lại) → chạy lại **đúng lệnh trên**; nó tự bỏ qua phần đã xong.
- `run.err` có `Traceback` → dừng, báo người dùng gửi Dung file `run.err` và `run.log`.

## Bước 8. Chạy lại lỗi hạ tầng (nếu có)

Nếu cuối `run.log` có dòng `Còn N bộ test lỗi hạ tầng`:
```powershell
python tn1_run.py --machine <M> --models qwen3coder-or --shard <S> --retry-infra
```
Lặp tối đa 2 lần. Vẫn còn thì ghi số lượng vào báo cáo ở Bước 9.

## Bước 9. Gom, kiểm, nộp

```powershell
python tn1_collect.py --machine <M>
```
**Kết quả mong đợi:** dòng `<M>: <số> bộ test (...)`, với `<số>` đúng **"Số dòng kết quả mong đợi"** trong bảng phân công. Sai số → báo người dùng, không nộp.

Nộp (cần quyền push; chưa đăng nhập GitHub thì nhờ người dùng):
```powershell
git checkout -b tn1/<M>
git add results/<M>
git commit -m "TN1 Qwen: <M> shard <S>"
git push -u origin tn1/<M>
```
(`homes/` tự bị bỏ qua nhờ `.gitignore`. Trên Windows, sau khi push xong và Dung xác nhận đã nhận đủ, có thể xóa `C:\arrow-homes\<M>` cho đỡ đầy ổ.)

**Báo cáo cho người dùng** (để gửi Dung), đúng mẫu:
```
TN1 Qwen · máy <M> · shard <S>
- Số bộ test: <số> / <mong đợi>
- Kết quả: <dòng "Kết quả: ..." cuối run.log>
- Lỗi hạ tầng còn lại: <số>
- Nhánh đã push: tn1/<M> (commit <mã>)
- Ghi chú: <worker đã dùng, sự cố nếu có>
```

---

## Lỗi thường gặp

| Thông báo | Cách xử lý |
|---|---|
| `arrow` không nhận lệnh | Chưa thêm `C:\tools\arrow` vào PATH, hoặc chưa mở PowerShell mới |
| `Sai bản arrow` / `Các lần chạy trước dùng tool ...` | Làm lại Bước 3 |
| `ARROW_HOME gốc chưa dùng bộ prompt của nhóm` | Chạy lại `python setup_tn1.py` |
| `Bộ prompt khác với các lần chạy trước` | `git checkout -- prompts` rồi chạy lại |
| Checksum dataset không khớp | `git checkout -- ..\..\ARROW_dataset` rồi chạy lại |
| `CFG_NO_MODEL` | Chạy lại `python setup_tn1.py` |
| `CFG_SECRET_MISSING` | Người dùng làm Bước 5 |
| `B2_LLM_CONFIG` / `B2_LLM_UNAVAILABLE` (401, 402, 429) | Khóa sai, hết tiền, hoặc OpenRouter quá tải → báo người dùng; sau đó Bước 8 |
| Lỗi `429` khi Maven tải thư viện | `~\.m2\settings.xml` chưa có bản sao Google (xem dòng `[X]` của `setup_tn1.py`) |
| `Filename too long` | Bật đường dẫn dài (Bước 1). Script đã tự đặt bản chép dự án ở `C:\arrow-homes\<M>`; vẫn lỗi thì báo Dung |

## Phiên bản

- Repo: nhánh `main` của `dungng2808/ARROW` (lấy bản mới nhất bằng `git pull origin main`)
- Tool: `arrow 0.2.0`, commit `d0f2b5c`
- Bộ prompt: `team-v1` (sha256 ghi trong `prompts/vi/README.md`)
- Dataset: tag `dataset-v1`
