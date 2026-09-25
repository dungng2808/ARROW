# Chuẩn bị máy Windows cho ARROW Preflight — dành cho agent không có context

## 0. Cách dùng và thông tin phải thay

Giao **toàn bộ file này** cho một agent mới và yêu cầu agent thực hiện, không chỉ
tóm tắt lại. Đây là task **setup và kiểm chứng máy**, chưa phải task chạy full
shard.

Trước khi bắt đầu, người dùng chỉ cần cung cấp các giá trị sau. Mọi chỗ cần thay
đều được đánh dấu bằng dấu `<...>`:

| Biến | Giá trị cần điền |
| --- | --- |
| `<TEN_NGUOI_CHAY>` | Tên người phụ trách, ví dụ `Dũng` |
| `<SHARD_NO>` | Hai chữ số: `01`, `02`, `03`, `04` hoặc `05` |
| `<ARROW_ROOT>` | Root repository ARROW trên máy đó; nếu chưa biết, agent phải tự tìm |
| `<INPUT_ROOT>` | Root dataset Classes2Test trên máy đó; nếu chưa biết, agent phải tự tìm rồi xác minh |

Không sao chép đường dẫn `R:\Đồ án`, `C:\Users\acer`, JDK hoặc config từ máy
của Hán sang máy khác. Mỗi đường dẫn phải được phát hiện và kiểm chứng trên
chính máy sắp chạy.

## 1. Mục tiêu và quyền được cấp

Agent phải chuẩn bị và kiểm chứng đủ các thành phần sau trên Windows x64:

- Git hoạt động và truy cập được repository từ xa cần thiết.
- Python **>= 3.11**, virtualenv riêng của `preflight_tool` và toàn bộ dependency.
- JDK **8, 11, 17, 21**, có cả `java.exe` và `javac.exe`, đúng major.
- Apache Maven **3.9.9** và Gradle **8.10.2** làm fallback khi repository không
  có wrapper.
- `Java-version/config.local.toml` dùng đúng đường dẫn trên máy hiện tại.
- Dataset và file shard đúng snapshot/hash.
- Maven và Gradle được nhìn thấy từ **chính tiến trình Python sẽ chạy
  preflight**, không chỉ từ một terminal khác.
- Smoke build Maven và Gradle thật đều thành công.
- CLI, test suite và `--index-only` của đúng shard đều đạt.

File này là yêu cầu setup rõ ràng: agent được phép tải và cài/tạo các dependency
còn thiếu trong phạm vi user hiện tại, tạo `.venv`, tạo/sửa các file local bị
Git ignore và chạy các phép kiểm chứng. Không cần xin lại quyền cho từng thao tác
đó.

Giới hạn bắt buộc:

- Không dùng quyền Administrator nếu không thật sự bắt buộc; nếu chỉ có thể tiếp
  tục bằng quyền admin thì dừng và hỏi người dùng.
- Không sửa `Machine PATH` hoặc `User PATH`; không dùng `setx PATH`.
- Không sửa source của preflight, dataset, shard hoặc build file của repository
  bên ngoài để ép pass.
- Không pull/checkout/reset/clean/stash, commit hoặc push nếu người dùng chưa yêu
  cầu riêng.
- Không xóa hoặc ghi đè run cũ. Không resume một run đã sinh hàng loạt kết quả
  terminal do thiếu JDK/Maven/Gradle.
- Không khởi chạy full shard trong task chuẩn bị này.

## 2. Đọc tài liệu và phát hiện đúng workspace

1. Xác định `<ARROW_ROOT>` bằng cấu trúc thực tế, không dựa vào đường dẫn của
   người khác. Root hợp lệ phải có tối thiểu:

   ```text
   preflight_tool/pyproject.toml
   preflight_tool/preflight/cli.py
   Java-version/AGENTS.md
   shards-5/summary.json
   preflight_tool_md/01-DUNG.md ... 05-HAN.md
   ```

2. Đọc đầy đủ file này, `<ARROW_ROOT>/Java-version/AGENTS.md`,
   `<ARROW_ROOT>/shards-5/README.md` và runbook ứng với `<SHARD_NO>`.
