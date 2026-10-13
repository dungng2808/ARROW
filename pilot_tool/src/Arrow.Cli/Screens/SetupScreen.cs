using Arrow.Cli.Ui;
using Arrow.Core.Config;
using Arrow.Core.Shards;
using Arrow.Core.Util;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Tooling;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>Cài đặt máy 8 bước (SETUP.md §5). Ghi machine.local.json và litellm.config.local.yaml.</summary>
public sealed class SetupScreen(Session session)
{
    private const string Back = "← Quay lại bước trước";

    /// <summary>Danh sách máy mặc định của pilot (SETUP.md §9).</summary>
    private static readonly ShardOwner[] DefaultMachines =
    [
        new("Dung-mac", "Dũng-mac"), new("Dung-win", "Dũng-win"), new("KieuAnh", "Kiều Anh"),
        new("Quang", "Quang"), new("Chinh", "Chinh"), new("Han", "Hán"),
    ];

    private enum Step { Next, Back }

    private MachineConfig _draft = new();
    private LiteLlmConfigFile _llm = new();

    public async Task<bool> RunAsync(CancellationToken cancellationToken)
    {
        var paths = session.Paths;
        _draft = session.Machine ?? new MachineConfig
        {
            ArrowRoot = paths.ArrowRoot,
            Os = MachineInfo.RuntimeId,
            WorkDir = DefaultWorkDir,
            Workers = new WorkersConfig { Build = MachineInfo.SuggestedBuildWorkers(), Llm = 2 },
        };
        _llm = new ModelScreen(session).LoadLlm();
        RemapMovedArrow();

        var steps = new Func<CancellationToken, Task<Step>>[]
        {
            _ => Task.FromResult(StepMachine()),
            _ => Task.FromResult(StepShard()),
            _ => Task.FromResult(StepDirectories()),
            _ => Task.FromResult(StepRepository()),
            StepJdkAsync,
            StepApiAsync,
            _ => Task.FromResult(StepResources()),
        };

        for (var index = 0; index < steps.Length;)
        {
            AnsiConsole.Clear();
            Theme.Logo();
            var result = await steps[index](cancellationToken);
            index = result == Step.Back ? Math.Max(0, index - 1) : index + 1;
        }

        AnsiConsole.Clear();
        Theme.Logo();
        if (!Review())
        {
            AnsiConsole.MarkupLine("[yellow]Chưa lưu. Chạy lại arrow setup khi cần.[/]");
            return false;
        }

        _draft = _draft with { Os = MachineInfo.RuntimeId, ArrowRoot = paths.ArrowRoot };
        _draft.Save(paths.MachineConfigPath);
        // Provider và khóa đã lưu ở bước 6 (ModelScreen); ở đây chỉ bảo đảm file có master_key.
        _llm = new ModelScreen(session).LoadLlm();
        _llm.Save(paths.LiteLlmConfigPath);
        ToolLog.Info($"setup: đã lưu cấu hình máy {_draft.MachineName}, shard {_draft.Shard}, thư mục {_draft.WorkDir}, repo {_draft.RepoPolicy}, worker build {_draft.Workers.Build}/model {_draft.Workers.Llm}, proxy {_draft.Llm.ProxyMode}, endpoint {_llm.EndpointHosts()}, khóa {_draft.Llm.KeyAlias}");
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã lưu [bold]{Markup.Escape(paths.Relative(paths.MachineConfigPath))}[/] và [bold]{Markup.Escape(paths.Relative(paths.LiteLlmConfigPath))}[/] (hai file không commit).");
        return true;
    }

