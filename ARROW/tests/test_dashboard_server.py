from __future__ import annotations

import csv
import io
import json
from pathlib import Path

from dashboard import server


def test_merge_reports_now_writes_dashboard_artifacts(monkeypatch, tmp_path):
    records = tmp_path / "runs" / "repo" / "sample" / "reports" / "records"
    records.mkdir(parents=True)
    (records / "experiments.jsonl").write_text(
        '{"run_id":"r","shard_id":"s","input_id":"i","agent_name":"a",'
        '"generation_prompt_strategy":"zero-shot","test_passed":true}\n',
        encoding="utf-8",
    )
    monkeypatch.setattr(server, "PROJECT_ROOT", tmp_path)
    monkeypatch.setattr(server, "_project_config", lambda: {"report": {"merged_dir": "merged"}})

    result = server._merge_reports_now()

    assert result["experiments"] == 1
    assert result["passed"] == 1
    assert result["failed"] == 0
    assert (tmp_path / "runs" / "merged" / "experiments_merged.jsonl").is_file()
    assert (tmp_path / "runs" / "merged" / "output_agone_classes_lite.csv").is_file()
    assert (tmp_path / "runs" / "merged" / "rq1_summary.csv").is_file()
    assert (tmp_path / "runs" / "merged" / "rq1_paired.csv").is_file()
    assert (tmp_path / "runs" / "merged" / "rq1_details.csv").is_file()
    assert result["artifacts"]["rq1"]["summary"]["rows"] == 3


def test_dashboard_contains_merge_button_and_api_binding():
    html = (server.STATIC_ROOT / "index.html").read_text(encoding="utf-8")
    javascript = (server.STATIC_ROOT / "app.js").read_text(encoding="utf-8")

    assert 'id="mergeReportsBtn"' in html
    assert 'id="mergeStatus"' in html
    assert 'api("/api/reports/merge"' in javascript


def test_dashboard_contains_shard05_export_button_and_api_binding():
    html = (server.STATIC_ROOT / "index.html").read_text(encoding="utf-8")
    javascript = (server.STATIC_ROOT / "app.js").read_text(encoding="utf-8")

    assert 'href="/shard05.html"' in html
    assert 'id="exportShard05Btn"' in html
    assert 'id="shard05Summary"' in html
    assert 'id="shard05Rows"' in html
    assert 'api("/api/reports/export/shard05"' in javascript
    assert 'api("/api/shards/shard05/status"' in javascript


def test_shard05_runner_page_locks_to_shard05_and_run_input():
    html = (server.STATIC_ROOT / "shard05.html").read_text(encoding="utf-8")
    javascript = (server.STATIC_ROOT / "shard05.js").read_text(encoding="utf-8")

    assert 'id="shard05RunForm"' in html
    assert 'id="inputMode"' in html
    assert 'id="samplesPerProject"' in html
    assert 'id="startIndex"' in html
    assert 'id="limit"' in html
    assert 'id="startShard05Btn"' in html
    assert 'id="projectErrorContent"' in html
    assert 'id="copyProjectErrorsBtn"' in html
    assert 'id="copyRunLogBtn"' in html
    assert 'id="copyRunLogStatus"' in html
    assert 'id="exportShard05MetricsBtn"' in html
    assert 'id="exportShard05MetricsStatus"' in html
    for header in ["Sample", "Class", "Agent", "Prompt", "Tokens", "State", "Repair", "Coverage", "Mutation", "Time"]:
        assert f"<th>{header}</th>" in html
    assert "Zero-shot" in javascript
    assert "Few-shot" in javascript
    assert "Repository-aware" in javascript
    assert 'const SHARD05_FILE = "repo_shard_05.txt"' in javascript
    assert 'const SHARD05_ID = "repo_shard_05"' in javascript
    assert 'run_scope: "shard"' in javascript
    assert "repo_shard: SHARD05_FILE" in javascript
    assert "shard_id: SHARD05_ID" in javascript
    assert "rerunProject" in javascript
    assert "loadProjectErrors" in javascript
    assert "copyRunLog" in javascript
    assert "state.runLogContent" in javascript
    assert "shard05DisplayRows" in javascript
    assert "state.shard05.experiments" in javascript
    assert "promptCell" in javascript
    assert "RQ1_PROMPT_LABELS" in javascript
    assert "exportShard05Metrics" in javascript
    assert "/api/reports/export/shard05" in javascript
    assert "/api/shards/shard05/projects/" in javascript
    assert 'api("/api/runs"' in javascript
    assert 'api("/api/shards/shard05/status"' in javascript


def test_static_serves_shard05_runner_page():
    handler = _bare_dashboard_handler("/shard05.html")

    handler.do_GET()

    body = handler.wfile.getvalue().decode("utf-8")
    assert handler.response_status == 200
    assert handler.response_headers["Content-Type"] == "text/html; charset=utf-8"
    assert 'id="shard05RunForm"' in body


