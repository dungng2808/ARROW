# Phân công pilot cho 6 máy (`pilot-6`)

Mỗi file `shard-NN.json` là danh sách task (CUT) giao cho một máy; mọi CUT của một repository nằm trên cùng một máy.
`plan.json` ghi sha256 của cấu hình thực nghiệm (`ARROW_Framework/experiments/pilot-v1.json`), của manifest và của từng file shard.

| Shard | Máy | Task |
| --- | --- | ---: |
| `shard-01.json` | Dung-mac | 9 |
| `shard-02.json` | Dung-win | 10 |
| `shard-03.json` | KieuAnh | 10 |
| `shard-04.json` | Quang | 10 |
| `shard-05.json` | Chinh | 10 |
| `shard-06.json` | Han | 10 |

## Chạy trên mỗi máy

Hướng dẫn đầy đủ (cài đặt lần đầu, menu, theo dõi, gửi kết quả): [ARROW_Framework/HUONG_DAN_CHAY.md](../../ARROW_Framework/HUONG_DAN_CHAY.md).

Pull bản mới nhất, cài máy (`arrow setup`, `arrow models`, `arrow doctor`), rồi từ thư mục `ARROW_Framework`:

```text
dotnet run --project src/Arrow.UI -- run --experiment experiments/pilot-v1.json --run-id pilot-6-<máy> --shard ../shards/pilot-6/shard-NN.json
```

Framework từ chối nếu `experiment_sha256` trong file shard khác sha256 của cấu hình đang dùng (pull thiếu hoặc cấu hình đã đổi).
Xong, gửi thư mục `runs/pilot-6-<máy>/` cho người tổng hợp; báo cáo gộp: `arrow report --run-id pilot-6-Dung-mac --run-id pilot-6-Dung-win …`.

## Cập nhật khi cấu hình hoặc manifest đổi

Giữ nguyên cách chia máy, chỉ thay task và ghi lại sha256 (từ thư mục `ARROW/`):

```text
python ARROW_Framework/scripts/update_shard_plan.py --plan-dir shards/pilot-6 --experiment ARROW_Framework/experiments/pilot-v1.json --replace OLD_TASK=NEW_TASK
```

Lần cập nhật 14/10/2026: chuyển sang ARROW Framework và manifest pilot v3 (`192942452_cec4fa16291b8ed3` → `48804926_92a9e8e5357804df`, shard-06).
