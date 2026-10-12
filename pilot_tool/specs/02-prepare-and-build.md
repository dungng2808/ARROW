# 02. S0: chuẩn bị repository và build

## 1. Tải repository

Mỗi repository có một **mirror** trong `<work_dir>/mirrors/<owner>__<repo>.git` (bare). Khóa theo repository cho mọi thao tác trên mirror.

| `repo_policy` | Tạo mirror | Lấy commit |
| --- | --- | --- |
| `commit_only_*` | `git init --bare` + `git remote add origin <repo_url>` | `git fetch --depth 1 --no-tags origin <checkout_sha>` |
| `full_history_keep` | `git clone --mirror <repo_url>` | đã có; nếu thiếu commit thì `git fetch origin <checkout_sha>` |

- Kiểm tra commit tồn tại: `git cat-file -e <sha>^{commit}`; thiếu thì lỗi hạ tầng `COMMIT_MISSING` (không thử lại).
- Lỗi mạng (timeout, 5xx, `Could not resolve host`): thử lại 3 lần, chờ 10s, 30s, 90s; vẫn lỗi thì `INFRA` với `reason=GIT_FETCH_FAILED`.
- Trên Windows MUST đặt `git config core.longpaths true` cho mirror và mọi worktree.
- Bản `--depth 1` không đủ khi build gọi git (ví dụ `git describe`, plugin `git-commit-id`). Dấu hiệu: log build có `fatal: No names found`, `shallow`, `git-commit-id`. Khi gặp, tool chạy `git fetch --unshallow --tags origin` cho repository đó, ghi `repo_history=full` vào `task.json` và chạy lại bước build đó **một lần**.

## 2. Workspace cho mỗi job

- Mỗi job cần build có workspace riêng: `<work_dir>/ws/<run_id>/<job_hash>/` tạo bằng `git --git-dir=<mirror> worktree add --detach <path> <sha>`. Không bao giờ build trên mirror.
- Sau checkout trên macOS/Linux: đặt quyền thực thi cho `mvnw`, `gradlew` nếu có.
- Xóa workspace khi job xong: `git worktree remove --force`; trên Windows thử lại tối đa 5 lần cách 2 giây nếu file bị khóa, rồi `git worktree prune`. Job lỗi và `keep_failed_workspaces=true` thì giữ.
- `commit_only_delete_after`: khi mọi job của mọi task thuộc repository trong shard đã có dòng ledger cuối cùng, xóa mirror. Mirror của repository còn job `INFRA` thì giữ.
- Đường dẫn workspace phải ngắn: `<job_hash>` là 12 ký tự đầu của SHA-256 khóa job.

## 3. Chọn JDK

Đầu vào: file build của module và tối đa 3 thư mục cha (module trước), `java_version` của bản ghi manifest (ví dụ `openjdk version "17.0.20.1" 2026-08-18`).

**Chốt 13/10/2026: ưu tiên mức Java khai trong file build, không dùng thẳng JDK của manifest.** Lý do: preflight chỉ biên dịch, không chạy test, và chỉ đọc `maven.compiler.release|target|java.version` trong `<properties>` của POM module; module khai `<target>1.8</target>` trong cấu hình `maven-compiler-plugin` hoặc khai ở POM cha bị preflight gán JDK 17 mặc định. Trên shard-01 của pilot, 5/9 task như vậy; test sẵn có dùng Mockito/ByteBuddy hoặc EqualsVerifier bản cũ fail khi chạy trên JDK 17 (NetCom2: 35/49 test fail với JDK 17, 49/49 pass với JDK 8). Test do model sinh dùng các thư viện này cũng sẽ fail vì môi trường, không phải vì ARROW. Đây là **sai lệch đã khai báo** so với JDK ghi trong manifest preflight; báo cáo ghi rõ.

