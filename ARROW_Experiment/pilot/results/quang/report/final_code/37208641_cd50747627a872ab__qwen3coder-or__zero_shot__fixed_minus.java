package com.github.loyada.jdollarx;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InBrowserFinder_ArrowTest {

    @Mock
    private WebDriver mockDriver;

    @Mock
    private WebElement mockWebElement;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFind_WithUnderlyingSourceAndXPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        
        when(mockWebElement.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.find(mockDriver, path);
        assertNotNull(result);
    }

    @Test
    public void testFind_WithUnderlyingSourceNoXPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        
        WebElement result = InBrowserFinder.find(mockDriver, path);
        assertEquals(mockWebElement, result);
    }

    @Test
    public void testFind_WithDriverAndXPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        when(mockDriver.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.find(mockDriver, path);
        assertNotNull(result);
    }

    @Test(expected = NoSuchElementException.class)
    public void testFind_ThrowsNoSuchElementException() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        doThrow(new org.openqa.selenium.NoSuchElementException("")).when(mockDriver).findElement(any(By.class));
        
        InBrowserFinder.find(mockDriver, path);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFind_EmptyPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.find(mockDriver, path);
    }

    @Test
    public void testFindPageWithout_WithUnderlyingSource() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        
        PathOperators mockPathOperators = mock(PathOperators.class);
        when(mockPathOperators.not(any(Path.class))).thenReturn(mock(Path.class));
        when(mockPathOperators.not(any(Path.class)).getXPath()).thenReturn(java.util.Optional.of(".//div[not(.//div)]"));
        
        when(mockWebElement.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithout(mockDriver, path);
        assertNotNull(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindPageWithout_NoPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.findPageWithout(mockDriver, path);
    }

    @Test
    public void testFindAll_WithUnderlyingSourceAndXPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        
        when(mockWebElement.findElements(any(By.class))).thenReturn(Collections.singletonList(mockWebElement));
        
        List<WebElement> result = InBrowserFinder.findAll(mockDriver, path);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindAll_WithUnderlyingSourceNoXPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        
        List<WebElement> result = InBrowserFinder.findAll(mockDriver, path);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testFindAll_WithDriverAndXPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        when(mockDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(mockWebElement));
        
        List<WebElement> result = InBrowserFinder.findAll(mockDriver, path);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAll_EmptyPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.findAll(mockDriver, path);
    }

    @Test
    public void testFindPageWithNumberOfOccurrences_WithUnderlyingSource() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        
        when(mockWebElement.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithNumberOfOccurrences(mockDriver, path, 2);
        assertNotNull(result);
    }

    @Test
    public void testFindPageWithNumberOfOccurrences_WithDriver() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        when(mockDriver.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithNumberOfOccurrences(mockDriver, path, 2);
        assertNotNull(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindPageWithNumberOfOccurrences_NoPath() {
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.findPageWithNumberOfOccurrences(mockDriver, path, 2);
    }

    @Test
    public void testProcessedPathForFind_NotDotSlash() {
        String input = "not(.//div)";
        String result = InBrowserFinder.processedPathForFind(input);
        assertEquals("/html[.not(.//div)]", result);
    }

    @Test
    public void testProcessedPathForFind_Not() {
        String input = "not(div)";
        String result = InBrowserFinder.processedPathForFind(input);
        assertEquals("/html[not(.//div)]", result);
    }

    @Test
    public void testProcessedPathForFind_NormalPath() {
        String input = ".//div";
        String result = InBrowserFinder.processedPathForFind(input);
        assertEquals("//div", result);
    }

    @Test
    public void testProcessedPathForFind_PathStartingWithSlash() {
        String input = "/div";
        String result = InBrowserFinder.processedPathForFind(input);
        assertEquals("/div", result);
    }

    @Test
    public void testProcessedPathForFind_PathStartingWithOpenParen() {
        String input = "(//div)";
        String result = InBrowserFinder.processedPathForFind(input);
        assertEquals("(//div)", result);
    }
}
