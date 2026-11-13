# Preflight .NET

Port .NET 8 của Preflight Python, gồm P0–P7 và P4b. Ba adapter Classes2Test,
GitBug-Java và CAT-LM dùng chung pipeline Git → revision → AST Java → build main/test
→ policy → API → compile probe → manifest. Runtime xử lý nghiệp vụ bằng C#;
không gọi Python để normalize, lọc CUT hay quyết định eligibility.

## Chạy thực nghiệm trên 5 máy

Toàn bộ phân công và hướng dẫn agent nằm trong [`Shards/`](Shards/):
[Dũng](Shards/01-DUNG.md), [KA](Shards/02-KIEU_ANH.md),
[Quang](Shards/03-QUANG.md), [Chính](Shards/04-CHINH.md),
[Hán](Shards/05-HAN.md). Plan chung ở `Shards/_shared/team-plan.json`;
revision 2 đã đổi phần việc của bạn và Chính theo yêu cầu.

## Build và test

Chạy từ `D:/ki9/SET490/ARROW/Prelight_Net`. SDK hiện tại là 10.0.403, target net8.0.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/dotnet-local.ps1 restore PreflightNet.sln
powershell -NoProfile -ExecutionPolicy Bypass -File tools/dotnet-local.ps1 build PreflightNet.sln -c Release --no-restore
powershell -NoProfile -ExecutionPolicy Bypass -File tools/dotnet-local.ps1 test PreflightNet.sln -c Release --no-build
dotnet src/Preflight.Cli/bin/Release/net8.0/preflight-net.dll --help
```

`preflight-net.ps1` chuyển tiếp tham số tới DLL Release. `tools/dotnet-local.ps1`
giữ NuGet, DOTNET_CLI_HOME, TEMP/TMP trong dự án. Runtime cần Git, JDK và Maven/Gradle
hoặc wrapper của repository. `preflight.local.json` đã điền các JDK 8/11/17/21,
Maven 3.9.9, Gradle 8.10.2 đang có trên máy; sửa đường dẫn nếu chuyển máy.
Cache build mặc định là `.cache/m2` và `.cache/gradle`.

DLL native `tools/tree-sitter-native/bin/tree_sitter_java_preflight.dll` là Windows x64,
khóa tree-sitter 0.24.0 / grammar Java 0.23.5, không cần Python lúc chạy.
Để build lại DLL (có tải compiler và sdist, mọi cache nằm trong `.work/native`):

```powershell
& '../preflight_tool/.venv/Scripts/python.exe' -B tools/tree-sitter-native/build.py
```

Checksum nguồn, compiler và DLL nằm trong `tools/tree-sitter-native/tools.lock.json`.
Build lại tool/DLL sẽ đổi fingerprint: run cũ phải dùng đúng binary cũ để resume.

## Normalize ba dataset

Các lệnh này tạo index và evidence; không chạy build dataset.
CAT-LM đọc phần Java của tar, giải nén snapshot vào cache riêng, kiểm Git HEAD,
shallow commit, đường dẫn cặp và dirty snapshot. GitBug raw cần truy cập mirror để
ghép test tại fixed commit; có thể replay input đã normalize hoàn toàn offline.

```powershell
.\preflight-net.ps1 normalize --dataset classes2test --config preflight.local.json --output-dir runs/c2t
.\preflight-net.ps1 normalize --dataset gitbug --config preflight.local.json --output-dir runs/gitbug
.\preflight-net.ps1 normalize --dataset catlm --config preflight.local.json --output-dir runs/catlm --offline
# Replay GitBug có sẵn, không clone/fetch:
.\preflight-net.ps1 normalize --dataset gitbug --config preflight.local.json --input-root ../gitbug_input --output-dir runs/gitbug-replay --offline
```

`--input-root` ghi đè dataset root; `--limit N` phục vụ kiểm thử nhỏ.
CAT-LM chấp nhận tar thô đang mang tên `Dataset/catlm/testdata.tgz`; dừng trước phần
Python bị cắt. Mặc định lấy 119 cặp/98 repo, gắn cờ 27 cặp eval. Có thể đặt
`cohort=eval_filepairs`. `revision_policy=snapshot_unverified` là mặc định;
`snapshot_dataset_record` là lựa chọn nghiên cứu riêng để coi snapshot là pinned.
`verify_upstream=true` kiểm thêm SHA qua fetch, cần mạng.

## Run và phục hồi

Những lệnh sau là hướng dẫn vận hành. Lượt triển khai chỉ chạy trên fixture Java
cục bộ và normalize dữ liệu; chưa chạy preflight/build trên ba dataset thật.

```powershell
.\preflight-net.ps1 run --output-dir runs/c2t --workers 5 --shard ../shards-5/shard-01.json
.\preflight-net.ps1 run --output-dir runs/gitbug --workers 5
.\preflight-net.ps1 resume --output-dir runs/gitbug --workers 3
.\preflight-net.ps1 sweep --output-dir runs/gitbug
.\preflight-net.ps1 reopen --output-dir runs/gitbug --status CLONE_FAILED --since 2026-11-13T00:00:00Z --reason "network outage; see Git logs"
```

Output phải là thư mục con của Prelight_Net. `run` cũng nhận dataset/config/input-root
để normalize trước. `--revision-map` nhận CSV/JSONL pinned có evidence_ref.
`--max-classes` không dùng cùng `--shard`. Chọn `--probe-profile v1` khi tạo run mới
để đối chiếu Python gốc; v2 gồm bản sửa probe/JDK đã dùng cho GitBug.
`--fast` chỉ cho PRECHECKED, không tạo technical/strict eligible.

Checkpoint SQLite WAL lưu contract, SHA evidence/manifest, fingerprint, session,
attempt và kết quả. Resume chỉ cho đổi workers; input/tool/config thay đổi bị từ chối.
Ctrl+C giữ task chưa xong ở PENDING; hard kill được phục hồi thành attempt ABANDONED
rồi chạy lại. Không dùng checkpoint Python schema 2 cho bản .NET schema 3.

Disk guard kiểm lúc cấp task và theo chu kỳ. Thiếu dung lượng trả exit 75, giữ task
PENDING; sau khi dọn đủ `resume_free_gb`, gọi resume. Exit 130 là bị hủy; 1 là lỗi
contract/input/CLI. Exit 0 nghĩa run hoàn tất, kể cả có CUT bị loại/lỗi build.
`reopen` chỉ mở lại status hạ tầng, giữ kết quả cũ trong audit và ghi lý do vào provenance.

Scheduler giới hạn repo đang xử lý theo workers, dọn mirror/snapshot khi repo xong,
quét workspace mồ côi khi resume; `keep_workspaces`/`keep_repo_cache` giữ cache theo config.
Build cache chỉ trim khi vượt ngưỡng và giữa các build trong run: Gradle build cache,
daemon/.tmp và Maven SNAPSHOT cũ hơn 7 ngày. Các run độc lập nên dùng cache riêng nếu
muốn bật trim đồng thời.

## Lệnh hỗ trợ

```powershell
.\preflight-net.ps1 export-shards --index runs/c2t/state/class_index.sqlite --output-dir shards-net --parts 5
.\preflight-net.ps1 split-dataset --source Dataset/classes2test-main --output-dir split-net --parts 5
.\preflight-net.ps1 summarize --run-dir runs/gitbug --output-dir summaries/gitbug
.\preflight-net.ps1 rerun --previous-run runs/gitbug --output-dir runs/gitbug-v2 --status PROBE_FAILED --workers 3
.\preflight-net.ps1 rerun --previous-run runs/gitbug-v2 --output-dir runs/gitbug-new --input-root ../gitbug_input --new-only
.\preflight-net.ps1 merge --run runs/gitbug --run runs/gitbug-v2 --output-dir merged/gitbug
.\preflight-net.ps1 convert-config ../Java-version/config.local.toml --output converted.json
```

Shards gom theo repository và khóa metadata/evidence. Split chép độc lập, kiểm SHA-256,
kiểm phủ đủ nguồn và dừng nếu có input bị loại. Summarize chỉ ghi khi report/provenance
khớp và run COMPLETED. Merge giữ nguyên byte từng dòng, bỏ eligibility cũ khi task đã
được chạy lại. Rerun mặc định chọn status khác ELIGIBLE/EXCLUDED, chạy v2, ghi patches
trước khi chạy và tạo `merged/` khi hoàn tất, kể cả hoàn tất qua resume.
Convert-config hỗ trợ các scalar/table của config TOML Preflight cũ; cú pháp TOML
ngoài phạm vi này sẽ báo lỗi.

## Kiểm chứng và giới hạn

- Full Classes2Test: 362.414 evidence, 85.819 CUT, index khớp Python; 5 shard khớp byte `ARROW/shards-5/`.
- AST: khớp 5.000 file Java thật với parser Python cùng phiên bản.
- GitBug: golden đủ 199 patch; replay 202 CUT và shard khớp Python; raw adapter có test Git local/manual pairing.
- CAT-LM: normalize đủ 119 cặp, 98 repo, 27 eval; ghi dirty snapshot và hash phần Java của archive.
- Fixture Maven đơn/nhiều module, JUnit 4/5, TestNG, Gradle compile probe; checkpoint, hard kill/resume, disk guard, tamper, cleanup và path dài.

Evidence nằm trong `.work/verification/`, TRX trong `.work/test-results/`;
báo cáo chi tiết: `file.md/BAO_CAO_P2_P7.md`.

Đối chiếu toàn bộ golden P0/P1 đã xuất:

```powershell
$env:PREFLIGHT_GOLDEN_ROOT = Join-Path (Get-Location) '.work/golden-full'
powershell -NoProfile -ExecutionPolicy Bypass -File tools/dotnet-local.ps1 test PreflightNet.sln -c Release --no-build
Remove-Item Env:PREFLIGHT_GOLDEN_ROOT
```

Các exporter oracle trong `tools/golden/` dùng Python cũ ở chế độ chỉ đọc (`-B`).
Chuỗi giống API key trong payload golden được khử trước khi tính oracle và checksum;
dataset gốc không bị sửa. `tools/golden/sanitize_records.py` cập nhật các golden đã xuất.
Golden AST được nén để bộ golden lưu trong repo dưới 50 MiB. Những script hậu xử lý
Python cũ tiếp tục đọc các trường manifest cũ; bản .NET bổ sung dataset/pairing/probe
profile/provenance. Quirk lọc của Python được giữ lại. P8 (S0, đổi policy source set,
cổng xác nhận test gọi CUT) chưa thuộc phạm vi P2–P7. R1/R2 chạy preflight dataset thật
vẫn cần thực hiện trước khi khẳng định parity end-to-end trên các repository thực tế.

Không sửa Python gốc, dataset, kết quả cũ, `.gitignore`, hoặc ARROW_Framework;
không commit/push trong lượt triển khai này.
