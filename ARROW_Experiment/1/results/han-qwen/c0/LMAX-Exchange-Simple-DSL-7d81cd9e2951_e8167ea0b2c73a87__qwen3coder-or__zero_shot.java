package com.lmax.simpledsl.internal;

import com.lmax.simpledsl.api.DslArg;
import com.lmax.simpledsl.api.DslParams;
import com.lmax.simpledsl.api.RepeatingArgGroup;
import com.lmax.simpledsl.api.SimpleDslArg;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DslParamsParser_ArrowTest
{
    @Test
    public void testParseWithSimpleArguments()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "value1", false, false, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", "value2", false, false, null, ",");
        
        DslParams result = parser.parse(new String[]{"--name=test", "--age=25"}, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(1, nameParam.getValuesAsList().size());
        assertEquals("test", nameParam.getValuesAsList().get(0));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(1, ageParam.getValuesAsList().size());
        assertEquals("25", ageParam.getValuesAsList().get(0));
    }
    
    @Test
    public void testParseWithRequiredArguments()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "default", true, false, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", "default", true, false, null, ",");
        
        DslParams result = parser.parse(new String[]{"--name=test", "--age=25"}, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(1, nameParam.getValuesAsList().size());
        assertEquals("test", nameParam.getValuesAsList().get(0));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(1, ageParam.getValuesAsList().size());
        assertEquals("25", ageParam.getValuesAsList().get(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithMissingRequiredArgument()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "default", true, false, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", "default", true, false, null, ",");
        
        parser.parse(new String[]{"--name=test"}, arg1, arg2);
    }
    
    @Test
    public void testParseWithDefaultValues()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "defaultName", false, false, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", "defaultAge", false, false, null, ",");
        
        DslParams result = parser.parse(new String[]{"--name=test"}, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(1, nameParam.getValuesAsList().size());
        assertEquals("test", nameParam.getValuesAsList().get(0));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(1, ageParam.getValuesAsList().size());
        assertEquals("defaultAge", ageParam.getValuesAsList().get(0));
    }
    
    @Test
    public void testParseWithMultipleValues()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "default", false, true, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", "default", false, true, null, ",");
        
        DslParams result = parser.parse(new String[]{"--name=test1,test2", "--age=25,30"}, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(2, nameParam.getValuesAsList().size());
        assertEquals("test1", nameParam.getValuesAsList().get(0));
        assertEquals("test2", nameParam.getValuesAsList().get(1));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(2, ageParam.getValuesAsList().size());
        assertEquals("25", ageParam.getValuesAsList().get(0));
        assertEquals("30", ageParam.getValuesAsList().get(1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithInvalidValue()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "default", false, false, new String[]{"valid1", "valid2"}, ",");
        
        parser.parse(new String[]{"--name=invalid"}, arg1);
    }
    
    @Test
    public void testParseWithNoArguments()
    {
        DslParamsParser parser = new DslParamsParser();
        
        SimpleDslArg arg1 = new SimpleDslArg("name", "default", false, false, null, ",");
        SimpleDslArg arg2 = new SimpleDslArg("age", "default", false, false, null, ",");
        
        DslParams result = parser.parse(new String[]{}, arg1, arg2);
        
        assertNotNull(result);
        Map<String, DslParam> params = result.getParams();
        assertEquals(2, params.size());
        
        DslParam nameParam = params.get("name");
        assertNotNull(nameParam);
        assertTrue(nameParam.hasValue());
        assertEquals("name", nameParam.getName());
        assertEquals(1, nameParam.getValuesAsList().size());
        assertEquals("default", nameParam.getValuesAsList().get(0));
        
        DslParam ageParam = params.get("age");
        assertNotNull(ageParam);
        assertTrue(ageParam.hasValue());
        assertEquals("age", ageParam.getName());
        assertEquals(1, ageParam.getValuesAsList().size());
        assertEquals("default", ageParam.getValuesAsList().get(0));
    }
}
