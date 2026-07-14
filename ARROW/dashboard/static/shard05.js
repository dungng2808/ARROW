const RQ1_PROMPTS = ["zero-shot", "few-shot", "zero-shot-project-aware"];
const RQ1_PROMPT_LABELS = {
  "zero-shot": "Zero-shot",
  "few-shot": "Few-shot",
  "zero-shot-project-aware": "Repository-aware",
};
const SHARD05_FILE = "repo_shard_05.txt";
const SHARD05_ID = "repo_shard_05";

const state = {
  config: null,
  runs: [],
  shard05: { summary: {}, projects: [] },
  selectedRunId: null,
  selectedProjectId: null,
  selectedPrompt: "",
  projectErrorContent: "",
  runLogContent: "",
  finishedRunIds: new Set(),
};

const $ = (selector) => document.querySelector(selector);

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function badge(text, kind = "idle") {
  return `<span class="badge ${kind}">${escapeHtml(text || "N/A")}</span>`;
}

function formatNumber(value) {
  const number = Number(value);
  return Number.isFinite(number) ? new Intl.NumberFormat("vi-VN").format(number) : "N/A";
}

function passKind(row) {
  if (row.__status === "NOT_RUN") return "idle";
  if (row.__status === "RUNNING") return "info";
  if (row.module_tests_passed || row.final_failure_state === "MODULE_TESTS_PASSED") return "pass";
  if (row.repair_status === "REPAIRED" || row.repair_status === "REGENERATED") return "warn";
  if (row.target_test_passed || row.final_failure_state === "TARGET_TEST_PASSED") return "info";
  return "fail";
}

function stateLabel(row) {
  return row.__status || row.final_failure_state || row.initial_failure_state || (row.test_passed ? "PASSED" : "FAILED");
}

function repairKind(status) {
  if (status === "NOT_NEEDED" || status === "REPAIRED") return "pass";
  if (status === "REGENERATED") return "warn";
  if (!status) return "idle";
  return "fail";
}

function experimentProjectId(row) {
  return String(row.project_id || row.Project_ID || row.__project_id || "").trim();
}

function experimentPrompt(row) {
  return String(row.generation_prompt_strategy || row.Prompt_Technique || row.__prompt || "").trim();
}

function experimentSample(row) {
  return String(row.sample_id || row.input_id || row.__sample_id || "").trim();
}

function experimentClass(row) {
  return String(row.focal_class || row.Class_Under_Test || row.__focal_class || "").trim();
}

function experimentAgent(row) {
  return String(row.agent_name || row["Generator(LLM)"] || "").trim();
}

function isExperimentPassed(row) {
  return row.module_tests_passed || row.test_passed || row.final_failure_state === "MODULE_TESTS_PASSED";
}

function shard05DisplayRows(projects, experiments) {
  const byProject = new Map();
  experiments.forEach((row) => {
    const projectId = experimentProjectId(row);
    if (!projectId) return;
    if (!byProject.has(projectId)) byProject.set(projectId, []);
    byProject.get(projectId).push(row);
  });
  return projects.flatMap((project) => {
    const rows = byProject.get(project.project_id) || [];
    if (rows.length) {
      return rows.map((row) => ({
        ...row,
        __project_id: project.project_id,
        __project_status: project.status,
        __sample_id: experimentSample(row) || project.sample_id || project.sample_file,
        __focal_class: experimentClass(row) || project.focal_class,
      }));
    }
    return [
      {
        __project_id: project.project_id,
        __project_status: project.status,
        __status: project.status === "RUNNING" ? "RUNNING" : "NOT_RUN",
        __sample_id: project.sample_id || project.sample_file,
        __focal_class: project.focal_class,
        agent_name: project.last_agent || "",
        generation_prompt_strategy: project.last_prompt || "",
      },
    ];
  });
}

async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(payload.error || response.statusText);
  }
  return payload;
}

