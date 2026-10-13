# 06. Màn hình console (tầng UI)

**Mục đích:** mô tả tầng UI của `arrow-fw`: mỗi màn hình hiện gì, người dùng nhập gì, và lệnh tương ứng. Kiến trúc 3 tầng ở spec 07. Tầng UI chỉ **nhập** và **hiện**; mọi xử lý gọi xuống tầng Service qua interface.

## 1. Nguyên tắc

| Nguyên tắc | Cách làm |
| --- | --- |
| Nhập | Chỉ bằng `Console.ReadLine()`: chọn mục bằng số rồi Enter, nhập chữ rồi Enter. Enter để trống = giữ giá trị mặc định ghi trong ngoặc vuông |
| Xuất | Chỉ bằng `Console.WriteLine()` / `Console.Write()`: chữ thuần, bảng căn cột bằng khoảng trắng (`PadRight`), không vẽ lại màn hình, không màu, không thư viện giao diện |
| Ký hiệu trạng thái | `[OK]` đạt, `[X]` lỗi, `[!]` cảnh báo, `[-]` bỏ qua (chữ ASCII để hiện đúng trên mọi terminal, kể cả Windows) |
| Tiếng Việt | Đặt `Console.OutputEncoding` và `Console.InputEncoding` là UTF-8 khi khởi động |
| Khóa API | Nhập bằng `ReadLine` như mọi chỗ khác (chốt 14/10/2026), nên khóa **hiện trên màn hình lúc gõ**. Trước khi hỏi, in nhắc `[!] Khoa se hien tren man hinh khi go`. Sau khi nhận, mọi chỗ khác chỉ hiện 4 ký tự cuối (`...2a4b`); khóa không bao giờ ghi vào log hay file kết quả |
| UI không xử lý | UI không đọc/ghi file, không chạy tiến trình, không gọi model. UI gọi interface của tầng Service (spec 07 Mục 4) và in kết quả trả về |
| Lệnh và menu | Mỗi màn hình mở được bằng menu (gõ `arrow-fw`) hoặc bằng lệnh trực tiếp (`arrow-fw run …`) để dùng trong script |

**Hàm nhập dùng chung** (lớp `ConsoleInput`):

| Hàm | Hiện | Đọc |
| --- | --- | --- |
| `Choose(title, items)` | Tiêu đề, danh sách `1. …`, `2. …`, dòng `Chọn số: ` | `ReadLine`; số không hợp lệ thì in `[!] Nhập số từ 1 đến n` và hỏi lại |
| `Ask(label, default)` | `label [default]: ` | `ReadLine`; trống → `default` |
| `AskInt(label, default, min, max)` | như trên | `ReadLine`; số ngoài khoảng thì hỏi lại |
| `Confirm(label, defaultYes)` | `label (y/n) [y]: ` | `ReadLine`; `y`/`n`, trống → mặc định |
| `AskSecret(label)` | In nhắc khóa sẽ hiện, rồi `label: ` | `ReadLine`; trống → giữ khóa cũ (nếu có) |

**Hàm xuất dùng chung** (lớp `ConsoleOutput`): `Title(text)` (dòng tiêu đề và dòng `====`), `Line(text)`, `Table(headers, rows)` (căn cột theo độ dài lớn nhất của mỗi cột), `Status(state, label, detail)` (in `[OK] label  detail`), `Blank()`.

Các ví dụ màn hình dưới đây viết không dấu cho thẳng cột trong tài liệu; màn hình thật hiện tiếng Việt có dấu.

## 2. Danh sách màn hình

| # | Màn hình | Lệnh |
| --- | --- | --- |
| S1 | Menu chính | `arrow-fw` |
| S2 | Cài đặt máy | `arrow-fw setup` |
| S3 | Model, provider, khóa API, giá | `arrow-fw models` |
| S4 | Kiểm tra môi trường | `arrow-fw doctor` |
| S5 | Chạy và theo dõi | `arrow-fw run [--run-id R] [--tasks …] [--blocks 1,2,3,4] [--rerun-infra]` |
| S6 | Trạng thái một run | `arrow-fw status --run-id R` |
| S7 | Chi tiết một task | `arrow-fw show --run-id R --task T` |
| S8 | Báo cáo | `arrow-fw report --run-id R [--run-id R2 …]` |
| S9 | Cần chạy lại riêng | `arrow-fw rerun-list --run-id R` |
| S10 | Sàng lọc CUT (tiêu chí B) | `arrow-fw screen --manifest F --run-id R` |

## 3. S1: Menu chính

