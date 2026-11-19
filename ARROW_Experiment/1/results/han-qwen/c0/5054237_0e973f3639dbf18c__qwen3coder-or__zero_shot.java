package org.apache.jsieve.utils;

import org.apache.jsieve.Argument;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.exception.SyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class ArgumentParser_ArrowTest extends TestCase {

    public void testInitializeWithOnlyTags() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new TagArgument("tag2"));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertEquals(0, parser.getSingleTags().size());
        assertEquals(0, parser.getRemainingArguments().size());
    }
    
    public void testInitializeWithTagsAndArguments() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        arguments.add(new TagArgument("tag2"));
        arguments.add(new NumberArgument(42));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertEquals(1, parser.getSingleTags().size());
        assertTrue(parser.getSingleTags().contains("tag1"));
        assertEquals(1, parser.getRemainingArguments().size());
    }
    
    public void testGetStringValueForTagExists() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertEquals("value1", parser.getStringValueForTag("tag1", "error"));
    }
    
    public void testGetStringValueForTagDoesNotExist() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertNull(parser.getStringValueForTag("tag2", "error"));
    }
    
    public void testGetNumericValueForTagExists() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new NumberArgument(42));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertEquals(Integer.valueOf(42), parser.getNumericValueForTag("tag1", "error"));
    }
    
    public void testGetNumericValueForTagDoesNotExist() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new NumberArgument(42));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertNull(parser.getNumericValueForTag("tag2", "error"));
    }
    
    public void testGetStringListForTagExists() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
            add("value2");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        List<String> expected = new ArrayList<String>();
        expected.add("value1");
        expected.add("value2");
        assertEquals(expected, parser.getStringListForTag("tag1", "error"));
    }
    
    public void testGetStringListForTagDoesNotExist() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertEquals(0, parser.getStringListForTag("tag2", "error").size());
    }
    
    public void testGetRemainingStringValueOneArgument() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertEquals("value1", parser.getRemainingStringValue("error"));
    }
    
    public void testGetRemainingStringValueNoArguments() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertNull(parser.getRemainingStringValue("error"));
    }
    
    public void testGetRemainingStringValueMultipleArguments() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value2");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.getRemainingStringValue("error");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }
    
    public void testThrowOnUnvalidSeenSingleTagValidTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        parser.throwOnUnvalidSeenSingleTag("tag1");
        // Should not throw
    }
    
    public void testThrowOnUnvalidSeenSingleTagInvalidTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenSingleTag("tag2");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }
    
    public void testThrowOnUnvalidSeenTagWithValueValidTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        parser.throwOnUnvalidSeenTagWithValue("tag1");
        // Should not throw
    }
    
    public void testThrowOnUnvalidSeenTagWithValueInvalidTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenTagWithValue("tag2");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }
    
    public void testDuplicatedTags() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new TagArgument("tag1"));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenSingleTag("tag1");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }
    
    public void testTagsAfterPositionalArguments() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }}));
        arguments.add(new TagArgument("tag1"));
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenSingleTag("tag1");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }
    
    private List<Argument> getRemainingArguments() {
        return new ArrayList<Argument>();
    }
}