function setActionStatus(message, kind = "") {
  const node = $("#runActionStatus");
  node.className = kind;
  node.textContent = message || "";
}

async function init() {
  state.config = await api("/api/config");
  renderConfig();
  bindEvents();
  await refreshAll();
  window.setInterval(() => refreshAll(false).catch((error) => console.error(error)), 3000);
  if (window.lucide) window.lucide.createIcons();
}

function bindEvents() {
  $("#shard05RunForm").addEventListener("submit", startShard05Run);
  $("#refreshShard05Btn").addEventListener("click", () => refreshAll(true));
  $("#stopShard05Btn").addEventListener("click", stopSelectedRun);
  $("#exportShard05MetricsBtn").addEventListener("click", exportShard05Metrics);
  $("#exportRq2Btn").addEventListener("click", exportRq2);
  $("#selectRq1PromptsBtn").addEventListener("click", selectRq1Prompts);
  $("#copyProjectErrorsBtn").addEventListener("click", copyProjectErrors);
  $("#copyRunLogBtn").addEventListener("click", copyRunLog);
  $("#clearPromptsBtn").addEventListener("click", () => {
    document.querySelectorAll('#promptOptions input[type="checkbox"]').forEach((input) => {
      input.checked = false;
    });
  });
  $("#shard05Search").addEventListener("input", renderShard05);
  $("#shard05StatusFilter").addEventListener("change", renderShard05);
  $("#rerunMode").addEventListener("change", updateSourceRunAvailability);
}

function renderConfig() {
  const agents = state.config.agents || [];
  const prompts = state.config.generation_prompts || [];
  $("#agentOptions").innerHTML = agents
    .map(
      (agent, index) => `
        <label class="agent-option" title="${escapeHtml(agent.model || agent.name)}">
          <input type="checkbox" name="agent" value="${escapeHtml(agent.name)}" ${index === 0 ? "checked" : ""} />
          <span>${escapeHtml(agent.name)}</span>
        </label>
      `,
    )
    .join("");
  $("#promptOptions").innerHTML = prompts
    .map(
      (prompt, index) => `
        <label class="prompt-option">
          <input type="checkbox" name="generation_prompt" value="${escapeHtml(prompt.name)}" ${RQ1_PROMPTS.includes(prompt.name) || (!hasRq1Prompts(prompts) && index === 0) ? "checked" : ""} />
          <span>${escapeHtml(prompt.name)}</span>
        </label>
      `,
    )
    .join("");
  const retry = state.config.adaptive_repair || {};
  const build = state.config.build || {};
  const input = state.config.input || {};
  const javaHomes = build.java_homes || {};
  $("#inputMode").value = input.mode || "project";
  $("#samplesPerProject").value = input.samples_per_project ?? 1;
  $("#retryMode").value = retry.retry_mode || "bounded";
  $("#wallClock").value = retry.unlimited_max_wall_clock_minutes || 120;
  $("#javaDefault").value = build.java_default || "";
  $("#javaHomes").value = Object.entries(javaHomes)
    .map(([key, value]) => `${key}: ${value}`)
    .join("\n");
}

function hasRq1Prompts(prompts) {
  const names = new Set(prompts.map((prompt) => prompt.name));
  return RQ1_PROMPTS.some((name) => names.has(name));
}

function selectRq1Prompts() {
  document.querySelectorAll('#promptOptions input[type="checkbox"]').forEach((input) => {
    input.checked = RQ1_PROMPTS.includes(input.value);
  });
}

async function refreshAll(showStatus = false) {
  if (showStatus) setActionStatus("Đang tải lại dữ liệu...", "");
  await Promise.all([loadShard05Status(), loadRuns()]);
  await loadSelectedRunLog();
  if (showStatus) setActionStatus("Đã tải lại.", "success");
}

function setExportStatus(message, kind = "") {
  const node = $("#exportShard05MetricsStatus");
  node.className = `shard05-export-status ${kind}`.trim();
  node.innerHTML = message || "";
}

