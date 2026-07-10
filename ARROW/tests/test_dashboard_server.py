from __future__ import annotations

from pathlib import Path

from dashboard import server


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


def test_process_watcher_splits_logs_by_project(monkeypatch, tmp_path):
    monkeypatch.setattr(server, "LOG_ROOT", tmp_path / "logs")
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
