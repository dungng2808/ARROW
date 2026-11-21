package org.xcolab.commons.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlUtil_ArrowTest {

    @Test
    public void testCleanAll() {
        String input = "<p>Hello <b>world</b></p>";
        String result = HtmlUtil.cleanAll(input);
        assertEquals("Hello world", result);
    }

    @Test
    public void testCleanMost() {
        String input = "<p>Hello <script>alert('xss')</script>world</p>";
        String result = HtmlUtil.cleanMost(input);
        assertEquals("Hello alert('xss')world", result);
    }

    @Test
    public void testCleanSome() {
        String input = "<p>Hello <img src='test.jpg'> world</p>";
        String result = HtmlUtil.cleanSome(input, "http://example.com");
        assertEquals("Hello <img src=\"test.jpg\"> world", result);
    }

    @Test
    public void testCleanWithEmptyInput() {
        String result = HtmlUtil.clean("", null, null);
        assertEquals("", result);
    }

    @Test
    public void testDecodeHTMLEntitiesForEmail() {
        String input = "It&#39;s a test &quot;quoted&quot;";
        String result = HtmlUtil.decodeHTMLEntitiesForEmail(input);
        assertEquals("It's a test \"quoted\"", result);
    }

    @Test
    public void testLinkifyUrlsInText() {
        String input = "Visit http://example.com for more info";
        String result = HtmlUtil.linkifyUrlsInText(input);
        assertTrue(result.contains("<a rel='nofollow' href='http://http://example.com'>http://example.com</a>"));
    }

    @Test
    public void testLinkifyUrlsInHtml() {
        String input = "<a href='http://example.com'>link</a> Visit http://example.com";
        String result = HtmlUtil.linkifyUrlsInHtml(input);
        assertTrue(result.contains("<a href='http://example.com'>link</a>"));
    }

    @Test
    public void testMakeRelativeLinksAbsolute() {
        String input = "<a href='/test'>link</a>";
        String result = HtmlUtil.makeRelativeLinksAbsolute(input, "http://example.com");
        assertEquals("<a href='http://example.com/test'>link</a>", result);
    }

    @Test
    public void testCreateLink() {
        String result = HtmlUtil.createLink("example.com", "Example");
        assertEquals("<a rel='nofollow' href='http://example.com'>Example</a>", result);
    }

    @Test
    public void testAddHtmlLineBreaks() {
        String input = "Line1\nLine2";
        String result = HtmlUtil.addHtmlLineBreaks(input);
        assertEquals("Line1 <br />\nLine2", result);
    }

    @Test
    public void testFilterAndFormatContent() {
        String input = "Visit http://example.com\nNew line";
        String result = HtmlUtil.filterAndFormatContent(input);
        assertTrue(result.contains("<a rel='nofollow' href='http://http://example.com'>http://example.com</a>"));
    }

    @Test
    public void testAddNoFollowToLinkTagsInDocument() {
        String html = "<a href='http://example.com'>link</a>";
        Document doc = Jsoup.parse(html);
        Document result = HtmlUtil.addNoFollowToLinkTagsInDocument(doc);
        assertTrue(result.body().html().contains("rel='nofollow'"));
    }
}
