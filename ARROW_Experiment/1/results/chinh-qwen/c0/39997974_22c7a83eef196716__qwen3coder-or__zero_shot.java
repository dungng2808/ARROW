package com.senacor.codecamp.reactive.services.integration;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import org.apache.commons.lang3.Validate;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CounterBackendImpl_ArrowTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCountWordsNullParsedPage() {
        CounterBackendImpl counterBackend = new CounterBackendImpl();
        counterBackend.countWords(null);
    }

    @Test
    public void testCountWordsEmptyText() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("");
        
        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);
        
        assert result == 0;
    }

    @Test
    public void testCountWordsSingleWord() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("hello");
        
        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);
        
        assert result == 1;
    }

    @Test
    public void testCountWordsMultipleWords() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("hello world test");
        
        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);
        
        assert result == 3;
    }

    @Test
    public void testCountWordsMultipleSpaces() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("hello   world  test");
        
        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);
        
        assert result == 3;
    }
}