async function exportShard05Metrics() {
  const button = $("#exportShard05MetricsBtn");
  button.disabled = true;
  setExportStatus("Đang export chỉ số shard 05...", "");
  try {
    const result = await api("/api/reports/export/shard05", { method: "POST", body: "{}" });
    setExportStatus(
      `
        <div><strong>Đã export ${formatNumber(result.rows)} dòng chi tiết.</strong></div>
        <div>Raw chi tiết: <code>${escapeHtml(result.relative_path || result.path)}</code></div>
        <div>Mean trung bình: <code>${escapeHtml(result.mean_relative_path || result.mean_path || "N/A")}</code></div>
        <small>Raw có chỉ số riêng từng run; Mean chỉ gồm Generator(LLM), Prompt_Technique, compilation count/rate, JaCoCo, PIT và tsDetect mean.</small>
      `,
      "success",
    );
  } catch (error) {
    setExportStatus(escapeHtml(error.message || "Export shard 05 thất bại."), "error");
  } finally {
    button.disabled = false;
  }
}

async function exportRq2() {
  const button = $("#exportRq2Btn");
  button.disabled = true;
  const status = $("#exportRq2Status");
  status.className = "shard05-export-status";
  status.textContent = "Đang xuất bảng RQ2 Repair...";
  try {
    const onlyGeneratedFailures = $("#rq2GeneratedOnly").checked;
    const result = await api("/api/reports/export/rq2", {
      method: "POST",
      body: JSON.stringify({only_generated_failures: onlyGeneratedFailures}),
    });
    status.className = "shard05-export-status success";
    status.innerHTML = `
      <div><strong>Đã xuất ${formatNumber(result.rows)} dòng RQ2.</strong></div>
      <div>File trên server: <code>${escapeHtml(result.relative_path || result.path)}</code></div>
      <div>Đã chọn: <strong>${formatNumber(result.selected_experiments || 0)}</strong> / ${formatNumber(result.source_experiments || 0)} experiment · loại ${formatNumber(result.excluded_experiments || 0)}</div>
      <div>Chế độ: <code>${escapeHtml(result.filter_mode || "all")}</code></div>
      <div>Cơ chế có dữ liệu: <code>${escapeHtml((result.mechanisms || []).join(", ") || "N/A")}</code></div>
      <small>${escapeHtml(result.warning || "")}</small>
    `;
  } catch (error) {
    status.className = "shard05-export-status error";
    const message = error.message || "Không xuất được RQ2.";
    status.textContent = message === "Unknown CSV export type"
      ? "Backend dashboard chưa nạp API RQ2. Hãy dừng và khởi động lại dashboard rồi thử lại."
      : message;
  } finally {
    button.disabled = false;
  }
}

async function loadShard05Status() {
  try {
    const payload = await api("/api/shards/shard05/status");
    state.shard05 = payload || { summary: {}, projects: [] };
  } catch (error) {
    state.shard05 = { summary: {}, projects: [], error: error.message || "Không tải được shard 05" };
  }
  renderShard05();
}

function clearProjectErrorPanel(message = "Không còn error artifact cho prompt đang chọn.") {
  state.selectedProjectId = null;
  state.selectedPrompt = "";
  state.projectErrorContent = "";
  $("#projectErrorTitle").textContent = "Error của project";
  $("#projectErrorMeta").textContent = message;
  $("#projectErrorContent").textContent = "Chưa chọn project lỗi.";
  $("#copyProjectErrorsBtn").disabled = true;
}