    // ── Bước 1: máy này ──────────────────────────────────────────────────
    private Step StepMachine()
    {
        Title(1, "Máy này");
        Explain("Cho tool biết máy này là máy nào trong 6 máy của nhóm.",
            "Kết quả chạy được gắn với tên máy; tool dựa vào tên máy để đề xuất đúng shard (phần việc) ở bước 2 và đặt tên file kết quả.",
            "Chọn tên của bạn trong danh sách (ví dụ Dũng-mac). Máy không có trong danh sách thì chọn \"Khác\" và nhập mã máy.");
        Info($"Hệ điều hành: {MachineInfo.OsDescription} · {MachineInfo.RuntimeId} · {MachineInfo.CpuCount} nhân · RAM {MachineInfo.RamGb} GB\n" +
             $"Thư mục ARROW: {session.Paths.ArrowRoot}");
        var machines = KnownMachines();
        var choices = machines.Select(m => $"{m.DisplayName} ({m.Id})").Append("Khác (nhập tay)").ToList();
        var current = machines.FindIndex(m => m.Id == _draft.MachineName);
        var prompt = new SelectionPrompt<string>().Title("Máy này là:").AddChoices(choices);
        if (current >= 0)
        {
            prompt.DefaultValue(choices[current]);
        }

        var choice = AnsiConsole.Prompt(prompt);
        var name = choice == "Khác (nhập tay)"
            ? AnsiConsole.Prompt(new TextPrompt<string>("Mã máy (chỉ chữ không dấu, số, gạch ngang):").Validate(v => System.Text.RegularExpressions.Regex.IsMatch(v, "^[A-Za-z0-9-]+$")))
            : machines[choices.IndexOf(choice)].Id;
        _draft = _draft with { MachineName = name };
        return Step.Next;
    }

    // ── Bước 2: shard ────────────────────────────────────────────────────
    private Step StepShard()
    {
        Title(2, "Shard");
        Explain("Chọn phần việc của máy này: danh sách CUT (class cần sinh test) mà máy này sẽ chạy.",
            "Pilot có 59 CUT được chia cho 6 máy để chạy song song; mỗi máy chạy đúng phần của mình, cuối cùng người điều phối gom kết quả.",
            "Giữ shard tool đề xuất theo tên máy. Chỉ chọn shard khác khi chạy thay người khác.");
        var experiments = Directory.Exists(session.Paths.ExperimentsDir)
            ? Directory.GetFiles(session.Paths.ExperimentsDir, "*.json").OrderBy(f => f).ToList()
            : [];
        if (experiments.Count == 0)
        {
            Info("[red]Không có cấu hình thực nghiệm trong pilot_tool/experiments/. git pull rồi chạy lại.[/]");
            return Pause();
        }

        var experimentPath = experiments[0];
        if (experiments.Count > 1)
        {
            var names = experiments.Select(e => Path.GetFileName(e)).ToList();
            var picked = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Cấu hình thực nghiệm:").AddChoices(names));
            experimentPath = experiments[names.IndexOf(picked)];
        }
        LoadedExperiment experiment;
        try
        {
            experiment = ExperimentLoader.Load(experimentPath);
        }
        catch (ArrowException ex)
        {
            Info($"[red]{Markup.Escape(ex.ToString())}[/]");
            return Pause();
        }

        Info($"Cấu hình thực nghiệm: {Path.GetFileName(experimentPath)} {Theme.Ok} hash manifest khớp");
        _draft = _draft with { Experiment = session.Paths.Relative(experimentPath) };

        var plans = FindPlans(experiment).ToList();
        if (plans.Count == 0)
        {
            Info("[yellow]Chưa có phân công shard cho cấu hình này.[/]\n" +
                 "Người điều phối: chọn \"Tạo phân công ngay\" (ghi vào ARROW/shards/, sau đó commit và push).\n" +
                 "Người khác: git pull để lấy phân công, hoặc bỏ qua rồi chạy lại arrow setup sau.");
            const string create = "Tạo phân công ngay (người điều phối)";
            const string skip = "Tiếp tục, bỏ qua bước này";
            var action = AnsiConsole.Prompt(new SelectionPrompt<string>().AddChoices(create, skip, Back));
            if (action == Back)
            {
                return Step.Back;
            }

            if (action == skip)
            {
                return Step.Next;
            }

            AnsiConsole.WriteLine();
            new ShardPlanScreen(session).Run(experimentPath, null, null, null, _draft.MachineName, assumeYes: false);
            plans = FindPlans(experiment).ToList();
            if (plans.Count == 0)
            {
                return Step.Next;
            }
        }

        var (planDir, plan) = plans[0];
        var mine = plan.Shards.FirstOrDefault(s => s.Owner == _draft.MachineName);
        var options = plan.Shards
            .Select(s => $"{s.ShardId} · {s.OwnerDisplay} · {s.TaskCount} CUT ({string.Join(", ", s.TasksByDataset.Select(kv => $"{kv.Value} {kv.Key}"))})")
            .Append(Back).ToList();
        Info($"Phân công: {session.Paths.Relative(planDir)} ({plan.Shards.Count} shard, {plan.Shards.Sum(s => s.TaskCount)} CUT)");
        var prompt = new SelectionPrompt<string>().Title("Shard của máy này:").AddChoices(options);
        if (mine is not null)
        {
            prompt.DefaultValue(options[plan.Shards.IndexOf(mine)]);
        }

        var choice = AnsiConsole.Prompt(prompt);
        if (choice == Back)
        {
            return Step.Back;
        }

        var shard = plan.Shards[options.IndexOf(choice)];
        if (shard.Owner != _draft.MachineName)
        {
            AnsiConsole.MarkupLine($"[yellow]Shard này giao cho {Markup.Escape(shard.OwnerDisplay)}; máy này sẽ chạy thay (ghi lại người chạy thật).[/]");
        }

        _draft = _draft with { Shard = session.Paths.Relative(Path.Combine(planDir, shard.File)) };
        return Step.Next;
    }

