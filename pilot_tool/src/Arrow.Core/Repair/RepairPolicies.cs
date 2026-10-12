using System.Text.Json.Serialization;
using Arrow.Core.Util;
using Arrow.Core.Verify;

namespace Arrow.Core.Repair;

[JsonConverter(typeof(UpperSnakeEnumConverter<Comparison>))]
public enum Comparison
{
    Better,
    Worse,
    Tie,
}

/// <summary>Bản test đã build: những gì cần để so sánh và chọn prompt (specs/05 Mục 3).</summary>
public sealed record BuiltCandidate
{
    /// <summary>Vòng tạo ra bản này (c0 = 0).</summary>
    public int Round { get; init; }
    public VerificationState State { get; init; }
    public string? Signature { get; init; }
    public int TestsPassed { get; init; }
    public int AssertionCount { get; init; }
    public string CodeHash { get; init; } = "";
    public string Code { get; init; } = "";

    /// <summary>Method lỗi để dùng ở mức 3 (COMPILE: method chứa lỗi; RUNTIME/ASSERTION: method fail).</summary>
    public List<string> FailingMethods { get; init; } = [];

    public static BuiltCandidate From(int round, string code, VerifyResult verify) => new()
    {
        Round = round,
        State = verify.State,
        Signature = verify.ErrorSignature,
        TestsPassed = verify.TestMethods.Passed,
        AssertionCount = verify.AssertionCount ?? 0,
        CodeHash = verify.CodeHash ?? "",
        Code = code,
        FailingMethods = verify.FailingMethods.ToList(),
    };
}

/// <summary>So sánh hai bản theo thứ tự: bước xác minh → số test method pass → số assertion (vòng sớm hơn thắng khi bằng).</summary>
public static class CheckpointComparer
{
    public static Comparison Compare(BuiltCandidate candidate, BuiltCandidate checkpoint)
    {
        var byState = States.Rank(candidate.State).CompareTo(States.Rank(checkpoint.State));
        if (byState != 0)
        {
            return byState > 0 ? Comparison.Better : Comparison.Worse;
        }

        var byPassed = candidate.TestsPassed.CompareTo(checkpoint.TestsPassed);
        if (byPassed != 0)
        {
            return byPassed > 0 ? Comparison.Better : Comparison.Worse;
        }

        var byAssertions = candidate.AssertionCount.CompareTo(checkpoint.AssertionCount);
        return byAssertions == 0 ? Comparison.Tie : byAssertions > 0 ? Comparison.Better : Comparison.Worse;
    }
}

/// <summary>Một mục của "cách đã thử và thất bại" (specs/04 Mục 6.2).</summary>
public sealed record FailedAttempt(int Round, string Outcome)
{
    public static string Describe(VerificationState state, string? signature) => $"{States.Name(state)} — {signature ?? "-"}";
    public const string Duplicate = "duplicate code (same as an earlier attempt)";
    public const string Invalid = "invalid output (not a valid test class)";
}

/// <summary>Prompt cần gửi ở vòng tới.</summary>
public sealed record PromptPlan
{
    /// <summary>Bản được đem đi sửa (Adaptive: checkpoint; Fixed: bản mới nhất).</summary>
    public required BuiltCandidate Base { get; init; }
    public int RequestedLevel { get; init; }
    public int Level { get; init; }

    /// <summary><c>level1</c>, <c>level2.compile</c>, <c>level3.runtime</c>…</summary>
    public string PromptKind { get; init; } = "level1";
    public List<string> FailingMethods { get; init; } = [];
    public List<FailedAttempt> Attempts { get; init; } = [];
}

/// <summary>Sự kiện của một vòng sửa, đủ để dựng lại trạng thái nhánh (specs/05 Mục 2.3).</summary>
public sealed record RoundEvent
{
    public int Round { get; init; }
    public bool InvalidOutput { get; init; }
    public bool DuplicateCode { get; init; }

    /// <summary>Bản đã build (null nếu output không hợp lệ hoặc mã trùng).</summary>
    public BuiltCandidate? Built { get; init; }

    /// <summary>Hash của mã (cả khi mã trùng; null khi output không hợp lệ).</summary>
    public string? CodeHash { get; init; }
}

