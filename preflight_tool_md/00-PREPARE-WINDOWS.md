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
| `<REPO_URL_CÔNG_KHAI_TRONG_SHARD>` | Agent chọn một URL công khai thật từ shard để kiểm Git/network; không dùng URL ví dụ |

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

2. Đọc đầy đủ file này, `06-DIAGNOSE-AND-RECOVER.md`,
   `<ARROW_ROOT>/Java-version/AGENTS.md`, `<ARROW_ROOT>/shards-5/README.md` và
   runbook ứng với `<SHARD_NO>`.
3. Ghi lại `git rev-parse HEAD` và `git status --short`. Không tự thay đổi Git
   state. Nếu code đang ở commit khác bản nhóm đã thống nhất thì báo người dùng
   trước khi setup tiếp.
4. Kiểm tra `preflight/runner.py`: fallback Windows khi không có wrapper phải là
   `mvn.cmd` và `gradle.bat`, không phải tên trần `mvn`/`gradle`. Commit
   `e84af81` phải là tổ tiên của HEAD hoặc agent phải xác minh bản sửa tương
   đương; test unit phải kiểm cả hai trường hợp. Nếu code cũ, chỉ cập nhật
   fast-forward từ remote đã thống nhất khi không có run hoạt động và đã kiểm
   mọi thay đổi local sẽ được giữ nguyên, không bị ghi đè. Nếu có tracked
   changes chồng lấn hoặc xung đột, giữ nguyên và báo `BLOCKED_CODE_VERSION`;
   không reset/stash/ghi đè/sửa nóng checkout đang chạy.
5. Kiểm tra có tiến trình `preflight.cli` đang chạy hay không. Nếu có, không cài
   chồng, không kill và không chạy song song; báo PID, command line và output của
   tiến trình đó rồi dừng để người dùng quyết định.

## 3. Kiểm kê trên tất cả ổ đĩa trước khi tải/cài

**Bắt buộc trước các mục 5–7.** Không kết luận “chưa cài” chỉ vì `Get-Command`
không thấy, hoặc vì không có ở `C:\tools`. Kiểm kê **tất cả ổ FileSystem đang
gắn và truy cập được** trên máy (ổ cố định, ổ rời, ổ mạng được map nếu đang
online). Ghi danh sách ổ đã kiểm, ổ không truy cập được và lý do. Không tự
kết nối ổ mạng/đăng nhập tài khoản khác.

1. Lấy danh sách bằng `Get-PSDrive -PSProvider FileSystem` và đối chiếu
   `Get-CimInstance Win32_LogicalDisk`; không mặc định chỉ có `C:`/`D:`.
2. Trên **mỗi ổ**, kiểm PATH, `Get-Command`, `where.exe`, Python launcher
   `py -0p`, registry `App Paths`/Uninstall (nếu có), rồi các vị trí thường
   dùng: root ARROW, `tools`, `Program Files`, `Users/<user>`, `.local`,
   `Java-version/runtime`, SDK manager/cache và các thư mục cài portable.
