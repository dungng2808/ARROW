# Giao việc cho AI: đưa manifest preflight lên Git

Khi người dùng gửi file này cho AI để xử lý manifest của họ, hãy **thực hiện công việc đến khi push xong**, không chỉ tóm tắt hướng dẫn. Yêu cầu đó bao gồm quyền sao chép manifest đã tạo, commit và push **chỉ thư mục shard của người dùng** trong `preflight_manifest/`; không cần hỏi lại quyền cho từng bước. Nếu thiếu output hợp lệ, chưa xác định được shard, hoặc Git không có quyền push, báo chính xác điều còn thiếu. Không tạo dữ liệu giả để hoàn thành.

## Đích đến

Repository ARROW có cấu trúc sau. `01-DUNG/` là ví dụ đã có trên máy tạo tài liệu; các thành viên thêm thư mục cùng cấp cho shard của mình.

| Shard | Người phụ trách | Thư mục Git | Số class |
| --- | --- | --- | ---: |
| 01 | Dũng | `preflight_manifest/01-DUNG/` | 17.164 |
| 02 | Kiều Anh | `preflight_manifest/02-KIEU_ANH/` | 17.164 |
| 03 | Quang | `preflight_manifest/03-QUANG/` | 17.164 |
| 04 | Chinh | `preflight_manifest/04-CHINH/` | 17.164 |
| 05 | Hán | `preflight_manifest/05-HAN/` | 17.163 |

Mỗi thư mục chứa đúng bốn artifact từ **cùng một full run** của shard đó:

```text
class_candidates.jsonl
locked_input_manifest.jsonl
technical_eligible_manifest.jsonl
strict_eligible_manifest.jsonl
```

## Các bước AI phải làm

1. Tìm root Git của ARROW và xác định người dùng đang phụ trách shard nào từ yêu cầu của họ, file phân công `preflight_tool_md/`, hoặc bằng chứng run. Nếu có nhiều shard/run khả dĩ mà không thể xác định đúng một nguồn, hỏi người dùng để chọn; không tự lấy output của người khác.
2. Tìm output full run trên máy người dùng, thường ở `preflight_tool/runs/<run-id>/`. Ưu tiên đường dẫn người dùng cung cấp. Chỉ dùng run đã hoàn tất và được đối soát; không dùng output `--index-only`, smoke test hoặc run dở. Đọc `provenance.json`, `reports/summary.json`, `reports/preflight_results.jsonl` và `HANDOFF.md` nếu có. Nếu chưa có full run hợp lệ, báo blocker và dừng việc push; nhiệm vụ này không yêu cầu tự chạy lại toàn bộ preflight.
3. Kiểm tra `provenance.json` trỏ đúng shard và `summary.total` bằng số class trong bảng. Đối chiếu task ID của `class_candidates.jsonl` và `locked_input_manifest.jsonl` với shard, bảo đảm không trùng/thiếu/thừa; số record technical/strict khớp summary và kết quả run. Nếu không có revision map được audit, `strict_eligible_manifest.jsonl` rỗng là hợp lệ và vẫn phải giữ file rỗng. Nếu đối soát lỗi, không sửa JSONL để ép khớp; báo lỗi.
4. Sao chép nguyên vẹn bốn file từ `<output-dir>/manifests/` sang đúng `preflight_manifest/<thu-muc-cua-minh>/`. Kiểm tra file nguồn và file đích bằng SHA-256. Không sửa `01-DUNG/` hay thư mục của shard khác. Không đưa lên Git dataset, JDK, cache, `preflight_tool/runs/`, log hoặc secret. Nếu phát hiện secret trong bốn file, dừng push và báo vị trí để xử lý.
5. Kiểm tra `git status` và diff trước commit. Chỉ stage bốn file trong thư mục shard của mình; không đưa thay đổi sẵn có của người khác vào commit. Commit với thông điệp nêu shard rồi push lên nhánh/remote mà repository đang dùng (thường là `main` trên `origin`). **Không force push, reset, clean hoặc ghi đè thay đổi của người khác.** Nếu remote có commit mới, cập nhật nhánh một cách an toàn, xử lý xung đột đúng phạm vi và thử push lại. Nếu không có quyền Git hoặc gặp xung đột không thể giải quyết từ bằng chứng hiện có, báo blocker cụ thể.
6. Xác minh commit chứa đúng bốn file và đã hiện trên remote. Trả lời người dùng bằng tiếng Việt: shard, run nguồn, số record của từng manifest, commit SHA, nhánh/remote và kết quả push. Nếu nội dung đã giống hệt trên remote, không tạo commit trống; báo đã có sẵn cùng commit hiện tại.

Ví dụ cho **shard 02** (AI phải thay `02-KIEU_ANH` bằng thư mục đúng của người dùng và kiểm tra nhánh/remote trước khi push):

```text
git status --short
git add -- preflight_manifest/02-KIEU_ANH/class_candidates.jsonl preflight_manifest/02-KIEU_ANH/locked_input_manifest.jsonl preflight_manifest/02-KIEU_ANH/technical_eligible_manifest.jsonl preflight_manifest/02-KIEU_ANH/strict_eligible_manifest.jsonl
git diff --cached --stat
git commit --only -m "Add preflight manifests for shard 02" -- preflight_manifest/02-KIEU_ANH/class_candidates.jsonl preflight_manifest/02-KIEU_ANH/locked_input_manifest.jsonl preflight_manifest/02-KIEU_ANH/technical_eligible_manifest.jsonl preflight_manifest/02-KIEU_ANH/strict_eligible_manifest.jsonl
git push origin main
```

Tài liệu đối chiếu: [phân công shard](../shards-5/README.md), [ý nghĩa các manifest](../preflight_tool/README.md#outputs), và file giao việc của từng người trong `preflight_tool_md/`.
