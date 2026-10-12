using Arrow.Cli.Ui;
using Arrow.Core.Util;
using Arrow.Infrastructure.Llm;
using Arrow.Infrastructure.Tooling;
using Spectre.Console;

namespace Arrow.Cli.Screens;

/// <summary>Các việc sửa tự động dùng chung cho doctor và setup.</summary>
public static class FixActions
{
    /// <summary>Cài LiteLLM vào ARROW/litellm bằng script; hiện kết quả. Trả về true nếu cài được.</summary>
    public static async Task<bool> InstallLiteLlmAsync(Session session, CancellationToken cancellationToken)
    {
        AnsiConsole.MarkupLine("[grey]LiteLLM là chương trình trung gian để tool gọi model. Script cài bản 1.91.0 vào ARROW/litellm (môi trường Python riêng, không commit). Cần Python 3.10 trở lên và mạng; mất vài phút.[/]");
        Arrow.Infrastructure.Processes.ProcessResult? result = null;
        await AnsiConsole.Status().Spinner(Spinner.Known.Dots).StartAsync("Đang cài LiteLLM vào ARROW/litellm…", async _ =>
        {
            result = await Installers.InstallLiteLlmAsync(session.Paths, cancellationToken);
        });

        var installed = await ToolProbe.ProbeLiteLlmAsync(session.Paths.ArrowRoot, cancellationToken);
        ToolLog.Info($"cài LiteLLM: mã thoát {result!.ExitCode}, {(installed is null ? "không tìm thấy sau khi cài" : $"{installed.Version} · {installed.Path}")}");
        if (result.Succeeded && installed is not null)
        {
            AnsiConsole.MarkupLine($"{Theme.Ok} Đã cài LiteLLM {Markup.Escape(installed.Version)} · {Markup.Escape(installed.Path)}");
            return true;
        }

        AnsiConsole.MarkupLine($"{Theme.Fail} Cài LiteLLM không thành công. 20 dòng cuối:");
        var tail = result.Output.Split('\n').Where(l => l.Trim().Length > 0).TakeLast(20);
        AnsiConsole.Write(new Panel(new Text(string.Join("\n", tail))).Border(BoxBorder.Rounded).Expand());
        AnsiConsole.MarkupLine(OperatingSystem.IsWindows()
            ? "[grey]Thường do thiếu Python 3.10+: cài từ python.org (tick \"Add to PATH\"), rồi thử lại.[/]"
            : "[grey]Thường do thiếu Python 3.10+: chạy brew install python@3.12, rồi thử lại.[/]");
        return false;
    }

    /// <summary>Nhập khóa API và endpoint, lưu vào litellm.config.local.yaml (không commit).</summary>
    public static void EnterApiKey(Session session)
    {
        var path = session.Paths.LiteLlmConfigPath;
        var config = LiteLlmConfigFile.TryLoad(path) ?? new LiteLlmConfigFile();
        if (string.IsNullOrEmpty(config.MasterKey))
        {
            config = config with { MasterKey = LiteLlmConfigFile.NewMasterKey() };
        }

        AnsiConsole.MarkupLine("[grey]Khóa API của bạn lưu trong pilot_tool/litellm.config.local.yaml trên máy này, không bao giờ lên Git. Màn hình chỉ hiện ****.[/]");
        var apiBase = AnsiConsole.Prompt(new TextPrompt<string>("Endpoint của provider (api_base):")
            .DefaultValue(string.IsNullOrEmpty(config.ApiBase) ? "https://api.openai.com/v1" : config.ApiBase)
            .Validate(v => Uri.TryCreate(v, UriKind.Absolute, out _) ? ValidationResult.Success() : ValidationResult.Error("Địa chỉ không hợp lệ")));
        var key = AnsiConsole.Prompt(new TextPrompt<string>("Khóa API của bạn:").Secret()).Trim();
        config = config with { ApiBase = apiBase.Trim(), ApiKey = key };
        config.Save(path);
        ToolLog.Info($"đã lưu khóa API mới cho endpoint {config.EndpointHost}");
        AnsiConsole.MarkupLine($"{Theme.Ok} Đã lưu khóa {Markup.Escape(LiteLlmConfigFile.Mask(key))} · endpoint {Markup.Escape(config.EndpointHost)}");
    }
}
