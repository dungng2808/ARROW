const state = {
  preview: null,
  pairedPage: 1,
  detailsPage: 1,
  pageSize: 50,
  activeTab: "summary",
  sidebarCollapsed: localStorage.getItem("arrow.sidebarCollapsed") === "true",
};

const $ = (selector) => document.querySelector(selector);
const NA_TEXT = "Không có";
const STRATEGY_LABELS = {
  "zero-shot": "Zero-shot",
  "few-shot": "Few-shot",
  "zero-shot-project-aware": "Repository-aware",
};
const STATUS_LABELS = {
  READY: "SẴN SÀNG",
  NOT_READY: "CHƯA SẴN SÀNG",
};
const RESULT_LABELS = {
  IMPROVED: "CẢI THIỆN",
  PARTIAL_IMPROVEMENT: "CẢI THIỆN MỘT PHẦN",
  WORSE: "KÉM HƠN",
  NO_SIGNIFICANT_DIFFERENCE: "KHÔNG KHÁC BIỆT CÓ Ý NGHĨA",
  NO_SIGNIFICANT_IMPROVEMENT: "CHƯA CÓ CẢI THIỆN CÓ Ý NGHĨA",
  NO_REPOSITORY_AWARE_IS_WORSE: "REPOSITORY-AWARE KÉM HƠN",
  YES_IMPROVES_COMPILE_AND_EXECUTION: "CÓ CẢI THIỆN",
  INSUFFICIENT_DATA: "CHƯA ĐỦ DỮ LIỆU",
};
const VALUE_LABELS = {
  true: "Có",
  false: "Không",
  overall: "Tổng thể",
  exact_binomial: "Nhị thức chính xác",
  normal_approximation: "Xấp xỉ chuẩn",
  COMPILE_FAILED: "Biên dịch lỗi",
  TEST_DISCOVERY_FAILED: "Không tìm thấy kiểm thử",
  RUNTIME_FAILED: "Lỗi runtime",
  ASSERTION_FAILED: "Assertion không đạt",
  TARGET_TEST_PASSED: "Kiểm thử mục tiêu đạt",
  MODULE_TESTS_FAILED: "Kiểm thử module không đạt",
  MODULE_TESTS_PASSED: "Kiểm thử module đạt",
  BUILD_TIMEOUT: "Timeout build",
  TIMEOUT: "Timeout",
  TOOL_ERROR: "Lỗi công cụ",
  UNKNOWN: "Không rõ",
  UNKNOWN_FAILED: "Lỗi không rõ",
  NOT_NEEDED: "Không cần sửa",
  REPAIRED: "Đã sửa",
  FAILED: "Thất bại",
  PASSED: "Đạt",
  STOPPED: "Đã dừng",
};
const COLUMN_LABELS = {
  scope: "Phạm vi",
  agent_name: "Tác nhân",
  model: "Mô hình",
  build_tools: "Công cụ build",
  total_samples: "Tổng số mẫu",
  zero_shot_available_samples: "Số mẫu có Zero-shot",
  few_shot_available_samples: "Số mẫu có Few-shot",
  repository_aware_available_samples: "Số mẫu có Repository-aware",
  complete_triplets: "Bộ ba đầy đủ",
  data_ready: "Dữ liệu sẵn sàng",
  prompt_strategy: "Chiến lược prompt",
  compile_paired_samples: "Cặp mẫu đủ điều kiện biên dịch",
  compile_success_count: "Biên dịch thành công",
  compile_success_rate_pct: "Tỷ lệ biên dịch thành công (%)",
  execution_paired_samples: "Cặp mẫu đủ điều kiện thực thi",
  execution_success_count: "Thực thi thành công",
  execution_success_rate_pct: "Tỷ lệ thực thi thành công (%)",
  alpha: "Alpha",
  rq1_conclusion: "Kết luận RQ1",
  rq1_answer_en: "Câu trả lời RQ1 (tương thích cũ)",
  rq1_answer_vi: "Câu trả lời RQ1",
  project_id: "Mã dự án",
  input_id: "Mã input",
  sample_id: "Mã sample",
  focal_class: "Lớp cần kiểm thử",
  complete_triplet: "Bộ ba đầy đủ",
  run_id: "Mã run",
  shard_id: "Mã shard",
  generation_prompt_strategy: "Chiến lược prompt",
  build_tool: "Công cụ build",
  initial_state: "Trạng thái ban đầu",
  initial_compile_success: "Biên dịch ban đầu thành công",
  initial_target_pass: "Kiểm thử mục tiêu ban đầu đạt",
  final_state: "Trạng thái cuối",
  final_compile_success: "Biên dịch cuối thành công",
  final_target_pass: "Kiểm thử mục tiêu cuối đạt",
  final_module_pass: "Module cuối đạt",
  repair_status: "Trạng thái sửa",
  repair_attempts: "Số lần sửa",
  regeneration_attempts: "Số lần sinh lại",
  total_llm_attempts: "Tổng lượt LLM",
  llm_input_tokens: "Token đầu vào",
  llm_output_tokens: "Token đầu ra",
  llm_total_tokens: "Tổng token",
  elapsed_seconds: "Thời gian chạy (giây)",
  coverage_branch: "Độ phủ nhánh",
  coverage_line: "Độ phủ dòng",
  coverage_method: "Độ phủ phương thức",
  mutation_score: "Điểm mutation",
  mutations_total: "Tổng mutation",
  mutations_killed: "Mutation bị kill",
  mutations_survived: "Mutation sống sót",
  test_smell_total: "Tổng mùi kiểm thử",
  test_smell_details: "Chi tiết mùi kiểm thử",
  initial_failure_origin: "Nguồn lỗi ban đầu",
  final_failure_origin: "Nguồn lỗi cuối",
  repair_stopped_reason: "Lý do dừng sửa",
  test_fail_reason: "Lý do kiểm thử không đạt",
  error: "Lỗi",
  started_at: "Bắt đầu lúc",
  finished_at: "Kết thúc lúc",
  is_latest_logical_result: "Là kết quả logical mới nhất",
};
const STRATEGY_PREFIX_LABELS = {
  zero_shot: "Zero-shot",
  few_shot: "Few-shot",
  repository_aware: "Repository-aware",
};
const PAIRED_METRIC_LABELS = {
  run_id: "Mã run",
  initial_state: "Trạng thái ban đầu",
  initial_compile_success: "Biên dịch ban đầu thành công",
  initial_target_pass: "Kiểm thử mục tiêu ban đầu đạt",
  final_state: "Trạng thái cuối",
  final_compile_success: "Biên dịch cuối thành công",
  final_target_pass: "Kiểm thử mục tiêu cuối đạt",
  final_module_pass: "Module cuối đạt",
  repair_status: "Trạng thái sửa",
  repair_attempts: "Số lần sửa",
  total_llm_attempts: "Tổng lượt LLM",
  elapsed_seconds: "Thời gian chạy (giây)",
  llm_total_tokens: "Tổng token",
};
const COMPARISON_PREFIX_LABELS = {
  repo_vs_zero_compile: "Repository-aware so với Zero-shot · Biên dịch",
  repo_vs_few_compile: "Repository-aware so với Few-shot · Biên dịch",
  repo_vs_zero_execution: "Repository-aware so với Zero-shot · Thực thi",
  repo_vs_few_execution: "Repository-aware so với Few-shot · Thực thi",
};
const COMPARISON_SUFFIX_LABELS = {
  improvement_pp: "Cải thiện (điểm %)",
  wins: "Thắng",
  losses: "Thua",
  ties: "Hòa",
  p_value: "p-value",
  p_method: "Phương pháp p-value",
  holm_p_value: "p-value Holm",
  result: "Kết quả",
};

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

