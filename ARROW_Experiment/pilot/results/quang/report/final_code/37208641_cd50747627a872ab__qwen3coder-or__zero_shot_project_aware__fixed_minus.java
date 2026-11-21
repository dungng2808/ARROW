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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class InBrowserFinder_ArrowTest {

    @Mock
    private WebDriver mockDriver;

    @Mock
    private WebElement mockWebElement;

    @Mock
    private Path mockPath;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFind_WithUnderlyingSourceAndXPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockWebElement.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.find(mockDriver, mockPath);
        
        assertEquals(mockWebElement, result);
        verify(mockWebElement).findElement(By.xpath(".//div"));
    }

    @Test
    public void testFind_WithUnderlyingSourceNoXPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        when(mockPath.getXPath()).thenReturn(java.util.Optional.empty());
        
        WebElement result = InBrowserFinder.find(mockDriver, mockPath);
        
        assertEquals(mockWebElement, result);
    }

    @Test
    public void testFind_WithDriverAndXPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockDriver.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.find(mockDriver, mockPath);
        
        assertEquals(mockWebElement, result);
        verify(mockDriver).findElement(By.xpath(".//div"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFind_ThrowsNoSuchElementException() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        doThrow(new org.openqa.selenium.NoSuchElementException("")).when(mockDriver).findElement(any(By.class));
        
        InBrowserFinder.find(mockDriver, mockPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFind_EmptyPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.find(mockDriver, mockPath);
    }

    @Test
    public void testFindPageWithout_WithUnderlyingSource() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockWebElement.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithout(mockDriver, mockPath);
        
        assertEquals(mockWebElement, result);
        verify(mockWebElement).findElement(By.xpath("//not(.//div)"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindPageWithout_NoPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.findPageWithout(mockDriver, mockPath);
    }

    @Test
    public void testFindPageWithout_WithDriver() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockDriver.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithout(mockDriver, mockPath);
        
        assertEquals(mockWebElement, result);
        verify(mockDriver).findElement(By.xpath("/html[not(.//div)]"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindPageWithout_ThrowsNoSuchElementException() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        doThrow(new org.openqa.selenium.NoSuchElementException("")).when(mockDriver).findElement(any(By.class));
        
        InBrowserFinder.findPageWithout(mockDriver, mockPath);
    }

    @Test
    public void testFindAll_WithUnderlyingSourceAndXPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockWebElement.findElements(any(By.class))).thenReturn(Collections.singletonList(mockWebElement));
        
        List<WebElement> result = InBrowserFinder.findAll(mockDriver, mockPath);
        
        assertEquals(1, result.size());
        assertEquals(mockWebElement, result.get(0));
        verify(mockWebElement).findElements(By.xpath(".//div"));
    }

    @Test
    public void testFindAll_WithUnderlyingSourceNoXPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        when(mockPath.getXPath()).thenReturn(java.util.Optional.empty());
        
        List<WebElement> result = InBrowserFinder.findAll(mockDriver, mockPath);
        
        assertEquals(1, result.size());
        assertEquals(mockWebElement, result.get(0));
    }

    @Test
    public void testFindAll_WithDriverAndXPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockDriver.findElements(any(By.class))).thenReturn(Collections.singletonList(mockWebElement));
        
        List<WebElement> result = InBrowserFinder.findAll(mockDriver, mockPath);
        
        assertEquals(1, result.size());
        assertEquals(mockWebElement, result.get(0));
        verify(mockDriver).findElements(By.xpath(".//div"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindAll_EmptyPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.findAll(mockDriver, mockPath);
    }

    @Test
    public void testFindPageWithNumberOfOccurrences_WithUnderlyingSource() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.of(mockWebElement));
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockWebElement.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithNumberOfOccurrences(mockDriver, mockPath, 2);
        
        assertEquals(mockWebElement, result);
        verify(mockWebElement).findElement(By.xpath("././/div[count(.//div) = 2]"));
    }

    @Test
    public void testFindPageWithNumberOfOccurrences_WithDriver() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.of(".//div"));
        
        when(mockDriver.findElement(any(By.class))).thenReturn(mockWebElement);
        
        WebElement result = InBrowserFinder.findPageWithNumberOfOccurrences(mockDriver, mockPath, 2);
        
        assertEquals(mockWebElement, result);
        verify(mockDriver).findElement(By.xpath("/html.//div[count(.//div) = 2]"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindPageWithNumberOfOccurrences_NoPath() {
        when(mockPath.getUnderlyingSource()).thenReturn(java.util.Optional.empty());
        when(mockPath.getXPath()).thenReturn(java.util.Optional.empty());
        
        InBrowserFinder.findPageWithNumberOfOccurrences(mockDriver, mockPath, 2);
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
        assertEquals("//.//div", result);
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
