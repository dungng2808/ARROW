# Đề nghị verify bản vá path security đa nền tảng

- Người gửi: `dungng2808`
- Người nhận: `CuuTroHan`
- Thời điểm: 22/09/2026, 20:31 (Asia/Ho_Chi_Minh)
- Phạm vi ban đầu: `preflight_tool/preflight/runner.py`, hàm `_path_candidates`

## Bối cảnh

Khi chạy local test gate trên macOS, test
`test_java_target_and_workspace_path_security` fail với input
`C:/secret.java`. Nguyên nhân là `pathlib.Path` trên POSIX coi Windows drive
path là relative, nên `_path_candidates` trả về candidate nằm dưới workspace
thay vì reject ngay theo hợp đồng path-security đa nền tảng.

Lỗi không cho phép đọc trực tiếp `C:/secret.java` từ macOS vì candidate vẫn bị
ghép dưới workspace. Tuy vậy, hành vi hiện tại không nhất quán giữa OS và vi
phạm điều kiện runner phải từ chối absolute Windows path, UNC path và drive-
relative path trước khi resolve source trong worktree.

## Đề xuất bản vá

Không dùng riêng `Path.is_absolute()` của OS hiện tại để quyết định input hợp
lệ. Validate chuỗi path theo quy tắc chung trước, rồi mới tạo `Path`:

```python
raw = relative.strip().replace("\\", "/")

if (
    not raw
    or raw.startswith("/")
    or raw.startswith("//")
    or re.match(r"^[A-Za-z]:", raw)
):
    return []

parts = PurePosixPath(raw).parts
if ".." in parts:
    return []

candidate = (workspace / Path(*parts)).resolve()
if not candidate.is_relative_to(workspace.resolve()):
    return []
```

Logic giữ nguyên mục đích hiện tại: sau khi validate path gốc, runner vẫn có
thể thử biến thể bỏ module prefix khi cần. Mọi candidate được trả về đều phải
được kiểm tra nằm trong workspace sau `resolve()`.

## Ca kiểm thử đề nghị bổ sung

| Nhóm | Input | Kết quả mong đợi |
| --- | --- | --- |
| POSIX absolute | `/etc/passwd` | Reject |
| Parent traversal | `../secret.java`, `src/../secret.java` | Reject |
| Windows absolute | `C:/secret.java`, `C:\\secret.java` | Reject trên mọi OS |
| Windows drive-relative | `C:secret.java` | Reject trên mọi OS |
| UNC | `\\\\server\\share\\secret.java` | Reject |
| Windows device path | `\\\\?\\C:\\secret.java` | Reject |
| Symlink escape | `linked/secret.java`, khi `linked` trỏ ngoài workspace | Reject |
| Valid POSIX path | `src/main/java/acme/Thing.java` | Accept |
| Valid Windows separator | `src\\main\\java\\acme\\Thing.java` | Normalize và accept |

Các test này nên chạy cùng một expected result trên Windows, macOS và Linux.
Test assertion phải kiểm tra cả danh sách candidate rỗng với input bị loại và
candidate sau `resolve()` luôn là descendant của workspace.

## Phát hiện bổ sung sau khi rà soát runner

Các điểm dưới đây được bổ sung để verify cùng đợt; chúng độc lập với bản vá
path security và chưa được xem là đã sửa.

### 1. Multi-thread và Git worktree

`run_all()` dùng `ThreadPoolExecutor`, nhận `--workers` và mặc định hai worker.
Mirror clone/fetch được lock theo `repo_url`. Tuy vậy, runner submit toàn bộ CUT
vào executor ngay từ đầu. Với dataset hiện tại có 85.819 CUT, điều này tạo một
hàng đợi future lớn trong RAM. Ngoài ra, thao tác `git worktree add/remove` cho
nhiều CUT thuộc cùng mirror diễn ra ngoài mirror lock.

Đề nghị verify:

- cần giới hạn số task đang chờ (batch/bounded in-flight) thay vì submit toàn
  bộ dataset hay không;
