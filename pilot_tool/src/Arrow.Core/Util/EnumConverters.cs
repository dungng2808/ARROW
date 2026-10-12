using System.Text.Json;
using System.Text.Json.Serialization;

namespace Arrow.Core.Util;

/// <summary>Enum ghi dạng SNAKE_CASE viết hoa, ví dụ <c>DONE</c>, <c>RUNNING</c>.</summary>
public sealed class UpperSnakeEnumConverter<TEnum>() : JsonStringEnumConverter<TEnum>(JsonNamingPolicy.SnakeCaseUpper)
    where TEnum : struct, Enum;

/// <summary>Enum ghi dạng snake_case viết thường, ví dụ <c>commit_only_delete_after</c>.</summary>
public sealed class LowerSnakeEnumConverter<TEnum>() : JsonStringEnumConverter<TEnum>(JsonNamingPolicy.SnakeCaseLower)
    where TEnum : struct, Enum;
