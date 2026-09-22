# Cơ chế tự cài đặt JDK cho ARROW Preflight Tool

Thư mục này quản lý cơ chế tự động chuẩn bị các phiên bản Java Development Kit (JDK 8, 11, 17, 21) phục vụ chạy `preflight_tool` để build và kiểm thử các repository lịch sử của dataset Classes2Test.

Toàn bộ cơ chế tuân thủ nghiêm ngặt theo hướng dẫn tại [`Java-version/AGENTS.md`](file:///Users/dungng/FPT/Ki%209/SET490/DoAnARROW/ARROW/Java-version/AGENTS.md).

---

## 1. Lệnh cài đặt nhanh (One-command Setup)

Chạy từ thư mục gốc của repository (`ARROW/`):

### Trên macOS (Apple Silicon hoặc Intel)
```bash
./Java-version/scripts/install-java-versions.sh
```

### Trên Windows (x64 PowerShell)
```powershell
powershell -ExecutionPolicy Bypass -File .\Java-version\scripts\install-java-versions.ps1
```

---

## 2. Ma trận hỗ trợ (JDK Matrix)

Sử dụng bản phân phối thống nhất **Azul Zulu Builds of OpenJDK** (giấy phép GPLv2 with Classpath Exception), đảm bảo tính nhất quán trên cả 3 nền tảng:

| Nền tảng | Kiến trúc | Định danh | Định dạng Archive | JDK hỗ trợ |
| :--- | :--- | :--- | :--- | :--- |
| **macOS** | Apple Silicon (`arm64`) | `darwin-aarch64` | `.tar.gz` | **8, 11, 17, 21** |
| **macOS** | Intel (`x86_64`) | `darwin-x64` | `.tar.gz` | **8, 11, 17, 21** |
| **Windows** | Intel/AMD 64-bit | `windows-x64` | `.zip` | **8, 11, 17, 21** |

Toàn bộ artifact được khóa cố định tại [`Java-version/java-versions.lock.json`](file:///Users/dungng/FPT/Ki%209/SET490/DoAnARROW/ARROW/Java-version/java-versions.lock.json) bao gồm:
- Vendor & Version chính thức (không dùng URL `latest` hay dynamic redirect).
- SHA-256 hash chuẩn từ nhà phân phối.
- Đường dẫn tương đối của `JAVA_HOME` (`Contents/Home` trên macOS, thư mục gốc trên Windows).
- License URL chính thức.

---

## 3. Cơ chế hoạt động & An toàn

Khi thực thi, script sẽ:
1. **Tự nhận diện OS và CPU**: Tự động ánh xạ vào đúng platform của máy hiện tại (`darwin-aarch64`, `darwin-x64` hoặc `windows-x64`).
2. **Kiểm tra tính lũy biến (Idempotent)**: Nếu JDK đã được cài và cả `java -version` lẫn `javac -version` trả về đúng major version, script sẽ bỏ qua việc tải lại.
3. **Tải & Kiểm tra toàn vẹn**:
   - Tải archive từ CDN chính thức của Azul vào `Java-version/.cache/`.
   - **Xác minh SHA-256 trước khi giải nén**. Nếu mismatch, script dừng ngay lập tức và giữ nguyên file trong `.cache/` để chẩn đoán.
4. **Giải nén & Cấu hình**:
   - Giải nén vào `Java-version/runtime/<platform>/jdk-<major>/`.
   - Gỡ bỏ thuộc tính quarantine trên macOS nếu có (`xattr`).
5. **Kiểm tra chức năng (Runtime verification)**:
   - Chạy trực tiếp `$JAVA_HOME/bin/java -version` và `$JAVA_HOME/bin/javac -version`.
   - Cả 2 lệnh phải exit 0 và khớp chính xác số major (8, 11, 17, 21).
6. **Dọn dẹp bộ nhớ đệm**:
   - Xóa archive thô trong `.cache/` ngay sau khi JDK tương ứng verify thành công.
   - Giữ lại archive trong `.cache/` nếu có lỗi để phục vụ chẩn đoán.
7. **Tự động sinh cấu hình Preflight**:
   - Sinh hoặc cập nhật file máy cục bộ `preflight_tool/config.local.toml` với đầy đủ map `[java.homes]`.

---

## 4. Chạy Preflight Tool với JDK vừa cài đặt

Sau khi chạy script thành công, `preflight_tool/config.local.toml` đã được cấu hình trỏ tới các JDK cục bộ:

```bash
cd preflight_tool
source .venv/bin/activate # hoặc kích hoạt virtualenv tương ứng

# Chạy test suite preflight
pytest

# Chạy công cụ preflight với config vừa tạo
python -m preflight.cli --config config.local.toml --input-root /path/to/classes2test ...
```

---

## 5. Ràng buộc an toàn Git

- Thư mục `Java-version/runtime/` và `Java-version/.cache/` đã được chặn hoàn toàn trong `.gitignore`.
- Tuyệt đối không commit binary JDK, archive `.zip`/`.tar.gz` hoặc `config.local.toml` vào Git.
