const state = {
  config: null,
  experiments: [],
  selectedId: null,
  selectedCheckpoint: null,
  checkpointPayload: null,
  currentTab: "decision",
  finishedRunIds: new Set(),
  selectedRunId: null,
  shards: [],
  sidebarCollapsed: localStorage.getItem("arrow.sidebarCollapsed") === "true",
};

const $ = (selector) => document.querySelector(selector);

function badge(text, kind = "idle") {
  return `<span class="badge ${kind}">${escapeHtml(text || "N/A")}</span>`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
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

function passKind(row) {
  if (row.module_tests_passed || row.final_failure_state === "MODULE_TESTS_PASSED") return "pass";
  if (row.repair_status === "REPAIRED" || row.repair_status === "REGENERATED") return "warn";
  if (row.target_test_passed || row.final_failure_state === "TARGET_TEST_PASSED") return "info";
  return "fail";
}

function stateLabel(row) {
  return row.final_failure_state || row.initial_failure_state || (row.test_passed ? "PASSED" : "FAILED");
}

function repairKind(status) {
  if (status === "NOT_NEEDED" || status === "REPAIRED") return "pass";
  if (status === "REGENERATED") return "warn";
  if (!status) return "idle";
  return "fail";
}

async function init() {
  applySidebarState();
  state.config = await api("/api/config");
  renderConfig();
  await loadProjects();
  await loadShards();
  await loadExperiments();
  await loadRuns();
  bindEvents();
  if (window.lucide) window.lucide.createIcons();
}

function bindEvents() {
  $("#refreshBtn").addEventListener("click", refreshAll);
  $("#sidebarToggle").addEventListener("click", toggleSidebar);
  $("#copyLogsBtn").addEventListener("click", copyRunLogs);
  $("#projectSelect").addEventListener("change", () => loadSamples($("#projectSelect").value));
  $("#runScope").addEventListener("change", toggleRunScope);
  $("#shardSelect").addEventListener("change", updateShardMeta);
  $("#runForm").addEventListener("submit", startRun);
  $("#searchBox").addEventListener("input", renderExperiments);
  $("#statusFilter").addEventListener("change", renderExperiments);
  document.querySelectorAll(".tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      document.querySelectorAll(".tab").forEach((item) => item.classList.remove("active"));
      tab.classList.add("active");
      state.currentTab = tab.dataset.tab;
      renderCheckpointContent();
    });
  });
  setInterval(loadRuns, 3000);
}

function toggleSidebar() {
  state.sidebarCollapsed = !state.sidebarCollapsed;
  localStorage.setItem("arrow.sidebarCollapsed", String(state.sidebarCollapsed));
  applySidebarState();
}

function applySidebarState() {
  document.body.classList.toggle("sidebar-collapsed", state.sidebarCollapsed);
  const button = $("#sidebarToggle");
  if (!button) return;
  button.setAttribute("aria-expanded", String(!state.sidebarCollapsed));
  button.title = state.sidebarCollapsed ? "Show sidebar" : "Hide sidebar";
  button.innerHTML = `<i data-lucide="${state.sidebarCollapsed ? "panel-left-open" : "panel-left-close"}"></i>`;
  if (window.lucide) window.lucide.createIcons();
}

