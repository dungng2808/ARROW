package com.senacor.codecamp.reactive.services.integration;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import org.apache.commons.lang3.Validate;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CounterBackendImpl_ArrowTest {

    private CounterBackendImpl counterBackend;
    private ParsedPage parsedPage;

    @Before
    public void setUp() {
        counterBackend = new CounterBackendImpl();
        parsedPage = mock(ParsedPage.class);
    }

    @Test
    public void testCountWords_NormalCase() {
        // Given
        String sampleText = "This is a sample text with several words";
        when(parsedPage.getText()).thenReturn(sampleText);

        // When
        int result = counterBackend.countWords(parsedPage);

        // Then
        assertThat(result, is(7));
    }

    @Test
    public void testCountWords_EmptyText() {
        // Given
        String emptyText = "";
        when(parsedPage.getText()).thenReturn(emptyText);

        // When
        int result = counterBackend.countWords(parsedPage);

        // Then
        assertThat(result, is(0));
    }

    @Test
    public void testCountWords_SingleWord() {
        // Given
        String singleWord = "Hello";
        when(parsedPage.getText()).thenReturn(singleWord);

        // When
        int result = counterBackend.countWords(parsedPage);

        // Then
        assertThat(result, is(1));
    }

    @Test
    public void testCountWords_MultipleSpaces() {
        // Given
        String textWithMultipleSpaces = "Hello    world   test";
        when(parsedPage.getText()).thenReturn(textWithMultipleSpaces);

        // When
        int result = counterBackend.countWords(parsedPage);

        // Then
        assertThat(result, is(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCountWords_NullParsedPage() {
        // When
        counterBackend.countWords(null);
    }

    @Test
    public void testCountWords_NullText() {
        // Given
        when(parsedPage.getText()).thenReturn(null);

        // When
        int result = counterBackend.countWords(parsedPage);

        // Then
        assertThat(result, is(0));
    }
}
