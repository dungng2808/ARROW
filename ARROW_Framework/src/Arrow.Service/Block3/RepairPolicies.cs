using Arrow.Models.Block2;
using Arrow.Models.Block3;
using Arrow.Models.Common;
using Arrow.Service.Block2;

namespace Arrow.Service.Block3;

/// <summary>Một bản test trong chain (c0 là vòng 0): mã, kết quả xác minh và chẩn đoán.</summary>
public sealed record ChainVersion(int Round, CandidateTest Candidate, VerifyResult Verify, Diagnosis Diagnosis)
{
    public string Code => Candidate.Code ?? "";
    public VerificationState State => Verify.State;
}

/// <summary>Kết quả một vòng sửa đưa vào policy: request đã gửi, candidate nhận về, và (nếu đã build) kết quả xác minh.</summary>
public sealed record RoundOutcome
{
    public int Round { get; init; }
    public RepairRequest Request { get; init; } = new();
    public CandidateTest Candidate { get; init; } = new();

    /// <summary>Vòng có cùng code hash (chỉ Adaptive kiểm tra); khi khác null thì bản này không được build.</summary>
    public int? DuplicateOf { get; init; }
    public VerifyResult? Verify { get; init; }
    public Diagnosis? Diagnosis { get; init; }
}

/// <summary>Spec 03 Mục 6.3: so sánh bản mới với checkpoint theo bước xác minh → số test pass → số assertion; bằng nhau thì giữ bản cũ.</summary>
public static class CheckpointComparer
{
    public const string Better = "BETTER";
    public const string Worse = "WORSE";
    public const string Equal = "EQUAL";

    public static string Compare(VerifyResult candidate, VerifyResult checkpoint)
    {
        var byState = ((int)candidate.State).CompareTo((int)checkpoint.State);
        if (byState != 0)
        {
            return byState > 0 ? Better : Worse;
        }

        var byPassed = candidate.TestMethods.Passed.CompareTo(checkpoint.TestMethods.Passed);
        if (byPassed != 0)
        {
            return byPassed > 0 ? Better : Worse;
        }

        var byAssertions = candidate.AssertionCount.CompareTo(checkpoint.AssertionCount);
        return byAssertions > 0 ? Better : byAssertions < 0 ? Worse : Equal;
    }
}

/// <summary>Dòng mã đã đổi so với bản được sửa (trường <c>change</c> của danh sách đã thử, spec 03 Mục 6.4).</summary>
public static class LineDiff
{
    public const int MaxLines = 15;

    /// <summary><c>-</c> dòng bị bỏ, <c>+</c> dòng thêm, theo thứ tự trong file, không dòng ngữ cảnh; tối đa 15 dòng.</summary>
    public static string? Changes(string before, string after, int maxLines = MaxLines)
    {
        var a = Lines(before);
        var b = Lines(after);

        // Bỏ phần đầu và phần cuối giống nhau, LCS trên phần giữa.
        var prefix = 0;
        while (prefix < a.Length && prefix < b.Length && a[prefix] == b[prefix])
        {
            prefix++;
        }

        var suffix = 0;
        while (suffix < a.Length - prefix && suffix < b.Length - prefix && a[a.Length - 1 - suffix] == b[b.Length - 1 - suffix])
        {
            suffix++;
        }

        var x = a[prefix..(a.Length - suffix)];
        var y = b[prefix..(b.Length - suffix)];
        var lcs = new int[x.Length + 1, y.Length + 1];
        for (var i = x.Length - 1; i >= 0; i--)
        {
            for (var j = y.Length - 1; j >= 0; j--)
            {
                lcs[i, j] = x[i] == y[j] ? lcs[i + 1, j + 1] + 1 : Math.Max(lcs[i + 1, j], lcs[i, j + 1]);
            }
        }

        var changes = new List<string>();
        int p = 0, q = 0;
        while (p < x.Length || q < y.Length)
        {
            if (p < x.Length && q < y.Length && x[p] == y[q])
            {
                p++;
                q++;
            }
            else if (p < x.Length && (q == y.Length || lcs[p + 1, q] >= lcs[p, q + 1]))
            {
                changes.Add("-" + x[p++]);
            }
            else
            {
                changes.Add("+" + y[q++]);
            }
        }

        if (changes.Count == 0)
        {
            return null;
        }

        var shown = changes.Take(maxLines).ToList();
        if (changes.Count > maxLines)
        {
            shown.Add($"… ({changes.Count - maxLines} more changed lines)");
        }

        return string.Join('\n', shown);
    }