def test_dashboard_links_to_rq1_preview_instead_of_direct_csv_buttons():
    html = (server.STATIC_ROOT / "index.html").read_text(encoding="utf-8")
    javascript = (server.STATIC_ROOT / "app.js").read_text(encoding="utf-8")

    assert 'id="rq1ExportLink"' in html
    assert 'href="/rq1.html"' in html
    assert "exportSummaryBtn" not in html
    assert "exportPairedBtn" not in html
    assert "exportDetailsBtn" not in html
    assert "/api/reports/export/${exportKind}" not in javascript


def _bare_dashboard_handler(path: str):
    handler = object.__new__(server.DashboardHandler)
    handler.path = path
    handler.wfile = io.BytesIO()
    handler.rfile = io.BytesIO()
    handler.headers = {}
    handler.response_status = None
    handler.response_headers = {}
    handler.send_response = lambda status: setattr(handler, "response_status", status)
    handler.send_header = lambda name, value: handler.response_headers.__setitem__(name, value)
    handler.end_headers = lambda: None
    return handler


def test_rq1_page_contains_preview_and_workbook_export_bindings():
    html = (server.STATIC_ROOT / "rq1.html").read_text(encoding="utf-8")
    javascript = (server.STATIC_ROOT / "rq1.js").read_text(encoding="utf-8")

    assert "Prompt Repository-aware có cải thiện" in html
    assert 'id="exportRq1Btn"' in html
    assert "Tổng hợp kết quả" in html
    assert 'id="overallResultsHead"' in html
    assert 'id="modelResultsHead"' in html
    assert 'data-preview-tab="summary"' in html
    assert 'data-preview-tab="paired"' in html
    assert 'data-preview-tab="details"' in html
    assert "/api/reports/rq1/preview" in javascript
    assert "renderResultTables" in javascript
    assert 'api("/api/reports/export/rq1"' in javascript


def test_rq1_export_endpoint_merges_and_saves_csv(monkeypatch, tmp_path):
    merged_dir = tmp_path / "runs" / "merged"
    merged_dir.mkdir(parents=True)
    csv_path = merged_dir / "rq1_summary.csv"
    csv_path.write_text("column\nvalue\n", encoding="utf-8-sig")
    merge_calls = []

    def fake_merge():
        merge_calls.append(True)
        return {
            "output_dir": str(merged_dir),
            "artifacts": {
                "rq1": {
                    "summary": {"path": str(csv_path), "rows": 1},
                }
            }
        }

    monkeypatch.setattr(server, "PROJECT_ROOT", tmp_path)
    monkeypatch.setattr(server, "RQ1_EXPORT_ROOT", tmp_path / "export" / "RQ1")
    monkeypatch.setattr(server, "_merge_reports_now", fake_merge)
    handler = _bare_dashboard_handler("/api/reports/export/summary")

    handler.do_POST()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    saved_path = Path(payload["path"])
    assert merge_calls == [True]
    assert handler.response_status == 201
    assert handler.response_headers["Content-Type"] == "application/json; charset=utf-8"
    assert payload["rows"] == 1
    assert payload["relative_path"].startswith("export/RQ1/rq1_summary_")
    assert saved_path.parent == tmp_path / "export" / "RQ1"
    assert saved_path.read_bytes().startswith(b"\xef\xbb\xbf")


def test_rq1_export_endpoint_rejects_unknown_type(monkeypatch):
    monkeypatch.setattr(server, "_merge_reports_now", lambda: (_ for _ in ()).throw(AssertionError("must not merge")))
    handler = _bare_dashboard_handler("/api/reports/export/../../secret")

    handler.do_POST()

    assert handler.response_status == 404
    assert b"Unknown CSV export type" in handler.wfile.getvalue()


