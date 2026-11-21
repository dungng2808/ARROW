package com.github.loyada.jdollarx;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InBrowserFinder_ArrowTest {

    @Test
    public void findAllUsesProcessedRelativeXPath() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        WebElement element = mock(WebElement.class);
        List<WebElement> expected = Arrays.asList(element);

        when(path.getXPath()).thenReturn(java.util.Optional.of("div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.<WebElement>empty());
        when(driver.findElements(By.xpath("//div"))).thenReturn(expected);

        assertEquals(expected, InBrowserFinder.findAll(driver, path));
    }

    @Test
    public void findAllReturnsUnderlyingElementWhenPathIsAbsent() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        WebElement underlying = mock(WebElement.class);

        when(path.getXPath()).thenReturn(java.util.Optional.<String>empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(underlying));

        List<WebElement> result = InBrowserFinder.findAll(driver, path);

        assertEquals(1, result.size());
        assertSame(underlying, result.get(0));
    }

    @Test
    public void findAllUsesXPathDirectlyForUnderlyingElement() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        WebElement underlying = mock(WebElement.class);
        WebElement found = mock(WebElement.class);
        List<WebElement> expected = Arrays.asList(found);

        when(path.getXPath()).thenReturn(java.util.Optional.of(".//span"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(underlying));
        when(underlying.findElements(By.xpath(".//span"))).thenReturn(expected);

        assertEquals(expected, InBrowserFinder.findAll(driver, path));
    }

    @Test
    public void findAllRejectsPathlessElementWithoutUnderlyingSource() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.<String>empty());
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.<WebElement>empty());

        try {
            InBrowserFinder.findAll(driver, path);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("webel is empty", expected.getMessage());
        }
    }

    @Test
    public void findPageWithNumberOfOccurrencesUsesHtmlRoot() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        WebElement found = mock(WebElement.class);

        when(path.getXPath()).thenReturn(java.util.Optional.of("//div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.<WebElement>empty());
        when(driver.findElement(By.xpath("/html" + XpathUtils.nOccurances("//div", 2, RelationOperator.exactly))))
                .thenReturn(found);

        assertSame(found, InBrowserFinder.findPageWithNumberOfOccurrences(driver, path, 2));
    }

    @Test
    public void findPageWithNumberOfOccurrencesUsesUnderlyingElement() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        WebElement underlying = mock(WebElement.class);
        WebElement found = mock(WebElement.class);
        String occurrencePath = XpathUtils.nOccurances("div", 1, RelationOperator.exactly);

        when(path.getXPath()).thenReturn(java.util.Optional.of("div"));
        when(path.getUnderlyingSource()).thenReturn(java.util.Optional.of(underlying));
        when(underlying.findElement(By.xpath("." + occurrencePath))).thenReturn(found);

        assertSame(found, InBrowserFinder.findPageWithNumberOfOccurrences(driver, path, 1));
    }

    @Test
    public void findPageWithNumberOfOccurrencesRejectsMissingPath() {
        WebDriver driver = mock(WebDriver.class);
        Path path = mock(Path.class);
        when(path.getXPath()).thenReturn(java.util.Optional.<String>empty());

        try {
            InBrowserFinder.findPageWithNumberOfOccurrences(driver, path, 1);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertEquals("findPageWithNumberOfOccurrences requires a path", expected.getMessage());
        }
    }
}
