using Arrow.Models.Run;
using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>
/// S5 (spec 06 Mục 7): chạy run ở luồng nền, in mỗi sự kiện một dòng và mỗi 30 giây một dòng tổng; luồng chính đọc lệnh
/// <c>p</c>/<c>r</c>/<c>s</c>/<c>t</c>/<c>h</c> bằng <c>ReadLine</c>. Input hoặc output bị chuyển hướng thì không chờ lệnh.
/// </summary>
public sealed class RunScreen(IPipelineService pipeline, ConsoleInput input, ConsoleOutput output, bool interactive)
{
    public static readonly TimeSpan TotalEvery = TimeSpan.FromSeconds(30);

    private sealed class LineProgress(ConsoleOutput output) : IProgress<RunEvent>
    {
        public void Report(RunEvent value) => output.Line(Format.Event(value));
    }

    /// <summary>Chạy tới khi run kết thúc. Mã thoát: 0 xong, 130 dừng, 1 lỗi.</summary>
    public async Task<int> RunAsync(RunRequest request)
    {
        var handle = pipeline.Start(request, new LineProgress(output));
        output.Line($"Run {handle.RunId} · khối {string.Join(',', request.Blocks)}{(request.FakeLlm ? " · model giả" : "")}");
        if (interactive)
        {
            output.Line("Lệnh trong khi chạy (gõ rồi Enter): p = tạm dừng, r = tiếp tục, s = dừng an toàn, t = in tổng, h = trợ giúp");
        }

        void OnCancel(object? sender, ConsoleCancelEventArgs e)
        {
            e.Cancel = true;
            output.Status(Mark.Warn, "Đang dừng an toàn", "job đã ghi ledger được giữ; chạy lại cùng lệnh để tiếp tục");
            pipeline.Stop();
        }

        Console.CancelKeyPress += OnCancel;
        using var totals = new CancellationTokenSource();
        var totalLoop = PrintTotalsAsync(totals.Token);
        try
        {
            if (interactive)
            {
                await ReadCommandsAsync(handle.Completion).ConfigureAwait(false);
            }

            var result = await handle.Completion.ConfigureAwait(false);
            await totals.CancelAsync().ConfigureAwait(false);
            await totalLoop.ConfigureAwait(false);
            output.Blank();
            output.Line($"---- Tổng ({DateTime.Now:HH:mm:ss}) ----");
            Format.Summary(output, result.Status);
            output.Line("-------------------------");
            return Finish(result);
        }
        finally
        {
            Console.CancelKeyPress -= OnCancel;
            await totals.CancelAsync().ConfigureAwait(false);
        }
    }

    private async Task PrintTotalsAsync(CancellationToken token)
    {
        try
        {
            while (true)
            {
                await Task.Delay(TotalEvery, token).ConfigureAwait(false);
                output.Line(Format.TotalLine(pipeline.GetSummary()));
            }
        }
        catch (OperationCanceledException)
        {
        }
    }

    private async Task ReadCommandsAsync(Task<RunResult> completion)
    {
        while (!completion.IsCompleted)
        {
            var read = Task.Run(input.TryReadLine);
            if (await Task.WhenAny(read, completion).ConfigureAwait(false) == completion)
            {
                return;
            }

            var line = await read.ConfigureAwait(false);
            if (line is null)
            {
                return;
            }

            switch (line.Trim().ToLowerInvariant())
            {
                case "p":
                    pipeline.Pause();
                    output.Status(Mark.Warn, "Đã tạm dừng", "bước đang chạy chạy nốt; gõ r để tiếp tục");
                    break;
                case "r":
                    pipeline.Resume();
                    output.Status(Mark.Ok, "Tiếp tục");
                    break;
                case "s":
                    output.Line("Dừng hẳn? (y/n) [n]:");
                    var answer = await Task.Run(input.TryReadLine).ConfigureAwait(false);
                    if (answer?.Trim().ToLowerInvariant() is "y" or "yes" or "c" or "co" or "có")
                    {
                        output.Status(Mark.Warn, "Đang dừng an toàn", "hủy bước đang chạy; chạy lại cùng lệnh để tiếp tục");
                        pipeline.Stop();
                    }

                    break;
                case "t":
                    output.Line($"---- Tổng ({DateTime.Now:HH:mm:ss}) ----");
                    Format.Summary(output, pipeline.GetSummary());
                    output.Line("-------------------------");
                    break;
                case "h":
                case "?":
                    output.Line("p = tạm dừng, r = tiếp tục, s = dừng an toàn, t = in tổng, h = trợ giúp; Ctrl+C = dừng ngay");
                    break;
                case "":
                    break;
                default:
                    output.Status(Mark.Warn, $"Không hiểu lệnh '{line.Trim()}'", "gõ h để xem danh sách");
                    break;
            }
        }
    }

    private int Finish(RunResult result)
    {
        switch (result.State)
        {
            case RunState.Done:
                output.Status(Mark.Ok, $"Run {result.RunId} xong");
                output.Line($"Tiếp theo: arrow report --run-id {result.RunId}   ·   arrow rerun-list --run-id {result.RunId}");
                return 0;
            case RunState.Stopped:
                output.Status(Mark.Warn, $"Run {result.RunId} đã dừng", $"chạy tiếp: arrow run --run-id {result.RunId}");
                return 130;
            default:
                output.Status(Mark.Fail, $"Run {result.RunId} lỗi", result.Error ?? "");
                return 1;
        }
    }
}
