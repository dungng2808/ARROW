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
    public void testCleanAllEmpty() {
        assertEquals("", HtmlUtil.cleanAll(""));
    }

    @Test
    public void testCleanAllNull() {
        assertEquals("", HtmlUtil.cleanAll(null));
    }

    @Test
    public void testCleanMost() {
        String input = "<p>Hello <b>world</b></p>";
        String expected = "Hello world";
        assertEquals(expected, HtmlUtil.cleanMost(input));
    }

    @Test
    public void testCleanMostEmpty() {
        assertEquals("", HtmlUtil.cleanMost(""));
    }

    @Test
    public void testCleanSome() {
        String input = "<p>Hello <img src='test.jpg'> world</p>";
        String expected = "Hello <img src=\"test.jpg\"> world";
        assertEquals(expected, HtmlUtil.cleanSome(input, ""));
    }

    @Test
    public void testCleanSomeEmpty() {
        assertEquals("", HtmlUtil.cleanSome("", ""));
    }

    @Test
    public void testClean() {
        String input = "<p>Hello <b>world</b></p>";
        String expected = "Hello world";
        assertEquals(expected, HtmlUtil.clean(input, org.jsoup.safety.Whitelist.simpleText(), ""));
    }

    @Test
    public void testCleanEmpty() {
        assertEquals("", HtmlUtil.clean("", org.jsoup.safety.Whitelist.none(), ""));
    }

    @Test
    public void testDecodeHTMLEntitiesForEmail() {
        String input = "Hello &quot;world&quot; &apos;test&apos;";
        String expected = "Hello \"world\" 'test'";
        assertEquals(expected, HtmlUtil.decodeHTMLEntitiesForEmail(input));
    }

    @Test
    public void testLinkifyUrlsInText() {
        String input = "Visit http://example.com for more info";
        String expected = "Visit <a rel='nofollow' href='http://http://example.com'>http://example.com</a> for more info";
        assertEquals(expected, HtmlUtil.linkifyUrlsInText(input));
    }

    @Test
    public void testLinkifyUrlsInTextNoUrl() {
        String input = "No URL here";
        assertEquals(input, HtmlUtil.linkifyUrlsInText(input));
    }

    @Test
    public void testLinkifyUrlsInHtml() {
        String input = "<p>Visit http://example.com for more info</p>";
        String expected = "<p>Visit <a rel='nofollow' href='http://http://example.com'>http://example.com</a> for more info</p>";
        assertEquals(expected, HtmlUtil.linkifyUrlsInHtml(input));
    }

    @Test
    public void testLinkifyUrlsInHtmlWithExistingLink() {
        String input = "<p>Visit <a href='http://example.com'>link</a> for more info</p>";
        String expected = "<p>Visit <a href='http://example.com'>link</a> for more info</p>";
        assertEquals(expected, HtmlUtil.linkifyUrlsInHtml(input));
    }

    @Test
    public void testMakeRelativeLinksAbsolute() {
        String input = "<a href='/test'>link</a>";
        String expected = "<a href='http://example.com/test'>link</a>";
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
        String input = "Hello\nWorld";
        String expected = "Hello <br />\nWorld";
        assertEquals(expected, HtmlUtil.addHtmlLineBreaks(input));
    }

    @Test
    public void testFilterAndFormatContent() {
        String input = "Hello\nWorld";
        String expected = "Hello <br />\nWorld";
        assertEquals(expected, HtmlUtil.filterAndFormatContent(input));
    }

    @Test
    public void testFilterAndFormatContentWithBr() {
        String input = "Hello<br />World";
        String expected = "Hello<br />World";
        assertEquals(expected, HtmlUtil.filterAndFormatContent(input));
    }

    @Test
    public void testAddNoFollowToLinkTagsInDocument() {
        String input = "<a href='http://example.com'>link</a>";
        Document doc = Jsoup.parse(input);
        Document result = HtmlUtil.addNoFollowToLinkTagsInDocument(doc);
        String expected = "<a rel=\"nofollow\" href=\"http://example.com\">link</a>";
        assertEquals(expected, result.body().html());
    }
}
