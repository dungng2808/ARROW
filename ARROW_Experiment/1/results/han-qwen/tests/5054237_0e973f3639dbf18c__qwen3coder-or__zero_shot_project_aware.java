package org.apache.jsieve.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jsieve.Argument;
import org.apache.jsieve.NumberArgument;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.TagArgument;
import org.apache.jsieve.exception.SyntaxException;
import org.junit.Test;

public class ArgumentParser_ArrowTest {

    @Test
    public void testInitializeWithEmptyList() {
        List<Argument> arguments = new ArrayList<Argument>();
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThat(parser.getSingleTags()).isEmpty();
        assertThat(parser.getStringListForTag("test", "error")).isEmpty();
        assertThat(parser.getStringValueForTag("test", "error")).isNull();
        assertThat(parser.getNumericValueForTag("test", "error")).isNull();
    }

    @Test
    public void testInitializeWithTagOnly() {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        Set<String> singleTags = parser.getSingleTags();
        assertThat(singleTags).containsExactly("tag1");
        assertThat(parser.getStringListForTag("tag1", "error")).isEmpty();
        assertThat(parser.getStringValueForTag("tag1", "error")).isNull();
        assertThat(parser.getNumericValueForTag("tag1", "error")).isNull();
    }

    @Test
    public void testInitializeWithTagWithValue() {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>());
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        Set<String> singleTags = parser.getSingleTags();
        assertThat(singleTags).isEmpty();
        
