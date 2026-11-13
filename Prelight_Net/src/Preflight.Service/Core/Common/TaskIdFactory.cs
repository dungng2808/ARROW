using System.Security.Cryptography;
using System.Text;
using Preflight.Service.Core.PyCompat;

namespace Preflight.Service.Core.Common;

/// <summary>Port của util.stable_task_id.</summary>
public static class TaskIdFactory
{
    public static string Create(string projectId, string repoUrl, string classPath, string classFqn)
    {
        string identity = string.Join('\0', projectId, repoUrl, classPath, classFqn);
        return projectId + "_" + Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(identity))).ToLowerInvariant()[..16];
    }
}
/// <summary>Port của util.normalize_java; giữ cả việc bỏ whitespace trong literal.</summary>
public static class JavaNormalizer
{
    public static string Normalize(string text)
    {
        var result = new StringBuilder();
        char state = '\0';
        for (int index = 0; index < text.Length; index++)
        {
            char current = text[index], following = index + 1 < text.Length ? text[index + 1] : '\0';
            if (state == '\0' && current == '/' && following == '/')
            {
                int end = text.IndexOf('\n', index);
                if (end < 0) break;
                index = end - 1; continue;
            }
            if (state == '\0' && current == '/' && following == '*')
            {
                int end = text.IndexOf("*/", index + 2, StringComparison.Ordinal);
                index = end < 0 ? text.Length : end + 1; continue;
            }
            if (state == '\0' && current is '\'' or '"') state = current;
            else if (state is '\'' or '"' && current == '\\')
            {
                result.Append(current);
                if (index + 1 < text.Length) result.Append(text[++index]);
                continue;
            }
            else if (state == current) state = '\0';
            if (!PyIsSpace.IsSpace(current)) result.Append(current);
        }
        return result.ToString();
    }
}
