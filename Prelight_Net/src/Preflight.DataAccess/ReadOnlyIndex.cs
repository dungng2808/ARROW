using Microsoft.Data.Sqlite;
using Preflight.Models;
using System.Text.Json;

namespace Preflight.DataAccess;

public static class ReadOnlyIndex
{
    public static IReadOnlyList<CandidateTask> Load(string path)
    {
        using var connection = new SqliteConnection(new SqliteConnectionStringBuilder { DataSource = path, Mode = SqliteOpenMode.ReadOnly, Pooling = false }.ToString()); connection.Open();
        using var check = connection.CreateCommand(); check.CommandText = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='normalized_tasks'";
        if (Convert.ToInt64(check.ExecuteScalar(), System.Globalization.CultureInfo.InvariantCulture) > 0)
        {
            using var command = connection.CreateCommand(); command.CommandText = "SELECT task_json FROM normalized_tasks ORDER BY task_id"; using var rows = command.ExecuteReader(); var values = new List<CandidateTask>();
            while (rows.Read()) values.Add(JsonSerializer.Deserialize<CandidateTask>(rows.GetString(0), PythonJson.Options)!);
            if (values.Count > 0) return values;
        }
        var candidates = new List<CandidateTask>();
        using var classes = connection.CreateCommand(); classes.CommandText = "SELECT task_id,project_id,repo_url,class_path,class_fqn,class_name FROM classes ORDER BY project_id,task_id";
        using var reader = classes.ExecuteReader();
        while (reader.Read())
        {
            using var evidence = connection.CreateCommand(); evidence.CommandText = "SELECT source_json_path,source_json_sha256,test_class_path FROM evidence WHERE task_id=$id ORDER BY source_json_path"; evidence.Parameters.AddWithValue("$id", reader.GetString(0));
            var paths = new List<string>(); var hashes = new List<string>(); var tests = new HashSet<string>(StringComparer.Ordinal); using var er = evidence.ExecuteReader();
            while (er.Read()) { paths.Add(er.GetString(0)); hashes.Add(er.GetString(1)); if (er.GetString(2).Length > 0) tests.Add(er.GetString(2)); }
            var c = new ClassCandidate(reader.GetString(0), reader.GetString(1), reader.GetString(2), reader.GetString(3), reader.GetString(4), reader.GetString(5), paths, hashes, tests.Order(PythonCodePointComparer.Instance).ToArray());
            candidates.Add(new(c, "classes2test", new(c.RepoUrl, c.RepoUrl), null, new("dataset_record")));
        }
        return candidates;
    }
}
