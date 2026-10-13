# 06. Màn hình console

**Mục đích:** mô tả từng màn hình của `arrow-fw` (C# console, dùng thư viện Spectre.Console để vẽ bảng, khung, thanh tiến độ), lệnh mở màn hình, phím tắt, và chế độ chữ thuần (`--plain`) cho log/CI.

Nguyên tắc chung:
- Mỗi màn hình có một lệnh tương ứng, chạy được không cần menu (để viết script). Gõ `arrow-fw` không kèm lệnh thì mở **menu chính**.
- Chữ tiếng Việt, thuật ngữ giữ tiếng Anh (VALID, COMPILE, chain…). Màu: xanh lá = đạt, đỏ = lỗi, vàng = cảnh báo/INFRA, xám = phụ.
- Không bao giờ hiện khóa API; chỉ hiện 4 ký tự cuối (`…2a4b`) và tên máy chủ của endpoint.
- Màn hình chỉ **đọc** dữ liệu của run (`status.json`, ledger, file kết quả); mọi thay đổi đều qua lệnh rõ ràng.

| # | Màn hình | Lệnh | Mục |
| --- | --- | --- | --- |
| S1 | Menu chính | `arrow-fw` | 1 |
| S2 | Cài đặt máy | `arrow-fw setup` | 2 |
| S3 | Model, provider, khóa API, giá | `arrow-fw models` | 3 |
| S4 | Kiểm tra môi trường | `arrow-fw doctor` | 4 |
| S5 | Chạy (màn hình theo dõi trực tiếp) | `arrow-fw run …` | 5 |
| S6 | Trạng thái một run | `arrow-fw status --run-id R` | 6 |
| S7 | Chi tiết một task | `arrow-fw show --run-id R --task T` | 7 |
| S8 | Báo cáo | `arrow-fw report --run-id R` | 8 |
| S9 | Cần chạy lại riêng | `arrow-fw rerun-list --run-id R` | 9 |
| S10 | Sàng lọc CUT (tiêu chí B) | `arrow-fw screen --manifest F --run-id R` | 10 |

## 1. S1: Menu chính

```
    _    ____  ____   _____        __   Framework
   / \  |  _ \|  _ \ / _ \ \      / /   v0.1 · .NET 8 · osx-arm64
  / _ \ | |_) | |_) | | | \ \ /\ / /
 / ___ \|  _ <|  _ <| |_| |\ V  V /
/_/   \_\_| \_\_| \_\\___/  \_/\_/

╭─ Máy này ────────────────────────────────────────────────────────────────────╮
│ Máy        Dung-mac                    Môi trường   ✔ READY (14/10 09:12)    │
│ Thực nghiệm pilot-v1 (sha d5567edc…)   Model        ✔ gpt-6-luna  api.…com   │
│ Thư mục repo ../repo  (84 GB trống)     Task         59 (50 Classes2Test, 9 GitBug) │
╰──────────────────────────────────────────────────────────────────────────────╯
╭─ Run gần đây ────────────────────────────────────────────────────────────────╮
│ pilot-1   ĐANG CHẠY   khối 3  112/240 job   đã chạy 1g12p   INFRA 1          │
│ smoke-1   XONG        4/4 khối              18p             cần chạy lại 0   │
╰──────────────────────────────────────────────────────────────────────────────╯

 Chọn việc (↑/↓, Enter):
 > Chạy thực nghiệm (khối 1 → 4)
   Sàng lọc CUT (chỉ khối 1, tiêu chí B)
   Chạy tiếp run đang dở
   Xem trạng thái run
   Xem chi tiết một task
   Báo cáo
   Cần chạy lại riêng
   ── Cài đặt ──
   Cài đặt máy
   Model, provider, khóa API, giá
   Kiểm tra môi trường
   Thoát
```

- "Chạy thực nghiệm" hỏi: run id (gợi ý `run-<ngày>-<giờ>`), tập task (tất cả / file danh sách / nhập task id), các khối (mặc định 1–4), rồi mở S5.
- Máy chưa có `machine.local.json` → vào thẳng S2.

## 2. S2: Cài đặt máy

Từng bước, mỗi bước một khung giải thích "làm gì / vì sao / bạn cần làm gì", có "← Quay lại".

```
╭─ Cài đặt 3/6 · JDK, Maven, Gradle, Git ──────────────────────────────────────╮
│ Bước này làm gì   Tìm JDK 8, 11, 17, 21 và công cụ build                     │
│ Vì sao cần        Mỗi repository build bằng đúng JDK của nó                  │
│ Bạn cần làm gì    Mọi dòng có ✔ thì chọn Tiếp tục                            │
╰──────────────────────────────────────────────────────────────────────────────╯
 ╭──────────┬───┬──────────────────────────────────────────────────╮
 │ Công cụ  │   │ Phiên bản / đường dẫn                             │
 ├──────────┼───┼──────────────────────────────────────────────────┤
 │ JDK 8    │ ✔ │ ../Java-version/runtime/jdk-8/Contents/Home       │
 │ JDK 11   │ ✔ │ …                                                 │
 │ JDK 17   │ ✔ │ …                                                 │
 │ JDK 21   │ ✘ │ không tìm thấy – xem Java-version/README.md       │
 │ Maven    │ ✔ │ 3.9.6 · /opt/homebrew/bin/mvn                     │
 │ Gradle   │ ✔ │ 8.10 · /opt/homebrew/bin/gradle                   │
 │ Git      │ ✔ │ 2.46 · /opt/homebrew/bin/git                      │
 ╰──────────┴───┴──────────────────────────────────────────────────╯
 > Tiếp tục   Sửa đường dẫn JDK 21   ← Quay lại
```

Các bước: (1) tên máy, (2) cấu hình thực nghiệm, (3) JDK/Maven/Gradle/Git, (4) thư mục repository (không dấu cách, không dấu tiếng Việt, ≥ 20 GB trống), (5) gọi model: chế độ proxy LiteLLM + provider và khóa cho từng model (dùng chung S3), (6) tài nguyên: số luồng build, số luồng gọi model (≤ 2 với TeamoRouter), giới hạn lượt/phút. Cuối cùng: bảng xem lại → lưu `machine.local.json` và `litellm.config.local.yaml` → tự chạy S4.

## 3. S3: Model, provider, khóa API, giá

```
╭─ Model · pilot-v1.json · sha d5567edc ───────────────────────────────────────╮
│ Model chạy, tham số, giá: cấu hình thực nghiệm (dùng chung, commit).         │
│ Provider, endpoint, khóa API: litellm.config.local.yaml của máy này.         │
╰──────────────────────────────────────────────────────────────────────────────╯
╭────────────────────────┬──────────────┬──────────────────────┬──────────╮
│ Model · tham số        │ Giá USD/1M   │ Provider (máy này)   │ Khóa API │
├────────────────────────┼──────────────┼──────────────────────┼──────────┤
│ gpt-6-luna             │ vào 0.05     │ commercial API       │ ✔ …2a4b  │
│ reasoning_effort=none, │ ra 0.25      │ openai/gpt-6-luna    │          │
│ temperature=0, …       │              │ api.teamorouter.com  │          │
│ qwen3-coder-30b        │ vào 0        │ open-weight (Ollama) │ không cần│
│ temperature=0, …       │ ra 0         │ localhost            │          │
╰────────────────────────┴──────────────┴──────────────────────┴──────────╯
 > Provider và khóa API của gpt-6-luna
   Giá của gpt-6-luna
   Gọi thử gpt-6-luna (1 lượt)
   …
```

- "Gọi thử" gửi 1 lượt nhỏ, hiện câu trả lời, model id (✔/✘ so với tiền tố), token, thời gian, chi phí theo giá.
- Sửa giá hay danh sách model thì sha256 của cấu hình thực nghiệm đổi: hiện cảnh báo trước khi ghi (run cũ không chạy tiếp được, các máy phải `git pull`).

## 4. S4: Kiểm tra môi trường

```
 Kiểm tra môi trường
╭─────────────────────────────┬───┬──────────────────────────────────────────╮
│ Kiểm tra                    │   │ Chi tiết                                 │
├─────────────────────────────┼───┼──────────────────────────────────────────┤
│ Cấu hình thực nghiệm        │ ✔ │ pilot-v1 · 2 manifest khớp sha256        │
│ JDK 8 / 11 / 17 / 21        │ ✔ │ 1.8.0_422 / 11.0.24 / 17.0.12 / 21.0.4   │
│ Maven, Gradle, Git          │ ✔ │ 3.9.6 · 8.10 · 2.46                      │
│ Thư mục repository          │ ✔ │ ../repo · 84 GB trống · không dấu cách   │
│ Kết nối GitHub              │ ✔ │ git ls-remote github.com/…               │
│ LiteLLM                     │ ✔ │ 1.91.0                                   │
│ Model gpt-6-luna            │ ✔ │ openai/gpt-6-luna · api.teamorouter.com  │
│ java-helper                 │ ✔ │ tools/java-helper/java-helper.jar        │
│ JaCoCo, PIT, tsDetect, EvoSuite │ ✔ │ đủ file, đúng sha256                 │
╰─────────────────────────────┴───┴──────────────────────────────────────────╯
 Trạng thái: READY
```

Dòng ✘ có thêm dòng xám "→ cách sửa" và, nếu tool tự sửa được (cài LiteLLM, tải công cụ khối 4, nhập khóa), hỏi "Sửa ngay?".

## 5. S5: Chạy, màn hình theo dõi trực tiếp

Mở khi `arrow-fw run`. Vẽ lại mỗi 0,5 giây từ trạng thái trong bộ nhớ; đồng thời ghi `status.json` mỗi 2 giây để S6 ở terminal khác đọc được.

```
╭─ pilot-1 · pilot-v1 · Dung-mac ─────────────────────── ĐANG CHẠY · 1g12p ────╮
│ Khối 1 Repository-Aware   ██████████████████████  59/59   EXCLUDED 2  INFRA 0 │
│ Khối 2 Test Generation    ██████████████████████ 171/171  không hợp lệ 4      │
│ Khối 3 Repair             ██████████░░░░░░░░░░░░ 112/240  chain xong 61/128   │
│ Khối 4 Assessment         ███░░░░░░░░░░░░░░░░░░░  14/90   EvoSuite 6/57       │
╰──────────────────────────────────────────────────────────────────────────────╯
╭─ Đang chạy (build 4/4 · model 2/2) ──────────────────────────────────────────╮
│ 555001  gpt-6-luna  zero_shot              adaptive  vòng 3 mức 2  build  0:41 │
│ 555001  gpt-6-luna  zero_shot              fixed     vòng 3        model  0:06 │
│ 618220  gpt-6-luna  few_shot               adaptive  vòng 1 mức 1  build  0:12 │
│ 703114  –           –                      khối 4    PIT                  2:03 │
╰──────────────────────────────────────────────────────────────────────────────╯
╭─ Kết quả tới giờ ────────────────────────────────────────────────────────────╮
│ c0:     VALID 31 · COMPILE 74 · UNKNOWN 2 · DISCOVERY 5 · RUNTIME 22 ·       │
│         ASSERTION 31 · REGRESSION 2 · không hợp lệ 4                         │
│ Chain:  Fixed 27/64 thành công · Adaptive 33/64 thành công                   │
│ Model:  612 lượt (phát lại 41) · 2,1M token vào · 0,6M ra · 0,26 USD         │
│ INFRA:  1 (cần chạy lại riêng) · EXCLUDED: 2                                 │
╰──────────────────────────────────────────────────────────────────────────────╯
╭─ Sự kiện ────────────────────────────────────────────────────────────────────╮
│ 10:41:07  555001 zero_shot adaptive vòng 2 → ASSERTION, tốt hơn, checkpoint  │
│ 10:40:52  618220 few_shot c0 → COMPILE · cannot find symbol | method         │
│ 10:40:31  402981 khối 3 B3_TIMEOUT lần thử 2/3, chạy lại sau 30 s            │
╰──────────────────────────────────────────────────────────────────────────────╯
 P tạm dừng · R tiếp tục · S dừng an toàn · Q thoát màn hình (run vẫn chạy nền)
```

Phím:

| Phím | Việc |
| --- | --- |
| `P` | Tạm dừng: không bắt đầu job mới; job đang chạy chạy nốt bước hiện tại |
| `R` | Tiếp tục |
| `S` hoặc Ctrl+C | Dừng an toàn: hủy job đang chạy, giữ mọi job đã ghi ledger; chạy lại cùng lệnh thì tiếp tục |
| `Q` | Đóng màn hình, run vẫn chạy (xem lại bằng S6) |

Run tự **tạm dừng** khi gặp lỗi cấu hình model (khóa sai, hết tiền, sai model id): khung đỏ hiện lý do và cách sửa (mở S3 ở terminal khác), sửa xong bấm `R`.

**Chế độ chữ thuần** (`--plain`, hoặc khi output không phải terminal): không vẽ khung, mỗi sự kiện một dòng, mỗi 30 giây một dòng tổng:

```
10:41:07 [B3] 555001 gpt-6-luna zero_shot adaptive r2 ASSERTION better checkpoint=r2 level=1
10:41:30 [tổng] B1 59/59 · B2 171/171 · B3 112/240 · B4 14/90 · INFRA 1 · 0,26 USD
```

## 6. S6: Trạng thái một run

Giống khung trên cùng và khung "Kết quả tới giờ" của S5, đọc từ `status.json` (không cần run còn chạy). Có thêm dòng "Còn sống" (heartbeat trong 10 giây gần nhất) và lệnh tạm dừng/tiếp tục/dừng từ terminal khác: `arrow-fw control --run-id R pause|resume|stop`.

## 7. S7: Chi tiết một task

```
╭─ 555001_ab12cd34ef56ab78 · com.acme.shop.OrderService · classes2test ────────╮
│ Khối 1  ✔  Maven 3.9.6 · Java 8 (JDK 8) · JUnit 4 + Mockito · đa module      │
│            baseline: 41 test pass ở cả 2 lần, 1 flaky                         │
╰──────────────────────────────────────────────────────────────────────────────╯
 Khối 2 + 3 (gpt-6-luna)
╭─────────────────────────┬────────────┬─────────────────────────┬────────────────────────╮
│ Quy trình               │ c0         │ Fixed                   │ Adaptive               │
├─────────────────────────┼────────────┼─────────────────────────┼────────────────────────┤
│ zero_shot               │ COMPILE    │ ✘ 5 vòng · ASSERTION    │ ✔ vòng 5 · 96 s        │
│ zero_shot_project_aware │ ✔ VALID    │ –                       │ –                      │
│ few_shot                │ RUNTIME    │ ✔ vòng 2 · 31 s         │ ✔ vòng 1 · 14 s        │
╰─────────────────────────┴────────────┴─────────────────────────┴────────────────────────╯
 Chọn để xem từng vòng: > zero_shot · adaptive
╭────┬──────┬─────────────────────────┬────────────┬───────────────────────────────╮
│ V  │ Mức  │ Kết quả                 │ So với CP  │ Quyết định                    │
├────┼──────┼─────────────────────────┼────────────┼───────────────────────────────┤
│ c0 │      │ COMPILE · cannot find symbol | method │  │ checkpoint = c0             │
│ 1  │ 1    │ COMPILE · constructor cannot be applied │ ngang │ giữ mức              │
│ 2  │ 1    │ mã trùng                │ –          │ lên mức 2                     │
│ 3  │ 2    │ ASSERTION 4/5           │ tốt hơn    │ checkpoint = 3, về mức 1      │
│ 4  │ 1    │ ASSERTION 4/5 (lặp lỗi) │ ngang      │ lên mức 2                     │
│ 5  │ 2    │ VALID                   │ –          │ thành công                    │
╰────┴──────┴─────────────────────────┴────────────┴───────────────────────────────╯
 Khối 4: zero_shot_project_aware__initial  line 0.72 · branch 0.55 · mutation 0.59 · smell 0.8
         zero_shot__adaptive               line 0.68 · branch 0.50 · mutation 0.52 · smell 1.0
         evosuite                          line 0.86 · branch 0.70 · mutation 0.48 · smell 1.4
 Enter trên một vòng: mở prompt, câu trả lời, log build của vòng đó (đường dẫn file)
```

## 8. S8: Báo cáo

Chạy khối 4 phần báo cáo (spec 04 Mục 6), ghi `reports/`, rồi hiện tóm tắt; mỗi bảng tách theo **model** và **dataset** (Q3):

```
 Báo cáo · pilot-1 · ghi vào runs/pilot-1/reports/
 RQ1 · Verification (classes2test · gpt-6-luna)
╭─────────────────────────┬────┬─────────┬───────────┬───────────┬───────────┬────────╮
│ Quy trình               │ n  │ Compile │ Discovery │ Execution │ Target    │ VALID  │
├─────────────────────────┼────┼─────────┼───────────┼───────────┼───────────┼────────┤
│ zero_shot               │ 50 │ 34%     │ 34%       │ 30%       │ 16%       │ 14%    │
│ zero_shot_project_aware │ 50 │ 62%     │ 60%       │ 54%       │ 40%       │ 38%    │
│ few_shot                │ 50 │ 46%     │ 44%       │ 40%       │ 22%       │ 20%    │
╰─────────────────────────┴────┴─────────┴───────────┴───────────┴───────────┴────────╯
 RQ2 · Repair (classes2test · gpt-6-luna) · 64 candidate
   Fixed     thành công 42% · vòng TB 7.1 · thời gian TB 58 s
   Adaptive  thành công 52% · vòng TB 6.4 · thời gian TB 51 s
 Pilot · chọn N: S(k) = 11 18 22 25 26 26 26 … → N = 7
 RQ3 · Quality (classes2test · gpt-6-luna) · ARROW vs EvoSuite, ghép cặp theo từng chỉ số
   line 0.71 vs 0.83 (n=29) · branch 0.52 vs 0.69 (n=25) · mutation 0.55 vs 0.47 (n=28) · smell 0.9 vs 1.3 (n=30)
 Cần chạy lại riêng: 1 job → arrow-fw rerun-list --run-id pilot-1
```

(Số liệu minh họa.)

## 9. S9: Cần chạy lại riêng

```
╭──────────────┬──────┬──────────────────────────────┬──────────────┬────────┬─────────────────╮
│ Task         │ Khối │ Job                          │ Lỗi          │ Lần thử│ Lặp lại?        │
├──────────────┼──────┼──────────────────────────────┼──────────────┼────────┼─────────────────┤
│ 402981_…     │ 3    │ gpt-6-luna/few_shot/adaptive │ B3_TIMEOUT   │ 3      │ không           │
╰──────────────┴──────┴──────────────────────────────┴──────────────┴────────┴─────────────────╯
 Lệnh: arrow-fw run --run-id pilot-1 --tasks 402981_… --rerun-infra
 > Chạy lại ngay task đã chọn   Chạy lại tất cả   ← Quay lại
```

## 10. S10: Sàng lọc CUT

Chạy **chỉ khối 1** (tải repository, build, baseline ×2) cho mọi CUT của một manifest, để script chọn mẫu biết CUT nào đạt tiêu chí B (spec 01 Mục 6.3). Màn hình theo dõi giống S5 nhưng chỉ có thanh khối 1; kết thúc in bảng tóm tắt và ghi `reports/screening.csv`.

```
 Sàng lọc · classes2test_200_seed42.jsonl · run screen-200
╭───────────────────────┬─────╮
│ Kết quả               │ CUT │
├───────────────────────┼─────┤
│ PASS (đạt tiêu chí B) │ 171 │
│ NO_TEST_RUN           │  11 │
│ FAILING               │   6 │
│ FLAKY                 │   3 │
│ EXCLUDED (build…)     │   7 │
│ INFRA (cần chạy lại)  │   2 │
╰───────────────────────┴─────╯
 Đã ghi runs/screen-200/reports/screening.csv
 Bước tiếp theo (ngoài framework): preflight_tool/scripts/select_pilot_after_s0.py … để thay CUT không đạt
```

(Số liệu minh họa.)