async function api(path, options = {}) {
  const response = await fetch(path, {headers: {"Content-Type": "application/json"}, ...options});
  const payload = await response.json();
  if (!response.ok) throw new Error(payload.error || response.statusText);
  return payload;
}

function formatNumber(value) {
  const number = Number(value);
  return Number.isFinite(number) ? new Intl.NumberFormat("vi-VN").format(number) : NA_TEXT;
}

function formatDecimal(value, digits = 2) {
  if (value === "" || value == null) return NA_TEXT;
  const number = Number(value);
  return Number.isFinite(number) ? number.toLocaleString("vi-VN", {minimumFractionDigits: digits, maximumFractionDigits: digits}) : NA_TEXT;
}

function formatPercent(value) {
  const formatted = formatDecimal(value);
  return formatted === NA_TEXT ? formatted : `${formatted}%`;
}

function localizeStatus(value) {
  return STATUS_LABELS[value] || String(value || "").replaceAll("_", " ");
}

function localizeResult(value) {
  return RESULT_LABELS[value] || String(value || "").replaceAll("_", " ");
}

function localizeMetric(value) {
  return {Compilation: "Biên dịch", Execution: "Thực thi"}[value] || value;
}

function localizeColumn(column) {
  if (COLUMN_LABELS[column]) return COLUMN_LABELS[column];
  for (const [prefix, label] of Object.entries(STRATEGY_PREFIX_LABELS)) {
    if (column.startsWith(`${prefix}_`)) {
      const metric = column.slice(prefix.length + 1);
      return `${label} · ${PAIRED_METRIC_LABELS[metric] || metric.replaceAll("_", " ")}`;
    }
  }
  for (const [prefix, label] of Object.entries(COMPARISON_PREFIX_LABELS)) {
    if (column.startsWith(`${prefix}_`)) {
      const suffix = column.slice(prefix.length + 1);
      return `${label} · ${COMPARISON_SUFFIX_LABELS[suffix] || suffix.replaceAll("_", " ")}`;
    }
  }
  return column.replaceAll("_", " ");
}