```
ARROW Framework v0.1 · .NET 8 · osx-arm64
==========================================
May:          Dung-mac
Thuc nghiem:  pilot-v1 (sha d5567edc...)
Moi truong:   [OK] READY (14/10 09:12)
Model:        [OK] gpt-6-luna  api.teamorouter.com
Thu muc repo: ../repo (84 GB trong)

Run gan day:
  pilot-1   DANG CHAY  khoi 3  112/240 job  INFRA 1
  smoke-1   XONG       4/4 khoi             can chay lai 0

Chon viec:
  1. Chay thuc nghiem (khoi 1 -> 4)
  2. Chay tiep run dang do
  3. Sang loc CUT (chi khoi 1, tieu chi B)
  4. Xem trang thai run
  5. Xem chi tiet mot task
  6. Bao cao
  7. Can chay lai rieng
  8. Cai dat may
  9. Model, provider, khoa API, gia
 10. Kiem tra moi truong
  0. Thoat
Chon so: 1
```

Chọn `1` thì hỏi tiếp, mỗi câu một lần `ReadLine`:

```
Run id [run-20261014-1030]: pilot-1
Tap task:
  1. Tat ca task cua thuc nghiem (59)
  2. Doc tu file danh sach task_id
  3. Nhap task_id, cach nhau dau phay
Chon so: 1
Cac khoi [1,2,3,4]:
Bat dau? (y/n) [y]: y
```

rồi chuyển sang S5. Máy chưa có `machine.local.json` thì S1 vào thẳng S2. Sau mỗi màn hình con, in `Nhan Enter de ve menu` và chờ `ReadLine`.

## 4. S2: Cài đặt máy

Sáu bước; mỗi bước in "Buoc nay lam gi / Vi sao / Ban can lam gi" rồi hỏi bằng `ReadLine`. Nhập `<` ở bất kỳ câu nào để quay lại bước trước.

```
Cai dat 3/6 · JDK, Maven, Gradle, Git
=====================================
Buoc nay lam gi:  Tim JDK 8, 11, 17, 21 va cong cu build
Vi sao:           Moi repository build bang dung JDK cua no
Ban can lam gi:   Moi dong [OK] thi chon Tiep tuc

Cong cu   Trang thai  Phien ban / duong dan
JDK 8     [OK]        ../Java-version/runtime/jdk-8/Contents/Home
JDK 11    [OK]        ...
JDK 17    [OK]        ...
JDK 21    [X]         khong tim thay - xem Java-version/README.md
Maven     [OK]        3.9.6 · /opt/homebrew/bin/mvn
Gradle    [OK]        8.10 · /opt/homebrew/bin/gradle
Git       [OK]        2.46 · /opt/homebrew/bin/git

  1. Tiep tuc
  2. Sua duong dan JDK 21
  3. Quay lai
Chon so: 2
Duong dan JAVA_HOME cua JDK 21: /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
[OK] JDK 21: 21.0.4
```

Các bước: (1) tên máy, (2) cấu hình thực nghiệm, (3) JDK/Maven/Gradle/Git, (4) thư mục repository (không dấu cách, không dấu tiếng Việt, ≥ 20 GB trống), (5) gọi model: chế độ LiteLLM proxy, provider và khóa từng model (dùng chung S3), (6) tài nguyên: số luồng build, số luồng gọi model, giới hạn lượt/phút. Cuối cùng in bảng xem lại, hỏi `Luu? (y/n) [y]`, lưu rồi tự chạy S4.

## 5. S3: Model, provider, khóa API, giá

```
Model · pilot-v1.json · sha d5567edc
====================================
Model chay, tham so, gia: cau hinh thuc nghiem (dung chung, commit).
Provider, endpoint, khoa API: litellm.config.local.yaml cua may nay.

Model       Tham so                         Gia USD/1M         Provider (may nay)                    Khoa
gpt-6-luna  reasoning_effort=none, temp=0   vao 0.05, ra 0.25  commercial API · api.teamorouter.com  [OK] ...2a4b

  1. Provider va khoa API cua gpt-6-luna
  2. Gia cua gpt-6-luna
  3. Goi thu gpt-6-luna (1 luot)
  4. Them model vao cau hinh thuc nghiem
  0. Xong
Chon so: 1
Provider:
  1. Endpoint tuong thich OpenAI (commercial API)
  2. Ollama tu host (open-weight model)
  3. Khac (chuoi model LiteLLM)
Chon so [1]:
Ten model o provider [gpt-6-luna]:
Endpoint (api_base) [https://api.teamorouter.com/v1]:
[!] Khoa se hien tren man hinh khi go
Khoa API (Enter de giu ...2a4b): sk-teamo-xxxxxxxxxxxxxxxxxxxx9f1c
[OK] Da luu litellm.config.local.yaml (gpt-6-luna · api.teamorouter.com · khoa ...9f1c)
```