3. Ghi lại `git rev-parse HEAD` và `git status --short`. Không tự thay đổi Git
   state. Nếu code đang ở commit khác bản nhóm đã thống nhất thì báo người dùng
   trước khi setup tiếp.
4. Kiểm tra `preflight/runner.py`: fallback Windows khi không có wrapper phải là
   `mvn.cmd` và `gradle.bat`, không phải tên trần `mvn`/`gradle`. Test unit phải
   kiểm cả hai trường hợp. Nếu code chưa có bản sửa này, kết luận
   `BLOCKED_CODE_VERSION`; không sửa nóng hoặc chạy full trên checkout cũ trong
   task setup máy.
5. Kiểm tra có tiến trình `preflight.cli` đang chạy hay không. Nếu có, không cài
   chồng, không kill và không chạy song song; báo PID, command line và output của
   tiến trình đó rồi dừng để người dùng quyết định.

## 3. Xác minh OS, tài nguyên và dataset

Agent phải ghi bằng chứng vào báo cáo cuối:

- Windows edition/build và architecture thực của OS. File này không tự tuyên bố
  hỗ trợ Windows ARM64; nếu máy không phải x64 thì dừng và báo.
- CPU, tổng/khả dụng RAM và dung lượng trống trên volume chứa output và volume
  chứa toolchain.
- Nếu volume output còn dưới 20 GB hoặc RAM khả dụng dưới 8 GB, không âm thầm
  chạy với 5 worker; báo số thực tế để người dùng quyết định.
- `<INPUT_ROOT>` phải tồn tại và chứa `dataset/<project-id>/<sample>.json`, không
  phải folder rỗng, archive chưa giải nén hoặc bản sao manifest shard.
- Parse `shards-5/summary.json`, lấy `class_count` và SHA-256 tương ứng
  `shard-<SHARD_NO>.json`, rồi tự tính lại hash file. Hash hoặc số class lệch thì
  kết luận `BLOCKED_DATASET_OR_SHARD`.

Giá trị hiện tại để đối chiếu, nhưng file `summary.json` trong checkout vẫn là
nguồn chuẩn:

| Shard | Class |
| --- | ---: |
| 01–04 | 17.164 mỗi shard |
| 05 | 17.163 |

## 4. Chuẩn bị Python và package preflight

1. Tìm Python >=3.11 x64 đã có bằng `py -0p`, `py -3.11`, `python` và đường dẫn
   tuyệt đối. Kiểm cả version lẫn architecture.
2. Nếu chưa có, cài bản Python ổn định x64 từ nguồn chính thức cho **Current
   User**, không cần quyền admin. Không dùng package/mirror không xác minh.
3. Tại `<ARROW_ROOT>/preflight_tool`, nếu `.venv` chưa có thì tạo bằng Python đã
   xác minh. Nếu `.venv` có nhưng hỏng hoặc dùng Python <3.11, không xóa âm thầm;
   ghi tình trạng, đổi tên nó sang `.venv.broken-<UTC>` để có thể khôi phục rồi
   tạo `.venv` mới; không xóa môi trường cũ.
4. Dùng interpreter tuyệt đối của venv để cài package và dependency test:

   ```powershell
   & "<ARROW_ROOT>\preflight_tool\.venv\Scripts\python.exe" -m pip install --upgrade pip
   & "<ARROW_ROOT>\preflight_tool\.venv\Scripts\python.exe" -m pip install -e ".[test]"
   ```

5. Kiểm import `preflight`, `tree_sitter`, `tree_sitter_java`, `pytest` và chạy
   `python -m preflight.cli --help`. Help phải có `--shard`, `--resume`,
   `--index-only`, `--workers` và `--output-dir`.

Không chấp nhận việc `python` ở một terminal chạy được nhưng interpreter dùng
cho full run lại là Python khác.

## 5. Chuẩn bị và xác minh JDK 8/11/17/21

Thực hiện đúng toàn bộ quy trình trong `<ARROW_ROOT>/Java-version/AGENTS.md`.
Việc người dùng giao file chuẩn bị này được coi là yêu cầu setup JDK rõ ràng theo
AGENTS.md đó.

Yêu cầu tối thiểu:

- Dùng JDK portable từ nguồn OpenJDK chính thức, đúng Windows x64; khóa URL và
  checksum trước khi dùng.