def test_shard05_export_endpoint_saves_filtered_class_report(monkeypatch, tmp_path):
    merged_dir = tmp_path / "runs" / "merged"
    merged_dir.mkdir(parents=True)
    merged_jsonl = merged_dir / "experiments_merged.jsonl"
    merged_jsonl.write_text(
        "\n".join(
            [
                '{"run_id":"r","shard_id":"repo_shard_05","input_id":"i1","agent_name":"a",'
                '"model":"m","generation_prompt_strategy":"zero-shot","build_tool":"maven",'
                '"compilation":true,"test_passed":true,"target_test_passed":true,"module_tests_passed":true,'
                '"coverage_line":80,"coverage_branch":70,"coverage_method":90,"mutation_score":60,'
                '"mutations_total":10,"mutations_killed":6,"test_smell_total":0,'
                '"Assertion Roulette":2,"Conditional Test Logic":4}',
                '{"run_id":"r2","shard_id":"repo_shard_05","input_id":"i2","agent_name":"a",'
                '"model":"m","generation_prompt_strategy":"zero-shot","build_tool":"gradle",'
                '"compilation":false,"test_passed":false,"target_test_passed":false,"module_tests_passed":false,'
                '"coverage_line":40,"coverage_branch":30,"coverage_method":50,"mutation_score":20,'
                '"mutations_total":20,"mutations_killed":4,"test_smell_total":2,'
                '"Assertion Roulette":0,"Conditional Test Logic":2}',
                '{"run_id":"r","shard_id":"repo_shard_04","input_id":"i2","agent_name":"a","generation_prompt_strategy":"zero-shot","test_passed":true}',
            ]
        )
        + "\n",
        encoding="utf-8",
    )

    monkeypatch.setattr(server, "PROJECT_ROOT", tmp_path)
    monkeypatch.setattr(server, "SHARD_EXPORT_ROOT", tmp_path / "export" / "shards")
    monkeypatch.setattr(
        server,
        "_merge_reports_now",
        lambda: {
            "output_dir": str(merged_dir),
            "artifacts": {"merged_jsonl": str(merged_jsonl)},
        },
    )
    handler = _bare_dashboard_handler("/api/reports/export/shard05")

    handler.do_POST()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    saved_path = Path(payload["path"])
    mean_path = Path(payload["mean_path"])
    assert handler.response_status == 201
    assert payload["rows"] == 2
    assert payload["relative_path"].startswith("export/shards/repo_shard_05_runs_")
    assert payload["mean_relative_path"].startswith("export/shards/repo_shard_05_mean_")
    assert saved_path.parent == tmp_path / "export" / "shards"
    exported = saved_path.read_text(encoding="utf-8")
    assert "repo_shard_05" in exported
    assert "repo_shard_04" not in exported
    assert mean_path.is_file()
    mean_exported = mean_path.read_text(encoding="utf-8")
    mean_rows = list(csv.DictReader(io.StringIO(mean_exported)))
    expected_mean_columns = [
        "Generator(LLM)",
        "Prompt_Technique",
        "Total_Samples",
        "Compilation_0_Count",
        "Compilation_1_Count",
        "Compilation_Success_Rate",
        "Branch_Coverage%_Mean",
        "Line_Coverage%_Mean",
        "Method_Coverage%_Mean",
        "Mutation_Score%_Mean",
        "Assertion Roulette_Mean",
        "Conditional Test Logic_Mean",
        "Constructor Initialization_Mean",
        "Default Test_Mean",
        "EmptyTest_Mean",
        "Exception Handling_Mean",
        "General Fixture_Mean",
        "Mystery Guest_Mean",
        "Print Statement_Mean",
        "Redundant Assertion_Mean",
        "Sensitive Equality_Mean",
        "Verbose Test_Mean",
        "Sleepy Test_Mean",
        "Eager Test_Mean",
        "Lazy Test_Mean",
        "Duplicate Assert_Mean",
        "Unknown Test_Mean",
        "IgnoredTest_Mean",
        "Resource Optimism_Mean",
        "Magic Number Test_Mean",
        "Dependent Test_Mean",
    ]
    assert list(mean_rows[0].keys()) == expected_mean_columns
    assert len(mean_rows) == 1
    assert mean_rows[0]["Generator(LLM)"] == "a"
    assert mean_rows[0]["Prompt_Technique"] == "zero-shot"
    assert mean_rows[0]["Total_Samples"] == "2"
    assert mean_rows[0]["Compilation_0_Count"] == "1"
    assert mean_rows[0]["Compilation_1_Count"] == "1"
    assert mean_rows[0]["Compilation_Success_Rate"] == "0.5"
    assert mean_rows[0]["Line_Coverage%_Mean"] == "60.0"
    assert mean_rows[0]["Branch_Coverage%_Mean"] == "50.0"
    assert mean_rows[0]["Method_Coverage%_Mean"] == "70.0"
    assert mean_rows[0]["Mutation_Score%_Mean"] == "40.0"
    assert mean_rows[0]["Assertion Roulette_Mean"] == "1.0"
    assert mean_rows[0]["Conditional Test Logic_Mean"] == "3.0"


