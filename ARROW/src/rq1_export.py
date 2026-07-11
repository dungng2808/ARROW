from __future__ import annotations

import hashlib
import json
import os
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import xlsxwriter

from .report_writer import (
    RQ1_DETAILS_COLUMNS,
    RQ1_PAIRED_COLUMNS,
    RQ1_PROMPT_STRATEGIES,
    RQ1_SUMMARY_COLUMNS,
    build_rq1_details_rows,
    build_rq1_paired_rows,
    build_rq1_summary_rows,
    load_experiment_jsonl,
    merge_rows,
)


EXCEL_MAX_ROWS = 1_048_576
RQ1_NOT_READY_WARNING = "Dữ liệu RQ1 chưa sẵn sàng để kết luận thống kê"
STRATEGY_LABELS = {
    "zero-shot": "Zero-shot",
    "few-shot": "Few-shot",
    "zero-shot-project-aware": "Repository-aware",
}
COMPARISON_SPECS = (
    ("repo_vs_zero_compile", "Repository-aware so với Zero-shot", "Biên dịch"),
    ("repo_vs_few_compile", "Repository-aware so với Few-shot", "Biên dịch"),
    ("repo_vs_zero_execution", "Repository-aware so với Zero-shot", "Thực thi"),
    ("repo_vs_few_execution", "Repository-aware so với Few-shot", "Thực thi"),
)
RESULT_TABLE_COLUMNS = [
    "Chiến lược prompt",
    "Tổng số mẫu",
    "Kiểm thử biên dịch, n (%)",
    "Kiểm thử thực thi, n (%)",
    "Kiểm thử mục tiêu đạt, n (%)",
]
MODEL_RESULT_TABLE_COLUMNS = ["Tác nhân", "Mô hình", *RESULT_TABLE_COLUMNS]
RESULT_LABELS_VI = {
    "IMPROVED": "CẢI THIỆN",
    "PARTIAL_IMPROVEMENT": "CẢI THIỆN MỘT PHẦN",
    "WORSE": "KÉM HƠN",
    "NO_SIGNIFICANT_DIFFERENCE": "KHÔNG KHÁC BIỆT CÓ Ý NGHĨA",
    "NO_SIGNIFICANT_IMPROVEMENT": "CHƯA CÓ CẢI THIỆN CÓ Ý NGHĨA",
    "NO_REPOSITORY_AWARE_IS_WORSE": "REPOSITORY-AWARE KÉM HƠN",
    "YES_IMPROVES_COMPILE_AND_EXECUTION": "CÓ CẢI THIỆN",
    "INSUFFICIENT_DATA": "CHƯA ĐỦ DỮ LIỆU",
}


class RQ1SnapshotChangedError(RuntimeError):
    pass


class RQ1NoDataError(ValueError):
    pass


class RQ1ExcelLimitError(ValueError):
    def __init__(self, sheet: str, rows: int, max_rows: int = EXCEL_MAX_ROWS) -> None:
        super().__init__(f"Sheet {sheet} cần {rows} dòng; Excel chỉ hỗ trợ tối đa {max_rows} dòng")
        self.sheet = sheet
        self.rows = rows
        self.max_rows = max_rows


@dataclass(frozen=True)
class RQ1Snapshot:
    source_revision: str
    generated_at: str
    source_files: int
    duplicate_rows: int
    source_rows: int
    rows: list[dict[str, Any]]
    summary_rows: list[dict[str, Any]]
    paired_rows: list[dict[str, Any]]
    detail_rows: list[dict[str, Any]]


def find_rq1_source_paths(runs_dir: Path) -> list[Path]:
    if not runs_dir.is_dir():
        return []
    return sorted(runs_dir.glob("**/reports/records/experiments.jsonl"))


