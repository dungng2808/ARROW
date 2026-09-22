# Traceability: `preflight.md` → automated tests

| Spec section | Test IDs | Evidence |
| --- | --- | --- |
| 2, 8 — independent states and eligibility | BUS-001…BUS-006 | `tests/business/test_policy.py` |
| 3 — locked input, SHA and revision provenance | UNT-ING-001…005, UNT-REV-001…006 | `tests/unit/test_ingest.py`, `test_revision.py` |
| 4 — read-only flow and separated workspace | INT-001…003 | `tests/integration/test_git_and_runner.py` |
| 5 — Maven/Gradle/JDK/build failure | UNT-BLD-001…008, UNT-RUN-001…009 | `tests/unit/test_runner_helpers.py`, `test_preflight_status_paths.py` |
| 6 — CUT accept/exclude and tags | BUS-CUT-001…009, UNT-AST-001…004 | `tests/business/test_class_policy.py`, `tests/unit/test_java_ast.py` |
| 7 — API count and compile probe | UNT-PROBE-001…004 | `tests/unit/test_runner_helpers.py`, `test_preflight_status_paths.py` |
| 8 — status priority | BUS-STAT-001…005 | `tests/business/test_policy.py` |
| 9 — JSONL/CSV/manifests | UNT-OUT-001…003, E2E-OUT-001 | `tests/unit/test_output.py`, `tests/e2e/test_cli_portfolio.py` |
| 10 — decision examples | E2E-DEC-001…003 | `tests/e2e/test_cli_portfolio.py`, `test_preflight_status_paths.py` |
| 12 — deterministic/reproducible run | NF-DET-001, NF-PERF-001 | `tests/nonfunctional/test_stability.py` |

`remote` tests are intentionally excluded from the blocking Windows gate. They
must use a frozen full SHA and record the expected status before being enabled.