/// <summary><c>round_&lt;t&gt;/decision.json</c> (specs/05 Mục 7.1).</summary>
public sealed record RepairDecision
{
    public string AttemptId { get; init; } = "";
    public int Round { get; init; }
    public string Mechanism { get; init; } = "";
    public string RunOrder { get; init; } = "";
    public int? BaseRound { get; init; }
    public int RequestedLevel { get; init; }
    public int Level { get; init; }
    public string PromptKind { get; init; } = "";
    public VerificationState? State { get; init; }
    public string? Group { get; init; }
    public string? ErrorSignature { get; init; }
    public string? CodeHash { get; init; }
    public bool DuplicateCode { get; init; }
    public bool InvalidOutput { get; init; }
    public Comparison? Comparison { get; init; }
    public bool Improved { get; init; }
    public bool Rollback { get; init; }
    public bool RepeatedSignature { get; init; }
    public List<string> Level3Replaced { get; init; } = [];
    public List<string> Level3Dropped { get; init; } = [];
    public int LevelAfter { get; init; }
    public int? CheckpointRound { get; init; }
    public int? TestsPassed { get; init; }
    public int? AssertionCount { get; init; }
    public double LlmSeconds { get; init; }
    public double BuildSeconds { get; init; }
    public int LlmRetries { get; init; }
    public Dictionary<string, long> LlmTokens { get; init; } = [];
    public bool Success { get; init; }

    /// <summary>Sự kiện gốc để dựng lại nhánh khi chạy tiếp.</summary>
    public RoundEvent? Event { get; init; }
}

/// <summary>Một cơ chế sửa: chọn prompt cho vòng tới và cập nhật trạng thái sau mỗi vòng. Code thuần.</summary>
public interface IRepairPolicy
{
    string Mechanism { get; }
    bool Succeeded { get; }

    /// <summary>Bản kết quả của nhánh: Adaptive là checkpoint, Fixed là bản mới nhất.</summary>
    BuiltCandidate Result { get; }
    PromptPlan NextPrompt();

    /// <summary>Có kiểm tra mã trùng trước khi build không (Adaptive: có; Fixed: không, mã trùng vẫn build).</summary>
    bool SkipsDuplicates { get; }
    bool IsDuplicate(string codeHash);
    RepairDecision Apply(RoundEvent roundEvent, PromptPlan plan);
}

/// <summary>Fixed Repair (specs/05 Mục 4): một prompt mức 1, luôn sửa từ bản mới nhất, chỉ dừng khi VALID hoặc hết N.</summary>
public sealed class FixedRepairPolicy(BuiltCandidate c0) : IRepairPolicy
{
    private BuiltCandidate _current = c0;

    public string Mechanism => "fixed";
    public bool Succeeded { get; private set; }
    public bool SkipsDuplicates => false;
    public BuiltCandidate Current => _current;
    public BuiltCandidate Result => _current;

    public bool IsDuplicate(string codeHash) => false;

    public PromptPlan NextPrompt() => new() { Base = _current, RequestedLevel = 1, Level = 1, PromptKind = "level1" };

    public RepairDecision Apply(RoundEvent e, PromptPlan plan)
    {
        if (e.Built is null)
        {
            // Output không hợp lệ: tính một vòng, giữ nguyên bản mới nhất.
            return new RepairDecision
            {
                Round = e.Round, Mechanism = Mechanism, BaseRound = _current.Round, RequestedLevel = 1, Level = 1, PromptKind = "level1",
                InvalidOutput = e.InvalidOutput, DuplicateCode = e.DuplicateCode, CodeHash = e.CodeHash, LevelAfter = 1, Event = e,
            };
        }

        var comparison = CheckpointComparer.Compare(e.Built, _current);
        var previous = _current;
        _current = e.Built;
        Succeeded = e.Built.State == VerificationState.Valid;
        return new RepairDecision
        {
            Round = e.Round,
            Mechanism = Mechanism,
            BaseRound = previous.Round,
            RequestedLevel = 1,
            Level = 1,
            PromptKind = "level1",
            State = e.Built.State,
            Group = States.Group(e.Built.State),
            ErrorSignature = e.Built.Signature,
            CodeHash = e.Built.CodeHash,
            Comparison = comparison,
            Improved = comparison == Comparison.Better,
            RepeatedSignature = e.Built.Signature is not null && e.Built.Signature == previous.Signature,
            LevelAfter = 1,
            TestsPassed = e.Built.TestsPassed,
            AssertionCount = e.Built.AssertionCount,
            Success = Succeeded,
            Event = e,
        };
    }
}

