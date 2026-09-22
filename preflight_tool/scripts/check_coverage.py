"""Fail the Windows quality gate using separate line and branch thresholds."""
from __future__ import annotations

import argparse
import json
from pathlib import Path


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("coverage_json", type=Path)
    parser.add_argument("--lines", type=float, default=90.0)
    parser.add_argument("--branches", type=float, default=85.0)
    args = parser.parse_args()
    totals = json.loads(args.coverage_json.read_text(encoding="utf-8"))["totals"]
    line_rate = 100 * totals["covered_lines"] / max(1, totals["num_statements"])
    branch_rate = 100 * totals["covered_branches"] / max(1, totals["num_branches"])
    print(f"lines={line_rate:.2f}% branches={branch_rate:.2f}%")
    if line_rate < args.lines or branch_rate < args.branches:
        raise SystemExit("coverage gate failed")


if __name__ == "__main__":
    main()