1. Mức khai báo: lấy giá trị đầu tiên tìm thấy (module trước, rồi thư mục cha) theo thứ tự `release` (`maven.compiler.release`, `<release>`, `release.set(n)`/`release = n`), rồi `target` (`maven.compiler.target`, `<target>`, `java.version`, `targetCompatibility`), rồi `source`. Chỉ nhận giá trị dạng phiên bản (`8`, `1.8`, `11`); giá trị khác (ví dụ `${java.version}`, `src/it/java`) bỏ qua.
2. Có mức khai báo → JDK = 8 nếu mức ≤ 8, 11 nếu 9–11, 17 nếu 12–17, 21 nếu lớn hơn (`jdk_reason=declared_level`). Không có → major theo `java_version` của manifest (`jdk_reason=manifest`).
3. JDK đã chọn là 8 mà file build khớp `options.release|release.set(|--release|<release>|maven.compiler.release` → JDK 11 (javac 8 không có `--release`; `jdk_reason` thêm `+release_flag`).
4. Chuỗi thử ở baseline: JDK ở bước 2–3, rồi JDK của manifest (`manifest_fallback`), rồi các JDK còn lại của máy từ thấp tới cao (`fallback`). Với mỗi JDK, baseline biên dịch (`main_compile`, `test_compile`) rồi chạy test của module; gặp `BASELINE_BUILD_FAILED` hoặc `BASELINE_TEST_RUN_FAILED` (test sẵn có không chạy được: plugin test quá cũ cho JDK mới, JVM không khởi động vì tham số của JDK mới) thì thử JDK tiếp theo; log của lần thử sau có hậu tố `_jdk<n>`. Chọn JDK đầu tiên mà test sẵn có biên dịch và chạy được. Hết ứng viên → `INFRA` với mã lỗi của lần thử cuối. Timeout không thử JDK khác. Mọi lần thử ghi vào `task.json` (`jdk_attempts`). Test fail (assertion, exception trong test) không làm đổi JDK.

   Gặp ở pilot (13/10/2026): nifi-stanfordcorenlp không khai mức Java, JDK 17 của manifest làm `maven-surefire-plugin:2.20.1` lỗi trước khi chạy test → JDK 8 chạy được; jsoup khai Java 8 nhưng cấu hình Surefire dùng tham số JVM của JDK mới → JDK 17 của manifest chạy được.
5. Không có JDK tương ứng trong `jdk_homes` → `INFRA` với `reason=JDK_MISSING`.
6. Môi trường của mọi tiến trình build: `JAVA_HOME=<jdk>`, `PATH=<jdk>/bin:<PATH>`, `MAVEN_OPTS="-Xmx2g"`, `GRADLE_OPTS="-Xmx2g -Dorg.gradle.daemon=false"`, `LC_ALL=C.UTF-8` (macOS/Linux), `JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=UTC"`.

   **Locale và múi giờ cố định (chốt 13/10/2026).** JVM trên macOS và Windows lấy locale từ cài đặt hệ điều hành, không theo `LC_ALL`; máy để tiếng Việt in ngày giờ dạng `Tháng 10 20, 2021 at 9:00SA`. Gặp ở pilot: jitterted-ensembler có 3 test sẵn có fail vì lý do này, và test do model sinh có so sánh ngày giờ, số thập phân cũng sẽ fail theo máy. Vì vậy mọi tiến trình Java (Maven, Gradle, JVM chạy test mà Surefire/Gradle tách ra) dùng cùng locale `en_US` và múi giờ `UTC` trên mọi máy. Danh sách tham số ghi vào `run_config.lock.json` ở `build_flags.jvm` (trường khóa), nên run tạo trước thay đổi này phải chạy lại với `--run-id` mới. Project tự đặt `user.language`/`user.timezone` trong `argLine` của Surefire hoặc `jvmArgs` của Gradle thì giá trị của project được giữ.

Ghi vào `task.json`: `jdk_major`, `jdk_home`, `jdk_reason` (`declared_level` | `manifest` | `manifest_fallback` | `fallback`, có thể kèm `+release_flag`), `jdk_attempts`.

## 3b. Thư mục làm việc

`work_dir` (mirror và workspace) MUST không có dấu cách và chỉ gồm ký tự ASCII (chốt 13/10/2026). Đường dẫn có dấu cách làm test đọc resource qua URL fail (`Ki 9` thành `Ki%209`; gặp ở doraemon), nên baseline và kết quả khác nhau giữa các máy. Mặc định `ARROW/repo` nếu đường dẫn đó sạch; nếu đường dẫn ARROW có dấu cách hoặc chữ có dấu thì `~/arrow-repo` (macOS/Linux), `C:\arrow-repo` (Windows). Đã thử tạo symlink không dấu cách trỏ vào thư mục có dấu cách: không được, vì Java dùng đường dẫn thật. Trên máy Dũng-mac đã đổi tên `FPT/Ki 9` thành `FPT/Ki9` để giữ `ARROW/repo`.

