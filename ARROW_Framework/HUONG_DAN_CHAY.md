# Hướng dẫn chạy ARROW Framework (pilot-6)

Hướng dẫn cho từng thành viên chạy phần pilot được giao trên máy của mình. Mọi lệnh dưới đây chạy từ thư mục `ARROW/ARROW_Framework` (trừ khi ghi khác). `arrow …` trong spec và trong menu trợ giúp là cách viết tắt của `dotnet run --project src/Arrow.UI -- …`.

## 1. Phân công

| Máy | Run id | Shard | File kết quả |
| --- | --- | --- | --- |
| Dung-mac | `pilot-6-Dung-mac` | `shard-01.json` | `shard-01-Dung-mac.zip` |
| Dung-win | `pilot-6-Dung-win` | `shard-02.json` | `shard-02-Dung-win.zip` |
| KieuAnh | `pilot-6-KieuAnh` | `shard-03.json` | `shard-03-KieuAnh.zip` |
| Quang | `pilot-6-Quang` | `shard-04.json` | `shard-04-Quang.zip` |
| Chinh | `pilot-6-Chinh` | `shard-05.json` | `shard-05-Chinh.zip` |
| Han | `pilot-6-Han` | `shard-06.json` | `shard-06-Han.zip` |

Mỗi shard khoảng 10 task; trên Mac M-series một shard chạy khoảng 20 phút và tốn khoảng 0,05 USD.

## 2. Cài đặt lần đầu

Chỉ làm một lần trên mỗi máy. Máy đã chạy pilot trước đây thì bỏ qua, sang Mục 3.

| Cần có | Ghi chú |
| --- | --- |
| .NET SDK 8 trở lên | `dotnet --version` |
| Git, Maven | Gradle chỉ cần khi repository không có `gradlew` |
| JDK 8, 11, 17, 21 | Theo `ARROW/Java-version/README.md`, cài vào `ARROW/Java-version/runtime/jdk-8 …` |
| Python 3.9 trở lên | Để cài LiteLLM |

**Thư mục ARROW không được có dấu cách hoặc dấu tiếng Việt** trong đường dẫn (một số công cụ build sẽ lỗi).

**Cài LiteLLM** vào môi trường Python riêng `ARROW/litellm` (framework tự tìm ở đây), từ thư mục `ARROW`:

```text
macOS:    python3 -m venv litellm && litellm/bin/pip install "litellm[proxy]"
Windows:  py -3 -m venv litellm ; litellm\Scripts\pip install "litellm[proxy]"
```

**Build:**

```text
dotnet build ArrowFramework.sln
```

**Cài đặt máy** (6 bước, có giải thích trên màn hình):

```text
dotnet run --project src/Arrow.UI -- setup
```

| Bước | Nhập |
| --- | --- |
| 1. Tên máy | Đúng tên ở cột "Máy" của Mục 1 (ví dụ `Quang`) |
| 2. Cấu hình thực nghiệm | Tiếp tục |
| 3. JDK, Maven, Gradle, Git | Mọi dòng `[OK]` thì Tiếp tục; dòng nào thiếu thì nhập đường dẫn |
| 4. Thư mục repository | Giữ mặc định (`ARROW/repo`); repository của các task được tải vào đây nên cần đủ chỗ trống |
| 5. Gọi model | Chọn `auto_start`; khai báo model ngay (xem dưới) |
| 6. Tài nguyên | Giữ mặc định |

**Khai báo model** (ở bước 5, hoặc chạy riêng `dotnet run --project src/Arrow.UI -- models --experiment experiments/pilot-v1.json`), cho model `gpt-6-luna`:

| Hỏi | Nhập |
| --- | --- |
| Provider | Endpoint tương thích OpenAI |
| Tên model ở provider | `gpt-6-luna` |
| Endpoint (api_base) | Hỏi Dung (không ghi vào file dùng chung) |
| Khóa API | Hỏi Dung; chỉ lưu trong `litellm.config.local.yaml` của máy, **không commit** |
| Giá token vào / ra | `0.05` / `0.25` |

**Tải công cụ khối 4** (JaCoCo, PIT, tsDetect, EvoSuite):

```text
dotnet run --project src/Arrow.UI -- tools install
```