def test_shard05_status_reports_run_and_not_run_projects(monkeypatch, tmp_path):
    shard_root = tmp_path / "shards"
    shard_root.mkdir()
    (shard_root / "repo_shard_05.txt").write_text("100\n200\n", encoding="utf-8")
    for project_id, class_name in [("100", "com.example.A"), ("200", "com.example.B")]:
        project_dir = tmp_path / "dataset" / project_id
        project_dir.mkdir(parents=True)
        (project_dir / f"{project_id}_0.json").write_text(
            json.dumps({"focal_class": {"identifier": class_name, "file": f"src/{class_name}.java"}}),
            encoding="utf-8",
        )
    result = tmp_path / "runs" / "100" / "100_0" / "reports" / "records" / "100_0" / "a" / "zero-shot"
    result.mkdir(parents=True)
    (result / "result.json").write_text(
        json.dumps(
            {
                "run_id": "r",
                "shard_id": "repo_shard_05",
                "project_id": "100",
                "input_id": "100_0",
                "agent_name": "a",
                "generation_prompt_strategy": "zero-shot",
                "test_passed": True,
                "module_tests_passed": True,
            }
        ),
        encoding="utf-8",
    )

    monkeypatch.setattr(server, "PROJECT_ROOT", tmp_path)
    monkeypatch.setattr(server, "SHARD_ROOT", shard_root)
    monkeypatch.setattr(server, "DATASET_ROOT", tmp_path / "dataset")
    monkeypatch.setattr(server, "RUNS", {})

    payload = server._shard_status("repo_shard_05")

    assert payload["summary"]["total_projects"] == 2
    assert payload["summary"]["done"] == 1
    assert payload["summary"]["not_run"] == 1
    assert len(payload["experiments"]) == 1
    assert (payload["experiments"][0].get("sample_id") or payload["experiments"][0].get("input_id")) == "100_0"
    assert payload["experiments"][0]["agent_name"] == "a"
    assert payload["experiments"][0]["generation_prompt_strategy"] == "zero-shot"
    by_project = {item["project_id"]: item for item in payload["projects"]}
    assert by_project["100"]["status"] == "DONE"
    assert by_project["100"]["experiments_completed"] == 1
    assert by_project["100"]["prompt_statuses"]["zero-shot"]["status"] == "DONE"
    assert by_project["100"]["prompt_statuses"]["zero-shot"]["total"] == 1
    assert by_project["100"]["prompt_statuses"]["few-shot"]["status"] == "NOT_RUN"
    assert by_project["200"]["status"] == "NOT_RUN"


def test_shard05_status_reports_each_prompt_separately(monkeypatch, tmp_path):
    shard_root = tmp_path / "shards"
    shard_root.mkdir()
    (shard_root / "repo_shard_05.txt").write_text("100\n", encoding="utf-8")
    for prompt, passed in [("zero-shot", False), ("few-shot", True)]:
        result = tmp_path / "runs" / "100" / "100_0" / "reports" / "records" / "100_0" / "a" / prompt
        result.mkdir(parents=True)
        (result / "result.json").write_text(
            json.dumps(
                {
                    "run_id": f"r-{prompt}",
                    "shard_id": "repo_shard_05",
                    "project_id": "100",
                    "input_id": "100_0",
                    "agent_name": "a",
                    "generation_prompt_strategy": prompt,
                    "test_passed": passed,
                    "module_tests_passed": passed,
                    "final_failure_state": "MODULE_TESTS_PASSED" if passed else "COMPILE_FAILED",
                }
            ),
            encoding="utf-8",
        )

    monkeypatch.setattr(server, "PROJECT_ROOT", tmp_path)
    monkeypatch.setattr(server, "SHARD_ROOT", shard_root)
    monkeypatch.setattr(server, "DATASET_ROOT", tmp_path / "dataset")
    monkeypatch.setattr(server, "RUNS", {})

    project = server._shard_status("repo_shard_05")["projects"][0]

    assert project["status"] == "HAS_FAILURES"
    assert project["prompt_statuses"]["zero-shot"]["status"] == "HAS_FAILURES"
    assert project["prompt_statuses"]["zero-shot"]["failed"] == 1
    assert project["prompt_statuses"]["few-shot"]["status"] == "DONE"
    assert project["prompt_statuses"]["few-shot"]["passed"] == 1
    assert project["prompt_statuses"]["zero-shot-project-aware"]["status"] == "NOT_RUN"


def test_shard05_project_errors_return_copyable_artifacts(monkeypatch, tmp_path):
    shard_root = tmp_path / "shards"
    shard_root.mkdir()
    (shard_root / "repo_shard_05.txt").write_text("100\n", encoding="utf-8")
    result_dir = tmp_path / "runs" / "100" / "100_0" / "reports" / "records" / "100_0" / "a" / "zero-shot"
    experiment_dir = tmp_path / "runs" / "100" / "100_0" / "a" / "zero-shot"
    result_dir.mkdir(parents=True)
    experiment_dir.mkdir(parents=True)
    (experiment_dir / "baseline_build_output.txt").write_text("BUILD FAILURE\n[ERROR] cannot find symbol", encoding="utf-8")
    (result_dir / "result.json").write_text(
        json.dumps(
            {
                "run_id": "r",
                "shard_id": "repo_shard_05",
                "project_id": "100",
                "input_id": "100_0",
                "agent_name": "a",
                "generation_prompt_strategy": "zero-shot",
                "test_passed": False,
                "module_tests_passed": False,
                "final_failure_state": "COMPILE_FAILED",
                "experiment_dir": str(experiment_dir),
            }
        ),
        encoding="utf-8",
    )
    monkeypatch.setattr(server, "PROJECT_ROOT", tmp_path)
    monkeypatch.setattr(server, "SHARD_ROOT", shard_root)

    payload = server._shard_project_errors("100")

    assert payload["project_id"] == "100"
    assert payload["failed_experiments"] == 1
    assert "cannot find symbol" in payload["content"]
    assert payload["experiments"][0]["error_files"][0]["relative_path"] == "baseline_build_output.txt"

    filtered = server._shard_project_errors("100", prompt_strategy="few-shot")
    assert filtered["prompt_strategy"] == "few-shot"
    assert filtered["failed_experiments"] == 0
    assert filtered["content"] == ""


