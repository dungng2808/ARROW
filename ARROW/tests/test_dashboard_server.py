from __future__ import annotations

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


def test_dashboard_contains_merge_button_and_api_binding():
    html = (server.STATIC_ROOT / "index.html").read_text(encoding="utf-8")
    javascript = (server.STATIC_ROOT / "app.js").read_text(encoding="utf-8")

    assert 'id="mergeReportsBtn"' in html
    assert 'id="mergeStatus"' in html
    assert 'api("/api/reports/merge"' in javascript


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
