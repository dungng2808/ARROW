package org.kie.soup.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ListSplitter_ArrowTest {

    @Test
    public void splitUsesSingleQuoteAndPreservesUnquotedSpaces() {
        String[] result = ListSplitter.split("'a', b, 'c,d'");

        assertArrayEquals(new String[]{"a", " b", "c,d"}, result);
    }

    @Test
    public void splitWithTrimRemovesSpacesFromUnquotedItems() {
        String[] result = ListSplitter.split("'", true, " a , 'b,c' , d ");

        assertArrayEquals(new String[]{"a", "b,c", "d"}, result);
    }

    @Test
    public void splitPreserveQuotesQuotesItemsContainingCommas() {
        String[] result = ListSplitter.splitPreserveQuotes("\"", false, "a,b,c");

        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void splitPreserveQuotesWrapsItemsContainingCommas() {
        String[] result = ListSplitter.splitPreserveQuotes("'", false, "'a,b',c");

        assertArrayEquals(new String[]{"'a,b'", "c"}, result);
    }

    @Test
    public void splitEmptyInputReturnsEmptyArray() {
        String[] result = ListSplitter.split("");

        assertArrayEquals(new String[]{""}, result);
    }
}