/// <summary>
/// Adaptive Repair (specs/05 Mục 5, AR 4): prompt theo trạng thái của checkpoint và mức 1–3; checkpoint, rollback,
/// mã trùng theo hash, lỗi lặp theo signature; "đã thử" không bị xóa khi đổi checkpoint.
/// </summary>
public sealed class AdaptiveRepairPolicy : IRepairPolicy
{
    private static readonly HashSet<VerificationState> Level3States = [VerificationState.Compile, VerificationState.Runtime, VerificationState.Assertion];

    private readonly HashSet<string> _seen = new(StringComparer.Ordinal);
    private readonly List<FailedAttempt> _attempts = [];

    public AdaptiveRepairPolicy(BuiltCandidate c0)
    {
        Best = c0;
        if (c0.CodeHash.Length > 0)
        {
            _seen.Add(c0.CodeHash);
        }
    }

    public string Mechanism => "adaptive";
    public bool Succeeded { get; private set; }
    public bool SkipsDuplicates => true;
    public BuiltCandidate Best { get; private set; }
    public BuiltCandidate Result => Best;
    public int Level { get; private set; } = 1;
    public IReadOnlyList<FailedAttempt> Attempts => _attempts;

    public bool IsDuplicate(string codeHash) => _seen.Contains(codeHash);

    public PromptPlan NextPrompt()
    {
        var state = States.Name(Best.State).ToLowerInvariant();
        var level = Level;
        if (Level == 3 && (!Level3States.Contains(Best.State) || Best.FailingMethods.Count == 0))
        {
            level = 2; // mức 3 chỉ cho COMPILE, RUNTIME, ASSERTION khi xác định được method lỗi
        }

        return new PromptPlan
        {
            Base = Best,
            RequestedLevel = Level,
            Level = level,
            PromptKind = level == 1 ? "level1" : $"level{level}.{state}",
            FailingMethods = level == 3 ? Best.FailingMethods : [],
            Attempts = [.. _attempts],
        };
    }

    public RepairDecision Apply(RoundEvent e, PromptPlan plan)
    {
        var decision = new RepairDecision
        {
            Round = e.Round,
            Mechanism = Mechanism,
            BaseRound = Best.Round,
            RequestedLevel = plan.RequestedLevel,
            Level = plan.Level,
            PromptKind = plan.PromptKind,
            CodeHash = e.CodeHash,
            Event = e,
        };

        if (e.InvalidOutput || e.DuplicateCode)
        {
            _attempts.Add(new FailedAttempt(e.Round, e.InvalidOutput ? FailedAttempt.Invalid : FailedAttempt.Duplicate));
            Level = Math.Min(Level + 1, 3);
            return decision with { InvalidOutput = e.InvalidOutput, DuplicateCode = e.DuplicateCode, LevelAfter = Level, CheckpointRound = Best.Round };
        }

        var built = e.Built ?? throw new ArgumentException("Vòng đã build phải có kết quả");
        _seen.Add(built.CodeHash);
        decision = decision with
        {
            State = built.State,
            Group = States.Group(built.State),
            ErrorSignature = built.Signature,
            CodeHash = built.CodeHash,
            TestsPassed = built.TestsPassed,
            AssertionCount = built.AssertionCount,
        };
        if (built.State == VerificationState.Valid)
        {
            Succeeded = true;
            var previous = Best;
            Best = built;
            return decision with { Comparison = Comparison.Better, Improved = true, Success = true, LevelAfter = Level, CheckpointRound = built.Round, BaseRound = previous.Round };
        }

        var comparison = CheckpointComparer.Compare(built, Best);
        var repeated = built.Signature is not null && built.Signature == Best.Signature;
        if (comparison == Comparison.Better)
        {
            if (built.State != Best.State)
            {
                Level = 1; // lỗi đổi loại
            }

            Best = built;
            return decision with { Comparison = comparison, Improved = true, RepeatedSignature = repeated, LevelAfter = Level, CheckpointRound = Best.Round };
        }

        _attempts.Add(new FailedAttempt(e.Round, FailedAttempt.Describe(built.State, built.Signature)));
        if (comparison == Comparison.Worse || repeated)
        {
            Level = Math.Min(Level + 1, 3); // rollback hoặc lỗi lặp → leo mức
        }

        return decision with { Comparison = comparison, Rollback = comparison == Comparison.Worse, RepeatedSignature = repeated, LevelAfter = Level, CheckpointRound = Best.Round };
    }