    /// <summary>
    /// Thư mục ARROW đã đổi tên/chuyển chỗ: đổi các đường dẫn nằm trong ARROW cũ (JDK, thư mục repository) sang chỗ mới
    /// và nhắc các việc tool không tự làm được (cài lại lệnh arrow, LiteLLM).
    /// </summary>
    private void RemapMovedArrow()
    {
        var oldRoot = _draft.ArrowRoot.TrimEnd('/', '\\');
        var newRoot = session.Paths.ArrowRoot.TrimEnd('/', '\\');
        if (string.IsNullOrEmpty(oldRoot) || string.Equals(oldRoot, newRoot, StringComparison.Ordinal))
        {
            return;
        }

        string Remap(string path) => path.StartsWith(oldRoot, StringComparison.Ordinal) ? newRoot + path[oldRoot.Length..] : path;
        _draft = _draft with
        {
            ArrowRoot = newRoot,
            JdkHomes = _draft.JdkHomes.ToDictionary(p => p.Key, p => Remap(p.Value)),
            WorkDir = Remap(_draft.WorkDir),
        };
        ToolLog.Info($"setup: thư mục ARROW đổi từ {oldRoot} sang {newRoot}; đã cập nhật đường dẫn JDK và thư mục repository");
        var steps = Arrow.Infrastructure.Doctor.DoctorService.MovedArrowSteps().Take(2).Select((s, i) => $"[bold]{i + 1}.[/] {Markup.Escape(s)}");
        AnsiConsole.Write(new Panel(new Markup(
                $"Cấu hình cũ ghi [bold]{Markup.Escape(oldRoot)}[/], hiện tại là [bold]{Markup.Escape(newRoot)}[/].\n" +
                "Tool đã đổi đường dẫn JDK và thư mục repository sang chỗ mới; bạn kiểm tra lại ở bước 3 và bước 5.\n" +
                "Hai việc tool không tự làm được, làm sau khi cài đặt xong (nếu chưa làm):\n" + string.Join('\n', steps)))
            .Header("[yellow] ⚠ Thư mục ARROW đã đổi tên hoặc chuyển chỗ [/]").BorderColor(Color.Yellow).Border(BoxBorder.Rounded).Expand());
    }

    /// <summary>Khung lỗi đường dẫn: thư mục nào sai, vì sao, các bước sửa.</summary>
    private static void ShowPathAdvice(PathAdvice advice, string title)
    {
        var lines = new List<string>();
        foreach (var issue in advice.Issues)
        {
            lines.Add($"[bold]{Markup.Escape(issue.Problem)}[/]");
            lines.Add($"[grey]Vì sao: {Markup.Escape(issue.Effect)}.[/]");
        }

        lines.Add("");
        lines.AddRange(advice.Steps.Select((s, i) => $"[bold]{i + 1}.[/] {Markup.Escape(s)}"));
        AnsiConsole.Write(new Panel(new Markup(string.Join('\n', lines)))
            .Header(advice.Blocking ? $"[red] ✘ {Markup.Escape(title)} [/]" : $"[yellow] ⚠ {Markup.Escape(title)} [/]")
            .BorderColor(advice.Blocking ? Color.Red : Color.Yellow).Border(BoxBorder.Rounded).Expand());
    }

    private PathAdvice Diagnose(string path) =>
        PathDiagnostics.Diagnose(path, session.Paths.ArrowRoot, OperatingSystem.IsWindows(), HomeDir);

