package org.kie.soup.commons.util;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class ListSplitter_ArrowTest {

    @Test
    public void testSplitSimple() {
        String[] result = ListSplitter.split("', 'test1, test2, test3'");
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, result);
    }

    @Test
    public void testSplitWithTrim() {
        String[] result = ListSplitter.split("'", true, "'test1', 'test2', 'test3'");
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, result);
    }

    @Test
    public void testSplitWithoutTrim() {
        String[] result = ListSplitter.split("'", false, "'test1', 'test2', 'test3'");
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, result);
    }

    @Test
    public void testSplitEmptyString() {
        String[] result = ListSplitter.split("', ''");
        assertArrayEquals(new String[]{"", ""}, result);
    }

    @Test
    public void testSplitSingleItem() {
        String[] result = ListSplitter.split("', 'test1'");
        assertArrayEquals(new String[]{"test1"}, result);
    }

    @Test
    public void testSplitWithSpaces() {
        String[] result = ListSplitter.split("'", true, "' test1 ', ' test2 '");
        assertArrayEquals(new String[]{"test1", "test2"}, result);
    }

    @Test
    public void testSplitWithUnquotedItems() {
        String[] result = ListSplitter.split("'", true, "test1, 'test2', test3");
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, result);
    }

    @Test
    public void testSplitWithQuotedItems() {
        String[] result = ListSplitter.split("'", true, "'test1', 'test2'");
        assertArrayEquals(new String[]{"test1", "test2"}, result);
    }

    @Test
    public void testSplitPreserveQuotes() {
        String[] result = ListSplitter.splitPreserveQuotes("'", true, "test1, 'test2', test3");
        assertArrayEquals(new String[]{"test1", "'test2'", "test3"}, result);
    }

    @Test
    public void testSplitPreserveQuotesWithCommaInValue() {
        String[] result = ListSplitter.splitPreserveQuotes("'", true, "test1, 'test,2', test3");
        assertArrayEquals(new String[]{"test1", "'test,2'", "test3"}, result);
    }

    @Test
    public void testSplitPreserveQuotesWithEmptyValue() {
        String[] result = ListSplitter.splitPreserveQuotes("'", true, "test1,,test3");
        assertArrayEquals(new String[]{"test1", "", "test3"}, result);
    }

    @Test
    public void testSplitWithOnlyQuotes() {
        String[] result = ListSplitter.split("'", true, "''");
        assertArrayEquals(new String[]{""}, result);
    }

    @Test
    public void testSplitWithMultipleQuotes() {
        String[] result = ListSplitter.split("'", true, "'test1', 'test2', 'test3'");
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, result);
    }

    @Test
    public void testSplitWithNoQuotes() {
        String[] result = ListSplitter.split("'", true, "test1, test2, test3");
        assertArrayEquals(new String[]{"test1", "test2", "test3"}, result);
    }

    @Test
    public void testSplitWithMixedQuotesAndNoQuotes() {
        String[] result = ListSplitter.split("'", true, "test1, 'test2', test3, 'test4'");
        assertArrayEquals(new String[]{"test1", "test2", "test3", "test4"}, result);
    }
}
