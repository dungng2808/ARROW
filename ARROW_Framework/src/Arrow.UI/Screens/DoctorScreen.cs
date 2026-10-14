using Arrow.Service.Interfaces;

namespace Arrow.UI.Screens;

/// <summary>S4 (spec 06 Mục 6): in từng dòng ngay khi kiểm xong một mục; cuối cùng READY / NOT READY và đề nghị tự sửa.</summary>
public sealed class DoctorScreen(IDoctorService doctor, ConsoleInput input, ConsoleOutput output, bool interactive)
{
    public async Task<int> RunAsync(string? experimentPath, bool fixWithoutAsking = false)
    {
        output.Title("Kiểm tra môi trường");
        var report = await doctor.RunAsync(experimentPath, Print, CancellationToken.None);
        output.Line(report.Ready ? "Trạng thái: READY" : $"Trạng thái: NOT READY ({report.Errors} lỗi)");
        if (report.Lines.Any(l => l.AutoFixable && l.Mark == DoctorMark.Fail)
            && (fixWithoutAsking || interactive && input.Confirm("Sửa ngay các lỗi tự sửa được?", true)))
        {
            await doctor.FixAsync(Print, CancellationToken.None);
            output.Line("Chạy lại arrow doctor để kiểm tra lại.");
        }

        return report.Ready ? 0 : 1;
    }

    private void Print(DoctorLine line)
    {
        var mark = line.Mark switch { DoctorMark.Ok => Mark.Ok, DoctorMark.Warn => Mark.Warn, _ => Mark.Fail };
        output.Line($"{ConsoleOutput.MarkText(mark),-4} {line.Item,-32} {line.Detail}");
        if (line.Fix is not null && line.Mark != DoctorMark.Ok)
        {
            output.Line($"     -> Cách sửa: {line.Fix}");
        }
    }
}
