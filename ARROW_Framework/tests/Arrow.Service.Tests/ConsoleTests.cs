using Arrow.UI;

namespace Arrow.Service.Tests;

/// <summary>Tầng UI: nhập bằng ReadLine, xuất bằng WriteLine (spec 06 Mục 1). Test bằng TextReader/TextWriter giả.</summary>
public class ConsoleTests
{
    private static (ConsoleInput Input, StringWriter Output) Io(string typed)
    {
        var output = new StringWriter();
        return (new ConsoleInput(new StringReader(typed), output), output);
    }

    [Fact]
    public void ChooseRetriesUntilValidNumber()
    {
        var (input, output) = Io("abc\n5\n2\n");
        Assert.Equal(1, input.Choose("Chọn việc:", ["Chạy", "Báo cáo", "Thoát"]));
        var text = output.ToString();
        Assert.Contains("  1. Chạy", text);
        Assert.Contains("  3. Thoát", text);
        Assert.Equal(2, text.Split("[!] Nhập số từ 1 đến 3").Length - 1);
    }

    [Fact]
    public void ChooseSupportsZeroAndDefault()
    {
        Assert.Equal(-1, Io("0\n").Input.Choose("x", ["a"], zeroLabel: "Quay lại"));
        Assert.Equal(0, Io("\n").Input.Choose("x", ["a", "b"], defaultIndex: 0));
    }

    [Fact]
    public void AskUsesDefaultAndValidation()
    {
        Assert.Equal("run-1", Io("\n").Input.Ask("Run id", "run-1"));
        var (input, output) = Io("\nbad id\nok\n");
        Assert.Equal("ok", input.Ask("Run id", validate: v => v.Contains(' ') ? "Không được có dấu cách" : null));
        Assert.Contains("[!] Không được để trống", output.ToString());
        Assert.Contains("[!] Không được có dấu cách", output.ToString());
    }

    [Fact]
    public void AskIntConfirmAndSecret()
    {
        var (input, output) = Io("99\n3\n\nn\nmaybe\ny\nsk-typed-key\n");
        Assert.Equal(3, input.AskInt("Số luồng build", 4, 1, 16));
        Assert.True(input.Confirm("Bắt đầu?", defaultYes: true));
        Assert.False(input.Confirm("Ghi?", defaultYes: true));
        Assert.True(input.Confirm("Lưu?", defaultYes: false));
        Assert.Equal("sk-typed-key", input.AskSecret("Khóa API"));
        Assert.Contains("[!] Khóa sẽ hiện trên màn hình khi gõ", output.ToString());
    }

    [Fact]
    public void ClosedInputThrows()
    {
        Assert.Throws<InputClosedException>(() => Io("").Input.Ask("x"));
    }

    [Fact]
    public void OutputTableAlignsColumns()
    {
        var writer = new StringWriter();
        var output = new ConsoleOutput(writer);
        output.Title("Báo cáo");
        output.Table(["Quy trình", "n", "VALID"], [["zero_shot", "50", "14%"], ["zero_shot_project_aware", "50", "38%"]]);
        output.Status(Mark.Ok, "Model gpt-6-luna", "api.teamorouter.com");
        output.Status(Mark.Fail, "PIT");
        var lines = writer.ToString().Split('\n');
        Assert.Equal("Báo cáo", lines[0]);
        Assert.Equal("=======", lines[1]);
        Assert.Equal("Quy trình                n   VALID", lines[2]);
        Assert.Equal("zero_shot                50  14%", lines[3]);
        Assert.Equal("zero_shot_project_aware  50  38%", lines[4]);
        Assert.Equal("[OK] Model gpt-6-luna  api.teamorouter.com", lines[5]);
        Assert.Equal("[X]  PIT", lines[6]);
    }

    [Fact]
    public void CommandLineParsesCommandsOptionsAndFlags()
    {
        var cmd = CommandLine.Parse(["report", "--run-id", "A", "--run-id", "B", "--verbose", "--tasks=t1,t2", "--plain"]);
        Assert.True(cmd.Is("report"));
        Assert.Equal(["A", "B"], cmd.Options("run-id"));
        Assert.Equal("B", cmd.Option("run-id"));
        Assert.Equal("t1,t2", cmd.Option("tasks"));
        Assert.True(cmd.Flag("verbose"));
        Assert.True(cmd.Flag("plain"));
        Assert.False(cmd.Flag("rerun-infra"));

        Assert.True(CommandLine.Parse(["prompts", "check"]).Is("prompts", "check"));
        Assert.Throws<ArgumentException>(() => CommandLine.Parse(["run", "--run-id", "A", "stray"]));
    }
}
