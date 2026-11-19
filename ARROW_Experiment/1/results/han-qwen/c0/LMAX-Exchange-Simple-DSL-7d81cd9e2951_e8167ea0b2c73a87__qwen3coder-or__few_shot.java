package com.lmax.simpledsl.internal;

import com.lmax.simpledsl.api.DslArg;
import com.lmax.simpledsl.api.DslParams;
import com.lmax.simpledsl.api.RepeatingArgGroup;
import com.lmax.simpledsl.api.SimpleDslArg;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DslParamsParser_ArrowTest {

    @Test
    public void testParseWithSimpleArguments() {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", true, false, null, null, ",");
        
        String[] args = {"name=John", "age=25"};
        DslParams result = parser.parse(args, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(1, nameParam.getValuesAsList().size());
        assertEquals("John", nameParam.getValuesAsList().get(0));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(1, ageParam.getValuesAsList().size());
        assertEquals("25", ageParam.getValuesAsList().get(0));
    }

    @Test
    public void testParseWithMultipleValues() {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("tags", true, true, null, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("name", true, false, null, null, ",");
        
        String[] args = {"tags=tag1,tag2,tag3", "name=John"};
        DslParams result = parser.parse(args, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam tagsParam = params.get("tags");
        assertNotNull(tagsParam);
        assertTrue(tagsParam.hasValue());
        assertEquals("tags", tagsParam.getName());
        assertEquals(3, tagsParam.getValuesAsList().size());
        assertEquals("tag1", tagsParam.getValuesAsList().get(0));
        assertEquals("tag2", tagsParam.getValuesAsList().get(1));
        assertEquals("tag3", tagsParam.getValuesAsList().get(2));
    }

    @Test
    public void testParseWithDefaultValues() {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", false, false, "defaultName", null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", true, false, null, null, ",");
        
        String[] args = {"age=25"};
        DslParams result = parser.parse(args, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(1, nameParam.getValuesAsList().size());
        assertEquals("defaultName", nameParam.getValuesAsList().get(0));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(1, ageParam.getValuesAsList().size());
        assertEquals("25", ageParam.getValuesAsList().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithMissingRequiredArgument() {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", true, false, null, null, ",");
        
        String[] args = {"name=John"};
        parser.parse(args, arg1, arg2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithInvalidArgument() {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", true, false, null, null, ",");
        
        String[] args = {"invalid=parameter"};
        parser.parse(args, arg1, arg2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithDuplicateParameter() {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", true, false, null, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("name", true, false, null, null, ",");
        
        String[] args = {"name=John"};
        parser.parse(args, arg1, arg2);
    }
}
