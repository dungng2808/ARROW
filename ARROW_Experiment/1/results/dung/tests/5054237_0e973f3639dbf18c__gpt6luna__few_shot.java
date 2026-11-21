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
    public void parsesSingleTagAndReturnsCopy() {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(new TagArgument("tag")));

        Set<String> tags = parser.getSingleTags();
        assertEquals(Collections.singleton("tag"), tags);
        tags.clear();
        assertEquals(Collections.singleton("tag"), parser.getSingleTags());
    }

    @Test
    public void retrievesStringValueForTag() throws Exception {
        List<String> values = Collections.singletonList("value");
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new StringListArgument(values)));

        assertEquals("value", parser.getStringValueForTag("tag", "invalid"));
    }

    @Test
    public void retrievesNumericValueForTag() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new NumberArgument(42)));

        assertEquals(Integer.valueOf(42), parser.getNumericValueForTag("tag", "invalid"));
    }

    @Test
    public void retrievesStringListForTag() throws Exception {
        List<String> values = Arrays.asList("one", "two");
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new StringListArgument(values)));

        assertEquals(values, parser.getStringListForTag("tag", "invalid"));
    }

    @Test
    public void missingTagValuesReturnNullOrEmptyList() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getStringValueForTag("missing", "invalid"));
        assertNull(parser.getNumericValueForTag("missing", "invalid"));
        assertEquals(Collections.emptyList(), parser.getStringListForTag("missing", "invalid"));
    }

    @Test
    public void retrievesRemainingStringValue() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new StringListArgument(Collections.singletonList("positional"))));

        assertEquals("positional", parser.getRemainingStringValue("invalid"));
    }

    @Test
    public void noRemainingArgumentsReturnsNull() throws Exception {
        ArgumentParser parser = new ArgumentParser(Collections.<Argument>emptyList());

        assertNull(parser.getRemainingStringValue("invalid"));
    }

    @Test
    public void acceptsValidSingleTags() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(new TagArgument("known")));

        parser.throwOnUnvalidSeenSingleTag("known");
        assertTrue(parser.getSingleTags().contains("known"));
    }

    @Test
    public void acceptsValidTagsWithValues() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("known"), new NumberArgument(1)));

        parser.throwOnUnvalidSeenTagWithValue("known");
        assertEquals(Integer.valueOf(1), parser.getNumericValueForTag("known", "invalid"));
    }

    @Test(expected = SyntaxException.class)
    public void singleTagCannotBeRetrievedAsAValue() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(new TagArgument("tag")));

        parser.getStringValueForTag("tag", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void wrongArgumentTypeForStringValueThrows() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new NumberArgument(1)));

        parser.getStringValueForTag("tag", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void wrongArgumentTypeForNumericValueThrows() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new StringListArgument(Collections.singletonList("value"))));

        parser.getNumericValueForTag("tag", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void stringValueRequiresExactlyOneItem() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new StringListArgument(Arrays.asList("one", "two"))));

        parser.getStringValueForTag("tag", "invalid");
    }

    @Test(expected = SyntaxException.class)
    public void remainingValueRequiresAtMostOneArgument() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new StringListArgument(Collections.singletonList("one")),
                new StringListArgument(Collections.singletonList("two"))));

        parser.getRemainingStringValue("invalid");
    }

    @Test(expected = SyntaxException.class)
    public void duplicateTagsAreRejected() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("tag"), new TagArgument("tag")));

        parser.throwOnUnvalidSeenSingleTag("tag");
    }

    @Test(expected = SyntaxException.class)
    public void tagsAfterPositionalArgumentsAreRejected() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new StringListArgument(Collections.singletonList("positional")),
                new TagArgument("tag")));

        parser.throwOnUnvalidSeenSingleTag("tag");
    }

    @Test(expected = SyntaxException.class)
    public void unexpectedSingleTagsAreRejected() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(new TagArgument("unexpected")));

        parser.throwOnUnvalidSeenSingleTag("known");
    }

    @Test(expected = SyntaxException.class)
    public void unexpectedValueTagsAreRejected() throws Exception {
        ArgumentParser parser = new ArgumentParser(Arrays.<Argument>asList(
                new TagArgument("unexpected"), new NumberArgument(1)));

        parser.throwOnUnvalidSeenTagWithValue("known");
    }
}
