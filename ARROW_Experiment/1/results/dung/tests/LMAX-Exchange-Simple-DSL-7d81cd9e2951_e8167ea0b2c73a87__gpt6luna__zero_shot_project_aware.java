package com.lmax.simpledsl.internal;

import com.lmax.simpledsl.api.DslArg;
import com.lmax.simpledsl.api.DslParams;
import com.lmax.simpledsl.api.SimpleDslArg;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DslParamsParser_ArrowTest
{
    @Test
    public void parsesRequiredPositionalValue()
    {
        SimpleDslArg arg = new SimpleDslArg("name", true, false, null, null, null);
        DslParams params = new DslParamsParser().parse(new String[]{"Alice"}, arg);

        assertEquals("Alice", params.get("name"));
    }

    @Test
    public void usesDefaultForOptionalValue()
    {
        SimpleDslArg arg = new SimpleDslArg("name", false, false, "default", null, null);
        DslParams params = new DslParamsParser().parse(new String[0], arg);

        assertEquals("default", params.get("name"));
    }

    @Test
    public void parsesNamedValueCaseInsensitively()
    {
        SimpleDslArg arg = new SimpleDslArg("name", true, false, null, null, null);
        DslParams params = new DslParamsParser().parse(new String[]{"name=Alice"}, arg);

        assertEquals("Alice", params.get("name"));
    }

    @Test
    public void rejectsMissingRequiredValue()
    {
        SimpleDslArg arg = new SimpleDslArg("name", true, false, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> new DslParamsParser().parse(new String[0], arg));
    }

    @Test
    public void rejectsUnexpectedNamedArgument()
    {
        SimpleDslArg arg = new SimpleDslArg("name", false, false, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> new DslParamsParser().parse(new String[]{"other=value"}, arg));
    }
}
