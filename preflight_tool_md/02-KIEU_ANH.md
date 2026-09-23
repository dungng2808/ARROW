# Giao việc preflight — Kiều Anh — shard 02

## 0. Cổng kiểm tra bắt buộc — thiếu môi trường thì dừng ngay

Đây là bước đầu tiên, trước khi tạo run, index, clone hoặc build. Chỉ thực hiện kiểm tra đọc file, parse config, hash và lệnh kiểm version/import cần thiết. **Nếu thiếu hoặc không dùng được bất kỳ thành phần bắt buộc nào, dừng tác vụ và báo người dùng bổ sung. Không tự cài, tải, tạo config/venv, dùng đường dẫn giả hoặc chạy tiếp để thử.** Chỉ setup khi người dùng yêu cầu riêng sau khi nhận báo cáo; yêu cầu setup trong tài liệu tham chiếu không tự cấp quyền setup cho lượt chạy này.

Kiểm tra trên chính môi trường sẽ thực thi:

- Có root ARROW, `preflight_tool/preflight/cli.py`, `preflight_tool/preflight/shards.py`, `preflight_tool/pyproject.toml`; code hỗ trợ `--shard` và dọn cache repo.
- Có `shards-5/shard-02.json`, `shards-5/summary.json`, `shards-5/README.md`, `preflight.md`, `preflight_tool/TEST_REPORT_FIX_FOUR_20260923.md`, `Java-version/AGENTS.md`. File phân công parse được và hash khớp summary.
- Có `ARROW/classes2test/dataset/` chứa JSON theo cấu trúc `<project-id>/<sample>.json`, không chỉ folder rỗng hoặc file nén chưa giải nén. Nếu người dùng đã cung cấp vị trí khác thì kiểm vị trí đó; nếu chưa rõ vị trí, hỏi, không tự tải/di chuyển dataset.
- Có Python >=3.11 và interpreter chạy được trong `preflight_tool/.venv` (macOS: `bin/python`; Windows: `Scripts/python.exe`), package preflight và dependency runtime import được. Thiếu venv/package thì báo tên thành phần và đường dẫn; không tự pip install.
- Có `Java-version/config.local.toml`, parse TOML thành công; đủ mapping JDK 8/11/17/21 và default_home hợp lệ. Đường dẫn phải thuộc máy/môi trường hiện tại, đúng OS/CPU, có cả java/javac chạy được và đúng major. Có folder JDK nhưng binary thiếu/sai version vẫn là chưa đạt.
- Có Git hoạt động và công cụ build cần thiết đã được chuẩn bị. Ghi rõ Maven/Gradle đang có; nếu thiếu fallback và chưa có phương án wrapper phù hợp được xác nhận thì báo để người dùng bổ sung, không tự cài hoặc giả định repo nào cũng có wrapper. File build/wrapper bên trong repo chưa clone không phải file local bắt buộc có ngay đầu.
- Có môi trường build cô lập an toàn sẵn sàng theo mục 2; các path Python/JDK/dataset/config phải dùng được bên trong môi trường đó, không chỉ trên host. Thiếu môi trường này cũng phải dừng **trước index-only**.
- Đường dẫn output có quyền ghi, tài nguyên và kết nối đáp ứng yêu cầu. Nếu không xác minh được điều kiện bắt buộc, ghi là chưa xác minh và hỏi người dùng, không coi là PASS.

Khi không đạt, trả lời trực tiếp bằng tiếng Việt với trạng thái **BLOCKED_ENVIRONMENT**:

| Thành phần thiếu/hỏng | Đường dẫn/lệnh đã kiểm | Bằng chứng lỗi | Người dùng cần bổ sung |
| --- | --- | --- | --- |
| Liệt kê từng mục thực tế | Dùng path của máy hiện tại | Không bịa kết quả | Hướng dẫn ngắn, không tự thực hiện |

Nêu rõ **chưa chạy index/clone/build**, người phụ trách và shard, đề nghị người dùng bổ sung rồi yêu cầu chạy lại. Không tạo run giả hoặc HANDOFF báo hoàn thành chỉ để đủ file. Có thể gom các lỗi bằng kiểm tra read-only an toàn; không thực thi binary chưa xác minh. Sau khi người dùng bổ sung, kiểm lại toàn bộ cổng này; chỉ khi đạt mới làm các mục dưới.


## 1. Nhiệm vụ và phạm vi

Agent hãy đọc **toàn bộ file này**, các AGENTS.md áp dụng và tài liệu được dẫn dưới đây, rồi thực hiện trên máy của **Kiều Anh**. Không chỉ đưa lại hướng dẫn hoặc dừng ở index-only: mục tiêu là chạy hết phần được giao, kiểm tra output và viết báo cáo bàn giao nếu môi trường đáp ứng điều kiện an toàn.