function localizeCell(column, value) {
  if (value === "" || value == null) return NA_TEXT;
  if (typeof value === "boolean") return value ? "Có" : "Không";
  if (typeof value === "object") return JSON.stringify(value);
  const text = String(value);
  if (STRATEGY_LABELS[text]) return STRATEGY_LABELS[text];
  if (RESULT_LABELS[text]) return localizeResult(text);
  if (VALUE_LABELS[text]) return VALUE_LABELS[text];
  if (column === "metric") return localizeMetric(text);
  return text;
}

function resultKind(value) {
  if (value === "IMPROVED" || value === "YES_IMPROVES_COMPILE_AND_EXECUTION") return "pass";
  if (value === "PARTIAL_IMPROVEMENT" || value === "NO_SIGNIFICANT_DIFFERENCE" || value === "NO_SIGNIFICANT_IMPROVEMENT") return "warn";
  if (value === "INSUFFICIENT_DATA") return "idle";
  return "fail";
}

function applySidebarState() {
  document.body.classList.toggle("sidebar-collapsed", state.sidebarCollapsed);
  const button = $("#sidebarToggle");
  button.setAttribute("aria-expanded", String(!state.sidebarCollapsed));
  button.title = state.sidebarCollapsed ? "Hiện thanh bên" : "Ẩn thanh bên";
  button.innerHTML = `<i data-lucide="${state.sidebarCollapsed ? "panel-left-open" : "panel-left-close"}"></i>`;
  if (window.lucide) window.lucide.createIcons();
}

function toggleSidebar() {
  state.sidebarCollapsed = !state.sidebarCollapsed;
  localStorage.setItem("arrow.sidebarCollapsed", String(state.sidebarCollapsed));
  applySidebarState();
}

async function loadPreview() {
  const refresh = $("#refreshRq1Btn");
  const status = $("#rq1Status");
  refresh.disabled = true;
  refresh.classList.add("busy");
  status.className = "rq1-action-status";
  status.textContent = "Đang tải snapshot RQ1 mới nhất…";
  try {
    const query = new URLSearchParams({
      paired_page: state.pairedPage,
      details_page: state.detailsPage,
      page_size: state.pageSize,
      baseline_valid_only: $("#rq1BaselineValidOnly").checked ? "true" : "false",
    });
    state.preview = await api(`/api/reports/rq1/preview?${query}`);
    renderPreview();
    status.textContent = "";
  } catch (error) {
    status.className = "rq1-action-status error";
    status.textContent = error.message || "Không thể tải dữ liệu RQ1";
    $("#exportRq1Btn").disabled = true;
  } finally {
    refresh.disabled = false;
    refresh.classList.remove("busy");
    if (window.lucide) window.lucide.createIcons();
  }
}

