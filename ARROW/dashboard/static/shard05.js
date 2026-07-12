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
  $("#selectRq1PromptsBtn").addEventListener("click", selectRq1Prompts);
  $("#copyProjectErrorsBtn").addEventListener("click", copyProjectErrors);
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

async function loadShard05Status() {
  try {
    const payload = await api("/api/shards/shard05/status");
    state.shard05 = payload || { summary: {}, projects: [] };
  } catch (error) {
    state.shard05 = { summary: {}, projects: [], error: error.message || "Không tải được shard 05" };
  }
  renderShard05();
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
  if (state.shard05.error) {
    $("#shard05LockedMeta").textContent = state.shard05.error;
    $("#shard05Meta").textContent = state.shard05.error;
    $("#shard05Summary").innerHTML = "";
    $("#shard05Rows").innerHTML = `<tr><td colspan="10">${escapeHtml(state.shard05.error)}</td></tr>`;
    return;
  }
  $("#shard05LockedMeta").textContent = `${formatNumber(summary.total_projects || projects.length)} project trong shard`;
  $("#shard05Meta").textContent = `${formatNumber(summary.total_projects || projects.length)} project · ${formatNumber(
    summary.experiments_completed || 0,
  )} lượt chạy đã ghi nhận`;
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
  const rows = projects.filter((project) => {
    if (filter && project.status !== filter) return false;
    if (!query) return true;
    const haystack = [
      project.project_id,
      project.sample_id,
      project.sample_file,
      project.focal_class,
      project.focal_class_path,
      project.status,
      project.last_agent,
      project.last_prompt,
      ...Object.values(project.prompt_statuses || {}).flatMap((prompt) => [
        prompt.prompt_strategy,
        prompt.status,
        prompt.latest_failed_agent,
        prompt.latest_failed_state,
      ]),
    ]
      .join(" ")
      .toLowerCase();
    return haystack.includes(query);
  });
  $("#shard05Rows").innerHTML = rows.length
    ? rows
        .map(
          (project) => `
            <tr data-project="${escapeHtml(project.project_id)}" class="${state.selectedProjectId === project.project_id ? "selected" : ""}">
              <td>${escapeHtml(project.index)}</td>
              <td title="${escapeHtml(project.project_id)}">${escapeHtml(project.project_id)}</td>
              <td title="${escapeHtml(project.sample_file || project.sample_id)}">${escapeHtml(project.sample_id || project.sample_file || "")}</td>
              <td title="${escapeHtml(project.focal_class_path || project.focal_class)}">${escapeHtml(project.focal_class || "")}</td>
              <td>${badge(shard05Label(project.status), shard05Kind(project.status))}</td>
              <td>${formatNumber(project.experiments_completed || 0)}</td>
              <td>${formatNumber(project.failed_experiments || 0)}</td>
              <td>${promptCell(project, "zero-shot")}</td>
              <td>${promptCell(project, "few-shot")}</td>
              <td>${promptCell(project, "zero-shot-project-aware")}</td>
              <td>
                ${
                  project.status === "HAS_FAILURES"
                    ? `<div class="row-actions">
                        <button class="mini-secondary view-project-errors" type="button" data-project="${escapeHtml(project.project_id)}" data-prompt="">Xem tất cả lỗi</button>
                      </div>`
                    : `<span class="muted-text">—</span>`
                }
              </td>
            </tr>
          `,
        )
        .join("")
    : `<tr><td colspan="10">Không có project nào khớp bộ lọc hiện tại.</td></tr>`;
  document.querySelectorAll("#shard05Rows tr[data-project]").forEach((row) => {
    row.addEventListener("click", () => {
      const project = rows.find((item) => item.project_id === row.dataset.project);
      if (project?.status === "HAS_FAILURES") loadProjectErrors(project.project_id);
    });
  });
  document.querySelectorAll(".view-project-errors").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation();
      loadProjectErrors(button.dataset.project, button.dataset.prompt || "");
    });
  });
  document.querySelectorAll(".rerun-project").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation();
      rerunProject(button.dataset.project, button.dataset.prompt || "");
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

async function rerunProject(projectId, promptStrategy = "") {
  if (!projectId) return;
  const options = currentRunOptions();
  if (promptStrategy) {
    options.generation_prompts = [promptStrategy];
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

async function copyProjectErrors() {
  if (!state.projectErrorContent.trim()) return;
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(state.projectErrorContent);
    } else {
      fallbackCopy(state.projectErrorContent);
    }
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
    $("#runLog").textContent = "Chưa chọn run.";
    return;
  }
  const payload = await api(`/api/runs/${encodeURIComponent(run.id)}/logs`);
  $("#logTitle").textContent = `Log ${run.id}`;
  $("#runLog").textContent = payload.logs || "Run chưa có log.";
}

init().catch((error) => {
  console.error(error);
  setActionStatus(error.message || "Không mở được trang chạy shard 05.", "error");
});