def test_shard05_project_error_endpoint(monkeypatch, tmp_path):
    monkeypatch.setattr(
        server,
        "_shard_project_errors",
        lambda project_id, shard_id="repo_shard_05", prompt_strategy="": {
            "project_id": project_id,
            "prompt_strategy": prompt_strategy,
            "content": "copy me",
        },
    )
    handler = _bare_dashboard_handler("/api/shards/shard05/projects/100/errors?prompt=zero-shot")

    handler.do_GET()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    assert handler.response_status == 200
    assert payload["project_id"] == "100"
    assert payload["prompt_strategy"] == "zero-shot"
    assert payload["content"] == "copy me"


def test_shard05_project_rerun_endpoint_writes_single_project_runtime_shard(monkeypatch, tmp_path):
    shard_root = tmp_path / "shards"
    runtime_root = tmp_path / "runtime_shards"
    log_root = tmp_path / "logs"
    config_root = tmp_path / "runtime_configs"
    record_root = tmp_path / "run_records"
    for path in (shard_root, runtime_root, log_root, config_root, record_root):
        path.mkdir()
    (shard_root / "repo_shard_05.txt").write_text("100\n200\n", encoding="utf-8")
    captured = {}

    def fake_start(payload):
        captured.update(payload)
        return {"id": "rerun-100", "request": payload}

    monkeypatch.setattr(server, "SHARD_ROOT", shard_root)
    monkeypatch.setattr(server, "RUNTIME_SHARD_ROOT", runtime_root)
    monkeypatch.setattr(server, "LOG_ROOT", log_root)
    monkeypatch.setattr(server, "RUNTIME_CONFIG_ROOT", config_root)
    monkeypatch.setattr(server, "RUN_RECORD_ROOT", record_root)
    monkeypatch.setattr(server, "_start_run", fake_start)
    body = json.dumps(
        {
            "input_mode": "project",
            "samples_per_project": "1",
            "agents": ["a"],
            "generation_prompts": ["zero-shot"],
        }
    ).encode("utf-8")
    handler = _bare_dashboard_handler("/api/shards/shard05/projects/100/rerun")
    handler.rfile = io.BytesIO(body)
    handler.headers = {"Content-Length": str(len(body))}

    handler.do_POST()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    runtime_shard = Path(captured["_resolved_repo_shard"])
    assert handler.response_status == 201
    assert payload["id"] == "rerun-100"
    assert captured["run_scope"] == "shard"
    assert captured["repo_shard"] == "repo_shard_05.txt"
    assert captured["shard_id"] == "repo_shard_05"
    assert captured["start_index"] == 0
    assert captured["limit"] == 0
    assert captured["selection"]["mode"] == "single_project"
    assert runtime_shard.read_text(encoding="utf-8") == "100\n"


def test_rq1_preview_endpoint_returns_latest_snapshot(monkeypatch):
    monkeypatch.setattr(
        server,
        "_rq1_preview",
        lambda query: {"query": query, "readiness": {"status": "NOT_READY"}},
    )
    handler = _bare_dashboard_handler(
        "/api/reports/rq1/preview?paired_page=2&details_page=3&page_size=25"
    )

    handler.do_GET()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    assert handler.response_status == 200
    assert payload["readiness"]["status"] == "NOT_READY"
    assert "paired_page=2" in payload["query"]


def test_rq1_workbook_export_endpoint_saves_server_file(monkeypatch):
    monkeypatch.setattr(
        server,
        "_save_rq1_workbook",
        lambda revision: {
            "relative_path": "export/RQ1/rq1_export_20260712_000000.xlsx",
            "preview_was_stale": revision != "current",
            "rows": {"Summary": 20, "Paired Samples": 2, "Raw Details": 4},
        },
    )
    body = json.dumps({"preview_revision": "old"}).encode("utf-8")
    handler = _bare_dashboard_handler("/api/reports/export/rq1")
    handler.rfile = io.BytesIO(body)
    handler.headers = {"Content-Length": str(len(body))}

    handler.do_POST()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    assert handler.response_status == 201
    assert payload["preview_was_stale"] is True
    assert payload["relative_path"].endswith(".xlsx")