Kiểm tra (`PathDiagnostics`, dùng chung cho setup bước 3, doctor và `run`), từng thư mục trong đường dẫn:

| Vấn đề | Mức | Vì sao |
| --- | --- | --- |
| Dấu cách | ✘ chặn | Java đổi thành `%20` trong URL, test đọc file báo không thấy |
| Chữ có dấu, ký tự ngoài ASCII | ✘ chặn | Mã hóa `%C3%…` trong URL; plugin cũ đọc sai mã hóa |
| `%` `#` `!` `;` `&` `^` `$` `'` `"` `` ` `` | ✘ chặn | Hỏng URL (`%`, `#`), đường dẫn jar (`!`), classpath Windows (`;`), script `mvnw.cmd`/`gradlew.bat` và shell |
| Đường dẫn Windows dài hơn 60 ký tự | ⚠ cảnh báo | Repository có thư mục sâu dễ vượt 260 ký tự nếu chưa bật LongPathsEnabled |

Mỗi vấn đề in ra: thư mục nào sai, vì sao, các bước sửa. Thư mục sai nằm trong đường dẫn ARROW → Cách A: đổi tên (tool gợi ý tên, ví dụ `Ki 9` → `Ki9`, `Hán` → `Han`), rồi chạy lại `install-arrow`, `install-litellm`, `arrow setup`, `arrow doctor`; Cách B: chọn thư mục ngoài ARROW. Doctor in khung "cần làm" dưới bảng; setup bước 3 in khung lỗi rồi hỏi lại; `run` dừng với mã `WORK_DIR_UNSAFE` kèm các bước.

Thư mục ARROW đổi tên/chuyển chỗ (`arrow_root` trong `machine.local.json` khác chỗ hiện tại): doctor ✘ kèm các bước (cài lại lệnh `arrow`, cài lại LiteLLM vì môi trường Python ghi cứng đường dẫn, chạy `arrow setup`); `arrow setup` tự đổi đường dẫn JDK và thư mục repository nằm trong ARROW cũ sang chỗ mới; `run` dừng với mã `ARROW_MOVED`. LiteLLM có file chương trình nhưng không chạy được → doctor ✘ "không chạy được" (không báo "chưa cài") và đề nghị cài lại. `doctor` báo ✘ và `run` từ chối chạy nếu vi phạm.

## 4. Lệnh build

### 4.1. Chọn chương trình

- Maven: có `mvnw` (Windows: `mvnw.cmd`) ở module hoặc thư mục cha trong repository **và** có `.mvn/wrapper/maven-wrapper.jar` hoặc `.mvn/wrapper/maven-wrapper.properties` → dùng wrapper; ngược lại dùng `maven_path` của máy.
- Gradle: có `gradlew` (Windows: `gradlew.bat`) và `gradle/wrapper/gradle-wrapper.jar` → dùng wrapper; ngược lại `gradle_path`.
- Ghi phiên bản sau khi đặt `JAVA_HOME`: `mvn -v`, `gradle --version` (timeout `tool_version_timeout_seconds`); lấy dòng `Apache Maven x.y.z` / `Gradle x.y`. Không lấy được thì ghi `unknown`, không coi là lỗi.

### 4.2. Module

`module_path` của manifest **không** tính từ gốc repository mà từ thư mục preflight đã gọi Maven/Gradle; thư mục đó có thể nằm sâu trong repository (ví dụ CUT `jqf/fuzz/src/main/java/…` có `module_path = fuzz`, Maven chạy tại `jqf/`; CUT `3rdParty/libsodium-jni-2.0.1/src/main/java/…` có `module_path = .`, Maven chạy tại `3rdParty/libsodium-jni-2.0.1/`). Vì vậy tool dò lại module từ `class_path` theo đúng thuật toán `detect_build` của preflight (`preflight_tool/preflight/runner.py`) và ghi kết quả vào `task.json` (`plan`); module dò được khác `module_path` của manifest thì ghi cảnh báo vào log.