    private static string[] Lines(string code) =>
        code.Replace("\r\n", "\n").TrimEnd('\n').Split('\n').Select(l => l.TrimEnd()).Where(l => l.Trim().Length > 0).ToArray();
}

/// <summary>Thuật toán sửa của một chain (spec 03 Mục 6, spec 07 Mục 4.2). Code thuần: không gọi model, không build.</summary>
public interface IRepairPolicy
{
    string Mechanism { get; }

    /// <summary>Yêu cầu sửa cho vòng <paramref name="round"/>; <c>extra_context</c> để trống, nơi gọi điền khi mức ≥ 2.</summary>
    RepairRequest NextRequest(int round);

    /// <summary>Vòng đã thử có cùng code hash (Adaptive); Fixed luôn null (mã trùng vẫn build lại).</summary>
    int? DuplicateOf(string? codeHash);

    /// <summary>Cập nhật trạng thái sau một vòng, trả <c>decision.json</c> (phần do thuật toán quyết định).</summary>
    RoundDecision Apply(RoundOutcome outcome);

    /// <summary>Flow 3.4: còn được sửa tiếp sau <paramref name="roundsDone"/> vòng không (chưa VALID).</summary>
    bool CanContinue(int roundsDone, int n);

    /// <summary>Lý do dừng khi <see cref="CanContinue"/> trả false: <c>budget</c> hoặc <c>stagnation</c>.</summary>
    string StopReason(int roundsDone, int n);

    /// <summary>Bản chốt khi hết vòng: checkpoint (Adaptive) hoặc bản mới nhất (Fixed).</summary>
    ChainVersion Current { get; }

    RepairCounters Counters { get; }
}

/// <summary>Chuỗi mô tả output không hợp lệ trong danh sách đã thử.</summary>
public static class InvalidOutputText
{
    public static string For(string? reason) => "invalid output: " + reason switch
    {
        TestGenerator.NoCode => "no Java class found",
        TestGenerator.ParseError => "the code does not parse",
        TestGenerator.MultipleClasses => "more than one top-level class",
        null or "" => "unknown",
        _ => reason,
    };
}

/// <summary>
/// Fixed Repair (spec 03 Mục 6.6): mọi vòng dùng prompt mức 1 với bản mới nhất; luôn sửa tiếp từ bản mới nhất (kể cả khi tệ hơn);
/// output không hợp lệ thì bản mới nhất giữ nguyên; không dùng hash, signature, checkpoint.
/// </summary>
public sealed class FixedRepairPolicy(ChainVersion c0) : IRepairPolicy
{
    private ChainVersion _latest = c0;
    private RepairCounters _counters = new();

    public string Mechanism => "fixed";
    public ChainVersion Current => _latest;
    public RepairCounters Counters => _counters;

    public RepairRequest NextRequest(int round) => new()
    {
        Mechanism = Mechanism,
        Round = round,
        RequestedLevel = 1,
        Level = 1,
        State = _latest.State,
        BaseCode = _latest.Code,
        BaseRound = _latest.Round,
        ErrorBlock = _latest.Diagnosis.ErrorBlock,
    };

    public int? DuplicateOf(string? codeHash) => null;

    public RoundDecision Apply(RoundOutcome outcome)
    {
        var decision = new RoundDecision
        {
            Round = outcome.Round,
            Mechanism = Mechanism,
            BaseRound = outcome.Request.BaseRound,
            RequestedLevel = 1,
            Level = 1,
            LevelAfter = 1,
            Template = PromptBuilder.RepairUserTemplate(1),
            CodeHash = outcome.Candidate.CodeHash,
            DroppedMethods = [.. outcome.Candidate.DroppedMethods],
        };
        if (!outcome.Candidate.ValidOutput || outcome.Verify is null || outcome.Diagnosis is null)
        {
            _counters = _counters with { InvalidOutput = _counters.InvalidOutput + 1 };
            return decision with { InvalidOutput = true, CheckpointRound = _latest.Round };
        }

        var verify = outcome.Verify;
        var stateChanged = verify.State != _latest.State;
        if (stateChanged)
        {
            _counters = _counters with { StateChange = _counters.StateChange + 1 };
        }

        _latest = new ChainVersion(outcome.Round, outcome.Candidate, verify, outcome.Diagnosis);
        return decision with
        {
            State = verify.State,
            ErrorSignature = verify.ErrorSignature,
            Improved = verify.State == VerificationState.Valid,
            CheckpointRound = outcome.Round,
            TestsPassed = verify.TestMethods.Passed,
            AssertionCount = verify.AssertionCount,
        };
    }

