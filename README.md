"# Workspace ARROW

Khoá workspace này chứa pipeline ARROW để phân tích repository, sinh test bằng LLM và chạy build Maven/Gradle tự động.

## Mục tiêu

- Đọc input từ thư mục classes2test/dataset
- Clone repository về local
- Phân tích project và tạo test bằng mô hình LLM
- Chạy build và ghi báo cáo kết quả

## Cấu trúc chính

- ARROW/: code pipeline chính
- classes2test/: dataset và các project mẫu
- .gitignore: bỏ qua thư mục classes2test khi push lên Git

## Yêu cầu hệ thống

Trước khi chạy, hãy chắc chắn máy đã cài:

- Python 3.9+
- Java JDK
- Maven hoặc Gradle
- Git
- Ollama (nếu dùng model local) hoặc API key OpenAI

## Cài đặt

Mở terminal tại thư mục workspace:

```powershell
cd "G:\FPT Specialized\Ki 9\SET490\workspace"
```

Cài dependency Python cho pipeline:

```powershell
cd ARROW
python -m pip install -r requirements.txt
```

Kiểm tra công cụ cần thiết:

```powershell
git --version
java -version
mvn -version
gradle --version
```

## Chạy nhanh

### 1) Kiểm tra input

```powershell
cd ARROW
python -m src.run_pipeline --count-only
```

### 2) Chạy thử một sample

```powershell
python -m src.run_pipeline --start-index 0 --limit 1
```

### 3) Chạy với model cụ thể

```powershell
python -m src.run_pipeline --agent gpt-4.1-mini --start-index 0 --limit 1
```

## Cấu hình môi trường

Pipeline dùng file cấu hình tại:

```text
ARROW/config/pipeline.yaml
```

Nếu dùng OpenAI, thiết lập biến môi trường trước khi chạy:

```powershell
$env:OPENAI_API_KEY="your_api_key"
```

Nếu dùng Ollama local, khởi động server trước:

```powershell
ollama serve
ollama pull qwen2.5-coder:1.5b
```

## Chạy dashboard (tuỳ chọn)

```powershell
cd ARROW
$env:OPENAI_API_KEY="your_api_key"
python -m dashboard.server --host 127.0.0.1 --port 8765
```

Sau đó mở trình duyệt tới:

```text
http://127.0.0.1:8765
```

## Kết quả đầu ra

Kết quả chạy sẽ được lưu trong thư mục:

```text
ARROW/runs/
```

## Lưu ý

- Nếu cần giữ workspace bị lỗi để debug, có thể dùng các flag như --keep-workspace hoặc --keep-repo-cache.
- Nếu bạn chỉ muốn test nhanh, có thể dùng --skip-metrics.
- Thư mục classes2test đã được cấu hình trong .gitignore để tránh bị push lên Git.
" 
