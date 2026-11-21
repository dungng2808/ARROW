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
    public void parsesAnEmptyArgumentList()
    {
        DslParams params = new DslParamsParser().parse(new String[0]);
        assertTrue(params != null);
    }

    @Test
    public void rejectsAnUnexpectedPositionalArgument()
    {
        try
        {
            new DslParamsParser().parse(new String[]{"unexpected"});
        }
        catch (IllegalArgumentException expected)
        {
            assertTrue(expected.getMessage().contains("Unexpected ambiguous argument"));
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