- Maven: module = thư mục gần nhất phía trên CUT có `pom.xml`; thư mục gọi build (`invocation_root`) = thư mục **cao nhất** phía trên module (trong repository) có `pom.xml`. Module trùng thư mục gọi build → `mode=root`, chạy tại module với `-f pom.xml`. Khác → `mode=reactor`, chạy tại thư mục gọi build với `-f pom.xml -pl <module_path> -am`; Maven báo `Could not find the selected project in the reactor` → `mode=standalone`, chạy tại module với `-f pom.xml` (các bước sau dùng luôn chế độ này).
- Gradle: module = thư mục gần nhất có `build.gradle(.kts)`; thư mục gọi build = thư mục gần nhất từ module trở lên có `settings.gradle(.kts)` (không có thì là module). `mode=reactor` → chạy tại thư mục gọi build với task `<project_path>:<task>` (`project_path` = `:` + các phần của `module_path`); Gradle báo `Project '…' not found in root project` hoặc `not part of the build defined by settings file` → `mode=standalone`, chạy tại module với task không có tiền tố.
- `-am` làm Maven chạy cả test của module phụ thuộc trong `module_test`; chỉ báo cáo của module chứa CUT được đọc.

### 4.3. Cờ chung `[D2]`

Maven (thêm vào mọi lệnh): `-B -ntp -Dstyle.color=never -Dcheckstyle.skip=true -Dspring-javaformat.skip=true -Dspotless.check.skip=true -Dspotless.apply.skip=true -Dformatter.skip=true -Dimpsort.skip=true -Dlicense.skip=true -Drat.skip=true -Djacoco.skip=true -Dpmd.skip=true -Dcpd.skip=true -Dspotbugs.skip=true -Denforcer.skip=true -Dmaven.javadoc.skip=true -Dgpg.skip=true`. Windows thêm `-Dsurefire.useManifestOnlyJar=false`.

Maven wrapper của repository có thể cũ hơn Maven của máy. `-ntp` chỉ có từ Maven 3.6.1: với Maven cũ hơn (đọc từ bước `tool_version`), tool thay `-ntp` bằng cờ cùng tác dụng `-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn` (gặp ở pilot: wrapper Maven 3.6.0 của `springboot-guide`).

Gradle: `--no-daemon --console=plain -x checkstyleMain -x checkstyleTest -x spotlessCheck -x pmdMain -x pmdTest -x spotbugsMain -x spotbugsTest`. Task bị loại trừ mà không tồn tại thì Gradle báo lỗi; tool MUST đọc danh sách task một lần (`gradle tasks --all -q`, lưu trong `task.json`) và chỉ thêm `-x` cho task có thật.

Danh sách cờ nằm trong cấu hình thực nghiệm (`build.maven_extra_flags`, `build.gradle_extra_flags`), giống nhau trên mọi máy và mọi hệ thống (ARROW, EvoSuite).

### 4.4. Các lệnh

`<T>` là FQCN class test sinh ra, `<Tsimple>` là tên đơn giản.

| Bước | Maven (`<mvn> <module-args> <flags>`) | Gradle (`<gradle> <flags>`) |
| --- | --- | --- |
| `main_compile` | `-DskipTests compile` | `<p>:classes` |
| `test_compile` | `-DskipTests test-compile` | `<p>:testClasses` |
| `target_test` | `-DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dtest=<Tsimple> test` | `<p>:test --tests <T>` |
| `module_test` | `-DfailIfNoTests=false -Dmaven.test.failure.ignore=true test` | `<p>:test --continue` (thoát mã khác 0 khi có test fail là bình thường) |

- Mỗi lệnh có timeout `step_timeout_seconds`; quá thời gian thì dừng cả cây tiến trình (`Process.Kill(entireProcessTree: true)`) và ghi `TIMEOUT`.
- Trước mỗi bước chạy test, xóa báo cáo cũ: `target/surefire-reports/`, `build/test-results/test/` của module.
- Log mỗi lệnh ghi `logs/<step>[_<n>].log` với dòng đầu là lệnh, thư mục, JDK, thời gian bắt đầu; cuối là mã thoát và thời gian.

## 5. Baseline

Chạy **trước khi** ghi test sinh ra, trên workspace sạch:

1. `main_compile`, `test_compile`. Lỗi → `INFRA` với `reason=BASELINE_BUILD_FAILED` (preflight đã qua bước này, nên lỗi ở đây là do môi trường) và không gọi model cho task đó.
2. `module_test` **2 lần** (`build.baseline_runs`). Đọc báo cáo (Mục 6).
3. `baseline.json`:

```json
{"runs": 2, "tests_total": 214, "failing_in_all_runs": ["a.b.FooTest#x"], "flaky": ["a.b.BarTest#y"],
 "durations_seconds": [61.2, 58.9], "report_source": "surefire-xml"}
```

- `failing_in_all_runs`: fail ở cả hai lần (test hỏng sẵn). `flaky`: kết quả khác nhau giữa hai lần. Cả hai nhóm bị loại khỏi phép so sánh regression.
- Module không có test nào: `tests_total = 0`, bước regression luôn đạt.
- `module_test` timeout ở baseline → `INFRA` `reason=BASELINE_TIMEOUT`.
- `module_test` thoát mã khác 0 mà báo cáo không có test nào fail/error → `INFRA` `reason=BASELINE_TEST_RUN_FAILED` (lỗi build hoặc plugin, không phải test hỏng sẵn). Thoát mã khác 0 kèm test fail là bình thường với Gradle `--continue`.
- `baseline.json` ghi thêm `passing_in_all_runs`: test pass ở mọi lần chạy, là tập dùng để xét regression ở `03`.
- Xóa mirror theo `commit_only_delete_after` chỉ làm khi mọi stage của repository đã xong; mốc M2 mới có S0 nên chưa xóa mirror.

Baseline dùng chung cho mọi job của task (mọi model, prompt, cơ chế, EvoSuite).

## 6. Đọc báo cáo test

- Maven: `target/surefire-reports/TEST-*.xml` của module. Gradle: `build/test-results/test/TEST-*.xml`.
- Mỗi `<testcase>`: id = `classname#name`; kết quả `PASS` (không có con), `FAIL` (`<failure>`), `ERROR` (`<error>`), `SKIP` (`<skipped>`). Giữ `type`, `message`, 30 dòng đầu của nội dung.
- Không có file báo cáo: `report_source = none`; khi đó xác minh dựa vào log (Mục 4 của `03`).

## 7. Trích 6 thông tin repository (`repo_context.json`)

| Thông tin | Cách lấy |
| --- | --- |
| Build tool | Theo manifest (`maven`/`gradle`); kèm có wrapper hay không và phiên bản |
| Java version | `jdk_major` đã chọn, cùng `source`/`target`/`release` khai trong file build nếu đọc được |
| Dependencies | Maven: `dependency:list -DincludeScope=test -DoutputFile=…` (plugin `org.apache.maven.plugins:maven-dependency-plugin:3.8.1`); Gradle: `<p>:dependencies --configuration testRuntimeClasspath`. Lưu danh sách `group:artifact:version:scope`. Prompt chỉ dùng phần **test** và các thư viện được CUT import (xem `04`) |
| Test framework | Theo manifest (`junit4`/`junit5`/`testng`), kèm phiên bản lấy từ danh sách dependencies; thêm thư viện mock/assert có trong scope test (Mockito, AssertJ, Hamcrest) |
| Project structure | Cây thư mục của module tới độ sâu 3, chỉ thư mục; số file `.java` trong `src/main/java` và `src/test/java`; danh sách module nếu là multi-module |
| Source / test path | Source path = `class_path` của manifest; test path = thư mục test của module + đường dẫn package của CUT + `<CUT>_ArrowTest.java` |

Maven: `maven-dependency-plugin:3.8.1` đòi Maven 3.6.3 trở lên; với Maven cũ hơn dùng `maven-dependency-plugin:3.1.2` (cùng goal `list`, cùng tham số). Lệnh chạy kèm `-DskipTests test-compile` để module anh em trong reactor đã có class; `outputFile` là `target/arrow-dependencies.txt` của module.

Lệnh lấy dependencies lỗi thì ghi `dependencies_error` và dùng danh sách khai báo trực tiếp trong file build; không coi là lỗi hạ tầng.

## 8. Kiểm thử

- Unit: chọn JDK (manifest 8/11/17/21, ngoại lệ `--release`), dựng lệnh Maven/Gradle cho module gốc, module con có trong reactor, module ngoài reactor; chọn wrapper theo hệ điều hành; lọc `-x` theo danh sách task.
- Integration (fixtures): `sample-maven-junit4` (1 module), `sample-maven-multimodule-junit5` (module con dùng `-pl -am`), `sample-gradle-junit5`; mỗi fixture có 1 test cố ý fail để kiểm tra `failing_in_all_runs`, và 1 test fail ngẫu nhiên để kiểm tra `flaky`.
