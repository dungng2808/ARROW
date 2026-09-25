# Class2Test Preflight (one-off CLI)

Tool này là một khối tiền xử lý độc lập: nhận JSON Classes2Test, gộp các test
case trùng class thành một CUT, kiểm tra revision/build/AST/probe, rồi xuất data
đã lọc trước khi thực nghiệm. Nó không import, sửa hoặc gọi ARROW pipeline.

## Quyết định revision

Classes2Test hiện chỉ lưu URL repository, path và snippets; không lưu commit SHA.
Vì vậy tool **không được đoán revision gốc**. Khi không có revision map, nó duyệt
lịch sử Git, đối chiếu body focal method và body test case sau khi bỏ comment và
normalize whitespace, rồi gắn `CONTENT_MATCHED` nếu tìm được. Các entry này chạy
`DISCOVERY_ONLY`, có thể có `technical_eligible=true`, nhưng không vào strict
experiment.

Chỉ record trong revision map đã audit mới có `UPSTREAM_PINNED`, `run_mode=STRICT`
và có thể xuất hiện trong `strict_eligible_manifest.jsonl`.

## Cài và chạy

```powershell
cd 'R:\Đồ án\ARROW\preflight_tool'
py -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -e . pytest

# Bước 1: tạo task_id cho toàn dataset mà chưa clone/build.
class2test-preflight --config config.example.toml --index-only

# Bước 2a: khảo sát kỹ thuật không có SHA upstream.
class2test-preflight --config config.example.toml --output-dir runs\discovery

# Bước 2b: strict experiment sau khi có revision map đã audit.
class2test-preflight --config config.example.toml --revision-map revisions.jsonl --output-dir runs\strict

# Fast diagnosis: never produces strict-eligible records because the probe is skipped.
class2test-preflight --config config.example.toml --fast --output-dir runs\fast
```

Smoke test không clone toàn bộ dataset:

```powershell
class2test-preflight --config config.example.toml --limit 20 --max-classes 5 --output-dir runs\smoke
```

## Dừng và resume một full run

Full run ghi checkpoint nguyên tử sau mỗi `task_id` hoàn tất. Có thể nhấn
`Ctrl+C` để dừng có kiểm soát; tool ngừng cấp việc mới, dừng process con, giữ
checkpoint/log và thoát với mã `130`. Resume bằng đúng output cũ:

```powershell
class2test-preflight --resume --output-dir runs\discovery

# Chỉ số worker được phép thay đổi giữa các session.
class2test-preflight --resume --output-dir runs\discovery --workers 3
```

Task đã có kết quả terminal, kể cả clone/build/timeout failure, sẽ không chạy
lại. Task đang chạy dở khi process bị dừng sẽ chạy lại từ đầu trong
`logs/<task_id>/attempt-<NNN>/`; log attempt cũ vẫn được giữ. Xem tiến độ tại
`reports/progress.json`; đây là snapshot checkpoint, có thể cũ sau hard kill.
Mirror local không hợp lệ hoặc có object database hỏng sẽ được thay bằng clone
mới khi resume. Lỗi mạng, quyền hoặc authentication trên mirror khỏe vẫn là kết
quả terminal và không tự retry. Bộ report/manifest chính thức chỉ được sinh khi
đủ toàn bộ task.

Resume không nhận lại config, input, shard, revision map hay các cờ thay đổi
kết quả: execution contract đã được khóa trong checkpoint. Phải giữ nguyên code
và schema tạo run, cùng các đường dẫn tuyệt đối tới dataset/JDK; thay đổi tool
fingerprint sẽ bị từ chối. Output `--index-only` và output tạo bởi phiên bản tool
cũ không có checkpoint không thể resume. Nếu một sự cố môi trường đã tạo ra hàng
loạt kết quả terminal sai lệch, không dùng resume để kỳ vọng chúng chạy lại; đánh
dấu run không hợp lệ và khởi tạo run mới sau khi xử lý nguyên nhân.

Mỗi historical project cần Maven/Gradle (hoặc wrapper), Git và JDK đúng version.
Khai báo đường dẫn JDK 6/7/8/11/17/21 trong `config.example.toml`; tool không đổi
dependency, build file hay source để ép build pass.

## Revision map audit

`revisions.jsonl` gồm một dòng cho mỗi `task_id` lấy từ
`manifests/class_candidates.jsonl`:

```json
{"task_id":"100021742_...","checkout_sha":"0123456789abcdef0123456789abcdef01234567","revision_provenance":"upstream_metadata","revision_verification_status":"UPSTREAM_PINNED","evidence_ref":"URL or immutable upstream dataset record"}
```

`revision_provenance` chỉ được là `upstream_metadata` hoặc `dataset_record` để
tránh việc một SHA tự nhập tay bị diễn giải thành provenance upstream.

## Outputs

- `reports/preflight_results.jsonl` là audit chuẩn, chứa states độc lập, tags,
  reason codes, content-match evidence và mọi build attempt/log path.
- `reports/preflight_results.csv` là bản phẳng để thống kê.
- `reports/progress.json` là trạng thái checkpoint hiện tại, kể cả khi run chưa
  hoàn tất.
- `manifests/locked_input_manifest.jsonl` giữ revision đã chọn cho mọi CUT.
- `manifests/technical_eligible_manifest.jsonl` chỉ phục vụ khảo sát khi revision
  chưa upstream-pinned.
- `manifests/strict_eligible_manifest.jsonl` là **data đầu ra để thực nghiệm**;
  nó chỉ có `STRICT + UPSTREAM_PINNED + ELIGIBLE`.

Tool tạo bare Git mirror và detached worktree tạm cho từng CUT. Probe được chèn
vào worktree riêng, chỉ chạy `test-compile` và bị xóa trước cleanup; mirror không
bị thêm test/source/generated file.
