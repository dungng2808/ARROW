# Setup JDK cho từng máy

Folder này chứa rule cho agent cài JDK 6, 7, 8, 11, 17, 21 phục vụ `preflight_tool`.
Mỗi máy tải đúng bộ cho OS/CPU của mình; JDK được lưu trong `runtime/` và không
đưa vào Git. Hiện đây là quy trình do agent thực hiện, chưa có script installer
chung. Git pull không tự tải Java.

Cả macOS và Windows dùng chung cấu trúc thư mục:

```text
Java-version/runtime/
  jdk-6/
  jdk-7/
  jdk-8/
  jdk-11/
  jdk-17/
  jdk-21/
```

Không thêm tầng folder OS/CPU. Agent chọn binary phù hợp khi tải và ghi
JAVA_HOME thực tế vào config local (gói macOS có thể chứa `Contents/Home`).

## Prompt gửi cho các thành viên

```text
Trong repository ARROW, hãy đọc đầy đủ Java-version/AGENTS.md và thực hiện
setup JDK trên máy hiện tại theo rule đó.

Tự nhận diện OS/CPU, tìm JDK 6, 7, 8, 11, 17, 21 trên mọi ổ trước; chỉ tải
những major còn thiếu từ nguồn chính thức vào Java-version/runtime/jdk-6/,
jdk-7/, jdk-8/, jdk-11/, jdk-17/, jdk-21/ với binary đúng OS/CPU,
không tạo thêm tầng folder platform. Nếu chưa có lock manifest hoặc script,
hãy tự tra cứu metadata chính thức và thực hiện cài đặt theo rule; lưu thông
tin artifact/checksum vào install.local.json.

Kiểm SHA-256 trước khi giải nén, kiểm java và javac đúng version, xóa archive
sau khi cài thành công. Sinh Java-version/config.local.toml phù hợp với
preflight_tool và đường dẫn dataset trên máy tôi. Tái sử dụng JDK đã xác minh
nếu chạy lại. Tuân thủ .gitignore trong Java-version/.

Không sửa code của preflight hoặc tài liệu/script dùng chung. Không commit
hay push. Cuối cùng báo cáo các version, JAVA_HOME, kết quả xác minh và lệnh
chạy preflight trên máy tôi; ghi rõ nếu còn JDK hoặc dependency bị thiếu.
```

Agent hỗ trợ thao tác terminal và tải mạng sẽ thực hiện các bước trên. Các
file `AGENTS.md`, `README.md` và `.gitignore` cần được commit/push trước khi
thành viên khác pull về dùng prompt này.