## 3. Trước mỗi lần chạy

```text
git pull                                     (trong thư mục ARROW)
dotnet run --project src/Arrow.UI -- doctor
```

`doctor` phải kết thúc bằng `Trạng thái: READY`. Dòng `[FAIL]` nào thì làm theo gợi ý in kèm.

Máy nào **đã chạy pilot bằng bản cũ** (trước commit `67c41ba`, `max_tokens` 8192) thì đổi tên hoặc xóa thư mục `runs/pilot-6-<máy>/` trước, vì cấu hình đã đổi sha256 nên run cũ không chạy tiếp được.

## 4. Chạy pilot

**Cách 1: menu**

```text
dotnet run --project src/Arrow.UI
```

| Màn hình hỏi | Chọn |
| --- | --- |
| Chọn việc | `1` Chạy thực nghiệm (khối 1 -> 4) |
| Cấu hình thực nghiệm | `pilot-v1.json` |
| Run id | `pilot-6-<máy>` (Mục 1) |
| Tập task | `2` File phân công máy |
| Shard của máy này | `pilot-6/shard-NN.json` của máy mình (Mục 1) |
| Các khối | Enter (mặc định `1,2,3,4`) |
| Bắt đầu? | Enter |

**Cách 2: một dòng lệnh** (thay `<máy>` và `NN`):

```text
dotnet run --project src/Arrow.UI -- run --experiment experiments/pilot-v1.json --run-id pilot-6-<máy> --shard ../shards/pilot-6/shard-NN.json
```

Hai cách cho kết quả như nhau. Framework từ chối chạy nếu file shard không khớp cấu hình (thường do chưa pull).

## 5. Khi đang chạy

| Việc | Cách làm |
| --- | --- |
| Xem tiến độ | Màn hình tự in dòng `[TỔNG]` mỗi 30 giây; terminal khác: `-- status --run-id pilot-6-<máy>` |
| Tạm dừng / chạy lại / dừng | Terminal khác: `-- control --run-id pilot-6-<máy> pause` (hoặc `resume`, `stop`) |
| Máy tắt, mất mạng, lỡ đóng terminal | Menu `2` Chạy tiếp run đang dở → chọn run; hoặc chạy lại đúng lệnh ở Mục 4. Task đã xong được bỏ qua, vẫn đúng shard |
| Có job INFRA (timeout, lỗi mạng) | `-- rerun-list --run-id pilot-6-<máy>` in lệnh chạy lại cho từng task |

Run xong khi màn hình in `[OK] Run pilot-6-<máy> xong`.

## 6. Kiểm tra và gửi kết quả

```text
dotnet run --project src/Arrow.UI -- report --run-id pilot-6-<máy>
dotnet run --project src/Arrow.UI -- rerun-list --run-id pilot-6-<máy>
```

`rerun-list` phải báo `Không có job nào cần chạy lại`; còn job thì chạy lại theo lệnh nó in ra trước khi gửi.

Nén thư mục run vào `ARROW/pilot_results/pilot-6/` (tên file theo Mục 1), rồi commit và push **chỉ file zip đó**:

```text
macOS (trong ARROW_Framework/runs):
  zip -rq ../../pilot_results/pilot-6/shard-NN-<máy>.zip pilot-6-<máy>

Windows PowerShell (trong ARROW_Framework):
  Compress-Archive -Path runs\pilot-6-<máy> -DestinationPath ..\pilot_results\pilot-6\shard-NN-<máy>.zip -Force
```

Thư mục run không chứa khóa API (chỉ ghi tên host của endpoint). Không commit `machine.local.json`, `litellm.config.local.yaml`, `runs/`, `logs/`.

## 7. Người tổng hợp

Giải nén 6 file zip vào `ARROW_Framework/runs/`, rồi:

```text
dotnet run --project src/Arrow.UI -- report --run-id pilot-6-Dung-mac --run-id pilot-6-Dung-win --run-id pilot-6-KieuAnh --run-id pilot-6-Quang --run-id pilot-6-Chinh --run-id pilot-6-Han
```

Báo cáo gộp ghi vào `runs/merged-…/reports/`; dòng `Pilot · chọn N` cho số vòng sửa N dùng ở thực nghiệm chính.
