# Hướng dẫn cài JDK cục bộ cho Preflight

## Mục đích

Folder này là nơi một agent chuẩn bị các JDK lịch sử để chạy
`preflight_tool` trên **chính máy đang chạy**. Tool cần JDK 8, 11, 17 và 21 để
build các repository Java ở revision cũ.

JDK binary là dependency cục bộ, không phải source code của ARROW. Chỉ commit
file hướng dẫn, script cài, lock manifest, checksum và license note; không tự
commit archive hay thư mục JDK đã giải nén nếu chưa có chỉ dẫn mới của người
dùng.

## Matrix bắt buộc

| Hệ điều hành / CPU | JDK phải có |
| --- | --- |
| macOS Apple Silicon (`darwin-aarch64`) | 8, 11, 17, 21 |
| macOS Intel (`darwin-x64`) | 8, 11, 17, 21 khi máy là Intel |
| Windows x64 (`windows-x64`) | 8, 11, 17, 21 |

Không tải JDK Linux khi yêu cầu chỉ là macOS/Windows.

## Cấu trúc cục bộ chuẩn

```text
Java-version/
  AGENTS.md
  java-versions.lock.json
  scripts/
    install-java-versions.sh
    install-java-versions.ps1
  runtime/                         # binary cục bộ, không tự commit
    darwin-aarch64/jdk-8/
    darwin-aarch64/jdk-11/
    darwin-aarch64/jdk-17/
    darwin-aarch64/jdk-21/
    darwin-x64/jdk-<version>/       # chỉ trên Mac Intel
    windows-x64/jdk-<version>/
  .cache/                           # archive tải về, không tự commit
```

`JAVA_HOME` là thư mục chứa `bin/java` và `bin/javac`:

- Với macOS package: thường là `runtime/<platform>/jdk-<version>/Contents/Home`.
- Với Windows archive: thường là `runtime/windows-x64/jdk-<version>`.

Không ghi absolute path của một máy vào file được commit. Không dùng lại path
cũ của workspace khác.

## Quy trình agent phải thực hiện

1. Nhận diện OS/CPU trước khi tải:
   - macOS: `uname -s` và `uname -m`.
   - Windows: PowerShell lấy OS và architecture của process/máy.
2. Đọc `java-versions.lock.json`. Mỗi artifact phải khóa các trường: `version`,
   `platform`, `architecture`, `vendor`, `download_url`, `sha256`, `archive`,
   `java_home_relative_path` và license URL.
3. Chỉ tải artifact khớp platform/CPU hiện tại. Không đoán URL, không dùng
   `latest`, branch động hay checksum thiếu.
4. Tải vào `.cache/`, kiểm SHA-256 **trước** khi giải nén, rồi giải nén vào
   `runtime/<platform>/jdk-<major>/` theo cách idempotent.
5. Với từng JDK, chạy `java -version` và `javac -version`; cả hai phải exit 0
   và major version phải đúng 8/11/17/21. Ghi kết quả vào report cục bộ, không
   thay đổi JDK hệ thống.
6. Khi JDK đã giải nén và kiểm version thành công, **xóa archive raw** tương
   ứng khỏi `.cache/` để không chiếm dung lượng. Nếu checksum, giải nén hoặc
   kiểm version thất bại thì giữ archive để chẩn đoán, báo lỗi rõ ràng và không
   dùng JDK đó. Chỉ giữ cache thành công khi người dùng yêu cầu explicit cache
   offline.
7. Sinh hoặc cập nhật `preflight_tool/config.local.toml` cục bộ với map
   `java.homes` đúng các `JAVA_HOME` vừa xác minh. Chạy tool bằng
   `--config preflight_tool/config.local.toml`; không sửa `config.example.toml`
   chỉ để phù hợp một máy.
8. Báo cáo rõ JDK nào đã sẵn sàng, thiếu, checksum mismatch hoặc không có
   artifact đúng platform. Không gọi một JDK khác version là thành công.

Ví dụ cấu hình local cho runner:

```toml
[java]
default_home = ""

[java.homes]
"8" = "/absolute/local/path/to/Java-version/runtime/darwin-aarch64/jdk-8/Contents/Home"
"11" = "/absolute/local/path/to/Java-version/runtime/darwin-aarch64/jdk-11/Contents/Home"
"17" = "/absolute/local/path/to/Java-version/runtime/darwin-aarch64/jdk-17/Contents/Home"
"21" = "/absolute/local/path/to/Java-version/runtime/darwin-aarch64/jdk-21/Contents/Home"
```

Trên Windows, path trong `config.local.toml` dùng dạng `C:/...` hoặc escaped
đúng theo TOML.

## Ràng buộc an toàn và Git

- Không chạy archive/script tải về trước khi checksum khớp lock manifest.
- Xóa archive raw trong `.cache/` ngay sau khi JDK tương ứng verify thành công;
  không xóa archive khi lần cài đặt đó thất bại.
- Không chạy installer có quyền admin nếu archive portable đủ dùng.
- Không xóa, ghi đè hay di chuyển JDK đã tồn tại khi chưa kiểm version và chưa
  có xác nhận của người dùng.
- Không `git add` các thư mục `runtime/` hoặc `.cache/` bằng wildcard.
- Nếu người dùng sau này quyết định phân phối binary qua Git LFS, chỉ thực hiện
  sau khi có manifest đầy đủ cả macOS/Windows, license review, quota Git LFS và
  xác nhận rõ ràng. Không đưa binary lớn vào Git thường.

## Điều kiện hoàn thành

Agent chỉ báo hoàn thành khi JDK cần cho platform hiện tại có `java` và `javac`
chạy được, version đúng, checksum đã verify, và đường dẫn đã được map vào
`config.local.toml`. Việc có folder tồn tại hoặc Maven tự tìm thấy một JDK khác
không đủ điều kiện pass.