function clearResolvedProjectErrorPanel(projects) {
  if (!state.selectedProjectId) return;
  const project = projects.find((item) => item.project_id === state.selectedProjectId);
  if (!project) {
    clearProjectErrorPanel("Project đang chọn không còn trong shard/status hiện tại.");
    return;
  }
  if (!state.selectedPrompt) {
    if (project.status !== "HAS_FAILURES") {
      clearProjectErrorPanel("Project đang chọn hiện không còn lỗi.");
    }
    return;
  }
  const prompt = project.prompt_statuses?.[state.selectedPrompt];
  if (!prompt || prompt.status !== "HAS_FAILURES") {
    const label = RQ1_PROMPT_LABELS[state.selectedPrompt] || state.selectedPrompt;
    clearProjectErrorPanel(`${label} hiện không còn lỗi trong dữ liệu mới nhất.`);
  }
}

function shard05Kind(status) {
  if (status === "DONE") return "pass";
  if (status === "RUNNING") return "info";
  if (status === "HAS_FAILURES") return "warn";
  return "idle";
}

function shard05Label(status) {
  if (status === "DONE") return "Đã chạy";
  if (status === "RUNNING") return "Đang chạy";
  if (status === "HAS_FAILURES") return "Có lỗi";
  if (status === "NOT_RUN") return "Chưa chạy";
  return status || "N/A";
}

function promptCell(project, promptStrategy) {
  const prompt = project.prompt_statuses?.[promptStrategy] || {
    status: "NOT_RUN",
    total: 0,
    passed: 0,
    failed: 0,
  };
  const selected = state.selectedProjectId === project.project_id && state.selectedPrompt === promptStrategy ? " selected" : "";
  const counts = `${formatNumber(prompt.passed || 0)}/${formatNumber(prompt.total || 0)}`;
  const failedText = Number(prompt.failed || 0) ? ` · lỗi ${formatNumber(prompt.failed)}` : "";
  const actions =
    prompt.status === "HAS_FAILURES"
      ? `<div class="prompt-actions">
          <button class="mini-secondary view-project-errors" type="button" data-project="${escapeHtml(project.project_id)}" data-prompt="${escapeHtml(promptStrategy)}">Xem lỗi</button>
          <button class="danger-secondary rerun-project" type="button" data-project="${escapeHtml(project.project_id)}" data-prompt="${escapeHtml(promptStrategy)}">Chạy lại</button>
        </div>`
      : "";
  return `
    <div class="prompt-cell${selected}">
      ${badge(shard05Label(prompt.status), shard05Kind(prompt.status))}
      <small>${escapeHtml(counts + failedText)}</small>
      ${actions}
    </div>
  `;
}