async function copyRunLogs() {
  const text = $("#runLog").textContent || "";
  if (!text.trim()) {
    setCopyLogStatus("No logs");
    return;
  }
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text);
    } else {
      fallbackCopy(text);
    }
    setCopyLogStatus("Copied");
  } catch (_error) {
    fallbackCopy(text);
    setCopyLogStatus("Copied");
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

function setCopyLogStatus(message) {
  $("#copyLogStatus").textContent = message;
  window.clearTimeout(setCopyLogStatus.timer);
  setCopyLogStatus.timer = window.setTimeout(() => {
    $("#copyLogStatus").textContent = "";
  }, 1800);
}

async function refreshAll() {
  await loadExperiments();
  await loadRuns();
}

function renderConfig() {
  const agents = state.config.agents || [];
  const prompts = state.config.generation_prompts || [];
  $("#agentSelect").innerHTML = agents.map((agent) => `<option value="${escapeHtml(agent.name)}">${escapeHtml(agent.name)}</option>`).join("");
  $("#promptSelect").innerHTML = prompts.map((prompt) => `<option value="${escapeHtml(prompt.name)}">${escapeHtml(prompt.name)}</option>`).join("");
  const retry = state.config.adaptive_repair || {};
  $("#retryMode").value = retry.retry_mode || "bounded";
  $("#wallClock").value = retry.unlimited_max_wall_clock_minutes || 120;
  $("#maxAttemptsPerPrompt").value = retry.max_attempts_per_prompt ?? 2;
  $("#maxRepairAttempts").value = retry.max_repair_attempts ?? 6;
  $("#maxRegenerateAttempts").value = retry.max_regenerate_attempts ?? 1;
  $("#maxTotalLlmAttempts").value = retry.max_total_llm_attempts ?? 7;
  $("#noProgressPatience").value = retry.no_progress_patience ?? 2;
  $("#repeatedErrorPatience").value = retry.repeated_error_patience ?? 1;
  $("#maxBuildTimeoutRetries").value = retry.max_build_timeout_retries ?? 1;
  $("#maxToolErrorRetries").value = retry.max_tool_error_retries ?? 1;
  const build = state.config.build || {};
  const input = state.config.input || {};
  const run = state.config.run || {};
  const javaHomes = build.java_homes || {};
  $("#inputMode").value = input.mode || "project";
  $("#samplesPerProject").value = input.samples_per_project ?? 1;
  $("#startIndex").value = 0;
  $("#limit").value = 0;
  $("#shardId").value = run.shard_id && run.shard_id !== "local" ? run.shard_id : "person_00";
  $("#javaDefault").value = build.java_default || "";
  $("#javaHomes").value = Object.entries(javaHomes)
    .map(([key, value]) => `${key}: ${value}`)
    .join("\n");
  const java = state.config.java || {};
  const javaHome = java.java_home_detected || java.java_home_env || "";
  $("#javaHome").value = "";
  $("#javaMeta").textContent = javaHome ? `Detected default: ${javaHome}` : "Auto maps repo Java version to configured JDK";
  $("#configMeta").textContent = `${agents.length} agents, ${prompts.length} prompts`;
}

async function loadProjects() {
  const payload = await api("/api/projects");
  const projects = payload.projects || [];
  $("#projectSelect").innerHTML = projects.map((project) => `<option value="${escapeHtml(project.project_id)}">${escapeHtml(project.project_id)} (${project.sample_count})</option>`).join("");
  $("#projectMeta").textContent = `${payload.project_count ?? projects.length} projects`;
  if (projects.length) {
    await loadSamples(projects[0].project_id);
  }
}

async function loadShards() {
  const payload = await api("/api/shards");
  state.shards = payload.shards || [];
  $("#shardSelect").innerHTML = state.shards
    .map((shard) => `<option value="${escapeHtml(shard.name)}">${escapeHtml(shard.name)} (${shard.repo_count})</option>`)
    .join("");
  updateShardMeta();
  toggleRunScope();
}

function updateShardMeta() {
  const selected = state.shards.find((shard) => shard.name === $("#shardSelect").value);
  $("#shardMeta").textContent = selected ? `${selected.repo_count} repos` : "No shard files";
  if (selected && (!$("#shardId").value || $("#shardId").value === "person_00")) {
    $("#shardId").value = selected.name.replace(/\.txt$/i, "");
  }
}

function toggleRunScope() {
  const shardMode = $("#runScope").value === "shard";
  document.querySelectorAll(".single-field").forEach((item) => item.classList.toggle("hidden", shardMode));
  document.querySelectorAll(".shard-field").forEach((item) => item.classList.toggle("hidden", !shardMode));
}

async function loadSamples(projectId) {
  const payload = await api(`/api/projects/${encodeURIComponent(projectId)}/samples`);
  const samples = payload.samples || [];
  $("#sampleSelect").innerHTML = samples.map((sample) => `<option value="${escapeHtml(sample)}">${escapeHtml(sample)}</option>`).join("");
}

async function loadExperiments() {
  const payload = await api("/api/experiments");
  state.experiments = payload.experiments || [];
  $("#experimentCount").textContent = `${state.experiments.length} records`;
  renderExperiments();
  if (!state.selectedId && state.experiments.length) {
    selectExperiment(state.experiments[0].dashboard_id);
  }
}

function renderExperiments() {
  const query = $("#searchBox").value.toLowerCase();
  const filter = $("#statusFilter").value;
  const rows = state.experiments.filter((row) => {
    const haystack = [
      row.sample_id,
      row.project_id,
      row.focal_class,
      row.agent_name,
      row.generation_prompt_strategy,
      row.repair_status,
      stateLabel(row),
    ].join(" ").toLowerCase();
    if (query && !haystack.includes(query)) return false;
    if (filter === "passed" && !(row.module_tests_passed || row.test_passed)) return false;
    if (filter === "failed" && (row.module_tests_passed || row.test_passed)) return false;
    if (filter === "repaired" && !["REPAIRED", "REGENERATED"].includes(row.repair_status)) return false;
    return true;
  });
  $("#experimentRows").innerHTML = rows
    .map((row) => {
      const coverage = row.coverage_line || row["Line_Coverage%"] || "";
      const mutation = row.mutation_score || row["Mutation_Score%"] || "";
      return `
        <tr data-id="${escapeHtml(row.dashboard_id)}" class="${state.selectedId === row.dashboard_id ? "selected" : ""}">
          <td>${escapeHtml(row.sample_id || row.input_id)}</td>
          <td>${escapeHtml(row.focal_class || row.Class_Under_Test)}</td>
          <td>${escapeHtml(row.agent_name || row["Generator(LLM)"])}</td>
          <td>${escapeHtml(row.generation_prompt_strategy || row.Prompt_Technique)}</td>
          <td>${badge(stateLabel(row), passKind(row))}</td>
          <td>${badge(row.repair_status || "N/A", repairKind(row.repair_status))}</td>
          <td>${escapeHtml(coverage ? `${coverage}%` : "")}</td>
          <td>${escapeHtml(mutation ? `${mutation}%` : "")}</td>
          <td>${escapeHtml(row.elapsed_seconds || "")}</td>
        </tr>
      `;
    })
    .join("");
  document.querySelectorAll("#experimentRows tr").forEach((row) => row.addEventListener("click", () => selectExperiment(row.dataset.id)));
}

async function selectExperiment(id) {
  state.selectedId = id;
  state.selectedCheckpoint = null;
  state.checkpointPayload = null;
  renderExperiments();
  const payload = await api(`/api/experiments/${encodeURIComponent(id)}`);
  renderDetail(payload.experiment, payload.repair_summary || {}, payload.checkpoints || []);
}

function renderDetail(row, repair, checkpoints) {
  $("#detailTitle").textContent = row.focal_class || row.Class_Under_Test || "Experiment";
  $("#detailSubtitle").textContent = `${row.project_id || ""} / ${row.sample_id || ""}`;
  const summary = [
    ["Agent", row.agent_name || row["Generator(LLM)"]],
    ["Prompt", row.generation_prompt_strategy || row.Prompt_Technique],
    ["Build", row.build_tool],
    ["Module", row.module_path],
    ["Initial", row.initial_failure_state],
    ["Final", row.final_failure_state],
    ["Repair", row.repair_status],
    ["Attempts", repair.repair_attempts ?? row.repair_attempts ?? 0],
    ["Rollbacks", repair.rollback_count ?? row.rollback_count ?? 0],
    ["Prompt switches", repair.prompt_switch_count ?? row.prompt_switch_count ?? 0],
    ["Elapsed", row.elapsed_seconds],
    ["Workspace", row.workspace_deleted ? "deleted" : "available"],
  ];
  $("#summaryGrid").innerHTML = summary
    .map(([label, value]) => `<div class="summary-item"><span>${escapeHtml(label)}</span><strong title="${escapeHtml(value)}">${escapeHtml(value ?? "")}</strong></div>`)
    .join("");
  renderMetrics(row);
  renderTimeline(checkpoints);
}

function renderMetrics(row) {
  const metrics = [
    ["Line coverage", row.coverage_line || row["Line_Coverage%"]],
    ["Branch coverage", row.coverage_branch || row["Branch_Coverage%"]],
    ["Method coverage", row.coverage_method || row["Method_Coverage%"]],
    ["Mutation score", row.mutation_score || row["Mutation_Score%"]],
  ];
  $("#metricMeta").textContent = row.test_smell_total ? `${row.test_smell_total} smells` : "";
  $("#metricBars").innerHTML = metrics
    .map(([label, value]) => {
      const number = Number.parseFloat(value || "0");
      const width = Number.isFinite(number) ? Math.max(0, Math.min(100, number)) : 0;
      return `
        <div class="metric-row">
          <div class="metric-head"><span>${escapeHtml(label)}</span><strong>${value ? escapeHtml(value) + "%" : "N/A"}</strong></div>
          <div class="bar"><span style="width:${width}%"></span></div>
        </div>
      `;
    })
    .join("");
  renderMetricDetails(row);
}

function renderMetricDetails(row) {
  const coverageRows = [
    ["Branch_Coverage%", row["Branch_Coverage%"] || row.coverage_branch],
    ["Line_Coverage%", row["Line_Coverage%"] || row.coverage_line],
    ["Method_Coverage%", row["Method_Coverage%"] || row.coverage_method],
    ["coverage_error", row.coverage_error],
  ];
  const mutationRows = [
    ["Mutation_Score%", row["Mutation_Score%"] || row.mutation_score],
    ["mutations_total", row.mutations_total],
    ["mutations_killed", row.mutations_killed],
    ["mutations_survived", row.mutations_survived],
    ["mutation_error", row.mutation_error],
  ];
  const smellFields = [
    "test_smell_total",
    "Assertion Roulette",
    "Conditional Test Logic",
    "Constructor Initialization",
    "Default Test",
    "EmptyTest",
    "Exception Handling",
    "General Fixture",
    "Mystery Guest",
    "Print Statement",
    "Redundant Assertion",
    "Sensitive Equality",
    "Verbose Test",
    "Sleepy Test",
    "Eager Test",
    "Lazy Test",
    "Duplicate Assert",
    "Unknown Test",
    "IgnoredTest",
    "Resource Optimism",
    "Magic Number Test",
    "Dependent Test",
    "smell_error",
  ];
  const smellRows = smellFields.map((field) => [field, row[field]]);
  $("#metricDetails").innerHTML = [
    metricGroup("JaCoCo", coverageRows),
    metricGroup("PIT", mutationRows),
    metricGroup("tsDetect", smellRows),
  ].join("");
}

function metricGroup(title, rows) {
  return `
    <div class="metric-group">
      <h3>${escapeHtml(title)}</h3>
      <table class="metric-table"><tbody>
        ${rows
          .map(([key, value]) => `<tr><td>${escapeHtml(key)}</td><td>${escapeHtml(value === "" || value == null ? "N/A" : value)}</td></tr>`)
          .join("")}
      </tbody></table>
    </div>
  `;
}

function renderTimeline(checkpoints) {
  $("#checkpointCount").textContent = `${checkpoints.length} checkpoints`;
  if (!checkpoints.length) {
    $("#timeline").innerHTML = `<div class="summary-item"><span>Status</span><strong>No checkpoints</strong></div>`;
    $("#checkpointTitle").textContent = "";
    $("#checkpointContent").textContent = "";
    return;
  }
  $("#timeline").innerHTML = checkpoints
    .map((item) => {
      const selected = state.selectedCheckpoint === item.attempt ? "selected" : "";
      const flags = [item.rollback_performed ? "rollback" : "", item.prompt_switched ? "switch" : "", item.build_skipped ? "skip build" : ""]
        .filter(Boolean)
        .join(" · ");
      return `
        <div class="timeline-item ${selected}" data-attempt="${escapeHtml(item.attempt)}">
          <div class="timeline-index">${escapeHtml(item.attempt_number)}</div>
          <div class="timeline-main">
            <strong>${escapeHtml(item.previous_state || "START")} -> ${escapeHtml(item.new_state || item.decision)}</strong>
            <span>${escapeHtml(item.decision || "")}${flags ? " · " + escapeHtml(flags) : ""}</span>
          </div>
        </div>
      `;
    })
    .join("");
  document.querySelectorAll(".timeline-item").forEach((item) => item.addEventListener("click", () => selectCheckpoint(item.dataset.attempt)));
  if (!state.selectedCheckpoint) {
    selectCheckpoint(checkpoints[0].attempt);
  }
}

async function selectCheckpoint(attempt) {
  state.selectedCheckpoint = attempt;
  document.querySelectorAll(".timeline-item").forEach((item) => item.classList.toggle("selected", item.dataset.attempt === attempt));
  const payload = await api(`/api/experiments/${encodeURIComponent(state.selectedId)}/checkpoints/${encodeURIComponent(attempt)}`);
  state.checkpointPayload = payload;
  $("#checkpointTitle").textContent = attempt;
  renderCheckpointContent();
}

function renderCheckpointContent() {
  if (!state.checkpointPayload) {
    $("#checkpointContent").textContent = "";
    return;
  }
  const payload = state.checkpointPayload;
  if (state.currentTab === "decision") {
    $("#checkpointContent").textContent = JSON.stringify(payload.decision || {}, null, 2);
  } else if (state.currentTab === "build") {
    $("#checkpointContent").textContent = payload.build_output_after || payload.build_output_before || "";
  } else if (state.currentTab === "response") {
    $("#checkpointContent").textContent = payload.llm_response || payload.repair_prompt || "";
  } else {
    $("#checkpointContent").textContent = payload.generated_test_after || payload.generated_test_before || "";
  }
}

async function startRun(event) {
  event.preventDefault();
  const payload = {
    run_scope: $("#runScope").value,
    project_id: $("#projectSelect").value,
    sample_file: $("#sampleSelect").value,
    repo_shard: $("#shardSelect").value,
    shard_id: $("#shardId").value,
    input_mode: $("#inputMode").value,
    samples_per_project: $("#samplesPerProject").value,
    start_index: Number($("#startIndex").value || 0),
    limit: Number($("#limit").value || 0),
    agent: $("#agentSelect").value,
    generation_prompt: $("#promptSelect").value,
    retry_mode: $("#retryMode").value,
    unlimited_max_wall_clock_minutes: Number($("#wallClock").value || 120),
    max_attempts_per_prompt: Number($("#maxAttemptsPerPrompt").value || 2),
    max_repair_attempts: Number($("#maxRepairAttempts").value || 6),
    max_regenerate_attempts: Number($("#maxRegenerateAttempts").value || 1),
    max_total_llm_attempts: Number($("#maxTotalLlmAttempts").value || 7),
    no_progress_patience: Number($("#noProgressPatience").value || 2),
    repeated_error_patience: Number($("#repeatedErrorPatience").value || 1),
    max_build_timeout_retries: Number($("#maxBuildTimeoutRetries").value || 1),
    max_tool_error_retries: Number($("#maxToolErrorRetries").value || 1),
    java_default: $("#javaDefault").value,
    java_home: $("#javaHome").value,
    java_homes: $("#javaHomes").value,
    skip_metrics: $("#skipMetrics").checked,
    keep_workspace: $("#keepWorkspace").checked,
    keep_repo_cache: $("#keepRepo").checked,
    mock_llm_smoke: $("#mockSmoke").checked,
  };
  const run = await api("/api/runs", { method: "POST", body: JSON.stringify(payload) });
  state.selectedRunId = run.id;
  renderRunList([run]);
  await loadSelectedRunLog();
}

async function loadRuns() {
  const payload = await api("/api/runs");
  const runs = payload.runs || [];
  const newlyFinished = runs.some((run) => {
    const done = ["completed", "failed"].includes(run.status);
    if (!done || state.finishedRunIds.has(run.id)) return false;
    state.finishedRunIds.add(run.id);
    return true;
  });
  renderRunList(runs);
  if (newlyFinished) {
    await loadExperiments();
  }
  if (!state.selectedRunId || !runs.some((run) => run.id === state.selectedRunId)) {
    const running = runs.find((run) => run.status === "running");
    const newest = runs[runs.length - 1];
    state.selectedRunId = (running || newest || {}).id || null;
  }
  await loadSelectedRunLog();
}

async function loadSelectedRunLog() {
  if (!state.selectedRunId) return;
  const log = await api(`/api/runs/${encodeURIComponent(state.selectedRunId)}/logs`);
  $("#runLog").textContent = log.logs || "";
  $("#copyLogStatus").textContent = "";
}

function renderRunList(runs) {
  $("#runList").innerHTML = runs
    .slice()
    .reverse()
    .map((run) => {
      const kind = run.status === "completed" ? "pass" : run.status === "running" ? "info" : "fail";
      const selected = state.selectedRunId === run.id ? "selected" : "";
      const stopButton =
        run.status === "running" || run.status === "stopping"
          ? `<button class="mini-stop" data-stop="${escapeHtml(run.id)}" title="Stop this run">Stop</button>`
          : "";
      return `
        <div class="run-item ${selected}" data-run="${escapeHtml(run.id)}">
          <strong>${escapeHtml(run.id)}</strong>
          <div class="run-actions">${badge(run.status, kind)}${stopButton}</div>
        </div>
      `;
    })
    .join("");
  document.querySelectorAll(".mini-stop").forEach((button) => {
    button.addEventListener("click", async (event) => {
      event.stopPropagation();
      await api(`/api/runs/${encodeURIComponent(button.dataset.stop)}/stop`, { method: "POST", body: "{}" });
      await loadRuns();
    });
  });
  document.querySelectorAll(".run-item").forEach((item) => {
    item.addEventListener("click", async () => {
      state.selectedRunId = item.dataset.run;
      renderRunList(runs);
      await loadSelectedRunLog();
    });
  });
  if (window.lucide) window.lucide.createIcons();
}

init().catch((error) => {
  console.error(error);
  $("#runLog").textContent = error.message;
});