- Cài/tái sử dụng bốn major 8, 11, 17, 21 trong vùng local đã được Git ignore.
- Gọi `java.exe -version` và `javac.exe -version` bằng đường dẫn tuyệt đối cho
  từng JDK; cả hai phải exit code 0 và cùng major.
- Ghi vendor, full version/build, JAVA_HOME và checksum vào các báo cáo local
  theo `Java-version/AGENTS.md`.
- Tạo hoặc cập nhật `Java-version/config.local.toml`; `[java.homes]` phải đủ bốn
  major và `[java].default_home` trỏ tới JDK 17 đã xác minh.
- `[input].root` phải là `<INPUT_ROOT>` thật. Giữ `workers=5`,
  `max_revision_candidates=500`, `build_timeout_seconds=900`,
  `keep_workspaces=false`, `keep_repo_cache=false`, trừ khi người dùng đã thống
  nhất giá trị khác.
- Parse lại TOML và xác nhận mọi đường dẫn trong config tồn tại. Không copy
  `config.local.toml` của máy khác.

## 6. Chuẩn bị Maven và Gradle fallback

Repository có `mvnw.cmd` hoặc `gradlew.bat` sẽ ưu tiên wrapper. Tuy nhiên nhiều
repository không có wrapper; khi đó runner gọi thẳng `mvn` hoặc `gradle`. Vì vậy
hai fallback này là bắt buộc cho full run của nhóm.

### 6.1 Phiên bản chuẩn

- Apache Maven `3.9.9`.
- Gradle `8.10.2`.

Nếu nhóm thay phiên bản chuẩn, agent phải ghi rõ khác biệt và nhận xác nhận trước
khi dùng; không tự chọn `latest`.

### 6.2 Cài local

1. Nếu version chuẩn đã tồn tại và chạy được thì tái sử dụng.
2. Nếu thiếu, tải binary distribution từ nguồn chính thức của Apache Maven và
   Gradle. Tải checksum từ nguồn chính thức, đối chiếu trước khi giải nén.
3. Cài vào vùng user-local hoặc vùng local của repository đã được Git ignore,
   ví dụ:

   ```text
   <ARROW_ROOT>/Java-version/.local/tools/apache-maven-3.9.9/
   <ARROW_ROOT>/Java-version/.local/tools/gradle-8.10.2/
   ```

4. Không cần và không được dựa vào việc thêm hai thư mục `bin` vào User PATH.
   Persistent PATH mới không cập nhật môi trường của agent/terminal đang chạy và
   chính điều này có thể tạo hàng nghìn kết quả exit code 127.

## 7. Tạo môi trường tiến trình dùng chung

Tạo file local, bị Git ignore:

`<ARROW_ROOT>/Java-version/.local/Enter-PreflightEnv.ps1`

Agent phải thay các placeholder trong script bằng đường dẫn tuyệt đối đã xác
minh trên **máy hiện tại**:

```powershell
$env:JAVA_HOME = '<JDK17_HOME>'
$env:PREFLIGHT_JDK_8 = '<JDK8_HOME>'
$env:PREFLIGHT_JDK_11 = '<JDK11_HOME>'
$env:PREFLIGHT_JDK_17 = '<JDK17_HOME>'
$env:PREFLIGHT_JDK_21 = '<JDK21_HOME>'
$env:PYTHONUTF8 = '1'

$preflightBins = @(
    '<MAVEN_3_9_9_BIN>',
    '<GRADLE_8_10_2_BIN>',
    (Join-Path $env:JAVA_HOME 'bin')
)
$env:Path = (($preflightBins + @($env:Path)) -join [IO.Path]::PathSeparator)
```

Mỗi terminal/session dùng để kiểm tra, chạy mới hoặc resume phải dot-source file
này **trước khi** gọi Python:

```powershell
. "<ARROW_ROOT>\Java-version\.local\Enter-PreflightEnv.ps1"
```

Nếu dùng `Start-Process`, phải dot-source trong tiến trình cha trước; child chỉ
kế thừa environment tại thời điểm được tạo. Mở terminal mới sau đó cũng phải
dot-source lại. Không coi User PATH là bằng chứng thay thế.