function renderShard05() {
  const summary = state.shard05.summary || {};
  const projects = state.shard05.projects || [];
  const experiments = state.shard05.experiments || [];
  if (state.shard05.error) {
    $("#shard05LockedMeta").textContent = state.shard05.error;
    $("#shard05Meta").textContent = state.shard05.error;
    $("#shard05Summary").innerHTML = "";
    $("#shard05Rows").innerHTML = `<tr><td colspan="11">${escapeHtml(state.shard05.error)}</td></tr>`;
    return;
  }
  $("#shard05LockedMeta").textContent = `${formatNumber(summary.total_projects || projects.length)} project trong shard`;
  $("#shard05Meta").textContent = `${formatNumber(summary.total_projects || projects.length)} project · ${formatNumber(
    summary.experiments_completed || 0,
  )} lượt chạy đã ghi nhận`;
  clearResolvedProjectErrorPanel(projects);
  const cards = [
    ["Tổng project", summary.total_projects ?? projects.length, "idle"],
    ["Chưa chạy", summary.not_run || 0, "idle"],
    ["Đang chạy", summary.running || 0, "info"],
    ["Có lỗi", summary.has_failures || 0, "warn"],
    ["Đã chạy", summary.done || 0, "pass"],
    ["Tổng lỗi", summary.failed_experiments || 0, "fail"],
  ];
  $("#shard05Summary").innerHTML = cards
    .map(
      ([label, value, kind]) => `
        <div class="shard05-card ${kind}">
          <span>${escapeHtml(label)}</span>
          <strong>${formatNumber(value)}</strong>
        </div>
      `,
    )
    .join("");

  const query = ($("#shard05Search").value || "").trim().toLowerCase();
  const filter = $("#shard05StatusFilter").value;
  const rows = shard05DisplayRows(projects, experiments).filter((row) => {
    const projectStatus = row.__project_status || row.__status || "";
    if (filter === "NOT_RUN" && row.__status !== "NOT_RUN") return false;
    if (filter === "RUNNING" && projectStatus !== "RUNNING") return false;
    if (filter === "HAS_FAILURES" && (row.__status === "NOT_RUN" || row.__status === "RUNNING" || isExperimentPassed(row))) return false;
    if (filter === "DONE" && !isExperimentPassed(row)) return false;
    if (!query) return true;
    const haystack = [
      experimentProjectId(row),
      experimentSample(row),
      experimentClass(row),
      row.agent_name || row["Generator(LLM)"],
      experimentPrompt(row),
      row.repair_status,
      stateLabel(row),
      projectStatus,
    ]
      .join(" ")
      .toLowerCase();
    return haystack.includes(query);
  });
  $("#shard05Rows").innerHTML = rows.length
    ? rows
        .map((row) => {
          const coverage = row.coverage_line || row["Line_Coverage%"] || "";
          const mutation = row.mutation_score || row["Mutation_Score%"] || "";
          const projectId = experimentProjectId(row);
          const prompt = experimentPrompt(row);
          const agent = experimentAgent(row);
          const failed = row.__status !== "NOT_RUN" && row.__status !== "RUNNING" && !isExperimentPassed(row);
          const action = failed && prompt
            ? `<button class="danger-secondary mini-row-action rerun-row" type="button" data-project="${escapeHtml(projectId)}" data-prompt="${escapeHtml(prompt)}" data-agent="${escapeHtml(agent)}">Chạy lại</button>`
            : "";
          return `
            <tr
              data-project="${escapeHtml(projectId)}"
              data-prompt="${escapeHtml(prompt)}"
              class="${state.selectedProjectId === projectId && (!state.selectedPrompt || state.selectedPrompt === prompt) ? "selected" : ""}${failed ? " clickable" : ""}"
              title="${failed ? "Click để xem error artifact" : ""}"
            >
              <td>${escapeHtml(experimentSample(row))}</td>
              <td>${escapeHtml(experimentClass(row))}</td>
              <td>${escapeHtml(row.agent_name || row["Generator(LLM)"] || "")}</td>
              <td>${escapeHtml(prompt)}</td>
              <td>${row.llm_total_tokens ? escapeHtml(formatNumber(row.llm_total_tokens)) : ""}</td>
              <td>${badge(stateLabel(row), passKind(row))}</td>
              <td>${badge(row.repair_status || "", repairKind(row.repair_status))}</td>
              <td>${escapeHtml(coverage ? `${coverage}%` : "")}</td>
              <td>${escapeHtml(mutation ? `${mutation}%` : "")}</td>
              <td>${escapeHtml(row.elapsed_seconds || "")}</td>
              <td>${action}</td>
            </tr>
          `;
        })
        .join("")
    : `<tr><td colspan="11">Không có experiment nào khớp bộ lọc hiện tại.</td></tr>`;
  document.querySelectorAll("#shard05Rows .rerun-row").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation();
      rerunProject(button.dataset.project || "", button.dataset.prompt || "", button.dataset.agent || "");
    });
  });
  document.querySelectorAll("#shard05Rows tr[data-project]").forEach((row) => {
    row.addEventListener("click", () => {
      const item = rows.find((candidate) => experimentProjectId(candidate) === row.dataset.project && experimentPrompt(candidate) === (row.dataset.prompt || ""));
      if (item && item.__status !== "NOT_RUN" && item.__status !== "RUNNING" && !isExperimentPassed(item)) {
        loadProjectErrors(row.dataset.project, row.dataset.prompt || "");
      }
    });
  });
}