- Repository: ARROW trên máy hiện tại; tự tìm đường dẫn, không hard-code username/ổ đĩa của người khác.
- File phân công duy nhất: `shards-5/shard-02.json`.
- Số class cần xử lý: **17164**; **1.882 repository**.
- Dataset đã có: ưu tiên `classes2test/dataset/`. Không tải/chia lại dataset nếu không cần.
- Đây là class candidate, chưa phải class chắc chắn build hoặc sinh test được.
- Không chạy shard của người khác. Không sửa manifest, source dataset, code tool hay build file của repo được kiểm tra để ép pass.
- Không commit/push, reset/clean/stash worktree, xóa evidence hoặc thay đổi Java/PATH toàn máy.
- Chỉ kiểm tra môi trường, chạy phần được giao và tạo output/report local khi cổng mục 0 đã đạt. Nếu cần setup, quyền, credentials hoặc quyết định ngoài phạm vi này, dừng và hỏi người dùng.

## 2. Kiểm tra trước khi chạy

1. Đọc `shards-5/README.md`, `preflight.md`, `preflight_tool/TEST_REPORT_FIX_FOUR_20260923.md`. Nếu một file chưa có, báo cần nhận đủ bản code mới; không tự suy đoán tính năng.
2. Ghi Git HEAD, `git status --short`, OS/CPU, Python/Git/build-tool version. Không tự pull/checkout khi có thay đổi local; giữ nguyên dữ liệu của người dùng. Các máy phải dùng cùng bản code đã thống nhất.
3. Xác nhận CLI có `--shard`. Kiểm SHA-256 file `shard-02.json` với entry tương ứng trong `shards-5/summary.json`; số class phải đúng 17164. Nếu thiếu/mismatch thì dừng trước build và báo lỗi.
4. Xác định input root thật trên máy. Nếu không nằm ở vị trí mặc định, thay `--input-root` trong lệnh bên dưới bằng đường dẫn đúng; không sửa shard.
5. Dùng virtualenv đã kiểm tra tại mục 0. Nếu phát hiện Python/dependency thiếu hoặc hỏng, dừng và báo người dùng; không tự tạo venv hoặc cài package.
6. Đọc đầy đủ `Java-version/AGENTS.md` để đối chiếu JDK/config đã có. Chỉ tái sử dụng JDK 8/11/17/21 được xác minh; không tự setup/tải JDK hay tạo/sửa `Java-version/config.local.toml` trong nhiệm vụ chạy shard. Thiếu hoặc sai thì dừng theo mục 0.
7. Kiểm `java` và `javac` từng major; Git và build tool/wrapper cần thiết. Trong môi trường tiến trình chạy tool, đặt JAVA_HOME/PATH về JDK 17 đã xác minh để lệnh kiểm version build tool không dùng Java khác ngoài ý muốn; không đổi cấu hình hệ thống. Runner vẫn dùng mapping JDK theo project.
8. Kiểm dung lượng trống, RAM và kết nối mạng. Bắt đầu `--workers 2`; ghi cấu hình thực tế. Giữ timeout chung 900 giây/command và max revision candidates 500 nếu nhóm chưa thống nhất cấu hình khác. Không tự tăng workers để chạy nhanh khi thiếu RAM.

### Điều kiện an toàn cho clone/build

Build của repository bên ngoài có thể thực thi script/plugin tùy ý. Chỉ chạy phần clone/build trong môi trường cô lập phù hợp (VM/container dùng để chạy dataset), không có secrets hoặc quyền truy cập tài liệu cá nhân/SSH agent/credential của host; không mount Docker socket hay toàn bộ home. Không tắt TLS hoặc tải executable không xác minh để vượt lỗi.

Nếu chưa có môi trường an toàn, dừng ngay ở cổng mục 0 và báo **BLOCKED_ENVIRONMENT: cần môi trường build cô lập**, không chạy index-only trước. Nếu chạy trong Linux container/VM, phải có sẵn JDK và đường dẫn phù hợp **bên trong** môi trường đó; không dùng binary JDK macOS/Windows của host. Rule Java hiện mô tả macOS/Windows; nền tảng chưa được mô tả cần người dùng xác nhận cách chuẩn bị, không tự coi là đã được hỗ trợ.

## 3. Chọn thư mục output duy nhất

Tạo RUN_ID từ thời gian UTC, ví dụ `20260924T080000Z`, và dùng cùng ID cho lượt này. Lệnh dưới có `<RUN_ID>`: agent **thay bằng giá trị thật trước khi chạy**, không truyền nguyên dấu `< >`.