def source_revision(paths: list[Path], runs_dir: Path) -> str:
    digest = hashlib.sha256()
    root = runs_dir.resolve()
    for path in paths:
        stat = path.stat()
        try:
            name = path.resolve().relative_to(root).as_posix()
        except ValueError:
            name = str(path.resolve())
        digest.update(f"{name}\0{stat.st_size}\0{stat.st_mtime_ns}\n".encode("utf-8"))
    return digest.hexdigest()


def load_rq1_snapshot(runs_dir: Path, retries: int = 1) -> RQ1Snapshot:
    attempts = max(0, retries) + 1
    for _attempt in range(attempts):
        paths_before = find_rq1_source_paths(runs_dir)
        revision_before = source_revision(paths_before, runs_dir)
        loaded = load_experiment_jsonl(paths_before)
        merged, duplicates = merge_rows(loaded)
        paths_after = find_rq1_source_paths(runs_dir)
        revision_after = source_revision(paths_after, runs_dir)
        if revision_before != revision_after or paths_before != paths_after:
            continue
        rq1_rows = [
            row
            for row in merged
            if str(row.get("generation_prompt_strategy") or row.get("Prompt_Technique") or "").strip().lower()
            in RQ1_PROMPT_STRATEGIES
        ]
        return RQ1Snapshot(
            source_revision=revision_after,
            generated_at=datetime.now(timezone.utc).isoformat(),
            source_files=len(paths_after),
            duplicate_rows=duplicates,
            source_rows=len(loaded),
            rows=rq1_rows,
            summary_rows=build_rq1_summary_rows(rq1_rows),
            paired_rows=build_rq1_paired_rows(rq1_rows),
            detail_rows=build_rq1_details_rows(rq1_rows),
        )
    raise RQ1SnapshotChangedError("Dữ liệu nguồn RQ1 thay đổi trong lúc đọc; vui lòng thử lại")


