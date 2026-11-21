package org.apache.jsieve.utils;

import org.apache.jsieve.Argument;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.exception.SyntaxException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ArgumentParser_ArrowTest {

    @Test
    public void returnsStringValueForTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("name"));
        arguments.add(new StringListArgument(Arrays.asList("alice")));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals("alice", parser.getStringValueForTag("name", "invalid"));
    }

    @Test
    public void returnsNumericValueForTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("limit"));
        arguments.add(new NumberArgument(7));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals(Integer.valueOf(7), parser.getNumericValueForTag("limit", "invalid"));
    }

    @Test
    public void returnsStringListForTag() throws Exception {
        List<String> values = Arrays.asList("one", "two");
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("items"));
        arguments.add(new StringListArgument(values));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals(values, parser.getStringListForTag("items", "invalid"));
    }

    @Test
    public void returnsNullForMissingStringTag() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getStringValueForTag("missing", "invalid"));
    }

    @Test
    public void returnsNullForMissingNumericTag() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getNumericValueForTag("missing", "invalid"));
    }

    @Test
    public void returnsEmptyListForMissingStringListTag() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertEquals(Collections.<String>emptyList(), parser.getStringListForTag("missing", "invalid"));
    }

    @Test
    public void returnsSingleTagNames() {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));

        ArgumentParser parser = new ArgumentParser(arguments);

        Set<String> expected = new HashSet<String>();
        expected.add("flag");
        assertEquals(expected, parser.getSingleTags());
    }

    @Test
    public void returnsRemainingStringValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(Arrays.asList("positional")));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals("positional", parser.getRemainingStringValue("invalid"));
    }

    @Test
    public void returnsNullWhenThereIsNoRemainingValue() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getRemainingStringValue("invalid"));
    }

    @Test
    public void acceptsValidSingleTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));

        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenSingleTag("flag");
    }

    @Test
    public void acceptsValidTagWithValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("name"));
        arguments.add(new StringListArgument(Arrays.asList("alice")));

        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenTagWithValue("name");
    }

    @Test
    public void rejectsSingleTagWhenItHasAValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.getStringValueForTag("flag", "invalid");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("invalid", expected.getMessage());
        }
    }

    @Test
    public void rejectsStringValueWhenArgumentIsNotAStringList() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("name"));
        arguments.add(new NumberArgument(3));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.getStringValueForTag("name", "invalid");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("invalid", expected.getMessage());
        }
    }

    @Test
    public void rejectsStringValueWhenStringListHasMultipleValues() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("name"));
        arguments.add(new StringListArgument(Arrays.asList("one", "two")));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.getStringValueForTag("name", "invalid");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("invalid", expected.getMessage());
        }
    }

    @Test
    public void rejectsNumericValueWhenArgumentIsNotNumeric() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("limit"));
        arguments.add(new StringListArgument(Arrays.asList("seven")));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.getNumericValueForTag("limit", "invalid");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("invalid", expected.getMessage());
        }
    }

    @Test
    public void rejectsStringListWhenArgumentIsNotAStringList() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("items"));
        arguments.add(new NumberArgument(3));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.getStringListForTag("items", "invalid");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("invalid", expected.getMessage());
        }
    }

    @Test
    public void rejectsMultipleRemainingArguments() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(Arrays.asList("one")));
        arguments.add(new StringListArgument(Arrays.asList("two")));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.getRemainingStringValue("invalid");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("invalid", expected.getMessage());
        }
    }

    @Test
    public void rejectsDuplicateTags() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));
        arguments.add(new TagArgument("flag"));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.throwOnUnvalidSeenSingleTag("flag");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertTrue(expected.getMessage().startsWith("Duplicated tags : "));
            assertTrue(expected.getMessage().contains("flag"));
        }
    }

    @Test
    public void rejectsTagAfterPositionalArgument() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(Arrays.asList("positional")));
        arguments.add(new TagArgument("flag"));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.throwOnUnvalidSeenSingleTag("flag");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertTrue(expected.getMessage().startsWith("Tags after the positional arguments : "));
            assertTrue(expected.getMessage().contains("flag"));
        }
    }

    @Test
    public void rejectsUnexpectedSingleTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("unexpected"));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.throwOnUnvalidSeenSingleTag("allowed");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("Unexpected tags : [\"unexpected\"]", expected.getMessage());
        }
    }

    @Test
    public void rejectsUnexpectedTagWithValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("unexpected"));
        arguments.add(new StringListArgument(Arrays.asList("value")));

        ArgumentParser parser = new ArgumentParser(arguments);
        try {
            parser.throwOnUnvalidSeenTagWithValue("allowed");
            fail("Expected SyntaxException");
        } catch (SyntaxException expected) {
            assertEquals("Unexpected tags : [\"unexpected\"]", expected.getMessage());
        }
    }
}