    // ── Bước 3: thư mục ──────────────────────────────────────────────────
    private Step StepDirectories()
    {
        Title(3, "Thư mục");
        var workDir = session.Paths.Resolve(_draft.WorkDir);
        if (!WorkDirPolicy.IsClean(workDir))
        {
            workDir = session.Paths.Resolve(DefaultWorkDir); // thư mục cũ có dấu cách hoặc dấu tiếng Việt: đề xuất mặc định
        }

        Explain("Chọn nơi tool tải các repository Java về (đúng commit trong manifest) và tạo bản sao riêng cho mỗi lần build.",
            "Mỗi CUT nằm trong một repository thật; tool phải tải repository đó về máy rồi build bằng Maven/Gradle. Thư mục này dùng tạm, có thể chiếm vài GB khi đang chạy. " +
            "Đường dẫn MUST không có dấu cách và không có chữ có dấu: một số test đọc file qua URL sẽ fail nếu đường dẫn có dấu cách, làm kết quả khác nhau giữa các máy.",
            "Nhấn Enter để dùng mặc định (thư mục repo trong ARROW; nếu đường dẫn ARROW có dấu cách thì ~/arrow-repo hoặc C:\\arrow-repo). Chỉ đổi khi ổ này không đủ chỗ (cần ≥ 20 GB trống). Nhập < để quay lại.");
        var arrowRepo = session.Paths.Resolve("../repo");
        if (!WorkDirPolicy.IsClean(arrowRepo))
        {
            // Giải thích vì sao mặc định không nằm trong ARROW, và cách để giữ được thư mục repo trong ARROW.
            ShowPathAdvice(Diagnose(arrowRepo), "Thư mục ARROW có đường dẫn không dùng được cho repository, nên mặc định đặt ngoài ARROW");
        }

        Info($"Hiện tại: {Markup.Escape(workDir)}   còn trống {MachineInfo.FreeGb(workDir)} GB\n" +
             $"Kết quả và log (không đổi ở đây): {Markup.Escape(session.Paths.Resolve(_draft.RunsDir))}" +
             (OperatingSystem.IsWindows() ? "\n[yellow]Windows: nếu đường dẫn dài, nên chọn đường dẫn ngắn, ví dụ D:\\aw[/]" : ""));
        var input = AnsiConsole.Prompt(new TextPrompt<string>("Thư mục repository và workspace:").DefaultValue(workDir)).Trim();
        if (input == "<")
        {
            return Step.Back;
        }

        var full = session.Paths.Resolve(WorkDirPolicy.ExpandHome(input, HomeDir), Environment.CurrentDirectory);
        var advice = Diagnose(full);
        if (advice.Blocking)
        {
            ShowPathAdvice(advice, $"Không dùng được {full}");
            return StepDirectories(); // hỏi lại
        }

        var free = MachineInfo.FreeGb(full);
        if (free < 20)
        {
            AnsiConsole.MarkupLine($"[yellow]Chỉ còn {free} GB trống (khuyến nghị ≥ 20 GB).[/]");
        }

        if (!advice.Clean)
        {
            ShowPathAdvice(advice, "Dùng được nhưng nên đổi");
        }

        // Thư mục nằm trong ARROW thì lưu tương đối, để chuyển ARROW sang chỗ khác vẫn dùng được.
        var insideArrow = full.StartsWith(session.Paths.ArrowRoot + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase);
        _draft = _draft with { WorkDir = insideArrow ? session.Paths.Relative(full) : full };
        return Step.Next;
    }

    // ── Bước 4: repository ───────────────────────────────────────────────
    private Step StepRepository()
    {
        Title(4, "Repository");
        Explain("Chọn cách tải repository và có xóa sau khi chạy xong không.",
            "\"Chỉ commit cần thiết\" tải nhanh và nhẹ vì chỉ lấy đúng một commit; xóa sau khi xong giúp ổ không đầy. Giữ lại thì lần chạy sau nhanh hơn nhưng tốn chỗ. Giữ workspace của job lỗi chỉ dùng khi cần gỡ lỗi, rất tốn chỗ.",
            "Giữ lựa chọn khuyến nghị, và trả lời \"không\" ở câu hỏi giữ workspace lỗi (trừ khi đang gỡ lỗi).");
        var labels = new Dictionary<string, RepoPolicy>
        {
            ["Chỉ commit cần thiết, xóa sau khi xong (khuyến nghị)"] = RepoPolicy.CommitOnlyDeleteAfter,
            ["Chỉ commit cần thiết, giữ lại"] = RepoPolicy.CommitOnlyKeep,
            ["Toàn bộ lịch sử, giữ lại"] = RepoPolicy.FullHistoryKeep,
        };
        var options = labels.Keys.Append(Back).ToList();
        var choice = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("Cách tải và xóa repository:").AddChoices(options)
            .DefaultValue(labels.First(kv => kv.Value == _draft.RepoPolicy).Key));
        if (choice == Back)
        {
            return Step.Back;
        }

