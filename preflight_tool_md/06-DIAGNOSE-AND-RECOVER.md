# Chẩn đoán run preflight — cho agent mới không có context

Đọc toàn bộ file này cùng `00-PREPARE-WINDOWS.md` và file shard của người phụ
trách. File này dùng khi run đang chạy, bị dừng, hoặc có tỷ lệ lỗi đáng ngờ.
Mục tiêu là **xác định nguyên nhân bằng evidence**, sửa lỗi thuộc máy/tool nếu có,
rồi quyết định tiếp tục hay chạy mới. Tên `preflight_status` một mình **không
chứng minh nguyên nhân**. Không sửa dataset, shard, source của repo được kiểm tra,
hoặc ghi đè/xóa output cũ để tăng số `ELIGIBLE`.

## 1. Xác định run và trạng thái thật

1. Hỏi/đọc đường dẫn tuyệt đối `<RUN_DIR>`; xác nhận nó thuộc đúng shard và máy.
   Ghi Git HEAD, command line/PID thực tế, `provenance.json`, `reports/progress.json`,
   `state/run_state.sqlite` và thời điểm cập nhật của chúng. `progress.json` có
   thể cũ sau khi process chết; kiểm process và run lock trước khi kết luận.
2. Nếu chưa có `run_state.sqlite`, run còn ở pha indexing. Theo dõi kích thước
   và số hàng của `state/class_index.sqlite` theo hai mốc cách nhau 10–30 giây.
   Không gọi đó là lỗi build, không khởi động run thứ hai.
3. Nếu có checkpoint, đọc **read-only** bảng `tasks`, trường `result_json`,
   `reason_codes`, `build_attempts` (`stage`, `command`, `exit_code`, `log_path`)
   và đối chiếu file log. Không sửa SQLite đang chạy.
4. Đếm lỗi theo `repo_url` **và** `task_id`: nhiều class của cùng một repo có
   thể làm tỷ lệ status trông rất lớn. Lấy mẫu ít nhất 3 repo khác nhau cho mỗi
   nhóm phổ biến; nếu ít hơn 3 repo thì đọc tất cả.

Ví dụ lệnh PowerShell đọc checkpoint, thay đúng hai placeholder. Dùng Python
của chính venv, không cần cài package khác. Lệnh chỉ đọc; nếu DB đang bận thì
chờ rồi thử lại, không copy/di chuyển file đang mở:

```powershell
@'
import collections, json, pathlib, sqlite3, sys
run = pathlib.Path(sys.argv[1]).resolve()
db = run / "state" / "run_state.sqlite"
con = sqlite3.connect(db.as_uri() + "?mode=ro", uri=True, timeout=10)
rows = [json.loads(r[0]) for r in con.execute(
    "SELECT result_json FROM tasks WHERE status='COMPLETED' AND result_json IS NOT NULL")]
print("completed", len(rows))
print("by_status", dict(collections.Counter(r["preflight_status"] for r in rows)))
for status in sorted({r["preflight_status"] for r in rows}):
    group = [r for r in rows if r["preflight_status"] == status]
    missing = [r for r in group if not r.get("build_attempts")]
    exit127 = [r for r in group if any(a.get("exit_code") == 127 for a in r.get("build_attempts", []))]
    repos = collections.Counter(r.get("repo_url", "") for r in group)
    reasons = collections.Counter(x for r in group for x in r.get("reason_codes", []))
    print("\n", status, "classes", len(group), "repos", len(repos),
          "no_attempt", len(missing), "exit127", len(exit127))
    print("top_repos", repos.most_common(3), "top_reasons", reasons.most_common(8))
    seen = set()
    for r in group:
        repo = r.get("repo_url", "")
        if repo in seen: continue
        seen.add(repo)
        print("sample", r["task_id"], repo, r.get("reason_codes"),
              [(a.get("stage"), a.get("exit_code"), a.get("log_path"))
               for a in r.get("build_attempts", [])])
        if len(seen) == 3: break
'@ | & '<VENV_PYTHON>' - '<RUN_DIR>'
```

`log_path` có thể là đường dẫn tuyệt đối. Chỉ mở file tồn tại; nếu log thiếu,
ghi rõ thiếu evidence. Không suy diễn lỗi Git từ `CHECKOUT_FAILED` vì tool hiện
chỉ lưu status tổng quát cho một số ngoại lệ Git.

## 2. Bảng điều tra và sửa lỗi thuộc phía máy/tool