function renderPreview() {
  const payload = state.preview;
  const generated = new Date(payload.generated_at);
  const selected = payload.selected_rows ?? payload.filter_source_rows ?? payload.details?.total_rows ?? 0;
  const source = payload.filter_source_rows ?? payload.source_rows ?? 0;
  const excluded = payload.excluded_rows ?? Math.max(0, source - selected);
  $("#snapshotMeta").textContent = `${formatNumber(payload.source_files)} file JSONL · ${formatNumber(payload.source_rows)} dòng nguồn · lọc ${payload.filter_mode || "all"} · chọn ${formatNumber(selected)} / ${formatNumber(source)} · loại ${formatNumber(excluded)} · snapshot ${Number.isNaN(generated.getTime()) ? payload.generated_at : generated.toLocaleString("vi-VN")}`;
  renderReadiness(payload.readiness);
  renderStrategies(payload.strategies, payload.primary_scope);
  renderResultTables(payload.result_tables || {});
  renderComparisons(payload.comparisons, payload.conclusion, payload.readiness);
  renderTable("summary", payload.summary);
  renderTable("paired", payload.paired);
  renderTable("details", payload.details);
  $("#pairedCount").textContent = `(${formatNumber(payload.paired.total_rows)})`;
  $("#detailsCount").textContent = `(${formatNumber(payload.details.total_rows)})`;
  renderPager("paired", payload.paired);
  renderPager("details", payload.details);
  $("#exportRq1Btn").disabled = payload.readiness.rq1_records === 0;
}

function renderReadiness(readiness) {
  const badge = $("#readinessBadge");
  badge.textContent = localizeStatus(readiness.status);
  badge.className = `readiness-badge ${readiness.data_ready ? "ready" : "not-ready"}`;
  const available = readiness.available_samples || {};
  const items = [
    ["Mẫu logic", readiness.total_samples],
    ["Mẫu có Zero-shot", available["zero-shot"]],
    ["Mẫu có Few-shot", available["few-shot"]],
    ["Mẫu có Repository-aware", available["zero-shot-project-aware"]],
    ["Bộ ba đầy đủ", readiness.complete_triplets],
    ["Bộ ba đủ tính biên dịch", readiness.compile_evaluable_triplets],
    ["Bộ ba đủ tính thực thi", readiness.execution_evaluable_triplets],
    ["Bản ghi RQ1", readiness.rq1_records],
  ];
  $("#readinessGrid").innerHTML = items.map(([label, value]) => `<div class="rq1-kpi"><span>${escapeHtml(label)}</span><strong>${escapeHtml(formatNumber(value))}</strong></div>`).join("");
  const warning = $("#readinessWarning");
  const filter = state.preview || {};
  const filterReasons = filter.excluded_reasons || {};
  const reasonText = [
    ["baseline_failed", "baseline không đạt"],
    ["infrastructure_error", "lỗi hạ tầng"],
    ["baseline_unknown", "baseline không xác định"],
  ]
    .filter(([key]) => Number(filterReasons[key] || 0) > 0)
    .map(([key, label]) => `${label}: ${formatNumber(filterReasons[key])}`)
    .join(", ");
  const filterText = Number(filter.excluded_rows || 0) > 0
    ? `Đã loại ${formatNumber(filter.excluded_rows)} bản ghi theo bộ lọc${reasonText ? ` (${reasonText})` : ""}.`
    : "";
  if (readiness.warning || filterText) {
    const missing = readiness.missing_strategies?.length ? ` Thiếu dữ liệu cho: ${readiness.missing_strategies.join(", ")}.` : "";
    warning.textContent = [readiness.warning ? `${readiness.warning}.` : "", filterText, missing].filter(Boolean).join(" ");
    warning.classList.remove("hidden");
  } else {
    warning.textContent = "";
    warning.classList.add("hidden");
  }
}

