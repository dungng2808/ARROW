package org.xcolab.commons.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlUtil_ArrowTest {

    @Test
    public void testCleanAll() {
        String input = "<p>Hello <b>world</b></p>";
        String expected = "Hello world";
        assertEquals(expected, HtmlUtil.cleanAll(input));
    }

    @Test
    public void testCleanMost() {
        String input = "<p>Hello <b>world</b></p>";
        String expected = "Hello world";
        assertEquals(expected, HtmlUtil.cleanMost(input));
    }

    @Test
    public void testCleanSome() {
        String input = "<p>Hello <img src='test.jpg'> world</p>";
        String expected = "Hello <img src=\"test.jpg\"> world";
        assertEquals(expected, HtmlUtil.cleanSome(input, ""));
    }

    @Test
    public void testCleanWithEmptyInput() {
        assertEquals("", HtmlUtil.clean("", org.jsoup.safety.Whitelist.none(), ""));
    }

    @Test
    public void testCleanWithNullInput() {
        assertEquals("", HtmlUtil.clean(null, org.jsoup.safety.Whitelist.none(), ""));
    }

    @Test
    public void testDecodeHTMLEntitiesForEmail() {
        String input = "It&#39;s a test";
        String expected = "It's a test";
        assertEquals(expected, HtmlUtil.decodeHTMLEntitiesForEmail(input));
    }

    @Test
    public void testLinkifyUrlsInText() {
        String input = "Visit http://example.com for more info";
        String expected = "Visit <a rel='nofollow' href='http://http://example.com'>http://example.com</a> for more info";
        assertEquals(expected, HtmlUtil.linkifyUrlsInText(input));
    }

    @Test
    public void testLinkifyUrlsInHtml() {
        String input = "<a href='http://example.com'>Link</a> Visit http://example.com for more info";
        String expected = "<a href='http://example.com'>Link</a> Visit <a rel='nofollow' href='http://http://example.com'>http://example.com</a> for more info";
        assertEquals(expected, HtmlUtil.linkifyUrlsInHtml(input));
    }

    @Test
    public void testMakeRelativeLinksAbsolute() {
        String input = "<a href='/test'>Link</a>";
        String expected = "<a href='http://example.com/test'>Link</a>";
        assertEquals(expected, HtmlUtil.makeRelativeLinksAbsolute(input, "http://example.com"));
    }

    @Test
    public void testCreateLink() {
        String input = "example.com";
        String expected = "<a rel='nofollow' href='http://example.com'>example.com</a>";
        assertEquals(expected, HtmlUtil.createLink(input, input));
    }

    @Test
    public void testAddHtmlLineBreaks() {
        String input = "Line1\nLine2";
        String expected = "Line1 <br />\nLine2";
        assertEquals(expected, HtmlUtil.addHtmlLineBreaks(input));
    }

    @Test
    public void testFilterAndFormatContent() {
        String input = "Line1\nLine2";
        String expected = "Line1 <br />\nLine2";
        assertEquals(expected, HtmlUtil.filterAndFormatContent(input));
    }

    @Test
    public void testAddNoFollowToLinkTagsInDocument() {
        String input = "<a href='http://example.com'>Link</a>";
        Document doc = Jsoup.parse(input);
        Document result = HtmlUtil.addNoFollowToLinkTagsInDocument(doc);
        String expected = "<a rel=\"nofollow\" href=\"http://example.com\">Link</a>";
        assertEquals(expected, result.body().html());
    }
}
