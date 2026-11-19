package com.senacor.codecamp.reactive.services.integration;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import org.apache.commons.lang3.Validate;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CounterBackendImpl_ArrowTest {

    @Test(expected = IllegalArgumentException.class)
    public void countWords_should_throw_IllegalArgumentException_when_parsedPage_is_null() {
        CounterBackendImpl counterBackend = new CounterBackendImpl();
        counterBackend.countWords(null);
    }

    @Test
    public void countWords_should_return_correct_word_count() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("Hello world this is a test");

        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);

        // Expected word count: 6 words
        // "Hello world this is a test"
        // Words: Hello, world, this, is, a, test
        assert result == 6;
    }

    @Test
    public void countWords_should_handle_empty_text() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("");

        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);

        // Empty string should result in 0 words
        assert result == 0;
    }

    @Test
    public void countWords_should_handle_single_word() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("single");

        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);

        // Single word should result in 1 word
        assert result == 1;
    }

    @Test
    public void countWords_should_handle_multiple_spaces() {
        ParsedPage parsedPage = mock(ParsedPage.class);
        when(parsedPage.getText()).thenReturn("word   with    multiple     spaces");

        CounterBackendImpl counterBackend = new CounterBackendImpl();
        int result = counterBackend.countWords(parsedPage);

        // Multiple spaces should still count as single delimiter
        // "word   with    multiple     spaces" -> 4 words
        assert result == 4;
    }
}
