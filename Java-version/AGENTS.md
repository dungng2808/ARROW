# Rule: chuẩn bị JDK cho máy chạy ARROW Preflight

## Phạm vi và kết quả cần đạt

Khi người dùng yêu cầu setup Java theo file này, agent tự tải và kiểm tra đủ
**JDK 8, 11, 17, 21** cho máy hiện tại vào `Java-version/runtime/`.
Tải JDK có cả `java` và `javac`, không chỉ JRE. File này là hướng dẫn cho agent,
không phải script tự chạy khi git pull.

Mỗi thành viên chỉ setup máy mình. Không sửa code preflight, không tạo lại hệ
thống installer chung, không commit/push trong quá trình setup cục bộ.

## 1. Xác định máy và vị trí repository

- Xác định đường dẫn tuyệt đối đến ARROW từ vị trí file này; không hard-code
  username, ổ đĩa hoặc đường dẫn của máy khác.
- macOS: kiểm tra OS và CPU; phân biệt Apple Silicon (`aarch64`/`arm64`) và
  Intel (`x86_64`/`x64`). Chú ý terminal có thể đang chạy qua Rosetta.
- Windows: xác định architecture thực của máy, không chỉ của PowerShell.
- Tên platform ghi trong metadata: `macos-arm64`, `macos-x64`, `windows-x64`.
  Không tạo tầng folder platform trong `runtime/`.
  Windows ARM64 cần tìm artifact native phù hợp; nếu không có đủ các major,
  báo rõ phần thiếu, không tự coi binary x64 chạy giả lập là đã được hỗ trợ.
- Chỉ tải artifact cho OS/CPU của máy hiện tại. Không tải cả bộ Windows về Mac.

## 2. Chọn nguồn và khóa artifact trước khi tải

- Tra cứu website/API chính thức của nhà cung cấp OpenJDK, ví dụ Eclipse
  Adoptium hoặc Azul. Ưu tiên cùng vendor nếu có đủ major cho platform.
- Chọn bản phát hành ổn định, portable archive; không chọn EA, installer cần
  admin hoặc nguồn mirror không xác minh được.
- Nếu có lock manifest được nhóm cung cấp thì ưu tiên dùng chính xác artifact
  đã khóa. Nếu chưa có, tự tra cứu metadata chính thức rồi tạo
  `Java-version/install.local.json`; không dừng vì thiếu lock/script cài đặt.
- Ghi major, full version/build, vendor, OS/CPU, URL tải cụ thể, SHA-256 từ
  nhà cung cấp, nguồn checksum, nguồn license và thời điểm chọn artifact.
  Không bịa URL/checksum; không chỉ ghi `latest` vào bản ghi cài đặt.
- Nếu không xác minh được URL hoặc checksum, báo lỗi cho JDK đó.
  Các máy tự chọn artifact vào thời điểm khác nhau có thể khác patch version;
  khi cần tái lập experiment, nhóm phải thống nhất full version/build.

## 3. Tải, giải nén và xác minh

1. Kiểm tra JDK đã có trong `Java-version/runtime/jdk-<major>/`. Nếu bản ghi cài
   đặt, OS/CPU và phiên bản khớp artifact đã chọn, kiểm lại java/javac rồi tái
   sử dụng. Không tái sử dụng binary của OS/CPU khác chỉ vì tên folder trùng.
2. Tải archive vào `Java-version/.cache/`; xử lý lỗi mạng rõ ràng và kiểm tra
   SHA-256 trước khi giải nén hoặc thực thi binary.
3. Giải nén vào thư mục tạm trong `runtime/`. Chặn đường dẫn archive thoát ra
   ngoài đích; không ghi đè JDK đang dùng. Nếu thư mục đích có dữ liệu không
   xác minh được, giữ nguyên và báo tình trạng cần xử lý.
