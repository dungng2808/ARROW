package org.apache.jsieve.utils;

import org.apache.jsieve.Argument;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.exception.SyntaxException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ArgumentParser_ArrowTest {

    @Test
    public void testInitializeWithOnlyTags() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new TagArgument("tag2"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        Set<String> singleTags = parser.getSingleTags();
        assertTrue(singleTags.contains("tag1"));
        assertTrue(singleTags.contains("tag2"));
        assertEquals(2, singleTags.size());
    }

    @Test
    public void testInitializeWithTagWithValue() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument("value1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        Set<String> singleTags = parser.getSingleTags();
        assertFalse(singleTags.contains("tag1"));
        assertEquals(0, singleTags.size());
    }

    @Test
    public void testGetStringValueForTag() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument("value1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getStringValueForTag("tag1", "error");
        assertEquals("value1", result);
    }

    @Test
    public void testGetStringValueForTagNotFound() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument("value1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getStringValueForTag("tag2", "error");
        assertNull(result);
    }

    @Test
    public void testGetNumericValueForTag() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new NumberArgument(42));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        Integer result = parser.getNumericValueForTag("tag1", "error");
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    public void testGetStringListForTag() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument("value1", "value2"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        List<String> result = parser.getStringListForTag("tag1", "error");
        assertEquals(2, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
    }

    @Test
    public void testGetRemainingStringValue() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new StringListArgument("value1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getRemainingStringValue("error");
        assertEquals("value1", result);
    }

    @Test
    public void testGetRemainingStringValueMultiple() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new StringListArgument("value1"));
        arguments.add(new StringListArgument("value2"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.getRemainingStringValue("error");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }

    @Test
    public void testThrowOnUnvalidSeenSingleTagValid() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        parser.throwOnUnvalidSeenSingleTag("tag1");
        // Should not throw
    }

    @Test
    public void testThrowOnUnvalidSeenSingleTagInvalid() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenSingleTag("tag2");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }

    @Test
    public void testThrowOnUnvalidSeenTagWithValueValid() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument("value1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        parser.throwOnUnvalidSeenTagWithValue("tag1");
        // Should not throw
    }

    @Test
    public void testThrowOnUnvalidSeenTagWithValueInvalid() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new TagArgument("tag1"));
        arguments.add(new StringListArgument("value1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenTagWithValue("tag2");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }

    @Test
    public void testDuplicatedTags() throws Exception {
        List<Argument> arguments = new ArrayList<>();
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

    @Test
    public void testTagsAfterPositionalArguments() throws Exception {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new StringListArgument("value1"));
        arguments.add(new TagArgument("tag1"));

        ArgumentParser parser = new ArgumentParser(arguments);
        
        try {
            parser.throwOnUnvalidSeenSingleTag("tag1");
            fail("Should have thrown SyntaxException");
        } catch (SyntaxException e) {
            // Expected
        }
    }
}
