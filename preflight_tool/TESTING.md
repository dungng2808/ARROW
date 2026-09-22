# Windows test gate

Run the local gate from this directory:

```powershell
.\.venv\Scripts\python.exe -m pip install -e ".[test]"
.\.venv\Scripts\python.exe -m pytest --cov=preflight --cov-branch --cov-report=term-missing
.\.venv\Scripts\coverage.exe json -o coverage.json
.\.venv\Scripts\python.exe scripts\check_coverage.py coverage.json
```

The current suite uses only local temporary Git repositories. `performance`
tests are intentionally opt-in because they create 10,000 JSON inputs:

```powershell
$env:RUN_PERFORMANCE = '1'
.\.venv\Scripts\python.exe -m pytest -m performance
```

For the real JDK matrix, set `PREFLIGHT_JDK_8`, `PREFLIGHT_JDK_11`,
`PREFLIGHT_JDK_17`, and `PREFLIGHT_JDK_21` to the corresponding JDK homes
before running pytest. Each configured version checks the actual `java` and
`javac` launchers; an unconfigured version is skipped. Run the same tests on
Windows, macOS, and Linux for cross-platform certification.

Before approving an experiment run, require at least 90% statement coverage,
85% branch coverage, and 100% branch coverage for `preflight.policy`. Run the
mutation gate for revision and policy logic separately:

```powershell
.\.venv\Scripts\python.exe -m pip install -e ".[mutation]"
.\.venv\Scripts\mutmut.exe run --paths-to-mutate preflight/revision.py preflight/policy.py
.\.venv\Scripts\mutmut.exe results
```

Remote smoke is deliberately non-blocking. Freeze three audited candidates
(first single-module Maven, first multi-module Maven, first Gradle candidate)
as full SHAs in a revision map, record their expected statuses, and execute on
a dedicated Windows VM with no secrets. It must never be a prerequisite for the
local CI result.