Từ `ARROW/preflight_tool`, mọi lệnh `python` bên dưới phải là interpreter của venv:

- macOS: `.venv/bin/python`;
- Windows: `.venv\Scripts\python.exe` (PowerShell dùng call operator `&` nếu đường dẫn có khoảng trắng);
- Hoặc kích hoạt venv rồi dùng `python`.

Không dùng output đã có dữ liệu. Tool hiện **không hỗ trợ resume** dù có comment cấu hình nhắc resume; không xóa output cũ để chạy lại cùng tên.

## 4. Kiểm tra shard trước, chưa clone/build

```text
python -m preflight.cli --config ../Java-version/config.local.toml --input-root ../classes2test --shard ../shards-5/shard-02.json --index-only --output-dir runs/kieu_anh-shard-02-<RUN_ID>-check
```

Phải kiểm tra:

- Exit code 0 và `provenance.json` có `shard_id = shard-02`, `classes_selected = 17164`.
- `manifests/class_candidates.jsonl` có đúng 17164 task ID duy nhất và đúng tập ID trong shard.
- `reports/input_rejections.jsonl` rỗng với dataset chuẩn hiện tại. Nếu có rejection, ghi rõ và dừng để đối soát trước full run.
- Với dataset đầy đủ như bản chia: `raw_json_indexed = 362414`, `deduplicated_classes = 85819`. Nếu số khác, kiểm snapshot và báo khác biệt; không tự coi tương đương. Tool đã kiểm metadata/evidence hash của class thuộc shard trước khi chọn.
- Index-only không tạo kết luận eligible/build pass và không đòi `summary.json`.

Nếu thiếu class hoặc hash mismatch, giữ log và báo cần đúng snapshot; không sửa manifest, task ID hoặc hash để bypass.

## 5. Chạy hết shard được giao

Chỉ tiếp tục khi bước 4 đạt và môi trường build an toàn đã sẵn sàng. Không dùng `--fast`, `--limit` hoặc `--max-classes`.

```text
python -m preflight.cli --config ../Java-version/config.local.toml --input-root ../classes2test --shard ../shards-5/shard-02.json --workers 2 --output-dir runs/kieu_anh-shard-02-<RUN_ID>
```

- Không có revision map upstream được audit trong giao việc này: mặc định chạy **DISCOVERY_ONLY**; không tự tạo SHA/provenance upstream để có strict eligible.
- Nếu nhóm cung cấp revision map được audit, cần xác nhận thay đổi chế độ trước khi bổ sung `--revision-map`; không tự chuyển nhiệm vụ discovery thành strict experiment.
- Lưu command, start/end UTC, stdout/stderr và exit code vào artifact local. Nếu dùng tee/pipeline, phải giữ exit code thật của Python, không lấy exit code của tee.
- Theo dõi tiến trình cho đến khi hoàn tất hoặc có blocker thật. Tool có thể chưa in summary trong lúc chạy; không kết luận treo chỉ vì stdout im lặng. Kiểm process/log đang thay đổi; không khởi động trùng lượt.
- Các trạng thái như CLONE_FAILED, MAIN_BUILD_FAILED, JDK_UNSUPPORTED, EXCLUDED, NEEDS_REVIEW là kết quả candidate cần ghi nhận, không tự sửa repo để đổi status.
- Nếu process chết, hết dung lượng hoặc mất môi trường: giữ output, ghi số lượng evidence còn lại và blocker. Không tuyên bố xong; không tự retry toàn bộ phần trên output cũ. Muốn chạy lại dùng ID mới sau khi nguyên nhân đã xử lý, không xóa bằng chứng.
- Không chỉ khởi động nền rồi báo hoàn thành. Nếu agent/môi trường không thể theo dõi tiếp, bàn giao PID/session, output và tình trạng thực tế.

## 6. Kiểm tra hoàn tất

### Dọn repository sau xử lý

- Bảo đảm config local có `[run].keep_workspaces=false` và `keep_repo_cache=false`; nếu config hiện tại yêu cầu giữ để debug, báo khác biệt và hỏi trước khi đổi lựa chọn đó. Không truyền `--keep-workspaces` hoặc `--keep-repo-cache` trong lượt chạy thông thường.
- Tool tự dọn workspace từng class, rồi xóa đúng mirror trong `runs/<run-id>/cache/mirrors/` khi mọi class được chọn của repo đã kết thúc; không xóa khi còn worker/việc chờ của repo. Không xóa dataset, JDK, log, report hoặc manifest.
- Kiểm `reports/repo_cleanup.jsonl` và `summary.repo_cleanup`: DELETED là đã xóa, ABSENT là không có mirror để xóa, KEPT là giữ có lý do, FAILED là không dọn được. Có workspace còn sót thì mirror được giữ để không làm hỏng worktree.
- Ghi số lượng theo trạng thái cleanup và repo còn cache trong HANDOFF. Nếu KEPT/FAILED ngoài dự kiến, báo riêng **cleanup chưa hoàn tất**, không tuyên bố đã dọn sạch. Không tự xóa đường dẫn ngoài cache của run hoặc xóa repo gốc.
- Process bị kill/tắt máy có thể để lại cache; không có cơ chế resume hoặc tự dọn toàn bộ cache cũ. Không xóa evidence để che lỗi. Mirror đã xóa không vào thùng rác; có thể cần clone lại nếu muốn debug/tái chạy.

