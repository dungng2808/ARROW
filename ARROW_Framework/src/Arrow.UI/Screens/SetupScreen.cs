using Arrow.Models.Config;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>
/// S2 (spec 06 Mục 4): sáu bước (tên máy, cấu hình thực nghiệm, JDK/Maven/Gradle/Git, thư mục repository, gọi model, tài nguyên); mỗi bước
/// in "Bước này làm gì / Vì sao / Bạn cần làm gì". Gõ <c>&lt;</c> ở câu hỏi để quay lại bước trước. Cuối cùng xem lại, lưu, rồi kiểm tra môi trường.
/// </summary>
public sealed class SetupScreen(ISetupService setup, ModelScreen models, DoctorScreen doctor, ConsoleInput input, ConsoleOutput output)
{
    private const string Back = "<";

    public async Task<int> RunAsync()
    {
        var state = await setup.LoadAsync(null, CancellationToken.None);
        var machine = state.Machine;
        var step = 1;
        while (step <= 6)
        {
            output.Blank();
            var next = step switch
            {
                1 => StepName(ref machine),
                2 => StepExperiment(state),
                3 => await StepToolsAsync(machine, m => machine = m),
                4 => StepWorkDir(ref machine),
                5 => await StepModelAsync(machine, m => machine = m),
                _ => StepResources(ref machine),
            };
            step = next ? step + 1 : Math.Max(1, step - 1);
        }

        output.Blank();
        output.Title("Xem lại cấu hình máy");
        output.Table(["Mục", "Giá trị"],
        [
            ["Tên máy", machine.MachineName],
            ["JDK", string.Join(", ", machine.JdkHomes.Select(j => $"{j.Key}={j.Value}"))],
            ["Maven / Gradle / Git", $"{Or(machine.MavenPath)} / {Or(machine.GradlePath)} / {Or(machine.GitPath)}"],
            ["Thư mục repository", machine.WorkDir],
            ["Thư mục công cụ", machine.ToolsDir],
            ["LiteLLM", $"{machine.Llm.ProxyMode} · {machine.Llm.ProxyUrl} · {machine.Llm.RpmLimit} lượt/phút"],
            ["Luồng", $"build {machine.Workers.Build} · gọi model {machine.Workers.Llm}"],
        ]);
        if (!input.Confirm("Lưu?", true))
        {
            output.Status(Mark.Skip, "Không lưu");
            return 1;
        }

        setup.Save(machine);
        output.Status(Mark.Ok, "Đã lưu machine.local.json");
        await doctor.RunAsync(null);
        return 0;
    }

    private static string Or(string value) => value.Length == 0 ? "(PATH)" : value;

    private void Explain(int step, string name, string what, string why, string todo)
    {
        output.Title($"Cài đặt {step}/6 · {name}");
        output.Line($"Bước này làm gì:  {what}");
        output.Line($"Vì sao:           {why}");
        output.Line($"Bạn cần làm gì:   {todo}");
        output.Blank();
    }

    private bool StepName(ref MachineConfig machine)
    {
        Explain(1, "Tên máy", "Đặt tên máy này", "Tên máy ghi vào run.lock.json để biết run chạy ở đâu khi gộp nhiều máy", "Nhập tên ngắn (ví dụ Dung-mac)");
        var name = input.Ask("Tên máy", machine.MachineName.Length > 0 ? machine.MachineName : Environment.MachineName);
        if (name == Back)
        {
            return false;
        }

        machine = machine with { MachineName = name };
        return true;
    }

    private bool StepExperiment(SetupState state)
    {
        Explain(2, "Cấu hình thực nghiệm", "Kiểm tra file experiments/*.json", "File này quyết định model, quy trình, manifest của thực nghiệm (dùng chung, commit)", "Chọn Tiếp tục");
        if (state.Experiments.Count == 0)
        {
            output.Status(Mark.Warn, "Chưa có file trong experiments/", "tạo theo spec 00 Mục 3.1 trước khi chạy");
        }

        foreach (var experiment in state.Experiments)
        {
            output.Status(Mark.Ok, Path.GetFileName(experiment));
        }

        return input.Choose("", ["Tiếp tục", "Quay lại"]) == 0;
    }

