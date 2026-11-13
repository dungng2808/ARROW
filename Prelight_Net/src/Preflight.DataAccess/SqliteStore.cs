using Microsoft.Data.Sqlite;

namespace Preflight.DataAccess;

/// <summary>SQLite cục bộ, transaction và WAL cho index/checkpoint.</summary>
public abstract class SqliteStore : IDisposable
{
    protected readonly SqliteConnection Connection;
    protected readonly object Gate = new();
    protected SqliteStore(string path)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(path))!);
        Connection = new SqliteConnection(new SqliteConnectionStringBuilder { DataSource = path, DefaultTimeout = 30, Pooling = false }.ToString());
        Connection.Open(); Execute("PRAGMA journal_mode=WAL; PRAGMA synchronous=FULL; PRAGMA foreign_keys=ON;");
    }
    protected int Execute(string sql, params object?[] args)
    {
        using var command = Connection.CreateCommand(); command.CommandText = sql;
        for (int i = 0; i < args.Length; i++) command.Parameters.AddWithValue("$" + i, args[i] ?? DBNull.Value);
        return command.ExecuteNonQuery();
    }
    protected List<object?[]> Query(string sql, params object?[] args)
    {
        using var command = Connection.CreateCommand(); command.CommandText = sql;
        for (int i = 0; i < args.Length; i++) command.Parameters.AddWithValue("$" + i, args[i] ?? DBNull.Value);
        using var reader = command.ExecuteReader(); var rows = new List<object?[]>();
        while (reader.Read()) { var values = new object[reader.FieldCount]; reader.GetValues(values); rows.Add(values.Select(v => v == DBNull.Value ? null : v).ToArray()); }
        return rows;
    }
    public void Dispose() => Connection.Dispose();
}