| Status | Bằng chứng cần đọc | Có thể sửa khi | Không được kết luận vội |
| --- | --- | --- | --- |
| `BUILD_TOOL_UNSUPPORTED` | Có/không có build attempt; exit code; `tool_version.log`, `main_compile.log`; repo có `pom.xml`/Gradle file không | Exit `127`, `EXECUTABLE_NOT_FOUND`, `WinError 2`: kiểm bản code gọi `mvn.cmd`/`gradle.bat`, PATH của tiến trình, cài fallback theo file 00 | Không có build plan/file Maven hoặc Gradle, hoặc repo dùng Ant/Bazel/SBT có thể là unsupported thật |
| `JDK_UNSUPPORTED` | `reason_codes`, `java_version`, command và log build | `JDK_HOME_MISSING`, `JDK_JAVA_MISSING`, `JDK_JAVAC_MISSING`, sai major/path, Maven/Gradle dùng JDK khác; tìm JDK trước, sửa config và process env | Repo đòi Java 6/7 có thể cần JDK bổ sung đã xác minh; không map giả sang JDK 17 |
| `DEPENDENCY_UNAVAILABLE` | `main_compile.log`/`test_compile.log`, URL/artifact, DNS/TLS/proxy và exit code | Mạng/proxy/certificate/cache/config `settings.xml` hoặc Gradle sai; sửa kết nối/cấu hình local rồi smoke build lại | Artifact đã xóa, repo private hoặc kho cũ ngừng phục vụ là vấn đề upstream; không tắt TLS hay thêm credential tuỳ tiện |
| `MAIN_BUILD_FAILED`, `TEST_COMPILE_FAILED` | Dòng lỗi đầu tiên và nguyên nhân gốc trong log, command, JDK, module path | Thiếu executable/JDK/plugin, path Windows quá dài, config local sai, plugin tải lỗi nhưng marker chưa bắt được | Lỗi cú pháp/compile của checkout thật là kết quả candidate; không sửa source repo để ép pass |
| `CHECKOUT_FAILED`, `CLONE_FAILED` | `repo_url`, `checkout_sha`, Git command/exit/stderr nếu còn, lỗi lặp theo repo | DNS/proxy/TLS/quyền, Git `longpaths`, quyền ghi, antivirus khóa file, mirror/worktree lỗi; xác minh bằng Git trong thư mục chẩn đoán riêng | Repo xóa/private, SHA không còn hoặc lịch sử upstream đổi không thể sửa bằng PATH; không xóa cache/evidence đang chạy |
| `SOURCE_INVALID` | `reason_codes`, `class_path`, `checkout_sha`, class thật ở revision được chọn | `SOURCE_PATH_NOT_FOUND` do dữ liệu/revision sai; `UNHANDLED_*` có thể là lỗi tool cần báo và tái hiện | Không tự sửa shard/evidence hash hoặc class source |
| `PROBE_FAILED` | `probe_compile.log`, API được chọn, main/test đã pass chưa | Probe do tool sinh sai hoặc compiler/JDK/dependency vẫn lỗi: tái hiện trong workspace riêng và báo bug có log | Không coi mọi probe failure là lỗi máy; không sửa repo để giấu lỗi |
| `BUILD_TIMEOUT` | Stage, thời lượng, CPU/RAM/disk, network | Thiếu tài nguyên, quá nhiều workers, mạng chậm hoặc cache bị khóa: xử lý blocker và xin quyết định nếu phải đổi workers/timeout | Repo build lâu thật có thể là kết quả đúng |
| `EXCLUDED`, `NEEDS_REVIEW` | Policy tag/reason | `NEEDS_REVIEW` cần người xem; `EXCLUDED` thường là chính sách | Không gọi là lỗi cài đặt và không cố chuyển thành `ELIGIBLE` |

`ELIGIBLE` chứng minh một số build thành công, nhưng **không chứng minh** Maven
và Gradle fallback đều hoạt động: các repo pass có thể dùng wrapper. `EXIT 127`
trong bất kỳ status nào vẫn phải điều tra.

### Cách xác minh trước khi sửa

1. **JDK:** với mỗi `JDK_UNSUPPORTED`, tách `JDK_VERSION_MISMATCH` khỏi lỗi
   xuất hiện trong `main_compile.log`. Đọc Java target trong `pom.xml` hoặc
   `build.gradle[.kts]`, so với `[java.homes]` và `default_home` của config
   lúc run. Gọi `java -version` **và** `javac -version` bằng đường dẫn tuyệt
   đối, rồi thử từ Python subprocess. Nếu target thuộc 8/11/17/21 nhưng
   mapping thiếu/sai, tìm trên mọi ổ và sửa theo file 00. Nếu target thật sự
   là 6/7 hoặc major khác: tìm JDK đúng major trên **mọi ổ** trước; nếu không
   có, quyền tải phần thiếu trong task này cho phép lấy artifact chính thức
   đúng OS/CPU có checksum kiểm được, cài local rồi thêm mapping major đó vào
   config cho **run mới**. Ghi rõ ma trận đã mở rộng và kiểm `java`/`javac`
   cùng smoke build; nếu không có artifact tin cậy/tương thích thì giữ
   `JDK_UNSUPPORTED`. Nếu parser nhận sai target, báo bug tool, không cài JDK
   theo target sai hoặc sửa code khi run đang chạy.