    public bool CanContinue(int roundsDone, int n) => roundsDone < n;

    public string StopReason(int roundsDone, int n) => "budget";
}

/// <summary>
/// Adaptive Repair (spec 03 Mục 6.4–6.5, AR): giữ checkpoint (bản tốt nhất), mức strategy 1–3, các code hash đã thấy và danh sách
/// "đã thử và thất bại". Bản tốt hơn thành checkpoint (đổi trạng thái → mức 1); bản tệ hơn bị rollback; tệ hơn, lặp signature, mã trùng
/// hoặc output không hợp lệ → lên mức (mức 3 → về 2, Q5). Mức 3 chỉ dùng khi checkpoint ở COMPILE/RUNTIME/ASSERTION và biết method lỗi.
/// </summary>
public sealed class AdaptiveRepairPolicy : IRepairPolicy
{
    private ChainVersion _best;
    private int _level = 1;
    private readonly Dictionary<string, int> _seen = new(StringComparer.Ordinal);
    private readonly List<Attempt> _attempts = [];
    private RepairCounters _counters = new();
    private readonly int? _earlyStopP;
    private int _roundsWithoutImprovement;

    public AdaptiveRepairPolicy(ChainVersion c0, int? earlyStopP = null)
    {
        _best = c0;
        _earlyStopP = earlyStopP;
        if (c0.Candidate.CodeHash is { } hash)
        {
            _seen[hash] = c0.Round;
        }
    }

    public string Mechanism => "adaptive";
    public ChainVersion Current => _best;
    public RepairCounters Counters => _counters;
    public int Level => _level;
    public IReadOnlyList<Attempt> Attempts => _attempts;

    /// <summary>Mức thực tế: mức 3 chỉ khi checkpoint ở COMPILE/RUNTIME/ASSERTION và có method lỗi; nếu không thì mức 2.</summary>
    public static int EffectiveLevel(int requested, ChainVersion best) =>
        requested == 3 && best.State is VerificationState.Compile or VerificationState.Runtime or VerificationState.Assertion && best.Diagnosis.FailingMethods.Count > 0
            ? 3
            : Math.Min(requested, 2);

    /// <summary>Q5: <c>NextLevel(1) = 2</c>, <c>NextLevel(2) = 3</c>, <c>NextLevel(3) = 2</c>.</summary>
    public static int NextLevel(int level) => level < 3 ? level + 1 : 2;

    public RepairRequest NextRequest(int round)
    {
        var level = EffectiveLevel(_level, _best);
        return new RepairRequest
        {
            Mechanism = Mechanism,
            Round = round,
            RequestedLevel = _level,
            Level = level,
            State = _best.State,
            BaseCode = _best.Code,
            BaseRound = _best.Round,
            ErrorBlock = _best.Diagnosis.ErrorBlock,
            FailedAttempts = level >= 2 ? [.. _attempts] : [],
            RewriteMethods = level == 3 ? [.. _best.Diagnosis.FailingMethods] : [],
        };
    }

    public int? DuplicateOf(string? codeHash) => codeHash is not null && _seen.TryGetValue(codeHash, out var round) ? round : null;

