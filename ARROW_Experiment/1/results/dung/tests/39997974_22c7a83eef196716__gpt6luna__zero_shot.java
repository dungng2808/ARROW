package com.senacor.codecamp.reactive.services.integration;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CounterBackendImpl_ArrowTest {

    @Test
    public void countWordsCountsTokensSeparatedBySpaces() {
        ParsedPage parsedPage = new ParsedPage();
        parsedPage.setText("one two three");

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(3, backend.countWords(parsedPage));
    }

    @Test
    public void countWordsReturnsZeroForEmptyText() {
        ParsedPage parsedPage = new ParsedPage();
        parsedPage.setText("");

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(0, backend.countWords(parsedPage));
    }

    @Test
    public void countWordsTreatsRepeatedSpacesAsSeparators() {
        ParsedPage parsedPage = new ParsedPage();
        parsedPage.setText("one  two");

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(2, backend.countWords(parsedPage));
    }

    @Test(expected = Exception.class)
    public void countWordsRejectsNullPage() {
        CounterBackendImpl backend = new CounterBackendImpl();

        backend.countWords(null);
    }
}