def _primary_scope_rows(summary_rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    overall = [row for row in summary_rows if row.get("scope") == "overall"]
    if overall:
        return overall
    if not summary_rows:
        return []
    first = summary_rows[0]
    return [
        row
        for row in summary_rows
        if row.get("scope") == first.get("scope")
        and row.get("agent_name") == first.get("agent_name")
        and row.get("model") == first.get("model")
    ]


def _comparison_rows(scope_rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    if not scope_rows:
        return []
    source = scope_rows[0]
    return [
        {
            "key": prefix,
            "comparison": comparison,
            "metric": metric,
            "paired_samples": source.get(
                "compile_paired_samples" if prefix.endswith("_compile") else "execution_paired_samples",
                0,
            ),
            "improvement_pp": source.get(f"{prefix}_improvement_pp", ""),
            "wins": source.get(f"{prefix}_wins", 0),
            "losses": source.get(f"{prefix}_losses", 0),
            "ties": source.get(f"{prefix}_ties", 0),
            "p_value": source.get(f"{prefix}_p_value", ""),
            "p_method": source.get(f"{prefix}_p_method", ""),
            "holm_p_value": source.get(f"{prefix}_holm_p_value", ""),
            "result": source.get(f"{prefix}_result", "INSUFFICIENT_DATA"),
        }
        for prefix, comparison, metric in COMPARISON_SPECS
    ]


def _readiness(scope_rows: list[dict[str, Any]], detail_count: int) -> dict[str, Any]:
    source = scope_rows[0] if scope_rows else {}
    total = int(source.get("total_samples") or 0)
    available = {
        "zero-shot": int(source.get("zero_shot_available_samples") or 0),
        "few-shot": int(source.get("few_shot_available_samples") or 0),
        "zero-shot-project-aware": int(source.get("repository_aware_available_samples") or 0),
    }
    missing = [STRATEGY_LABELS[key] for key, value in available.items() if value < total or total == 0]
    ready = bool(source.get("data_ready"))
    return {
        "status": "READY" if ready else "NOT_READY",
        "data_ready": ready,
        "total_samples": total,
        "available_samples": available,
        "complete_triplets": int(source.get("complete_triplets") or 0),
        "compile_evaluable_triplets": int(source.get("compile_paired_samples") or 0),
        "execution_evaluable_triplets": int(source.get("execution_paired_samples") or 0),
        "missing_strategies": missing,
        "rq1_records": detail_count,
        "warning": "" if ready else RQ1_NOT_READY_WARNING,
    }


def _strategy_rows(scope_rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    by_strategy = {str(row.get("prompt_strategy")): row for row in scope_rows}
    output = []
    for strategy in RQ1_PROMPT_STRATEGIES:
        row = by_strategy.get(strategy, {})
        output.append(
            {
                "strategy": strategy,
                "label": STRATEGY_LABELS[strategy],
                "compile_evaluable": int(row.get("compile_paired_samples") or 0),
                "compile_success": int(row.get("compile_success_count") or 0),
                "compile_rate_pct": row.get("compile_success_rate_pct", ""),
                "execution_evaluable": int(row.get("execution_paired_samples") or 0),
                "execution_success": int(row.get("execution_success_count") or 0),
                "execution_rate_pct": row.get("execution_success_rate_pct", ""),
            }
        )
    return output


def _prefix(strategy: str) -> str:
    return {
        "zero-shot": "zero_shot",
        "few-shot": "few_shot",
        "zero-shot-project-aware": "repository_aware",
    }[strategy]


def _is_true(value: Any) -> bool:
    if isinstance(value, bool):
        return value
    return str(value).strip().lower() in {"true", "1", "yes"}


def _rate_pct(count: int, total: int) -> float | str:
    if total <= 0:
        return ""
    return round((count / total) * 100, 2)


def _vi_result(value: Any) -> Any:
    return RESULT_LABELS_VI.get(str(value), value)


def _result_table_row(strategy: str, rows: list[dict[str, Any]]) -> dict[str, Any]:
    prefix = _prefix(strategy)
    available = [row for row in rows if row.get(f"{prefix}_run_id")]
    total = len(available)
    compiled = sum(_is_true(row.get(f"{prefix}_initial_compile_success")) for row in available)
    executed = sum(_is_true(row.get(f"{prefix}_initial_target_pass")) for row in available)
    target_passed = sum(_is_true(row.get(f"{prefix}_final_target_pass")) for row in available)
    return {
        "prompt_strategy": strategy,
        "prompt_strategy_label": STRATEGY_LABELS[strategy],
        "total_samples": total,
        "compiled_count": compiled,
        "compiled_rate_pct": _rate_pct(compiled, total),
        "executed_count": executed,
        "executed_rate_pct": _rate_pct(executed, total),
        "target_passed_count": target_passed,
        "target_passed_rate_pct": _rate_pct(target_passed, total),
    }


def _result_tables(paired_rows: list[dict[str, Any]]) -> dict[str, Any]:
    model_groups: dict[tuple[str, str], list[dict[str, Any]]] = {}
    for row in paired_rows:
        key = (str(row.get("agent_name") or "N/A"), str(row.get("model") or "N/A"))
        model_groups.setdefault(key, []).append(row)

    overall_rows = [_result_table_row(strategy, paired_rows) for strategy in RQ1_PROMPT_STRATEGIES]
    by_model_rows: list[dict[str, Any]] = []
    for (agent, model), rows in sorted(model_groups.items()):
        for strategy in RQ1_PROMPT_STRATEGIES:
            item = _result_table_row(strategy, rows)
            by_model_rows.append({"agent_name": agent, "model": model, **item})
    return {
        "overall": {"columns": RESULT_TABLE_COLUMNS, "rows": overall_rows},
        "by_model": {"columns": MODEL_RESULT_TABLE_COLUMNS, "rows": by_model_rows},
    }


def _paginate(rows: list[dict[str, Any]], page: int, page_size: int) -> dict[str, Any]:
    total_rows = len(rows)
    total_pages = max(1, (total_rows + page_size - 1) // page_size)
    current = min(max(1, page), total_pages)
    start = (current - 1) * page_size
    return {
        "rows": rows[start : start + page_size],
        "page": current,
        "page_size": page_size,
        "total_rows": total_rows,
        "total_pages": total_pages,
    }


def build_rq1_preview(
    snapshot: RQ1Snapshot,
    *,
    paired_page: int = 1,
    details_page: int = 1,
    page_size: int = 50,
) -> dict[str, Any]:
    if page_size not in {25, 50, 100}:
        raise ValueError("page_size must be one of 25, 50, or 100")
    primary = _primary_scope_rows(snapshot.summary_rows)
    primary_source = primary[0] if primary else {}
    return {
        "source_revision": snapshot.source_revision,
        "generated_at": snapshot.generated_at,
        "source_files": snapshot.source_files,
        "source_rows": snapshot.source_rows,
        "duplicate_rows": snapshot.duplicate_rows,
        "readiness": _readiness(primary, len(snapshot.detail_rows)),
        "primary_scope": {
            "scope": primary_source.get("scope", "overall"),
            "agent_name": primary_source.get("agent_name", "ALL"),
            "model": primary_source.get("model", "ALL"),
        },
        "strategies": _strategy_rows(primary),
        "result_tables": _result_tables(snapshot.paired_rows),
        "comparisons": _comparison_rows(primary),
        "conclusion": {
            "code": primary_source.get("rq1_conclusion", "INSUFFICIENT_DATA"),
            "text": primary_source.get(
                "rq1_answer_vi",
                "CHƯA ĐỦ DỮ LIỆU: cần chạy đủ ba prompt trên cùng sample và model để trả lời RQ1.",
            ),
        },
        "summary": {"columns": RQ1_SUMMARY_COLUMNS, "rows": snapshot.summary_rows},
        "paired": {"columns": RQ1_PAIRED_COLUMNS, **_paginate(snapshot.paired_rows, paired_page, page_size)},
        "details": {"columns": RQ1_DETAILS_COLUMNS, **_paginate(snapshot.detail_rows, details_page, page_size)},
    }


def _formats(workbook: xlsxwriter.Workbook) -> dict[str, Any]:
    return {
        "title": workbook.add_format({"bold": True, "font_size": 18, "font_color": "#17202A"}),
        "section": workbook.add_format({"bold": True, "font_size": 12, "font_color": "#FFFFFF", "bg_color": "#2563EB"}),
        "header": workbook.add_format({"bold": True, "font_color": "#FFFFFF", "bg_color": "#1F2937", "text_wrap": True, "valign": "vcenter"}),
        "label": workbook.add_format({"bold": True, "font_color": "#374151", "bg_color": "#F3F4F6"}),
        "warning": workbook.add_format({"bold": True, "font_color": "#92400E", "bg_color": "#FEF3C7", "text_wrap": True}),
        "ready": workbook.add_format({"bold": True, "font_color": "#065F46", "bg_color": "#D1FAE5"}),
        "integer": workbook.add_format({"num_format": "#,##0"}),
        "decimal": workbook.add_format({"num_format": "0.00"}),
        "p_value": workbook.add_format({"num_format": "0.0000"}),
        "datetime": workbook.add_format({"num_format": "yyyy-mm-dd hh:mm:ss"}),
        "wrap": workbook.add_format({"text_wrap": True, "valign": "top"}),
    }


def _write_typed(
    worksheet: Any,
    row: int,
    column: int,
    key: str,
    value: Any,
    formats: dict[str, Any],
) -> None:
    if value is None or value == "":
        worksheet.write_blank(row, column, None)
        return
    if isinstance(value, bool):
        worksheet.write_boolean(row, column, value)
        return
    if isinstance(value, (int, float)):
        number_format = formats["p_value"] if "p_value" in key else formats["decimal"] if any(token in key for token in ("pct", "pp", "score", "coverage", "elapsed")) else formats["integer"]
        worksheet.write_number(row, column, value, number_format)
        return
    if key in {"started_at", "finished_at"}:
        try:
            parsed = datetime.fromisoformat(str(value).replace("Z", "+00:00")).replace(tzinfo=None)
            worksheet.write_datetime(row, column, parsed, formats["datetime"])
            return
        except ValueError:
            pass
    if isinstance(value, (dict, list, tuple, set)):
        value = json.dumps(value, ensure_ascii=False, default=str, sort_keys=True)
    worksheet.write_string(row, column, str(value), formats["wrap"] if len(str(value)) > 80 else None)


def _column_width(column: str) -> int:
    if column in {"error", "test_smell_details", "rq1_answer_en", "rq1_answer_vi"}:
        return 46
    if column.endswith("_at") or "timestamp" in column:
        return 21
    if any(token in column for token in ("state", "status", "strategy", "prompt", "conclusion", "result")):
        return 25
    if column == "model":
        return 44
    if column in {"focal_class", "agent_name"}:
        return 28
    if column in {"run_id", "project_id", "input_id", "sample_id"}:
        return 20
    return min(24, max(12, len(column) + 2))


def _write_flat_sheet(
    worksheet: Any,
    columns: list[str],
    rows: list[dict[str, Any]],
    formats: dict[str, Any],
) -> None:
    worksheet.hide_gridlines(2)
    worksheet.freeze_panes(1, 2)
    worksheet.set_row(0, 34)
    for column_index, column in enumerate(columns):
        worksheet.write(0, column_index, column, formats["header"])
        worksheet.set_column(column_index, column_index, _column_width(column))
    for row_index, item in enumerate(rows, start=1):
        for column_index, column in enumerate(columns):
            _write_typed(worksheet, row_index, column_index, column, item.get(column, ""), formats)
    if rows:
        worksheet.autofilter(0, 0, len(rows), len(columns) - 1)
        status_columns = [index for index, name in enumerate(columns) if name.endswith("result") or name in {"complete_triplet", "data_ready"}]
        for column_index in status_columns:
            worksheet.conditional_format(1, column_index, len(rows), column_index, {"type": "text", "criteria": "containing", "value": "INSUFFICIENT", "format": formats["warning"]})


def _write_summary_sheet(
    worksheet: Any,
    snapshot: RQ1Snapshot,
    preview: dict[str, Any],
    formats: dict[str, Any],
) -> int:
    worksheet.hide_gridlines(2)
    worksheet.set_column("A:A", 32)
    worksheet.set_column("B:B", 34)
    worksheet.set_column("C:C", 46)
    worksheet.set_column("D:D", 26)
    worksheet.set_column("E:M", 22)
    worksheet.set_column("N:N", 30)
    row = 0
    worksheet.write(row, 0, "So sánh chiến lược prompt cho RQ1", formats["title"])
    row += 2
    metadata = (
        ("Thời điểm xuất (UTC)", datetime.now(timezone.utc).isoformat()),
        ("Revision nguồn", snapshot.source_revision),
        ("Phạm vi", "Tất cả dữ liệu RQ1"),
        ("Số file JSONL nguồn", snapshot.source_files),
        ("Số bản ghi RQ1", len(snapshot.detail_rows)),
    )
    for label, value in metadata:
        worksheet.write(row, 0, label, formats["label"])
        _write_typed(worksheet, row, 1, label.lower().replace(" ", "_"), value, formats)
        row += 1
    row += 1
    readiness = preview["readiness"]
    worksheet.write(row, 0, "Mức độ sẵn sàng dữ liệu", formats["section"])
    row += 1
    worksheet.write(row, 0, "Trạng thái", formats["label"])
    worksheet.write(row, 1, "SẴN SÀNG" if readiness["data_ready"] else "CHƯA SẴN SÀNG", formats["ready"] if readiness["data_ready"] else formats["warning"])
    row += 1
    for label, key in (
        ("Tổng số mẫu logical", "total_samples"),
        ("Bộ ba đầy đủ", "complete_triplets"),
        ("Bộ ba đủ tính biên dịch", "compile_evaluable_triplets"),
        ("Bộ ba đủ tính thực thi", "execution_evaluable_triplets"),
    ):
        worksheet.write(row, 0, label, formats["label"])
        worksheet.write_number(row, 1, readiness[key], formats["integer"])
        row += 1
    if readiness["warning"]:
        worksheet.write(row, 0, "Cảnh báo", formats["label"])
        worksheet.write(row, 1, readiness["warning"], formats["warning"])
        row += 1

    row += 1
    worksheet.write(row, 0, "Kết quả chiến lược chính", formats["section"])
    row += 1
    strategy_headers = ["Chiến lược", "Biên dịch thành công", "Mẫu đủ tính biên dịch", "Tỷ lệ biên dịch (%)", "Thực thi thành công", "Mẫu đủ tính thực thi", "Tỷ lệ thực thi (%)"]
    strategy_keys = ["strategy", "compile_success", "compile_evaluable", "compile_rate_pct", "execution_success", "execution_evaluable", "execution_rate_pct"]
    for column, header in enumerate(strategy_headers):
        worksheet.write(row, column, header, formats["header"])
    row += 1
    for item in preview["strategies"]:
        values = [item["label"], item["compile_success"], item["compile_evaluable"], item["compile_rate_pct"], item["execution_success"], item["execution_evaluable"], item["execution_rate_pct"]]
        for column, value in enumerate(values):
            _write_typed(worksheet, row, column, strategy_keys[column], value, formats)
        row += 1

    row += 1
    worksheet.write(row, 0, "So sánh thống kê", formats["section"])
    row += 1
    comparison_headers = ["So sánh", "Chỉ số", "Cặp mẫu", "Cải thiện (điểm %)", "Thắng", "Thua", "Hòa", "p-value", "p-value Holm", "Kết quả"]
    comparison_keys = ["comparison", "metric", "paired_samples", "improvement_pp", "wins", "losses", "ties", "p_value", "holm_p_value", "result"]
    for column, header in enumerate(comparison_headers):
        worksheet.write(row, column, header, formats["header"])
    row += 1
    for item in preview["comparisons"]:
        values = [item["comparison"], item["metric"], item["paired_samples"], item["improvement_pp"], item["wins"], item["losses"], item["ties"], item["p_value"], item["holm_p_value"], _vi_result(item["result"])]
        for column, value in enumerate(values):
            _write_typed(worksheet, row, column, comparison_keys[column], value, formats)
        row += 1

    row += 1
    worksheet.write(row, 0, "Kết luận", formats["section"])
    row += 1
    worksheet.write(row, 0, _vi_result(preview["conclusion"]["code"]), formats["label"])
    worksheet.write(row, 1, preview["conclusion"]["text"], formats["warning"] if not readiness["data_ready"] else formats["wrap"])
    row += 2

    worksheet.write(row, 0, "Kết quả theo mô hình và tổng thể", formats["section"])
    row += 1
    all_headers = ["Phạm vi", "Tác nhân", "Mô hình", "Chiến lược", "Tổng số mẫu", "Bộ ba đầy đủ", "Dữ liệu sẵn sàng", "Biên dịch thành công", "Mẫu đủ tính biên dịch", "Tỷ lệ biên dịch (%)", "Thực thi thành công", "Mẫu đủ tính thực thi", "Tỷ lệ thực thi (%)", "Kết luận"]
    all_keys = ["scope", "agent_name", "model", "strategy", "total_samples", "complete_triplets", "data_ready", "compile_success_count", "compile_paired_samples", "compile_success_rate_pct", "execution_success_count", "execution_paired_samples", "execution_success_rate_pct", "conclusion"]
    for column, header in enumerate(all_headers):
        worksheet.write(row, column, header, formats["header"])
    row += 1
    for item in snapshot.summary_rows:
        values = ["Tổng thể" if item.get("scope") == "overall" else item.get("scope"), item.get("agent_name"), item.get("model"), STRATEGY_LABELS.get(str(item.get("prompt_strategy")), item.get("prompt_strategy")), item.get("total_samples"), item.get("complete_triplets"), item.get("data_ready"), item.get("compile_success_count"), item.get("compile_paired_samples"), item.get("compile_success_rate_pct"), item.get("execution_success_count"), item.get("execution_paired_samples"), item.get("execution_success_rate_pct"), _vi_result(item.get("rq1_conclusion"))]
        for column, value in enumerate(values):
            _write_typed(worksheet, row, column, all_keys[column], value, formats)
        row += 1
    worksheet.freeze_panes(1, 0)
    return row


def write_rq1_workbook(path: Path, snapshot: RQ1Snapshot) -> dict[str, int]:
    if not snapshot.detail_rows:
        raise RQ1NoDataError("Chưa có bản ghi RQ1 để xuất")
    sheet_rows = {
        "Paired Samples": len(snapshot.paired_rows) + 1,
        "Raw Details": len(snapshot.detail_rows) + 1,
    }
    for sheet, rows in sheet_rows.items():
        if rows > EXCEL_MAX_ROWS:
            raise RQ1ExcelLimitError(sheet, rows, EXCEL_MAX_ROWS)
    path.parent.mkdir(parents=True, exist_ok=True)
    workbook = xlsxwriter.Workbook(str(path), {"constant_memory": True})
    try:
        formats = _formats(workbook)
        summary = workbook.add_worksheet("Summary")
        paired = workbook.add_worksheet("Paired Samples")
        details = workbook.add_worksheet("Raw Details")
        preview = build_rq1_preview(snapshot)
        summary_rows = _write_summary_sheet(summary, snapshot, preview, formats)
        if summary_rows > EXCEL_MAX_ROWS:
            raise RQ1ExcelLimitError("Summary", summary_rows, EXCEL_MAX_ROWS)
        _write_flat_sheet(paired, RQ1_PAIRED_COLUMNS, snapshot.paired_rows, formats)
        _write_flat_sheet(details, RQ1_DETAILS_COLUMNS, snapshot.detail_rows, formats)
    except Exception:
        workbook.close()
        raise
    workbook.close()
    return {"Summary": summary_rows, **sheet_rows}


def save_rq1_workbook(
    export_root: Path,
    project_root: Path,
    snapshot: RQ1Snapshot,
    preview_revision: str = "",
) -> dict[str, Any]:
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    export_root.mkdir(parents=True, exist_ok=True)
    destination = export_root / f"rq1_export_{timestamp}.xlsx"
    suffix = 2
    while destination.exists():
        destination = export_root / f"rq1_export_{timestamp}_{suffix}.xlsx"
        suffix += 1
    temporary = destination.with_suffix(".xlsx.tmp")
    try:
        row_counts = write_rq1_workbook(temporary, snapshot)
        os.replace(temporary, destination)
    finally:
        if temporary.exists():
            temporary.unlink()
    try:
        relative_path = destination.relative_to(project_root).as_posix()
    except ValueError:
        relative_path = str(destination)
    readiness = _readiness(_primary_scope_rows(snapshot.summary_rows), len(snapshot.detail_rows))
    return {
        "filename": destination.name,
        "path": str(destination),
        "relative_path": relative_path,
        "source_revision": snapshot.source_revision,
        "preview_was_stale": bool(preview_revision and preview_revision != snapshot.source_revision),
        "readiness": readiness,
        "warning": readiness["warning"],
        "rows": row_counts,
    }
