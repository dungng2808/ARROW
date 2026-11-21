package org.kie.soup.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ListSplitter_ArrowTest {

    @Test
    public void splitUsesSingleQuoteAndPreservesUnquotedWhitespace() {
        assertArrayEquals(new String[]{"one", " two", "three"},
                          ListSplitter.split("'one', two,three"));
    }

    @Test
    public void splitTrimsItemsWhenRequested() {
        assertArrayEquals(new String[]{"one", "two", "three"},
                          ListSplitter.split("'", true, " one , two , three "));
    }

    @Test
    public void splitPreservesItemsWhenTrimIsFalse() {
        assertArrayEquals(new String[]{" one ", " two "},
                          ListSplitter.split("'", false, " one , two "));
    }

    @Test
    public void splitCombinesCommaSeparatedQuotedItem() {
        assertArrayEquals(new String[]{"one,two", "three"},
                          ListSplitter.split("'", true, "'one,two', three"));
    }

    @Test
    public void splitPreserveQuotesAddsQuotesAroundCombinedItem() {
        assertArrayEquals(new String[]{"'one,two'", "three"},
                          ListSplitter.splitPreserveQuotes("'", true, "'one,two', three"));
    }

    @Test
    public void splitEmptyStringReturnsEmptyItem() {
        assertArrayEquals(new String[]{""}, ListSplitter.split(""));
    }

    @Test(expected = NullPointerException.class)
    public void splitNullValueListThrows() {
        ListSplitter.split((String) null);
    }
}