3. Nếu chưa tìm đủ, quét **tên file/thư mục có mục tiêu** trên từng ổ:
   `java.exe`, `javac.exe`, `mvn.cmd`, `gradle.bat`, `python.exe`, `git.exe`,
   thư mục dataset Classes2Test và `config.local.toml`. Ưu tiên `rg --files
   --hidden --no-ignore` với `--glob` cho từng tên trên một ổ mỗi lần; nếu
   không có `rg`, dùng `Get-ChildItem -Recurse` với filter cụ thể. Không quét
   nội dung mọi file JSON/source, không theo junction/symlink sang ổ khác,
   không xóa/di chuyển gì trong lúc tìm. Ghi mọi lỗi quyền truy cập; nếu một
   vùng quan trọng chưa đọc được thì trạng thái là “chưa xác minh”, không phải
   “không tồn tại”.

   Ví dụ lệnh chỉ đọc cho **từng** `<DRIVE_ROOT>` (thay bằng root ổ đã liệt
   kê, như `D:\`):

   ```powershell
   rg --files --hidden --no-ignore -g 'java.exe' -g 'javac.exe' `
     -g 'mvn.cmd' -g 'gradle.bat' -g 'python.exe' -g 'git.exe' `
     -g 'config.local.toml' -- '<DRIVE_ROOT>'
   ```

   Sau đó tìm riêng tên thư mục dataset theo từng ổ và xác minh vài JSON thật;
   không coi thư mục tên `dataset` bất kỳ là đúng snapshot. Nếu `rg` báo lỗi
   quyền hoặc ổ offline, ghi rõ ổ/vùng chưa xác minh.
4. Với **mọi bản tìm được**, xác minh OS/CPU, version đầy đủ, chữ ký/checksum
   hoặc nguồn tin cậy khi có, `java` **và** `javac` cùng major, Maven/Gradle
   chạy được bằng đường dẫn tuyệt đối và từ Python subprocess. Chỉ tái dùng
   bản hợp lệ; không chọn bản đầu tiên theo tên folder. Ghi bảng `đã tìm ở đâu
   → phiên bản → dùng/không dùng → lý do` vào báo cáo.
5. **Chỉ khi kết thúc kiểm kê tất cả ổ mà vẫn thiếu bản hợp lệ**, mới tải phần
   thiếu từ nguồn chính thức, kiểm checksum rồi cài local. Nếu dataset thiếu,
   chỉ lấy đúng snapshot được nhóm cung cấp/xác minh bằng shard hash và
   index-only; không tự lấy một bản Classes2Test bất kỳ trên Internet.

Nếu việc quét rộng quá lâu, ghi tiến độ theo từng ổ và tiếp tục; không đánh dấu
`READY_FOR_FULL_RUN` khi chưa kiểm hết các ổ truy cập được. Không chạy binary
không rõ nguồn chỉ để thử version.

## 4. Xác minh OS, tài nguyên và dataset

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

## 5. Chuẩn bị Python và package preflight

1. Tìm Python >=3.11 x64 đã có bằng `py -0p`, `py -3.11`, `python` và đường dẫn
   tuyệt đối. Kiểm cả version lẫn architecture.
2. Nếu không tìm được Python hợp lệ sau kiểm kê mục 3, cài bản ổn định x64 từ
   nguồn chính thức cho **Current User**, không cần quyền admin. Không dùng
   package/mirror không xác minh.
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

## 6. Chuẩn bị và xác minh JDK 8/11/17/21

Thực hiện đúng toàn bộ quy trình trong `<ARROW_ROOT>/Java-version/AGENTS.md`.
Việc người dùng giao file chuẩn bị này được coi là yêu cầu setup JDK rõ ràng theo
AGENTS.md đó.

Yêu cầu tối thiểu:

- Dùng JDK portable từ nguồn OpenJDK chính thức, đúng Windows x64; khóa URL và
  checksum trước khi dùng.
- Ưu tiên tái sử dụng JDK hợp lệ đã tìm thấy trên bất kỳ ổ nào, kể cả ngoài
  repository; tải/cài major còn thiếu vào vùng local bị Git ignore. Ghi rõ
  đường dẫn và nguồn của từng bản.
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
- Bốn major này là baseline. Nếu log của run cho thấy project thật sự cần
  major khác, làm theo mục JDK trong `06-DIAGNOSE-AND-RECOVER.md`: tìm trên mọi
  ổ trước, chỉ tải artifact được xác minh, thêm mapping tường minh và ghi rõ
  ma trận mở rộng cho run mới.

## 7. Chuẩn bị Maven và Gradle fallback

Repository có `mvnw.cmd` hoặc `gradlew.bat` sẽ ưu tiên wrapper. Tuy nhiên nhiều
repository không có wrapper; khi đó runner trên Windows gọi `mvn.cmd` hoặc
`gradle.bat`. Vì vậy hai fallback này là bắt buộc cho full run của nhóm.

### 6.1 Phiên bản chuẩn

- Apache Maven `3.9.9`.
- Gradle `8.10.2`.

Nếu nhóm thay phiên bản chuẩn, agent phải ghi rõ khác biệt và nhận xác nhận trước
khi dùng; không tự chọn `latest`.

### 6.2 Cài local

1. Nếu version chuẩn đã tìm được trên bất kỳ ổ nào và chạy được thì tái sử dụng.
2. Nếu thiếu sau kiểm kê mục 3, tải binary distribution từ nguồn chính thức
   của Apache Maven và Gradle. Tải checksum từ nguồn chính thức, đối chiếu
   trước khi giải nén.
3. Cài vào vùng user-local hoặc vùng local của repository đã được Git ignore,
   ví dụ:

   ```text
   <ARROW_ROOT>/Java-version/.local/tools/apache-maven-3.9.9/
   <ARROW_ROOT>/Java-version/.local/tools/gradle-8.10.2/
   ```

4. Không cần và không được dựa vào việc thêm hai thư mục `bin` vào User PATH.
   Persistent PATH mới không cập nhật môi trường của agent/terminal đang chạy và
   chính điều này có thể tạo hàng nghìn kết quả exit code 127.

## 8. Tạo môi trường tiến trình dùng chung

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

## 9. Cổng kiểm chứng toolchain — bắt buộc đạt hết

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
- `git ls-remote <REPO_URL_CÔNG_KHAI_TRONG_SHARD> HEAD` chạy được từ chính môi
  trường này. Nếu một repo lỗi, thử repo công khai thứ hai trước khi quy thành
  lỗi mạng; ghi DNS/TLS/proxy/credential thực tế, không tự tắt xác minh TLS.

Tiếp theo, dùng **đúng Python của venv** kiểm từ trong subprocess:

```powershell
& "<ARROW_ROOT>\preflight_tool\.venv\Scripts\python.exe" -c "import shutil,subprocess; mvn=shutil.which('mvn.cmd'); gradle=shutil.which('gradle.bat'); assert mvn and gradle; subprocess.run([mvn,'--version'],check=True); subprocess.run([gradle,'--version'],check=True)"
```

Đây là cổng quan trọng nhất để ngăn lặp lại trường hợp hàng nghìn task bị đánh
`BUILD_TOOL_UNSUPPORTED` chỉ vì tiến trình Python không thấy build tool.

## 10. Smoke build thật cho Maven và Gradle

Agent tạo hai project Java tối giản trong một thư mục tạm riêng dưới `%TEMP%`,
không tạo trong dataset hay repository ARROW:

- Maven: `pom.xml` dùng compiler release 17 và một class Java tối giản; chạy
  `mvn -q -DskipTests compile`.
- Gradle: `settings.gradle`, `build.gradle` dùng plugin `java`, toolchain/source
  compatibility 17 và một class Java tối giản; chạy
  `gradle --no-daemon classes`.

Cả hai lệnh phải chạy bằng environment ở mục 8, exit code 0 và thực sự tạo file
`.class`. Chỉ kiểm `--version` là chưa đủ. Có thể xóa đúng thư mục smoke tạm sau
khi đã lưu log và xác minh thành công; nếu lỗi thì giữ để chẩn đoán.

Nếu lỗi do mạng tải plugin/dependency, proxy, TLS hoặc quyền ghi cache, kết luận
`BLOCKED_TOOLCHAIN_SMOKE`; không chuyển sang full run.

## 11. Chạy test của preflight với đủ bốn JDK

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

## 12. Kiểm tra dataset và đúng shard bằng index-only

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

## 13. Quy tắc cho run tiếp theo

Chỉ sau khi báo cáo kết luận `READY_FOR_FULL_RUN`, agent chạy shard mới được
dùng runbook cá nhân tương ứng trong `preflight_tool_md/`. Khi có status bất
thường, thực hiện `06-DIAGNOSE-AND-RECOVER.md`; không suy nguyên nhân từ tên
status hoặc tỷ lệ `ELIGIBLE`.

Khi chạy full:

1. Dot-source `Enter-PreflightEnv.ps1` trong chính session khởi chạy.
2. Dùng output hoàn toàn mới và đúng shard của người đó.
3. Không dùng `--resume` với run cũ đã có hàng loạt lỗi môi trường. Các task đã
   checkpoint ở trạng thái terminal sẽ không tự chạy lại sau khi sửa PATH.
4. Resume chỉ dùng cho đúng run mới, khi môi trường và execution contract không
   đổi, để tiếp tục sau dừng máy/`Ctrl+C`.
5. Trong 20–50 kết quả đầu, kiểm checkpoint JSON, log và phân bố status theo
   **class lẫn repo**. Điều tra mọi exit 127, `JDK_*_MISSING`, lỗi DNS/TLS,
   `CHECKOUT_FAILED` lặp nhiều repo, và `MAIN_BUILD_FAILED` có chung signature.
   Nếu xác nhận blocker môi trường lặp lại, dừng sớm, giữ evidence và sửa;
   không để chạy đến hàng nghìn task.
6. Không kết luận lỗi chỉ vì có `BUILD_TOOL_UNSUPPORTED`: record không có
   `pom.xml`/Gradle build file có thể thật sự unsupported. Dấu hiệu lỗi môi
   trường là có build plan/attempt nhưng executable không chạy được, đặc biệt
   exit code 127 hoặc thông báo command not found.

## 14. Báo cáo bàn giao bắt buộc

Tạo:

`<ARROW_ROOT>/Java-version/.local/PREFLIGHT_SETUP_REPORT.md`

Báo cáo phải có:

- `<TEN_NGUOI_CHAY>`, `<SHARD_NO>`, đường dẫn ARROW/input thực tế, Git HEAD và
  git status.
- Danh sách **tất cả ổ đã tìm**, vị trí từng bản Python/Git/JDK/Maven/Gradle
  tìm được, kết quả xác minh, lý do chọn/từ chối và phần thực sự phải tải.
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
- `BLOCKED_ENVIRONMENT`: thiếu/hỏng Python, Git, JDK, Maven hoặc Gradle sau khi
  đã kiểm kê mọi ổ truy cập được.
- `BLOCKED_CODE_VERSION`: checkout chưa có fallback Windows
  `mvn.cmd`/`gradle.bat` hoặc thiếu tính năng CLI cần thiết.
- `BLOCKED_TOOLCHAIN_SMOKE`: version check pass nhưng Maven/Gradle build thật
  thất bại.
- `BLOCKED_DATASET_OR_SHARD`: dataset, hash, count hoặc index-only không đạt.
- `BLOCKED_RESOURCE`: tài nguyên không đủ và cần người dùng quyết định.

Không ghi `READY_FOR_FULL_RUN` nếu chỉ thêm PATH, chỉ chạy `--version`, còn test
JDK bị skip hoặc chưa chạy smoke build. Cuối cùng trả lời người dùng bằng tiếng
Việt, nêu ngắn gọn trạng thái, blocker nếu có và đường dẫn báo cáo.