async function loadRuns() {
  const payload = await api("/api/runs");
  state.runs = (payload.runs || []).filter((run) => {
    const request = run.request || {};
    return request.run_scope === "shard" && shardName(request.repo_shard) === SHARD05_FILE;
  });
  const running = state.runs.find((run) => run.status === "running" || run.status === "stopping");
  if (!state.selectedRunId || !state.runs.some((run) => run.id === state.selectedRunId)) {
    state.selectedRunId = (running || state.runs.at(-1) || {}).id || null;
  }
  const newlyFinished = state.runs.some((run) => {
    const done = ["completed", "failed", "stopped"].includes(run.status);
    if (!done || state.finishedRunIds.has(run.id)) return false;
    state.finishedRunIds.add(run.id);
    return true;
  });
  if (newlyFinished) {
    await loadShard05Status();
  }
  renderRuns();
  renderSourceRunOptions();
}

function shardName(value) {
  return String(value || "").split(/[\\/]/).pop();
}

function renderRuns() {
  $("#runCount").textContent = `${formatNumber(state.runs.length)} run`;
  $("#stopShard05Btn").disabled = !state.selectedRunId || !["running", "stopping"].includes(selectedRun()?.status);
  $("#runList").innerHTML = state.runs.length
    ? state.runs
        .slice()
        .reverse()
        .map((run) => {
          const selected = run.id === state.selectedRunId ? "selected" : "";
          const kind = run.status === "completed" ? "pass" : run.status === "running" || run.status === "stopping" ? "info" : "fail";
          const projects = run.project_logs || [];
          const completed = projects.filter((project) => project.status === "completed").length;
          const meta = projects.length ? `${projects.length} project đã thấy · ${completed} hoàn tất` : "Chưa có project log";
          return `
            <div class="run-item ${selected}" data-run="${escapeHtml(run.id)}">
              <strong>${escapeHtml(run.id)}</strong>
              <small>${escapeHtml(meta)}</small>
              <div class="run-actions">${badge(run.status, kind)}</div>
            </div>
          `;
        })
        .join("")
    : `<div class="empty-state">Chưa có run nào cho shard 05.</div>`;
  document.querySelectorAll(".run-item").forEach((item) => {
    item.addEventListener("click", async () => {
      state.selectedRunId = item.dataset.run;
      renderRuns();
      await loadSelectedRunLog();
    });
  });
  if (window.lucide) window.lucide.createIcons();
}

function selectedRun() {
  return state.runs.find((run) => run.id === state.selectedRunId);
}

function renderSourceRunOptions() {
  const options = state.runs
    .filter((run) => !["running", "stopping"].includes(run.status))
    .slice()
    .reverse();
  $("#sourceRunId").innerHTML = options.length
    ? options.map((run) => `<option value="${escapeHtml(run.id)}">${escapeHtml(run.id)} · ${escapeHtml(run.status)}</option>`).join("")
    : `<option value="">Chưa có run cũ</option>`;
  updateSourceRunAvailability();
}

function updateSourceRunAvailability() {
  const mode = $("#rerunMode").value;
  const needsSource = ["failed_only", "resume", "failed_then_resume"].includes(mode);
  $("#sourceRunId").disabled = !needsSource;
}

function selectedAgents() {
  return Array.from(document.querySelectorAll('#agentOptions input[type="checkbox"]:checked')).map((input) => input.value);
}

function selectedPrompts() {
  return Array.from(document.querySelectorAll('#promptOptions input[type="checkbox"]:checked')).map((input) => input.value);
}

