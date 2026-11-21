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

public class ArgumentParser_ArrowTest {

    @Test
    public void returnsSingleTagAndRemainingString() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));
        arguments.add(new StringListArgument(Arrays.asList("value")));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals(Collections.singleton("flag"), parser.getSingleTags());
        assertNull(parser.getRemainingStringValue("invalid"));
    }

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
        arguments.add(new TagArgument("count"));
        arguments.add(new NumberArgument(7));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals(Integer.valueOf(7), parser.getNumericValueForTag("count", "invalid"));
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
    public void returnsNullForMissingTagValue() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getStringValueForTag("missing", "invalid"));
    }

    @Test
    public void returnsEmptyListForMissingTagList() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertTrue(parser.getStringListForTag("missing", "invalid").isEmpty());
    }

    @Test
    public void returnsNullForMissingNumericTag() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getNumericValueForTag("missing", "invalid"));
    }

    @Test
    public void returnsRemainingStringValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(Arrays.asList("positional")));

        ArgumentParser parser = new ArgumentParser(arguments);

        assertEquals("positional", parser.getRemainingStringValue("invalid"));
    }

    @Test
    public void returnsNullWhenThereIsNoRemainingArgument() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getRemainingStringValue("invalid"));
    }

    @Test
    public void acceptsValidSingleTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenSingleTag("flag");
        assertEquals(Collections.singleton("flag"), parser.getSingleTags());
    }

    @Test
    public void acceptsValidTagWithValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("name"));
        arguments.add(new StringListArgument(Arrays.asList("alice")));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenTagWithValue("name");
        assertEquals("alice", parser.getStringValueForTag("name", "invalid"));
    }

    @Test(expected = SyntaxException.class)
    public void rejectsValueRequestedForSingleTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.getStringValueForTag("flag", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsStringValueForNonStringListArgument() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("count"));
        arguments.add(new NumberArgument(3));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.getStringValueForTag("count", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsNumericValueForNonNumberArgument() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("name"));
        arguments.add(new StringListArgument(Arrays.asList("alice")));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.getNumericValueForTag("name", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsStringValueWhenListHasMultipleValues() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("items"));
        arguments.add(new StringListArgument(Arrays.asList("one", "two")));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.getStringValueForTag("items", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsMultipleRemainingArguments() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(Arrays.asList("one")));
        arguments.add(new StringListArgument(Arrays.asList("two")));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.getRemainingStringValue("invalid");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsDuplicateTags() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("flag"));
        arguments.add(new TagArgument("flag"));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenSingleTag("flag");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsTagAfterPositionalArgument() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new StringListArgument(Arrays.asList("positional")));
        arguments.add(new TagArgument("flag"));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenSingleTag("flag");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsUnexpectedSingleTag() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("unexpected"));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenSingleTag("allowed");
    }

    @Test(expected = SyntaxException.class)
    public void rejectsUnexpectedTagWithValue() throws Exception {
        List<Argument> arguments = new ArrayList<Argument>();
        arguments.add(new TagArgument("unexpected"));
        arguments.add(new StringListArgument(Arrays.asList("value")));
        ArgumentParser parser = new ArgumentParser(arguments);

        parser.throwOnUnvalidSeenTagWithValue("allowed");
    }
}