"Gọi thử" in câu trả lời, model id (`[OK]`/`[X]` so với tiền tố), token, thời gian, chi phí. Sửa giá hay danh sách model thì in cảnh báo sha256 đổi và hỏi `Ghi? (y/n) [n]`.

## 6. S4: Kiểm tra môi trường

In từng dòng ngay khi kiểm xong một mục:

```
Kiem tra moi truong
===================
[OK] Cau hinh thuc nghiem      pilot-v1 · 2 manifest khop sha256
[OK] JDK 8 / 11 / 17 / 21      1.8.0_422 / 11.0.24 / 17.0.12 / 21.0.4
[OK] Maven, Gradle, Git        3.9.6 · 8.10 · 2.46
[OK] Thu muc repository        ../repo · 84 GB trong · khong dau cach
[OK] Ket noi GitHub            git ls-remote
[OK] LiteLLM                   1.91.0
[OK] Model gpt-6-luna          openai/gpt-6-luna · api.teamorouter.com
[OK] java-helper               tools/java-helper/java-helper.jar
[X]  PIT                       thieu pitest-testng-plugin
     -> Cach sua: arrow-fw tools install
Trang thai: NOT READY (1 loi)
Sua ngay cac loi tool tu sua duoc? (y/n) [y]:
```

## 7. S5: Chạy và theo dõi

Không vẽ lại màn hình. Pipeline chạy ở luồng nền; luồng chính vừa in sự kiện, vừa chờ lệnh bằng `ReadLine`.

**Xuất:**
- Mỗi sự kiện đáng chú ý một dòng (xong khối 1 của task, kết quả c0, kết quả mỗi vòng sửa, lỗi, INFRA).
- Mỗi 30 giây một dòng tổng.
- Khi run kết thúc: in bảng tổng (giống S6) và gợi ý lệnh tiếp theo.

```
Run pilot-1 · pilot-v1 · Dung-mac · 59 task · khoi 1,2,3,4
Lenh trong khi chay (go roi Enter): p = tam dung, r = tiep tuc, s = dung an toan, t = in tong, h = tro giup

10:40:31 [K1] 402981_... OK · Maven · Java 8 · JUnit 4 · baseline 41 test · tieu chi B PASS
10:40:52 [K2] 618220_... few_shot c0 -> COMPILE · cannot find symbol | method
10:41:07 [K3] 555001_... zero_shot adaptive vong 2 -> ASSERTION · tot hon · checkpoint=2 · muc 1
10:41:30 [TONG] K1 59/59 · K2 171/171 · K3 112/240 · K4 14/90 · INFRA 1 · 612 luot goi · 0.26 USD
10:41:44 [K3] 402981_... few_shot adaptive B3_TIMEOUT lan thu 2/3, chay lai sau 30 s
t
---- Tong (10:41:50) ----
Khoi  Xong / Tong  Ghi chu
K1    59 / 59      EXCLUDED 2
K2    171 / 171    khong hop le 4
K3    112 / 240    chain xong 61/128 (Fixed 27, Adaptive 33 thanh cong)
K4    14 / 90      EvoSuite 6/57
Dang chay: 4/4 build · 2/2 goi model
c0: VALID 31 · COMPILE 74 · UNKNOWN 2 · DISCOVERY 5 · RUNTIME 22 · ASSERTION 31 · REGRESSION 2
-------------------------
```

**Nhập** (luồng chính lặp `ReadLine()` tới khi run kết thúc):

| Gõ | Việc |
| --- | --- |
| `p` | Tạm dừng: không bắt đầu bước mới; bước đang chạy chạy nốt. In `[!] Da tam dung` |
| `r` | Tiếp tục |
| `s` | Dừng an toàn: hỏi `Dung han? (y/n) [n]`; có thì hủy bước đang chạy, giữ mọi job đã ghi ledger; chạy lại cùng lệnh thì tiếp tục |
| `t` | In bảng tổng như trên |
| `h` | In danh sách lệnh |
| Ctrl+C | Như `s` nhưng không hỏi lại |

Run tự **tạm dừng** khi gặp lỗi cấu hình model (khóa sai, hết tiền, sai model id): in `[X] TAM DUNG: <lý do>` và cách sửa (chạy `arrow-fw models` ở terminal khác), sửa xong gõ `r`.

Khi output bị chuyển hướng ra file (`Console.IsOutputRedirected`) hoặc input không phải bàn phím (`Console.IsInputRedirected`), S5 không chờ `ReadLine`, chỉ in dòng sự kiện và dòng tổng.

## 8. S6: Trạng thái một run

Lấy trạng thái đã lưu qua tầng Service, in bảng tổng như lệnh `t` của S5, thêm dòng `Con song: co (heartbeat 3 s truoc)` hoặc `khong`. Điều khiển run đang chạy từ terminal khác: `arrow-fw control --run-id R pause|resume|stop`.