## 8. Cổng kiểm chứng toolchain — bắt buộc đạt hết

Sau khi dot-source `Enter-PreflightEnv.ps1`, chạy và lưu stdout, stderr, exit
code của tất cả lệnh sau:

```powershell
Get-Command git, java, javac, mvn, gradle | Select-Object Name, Source
git --version
java -version
javac -version
mvn --version
gradle --version
```

Điều kiện pass:

- `mvn` resolve đúng Maven 3.9.9 vừa xác minh.
- `gradle` resolve đúng Gradle 8.10.2 vừa xác minh.
- Cả Maven và Gradle báo JVM là JDK 17 đã chọn, không phải Java ngẫu nhiên từ
  PATH hệ thống.
- Không lệnh nào có exit code 127, `CommandNotFoundException`, “not recognized”
  hoặc lỗi không tìm thấy executable.

Tiếp theo, dùng **đúng Python của venv** kiểm từ trong subprocess:

```powershell
& "<ARROW_ROOT>\preflight_tool\.venv\Scripts\python.exe" -c "import shutil,subprocess; mvn=shutil.which('mvn.cmd'); gradle=shutil.which('gradle.bat'); assert mvn and gradle; subprocess.run([mvn,'--version'],check=True); subprocess.run([gradle,'--version'],check=True)"
```

Đây là cổng quan trọng nhất để ngăn lặp lại trường hợp hàng nghìn task bị đánh
`BUILD_TOOL_UNSUPPORTED` chỉ vì tiến trình Python không thấy build tool.

## 9. Smoke build thật cho Maven và Gradle

Agent tạo hai project Java tối giản trong một thư mục tạm riêng dưới `%TEMP%`,
không tạo trong dataset hay repository ARROW:

- Maven: `pom.xml` dùng compiler release 17 và một class Java tối giản; chạy
  `mvn -q -DskipTests compile`.
- Gradle: `settings.gradle`, `build.gradle` dùng plugin `java`, toolchain/source
  compatibility 17 và một class Java tối giản; chạy
  `gradle --no-daemon classes`.

Cả hai lệnh phải chạy bằng environment ở mục 7, exit code 0 và thực sự tạo file
`.class`. Chỉ kiểm `--version` là chưa đủ. Có thể xóa đúng thư mục smoke tạm sau
khi đã lưu log và xác minh thành công; nếu lỗi thì giữ để chẩn đoán.

Nếu lỗi do mạng tải plugin/dependency, proxy, TLS hoặc quyền ghi cache, kết luận
`BLOCKED_TOOLCHAIN_SMOKE`; không chuyển sang full run.

## 10. Chạy test của preflight với đủ bốn JDK

Từ `<ARROW_ROOT>/preflight_tool`, sau khi dot-source environment:

```powershell
& ".\.venv\Scripts\python.exe" -m pytest -q -ra
```

Yêu cầu:

- Không có test failed/error.
- Bốn case `test_real_jdk_version_matrix_when_configured` cho 8/11/17/21 phải
  chạy và pass, không được skip vì thiếu `PREFLIGHT_JDK_*`.
- Các skip khác do platform/remote/performance có thể hợp lệ; agent phải đọc lý
  do và ghi vào báo cáo, không đánh đồng `skipped` với `failed`.

## 11. Kiểm tra dataset và đúng shard bằng index-only

Tạo `RUN_ID` UTC mới. Output check phải là thư mục chưa tồn tại:

```powershell
& "<ARROW_ROOT>\preflight_tool\.venv\Scripts\python.exe" -m preflight.cli `
  --config "<ARROW_ROOT>\Java-version\config.local.toml" `
  --input-root "<INPUT_ROOT>" `
  --shard "<ARROW_ROOT>\shards-5\shard-<SHARD_NO>.json" `
  --index-only `
  --output-dir "<ARROW_ROOT>\preflight_tool\runs\setup-check-<SHARD_NO>-<RUN_ID>"
