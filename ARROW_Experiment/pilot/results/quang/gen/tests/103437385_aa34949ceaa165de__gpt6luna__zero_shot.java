package org.kie.soup.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ListSplitter_ArrowTest {

    @Test
    public void splitUsesSingleQuotesAndPreservesUnquotedWhitespace() {
        assertArrayEquals(new String[]{"one", " two ", "three"},
                          ListSplitter.split("'one', two , 'three'"));
    }

    @Test
    public void splitWithCustomQuoteAndTrim() {
        assertArrayEquals(new String[]{"one", "two", "three"},
                          ListSplitter.split("\"", true, "\"one\", two , \"three\""));
    }

    @Test
    public void splitCombinesCommaSeparatedQuotedValue() {
        assertArrayEquals(new String[]{"one,two", "three"},
                          ListSplitter.split("'", false, "'one,two',three"));
    }

    @Test
    public void splitPreserveQuotesAddsQuotesAroundCombinedValues() {
        assertArrayEquals(new String[]{"'one,two'", "three"},
                          ListSplitter.splitPreserveQuotes("'", false, "'one,two',three"));
    }

    @Test
    public void splitEmptyStringReturnsEmptyItem() {
        assertArrayEquals(new String[]{""}, ListSplitter.split(""));
    }

    @Test
    public void splitTrimsUnquotedItemsWhenRequested() {
        assertArrayEquals(new String[]{"one", "two"},
                          ListSplitter.split("'", true, " one , two "));
    }

    @Test
    public void splitPreserveQuotesLeavesItemsWithoutCommasUnquoted() {
        assertArrayEquals(new String[]{"one", "two"},
                          ListSplitter.splitPreserveQuotes("'", false, "one,two"));
    }
}
