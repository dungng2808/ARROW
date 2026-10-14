using System.Text;
using System.Xml.Linq;
using Arrow.Models.Block3;
using Arrow.Service.Interfaces;

namespace Arrow.Service.Block3;

/// <summary>
/// Ghi một bộ test vào bản sao workspace (spec 03 Mục 3.0, 9; spec 04 Mục 4.1 bước 1): mọi file của <see cref="SuiteFiles"/> và classpath
/// phụ (Maven: dependency scope <c>system</c> trong <c>pom.xml</c> của module đích; Gradle: init script phụ). Dùng chung cho khối 3 và 4.
/// </summary>
public static class SuiteWriter
{
    public const string ExtraInitScriptName = "arrow-extra-classpath.gradle";

    /// <summary>Ghi file và classpath phụ; trả đối số thêm cho lệnh build (Gradle: <c>--init-script</c>).</summary>
    public static async Task<IReadOnlyList<string>> WriteAsync(string copy, SuiteFiles suite, TaskContext task, CancellationToken cancellationToken)
    {
        foreach (var (path, content) in suite.Files)
        {
            var target = Path.Combine(copy, path.Replace('/', Path.DirectorySeparatorChar));
            Directory.CreateDirectory(Path.GetDirectoryName(target)!);
            await File.WriteAllTextAsync(target, content.Replace("\r\n", "\n"), new UTF8Encoding(false), cancellationToken).ConfigureAwait(false);
        }

        if (suite.ExtraTestClasspath.Count == 0)
        {
            return [];
        }

        if (task.Workspace.Tool == "maven")
        {
            AddMavenSystemDependencies(Path.Combine(copy, task.Metadata.ProjectStructure.ModuleRoot.Replace('/', Path.DirectorySeparatorChar), "pom.xml"), suite.ExtraTestClasspath);
            return [];
        }

        var files = string.Join(", ", suite.ExtraTestClasspath.Select(j => "'" + Path.GetFullPath(j).Replace("\\", "/", StringComparison.Ordinal) + "'"));
        var project = task.Workspace.Module ?? ":";
        var script = Path.Combine(copy, ExtraInitScriptName);
        await File.WriteAllTextAsync(script, $$"""
            allprojects { p ->
                p.afterEvaluate {
                    if (p.path == '{{project}}') { p.dependencies { testImplementation p.files({{files}}) } }
                }
            }
            """, new UTF8Encoding(false), cancellationToken).ConfigureAwait(false);
        return ["--init-script", script];
    }

    /// <summary>
    /// Maven: thêm jar của classpath phụ vào <c>pom.xml</c> của module đích (chỉ trong bản sao) dưới dạng dependency scope <c>system</c>
    /// (Maven không có cách khác để trỏ một dependency tới file jar mà không cài vào <c>~/.m2</c>).
    /// </summary>
    public static void AddMavenSystemDependencies(string pomPath, IReadOnlyList<string> jars)
    {
        var document = XDocument.Load(pomPath, LoadOptions.PreserveWhitespace);
        var project = document.Root!;
        var ns = project.Name.Namespace;
        var dependencies = project.Element(ns + "dependencies");
        if (dependencies is null)
        {
            dependencies = new XElement(ns + "dependencies");
            project.Add(dependencies);
        }

        foreach (var (jar, index) in jars.Select((j, i) => (j, i)))
        {
            dependencies.Add(new XElement(ns + "dependency",
                new XElement(ns + "groupId", "arrow.extra"),
                new XElement(ns + "artifactId", $"extra-{index}-{Path.GetFileNameWithoutExtension(jar)}"),
                new XElement(ns + "version", "0"),
                new XElement(ns + "scope", "system"),
                new XElement(ns + "systemPath", Path.GetFullPath(jar))));
        }

        File.WriteAllText(pomPath, document.Declaration + document.ToString(SaveOptions.DisableFormatting), new UTF8Encoding(false));
    }
}