- Git worktree có cần một lock ngắn theo mirror cho add/remove để tránh race;
- fixture integration phải chạy ít nhất hai CUT **khác nhau** cùng repository,
  không chỉ hai JSON bị deduplicate thành một CUT.

### 2. Chọn wrapper theo hệ điều hành

`detect_build()` hiện tìm Maven theo thứ tự `mvnw.cmd`, rồi `mvnw`; Gradle theo
thứ tự `gradlew.bat`, rồi `gradlew`, không phụ thuộc OS. Trên macOS/Linux, nếu
repository chứa cả hai wrapper thì tool có thể chọn `.cmd`/`.bat` và process
không thực thi được. Ngược lại, Windows cần ưu tiên wrapper batch.

Đề nghị verify và sửa thứ tự wrapper theo `os.name`, rồi thêm test cùng fixture
có cả hai wrapper trên Windows và macOS/Linux.

### 3. Timeout process tree ngoài Windows

Windows dùng `taskkill /T`, nhưng macOS/Linux chỉ gọi `process.kill()` cho
process cha. Maven/Gradle có thể để lại process con khi timeout. Đây là rủi ro
resource leak trong full run; cần verify bằng fixture tạo child process, rồi
quản lý process group trên POSIX trước khi coi timeout contract là đa nền tảng.

### 4. Xác minh JDK/version

`_java_env()` có map JDK theo version, trả `JDK_HOME_MISSING` khi path cấu hình
không tồn tại và `JDK_UNSUPPORTED` khi không tìm được executable `java`. Nhưng
hàm chưa kiểm tra exit code của `java -version` và chưa parse/so sánh major
version thực chạy với target từ Maven/Gradle. Một Java launcher trả lỗi vẫn có
thể bị ghi như version, hoặc build dùng JDK khác target.

Đề nghị verify:

- `java -version` phải exit 0, có `java` và `javac`, và major version phải khớp
  target đã phát hiện;
- thiếu mapping/version không phù hợp phải trả `JDK_UNSUPPORTED` với reason
  code phân biệt;
- test JDK 8/11/17/21 trên Windows và macOS/Linux, không chỉ mock subprocess.

### 5. Gói `Java-version/` dự kiến commit

Hiện folder local có bốn JDK macOS ARM (8/11/17/21), tổng khoảng 1,4 GB. Có các
file đơn 123–186 MB và map/script đang chứa absolute path của workspace khác.
Gói này chưa dùng được trên Windows. Đây không phải phần của patch runner;
trước khi đưa vào repository cần quyết định cơ chế artifact và bộ JDK Windows
tương ứng.

## Đề nghị verify

Xin verify các điểm sau trước khi merge bản vá:

1. Bộ quy tắc reject ở trên đã đủ bao phủ absolute, drive-relative, UNC,
   device path, traversal và symlink escape chưa.
2. Việc reject mọi prefix `^[A-Za-z]:` có phù hợp với contract đa nền tảng hay
   cần cho phép trường hợp nào trong Classes2Test.
3. Vị trí kiểm tra containment sau `resolve()` đã đủ để bảo vệ luồng đọc source
   và test source trong worktree chưa.
4. Mức độ lỗi nên được giữ là High/P0 cho đến khi test gate pass trở lại hay
   cần điều chỉnh.
5. Có edge case Windows/macOS/Linux nào cần thêm vào fixture trước khi sửa code
   không.
6. Phương án bounded concurrency và khóa worktree theo mirror có cần thiết
   trước khi chạy nhiều worker trên full dataset không.
7. Thứ tự wrapper theo OS và cleanup process tree trên POSIX nên được đưa vào
   cùng bản vá đa nền tảng hay tách issue.
8. Contract `JDK_UNSUPPORTED` cần yêu cầu các điều kiện xác minh JDK nào.
9. Cách phát hành JDK 8/11/17/21 cho cả macOS/Windows nên dùng Git LFS, release
   artifact hay kho binary riêng.

Sau khi nhận được verify, nhóm sẽ tách hoặc patch `runner.py`, bổ sung
unit/security/integration test và chạy lại full local gate, performance gate,
toolchain matrix và actual-dataset index smoke.
