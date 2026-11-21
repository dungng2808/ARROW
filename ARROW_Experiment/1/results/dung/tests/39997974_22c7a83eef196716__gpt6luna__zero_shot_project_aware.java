package com.senacor.codecamp.reactive.services.integration;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CounterBackendImpl_ArrowTest {

    @Test
    public void countWordsCountsTokensSeparatedBySpaces() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("one two three");

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(3, backend.countWords(parsedPage));
    }

    @Test
    public void countWordsCountsRepeatedSpacesAsSeparators() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("one  two");

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(2, backend.countWords(parsedPage));
    }

    @Test
    public void countWordsReturnsZeroForEmptyText() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("");

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(0, backend.countWords(parsedPage));
    }

    @Test(expected = NullPointerException.class)
    public void countWordsRejectsNullParsedPage() {
        CounterBackendImpl backend = new CounterBackendImpl();

        backend.countWords(null);
    }
}
