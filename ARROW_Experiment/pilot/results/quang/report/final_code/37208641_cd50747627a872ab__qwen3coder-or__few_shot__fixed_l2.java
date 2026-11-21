package com.github.loyada.jdollarx;

import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InBrowserFinder_ArrowTest {

    @Test
    public void testFindWithUnderlyingSourceAndXPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement underlyingElement = Mockito.mock(WebElement.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.of(underlyingElement));
        
        when(underlyingElement.findElement(any(By.class))).thenReturn(foundElement);
        
        WebElement result = InBrowserFinder.find(driver, path);
        assertEquals(foundElement, result);
    }

    @Test
    public void testFindWithUnderlyingSourceNoXPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement underlyingElement = Mockito.mock(WebElement.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(Optional.of(underlyingElement));
        
        when(underlyingElement.findElement(any(By.class))).thenReturn(foundElement);
        
        WebElement result = InBrowserFinder.find(driver, path);
        assertEquals(foundElement, result);
    }

    @Test
    public void testFindWithoutUnderlyingSourceAndXPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.empty());
        
        when(driver.findElement(any(By.class))).thenReturn(foundElement);
        
        WebElement result = InBrowserFinder.find(driver, path);
        assertEquals(foundElement, result);
    }

    @Test(expected = NoSuchElementException.class)
    public void testFindNoSuchElementException() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.empty());
        
        doThrow(new org.openqa.selenium.NoSuchElementException("")).when(driver).findElement(any(By.class));
        
        InBrowserFinder.find(driver, path);
    }

    @Test
    public void testFindPageWithoutWithUnderlyingSource() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement underlyingElement = Mockito.mock(WebElement.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.of(underlyingElement));
        
        Path notPath = Mockito.mock(Path.class);
        when(notPath.getXPath()).thenReturn(Optional.of(".//div[not(.//div)]"));
        when(PathOperators.not(path)).thenReturn(notPath);
        
        when(underlyingElement.findElement(any(By.class))).thenReturn(foundElement);
        
        WebElement result = InBrowserFinder.findPageWithout(driver, path);
        assertEquals(foundElement, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindPageWithoutNoPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.empty());
        
        InBrowserFinder.findPageWithout(driver, path);
    }

    @Test
    public void testFindAllWithUnderlyingSourceAndXPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement underlyingElement = Mockito.mock(WebElement.class);
        List<WebElement> foundElements = Collections.singletonList(Mockito.mock(WebElement.class));
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.of(underlyingElement));
        
        when(underlyingElement.findElements(any(By.class))).thenReturn(foundElements);
        
        List<WebElement> result = InBrowserFinder.findAll(driver, path);
        assertEquals(foundElements, result);
    }

    @Test
    public void testFindAllWithUnderlyingSourceNoXPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement underlyingElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.empty());
        when(path.getUnderlyingSource()).thenReturn(Optional.of(underlyingElement));
        
        List<WebElement> result = InBrowserFinder.findAll(driver, path);
        assertEquals(1, result.size());
        assertEquals(underlyingElement, result.get(0));
    }

    @Test
    public void testFindAllWithoutUnderlyingSourceAndXPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        List<WebElement> foundElements = Collections.singletonList(Mockito.mock(WebElement.class));
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.empty());
        
        when(driver.findElements(any(By.class))).thenReturn(foundElements);
        
        List<WebElement> result = InBrowserFinder.findAll(driver, path);
        assertEquals(foundElements, result);
    }

    @Test
    public void testFindPageWithNumberOfOccurrencesWithUnderlyingSource() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement underlyingElement = Mockito.mock(WebElement.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.of(underlyingElement));
        
        when(underlyingElement.findElement(any(By.class))).thenReturn(foundElement);
        
        WebElement result = InBrowserFinder.findPageWithNumberOfOccurrences(driver, path, 2);
        assertEquals(foundElement, result);
    }

    @Test
    public void testFindPageWithNumberOfOccurrencesWithoutUnderlyingSource() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        WebElement foundElement = Mockito.mock(WebElement.class);
        
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.of(".//div"));
        when(path.getUnderlyingSource()).thenReturn(Optional.empty());
        
        when(driver.findElement(any(By.class))).thenReturn(foundElement);
        
        WebElement result = InBrowserFinder.findPageWithNumberOfOccurrences(driver, path, 2);
        assertEquals(foundElement, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindPageWithNumberOfOccurrencesNoPath() {
        WebDriver driver = Mockito.mock(WebDriver.class);
        Path path = Mockito.mock(Path.class);
        when(path.getXPath()).thenReturn(Optional.empty());
        
        InBrowserFinder.findPageWithNumberOfOccurrences(driver, path, 2);
    }

    @Test
    public void testProcessedPathForFindNotStartsWith() {
        String path = "not(.//div)";
        String result = InBrowserFinder.processedPathForFind(path);
        assertEquals("/html[.not(.//div)]", result);
    }

    @Test
    public void testProcessedPathForFindNotStartsWithNot() {
        String path = "not(div)";
        String result = InBrowserFinder.processedPathForFind(path);
        assertEquals("/html[not(.//div)]", result);
    }

    @Test
    public void testProcessedPathForFindNormalPath() {
        String path = ".//div";
        String result = InBrowserFinder.processedPathForFind(path);
        assertEquals("//.//div", result);
    }

    @Test
    public void testProcessedPathForFindWithPathStartingWithSlash() {
        String path = "/div";
        String result = InBrowserFinder.processedPathForFind(path);
        assertEquals("/div", result);
    }

    @Test
    public void testProcessedPathForFindWithPathStartingWithOpenParen() {
        String path = "(//div)";
        String result = InBrowserFinder.processedPathForFind(path);
        assertEquals("(//div)", result);
    }
}
