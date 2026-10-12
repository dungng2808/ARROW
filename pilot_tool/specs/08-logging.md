# 08. Log và dữ liệu lưu lại

Gom mọi thứ tool ghi ra đĩa: ở đâu, định dạng gì, khi nào ghi, giữ bao lâu. Định dạng chi tiết của từng file JSON ở `01`, `03`, `05`.

## 1. Ba loại log

| Loại | Dùng để | Ở đâu |
| --- | --- | --- |
| **Log của tool** | Gỡ lỗi chính tool (lỗi, cảnh báo, lệnh đã chạy) | `pilot_tool/logs/arrow-<yyyyMMdd>.log` (mọi lệnh `arrow`, kể cả `setup`, `doctor`, `shard plan`, `control`, `merge`); `runs/<run_id>/tool.log` (bản sao các dòng ghi trong lúc run đó đang chạy) |
| **Log thực nghiệm** | Dữ liệu để tính kết quả và tái lập: mỗi job, mỗi ứng viên, mỗi vòng sửa | `runs/<run_id>/` (Mục 3) |
| **Log build** | Output đầy đủ của từng lệnh Maven/Gradle/EvoSuite/PIT… | `runs/<run_id>/tasks/<dataset>/<task_id>/logs/` |

## 2. Log của tool

- Serilog, định dạng một dòng: `2026-10-15T16:13:02.114+07:00 [INF] <thông điệp>` (giờ địa phương kèm độ lệch múi giờ, để đọc log trên máy mình cho dễ; ledger và status vẫn dùng UTC); mức `DBG`, `INF`, `WRN`, `ERR`. Thông điệp trong run bắt đầu bằng `run <run_id>:`; từ M3 thêm tên worker (`worker build-2`).
- Mỗi lệnh ghi một dòng mở đầu (phiên bản tool, nền tảng, .NET, tham số) và một dòng kết thúc với mã thoát (`INF` nếu 0, `WRN` nếu khác 0); lỗi không bắt được ghi `ERR` kèm stack trace.
- Nội dung đã ghi ở M1: từng mục doctor và kết quả; cấu hình máy đã lưu ở setup (chỉ alias khóa, host endpoint); kết quả cài LiteLLM; shard plan đã ghi; lệnh pause/resume/stop gửi đi và run nhận được, khoảng tạm dừng; tiến trình con (`DBG`: lệnh và thư mục; `INF`: mã thoát và thời gian; `WRN`: timeout, không khởi động được).
- Mặc định ghi từ `INF`; `--verbose` ghi từ `DBG`.
- File theo ngày, tối đa 50 MB mỗi file, giữ 14 file gần nhất (`pilot_tool/logs/`); `runs/<run_id>/tool.log` không xoay vòng.
- MUST NOT ghi khóa API, `master_key`, `api_base` đầy đủ, nội dung `litellm.config.local.yaml`. Mọi chuỗi khớp `sk-[A-Za-z0-9_-]{8,}` được thay bằng `sk-***` trước khi ghi (cả trong log build và response).

## 3. Log thực nghiệm trong một run

```
runs/<run_id>/
├─ run_config.lock.json        ghi 1 lần lúc tạo run: cấu hình đã khóa (01 §4.1)
├─ status.json                 ghi đè mỗi 5 giây: trạng thái hiện tại (01 §4.3)
├─ control.json                lệnh tạm dừng/tiếp tục/dừng (01 §4.3)
├─ ledger.jsonl                CHỈ GHI THÊM: mỗi job xong 1 dòng; mỗi lần tạm dừng 1 dòng (01 §4.2)
├─ events.jsonl                CHỈ GHI THÊM: dòng sự kiện cho dashboard và --plain (01 §4.4)
├─ tool.log                    log của tool trong run này
├─ llm_cache/<sha256>.json     response gốc theo khóa cache (04 §1.1)
├─ tasks/<dataset>/<task_id>/
│  ├─ task.json                bản ghi manifest + JDK đã chọn + lịch sử git (02)
│  ├─ baseline.json            kết quả 2 lần chạy test của module (02 §5)
│  ├─ repo_context.json        6 thông tin repository (02 §7)
│  ├─ gen/<model>/<strategy>/  bước sinh test
│  │  ├─ prompt.json           messages đã gửi
│  │  ├─ response.json         response gốc + finish_reason
│  │  ├─ usage.json            model id, token, độ trễ, số lần gọi lại, cache hit
│  │  ├─ normalization.json    kết quả kiểm tra và chuẩn hóa (04 §3)
│  │  ├─ cand_0.java           mã test sau chuẩn hóa (không có nếu output không hợp lệ)
│  │  └─ verify_0.json         kết quả 5 bước (03 §5)
│  ├─ repair/<model>/<strategy>/
│  │  ├─ excluded.json         lý do c0 bị loại khỏi bước sửa (nếu có)
│  │  └─ <fixed|adaptive>/
│  │     ├─ summary.json       kết quả cả nhánh + bộ đếm (05 §7.2)
│  │     └─ <attempt_id>/round_<t>/
│  │        ├─ prompt.json  response.json  usage.json  normalization.json
│  │        ├─ cand.java       (không có nếu output không hợp lệ hoặc mã trùng)
│  │        ├─ verify.json     (không có nếu không build)
│  │        └─ decision.json   quyết định của vòng (05 §7.1)
│  ├─ evosuite/                test sinh ra, verify.json, evosuite_stats.json (06)
│  ├─ quality/<system>/        jacoco.json, pit.json, tsdetect.json (06)
│  └─ logs/                    log build (Mục 4)
└─ reports/                    do lệnh report sinh ra (07)
```