def test_rq1_workbook_export_endpoint_reports_excel_limit(monkeypatch):
    def fail(_revision):
        raise server.RQ1ExcelLimitError("Raw Details", 1_048_577)

    monkeypatch.setattr(server, "_save_rq1_workbook", fail)
    handler = _bare_dashboard_handler("/api/reports/export/rq1")

    handler.do_POST()

    payload = json.loads(handler.wfile.getvalue().decode("utf-8"))
    assert handler.response_status == 422
    assert payload["sheet"] == "Raw Details"
    assert payload["rows"] == 1_048_577
    assert payload["max_rows"] == 1_048_576


def test_pipeline_command_accepts_multiple_generation_prompts(tmp_path):
    command = server._build_pipeline_command(
        {
            "run_scope": "single",
            "project_id": "10016717",
            "sample_file": "10016717_17.json",
            "generation_prompts": ["zero-shot", "few-shot"],
        },
        tmp_path / "runtime.yaml",
    )

    prompt_indexes = [index for index, item in enumerate(command) if item == "--generation-prompt"]
    assert [command[index + 1] for index in prompt_indexes] == ["zero-shot", "few-shot"]


def test_pipeline_command_accepts_multiple_agents(tmp_path):
    command = server._build_pipeline_command(
        {
            "run_scope": "single",
            "project_id": "10016717",
            "sample_file": "10016717_17.json",
            "agents": ["qwen-coder-1.5b", "qwen-coder-2.5-7b"],
            "generation_prompts": ["zero-shot"],
        },
        tmp_path / "runtime.yaml",
    )

    agent_indexes = [index for index, item in enumerate(command) if item == "--agent"]
    assert [command[index + 1] for index in agent_indexes] == ["qwen-coder-1.5b", "qwen-coder-2.5-7b"]


def test_runtime_config_records_selected_generation_prompts(monkeypatch, tmp_path):
    monkeypatch.setattr(server, "RUNTIME_CONFIG_ROOT", tmp_path)
    monkeypatch.setattr(
        server,
        "_project_config",
        lambda: {
            "prompts": {
                "generation_strategies": [
                    {"name": "zero-shot", "template": "zero.txt"},
                    {"name": "few-shot", "template": "few.txt"},
                ]
            },
            "experiment": {"run_all_generation_prompts": True},
        },
    )

    path = server._write_runtime_config(
        {"generation_prompts": ["zero-shot", "few-shot"]},
        "run-1",
    )
    config = server.yaml.safe_load(path.read_text(encoding="utf-8"))

    assert config["experiment"]["run_all_generation_prompts"] is False
    assert config["experiment"]["selected_generation_prompts"] == ["zero-shot", "few-shot"]


def test_runtime_config_records_selected_agents(monkeypatch, tmp_path):
    monkeypatch.setattr(server, "RUNTIME_CONFIG_ROOT", tmp_path)
    monkeypatch.setattr(
        server,
        "_project_config",
        lambda: {
            "llm": {
                "agents": [
                    {"name": "qwen-coder-1.5b", "model": "ollama/qwen-small"},
                    {"name": "qwen-coder-2.5-7b", "model": "ollama/qwen-large"},
                ]
            },
            "experiment": {"run_all_agents": True},
        },
    )

    path = server._write_runtime_config(
        {"agents": ["qwen-coder-1.5b", "qwen-coder-2.5-7b"]},
        "run-models",
    )
    config = server.yaml.safe_load(path.read_text(encoding="utf-8"))

    assert config["experiment"]["run_all_agents"] is False
    assert config["experiment"]["selected_agents"] == ["qwen-coder-1.5b", "qwen-coder-2.5-7b"]


def test_process_watcher_splits_logs_by_project(monkeypatch, tmp_path):
    monkeypatch.setattr(server, "LOG_ROOT", tmp_path / "logs")
    monkeypatch.setattr(server, "RUN_RECORD_ROOT", tmp_path / "run_records")
    run_id = "run-1"
    main_log = server.LOG_ROOT / "run-1.log"
    main_log.parent.mkdir(parents=True)
    server.RUNS.clear()
    server.RUNS[run_id] = {
        "id": run_id,
        "status": "running",
        "project_logs": [],
    }

    class FakeProcess:
        stdout = iter(
            [
                "[10:00:00] START 100_0 | project=100 | agent=qwen | prompt=zero-shot\n",
                "project 100 output\n",
                "[10:00:01] FINISH 100_0 status=PASSED elapsed=1s\n",
                "[10:00:02] START 200_0 | project=200 | agent=qwen | prompt=few-shot\n",
                "project 200 output\n",
                "[10:00:03] FINISH 200_0 status=FAILED elapsed=1s\n",
            ]
        )

        @staticmethod
        def wait():
            return 0

    server._watch_process(run_id, FakeProcess(), main_log)

    projects = server.RUNS[run_id]["project_logs"]
    assert [(item["project_id"], item["status"], item["experiments_completed"]) for item in projects] == [
        ("100", "completed", 1),
        ("200", "completed", 1),
    ]
    project_100 = server._project_log_path(run_id, "100").read_text(encoding="utf-8")
    project_200 = server._project_log_path(run_id, "200").read_text(encoding="utf-8")
    assert "project 100 output" in project_100
    assert "project 200 output" not in project_100
    assert "project 200 output" in project_200
    assert "project 100 output" not in project_200