Trong output full run, cần có:

```text
provenance.json
reports/preflight_results.jsonl
reports/preflight_results.csv
reports/summary.json
reports/input_rejections.jsonl
reports/repo_cleanup.jsonl
manifests/class_candidates.jsonl
manifests/locked_input_manifest.jsonl
manifests/technical_eligible_manifest.jsonl
manifests/strict_eligible_manifest.jsonl
logs/
```

Agent phải parse dữ liệu và đối soát, không chỉ kiểm file tồn tại:

1. Full run exit code 0; `summary.total = 17164`.
2. JSONL có đúng 17164 kết quả và task ID duy nhất; tập task ID bằng **chính xác** tập trong shard, không thiếu hoặc thừa. CSV có cùng tập ID.
3. Tổng `summary.by_status` bằng 17164; từng số lượng khớp JSONL.
4. Số technical/strict trong summary bằng số record tương ứng có cờ true; các manifest eligible chứa đúng tập ID đó.
5. Không có audited revision map thì mọi record `strict_eligible=false`, strict manifest rỗng và summary strict=0. Không suy luận strict từ việc compile pass.
6. Provenance shard ID/hash và config hash khớp file thực dùng; kiểm theo đúng thời điểm chạy, không sửa config giữa chừng. Mỗi kết quả có status/reason cần thiết; log được tham chiếu phải tồn tại, nếu thiếu thì ghi lỗi đối soát.
7. Nếu thiếu record hoặc số liệu lệch, đánh dấu **INCOMPLETE/FAILED_VALIDATION**, không báo hoàn thành và không tự gộp/xóa record để đủ số.

**Hoàn thành phần việc không có nghĩa mọi repository build pass**: đủ kết quả và bằng chứng cho mọi class là tiêu chí xử lý xong; số eligible/fail/excluded phải báo trung thực.

## 7. Báo cáo bàn giao bắt buộc

Tạo `HANDOFF.md` trong output full run; nếu đã chạy index-only rồi mới bị chặn thì đặt ở output check. Nếu dừng ngay tại cổng môi trường mục 0, báo trực tiếp BLOCKED_ENVIRONMENT, không tạo output chỉ để chứa HANDOFF. Report/output đã sinh nằm trong `preflight_tool/runs/` được Git ignore.

Nội dung:

- Người phụ trách **Kiều Anh**, shard **02**, RUN_ID, Git HEAD và thay đổi local ảnh hưởng code.
- OS/CPU, Python, Git, Maven/Gradle nếu có, vendor/full build của JDK 8/11/17/21; config, input và môi trường cô lập thực dùng.
- Command, workers, timeout, start/end, exit code, shard SHA-256 và config SHA-256.
- Expected **17164**, actual total, số missing/extra/duplicate và kết quả từng phép đối soát mục 6.
- Bảng count theo preflight_status, technical eligible, strict eligible; top reason_codes và ví dụ task/log cho lỗi nổi bật.
- Kết luận duy nhất phù hợp: **COMPLETED_DISCOVERY**, **BLOCKED_ENVIRONMENT**, **BLOCKED**, **INCOMPLETE** hoặc **FAILED_VALIDATION**. Không gọi là strict experiment hoàn tất.
- Đường dẫn output/evidence; phần chưa chạy, blocker và hành động người dùng cần làm nếu có.

Giữ output trên máy. Nếu cần đóng gói bàn giao, chỉ đóng gói reports/manifests/provenance/logs/HANDOFF/stdout-stderr của lượt này; không kèm JDK, source dataset, cache mirror, workspaces hoặc secrets. Không xóa output sau đóng gói; không tự upload/gửi bên ngoài hoặc push Git khi chưa được yêu cầu.

Cuối cùng trả lời người dùng bằng tiếng Việt: đã xử lý bao nhiêu/17164 class, phân bố trạng thái chính, kết quả đối soát, đường dẫn HANDOFF và phần còn chặn. Nếu mọi điều kiện đạt, kết thúc với COMPLETED_DISCOVERY.