        assertThat(parser.getStringListForTag("tag1", "error")).isEmpty();
        assertThat(parser.getStringValueForTag("tag1", "error")).isNull();
        assertThat(parser.getNumericValueForTag("tag1", "error")).isNull();
    }

    @Test
    public void testInitializeWithMultipleTagsAndValues() {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg1 = new TagArgument("tag1");
        arguments.add(tagArg1);
        
        StringListArgument stringArg1 = new StringListArgument(new ArrayList<String>());
        arguments.add(stringArg1);
        
        TagArgument tagArg2 = new TagArgument("tag2");
        arguments.add(tagArg2);
        
        NumberArgument numArg = new NumberArgument(42);
        arguments.add(numArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        Set<String> singleTags = parser.getSingleTags();
        assertThat(singleTags).isEmpty();
        
        assertThat(parser.getStringListForTag("tag1", "error")).isEmpty();
        assertThat(parser.getStringValueForTag("tag1", "error")).isNull();
        assertThat(parser.getNumericValueForTag("tag1", "error")).isNull();
        
        assertThat(parser.getStringListForTag("tag2", "error")).isEmpty();
        assertThat(parser.getStringValueForTag("tag2", "error")).isNull();
        assertThat(parser.getNumericValueForTag("tag2", "error")).isEqualTo(Integer.valueOf(42));
    }

    @Test
    public void testGetStringValueForTagWhenTagExists() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }});
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getStringValueForTag("tag1", "error");
        assertThat(result).isEqualTo("value1");
    }

    @Test
    public void testGetStringValueForTagWhenTagDoesNotExist() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getStringValueForTag("tag2", "error");
        assertThat(result).isNull();
    }

    @Test
    public void testGetStringValueForTagWhenTagExistsButNotStringList() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        NumberArgument numArg = new NumberArgument(42);
        arguments.add(numArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.getStringValueForTag("tag1", "error"))
            .isInstanceOf(SyntaxException.class)
            .hasMessage("error");
    }

    @Test
    public void testGetNumericValueForTagWhenTagExists() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        NumberArgument numArg = new NumberArgument(42);
        arguments.add(numArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        Integer result = parser.getNumericValueForTag("tag1", "error");
        assertThat(result).isEqualTo(Integer.valueOf(42));
    }

    @Test
    public void testGetNumericValueForTagWhenTagDoesNotExist() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        Integer result = parser.getNumericValueForTag("tag2", "error");
        assertThat(result).isNull();
    }

    @Test
    public void testGetNumericValueForTagWhenTagExistsButNotNumber() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>());
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.getNumericValueForTag("tag1", "error"))
            .isInstanceOf(SyntaxException.class)
            .hasMessage("error");
    }

    @Test
    public void testGetStringListForTagWhenTagExists() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        List<String> values = new ArrayList<String>();
        values.add("value1");
        values.add("value2");
        StringListArgument stringArg = new StringListArgument(values);
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        List<String> result = parser.getStringListForTag("tag1", "error");
        assertThat(result).containsExactly("value1", "value2");
    }

    @Test
    public void testGetStringListForTagWhenTagDoesNotExist() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        List<String> result = parser.getStringListForTag("tag2", "error");
        assertThat(result).isEmpty();
    }

    @Test
    public void testGetStringListForTagWhenTagExistsButNotStringList() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        NumberArgument numArg = new NumberArgument(42);
        arguments.add(numArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.getStringListForTag("tag1", "error"))
            .isInstanceOf(SyntaxException.class)
            .hasMessage("error");
    }

    @Test
    public void testGetRemainingStringValueWithOneArgument() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }});
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getRemainingStringValue("error");
        assertThat(result).isEqualTo("value1");
    }

    @Test
    public void testGetRemainingStringValueWithNoArguments() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        ArgumentParser parser = new ArgumentParser(arguments);
        
        String result = parser.getRemainingStringValue("error");
        assertThat(result).isNull();
    }

    @Test
    public void testGetRemainingStringValueWithMultipleArguments() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        StringListArgument stringArg1 = new StringListArgument(new ArrayList<String>() {{
            add("value1");
        }});
        arguments.add(stringArg1);
        
        StringListArgument stringArg2 = new StringListArgument(new ArrayList<String>() {{
            add("value2");
        }});
        arguments.add(stringArg2);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.getRemainingStringValue("error"))
            .isInstanceOf(SyntaxException.class)
            .hasMessage("error");
    }

    @Test
    public void testThrowOnUnvalidSeenSingleTagWithValidTags() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        parser.throwOnUnvalidSeenSingleTag("tag1", "tag2");
        // Should not throw
    }

    @Test
    public void testThrowOnUnvalidSeenSingleTagWithInvalidTags() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.throwOnUnvalidSeenSingleTag("tag2"))
            .isInstanceOf(SyntaxException.class)
            .hasMessageContaining("Unexpected tags");
    }

    @Test
    public void testThrowOnUnvalidSeenTagWithValueWithValidTags() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>());
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        parser.throwOnUnvalidSeenTagWithValue("tag1", "tag2");
        // Should not throw
    }

    @Test
    public void testThrowOnUnvalidSeenTagWithValueWithInvalidTags() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>());
        arguments.add(stringArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.throwOnUnvalidSeenTagWithValue("tag2"))
            .isInstanceOf(SyntaxException.class)
            .hasMessageContaining("Unexpected tags");
    }

    @Test
    public void testDuplicatedTagsDetection() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        TagArgument tagArg1 = new TagArgument("tag1");
        arguments.add(tagArg1);
        
        TagArgument tagArg2 = new TagArgument("tag1");
        arguments.add(tagArg2);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.throwOnUnvalidSeenSingleTag("tag1"))
            .isInstanceOf(SyntaxException.class)
            .hasMessageContaining("Duplicated tags");
    }

    @Test
    public void testTagsAfterPositionalArgumentsDetection() throws SyntaxException {
        List<Argument> arguments = new ArrayList<Argument>();
        StringListArgument stringArg = new StringListArgument(new ArrayList<String>());
        arguments.add(stringArg);
        
        TagArgument tagArg = new TagArgument("tag1");
        arguments.add(tagArg);
        
        ArgumentParser parser = new ArgumentParser(arguments);
        
        assertThatThrownBy(() -> parser.throwOnUnvalidSeenSingleTag("tag1"))
            .isInstanceOf(SyntaxException.class)
            .hasMessageContaining("Tags after the positional arguments");
    }
}
