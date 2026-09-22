# Phản hồi verify path security và runner đa nền tảng

- Người gửi: `CuuTroHan`
- Người nhận: `dungng2808`
- Thời điểm: 22/09/2026, 21:05 (Asia/Ho_Chi_Minh)
- Tài liệu được xét: `20260922-2031-dungng2808-to-CuuTroHan-de-nghi-verify-path-security-da-nen-tang.md`, commit `46ec8f5`.
- Phạm vi thực hiện: `preflight_tool/preflight/runner.py`, unit/integration test và hướng dẫn chạy test; không đưa JDK binary vào Git.

## Kết luận verify

Nhận định chính **đúng**. Trên POSIX, `Path("C:/secret.java")` là relative, nên test cũ fail và runner không từ chối Windows absolute path theo contract. Điều này chưa chứng minh đọc được file ngoài workspace trên macOS/Linux, nhưng vẫn là lỗi validate đầu vào và làm hỏng test gate. Đề xuất reject path trước khi tạo `Path`, kiểm tra containment sau `resolve()` và giữ biến thể bỏ module prefix là phù hợp. Reject mọi prefix `^[A-Za-z]:` là hợp lý cho đường dẫn class/test trong Classes2Test; không có nhu cầu hợp lệ đã biết với drive-relative path.

Tôi đề xuất **High/P1** cho lỗi này đến khi chạy qua test gate trên Windows, macOS và Linux; chưa có bằng chứng để gọi P0 (khai thác đọc ngoài workspace hoặc mất dữ liệu). Nếu threat model coi dataset không tin cậy và phát hiện được đường đọc/ghi ra ngoài, cần nâng mức ngay.

## Phần đã sửa theo yêu cầu

1. `_path_candidates` chuẩn hóa dấu phân cách, từ chối path rỗng, absolute POSIX/UNC/device, Windows drive hoặc drive-relative, `..` và NUL. Đường dẫn gốc và mọi candidate đều phải nằm trong workspace sau `resolve()`. Nếu đường gốc đi qua symlink thoát workspace, không thử fallback bỏ prefix. Luồng đọc parent source và đường ghi probe cũng được kiểm tra containment; wrapper không còn được tìm ngoài workspace.
2. Maven/Gradle chọn wrapper phù hợp `os.name`: `.cmd`/`.bat` trên Windows, shell wrapper trên POSIX; nếu thiếu thì dùng tool hệ thống thay vì chọn wrapper sai OS.
3. `run_all` giữ tối đa `2 × workers` futures đang chờ; thao tác `git worktree add/remove` được khóa ngắn theo mirror. Integration fixture nay chạy **hai CUT khác nhau** cùng repository, thay cho hai JSON bị deduplicate thành một CUT.
4. Timeout dừng cả process tree: `taskkill /T /F` trên Windows và process group riêng + `SIGKILL` trên POSIX. Test tạo child process để kiểm tra cleanup trên OS đang chạy.
5. `_java_env` đòi cả `java` và `javac`, kiểm tra exit code, parse major version của hai launcher và đối chiếu với target nếu đã phát hiện. Các reason code phân biệt: `JDK_HOME_MISSING`, `JDK_JAVA_MISSING`, `JDK_JAVAC_MISSING`, `JDK_VERSION_COMMAND_FAILED`, `JDK_VERSION_UNPARSEABLE`, `JDK_VERSION_MISMATCH`; tất cả vẫn có `preflight_status=JDK_UNSUPPORTED`.

## Bằng chứng và giới hạn

- Windows local gate: **169 passed, 6 skipped**; coverage **96.00% lines, 88.68% branches**, đạt ngưỡng trong `TESTING.md`. Test process child trên Windows pass. Performance gate 10.000 JSON pass trong giới hạn 120 giây/512 MiB của test.
- JDK 21 thực (`T:/Java-version/java-21`) pass. Unit test đã bao phủ logic 8/11/17/21; test với JDK thực có thể chạy bằng `PREFLIGHT_JDK_8/11/17/21` trên mỗi OS. JDK thực 8/11/17 và macOS/Linux **chưa được chạy tại máy này**. Hai test symlink skip trên Windows vì không có quyền tạo symlink.
- Chưa chạy full preflight trên 85.819 CUT hoặc actual-dataset index smoke sau thay đổi này; performance gate nói trên chỉ kiểm tra index 10.000 JSON. Không coi đây là chứng nhận đầy đủ đa nền tảng.
- Kiểm tra containment qua `resolve()` bảo vệ đường dẫn ở thời điểm kiểm tra, nhưng không loại trừ tuyệt đối race nếu một tác nhân khác có thể thay symlink đồng thời trong worktree. Nếu dataset/worktree bị tác nhân đối địch sửa trong lúc chạy, cần mô hình sandbox/handle-based access riêng.

## Gói JDK và bước còn lại

Không commit `Java-version/` 1,4 GB: gói hiện là macOS ARM, chứa absolute path máy khác và không đáp ứng Windows. Đề nghị chọn release artifact hoặc kho binary riêng theo OS/architecture, kèm checksum SHA-256, license và mapping cấu hình; Git LFS chỉ nên chọn nếu nhóm chấp nhận quota, clone size và quy trình phân phối tương ứng. Quyết định này thuộc quản lý artifact, không phải bản vá runner.

Trước khi coi patch đạt contract đa nền tảng, cần chạy cùng suite trên macOS/Linux, real-JDK matrix 8/11/17/21 nơi có toolchain, rồi actual-dataset smoke và full run theo gate đã định. Các phần code có thể kiểm chứng cục bộ đã được sửa trong commit chứa phản hồi này; các bước phụ thuộc môi trường ngoài được giữ là việc còn mở, không ghi nhận là đã hoàn thành.
