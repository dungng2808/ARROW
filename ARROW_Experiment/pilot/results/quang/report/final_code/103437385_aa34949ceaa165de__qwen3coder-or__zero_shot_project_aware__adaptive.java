package org.kie.soup.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ListSplitter_ArrowTest {

    @Test
    public void testSplitSimple() {
        String[] result = ListSplitter.split("'");
        assertArrayEquals(new String[]{}, result);
    }

    @Test
    public void testSplitWithItems() {
        String[] result = ListSplitter.split("'", false, "'hello', 'world'");
        assertArrayEquals(new String[]{"hello", "world"}, result);
    }

    @Test
    public void testSplitWithTrim() {
        String[] result = ListSplitter.split("'", true, "' hello ', ' world '");
        assertArrayEquals(new String[]{"hello", "world"}, result);
    }

    @Test
    public void testSplitWithoutTrim() {
        String[] result = ListSplitter.split("'", false, "' hello ', ' world '");
        assertArrayEquals(new String[]{" hello ", " world "}, result);
    }

    @Test
    public void testSplitWithEmptyItems() {
        String[] result = ListSplitter.split("'", false, "'hello', '', 'world'");
        assertArrayEquals(new String[]{"hello", "", "world"}, result);
    }

    @Test
    public void testSplitWithUnquotedItems() {
        String[] result = ListSplitter.split("'", false, "hello, world");
        assertArrayEquals(new String[]{"hello", "world"}, result);
    }

    @Test
    public void testSplitWithMixedQuotedAndUnquoted() {
        String[] result = ListSplitter.split("'", false, "'hello', world, 'world'");
        assertArrayEquals(new String[]{"hello", "world", "world"}, result);
    }

    @Test
    public void testSplitPreserveQuotesWithCommaInValue() {
        String[] result = ListSplitter.splitPreserveQuotes("'", false, "'hello,world', normal");
        assertArrayEquals(new String[]{"'hello,world'", "normal"}, result);
    }

    @Test
    public void testSplitPreserveQuotesWithoutCommaInValue() {
        String[] result = ListSplitter.splitPreserveQuotes("'", false, "'hello', normal");
        assertArrayEquals(new String[]{"hello", "normal"}, result);
    }

    @Test
    public void testSplitPreserveQuotesWithTrim() {
        String[] result = ListSplitter.splitPreserveQuotes("'", true, "' hello , world ', normal");
        assertArrayEquals(new String[]{"' hello , world '", "normal"}, result);
    }

    @Test
    public void testSplitWithSingleQuoteItem() {
        String[] result = ListSplitter.split("'", false, "'");
        assertArrayEquals(new String[]{"'"}, result);
    }

    @Test
    public void testSplitWithNestedQuotes() {
        String[] result = ListSplitter.split("'", false, "'a\"b\"c', d");
        assertArrayEquals(new String[]{"a\"b\"c", "d"}, result);
    }

    @Test
    public void testSplitWithTrailingComma() {
        String[] result = ListSplitter.split("'", false, "a,");
        assertArrayEquals(new String[]{"a", ""}, result);
    }

    @Test
    public void testSplitWithLeadingComma() {
        String[] result = ListSplitter.split("'", false, ",a");
        assertArrayEquals(new String[]{"", "a"}, result);
    }

    @Test
    public void testSplitWithMultipleCommas() {
        String[] result = ListSplitter.split("'", false, "a,,b");
        assertArrayEquals(new String[]{"a", "", "b"}, result);
    }

    @Test
    public void testSplitWithNoQuotes() {
        String[] result = ListSplitter.split("'", false, "a,b,c");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testSplitWithSingleItem() {
        String[] result = ListSplitter.split("'", false, "single");
        assertArrayEquals(new String[]{"single"}, result);
    }

    @Test
    public void testSplitWithOnlySpaces() {
        String[] result = ListSplitter.split("'", false, "   ");
        assertArrayEquals(new String[]{"   "}, result);
    }

    @Test
    public void testSplitWithEmptyString() {
        String[] result = ListSplitter.split("'", false, "");
        assertArrayEquals(new String[]{""}, result);
    }

    @Test
    public void testSplitWithOnlyComma() {
        String[] result = ListSplitter.split("'", false, ",");
        assertArrayEquals(new String[]{"", ""}, result);
    }

    @Test
    public void testSplitWithUnbalancedQuotes() {
        String[] result = ListSplitter.split("'", false, "'unbalanced");
        assertArrayEquals(new String[]{"'unbalanced"}, result);
    }

    @Test
    public void testSplitWithUnbalancedQuotesEnd() {
        String[] result = ListSplitter.split("'", false, "unbalanced'");
        assertArrayEquals(new String[]{"unbalanced'"}, result);
    }

    @Test
    public void testSplitWithMultipleQuotes() {
        String[] result = ListSplitter.split("'", false, "'a','b','c'");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testSplitWithDifferentQuoteCharacter() {
        String[] result = ListSplitter.split("\"", false, "\"hello\", \"world\"");
        assertArrayEquals(new String[]{"hello", "world"}, result);
    }

    @Test
    public void testSplitWithEscapedQuotes() {
        String[] result = ListSplitter.split("'", false, "'a\\'b', c");
        assertArrayEquals(new String[]{"a'b", "c"}, result);
    }

    @Test
    public void testSplitPreserveQuotesWithEscapedComma() {
        String[] result = ListSplitter.splitPreserveQuotes("'", false, "'hello\\,world', normal");
        assertArrayEquals(new String[]{"hello,world", "normal"}, result);
    }
}
