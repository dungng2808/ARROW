"""Re-run quality metrics for an existing experiment without invoking an LLM."""

from __future__ import annotations

import json
import shutil
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from .build_runner import BuildContext
from .fs_utils import atomic_write_json, atomic_write_text, ensure_dir
from .import_repair import repair_imports_for_context
from .input_selector import load_sample
from .java_resolver import resolve_java_home
from .metrics_runner import run_maven_metrics
from .models import SampleInput
from .project_analyzer import analyze_experiment
from .repo_manager import checkout_dataset_revision, clone_repo, ensure_experiment_workspace, safe_remove_tree
from .report_writer import (
    ensure_paper_report_fields,
    load_experiment_jsonl,
    write_experiment_json,
    write_merged_jsonl,
)
from .test_writer import code_hash, validate_java_candidate, write_owned_generated_test


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def _sample_from_row(row: dict[str, Any], project_root: Path) -> SampleInput:
    sample_path = Path(str(row.get("sample_file") or ""))
    if sample_path.is_file():
        return load_sample(sample_path, sample_path.parent.parent)
    raw = {}
    context_path = Path(str(row.get("experiment_dir") or "")) / "context.json"
    if context_path.is_file():
        try:
            raw = json.loads(context_path.read_text(encoding="utf-8")).get("sample") or {}
        except json.JSONDecodeError:
            raw = {}
    repository = raw.get("repository") or {}
    focal = raw.get("focal_class") or {}
    test = raw.get("test_class") or {}
    return SampleInput(
        project_id=str(row.get("project_id") or ""),
        sample_file=sample_path,
        repository_url=str(row.get("repository_url") or repository.get("url") or ""),
        focal_class_name=str(row.get("focal_class") or focal.get("identifier") or ""),
        focal_class_path=str(row.get("focal_class_path") or focal.get("file") or ""),
        test_class_name=str(test.get("identifier") or ""),
        test_class_path=str(test.get("file") or ""),
        raw=raw,
    )


def _checkpoint_source(experiment_dir: Path) -> Path | None:
    root = experiment_dir / "repair" / "checkpoints"
    candidates = [path for path in root.glob("attempt_*/generated_test_after.java") if path.is_file()]
    if candidates:
        summary_path = experiment_dir / "repair_summary.json"
        if summary_path.is_file():
            try:
                best_hash = str(json.loads(summary_path.read_text(encoding="utf-8")).get("best_candidate_hash") or "")
            except json.JSONDecodeError:
                best_hash = ""
            if best_hash:
                for candidate in reversed(sorted(candidates, key=lambda path: int(path.parent.name.split("_")[-1]))):
                    content = candidate.read_text(encoding="utf-8", errors="replace")
                    if content and code_hash(content) == best_hash:
                        return candidate
        return sorted(candidates, key=lambda path: int(path.parent.name.split("_")[-1]))[-1]
    source = experiment_dir / "llm_response.txt"
    return source if source.is_file() else None


def _write_verification_artifacts(experiment_dir: Path, verifications: dict[str, Any]) -> None:
    for name, verification in verifications.items():
        atomic_write_json(experiment_dir / f"{name}_verification.json", verification.to_dict())
        atomic_write_text(experiment_dir / f"{name}_build_output.txt", verification.raw_output or "")


def _update_jsonl(row: dict[str, Any]) -> None:
    reports_dir = Path(str(row.get("reports_dir") or ""))
    jsonl_path = reports_dir / "records" / "experiments.jsonl"
    if not jsonl_path.is_file():
        return
    key = tuple(str(row.get(item, "")) for item in ("run_id", "shard_id", "input_id", "agent_name", "generation_prompt_strategy"))
    rows = load_experiment_jsonl([jsonl_path])
    updated = False
    for index, current in enumerate(rows):
        current_key = tuple(str(current.get(item, "")) for item in ("run_id", "shard_id", "input_id", "agent_name", "generation_prompt_strategy"))
        if current_key == key:
            rows[index] = row
            updated = True
    if updated:
        write_merged_jsonl(jsonl_path, rows)


def _apply_metrics(row: dict[str, Any], metrics: Any) -> dict[str, Any]:
    values = metrics.to_dict()
    row.update(values)
    row.update(
        {
            "Branch_Coverage%": values.get("coverage_branch", ""),
            "Line_Coverage%": values.get("coverage_line", ""),
            "Method_Coverage%": values.get("coverage_method", ""),
            "Mutation_Score%": values.get("mutation_score", ""),
            "coverage_branch": values.get("coverage_branch", ""),
            "coverage_line": values.get("coverage_line", ""),
            "coverage_method": values.get("coverage_method", ""),
            "mutation_score": values.get("mutation_score", ""),
            "metrics_rerun_at": _now_iso(),
            "metrics_rerun_status": "COMPLETED",
            "metrics_rerun_error": "",
        }
    )
    return ensure_paper_report_fields(row)