    /// <summary>Dựng lại trạng thái từ các sự kiện đã ghi (chạy tiếp sau khi dừng giữa chừng).</summary>
    public static AdaptiveRepairPolicy Replay(BuiltCandidate c0, IEnumerable<RoundEvent> events)
    {
        var policy = new AdaptiveRepairPolicy(c0);
        foreach (var e in events.OrderBy(e => e.Round))
        {
            policy.Apply(e, policy.NextPrompt());
        }

        return policy;
    }
}

/// <summary>Dựng lại Fixed từ sự kiện đã ghi.</summary>
public static class RepairReplay
{
    public static IRepairPolicy Create(string mechanism, BuiltCandidate c0, IEnumerable<RoundEvent> events)
    {
        if (mechanism == "adaptive")
        {
            return AdaptiveRepairPolicy.Replay(c0, events);
        }

        var policy = new FixedRepairPolicy(c0);
        foreach (var e in events.OrderBy(e => e.Round))
        {
            policy.Apply(e, policy.NextPrompt());
        }

        return policy;
    }
}

/// <summary><c>&lt;mechanism&gt;/summary.json</c> (specs/05 Mục 7.2).</summary>
public sealed record RepairSummary
{
    public string AttemptId { get; init; } = "";
    public string Mechanism { get; init; } = "";
    public string RunOrder { get; init; } = "";
    public VerificationState C0State { get; init; }
    public VerificationState? FinalState { get; init; }
    public bool Success { get; init; }
    public int? SuccessRound { get; init; }
    public int RoundsUsed { get; init; }
    public int NMax { get; init; }
    public double RepairSeconds { get; init; }
    public List<string> DroppedMethods { get; init; } = [];
    public int Rollbacks { get; init; }
    public int DuplicateCode { get; init; }
    public int InvalidOutput { get; init; }
    public int RepeatedSignature { get; init; }
    public int LevelUps { get; init; }
    public int StateChanges { get; init; }
    public int Level3Rounds { get; init; }
    public long PromptTokens { get; init; }
    public long CompletionTokens { get; init; }

    /// <param name="finalState">Adaptive: trạng thái của checkpoint cuối; Fixed: của bản mới nhất.</param>
    public static RepairSummary From(string attemptId, string mechanism, string runOrder, BuiltCandidate c0, IReadOnlyList<RepairDecision> rounds,
        int nMax, VerificationState finalState, IReadOnlyList<string> c0Methods, IReadOnlyList<string> finalMethods)
    {
        var success = rounds.FirstOrDefault(r => r.Success);
        var levelUps = 0;
        var stateChanges = 0;
        var previousLevel = 1;
        VerificationState? previousState = c0.State;
        foreach (var round in rounds)
        {
            if (round.LevelAfter > previousLevel)
            {
                levelUps++;
            }

            previousLevel = round.LevelAfter;
            if (round.State is { } state && state != previousState)
            {
                stateChanges++;
                previousState = state;
            }
        }

        return new RepairSummary
        {
            AttemptId = attemptId,
            Mechanism = mechanism,
            RunOrder = runOrder,
            C0State = c0.State,
            FinalState = success is not null ? VerificationState.Valid : finalState,
            Success = success is not null,
            SuccessRound = success?.Round,
            RoundsUsed = rounds.Count,
            NMax = nMax,
            RepairSeconds = Math.Round(rounds.Sum(r => r.LlmSeconds + r.BuildSeconds), 2),
            DroppedMethods = c0Methods.Except(finalMethods).ToList(),
            Rollbacks = rounds.Count(r => r.Rollback),
            DuplicateCode = rounds.Count(r => r.DuplicateCode),
            InvalidOutput = rounds.Count(r => r.InvalidOutput),
            RepeatedSignature = rounds.Count(r => r.RepeatedSignature),
            LevelUps = levelUps,
            StateChanges = stateChanges,
            Level3Rounds = rounds.Count(r => r.Level == 3),
            PromptTokens = rounds.Sum(r => r.LlmTokens.GetValueOrDefault("prompt")),
            CompletionTokens = rounds.Sum(r => r.LlmTokens.GetValueOrDefault("completion")),
        };
    }
}