## 9. S7: Chi tiết một task

```
Task 555001_ab12cd34ef56ab78 · com.acme.shop.OrderService · classes2test
=========================================================================
Khoi 1  [OK] Maven 3.9.6 · build root . (reactor, -pl shop-core -am) · Java 8 (JDK 8) · JUnit 4 + Mockito
        baseline: 41 test pass ca 2 lan, 1 flaky · tieu chi B: PASS

Khoi 2 + 3 (gpt-6-luna)
Quy trinh                c0       Fixed                   Adaptive
zero_shot                COMPILE  [X] 5 vong · ASSERTION  [OK] vong 5 · 96 s
zero_shot_project_aware  VALID    -                       -
few_shot                 RUNTIME  [OK] vong 2 · 31 s      [OK] vong 1 · 14 s

Xem tung vong cua chain nao?
  1. zero_shot · fixed
  2. zero_shot · adaptive
  3. few_shot · fixed
  4. few_shot · adaptive
  0. Bo qua
Chon so: 2

Vong  Muc  Ket qua                                  So voi checkpoint  Quyet dinh
c0    -    COMPILE · cannot find symbol | method    -                  checkpoint = c0
1     1    COMPILE · constructor cannot be applied  ngang              giu muc
2     1    ma trung                                 -                  len muc 2
3     2    ASSERTION 4/5                            tot hon            checkpoint = 3, ve muc 1
4     1    ASSERTION 4/5 (lap loi)                  ngang              len muc 2
5     2    VALID                                    -                  thanh cong
File tung vong: runs/pilot-1/tasks/classes2test/555001_.../block3/gpt-6-luna/zero_shot/adaptive/round_<k>/

Khoi 4
Bo test                                       line  branch  mutation  smell
gpt-6-luna__zero_shot_project_aware__initial  0.72  0.55    0.59      0.80
gpt-6-luna__zero_shot__adaptive               0.68  0.50    0.52      1.00
evosuite                                      0.86  0.70    0.48      1.40
```

## 10. S8: Báo cáo

Gọi tầng Service tạo báo cáo (spec 04 Mục 6), rồi in tóm tắt; mỗi bảng tách theo model và dataset:

```
Bao cao · pilot-1 · da ghi runs/pilot-1/reports/
================================================
RQ1 · Verification (classes2test · gpt-6-luna)
Quy trinh                n   Compile  Discovery  Execution  Target  VALID
zero_shot                50  34%      34%        30%        16%     14%
zero_shot_project_aware  50  62%      60%        54%        40%     38%
few_shot                 50  46%      44%        40%        22%     20%

RQ2 · Repair (classes2test · gpt-6-luna) · 64 candidate
Fixed     thanh cong 42% · vong TB 7.1 · thoi gian TB 58 s
Adaptive  thanh cong 52% · vong TB 6.4 · thoi gian TB 51 s

Pilot · chon N: S(k) = 11 18 22 25 26 26 26 ... -> N = 7

RQ3 · Quality (classes2test · gpt-6-luna) · ARROW vs EvoSuite, ghep cap theo tung chi so
Chi so    n   ARROW  EvoSuite
line      29  0.71   0.83
branch    25  0.52   0.69
mutation  28  0.55   0.47
smell     30  0.90   1.30

Can chay lai rieng: 1 job -> arrow-fw rerun-list --run-id pilot-1
```

(Số liệu minh họa.)

## 11. S9: Cần chạy lại riêng

```
Can chay lai rieng · pilot-1
============================
#  Task        Khoi  Job                           Loi         Lan thu  Lap lai?
1  402981_...  3     gpt-6-luna/few_shot/adaptive  B3_TIMEOUT  3        khong

Lenh: arrow-fw run --run-id pilot-1 --tasks 402981_... --rerun-infra
  1. Chay lai task so 1
  2. Chay lai tat ca
  0. Quay lai
Chon so:
```

## 12. S10: Sàng lọc CUT

Chạy chỉ khối 1 cho mọi CUT của manifest (theo dõi như S5, chỉ có dòng `[K1]`), kết thúc in:

```
Sang loc · classes2test_200_seed42.jsonl · run screen-200
=========================================================
Ket qua                CUT
PASS (dat tieu chi B)  171
NO_TEST_RUN            11
FAILING                6
FLAKY                  3
EXCLUDED (build...)    7
INFRA (can chay lai)   2
Da ghi runs/screen-200/reports/screening.csv
Buoc tiep theo (ngoai framework): preflight_tool/scripts/select_pilot_after_s0.py ... de thay CUT khong dat
```

(Số liệu minh họa.)
