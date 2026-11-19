package com.mkyong;

import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class App_ArrowTest {

    @Test
    public void testFindLinksReturnsEmptySetWhenNoLinksFound() throws IOException {
        // Arrange
        String testUrl = "http://example.com";
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        
        when(Jsoup.connect(testUrl)).thenReturn(mockDoc);
        when(mockDoc.select("a[href]")).thenReturn(mockElements);
        when(mockElements.size()).thenReturn(0);
        
        // Act
        Set<String> result = App.findLinks(testUrl);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindLinksReturnsSetWithOneLink() throws IOException {
        // Arrange
        String testUrl = "http://example.com";
        String expectedLink = "http://example.com/page1";
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        Element mockElement = mock(Element.class);
        
        when(Jsoup.connect(testUrl)).thenReturn(mockDoc);
        when(mockDoc.select("a[href]")).thenReturn(mockElements);
        when(mockElements.iterator()).thenReturn(java.util.Collections.singletonList(mockElement).iterator());
        when(mockElement.attr("href")).thenReturn(expectedLink);
        
        // Act
        Set<String> result = App.findLinks(testUrl);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(expectedLink));
    }

    @Test
    public void testFindLinksReturnsSetWithMultipleLinks() throws IOException {
        // Arrange
        String testUrl = "http://example.com";
        String link1 = "http://example.com/page1";
        String link2 = "http://example.com/page2";
        String link3 = "http://example.com/page3";
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        Element mockElement1 = mock(Element.class);
        Element mockElement2 = mock(Element.class);
        Element mockElement3 = mock(Element.class);
        
        when(Jsoup.connect(testUrl)).thenReturn(mockDoc);
        when(mockDoc.select("a[href]")).thenReturn(mockElements);
        when(mockElements.iterator()).thenReturn(
            java.util.Arrays.asList(mockElement1, mockElement2, mockElement3).iterator()
        );
        when(mockElement1.attr("href")).thenReturn(link1);
        when(mockElement2.attr("href")).thenReturn(link2);
        when(mockElement3.attr("href")).thenReturn(link3);
        
        // Act
        Set<String> result = App.findLinks(testUrl);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(link1));
        assertTrue(result.contains(link2));
        assertTrue(result.contains(link3));
    }

    @Test
    public void testFindLinksHandlesIOException() throws IOException {
        // Arrange
        String testUrl = "http://example.com";
        when(Jsoup.connect(testUrl)).thenThrow(new IOException("Connection failed"));
        
        // Act & Assert
        assertThrows(IOException.class, () -> App.findLinks(testUrl));
    }
}