def test_finish_event_reads_passed_flag():
    failed = server._finish_event("[10:00:03] FINISH 200_0 status=FAILED passed=False elapsed=1s")
    passed = server._finish_event("[10:00:03] FINISH 200_0 status=REPAIRED passed=True elapsed=1s")

    assert failed == {"sample_id": "200_0", "status": "FAILED", "passed": "false"}
    assert passed == {"sample_id": "200_0", "status": "REPAIRED", "passed": "true"}


def _rerun_roots(monkeypatch, tmp_path):
    shard_root = tmp_path / "shards"
    runtime_shard_root = tmp_path / "runtime_shards"
    shard_root.mkdir()
    runtime_shard_root.mkdir()
    shard = shard_root / "repo_shard_00.txt"
    shard.write_text("100\n200\n300\n", encoding="utf-8")
    monkeypatch.setattr(server, "SHARD_ROOT", shard_root)
    monkeypatch.setattr(server, "RUNTIME_SHARD_ROOT", runtime_shard_root)
    return shard


def test_prepare_failed_only_shard(monkeypatch, tmp_path):
    _rerun_roots(monkeypatch, tmp_path)
    server.RUNS.clear()
    server.RUNS["old-run"] = {
        "id": "old-run",
        "status": "completed",
        "request": {"run_scope": "shard", "repo_shard": "repo_shard_00.txt"},
        "project_logs": [
            {"project_id": "100", "failed_experiments": 0, "last_experiment_passed": True},
            {"project_id": "200", "failed_experiments": 1, "last_experiment_passed": False},
        ],
    }

    prepared = server._prepare_run_payload(
        {
            "run_scope": "shard",
            "repo_shard": "repo_shard_00.txt",
            "rerun_mode": "failed_only",
            "source_run_id": "old-run",
            "start_index": 22,
            "limit": 5,
        },
        "new-run",
    )

    selected = Path(prepared["_resolved_repo_shard"]).read_text(encoding="utf-8").splitlines()
    assert selected == ["200"]
    assert prepared["start_index"] == 0
    assert prepared["limit"] == 0
    assert prepared["selection"]["project_count"] == 1


def test_prepare_resume_includes_stopped_project_and_remaining(monkeypatch, tmp_path):
    _rerun_roots(monkeypatch, tmp_path)
    server.RUNS.clear()
    server.RUNS["stopped-run"] = {
        "id": "stopped-run",
        "status": "stopped",
        "request": {"run_scope": "shard", "repo_shard": "repo_shard_00.txt"},
        "project_logs": [
            {"project_id": "100", "status": "completed"},
            {"project_id": "200", "status": "stopped"},
        ],
    }

    prepared = server._prepare_run_payload(
        {
            "run_scope": "shard",
            "repo_shard": "repo_shard_00.txt",
            "rerun_mode": "resume",
            "source_run_id": "stopped-run",
        },
        "continued-run",
    )

    selected = Path(prepared["_resolved_repo_shard"]).read_text(encoding="utf-8").splitlines()
    assert selected == ["200", "300"]
    assert prepared["selection"]["first_project_id"] == "200"


def test_prepare_failed_then_resume_orders_failures_before_remaining_and_deduplicates(monkeypatch, tmp_path):
    _rerun_roots(monkeypatch, tmp_path)
    server.RUNS.clear()
    server.RUNS["stopped-with-failures"] = {
        "id": "stopped-with-failures",
        "status": "stopped",
        "request": {"run_scope": "shard", "repo_shard": "repo_shard_00.txt"},
        "project_logs": [
            {
                "project_id": "100",
                "status": "completed",
                "failed_experiments": 1,
                "last_experiment_passed": False,
            },
            {
                "project_id": "200",
                "status": "stopped",
                "failed_experiments": 1,
                "last_experiment_passed": False,
            },
        ],
    }

    prepared = server._prepare_run_payload(
        {
            "run_scope": "shard",
            "repo_shard": "repo_shard_00.txt",
            "rerun_mode": "failed_then_resume",
            "source_run_id": "stopped-with-failures",
        },
        "recovered-run",
    )

    selected = Path(prepared["_resolved_repo_shard"]).read_text(encoding="utf-8").splitlines()
    assert selected == ["100", "200", "300"]
    assert selected.count("200") == 1
    assert prepared["selection"]["mode"] == "failed_then_resume"


