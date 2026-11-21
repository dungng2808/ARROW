package com.lmax.simpledsl.internal;

import com.lmax.simpledsl.api.DslArg;
import com.lmax.simpledsl.api.DslParams;
import com.lmax.simpledsl.api.RepeatingArgGroup;
import com.lmax.simpledsl.api.SimpleDslArg;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DslParamsParser_ArrowTest
{
    @Test
    public void testParseWithNoArguments()
    {
        final DslParamsParser parser = new DslParamsParser();
        final DslArg[] dslArgs = {};
        final String[] args = {};
        final DslParams result = parser.parse(args, dslArgs);
        assertNotNull(result);
    }

    @Test
    public void testParseWithSimpleArguments()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        final SimpleDslArg arg2 = new SimpleDslArg("age", true, false, null, null, ",");
        final DslArg[] dslArgs = {arg1, arg2};
        final String[] args = {"name=John", "age=25"};
        final DslParams result = parser.parse(args, dslArgs);
        assertNotNull(result);
        final Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        assertTrue(params.containsKey("name"));
        assertTrue(params.containsKey("age"));
    }

    @Test
    public void testParseWithRequiredArgumentsMissing()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        final SimpleDslArg arg2 = new SimpleDslArg("age", true, false, null, null, ",");
        final DslArg[] dslArgs = {arg1, arg2};
        final String[] args = {"name=John"};
        assertThrows(IllegalArgumentException.class, () -> parser.parse(args, dslArgs));
    }

    @Test
    public void testParseWithInvalidArgumentName()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        final DslArg[] dslArgs = {arg1};
        final String[] args = {"invalid=name"};
        assertThrows(IllegalArgumentException.class, () -> parser.parse(args, dslArgs));
    }

    @Test
    public void testParseWithAllowedValues()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("status", true, false, new String[]{"active", "inactive"}, null, ",");
        final DslArg[] dslArgs = {arg1};
        final String[] args = {"status=active"};
        final DslParams result = parser.parse(args, dslArgs);
        assertNotNull(result);
        final Map<String, DslParam> params = result.getParams();
        assertEquals(1, params.size());
        assertTrue(params.containsKey("status"));
    }

    @Test
    public void testParseWithInvalidAllowedValue()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("status", true, false, new String[]{"active", "inactive"}, null, ",");
        final DslArg[] dslArgs = {arg1};
        final String[] args = {"status=unknown"};
        assertThrows(IllegalArgumentException.class, () -> parser.parse(args, dslArgs));
    }

    @Test
    public void testParseWithDefaultValues()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("name", false, false, null, "default", ",");
        final DslArg[] dslArgs = {arg1};
        final String[] args = {};
        final DslParams result = parser.parse(args, dslArgs);
        assertNotNull(result);
        final Map<String, DslParam> params = result.getParams();
        assertEquals(1, params.size());
        assertTrue(params.containsKey("name"));
        final DslParam param = params.get("name");
        assertEquals("default", param.getValuesAsList().get(0));
    }

    @Test
    public void testParseWithMultipleValues()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg arg1 = new SimpleDslArg("tags", true, true, null, null, ",");
        final DslArg[] dslArgs = {arg1};
        final String[] args = {"tags=tag1,tag2,tag3"};
        final DslParams result = parser.parse(args, dslArgs);
        assertNotNull(result);
        final Map<String, DslParam> params = result.getParams();
        assertEquals(1, params.size());
        assertTrue(params.containsKey("tags"));
        final DslParam param = params.get("tags");
        final List<String> values = param.getValuesAsList();
        assertEquals(3, values.size());
        assertEquals("tag1", values.get(0));
        assertEquals("tag2", values.get(1));
        assertEquals("tag3", values.get(2));
    }

    @Test
    public void testParseWithRepeatingGroup()
    {
        final DslParamsParser parser = new DslParamsParser();
        final SimpleDslArg identityArg = new SimpleDslArg("id", true, false, null, null, ",");
        final SimpleDslArg otherArg = new SimpleDslArg("name", true, false, null, null, ",");
        final RepeatingArgGroup group = new RepeatingArgGroup("group", identityArg, new SimpleDslArg[]{otherArg});
        final DslArg[] dslArgs = {group};
        final String[] args = {"id=1", "name=John", "id=2", "name=Jane"};
        final DslParams result = parser.parse(args, dslArgs);
        assertNotNull(result);
        final Map<String, DslParam> params = result.getParams();
        assertEquals(1, params.size());
        assertTrue(params.containsKey("group"));
    }
}
