# Hướng dẫn chạy pilot cho từng máy

Pilot chia 59 CUT cho 6 máy (`shards/pilot-6/`). Mỗi người chạy đúng phần của máy mình, xuất kết quả rồi đẩy lên GitHub để người điều phối (Dũng-mac) gom lại.

| Shard | Người | Số CUT |
| --- | --- | ---: |
| shard-01 | Dũng-mac | 9 |
| shard-02 | Dũng-win | 10 |
| shard-03 | Kiều Anh | 10 |
| shard-04 | Quang | 10 |
| shard-05 | Chinh | 10 |
| shard-06 | Hán | 10 |

Thời gian chạy khoảng 1–2 giờ mỗi máy. Tiền model khoảng 0,1 USD.

Các lệnh bên dưới chạy trong thư mục `ARROW/pilot_tool`. Khi chép lệnh, không chép phần ghi chú phía sau lệnh.

## 1. Cần có trên máy

- Git và .NET SDK 8 trở lên (`dotnet --version`).
- Python 3.10 trở lên, dùng để chạy LiteLLM. macOS: `brew install python@3.12`. Windows: tải từ python.org và tick "Add to PATH".
- JDK 8, 11, 17, 21: cài theo `Java-version/README.md`.
- Maven và Gradle: chỉ cần khi repository không có sẵn `mvnw`/`gradlew`. Nên cài.
- Khóa API TeamoRouter của riêng bạn.
- Ổ đĩa còn trống ít nhất 20 GB. Đường dẫn ARROW không có dấu cách và không có chữ có dấu.

## 2. Lấy code mới nhất và cài một lần

```
git pull
sh install/install-arrow.sh
sh install/install-litellm.sh
arrow tools install
```

Windows dùng các lệnh sau thay cho hai lệnh `sh`:

```
powershell -ExecutionPolicy Bypass -File install\install-arrow.ps1
powershell -ExecutionPolicy Bypass -File install\install-litellm.ps1
```

Sau khi cài lệnh `arrow`, mở terminal mới.

## 3. Cài đặt máy: `arrow setup`

Gõ `arrow`. Lần đầu, tool tự vào phần cài đặt 8 bước. Máy đã cài trước đây thì chọn "Cài đặt máy".

1. **Máy này**: chọn tên của bạn.
2. **Shard**: giữ shard tool đề xuất.
3. **Thư mục** và 4. **Repository**: nhấn Enter để giữ mặc định. Ở câu hỏi giữ workspace của job lỗi, trả lời "không".
5. **JDK**: cả 4 dòng JDK phải có ✔.
6. **Gọi model**:
   - Chọn "Tool tự bật LiteLLM proxy".
   - Với `gpt-6-luna`, chọn provider "Endpoint tương thích OpenAI".
   - Tên model: `gpt-6-luna`.
   - Endpoint: địa chỉ TeamoRouter của nhóm (hỏi Dũng).
   - Dán khóa API của bạn vào.
7. **Tài nguyên**: số luồng build để theo gợi ý, **số luồng gọi model = 2** (nhiều hơn thì TeamoRouter từ chối).
8. **Xem lại**: đúng thì chọn y.

Khóa API chỉ lưu trong `litellm.config.local.yaml` trên máy bạn, không bao giờ lên Git.

## 4. Kiểm tra trước khi chạy

```
arrow doctor
arrow models
```

- `arrow doctor` phải báo **READY**. Dòng nào có ✘ thì làm theo gợi ý ngay trên dòng đó.
- Trong `arrow models`, chọn "Gọi thử gpt-6-luna". Kết quả phải là "✔ Gọi được", model id `gpt-6-luna`.
- Không thay đổi model, tham số hay giá trong `arrow models`. Những thứ đó dùng chung cả nhóm, sửa sẽ làm hỏng phân công shard.

Nếu muốn chạy thử cả pipeline với ít lượt gọi: menu `arrow` → "Chạy smoke với model thật".

## 5. Chạy shard

Trong menu `arrow`, chọn **"Chạy shard của tôi"**, hoặc gõ:

```
arrow run
```

- Dashboard hiện tiến độ các bước S0 → S5. Bấm **P** để tạm dừng, **R** để tiếp tục, **S** để dừng hẳn.
- Bị tắt giữa chừng (mất điện, đóng terminal): gõ lại `arrow run`, tool chạy tiếp từ chỗ dừng, không làm lại phần đã xong.
- **Giữ máy thức trong lúc chạy.** Máy ngủ thì run bị treo.
  - macOS: cắm sạc, không gập máy. Có thể mở thêm một terminal chạy `caffeinate -dimsu`.
  - Windows: Settings → Power → Sleep đặt "Never" trong lúc chạy.
- Run tự tạm dừng khi gọi model lỗi (sai khóa, hết tiền, TeamoRouter từ chối). Sửa khóa bằng `arrow models` rồi bấm R. Gặp lỗi khác thì báo Dũng (xem mục 7).

## 6. Xuất kết quả và đẩy lên GitHub

Khi run báo DONE:

```
arrow show
arrow export
```

`arrow show` để xem kết quả. `arrow export` tạo file `ARROW/pilot_results/pilot-6/shard-XX-<Tên máy>.zip`. File này không chứa khóa API.

Sau đó chỉ commit đúng file zip của máy mình (đứng ở thư mục `ARROW`):

```
git add pilot_results/pilot-6/shard-XX-<Tên máy>.zip
git commit -m "pilot-6: kết quả shard-XX"
git pull --rebase
git push
```

**Không commit** `machine.local.json`, `litellm.config.local.yaml`, `runs/`, `logs/`, `repo/`, `tools/`. Các thư mục này đã có trong `.gitignore`, nên đừng dùng `git add -f`.

## 7. Lỗi hay gặp

| Thông báo | Cách xử lý |
| --- | --- |
| `SHARD_EXPERIMENT_MISMATCH` hoặc doctor báo phân công cho cấu hình khác | `git pull`. Đừng sửa `experiments/` hay `shards/` |
| `LLM_CONFIG_MISSING` | `arrow models` → nhập provider và khóa cho model |
| `UnsupportedParamsError` / `reasoning_effort` | `arrow models` → mở "Provider và khóa API" → Enter qua hết để lưu lại file đúng mẫu |
| `enterprise_insufficient_balance`, `routing_strategy_resource_unavailable` | Số luồng gọi model phải ≤ 2 (`arrow setup` bước 7); kiểm tra số dư khóa |
| `ARROW_MOVED` (đã đổi tên hoặc chuyển thư mục ARROW) | Cài lại `install-arrow` và `install-litellm`, rồi chạy `arrow setup` |
| Run đứng yên rất lâu | Thường do máy ngủ. Bấm S để dừng rồi chạy lại `arrow run` |

Khi báo lỗi cho Dũng, gửi kèm file log mới nhất trong `pilot_tool/logs/` (`arrow-<ngày>.log`). Log không chứa khóa API.

Tài liệu chi tiết: `SETUP.md` (cài đặt, `arrow models` ở Mục 5a), `README.md` (danh sách lệnh), `CLI_DASHBOARD.md` (dashboard).
