from src.rq2_export import RQ2_COLUMNS, build_rq2_rows


def test_build_rq2_rows_uses_initial_failure_denominator_and_excludes_infrastructure():
    rows = [
        {
            "initial_failure_state": "COMPILE_FAILED",
            "initial_failure_origin": "GENERATED_TEST",
            "final_failure_state": "MODULE_TESTS_PASSED",
            "module_tests_passed": True,
            "repair_attempts": 2,
            "repair_time_seconds": 10,
        },
        {
            "initial_failure_state": "ASSERTION_FAILED",
            "initial_failure_origin": "GENERATED_TEST",
            "final_failure_state": "ASSERTION_FAILED",
            "module_tests_passed": False,
            "repair_attempts": 4,
            "repair_time_seconds": 20,
        },
        {
            "initial_failure_state": "TOOL_ERROR",
            "initial_failure_origin": "INFRASTRUCTURE",
            "final_failure_state": "TOOL_ERROR",
            "repair_attempts": 0,
        },
    ]

    result = build_rq2_rows(rows)
    adaptive = next(row for row in result if row["Repair mechanism"] == "Adaptive Repair")
    assert adaptive["Initial failed tests"] == 2
    assert adaptive["Final compile, n (%)"] == "2 (100.00%)"
    assert adaptive["Final target pass, n (%)"] == "1 (50.00%)"
    assert adaptive["Repair success, n (%)"] == "1 (50.00%)"
    assert adaptive["Repair attempts, Median [IQR]"] == "3.00 [2.50–3.50]"
    assert adaptive["Repair time, Median [IQR]"] == "15.00 s [12.50–17.50 s]"


def test_build_rq2_rows_has_requested_columns():
    assert list(build_rq2_rows([])) == []
    assert RQ2_COLUMNS == [
        "Repair mechanism",
        "Initial failed tests",
        "Final compile, n (%)",
        "Final target pass, n (%)",
        "Repair success, n (%)",
        "Repair attempts, Median [IQR]",
        "Repair time, Median [IQR]",
    ]