def test_pipeline_command_uses_prepared_runtime_shard(monkeypatch, tmp_path):
    _rerun_roots(monkeypatch, tmp_path)
    runtime_shard = server.RUNTIME_SHARD_ROOT / "new-run-failed_only.txt"
    runtime_shard.write_text("200\n", encoding="utf-8")

    command = server._build_pipeline_command(
        {
            "run_scope": "shard",
            "repo_shard": "repo_shard_00.txt",
            "_resolved_repo_shard": str(runtime_shard),
            "generation_prompts": ["zero-shot"],
        },
        tmp_path / "runtime.yaml",
    )

    assert command[command.index("--repo-shard") + 1] == str(runtime_shard)


def test_load_run_records_migrates_old_logs_for_resume(monkeypatch, tmp_path):
    log_root = tmp_path / "logs"
    config_root = tmp_path / "runtime_configs"
    record_root = tmp_path / "run_records"
    runtime_shard_root = tmp_path / "runtime_shards"
    shard_root = tmp_path / "shards"
    for path in (log_root, config_root, record_root, runtime_shard_root, shard_root):
        path.mkdir()
    (shard_root / "repo_shard_00.txt").write_text("100\n200\n300\n", encoding="utf-8")
    (config_root / "old-run.yaml").write_text(
        "run:\n  shard_id: repo_shard_00\ninput:\n  mode: project\n  samples_per_project: 1\n",
        encoding="utf-8",
    )
    (log_root / "old-run.log").write_text(
        "[10:00:00] START 100_0 | project=100 | agent=qwen | prompt=zero-shot\n"
        "[10:00:01] FINISH 100_0 status=FAILED passed=False elapsed=1s\n"
        "[10:00:02] START 200_0 | project=200 | agent=qwen | prompt=zero-shot\n",
        encoding="utf-8",
    )
    monkeypatch.setattr(server, "LOG_ROOT", log_root)
    monkeypatch.setattr(server, "RUNTIME_CONFIG_ROOT", config_root)
    monkeypatch.setattr(server, "RUN_RECORD_ROOT", record_root)
    monkeypatch.setattr(server, "RUNTIME_SHARD_ROOT", runtime_shard_root)
    monkeypatch.setattr(server, "SHARD_ROOT", shard_root)
    server.RUNS.clear()

    server._load_run_records()

    migrated = server.RUNS["old-run"]
    assert migrated["status"] == "stopped"
    assert migrated["request"]["repo_shard"] == "repo_shard_00.txt"
    assert migrated["project_logs"][0]["failed_experiments"] == 1
    assert migrated["project_logs"][1]["status"] == "stopped"
    assert (record_root / "old-run.json").is_file()


def test_error_artifacts_include_every_failed_build_file(tmp_path):
    experiment = tmp_path / "experiment"
    checkpoint = experiment / "repair" / "checkpoints" / "attempt_1"
    checkpoint.mkdir(parents=True)
    (experiment / "baseline_build_output.txt").write_text("BUILD SUCCESS", encoding="utf-8")
    (experiment / "mutation_prepare_build_output.txt").write_text(
        "\x1b[1;31m[ERROR]\x1b[0m cannot find symbol\nBUILD FAILURE",
        encoding="utf-8",
    )
    (experiment / "metrics_report.json").write_text(
        '{"mutation_error": "pitest dependency prepare failed"}',
        encoding="utf-8",
    )
    (checkpoint / "build_output_after.txt").write_text(
        "COMPILATION ERROR\n/path/FooTest.java:[12,4] cannot find symbol",
        encoding="utf-8",
    )

    summaries = server._error_artifact_summaries(experiment)
    paths = [item["relative_path"] for item in summaries]
    combined = server._combined_error_artifacts(experiment)

    assert "baseline_build_output.txt" not in paths
    assert "mutation_prepare_build_output.txt" in paths
    assert "metrics_report.json" in paths
    assert "repair/checkpoints/attempt_1/build_output_after.txt" in paths
    assert "\x1b" not in combined
    assert "===== mutation_prepare_build_output.txt =====" in combined
    assert "===== repair/checkpoints/attempt_1/build_output_after.txt =====" in combined


def test_old_experiment_token_usage_is_read_from_generation_metadata(tmp_path):
    experiment = tmp_path / "experiment"
    experiment.mkdir()
    (experiment / "generation_metadata.json").write_text(
        '{"metadata":{"usage":{"prompt_tokens":1228,"completion_tokens":125,"total_tokens":1353}}}',
        encoding="utf-8",
    )

    report = server._experiment_token_usage(
        {"generation_prompt_strategy": "zero-shot"},
        experiment,
    )

    assert report["llm_input_tokens"] == 1228
    assert report["llm_output_tokens"] == 125
    assert report["llm_total_tokens"] == 1353
    assert report["token_usage_by_prompt"]["generation:zero-shot"]["calls"] == 1