        var keep = AnsiConsole.Confirm("Giữ workspace của job lỗi để gỡ lỗi?", _draft.KeepFailedWorkspaces);
        _draft = _draft with { RepoPolicy = labels[choice], KeepFailedWorkspaces = keep };
        return Step.Next;
    }

    // ── Bước 5: JDK, Maven, Gradle, Git ──────────────────────────────────
    private async Task<Step> StepJdkAsync(CancellationToken cancellationToken)
    {
        Title(5, "JDK, Maven, Gradle, Git");
        Explain("Tìm và kiểm tra các công cụ cần để build repository: JDK 8, 11, 17, 21, Maven, Gradle, Git.",
            "Mỗi repository phải build bằng đúng JDK mà preflight đã dùng; Maven/Gradle chỉ cần khi repository không có sẵn mvnw/gradlew; Git để tải repository.",
            "Nếu mọi dòng có ✔ thì chọn \"Tiếp tục\". Dòng nào ✘: cài theo Java-version/README.md, hoặc chọn \"Sửa đường dẫn JDK …\" và nhập thư mục JDK đã có.");
        var homes = new Dictionary<string, string>(_draft.JdkHomes);
        ToolInfo? maven = null, gradle = null, git = null;
        await AnsiConsole.Status().StartAsync("Đang tìm JDK và công cụ…", async _ =>
        {
            foreach (var major in ToolProbe.RequiredJdkMajors)
            {
                homes.TryGetValue(major.ToString(), out var current);
                var info = await ToolProbe.FindJdkAsync(major, session.Paths.ArrowRoot, current, cancellationToken);
                if (info is not null)
                {
                    homes[major.ToString()] = info.Home;
                }
            }

            homes.TryGetValue("17", out var jdk17);
            maven = await ToolProbe.ProbeMavenAsync(_draft.MavenPath, jdk17, cancellationToken);
            gradle = await ToolProbe.ProbeGradleAsync(_draft.GradlePath, jdk17, cancellationToken);
            git = await ToolProbe.ProbeGitAsync(_draft.GitPath, cancellationToken);
        });

        while (true)
        {
            var table = new Table().Border(TableBorder.Rounded).AddColumns("Công cụ", "", "Phiên bản / đường dẫn");
            foreach (var major in ToolProbe.RequiredJdkMajors)
            {
                var found = homes.TryGetValue(major.ToString(), out var home);
                table.AddRow($"JDK {major}", found ? Theme.Ok : Theme.Fail, found ? Markup.Escape(home!) : "[red]không tìm thấy – xem Java-version/README.md[/]");
            }

            table.AddRow("Maven", maven is null ? Theme.Warn : Theme.Ok, maven is null ? "không có (chỉ cần khi repo không có mvnw)" : Markup.Escape($"{maven.Version} · {maven.Path}"));
            table.AddRow("Gradle", gradle is null ? Theme.Warn : Theme.Ok, gradle is null ? "không có (chỉ cần khi repo không có gradlew)" : Markup.Escape($"{gradle.Version} · {gradle.Path}"));
            table.AddRow("Git", git is null ? Theme.Fail : Theme.Ok, git is null ? "[red]không tìm thấy[/]" : Markup.Escape($"{git.Version} · {git.Path}"));
            AnsiConsole.Write(table);

            var options = new List<string> { "Tiếp tục" };
            options.AddRange(ToolProbe.RequiredJdkMajors.Select(m => $"Sửa đường dẫn JDK {m}"));
            options.Add(Back);
            var choice = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("").AddChoices(options));
            if (choice == Back)
            {
                return Step.Back;
            }

            if (choice == "Tiếp tục")
            {
                break;
            }

            var majorToEdit = int.Parse(choice.Split(' ').Last());
            var path = AnsiConsole.Ask<string>($"Đường dẫn JAVA_HOME của JDK {majorToEdit}:");
            var verified = await ToolProbe.VerifyJdkAsync(path.Trim(), majorToEdit, cancellationToken);
            if (verified is null)
            {
                AnsiConsole.MarkupLine($"{Theme.Fail} Không phải JDK {majorToEdit} chạy được (cần bin/java và bin/javac đúng phiên bản).");
            }
            else
            {
                homes[majorToEdit.ToString()] = verified.Home;
            }
        }

        _draft = _draft with
        {
            JdkHomes = homes,
            MavenPath = maven?.Path ?? "",
            GradlePath = gradle?.Path ?? "",
            GitPath = git?.Path ?? "",
        };
        return Step.Next;
    }

    // ── Bước 6: API và LiteLLM ───────────────────────────────────────────
    private async Task<Step> StepApiAsync(CancellationToken cancellationToken)
    {
        Title(6, "Gọi model");
        Explain("Cài cách máy này gọi các model của thực nghiệm: qua LiteLLM proxy, mỗi model tới provider (endpoint) của nó, bằng khóa API của bạn.",
            "Mọi lượt sinh test và sửa test đều gọi model. Model, tham số và giá giống nhau trên mọi máy (lấy từ cấu hình thực nghiệm); chỉ provider, endpoint và khóa API là của riêng máy bạn. Khóa lưu trong file litellm.config.local.yaml trên máy, không bao giờ lên Git.",
            "Chọn \"Tool tự bật LiteLLM proxy\". Với mỗi model: chọn provider, giữ endpoint mặc định trừ khi bạn dùng cổng trung gian khác, dán khóa API (màn hình chỉ hiện ****). Đặt tên gợi nhớ cho khóa. Sửa sau bằng arrow models.");
        Info("Model, tham số, giá của thực nghiệm: xem và sửa bằng arrow models (menu \"Model, provider, khóa API, giá\").");
        var modes = new Dictionary<string, string>
        {
            ["Tool tự bật LiteLLM proxy khi chạy"] = "auto_start",
            ["Dùng proxy đang chạy sẵn ở địa chỉ khác"] = "external",
        };
        var modeChoice = AnsiConsole.Prompt(new SelectionPrompt<string>().Title("LiteLLM proxy:").AddChoices(modes.Keys.Append(Back))
            .DefaultValue(modes.First(kv => kv.Value == _draft.Llm.ProxyMode).Key));
        if (modeChoice == Back)
        {
            return Step.Back;
        }

        if (modes[modeChoice] == "auto_start" && await ToolProbe.ProbeLiteLlmAsync(session.Paths.ArrowRoot, cancellationToken) is null or { Version: ToolProbe.Broken })
        {
            AnsiConsole.MarkupLine($"{Theme.Warn} Máy này chưa có LiteLLM (chương trình trung gian để gọi model).");
            if (AnsiConsole.Confirm("Cài ngay vào ARROW/litellm?"))
            {
                await FixActions.InstallLiteLlmAsync(session, cancellationToken);
            }
            else
            {
                AnsiConsole.MarkupLine("[grey]Bỏ qua; có thể cài sau trong \"Kiểm tra môi trường\".[/]");
            }
        }

        var proxyUrl = _draft.Llm.ProxyUrl;
        if (modes[modeChoice] == "external")
        {
            proxyUrl = AnsiConsole.Prompt(new TextPrompt<string>("Địa chỉ proxy:").DefaultValue(proxyUrl));
        }

        ConfigureModels();
        var alias = AnsiConsole.Prompt(new TextPrompt<string>("Tên gợi nhớ của khóa (ghi vào kết quả, không ghi khóa):")
            .DefaultValue(string.IsNullOrEmpty(_draft.Llm.KeyAlias) ? _draft.MachineName.ToLowerInvariant() : _draft.Llm.KeyAlias));
        AnsiConsole.MarkupLine("[grey]Gọi thử từng model (1 lượt): arrow models → \"Gọi thử\". Chạy thử cả pipeline: menu \"Chạy smoke\".[/]");

        _draft = _draft with { Llm = _draft.Llm with { ProxyMode = modes[modeChoice], ProxyUrl = proxyUrl, KeyAlias = alias } };
        return Step.Next;
    }

    /// <summary>Hỏi provider và khóa cho từng model của cấu hình thực nghiệm đã chọn ở bước 2.</summary>
    private void ConfigureModels()
    {
        var screen = new ModelScreen(session);
        foreach (var model in ExperimentModels())
        {
            var route = screen.LoadLlm().Route(model);
            var status = route is { IsComplete: true }
                ? $"{Theme.Ok} {Markup.Escape($"{route.ProviderModel} · {route.EndpointHost}")}" + (route.HasApiKey ? $" · khóa {Markup.Escape(LiteLlmConfigFile.Mask(route.ApiKey))}" : "")
                : $"{Theme.Fail} chưa có provider hoặc khóa";
            AnsiConsole.MarkupLine($"Model [bold]{Markup.Escape(model)}[/]: {status}");
            if (AnsiConsole.Confirm($"Cài provider và khóa API cho {Markup.Escape(model)}?", route is not { IsComplete: true }))
            {
                screen.ConfigureRoute(model);
            }
        }
    }

    private List<string> ExperimentModels()
    {
        try
        {
            return ExperimentLoader.Load(session.Paths.Resolve(_draft.Experiment)).Config.Models.Select(m => m.Name).ToList();
        }
        catch (Exception ex) when (ex is ArrowException or IOException)
        {
            return [];
        }
    }

    // ── Bước 7: tài nguyên ───────────────────────────────────────────────
    private Step StepResources()
    {
        Title(7, "Tài nguyên");
        Explain("Chọn số việc chạy cùng lúc: số luồng build, số luồng gọi model, và giới hạn số lượt gọi model mỗi phút.",
            "Nhiều luồng thì chạy nhanh hơn, nhưng mỗi luồng build tốn khoảng 3 GB RAM và CPU; gọi model quá nhiều mỗi phút sẽ chạm hạn mức của khóa API (lỗi 429).",
            $"Nhấn Enter để giữ giá trị gợi ý (theo {MachineInfo.CpuCount} nhân, RAM {MachineInfo.RamGb} GB). Nhập 0 ở câu đầu để quay lại.");
        var build = AnsiConsole.Prompt(new TextPrompt<int>("Số luồng build:").DefaultValue(_draft.Workers.Build).Validate(v => v is >= 0 and <= 16));
        if (build == 0)
        {
            return Step.Back;
        }

        var llm = AnsiConsole.Prompt(new TextPrompt<int>("Số luồng gọi model:").DefaultValue(_draft.Workers.Llm).Validate(v => v is >= 1 and <= 16));
        var rpm = AnsiConsole.Prompt(new TextPrompt<int>("Giới hạn số lượt gọi model mỗi phút (theo hạn mức khóa của bạn):").DefaultValue(_draft.Llm.RpmLimit).Validate(v => v is >= 1 and <= 10000));
        _draft = _draft with { Workers = new WorkersConfig { Build = build, Llm = llm }, Llm = _draft.Llm with { RpmLimit = rpm } };
        return Step.Next;
    }

    // ── Bước 8: xem lại ──────────────────────────────────────────────────
    private bool Review()
    {
        Title(8, "Xem lại");
        Explain("Hiện lại toàn bộ lựa chọn để bạn kiểm tra trước khi lưu.",
            "Lưu xong, tool ghi machine.local.json (cấu hình máy) và litellm.config.local.yaml (khóa API), rồi tự chạy \"Kiểm tra môi trường\" để chắc máy chạy được.",
            "Xem bảng; đúng thì chọn Yes (y). Muốn sửa thì chọn No (n) rồi chạy lại arrow setup.");
        var table = new Table().Border(TableBorder.Rounded).HideHeaders().AddColumns("", "");
        table.AddRow("Máy", Markup.Escape($"{_draft.MachineName} · {MachineInfo.RuntimeId}"));
        table.AddRow("Thực nghiệm", Markup.Escape(_draft.Experiment));
        table.AddRow("Shard", Markup.Escape(string.IsNullOrEmpty(_draft.Shard) ? "(chưa có phân công)" : _draft.Shard));
        table.AddRow("Thư mục", Markup.Escape($"repository: {_draft.WorkDir} · runs: {_draft.RunsDir}"));
        table.AddRow("Repository", Markup.Escape($"{_draft.RepoPolicy} · giữ workspace lỗi: {(_draft.KeepFailedWorkspaces ? "có" : "không")}"));
        table.AddRow("JDK", string.Join(" / ", ToolProbe.RequiredJdkMajors.Select(m => $"{m} {(_draft.JdkHomes.ContainsKey(m.ToString()) ? Theme.Ok : Theme.Fail)}")));
        var routes = new ModelScreen(session).LoadLlm();
        table.AddRow("Model", string.Join("\n", ExperimentModels().Select(m => routes.Route(m) is { IsComplete: true } r
            ? $"{Theme.Ok} {Markup.Escape($"{m} · {r.ProviderModel} · {r.EndpointHost}")}"
            : $"{Theme.Fail} {Markup.Escape(m)} chưa có provider/khóa")) + Markup.Escape($"\nLiteLLM {_draft.Llm.ProxyMode} · khóa: {_draft.Llm.KeyAlias}"));
        table.AddRow("Worker", Markup.Escape($"build {_draft.Workers.Build} · model {_draft.Workers.Llm} · {_draft.Llm.RpmLimit} lượt/phút"));
        AnsiConsole.Write(table);
        return AnsiConsole.Confirm("Lưu vào machine.local.json?");
    }

    // ── tiện ích ─────────────────────────────────────────────────────────
    private List<ShardOwner> KnownMachines()
    {
        var owners = new List<ShardOwner>();
        foreach (var planPath in EnumeratePlanFiles())
        {
            try
            {
                owners.AddRange(Json.Read<ShardPlanFile>(planPath).Shards.Select(s => new ShardOwner(s.Owner, s.OwnerDisplay)));
            }
            catch (Exception ex) when (ex is IOException or System.Text.Json.JsonException)
            {
            }
        }

        owners.AddRange(DefaultMachines);
        return owners.DistinctBy(o => o.Id).ToList();
    }

    private IEnumerable<string> EnumeratePlanFiles() =>
        Directory.Exists(session.Paths.ShardsDir)
            ? Directory.EnumerateFiles(session.Paths.ShardsDir, "plan.json", SearchOption.AllDirectories).OrderBy(p => p)
            : [];

    private IEnumerable<(string Dir, ShardPlanFile Plan)> FindPlans(LoadedExperiment experiment)
    {
        foreach (var planPath in EnumeratePlanFiles())
        {
            ShardPlanFile? plan = null;
            try
            {
                plan = Json.Read<ShardPlanFile>(planPath);
            }
            catch (Exception ex) when (ex is IOException or System.Text.Json.JsonException)
            {
            }

            if (plan is not null && string.Equals(plan.ExperimentSha256, experiment.Sha256, StringComparison.OrdinalIgnoreCase))
            {
                yield return (Path.GetDirectoryName(planPath)!, plan);
            }
        }
    }

    private static string HomeDir => Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);

    /// <summary>
    /// Mặc định: thư mục <c>repo</c> trong ARROW (đã có trong .gitignore), lưu tương đối so với pilot_tool.
    /// Đường dẫn ARROW có dấu cách hoặc chữ có dấu thì dùng <c>~/arrow-repo</c> / <c>C:\arrow-repo</c> (WorkDirPolicy).
    /// </summary>
    private string DefaultWorkDir =>
        WorkDirPolicy.IsClean(session.Paths.Resolve("../repo")) ? "../repo" : WorkDirPolicy.Default(OperatingSystem.IsWindows(), HomeDir);

    private static void Title(int step, string name) => AnsiConsole.Write(new Rule($"[bold]Cài đặt {step}/8 · {name}[/]").LeftJustified());

    private static void Info(string markup) => AnsiConsole.Write(new Panel(new Markup(markup)).Border(BoxBorder.Rounded).Expand());

    /// <summary>Khung giải thích đầu mỗi bước: bước này làm gì, vì sao cần, bạn cần làm gì.</summary>
    private static void Explain(string what, string why, string todo)
    {
        var grid = new Grid().AddColumn(new GridColumn().NoWrap()).AddColumn();
        grid.AddRow("[bold teal]Bước này làm gì[/]", what);
        grid.AddRow("[bold teal]Vì sao cần[/]", why);
        grid.AddRow("[bold teal]Bạn cần làm gì[/]", todo);
        AnsiConsole.Write(new Panel(grid).Border(BoxBorder.Rounded).BorderColor(Color.Teal).Expand());
    }

    private static Step AskContinue() =>
        AnsiConsole.Prompt(new SelectionPrompt<string>().AddChoices("Tiếp tục", Back)) == Back ? Step.Back : Step.Next;

    private static Step Pause() => AskContinue();
}