Quy tắc ghi:

- Mọi file JSON ghi nguyên tử (ghi `.tmp` rồi đổi tên). `ledger.jsonl` và `events.jsonl` chỉ ghi thêm, flush sau mỗi dòng.
- **Ghi output trước, ghi ledger sau**: một job chỉ được coi là xong khi đã có dòng trong `ledger.jsonl`; output dở của job chưa có dòng ledger sẽ bị ghi đè khi chạy lại.
- Không sửa file của job đã xong. Chạy lại job `INFRA` ghi vào `attempt_id` mới (với bước sửa) hoặc ghi đè (với job khác) và thêm dòng ledger mới.
- Thời gian lưu dạng giây (số thực) và thời điểm dạng ISO-8601 UTC.

## 4. Log build

Tên file: `logs/<job_key_ngắn>__<bước>[_<lần>].log`, ví dụ `logs/gen-gpt-6-luna-few_shot__target_test.log`, `logs/repair-adaptive-a1-r3__module_test.log`.

Nội dung:

```
$ /Users/…/mvnw -B -ntp … -Dtest=PairService_ArrowTest test
cwd: /Users/…/ws/pilot-6-shard-01-Dung-mac/3f9a0c1d2e4b/trading-common
jdk: 17 (/…/jdk-17/Contents/Home) · started: 2026-10-15T09:13:02Z
--- stdout+stderr ---
<toàn bộ output>
--- end ---
exit: 1 · duration: 9.42s · timeout: false
```

- Báo cáo XML của test (Surefire/Gradle) được chép sang `logs/reports/<job_key_ngắn>__<bước>/` trước khi xóa workspace.
- Log build lớn hơn 5 MB thì chỉ giữ 2 MB đầu và 2 MB cuối, ghi dòng `… N bytes omitted …` ở giữa.

## 5. Mỗi vòng sửa lưu những gì

| Câu hỏi khi phân tích | Lấy ở |
| --- | --- |
| Vòng này gửi prompt gì, model trả gì | `round_<t>/prompt.json`, `response.json` |
| Mã nào được build | `round_<t>/cand.java` |
| Kết quả 5 bước, lỗi, signature, số method pass, số assertion | `round_<t>/verify.json` |
| Sửa từ bản nào, mức nào, có rollback, mã trùng, output không hợp lệ, ghép method nào | `round_<t>/decision.json` |
| Tốn bao nhiêu token, bao lâu, gọi lại mấy lần | `round_<t>/usage.json`, `decision.json` |
| Cả nhánh: thành công ở vòng nào, method bị bỏ, đếm rollback… | `<mechanism>/summary.json` |
| Log Maven/Gradle | `logs/repair-<mechanism>-<attempt>-r<t>__*.log` |

## 6. Giữ, xóa, xuất

| Thứ | Giữ trên máy | Trong file zip `export` |
| --- | --- | --- |
| Workspace của job | Xóa khi job xong (giữ nếu `keep_failed_workspaces` và job lỗi) | Không |
| Mirror repository | Theo `repo_policy` | Không |
| `llm_cache/` | Giữ | Không (đã có trong `response.json`) |
| JSON, JSONL, `.java` | Giữ | Có |
| Log build | Giữ | Bước lỗi: đủ; bước đạt: 200 dòng cuối |
| Báo cáo XML của test | Giữ | Có |
| `tool.log` | Giữ | Có (đã che khóa) |

Không xóa `runs/` tự động. Lệnh `arrow runs --clean <run_id>` xóa một run sau khi hỏi xác nhận và chỉ khi run đã được export.

## 7. Kiểm thử

- Che khóa: chuỗi `sk-…` trong response, log build, thông điệp lỗi đều thành `sk-***`.
- Dừng giữa lúc ghi: job chưa có dòng ledger được chạy lại; file `.tmp` sót lại bị bỏ qua.
- Log build quá 5 MB được cắt đúng; báo cáo XML được chép trước khi xóa workspace.
