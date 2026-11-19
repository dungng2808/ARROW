package com.mkyong;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class App_ArrowTest {

    @Test
    public void testFindLinksReturnsEmptySetWhenNoLinksFound() throws IOException {
        // Mock the Jsoup.connect behavior
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        
        // Setup mock to return empty elements
        when(mockDoc.select("a[href]")).thenReturn(mockElements);
        when(mockElements.size()).thenReturn(0);
        
        // Mock Jsoup.connect to return our mock document
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()))
                       .thenReturn(mockDoc);
            
            Set<String> result = App.findLinks("http://example.com");
            
            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    @Test
    public void testFindLinksReturnsSetWithOneLink() throws IOException {
        // Mock the Jsoup.connect behavior
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        Element mockElement = mock(Element.class);
        
        // Setup mock to return one element with href attribute
        Set<String> expectedLinks = new HashSet<>();
        expectedLinks.add("http://example.com/link1");
        
        when(mockDoc.select("a[href]")).thenReturn(mockElements);
        when(mockElements.iterator()).thenReturn(expectedLinks.iterator());
        when(mockElements.size()).thenReturn(1);
        when(mockElement.attr("href")).thenReturn("http://example.com/link1");
        
        // Mock Jsoup.connect to return our mock document
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()))
                       .thenReturn(mockDoc);
            
            Set<String> result = App.findLinks("http://example.com");
            
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains("http://example.com/link1"));
        }
    }

    @Test
    public void testFindLinksReturnsSetWithMultipleLinks() throws IOException {
        // Mock the Jsoup.connect behavior
        Document mockDoc = mock(Document.class);
        Elements mockElements = mock(Elements.class);
        
        // Setup mock to return multiple elements with href attributes
        Set<String> expectedLinks = new HashSet<>();
        expectedLinks.add("http://example.com/link1");
        expectedLinks.add("http://example.com/link2");
        expectedLinks.add("http://example.com/link3");
        
        when(mockDoc.select("a[href]")).thenReturn(mockElements);
        when(mockElements.size()).thenReturn(3);
        
        // Mock Jsoup.connect to return our mock document
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()))
                       .thenReturn(mockDoc);
            
            Set<String> result = App.findLinks("http://example.com");
            
            assertNotNull(result);
            assertEquals(3, result.size());
            assertTrue(result.contains("http://example.com/link1"));
            assertTrue(result.contains("http://example.com/link2"));
            assertTrue(result.contains("http://example.com/link3"));
        }
    }

    @Test(expected = IOException.class)
    public void testFindLinksThrowsIOExceptionWhenConnectionFails() throws IOException {
        // Mock Jsoup.connect to throw IOException
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString()))
                       .thenThrow(new IOException("Connection failed"));
            
            App.findLinks("http://example.com");
        }
    }
}
