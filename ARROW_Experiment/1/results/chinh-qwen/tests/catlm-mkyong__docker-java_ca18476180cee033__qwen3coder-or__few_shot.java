package com.mkyong;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class App_ArrowTest {

    @Mock
    private Document mockDocument;

    @Mock
    private Elements mockElements;

    @Mock
    private Element mockElement;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindLinksReturnsEmptySetWhenNoLinksFound() throws IOException {
        // Mock the Jsoup behavior
        when(Jsoup.connect(anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).data(anyString(), anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).userAgent(anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).cookie(anyString(), anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).timeout(anyInt()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).get())
                .thenReturn(mockDocument);

        // Mock document to return empty elements
        when(mockDocument.select(anyString())).thenReturn(mockElements);
        when(mockElements.size()).thenReturn(0);

        // Call the method
        Set<String> result = App.findLinks("http://example.com");

        // Verify result
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindLinksReturnsLinksWhenLinksExist() throws IOException {
        // Mock the Jsoup behavior
        when(Jsoup.connect(anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).data(anyString(), anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).userAgent(anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).cookie(anyString(), anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).timeout(anyInt()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).get())
                .thenReturn(mockDocument);

        // Mock document to return elements with href attributes
        when(mockDocument.select(anyString())).thenReturn(mockElements);
        when(mockElements.size()).thenReturn(2);
        when(mockElements.get(0)).thenReturn(mockElement);
        when(mockElements.get(1)).thenReturn(mockElement);
        when(mockElement.attr(anyString())).thenReturn("http://example.com/link1", "http://example.com/link2");

        // Call the method
        Set<String> result = App.findLinks("http://example.com");

        // Verify result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("http://example.com/link1"));
        assertTrue(result.contains("http://example.com/link2"));
    }

    @Test
    public void testFindLinksHandlesIOException() throws IOException {
        // Mock the Jsoup behavior to throw IOException
        when(Jsoup.connect(anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).data(anyString(), anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).userAgent(anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).cookie(anyString(), anyString()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).timeout(anyInt()))
                .thenReturn(mock(org.jsoup.Connection.class));
        when(Jsoup.connect(anyString()).get())
                .thenThrow(new IOException("Connection failed"));

        // Call the method and expect IOException
        try {
            App.findLinks("http://example.com");
            fail("Expected IOException to be thrown");
        } catch (IOException e) {
            assertEquals("Connection failed", e.getMessage());
        }
    }
}