    public RoundDecision Apply(RoundOutcome outcome)
    {
        var request = outcome.Request;
        var decision = new RoundDecision
        {
            Round = outcome.Round,
            Mechanism = Mechanism,
            BaseRound = request.BaseRound,
            RequestedLevel = request.RequestedLevel,
            Level = request.Level,
            Template = PromptBuilder.RepairUserTemplate(request.Level),
            CodeHash = outcome.Candidate.CodeHash,
            DroppedMethods = [.. outcome.Candidate.DroppedMethods],
        };
        if (request.Level == 3)
        {
            _counters = _counters with { Level3Rounds = _counters.Level3Rounds + 1 };
        }

        // O2: output không hợp lệ → một vòng không build, lên mức.
        if (!outcome.Candidate.ValidOutput)
        {
            _attempts.Add(new Attempt { Round = outcome.Round, Signature = InvalidOutputText.For(outcome.Candidate.InvalidReason) });
            _counters = _counters with { InvalidOutput = _counters.InvalidOutput + 1 };
            return Finish(decision with { InvalidOutput = true }, improved: false, levelUp: true);
        }

        // O3: mã trùng một bản đã thử → không build, lên mức.
        if (outcome.DuplicateOf is { } same)
        {
            _attempts.Add(new Attempt
            {
                Round = outcome.Round,
                Signature = $"duplicate code (same as round {same})",
                Change = LineDiff.Changes(_best.Code, outcome.Candidate.Code ?? ""),
            });
            _counters = _counters with { DuplicateCode = _counters.DuplicateCode + 1 };
            return Finish(decision with { DuplicateCode = true }, improved: false, levelUp: true);
        }

        var verify = outcome.Verify ?? throw new ArgumentException("Bản không trùng và hợp lệ phải có kết quả xác minh", nameof(outcome));
        var diagnosis = outcome.Diagnosis ?? throw new ArgumentException("Thiếu chẩn đoán", nameof(outcome));
        if (outcome.Candidate.CodeHash is { } hash)
        {
            _seen.TryAdd(hash, outcome.Round);
        }

        decision = decision with { State = verify.State, ErrorSignature = verify.ErrorSignature, TestsPassed = verify.TestMethods.Passed, AssertionCount = verify.AssertionCount };
        var comparison = CheckpointComparer.Compare(verify, _best.Verify);
        decision = decision with { Comparison = comparison };

        // O1, O4, O5: tốt hơn checkpoint (VALID luôn tốt hơn) → checkpoint mới; đổi trạng thái → mức 1.
        if (comparison == CheckpointComparer.Better)
        {
            if (verify.State != _best.State)
            {
                _level = 1;
                _counters = _counters with { StateChange = _counters.StateChange + 1 };
            }

            _best = new ChainVersion(outcome.Round, outcome.Candidate, verify, diagnosis);
            return Finish(decision with { Improved = true }, improved: true, levelUp: false);
        }

        // O6–O8: không tốt hơn → bỏ bản mới, ghi vào đã thử; tệ hơn (rollback) hoặc lặp signature → lên mức.
        var repeated = verify.ErrorSignature is not null && verify.ErrorSignature == _best.Verify.ErrorSignature;
        var rollback = comparison == CheckpointComparer.Worse;
        _attempts.Add(new Attempt
        {
            Round = outcome.Round,
            Signature = verify.ErrorSignature ?? verify.State.ToString().ToUpperInvariant(),
            FirstError = diagnosis.FirstError,
            Change = LineDiff.Changes(_best.Code, outcome.Candidate.Code ?? ""),
        });
        if (rollback)
        {
            _counters = _counters with { Rollback = _counters.Rollback + 1 };
        }

        if (repeated)
        {
            _counters = _counters with { RepeatedSignature = _counters.RepeatedSignature + 1 };
        }

        return Finish(decision with { Rollback = rollback, RepeatedSignature = repeated }, improved: false, levelUp: rollback || repeated);
    }

    private RoundDecision Finish(RoundDecision decision, bool improved, bool levelUp)
    {
        if (levelUp)
        {
            _level = NextLevel(_level);
            _counters = _counters with { LevelUp = _counters.LevelUp + 1 };
        }

        _roundsWithoutImprovement = improved ? 0 : _roundsWithoutImprovement + 1;
        return decision with { LevelAfter = _level, CheckpointRound = _best.Round };
    }

    public bool CanContinue(int roundsDone, int n) => roundsDone < n && !(_earlyStopP is { } p && _roundsWithoutImprovement >= p);

    public string StopReason(int roundsDone, int n) => roundsDone >= n ? "budget" : "stagnation";
}