4. Xác định JAVA_HOME thực tế: thư mục chứa `bin/java` và `bin/javac` (Windows
   là `.exe`). Gói macOS thường có `Contents/Home`; không mặc định mọi archive
   có cùng cấu trúc. Thư mục cài cuối cùng phải là `runtime/jdk-<major>/`,
   không thêm tầng platform hoặc folder tên archive. JAVA_HOME trên macOS có
   thể là `runtime/jdk-<major>/Contents/Home`; Windows thường là
   `runtime/jdk-<major>/`.
5. Gọi java và javac bằng đường dẫn tuyệt đối, kiểm exit code 0 và major đúng
   8/11/17/21; Java 8 có thể báo `1.8.0_...`. Kiểm full version/build với metadata
   và file `release` khi có. Lưu stdout/stderr cùng kết quả vào
   `Java-version/verification.local.json`.
6. Sau khi xác minh và hoàn tất cài đặt, xóa **đúng file archive đã tải** trong
   `.cache/`. Không xóa cả folder hay binary đã giải nén. Nếu cài đặt lỗi, giữ
   archive để chẩn đoán và không dùng JDK đó. Báo rõ archive đã xóa.

Không thay đổi JAVA_HOME/PATH toàn hệ thống, không cần quyền admin. Nếu tạo
script hỗ trợ cục bộ, đặt trong `Java-version/.local/`.

Cấu trúc chung trên cả macOS và Windows:

```text
Java-version/runtime/
  jdk-8/
  jdk-11/
  jdk-17/
  jdk-21/
```

Nội dung mỗi JDK phải đúng OS/CPU của máy, dù tên folder giống nhau.

## 4. Cấu hình preflight

- Sinh `Java-version/config.local.toml` bằng cách lấy cấu hình hiện có của máy
  (nếu có) hoặc dùng `preflight_tool/config.example.toml` làm mẫu. Giữ các thiết
  lập run/policy/input hợp lệ đã có; không ghi đè lựa chọn người dùng.
- Điền `[java.homes]` cho `"8"`, `"11"`, `"17"`, `"21"` bằng đường dẫn tuyệt đối
  tới JAVA_HOME vừa xác minh. Không map một major sang JDK khác major.
- `[java].default_home` chỉ chọn JDK đã xác minh và ghi rõ lựa chọn trong báo
  cáo; không thay thế mapping thiếu một cách âm thầm.
- Nếu có `ARROW/classes2test/dataset`, đặt `[input].root` theo đường dẫn local
  tương ứng. Nếu chưa có dataset, báo rõ cần truyền `--input-root`; không giữ
  đường dẫn ví dụ Windows như thể dataset tồn tại trên mọi máy.
- Trên Windows dùng slash `/` hoặc literal string TOML để tránh lỗi escape.
- Kiểm parse TOML và xác nhận đủ bốn đường dẫn. Nếu môi trường Python của
  preflight sẵn sàng, gọi `_java_env()` cho từng major để kiểm tương thích;
  báo riêng nếu chưa có dependency Python. Không cần chạy toàn dataset để
  xác minh setup JDK, không tự clone/build repository bên ngoài.

Sau khi cài dependency của preflight, chạy từ root ARROW bằng:

```text
class2test-preflight --config Java-version/config.local.toml --input-root <dataset-local> --index-only
```

`--index-only` chỉ kiểm tra luồng input, không chứng minh JDK build được mọi
repository; kết quả xác minh JDK dựa trên các bước java/javac ở trên.

## 5. Git và báo cáo bàn giao

`.gitignore` trong folder này loại trừ runtime, cache, cấu hình, script hỗ trợ
và báo cáo local. Không dùng `git add -f` để đưa chúng vào Git.

Báo cáo gồm: OS/CPU, mỗi JDK đã cài/tái sử dụng, vendor/full version, JAVA_HOME,
checksum đã khớp hay chưa, kết quả java/javac, archive đã xóa, đường dẫn config
và phần còn lỗi. Chỉ kết luận hoàn tất khi đủ cả bốn JDK được xác minh và config
local hợp lệ. Không commit/push theo prompt setup cho thành viên.