function renderStrategies(strategies, scope) {
  $("#primaryScopeMeta").textContent = scope.scope === "overall" ? "Tổng thể · tất cả tác nhân và mô hình" : `${scope.agent_name} · ${scope.model}`;
  $("#strategyCards").innerHTML = strategies.map((item) => `
    <article class="strategy-card strategy-${escapeHtml(item.strategy)}">
      <span>${escapeHtml(item.label)}</span>
      <div><small>Biên dịch thành công</small><strong>${escapeHtml(formatNumber(item.compile_success))} / ${escapeHtml(formatNumber(item.compile_evaluable))}</strong><b>${escapeHtml(formatPercent(item.compile_rate_pct))}</b></div>
      <div><small>Thực thi thành công</small><strong>${escapeHtml(formatNumber(item.execution_success))} / ${escapeHtml(formatNumber(item.execution_evaluable))}</strong><b>${escapeHtml(formatPercent(item.execution_rate_pct))}</b></div>
    </article>`).join("");
  $("#strategyBars").innerHTML = [["Biên dịch thành công", "compile_rate_pct"], ["Thực thi thành công", "execution_rate_pct"]].map(([label, key]) => `<div class="rq1-bar-group"><strong>${label}</strong>${strategies.map((item) => {
    const rate = Number(item[key]);
    const hasRate = item[key] !== "" && item[key] != null && Number.isFinite(rate);
    const width = hasRate ? Math.max(0, Math.min(100, rate)) : 0;
    return `<div class="rq1-bar-row"><span>${escapeHtml(item.label)}</span><div class="rq1-bar-track"><i class="strategy-bg-${escapeHtml(item.strategy)}" style="width:${width}%"></i></div><b>${hasRate ? rate.toLocaleString("vi-VN", {minimumFractionDigits: 2, maximumFractionDigits: 2}) + "%" : NA_TEXT}</b></div>`;
  }).join("")}</div>`).join("");
}

function formatCountRate(count, rate) {
  return `${formatNumber(count)} (${formatPercent(rate)})`;
}

function resultTableCells(row, includeModel) {
  const cells = [];
  if (includeModel) {
    cells.push(row.agent_name || "N/A", row.model || "N/A");
  }
  cells.push(
    row.prompt_strategy_label || row.prompt_strategy || "N/A",
    formatNumber(row.total_samples),
    formatCountRate(row.compiled_count, row.compiled_rate_pct),
    formatCountRate(row.executed_count, row.executed_rate_pct),
    formatCountRate(row.target_passed_count, row.target_passed_rate_pct),
  );
  return cells;
}

function renderResultTable(prefix, dataset, includeModel = false) {
  const head = $(`#${prefix}Head`);
  const body = $(`#${prefix}Body`);
  const columns = dataset?.columns || [];
  const rows = dataset?.rows || [];
  head.innerHTML = `<tr>${columns.map((column) => `<th>${escapeHtml(column)}</th>`).join("")}</tr>`;
  body.innerHTML = rows.length
    ? rows.map((row) => `<tr>${resultTableCells(row, includeModel).map((value) => `<td>${escapeHtml(value)}</td>`).join("")}</tr>`).join("")
    : `<tr><td colspan="${Math.max(1, columns.length)}" class="empty-table">Chưa có dữ liệu.</td></tr>`;
}

function renderResultTables(tables) {
  renderResultTable("overallResults", tables.overall, false);
  renderResultTable("modelResults", tables.by_model, true);
}

function renderComparisons(comparisons, conclusion, readiness) {
  $("#comparisonRows").innerHTML = comparisons.length ? comparisons.map((item) => `<tr><td>${escapeHtml(item.comparison)}</td><td>${escapeHtml(localizeMetric(item.metric))}</td><td>${escapeHtml(formatNumber(item.paired_samples))}</td><td>${escapeHtml(formatDecimal(item.improvement_pp))}</td><td>${escapeHtml(formatNumber(item.wins))}</td><td>${escapeHtml(formatNumber(item.losses))}</td><td>${escapeHtml(formatNumber(item.ties))}</td><td>${escapeHtml(formatDecimal(item.p_value, 4))}</td><td>${escapeHtml(formatDecimal(item.holm_p_value, 4))}</td><td><span class="badge ${resultKind(item.result)}">${escapeHtml(localizeResult(item.result))}</span></td></tr>`).join("") : `<tr><td colspan="10" class="empty-table">Chưa có so sánh hoàn chỉnh.</td></tr>`;
  const box = $("#rq1Conclusion");
  box.className = `rq1-conclusion ${resultKind(conclusion.code)} ${readiness.data_ready ? "" : "insufficient"}`;
  box.innerHTML = `<strong>${escapeHtml(localizeResult(conclusion.code))}</strong><span>${escapeHtml(conclusion.text)}</span>`;
}