2. **Dependency/mạng:** lấy artifact URL và dòng nguyên nhân đầu tiên trong log.
   Kiểm DNS, TLS, proxy, quyền truy cập và disk; so sánh `~/.m2/settings.xml`,
   Gradle user home/init scripts và biến proxy trên máy với cấu hình nhóm đã
   xác minh. Tìm file/cache/cấu hình có sẵn trên mọi ổ trước khi tải phần thiếu.
   Thử một build smoke cùng environment. Không xóa toàn bộ `.m2`/`.gradle`,
   tắt TLS hoặc tự tạo credential. Artifact thật sự không còn thì giữ kết quả
   `DEPENDENCY_UNAVAILABLE`.
3. **Git:** trước tiên phân nhóm `CHECKOUT_FAILED` theo repo. Kiểm version Git,
   network và quyền ghi. Nếu log/reproduction báo `Filename too long`, kiểm
   `git config --show-origin --get core.longpaths`; có thể thử **chỉ trong
   tiến trình chẩn đoán** bằng `git -c core.longpaths=true ...`. Nếu cách này
   giải quyết, đưa `core.longpaths` vào môi trường Git của **run mới** sau khi
   ghi bằng chứng; không đổi global config âm thầm. Nếu mirror còn, kiểm
   `git fsck --connectivity-only` trên mirror bằng lệnh chỉ đọc. Nếu mirror đã
   được cleanup, tái hiện clone/checkout ở thư mục chẩn đoán riêng có đủ disk,
   giữ stdout/stderr; không sửa mirror/cache của run đang chạy.
4. **Build/probe/source:** mở đúng `log_path` của stage lỗi và đọc lỗi compiler
   hoặc resolver đầu tiên, không chỉ dòng `BUILD FAILED` cuối cùng. So sánh
   command, JDK và module path; tái hiện trong workspace chẩn đoán riêng nếu
   cần. Nếu nhiều repo khác nhau cùng một lỗi path/tool, đó là tín hiệu lỗi
   chung. Nếu chỉ một repo compile sai, ghi kết quả candidate. Với
   `SOURCE_INVALID`, xem `SOURCE_PATH_NOT_FOUND`, `SOURCE_FQN_MISMATCH` hay
   `UNHANDLED_*` trước khi quyết định có bug tool.

## 3. Quy tắc dừng sớm và quyết định resume/run mới

- Kiểm sau 20–50 kết quả đầu và sau mỗi 10–15 phút. Không đợi đến hàng nghìn
  class. Nếu cùng một lỗi môi trường lặp trên nhiều repo (đặc biệt exit `127`,
  sai JAVA_HOME, lỗi DNS/proxy, hết disk hoặc `CHECKOUT_FAILED` đồng loạt), dừng
  có kiểm soát, giữ nguyên output/log, chẩn đoán rồi sửa.
- Nếu lỗi chỉ riêng một số repository, evidence đầy đủ và toolchain chung đã
  pass, ghi nhận status rồi để run tiếp. Không có tỷ lệ `ELIGIBLE` tối thiểu
  chung cho mọi shard.
- `--resume` chỉ tiếp tục task chưa hoàn tất trong **cùng run, cùng code/schema
  và cùng đường dẫn đã khóa**. Task `COMPLETED` với status lỗi môi trường sẽ
  không chạy lại. Nếu muốn tính lại các task đó, đánh dấu run cũ không hợp lệ,
  chuẩn bị xong môi trường rồi tạo output/run mới. Fresh run tự indexing lại;
  không copy `class_index.sqlite` từ output cũ sang output mới.
- Không pull/sửa code trong khi tiến trình đang chạy. Bản code mới thay đổi
  fingerprint, có thể khiến checkpoint cũ không resume được. Không kill process
  hoặc xóa cache chỉ để làm sạch tỷ lệ lỗi.

## 4. Bàn giao kết luận có bằng chứng

Ghi trong `HANDOFF.md` hoặc báo cáo chẩn đoán local: RUN_ID, PID/command,
Git HEAD, completed/total, phân bố status theo class **và repo**, số exit 127,
top reason_codes, 3 task/log mẫu mỗi nhóm lớn, lỗi chung đã xác nhận, cách sửa,
kết quả kiểm lại và quyết định `CONTINUE`, `RESUME`, `NEW_RUN` hoặc `BLOCKED`.
Tách rõ **đã xác nhận bằng log** với **giả thuyết**. Nếu có thay đổi môi trường,
ghi đường dẫn/phiên bản trước và sau. Không tuyên bố đã sửa lỗi upstream nếu
chỉ thay PATH/JDK của máy.
