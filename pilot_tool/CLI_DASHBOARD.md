# Dashboard trong terminal

**Trạng thái:** thiết kế (12/10/2026). Mọi thứ chạy trong terminal, không có web. Số liệu trong các màn hình dưới đây là ví dụ minh họa, không phải kết quả.

## 1. Các lệnh

| Lệnh | Làm gì | Có dashboard trực tiếp |
| --- | --- | --- |
| `run --manifest <file> --run-id <id>` | Chạy lần lượt S0 → S5 | ✅ |
| `prepare` / `generate` / `repair` / `evosuite` / `quality` | Chạy một bước | ✅ |
| `report --run-id <id>` | Xuất `pilot_report.md` và các CSV | In tóm tắt |
| `runs` | Liệt kê mọi run trong `runs/` | Bảng |
| `status --run-id <id>` | Tổng quan một run (đang chạy hay đã xong) | Cùng màn hình với `run`, chỉ xem |
| `show --run-id <id> --task <task_id>` | Chi tiết một task: 5 bước xác minh, các vòng sửa, đường dẫn file | Bảng |
| `control --run-id <id> pause\|resume\|stop` | Điều khiển run đang chạy từ terminal khác | Không |
| `arrow` (không kèm lệnh), `setup`, `doctor`, `shard plan`, `export`, `merge` | Menu chính, cài đặt máy, kiểm tra môi trường, chia shard, gom kết quả | Xem `SETUP.md` |

Lệnh đầy đủ có dạng `dotnet run --project src/Arrow.Cli -- <lệnh> ...`; sau khi `dotnet publish` thì gọi thẳng `arrow <lệnh> ...`.

## 2. Màn hình khi đang chạy (`run`, bước sinh test)

```
╭─ ARROW pilot ──────────────────────────────────────────────────────────────────────────╮
│ Run: pilot-c2t-20261015    Dataset: Classes2Test (50 CUT)    Model: gpt-6-luna         │
│ Trạng thái: ● RUNNING      Đã chạy: 01:42:10    Tạm dừng: 00:05:12    Máy: macOS arm64 │
╰────────────────────────────────────────────────────────────────────────────────────────╯
╭─ Tiến độ ──────────────────────────────────────────────────────────────────────────────╮
│ S0 Chuẩn bị      ██████████████████████████████  50/50   ✔  (lỗi hạ tầng: 1)            │
│ S1 Sinh test     ██████████████████░░░░░░░░░░░░  98/150  65%   còn khoảng 00:51:00      │
│ S2 Sửa test      ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   chờ                                    │
│ S3 EvoSuite      ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   chờ                                    │
│ S4 Chất lượng    ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   chờ                                    │
╰────────────────────────────────────────────────────────────────────────────────────────╯
╭─ Kết quả xác minh (98 ứng viên) ─────────────╮╭─ Đang chạy ─────────────────────────────╮
│ VALID       ███████████         31           ││ LLM-1  26722690_810c…  few-shot   9s     │
│ REGRESSION  █                    2           ││ LLM-2  91552889_eaa2…  zero-shot  3s     │
│ ASSERTION   █████               14           ││ BLD-1  51700398_fc0e…  zs-aware   [test] │
│ RUNTIME     ████                11           ││ BLD-2  57073755_c72c…  few-shot  [compile]│
│ DISCOVERY   █                    3           ││                                          │
│ UNKNOWN                          0           │╰──────────────────────────────────────────╯
│ COMPILE     ████████████        37           │╭─ Chi phí và thời gian ──────────────────╮
│ INFRA                            0           ││ Token: 412k vào / 188k ra   $3.41        │
╰──────────────────────────────────────────────╯│ LLM trung vị 9s · Build trung vị 38s     │
                                                 │ Đĩa: workspace 2.1 GB · runs/ 180 MB     │
                                                 ╰──────────────────────────────────────────╯
╭─ Sự kiện gần nhất ─────────────────────────────────────────────────────────────────────╮
│ 01:41:55  26722690_810c…  few-shot   → COMPILE   cannot find symbol: method getTotal    │
│ 01:41:20  91552889_eaa2…  zero-shot  → VALID     3/3 test method pass                   │
│ 01:40:58  64226557_9473…  zs-aware   → INFRA     build timeout 900s (sẽ thử lại 1 lần)  │
╰────────────────────────────────────────────────────────────────────────────────────────╯
 [P] Tạm dừng   [R] Tiếp tục   [S] Dừng hẳn   (Ctrl+C = S)
```

- Màu: VALID xanh lá; REGRESSION, ASSERTION, RUNTIME vàng; DISCOVERY, UNKNOWN, COMPILE đỏ; INFRA xám.
- "Tạm dừng" trong khung đầu là tổng thời gian đã tạm dừng; thời gian này không tính vào số liệu.
- "Còn khoảng" ước tính từ tốc độ các job đã xong.

## 3. Khi tạm dừng

```
╭─ ARROW pilot ──────────────────────────────────────────────────────────────────────────╮
│ Run: pilot-c2t-20261015    Trạng thái: ⏸ PAUSING – đang chờ 2 bước đang chạy xong       │
╰────────────────────────────────────────────────────────────────────────────────────────╯
            ...
 [R] Tiếp tục   [S] Dừng hẳn
```

Sau khi các bước đang chạy xong, trạng thái chuyển sang `⏸ PAUSED`. Bấm `S` khi đang tạm dừng thì thoát ngay (không còn bước nào dở). Khi bấm `S` lúc đang chạy, trạng thái là `■ STOPPING` cho tới khi các bước đang chạy xong, rồi chương trình thoát và in lệnh để chạy tiếp:

```
Đã dừng run pilot-c2t-20261015 (98/150 job của S1 đã xong).
Chạy tiếp: dotnet run --project src/Arrow.Cli -- run --run-id pilot-c2t-20261015
```

## 4. Màn hình bước sửa test (S2, chỉ Classes2Test)

```
╭─ Tiến độ ──────────────────────────────────────────────────────────────────────────────╮
│ S2 Sửa test   ███████████░░░░░░░░░░░░░░░░░░░  ứng viên 34/80 · vòng đã chạy 412/1600   │
╰────────────────────────────────────────────────────────────────────────────────────────╯
╭─ Sửa thành công cộng dồn theo vòng (chung hai cơ chế, chỉ ứng viên đã chạy hết) ───────╮
│ vòng  1  ████████████████████              40                                          │
│ vòng  2  █████████████████████████████     58                                          │
│ vòng  3  █████████████████████████████████ 66                                          │
│ vòng  4  ...                                                                           │
│ N theo quy tắc: chưa tính (cần chạy xong mọi ứng viên)                                 │
╰────────────────────────────────────────────────────────────────────────────────────────╯
```

- Với run có tiền tố `pilot-`, màn hình **chỉ** hiển thị số liệu chung hai cơ chế. Không có cột hay bảng nào tách Fixed với Adaptive, kể cả số lần rollback hay mã trùng (đúng quy định pilot không xem so sánh hai cơ chế; số liệu tách vẫn nằm trong log).
- N chỉ được tính và hiển thị khi mọi ứng viên đã chạy xong 10 vòng hoặc đạt VALID.

## 5. Danh sách run (`runs`)

```
 Run                          Dataset        CUT   Bước hiện tại   Trạng thái   Bắt đầu            Đã chạy
 pilot-c2t-20261015           Classes2Test    50   S1 98/150       RUNNING      15/10 09:12        01:42:10
 pilot-gitbug-20261015        GitBug-Java      9   S4 xong         DONE         15/10 08:01        00:47:33
 smoke-20261014               Classes2Test     2   S1 6/6          DONE         14/10 21:40        00:03:05
```

## 6. Chi tiết một task (`show --task`)

```
Task 51700398_fc0e38f96f8a8eb5   ymanvieu/trading @ 88d9ce4a   Maven · JDK 17 · JUnit 5
CUT:   trading-common/src/main/java/fr/ymanvieu/trading/common/provider/PairService.java
Test:  trading-common/src/test/java/fr/ymanvieu/trading/common/provider/PairService_ArrowTest.java
Baseline: 214 test, 0 fail sẵn, 1 flaky (bỏ qua khi xét regression)

 Model        Prompt      Compile  Discovery  Execution  Pass   Regression  Trạng thái
 gpt-6-luna   zero-shot   ✔        ✔          ✔          ✘ 2/5  –           ASSERTION
 gpt-6-luna   few-shot    ✘        –          –          –      –           COMPILE
 gpt-6-luna   zs-aware    ✔        ✔          ✔          ✔ 5/5  ✔           VALID

Sửa test (zero-shot, chung hai cơ chế trong pilot: chỉ liệt kê vòng, không tổng hợp so sánh)
 Vòng  Cơ chế    Trạng thái   Mức  Sửa từ  Rollback  Mã trùng  LLM   Build
 1     fixed     RUNTIME      1    0       –         –         8s    41s
 1     adaptive  ASSERTION    1    0       –         –         9s    40s
 ...

File: runs/pilot-c2t-20261015/tasks/classes2test/51700398_fc0e38f96f8a8eb5/
```

Đường dẫn ở dòng cuối để mở prompt, response, mã test và log build trong VS Code.

## 7. Chế độ không vẽ màn hình (`--plain`)

Khi chạy nền, chuyển hướng output ra file, hoặc thêm `--plain`, CLI không vẽ khung mà in từng dòng sự kiện:

```
2026-10-15T09:13:02Z S1 26722690_810cb7e8a826af52 gpt-6-luna few-shot COMPILE llm=9.1s build=38.4s
2026-10-15T09:13:40Z S1 91552889_eaa279c028d6eb1f gpt-6-luna zero-shot VALID llm=7.8s build=52.0s
```

Phím tắt không dùng được ở chế độ này; điều khiển bằng lệnh `control` từ terminal khác.

## 8. Phím tắt

| Phím | Màn hình | Tác dụng |
| --- | --- | --- |
| `P` | `run` | Tạm dừng: làm nốt bước đang chạy rồi chờ |
| `R` | `run` | Tiếp tục |
| `S`, `Ctrl+C` | `run` | Dừng hẳn: làm nốt bước đang chạy rồi thoát |
| `Q` | `status` | Thoát màn hình xem (không ảnh hưởng run) |

## 9. Yêu cầu terminal

- macOS: Terminal hoặc iTerm2. Windows: Windows Terminal hoặc terminal trong VS Code (cửa sổ cmd cũ có thể hiển thị sai ký tự khung).
- Rộng tối thiểu 100 cột, cao 30 dòng; nhỏ hơn thì tự chuyển sang bố cục gọn (bỏ khung "Đang chạy" và "Sự kiện gần nhất").
- Đóng cửa sổ terminal thì run dừng; chạy lại cùng `run_id` để tiếp tục.