function renderTable(kind, dataset) {
  const head = $(`#${kind}Head`);
  const body = $(`#${kind}Body`);
  head.innerHTML = `<tr>${dataset.columns.map((column) => `<th>${escapeHtml(localizeColumn(column))}</th>`).join("")}</tr>`;
  body.innerHTML = dataset.rows.length ? dataset.rows.map((row) => `<tr>${dataset.columns.map((column) => `<td title="${escapeHtml(formatCell(column, row[column], row))}">${escapeHtml(formatCell(column, row[column], row))}</td>`).join("")}</tr>`).join("") : `<tr><td colspan="${dataset.columns.length}" class="empty-table">Chưa có dữ liệu.</td></tr>`;
}

function formatCell(column, value, row = {}) {
  if (column === "rq1_answer_en" && row.rq1_answer_vi) return row.rq1_answer_vi;
  if (typeof value === "number") return Number.isInteger(value) ? formatNumber(value) : formatDecimal(value);
  return localizeCell(column, value);
}

function renderPager(kind, dataset) {
  const pager = $(`#${kind}Pager`);
  pager.innerHTML = `<span>Trang ${formatNumber(dataset.page)} / ${formatNumber(dataset.total_pages)} · ${formatNumber(dataset.total_rows)} dòng</span><div><button type="button" data-page-kind="${kind}" data-page="${dataset.page - 1}" ${dataset.page <= 1 ? "disabled" : ""}>Trước</button><button type="button" data-page-kind="${kind}" data-page="${dataset.page + 1}" ${dataset.page >= dataset.total_pages ? "disabled" : ""}>Sau</button></div>`;
  pager.querySelectorAll("button[data-page]").forEach((button) => button.addEventListener("click", async () => {
    if (kind === "paired") state.pairedPage = Number(button.dataset.page);
    else state.detailsPage = Number(button.dataset.page);
    await loadPreview();
  }));
}

function selectTab(tab) {
  state.activeTab = tab;
  document.querySelectorAll(".rq1-preview-tab").forEach((button) => button.classList.toggle("active", button.dataset.previewTab === tab));
  document.querySelectorAll(".rq1-preview-panel").forEach((panel) => panel.classList.toggle("active", panel.id === `${tab}Preview`));
}

async function exportRq1() {
  if (!state.preview) return;
  const button = $("#exportRq1Btn");
  const status = $("#rq1Status");
  button.disabled = true;
  button.classList.add("busy");
  status.className = "rq1-action-status";
  status.textContent = "Đang tạo workbook RQ1 mới nhất…";
  try {
    const result = await api("/api/reports/export/rq1", {
      method: "POST",
      body: JSON.stringify({
        preview_revision: state.preview.source_revision,
        baseline_valid_only: $("#rq1BaselineValidOnly").checked,
      }),
    });
    status.className = `rq1-action-status ${result.warning ? "warning" : "success"}`;
    const stale = result.preview_was_stale ? " Dữ liệu nguồn đã thay đổi sau khi xem trước, nên workbook dùng dữ liệu mới hơn." : "";
    const warning = result.warning ? ` ${result.warning}.` : "";
    status.textContent = `Đã lưu ${result.relative_path}. Đã chọn ${formatNumber(result.selected_rows || 0)} / ${formatNumber(result.filter_source_rows || 0)} bản ghi.${warning}${stale}`;
  } catch (error) {
    status.className = "rq1-action-status error";
    status.textContent = error.message || "Xuất dữ liệu thất bại";
  } finally {
    button.disabled = !state.preview || state.preview.readiness.rq1_records === 0;
    button.classList.remove("busy");
    if (window.lucide) window.lucide.createIcons();
  }
}

function bindEvents() {
  $("#sidebarToggle").addEventListener("click", toggleSidebar);
  $("#refreshRq1Btn").addEventListener("click", loadPreview);
  $("#exportRq1Btn").addEventListener("click", exportRq1);
  $("#rq1BaselineValidOnly").addEventListener("change", async () => {
    state.pairedPage = 1;
    state.detailsPage = 1;
    await loadPreview();
  });
  $("#pageSize").addEventListener("change", async () => {
    state.pageSize = Number($("#pageSize").value);
    state.pairedPage = 1;
    state.detailsPage = 1;
    await loadPreview();
  });
  document.querySelectorAll(".rq1-preview-tab").forEach((button) => button.addEventListener("click", () => selectTab(button.dataset.previewTab)));
}

applySidebarState();
bindEvents();
loadPreview();
if (window.lucide) window.lucide.createIcons();
