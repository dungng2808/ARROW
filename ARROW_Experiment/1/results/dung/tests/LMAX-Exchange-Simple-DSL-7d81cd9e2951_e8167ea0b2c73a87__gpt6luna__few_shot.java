package com.lmax.simpledsl.internal;

import com.lmax.simpledsl.api.DslArg;
import com.lmax.simpledsl.api.DslParams;
import com.lmax.simpledsl.api.SimpleDslArg;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DslParamsParser_ArrowTest
{
    @Test
    public void parsesAnOptionalArgumentValue()
    {
        SimpleDslArg arg = new SimpleDslArg("name", false, null, null, false);
        DslParams params = new DslParamsParser().parse(new String[]{"value"}, arg);

        assertEquals("value", params.get("name").getValue());
    }

    @Test
    public void usesDefaultValueWhenOptionalArgumentIsMissing()
    {
        SimpleDslArg arg = new SimpleDslArg("name", false, "default", null, false);
        DslParams params = new DslParamsParser().parse(new String[0], arg);

        assertEquals("default", params.get("name").getValue());
    }

    @Test
    public void acceptsAnAllowedValueIgnoringCaseAndReturnsCanonicalValue()
    {
        SimpleDslArg arg = new SimpleDslArg("mode", false, null, new String[]{"FAST", "SLOW"}, false);
        DslParams params = new DslParamsParser().parse(new String[]{"fast"}, arg);

        assertEquals("FAST", params.get("mode").getValue());
    }

    @Test
    public void rejectsAnUnexpectedArgument()
    {
        SimpleDslArg arg = new SimpleDslArg("name", false, null, null, false);
        try
        {
            new DslParamsParser().parse(new String[]{"unknown=value"}, arg);
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue(expected.getMessage().contains("Unexpected argument"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void rejectsMissingRequiredValue()
    {
        SimpleDslArg arg = new SimpleDslArg("name", true, null, null, false);
        try
        {
            new DslParamsParser().parse(new String[0], arg);
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue(expected.getMessage().contains("Missing value for parameter"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void optionalArgumentWithoutValueIsAbsent()
    {
        SimpleDslArg arg = new SimpleDslArg("name", false, null, null, false);
        DslParams params = new DslParamsParser().parse(new String[0], arg);

        assertFalse(params.get("name").hasValue());
    }
}