function currentRunOptions() {
  return {
    input_mode: $("#inputMode").value,
    samples_per_project: $("#samplesPerProject").value,
    agents: selectedAgents(),
    generation_prompts: selectedPrompts(),
    retry_mode: $("#retryMode").value,
    unlimited_max_wall_clock_minutes: Number($("#wallClock").value || 120),
    java_default: $("#javaDefault").value,
    java_home: $("#javaHome").value,
    java_homes: $("#javaHomes").value,
    skip_metrics: $("#skipMetrics").checked,
    keep_workspace: $("#keepWorkspace").checked,
    keep_repo_cache: $("#keepRepo").checked,
    mock_llm_smoke: $("#mockSmoke").checked,
  };
}

function validateModelAndPrompt(options) {
  if (!options.agents.length) {
    setActionStatus("Bạn cần chọn ít nhất 1 model.", "error");
    return false;
  }
  if (!options.generation_prompts.length) {
    setActionStatus("Bạn cần chọn ít nhất 1 prompt.", "error");
    return false;
  }
  return true;
}

async function loadProjectErrors(projectId, promptStrategy = "") {
  if (!projectId) return;
  state.selectedProjectId = projectId;
  state.selectedPrompt = promptStrategy || "";
  state.projectErrorContent = "";
  const promptLabel = promptStrategy ? ` · ${RQ1_PROMPT_LABELS[promptStrategy] || promptStrategy}` : " · tất cả prompt";
  $("#projectErrorTitle").textContent = `Error của project ${projectId}${promptLabel}`;
  $("#projectErrorMeta").textContent = "Đang tải error artifact...";
  $("#projectErrorContent").textContent = "Đang tải...";
  $("#copyProjectErrorsBtn").disabled = true;
  renderShard05();
  try {
    const query = promptStrategy ? `?prompt=${encodeURIComponent(promptStrategy)}` : "";
    const payload = await api(`/api/shards/shard05/projects/${encodeURIComponent(projectId)}/errors${query}`);
    state.projectErrorContent = payload.content || "";
    $("#projectErrorMeta").textContent = `${formatNumber(payload.failed_experiments || 0)} experiment lỗi · ${
      payload.experiments?.length || 0
    } bản ghi`;
    $("#projectErrorContent").textContent = state.projectErrorContent || "Không tìm thấy error artifact cho project này.";
    $("#copyProjectErrorsBtn").disabled = !state.projectErrorContent;
  } catch (error) {
    $("#projectErrorMeta").textContent = error.message || "Không tải được error.";
    $("#projectErrorContent").textContent = error.message || "Không tải được error.";
  }
}

async function rerunProject(projectId, promptStrategy = "", agentName = "") {
  if (!projectId) return;
  const options = currentRunOptions();
  if (promptStrategy) {
    options.generation_prompts = [promptStrategy];
  }
  if (agentName) {
    options.agents = [agentName];
  }
  if (!validateModelAndPrompt(options)) return;
  const promptLabel = promptStrategy ? ` (${RQ1_PROMPT_LABELS[promptStrategy] || promptStrategy})` : "";
  setActionStatus(`Đang chạy lại riêng project ${projectId}${promptLabel}...`, "");
  try {
    const run = await api(`/api/shards/shard05/projects/${encodeURIComponent(projectId)}/rerun`, {
      method: "POST",
      body: JSON.stringify(options),
    });
    state.selectedRunId = run.id;
    setActionStatus(`Đã bắt đầu chạy lại project ${projectId}${promptLabel}: ${run.id}.`, "success");
    await refreshAll(false);
  } catch (error) {
    setActionStatus(error.message || `Không chạy lại được project ${projectId}.`, "error");
  }
}

function fallbackCopy(text) {
  const textarea = document.createElement("textarea");
  textarea.value = text;
  textarea.setAttribute("readonly", "");
  textarea.style.position = "fixed";
  textarea.style.left = "-9999px";
  document.body.appendChild(textarea);
  textarea.select();
  document.execCommand("copy");
  textarea.remove();
}

async function copyText(text) {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
  } else {
    fallbackCopy(text);
  }
}