    private async Task<bool> StepToolsAsync(MachineConfig machine, Action<MachineConfig> save)
    {
        while (true)
        {
            var state = await setup.LoadAsync(machine, CancellationToken.None);
            Explain(3, "JDK, Maven, Gradle, Git", "Tìm JDK 8, 11, 17, 21 và công cụ build", "Mỗi repository build bằng đúng JDK của nó", "Mỗi dòng [OK] thì chọn Tiếp tục");
            output.Table(["Công cụ", "Trạng thái", "Phiên bản / đường dẫn"], state.Tools.Select(t => (IReadOnlyList<string>)
            [
                t.Name, t.Ok ? "[OK]" : "[X]", t.Ok ? $"{t.Version} · {t.Path}" : t.Hint ?? "",
            ]).ToList());
            output.Blank();
            var missing = state.Tools.Where(t => !t.Ok).ToList();
            var options = new List<string> { "Tiếp tục" };
            options.AddRange(missing.Select(t => $"Sửa đường dẫn {t.Name}"));
            options.Add("Quay lại");
            var choice = input.Choose("", options);
            if (choice == 0)
            {
                return true;
            }

            if (choice == options.Count - 1)
            {
                return false;
            }

            var tool = missing[choice - 1];
            if (tool.Name.StartsWith("JDK ", StringComparison.Ordinal))
            {
                var major = int.Parse(tool.Name[4..]);
                var home = input.Ask($"Đường dẫn JAVA_HOME của {tool.Name}");
                var check = await setup.CheckJdkAsync(major, home, CancellationToken.None);
                output.Status(check.Ok ? Mark.Ok : Mark.Fail, tool.Name, check.Ok ? check.Version ?? "" : check.Hint ?? "");
                if (check.Ok)
                {
                    machine = machine with { JdkHomes = new Dictionary<string, string>(machine.JdkHomes) { [major.ToString()] = check.Path! } };
                }
            }
            else
            {
                var path = input.Ask($"Đường dẫn chương trình {tool.Name}");
                machine = tool.Name switch
                {
                    "Maven" => machine with { MavenPath = path },
                    "Gradle" => machine with { GradlePath = path },
                    _ => machine with { GitPath = path },
                };
            }

            save(machine);
        }
    }

    private bool StepWorkDir(ref MachineConfig machine)
    {
        Explain(4, "Thư mục repository", "Chọn nơi tải repository và tạo workspace build", "Mỗi task cần một bản sao repository; đường dẫn có dấu cách hoặc dấu tiếng Việt làm hỏng một số công cụ build",
            "Nhập thư mục không dấu cách, không dấu tiếng Việt, còn ≥ 20 GB trống");
        while (true)
        {
            var dir = input.Ask("Thư mục repository", machine.WorkDir);
            if (dir == Back)
            {
                return false;
            }

            if (setup.ValidateWorkDir(dir) is { } error)
            {
                output.Status(Mark.Fail, dir, error);
                continue;
            }

            output.Status(Mark.Ok, dir, $"{setup.FreeSpaceGb(dir):0} GB trống");
            machine = machine with { WorkDir = dir };
            return true;
        }
    }

    private async Task<bool> StepModelAsync(MachineConfig machine, Action<MachineConfig> save)
    {
        Explain(5, "Gọi model", "Chọn cách chạy proxy LiteLLM, khai báo provider và khóa từng model", "Mọi lượt gọi model đi qua proxy; khóa chỉ nằm ở file của máy này",
            "Chọn chế độ proxy, rồi khai báo model (có thể làm sau bằng arrow-fw models)");
        var mode = input.Choose("Chế độ proxy LiteLLM:", ["auto_start (framework tự bật/tắt proxy)", "external (proxy đang chạy sẵn)", "Quay lại"],
            defaultIndex: machine.Llm.ProxyMode == "external" ? 1 : 0);
        if (mode == 2)
        {
            return false;
        }

        var url = input.Ask("Địa chỉ proxy", machine.Llm.ProxyUrl);
        machine = machine with { Llm = machine.Llm with { ProxyMode = mode == 0 ? "auto_start" : "external", ProxyUrl = url } };
        save(machine);
        if (input.Confirm("Khai báo model, provider và khóa bây giờ?", true))
        {
            await models.RunAsync(null);
        }

        return true;
    }

    private bool StepResources(ref MachineConfig machine)
    {
        Explain(6, "Tài nguyên", "Số việc chạy song song", "Build tốn CPU/RAM; gọi model bị giới hạn lượt/phút của provider", "Nhập số phù hợp với máy (mặc định đã hợp lý)");
        var build = input.AskInt("Số luồng build", machine.Workers.Build, 1, 64);
        var llm = input.AskInt("Số luồng gọi model", machine.Workers.Llm, 1, 64);
        var rpm = input.AskInt("Giới hạn lượt gọi mỗi phút (0 = không giới hạn)", machine.Llm.RpmLimit, 0, 100_000);
        machine = machine with { Workers = new WorkersConfig { Build = build, Llm = llm }, Llm = machine.Llm with { RpmLimit = rpm } };
        return true;
    }
}
