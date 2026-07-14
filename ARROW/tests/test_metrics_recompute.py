import json
from pathlib import Path

from src import metrics_recompute
from src.metrics_runner import MetricsResult
from src.models import ExperimentContext


def test_recompute_metrics_updates_result_without_llm(monkeypatch, tmp_path):
    project_root = tmp_path / "ARROW"
    experiment_dir = project_root / "runs" / "repo" / "sample" / "agent" / "zero-shot"
    result_path = project_root / "runs" / "repo" / "sample" / "reports" / "records" / "sample" / "agent" / "zero-shot" / "result.json"
    sample_path = tmp_path / "dataset" / "project" / "sample.json"
    sample_path.parent.mkdir(parents=True)
    sample_path.write_text(
        json.dumps(
            {
                "repository": {"url": "https://example.invalid/repo"},
                "focal_class": {"identifier": "Focal", "file": "src/main/java/demo/Focal.java"},
                "test_class": {"identifier": "HumanTest", "file": "src/test/java/demo/HumanTest.java"},
            }
        ),
        encoding="utf-8",
    )
    experiment_dir.mkdir(parents=True)
    (experiment_dir / "llm_response.txt").write_text(
        "package demo; public class FocalTest_abcd1234 { @org.junit.Test public void works() {} }",
        encoding="utf-8",
    )
    row = {
        "run_id": "run-1",
        "shard_id": "repo_shard_05",
        "input_id": "sample",
        "sample_id": "sample",
        "project_id": "project",
        "sample_file": str(sample_path),
        "repository_url": "https://example.invalid/repo",
        "focal_class_path": "src/main/java/demo/Focal.java",
        "agent_name": "agent",
        "generation_prompt_strategy": "zero-shot",
        "experiment_dir": str(experiment_dir),
        "reports_dir": str(result_path.parents[4]),
        "java_home": "",
    }
    result_path.parent.mkdir(parents=True)
    result_path.write_text(json.dumps(row), encoding="utf-8")
    (result_path.parents[4] / "records").mkdir(parents=True, exist_ok=True)
    (result_path.parents[4] / "records" / "experiments.jsonl").write_text(json.dumps(row) + "\n", encoding="utf-8")

    def fake_clone(_url, destination):
        destination.mkdir(parents=True)
        return destination

    def fake_workspace(*, cached_repo, experiment_workspace):
        (experiment_workspace / "src" / "main" / "java" / "demo").mkdir(parents=True)
        (experiment_workspace / "src" / "main" / "java" / "demo" / "Focal.java").write_text("package demo; public class Focal {}", encoding="utf-8")
        return experiment_workspace

    def fake_analyze(*, sample, workspace, run_id, shard_id, agent_name, generation_prompt):
        module_root = workspace
        context = ExperimentContext(
            run_id=run_id,
            shard_id=shard_id,
            input_id=sample.input_id,
            agent_name=agent_name,
            generation_prompt=generation_prompt,
            workspace=workspace,
            generated_test_path=workspace / "src" / "test" / "java" / "demo" / "FocalTest_abcd1234.java",
            generated_test_class_name="FocalTest_abcd1234",
            package_name="demo",
            testing_framework="junit4",
            build_tool="maven",
            module_path=".",
        )
        return context, module_root

    def fake_metrics(*args, **kwargs):
        return MetricsResult(coverage_line="88.00", coverage_method="90.00", mutation_score="42.00"), {}

    monkeypatch.setattr(metrics_recompute, "clone_repo", fake_clone)
    monkeypatch.setattr(metrics_recompute, "checkout_dataset_revision", lambda *args: "")
    monkeypatch.setattr(metrics_recompute, "ensure_experiment_workspace", fake_workspace)
    monkeypatch.setattr(metrics_recompute, "analyze_experiment", fake_analyze)
    monkeypatch.setattr(metrics_recompute, "run_maven_metrics", fake_metrics)
    monkeypatch.setattr(metrics_recompute, "resolve_java_home", lambda *args, **kwargs: type("Selection", (), {"java_home": ""})())

    output = metrics_recompute.recompute_metrics(
        result_path=result_path,
        project_root=project_root,
        config={"repo": {"checkout_commit": False}, "build": {}, "metrics": {"smells": True}},
    )

    assert output["status"] == "completed"
    updated = json.loads(result_path.read_text(encoding="utf-8"))
    assert updated["Line_Coverage%"] == "88.00"
    assert updated["Mutation_Score%"] == "42.00"
    assert updated["metrics_rerun_status"] == "COMPLETED"