```

Agent phải parse output, không chỉ thấy exit code 0:

- `provenance.json` có đúng `shard_id` và shard SHA-256.
- `classes_selected` đúng `class_count` lấy từ `summary.json`.
- Manifest có đúng số task ID duy nhất, đúng tập task ID trong shard.
- Với snapshot chuẩn hiện tại: `raw_json_indexed=362414`,
  `deduplicated_classes=85819`; nếu khác thì báo và đối soát, không tự coi là
  tương đương.
- `reports/input_rejections.jsonl` rỗng. Nếu không rỗng, dừng trước full run.

`--index-only` không clone/build và không chứng minh candidate eligible; nhiệm vụ
của nó ở đây là xác minh dataset, shard và config input.

## 12. Quy tắc cho run tiếp theo

Chỉ sau khi báo cáo kết luận `READY_FOR_FULL_RUN`, agent chạy shard mới được
dùng runbook cá nhân tương ứng trong `preflight_tool_md/`.

Khi chạy full:

1. Dot-source `Enter-PreflightEnv.ps1` trong chính session khởi chạy.
2. Dùng output hoàn toàn mới và đúng shard của người đó.
3. Không dùng `--resume` với run cũ đã có hàng loạt lỗi môi trường. Các task đã
   checkpoint ở trạng thái terminal sẽ không tự chạy lại sau khi sửa PATH.
4. Resume chỉ dùng cho đúng run mới, khi môi trường và execution contract không
   đổi, để tiếp tục sau dừng máy/`Ctrl+C`.
5. Trong 20–50 kết quả đầu, kiểm log và phân bố status. Nếu thấy hàng loạt exit
   code 127, `mvn/gradle not found`, `JDK_*_MISSING` hoặc cùng một lỗi môi trường,
   dừng sớm, giữ evidence và sửa blocker; không để chạy đến hàng nghìn task.
6. Không kết luận lỗi chỉ vì có `BUILD_TOOL_UNSUPPORTED`: record không có
   `pom.xml`/Gradle build file có thể thật sự unsupported. Dấu hiệu lỗi môi
   trường là có build plan/attempt nhưng executable không chạy được, đặc biệt
   exit code 127 hoặc thông báo command not found.

## 13. Báo cáo bàn giao bắt buộc

Tạo:

`<ARROW_ROOT>/Java-version/.local/PREFLIGHT_SETUP_REPORT.md`

Báo cáo phải có:

- `<TEN_NGUOI_CHAY>`, `<SHARD_NO>`, đường dẫn ARROW/input thực tế, Git HEAD và
  git status.
- OS/CPU/RAM/disk.
- Python/venv/package version và CLI help check.
- Bảng JDK 8/11/17/21: vendor, full version, JAVA_HOME, java/javac, checksum.
- Maven/Gradle: version, install root, checksum, đường dẫn `Get-Command`, JVM
  thực dùng.
- Kết quả kiểm từ Python subprocess và hai smoke build thật.
- Kết quả pytest; nêu rõ bốn JDK toolchain test đã chạy hay bị skip.
- Hash/count shard và kết quả index-only.
- Đường dẫn `Enter-PreflightEnv.ps1`, config local và output setup-check.
- Mọi thay đổi local đã thực hiện; xác nhận không sửa User/Machine PATH.

Kết luận phải là đúng một trong các trạng thái:

- `READY_FOR_FULL_RUN`: mọi cổng bắt buộc ở trên đều pass.
- `BLOCKED_ENVIRONMENT`: thiếu/hỏng Python, Git, JDK, Maven hoặc Gradle.
- `BLOCKED_CODE_VERSION`: checkout chưa có fallback Windows
  `mvn.cmd`/`gradle.bat` hoặc thiếu tính năng CLI cần thiết.
- `BLOCKED_TOOLCHAIN_SMOKE`: version check pass nhưng Maven/Gradle build thật
  thất bại.
- `BLOCKED_DATASET_OR_SHARD`: dataset, hash, count hoặc index-only không đạt.
- `BLOCKED_RESOURCE`: tài nguyên không đủ và cần người dùng quyết định.

Không ghi `READY_FOR_FULL_RUN` nếu chỉ thêm PATH, chỉ chạy `--version`, còn test
JDK bị skip hoặc chưa chạy smoke build. Cuối cùng trả lời người dùng bằng tiếng
Việt, nêu ngắn gọn trạng thái, blocker nếu có và đường dẫn báo cáo.
