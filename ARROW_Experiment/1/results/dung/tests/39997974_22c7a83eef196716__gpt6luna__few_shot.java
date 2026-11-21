package com.senacor.codecamp.reactive.services.integration;

import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CounterBackendImpl_ArrowTest {

    @Test
    public void countWordsCountsTokensSeparatedBySpaces() {
        ParsedPage parsedPage = new ParsedPage() {
            @Override
            public String getText() {
                return "one two three";
            }
        };

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(3, backend.countWords(parsedPage));
    }

    @Test
    public void countWordsReturnsZeroForEmptyText() {
        ParsedPage parsedPage = new ParsedPage() {
            @Override
            public String getText() {
                return "";
            }
        };

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(0, backend.countWords(parsedPage));
    }

    @Test
    public void countWordsTreatsRepeatedSpacesAsDelimiters() {
        ParsedPage parsedPage = new ParsedPage() {
            @Override
            public String getText() {
                return "one  two";
            }
        };

        CounterBackendImpl backend = new CounterBackendImpl();

        assertEquals(2, backend.countWords(parsedPage));
    }

    @Test(expected = Exception.class)
    public void countWordsRejectsNullPage() {
        CounterBackendImpl backend = new CounterBackendImpl();

        backend.countWords(null);
    }
}