async function copyProjectErrors() {
  if (!state.projectErrorContent.trim()) return;
  try {
    await copyText(state.projectErrorContent);
    $("#copyProjectErrorStatus").textContent = "Đã copy";
  } catch (_error) {
    fallbackCopy(state.projectErrorContent);
    $("#copyProjectErrorStatus").textContent = "Đã copy";
  }
  window.clearTimeout(copyProjectErrors.timer);
  copyProjectErrors.timer = window.setTimeout(() => {
    $("#copyProjectErrorStatus").textContent = "";
  }, 1800);
}

async function copyRunLog() {
  const content = state.runLogContent || $("#runLog").textContent || "";
  if (!content.trim()) return;
  try {
    await copyText(content);
    $("#copyRunLogStatus").textContent = "Đã copy";
  } catch (_error) {
    fallbackCopy(content);
    $("#copyRunLogStatus").textContent = "Đã copy";
  }
  window.clearTimeout(copyRunLog.timer);
  copyRunLog.timer = window.setTimeout(() => {
    $("#copyRunLogStatus").textContent = "";
  }, 1800);
}

async function startShard05Run(event) {
  event.preventDefault();
  const options = currentRunOptions();
  if (!validateModelAndPrompt(options)) return;
  const rerunMode = $("#rerunMode").value;
  const needsSource = ["failed_only", "resume", "failed_then_resume"].includes(rerunMode);
  if (needsSource && !$("#sourceRunId").value) {
    setActionStatus("Chế độ này cần chọn run nguồn.", "error");
    return;
  }
  const payload = {
    run_scope: "shard",
    repo_shard: SHARD05_FILE,
    shard_id: SHARD05_ID,
    input_mode: $("#inputMode").value,
    samples_per_project: $("#samplesPerProject").value,
    start_index: Number($("#startIndex").value || 0),
    limit: Number($("#limit").value || 0),
    rerun_mode: rerunMode,
    source_run_id: needsSource ? $("#sourceRunId").value : "",
    ...options,
  };
  setActionStatus("Đang khởi động run shard 05...", "");
  $("#startShard05Btn").disabled = true;
  try {
    const run = await api("/api/runs", { method: "POST", body: JSON.stringify(payload) });
    state.selectedRunId = run.id;
    setActionStatus(`Đã bắt đầu run ${run.id}.`, "success");
    await refreshAll(false);
  } catch (error) {
    setActionStatus(error.message || "Không chạy được shard 05.", "error");
  } finally {
    $("#startShard05Btn").disabled = false;
  }
}

async function stopSelectedRun() {
  const run = selectedRun();
  if (!run || !["running", "stopping"].includes(run.status)) return;
  setActionStatus(`Đang dừng run ${run.id}...`, "");
  try {
    await api(`/api/runs/${encodeURIComponent(run.id)}/stop`, { method: "POST", body: "{}" });
    setActionStatus(`Đã gửi yêu cầu dừng run ${run.id}.`, "success");
    await refreshAll(false);
  } catch (error) {
    setActionStatus(error.message || "Không dừng được run.", "error");
  }
}

async function loadSelectedRunLog() {
  const run = selectedRun();
  if (!run) {
    $("#logTitle").textContent = "Log";
    state.runLogContent = "";
    $("#runLog").textContent = "Chưa chọn run.";
    $("#copyRunLogBtn").disabled = true;
    $("#copyRunLogStatus").textContent = "";
    return;
  }
  const payload = await api(`/api/runs/${encodeURIComponent(run.id)}/logs`);
  $("#logTitle").textContent = `Log ${run.id}`;
  state.runLogContent = payload.logs || "";
  $("#runLog").textContent = state.runLogContent || "Run chưa có log.";
  $("#copyRunLogBtn").disabled = !state.runLogContent.trim();
  $("#copyRunLogStatus").textContent = "";
}

init().catch((error) => {
  console.error(error);
  setActionStatus(error.message || "Không mở được trang chạy shard 05.", "error");
});
