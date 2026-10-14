using System.Reflection;

namespace Arrow.Service.Tests;

/// <summary>Canh hướng phụ thuộc của kiến trúc 3 tầng (spec 07 Mục 1): UI → Service → Data Access; cả ba → Models.</summary>
public class ArchitectureTests
{
    private static IReadOnlySet<string> References(Assembly assembly) =>
        assembly.GetReferencedAssemblies().Select(a => a.Name!).Where(n => n.StartsWith("Arrow.", StringComparison.Ordinal)).ToHashSet();

    [Fact]
    public void UiUsesOnlyServiceAndModels()
    {
        var ui = References(typeof(Arrow.UI.ConsoleInput).Assembly);
        Assert.Contains("Arrow.Service", ui);
        Assert.DoesNotContain("Arrow.DataAccess", ui);
    }

    [Fact]
    public void ServiceUsesDataAccessButNotUi()
    {
        var service = References(typeof(ServiceFactory).Assembly);
        Assert.Contains("Arrow.DataAccess", service);
        Assert.Contains("Arrow.Models", service);
        Assert.DoesNotContain(service, n => n.StartsWith("arrow", StringComparison.Ordinal));
    }

    [Fact]
    public void DataAccessUsesOnlyModelsAndModelsUsesNothing()
    {
        var dataAccess = References(typeof(Arrow.DataAccess.ResultPaths).Assembly);
        Assert.Equal(["Arrow.Models"], dataAccess.ToList());
        Assert.Empty(References(typeof(Arrow.Models.Common.ArrowException).Assembly));
    }
}
