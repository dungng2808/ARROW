# Traceability: `preflight.md` → automated tests

# Traceability: `preflight.md` → automated tests

| Spec section | Yêu cầu kỹ thuật trong `preflight.md` | Test IDs | File kiểm thử chính |
| --- | --- | --- | --- |
| 1 — Mục đích và ranh giới | Pha chỉ đọc, không sửa repo gốc, không đoán semantic validation | INT-001…003 | `tests/integration/test_git_and_runner.py` |
| 2 — Thuật ngữ & 7 trục trạng thái | 7 trường độc lập, phân định `technical_eligible` vs `strict_eligible` | BUS-001…006 | `tests/business/test_policy.py` |
| 3 — Locked input & provenance | Schema JSON, SHA256 file content, UPSTREAM_PINNED, CONTENT_MATCHED | UNT-ING-001…005, UNT-REV-001…006 | `tests/unit/test_ingest.py`, `test_revision.py` |
| 3.2 — Chế độ DISCOVERY_ONLY | DISCOVERY_ONLY luôn có `strict_eligible=false` | BUS-003, E2E-002 | `tests/business/test_policy.py`, `tests/e2e/test_cli_portfolio.py` |
| 4 — Quy trình preflight | Quy trình 8 bước, tách biệt mirror và worktree tạm, xóa probe sau compile | INT-001…003 | `tests/integration/test_git_and_runner.py` |
| 5 — Build theo module | Maven `-pl -am`, fallback module POM, Gradle wrapper, 6 loại build failure | UNT-BLD-001…008, UNT-RUN-001…009 | `tests/unit/test_runner_helpers.py`, `test_preflight_status_paths.py` |
| 6.1..6.3 — CUT accept & exclude | Loại interface/enum/record/abstract/generated/test; accept DTO, static utility | BUS-CUT-001…010, UNT-AST-001…005 | `tests/business/test_class_policy.py`, `tests/unit/test_java_ast.py` |
| 6.4 — DAO, SQL & dependency ngoài | DataSource/JdbcTemplate mock được, DriverManager tạo tag DIRECT_CONNECTION | BUS-DAO-001…002 | `tests/business/test_class_policy.py` |
| 7.1 — API count | `api_count = constructor_count + method_count`, loại bỏ private/Object methods | UNT-API-001…003 | `tests/unit/test_java_ast.py` |
| 7.2 — Probe bắt buộc cho strict | Sinh probe test cho static/instance/constructor, fast mode đặt PRECHECKED | UNT-PRB-001…004, INT-002 | `tests/unit/test_runner_helpers.py`, `test_git_and_runner.py` |
| 8 — Thứ tự ưu tiên trạng thái | Ma trận 8 bậc ưu tiên: repo -> build -> source -> exclusion -> probe -> review | BUS-STAT-001…008 | `tests/business/test_policy.py` |
| 9 — Dữ liệu đầu ra | JSONL, CSV, 4 files manifest, summary.json, provenance.json, JSON schema check | UNT-OUT-001…003, E2E-OUT-001 | `tests/unit/test_output.py`, `tests/e2e/test_cli_portfolio.py` |
| 10 — Ví dụ quyết định | Tái hiện đúng các ca mẫu (DTO, static utility, DAO mock, direct connection) | E2E-DEC-001…003, BUS-DEC-001 | `tests/e2e/test_cli_portfolio.py`, `test_preflight_status_paths.py` |
| 11 — Ranh giới với validation | Preflight dừng ở strict manifest; không thực thi LLM test generation | E2E-DEC-001 | `tests/e2e/test_cli_portfolio.py` |
| 12 — Tính ổn định & tái lập | Chạy lặp 20 lần cho kết quả xác định 100%, budget bộ nhớ cho 10k JSON | NF-DET-001, NF-PERF-001 | `tests/nonfunctional/test_stability.py` |

`remote` tests là non-blocking và chỉ chạy khi có môi trường cấu hình khoá SHA cụ thể.