def recompute_metrics(*, result_path: Path, project_root: Path, config: dict[str, Any]) -> dict[str, Any]:
    """Rebuild one experiment workspace and run metrics only.

    The generated test is recovered from the last repair checkpoint or the
    original saved response. No LiteLLM client, prompt generation, or repair
    loop is created here.
    """
    row = json.loads(result_path.read_text(encoding="utf-8"))
    experiment_dir = Path(str(row.get("experiment_dir") or result_path.parent))
    source = _checkpoint_source(experiment_dir)
    if source is None:
        raise ValueError("Không tìm thấy mã generated test đã lưu để đo lại metrics")
    sample = _sample_from_row(row, project_root)
    if not sample.repository_url or not sample.focal_class_path:
        raise ValueError("Experiment thiếu repository hoặc focal class path")

    metrics_root = experiment_dir / ".metrics_recompute"
    if metrics_root.exists():
        safe_remove_tree(metrics_root, experiment_dir)
    ensure_dir(metrics_root)
    started = time.monotonic()
    try:
        repo = clone_repo(sample.repository_url, metrics_root / "repo")
        if bool(config.get("repo", {}).get("checkout_commit", True)):
            checkout_dataset_revision(repo, sample.focal_class_path, sample.test_class_path)
        workspace = metrics_root / "workspace"
        ensure_experiment_workspace(cached_repo=repo, experiment_workspace=workspace)
        run_id = str(row.get("run_id") or "metrics")
        shard_id = str(row.get("shard_id") or "local")
        agent_name = str(row.get("agent_name") or "agent")
        prompt = str(row.get("generation_prompt_strategy") or "prompt")
        context, module_root = analyze_experiment(
            sample=sample,
            workspace=workspace,
            run_id=run_id,
            shard_id=shard_id,
            agent_name=agent_name,
            generation_prompt=prompt,
        )
        source_code = source.read_text(encoding="utf-8", errors="replace")
        code, _ = validate_java_candidate(
            source_code,
            expected_package=context.package_name,
            expected_class_name=context.generated_test_class_name,
            testing_framework=context.testing_framework,
        )
        repaired = repair_imports_for_context(code, context)
        code = repaired.code
        experiment_id = f"{run_id}:{shard_id}:{sample.input_id}:{agent_name}:{prompt}"
        write_owned_generated_test(
            experiment_id=experiment_id,
            workspace=workspace,
            generated_test_path=context.generated_test_path,
            generated_test_class_name=context.generated_test_class_name,
            code=code,
        )
        java_selection = resolve_java_home(workspace, module_root, config, manual_java_home=str(row.get("java_home") or "") or None)
        build_cfg = config.get("build", {})
        maven_cfg = build_cfg.get("maven", {})
        build_context = BuildContext(
            repository_root=workspace,
            module_root=module_root,
            build_tool=context.build_tool,
            generated_test_class_name=context.generated_test_class_name,
            generated_test_fqcn=f"{context.package_name}.{context.generated_test_class_name}" if context.package_name else context.generated_test_class_name,
            timeout_seconds=int(build_cfg.get("test_timeout_seconds", 900)),
            prefer_wrapper=bool(build_cfg.get("prefer_wrapper", True)),
            java_home=java_selection.java_home or None,
            maven_multi_module_strategy=maven_cfg.get("multi_module_strategy", "module_only"),
            maven_use_also_make=bool(maven_cfg.get("use_also_make", True)),
            maven_fail_if_no_specified_tests=bool(maven_cfg.get("fail_if_no_specified_tests", False)),
        )
        if context.build_tool != "maven":
            raise ValueError(f"Đo lại metrics hiện chỉ hỗ trợ Maven; build tool của experiment là {context.build_tool}")
        metrics_cfg = config.get("metrics", {})
        metrics, verifications = run_maven_metrics(
            build_context,
            sample.focal_class_name,
            f"{context.package_name}.{sample.focal_class_name}" if context.package_name else sample.focal_class_name,
            context.testing_framework,
            sample.project_id,
            generated_test_path=context.generated_test_path,
            focal_class_path=sample.focal_class_path,
            smells_enabled=bool(metrics_cfg.get("smells", True)),
        )
        _write_verification_artifacts(experiment_dir, verifications)
        atomic_write_json(experiment_dir / "metrics_report.json", metrics.to_dict())
        row["metrics_rerun_elapsed_seconds"] = round(time.monotonic() - started, 3)
        row = _apply_metrics(row, metrics)
        write_experiment_json(result_path, row)
        _update_jsonl(row)
        return {
            "status": "completed",
            "result_path": str(result_path),
            "metrics": metrics.to_dict(),
            "elapsed_seconds": row["metrics_rerun_elapsed_seconds"],
        }
    except Exception as exc:
        row["metrics_rerun_at"] = _now_iso()
        row["metrics_rerun_status"] = "FAILED"
        row["metrics_rerun_error"] = str(exc)
        write_experiment_json(result_path, row)
        _update_jsonl(row)
        raise
    finally:
        if metrics_root.exists():
            shutil.rmtree(metrics_root, ignore_errors=True)
