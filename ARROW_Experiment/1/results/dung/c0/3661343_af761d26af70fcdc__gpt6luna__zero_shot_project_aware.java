package org.xcolab.commons.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlUtil_ArrowTest {

    @Test
    public void cleanAllRemovesTags() {
        assertEquals("Hello world", HtmlUtil.cleanAll("<b>Hello</b> world"));
    }

    @Test
    public void cleanAllReturnsEmptyForEmptyInput() {
        assertEquals("", HtmlUtil.cleanAll(""));
    }

    @Test
    public void cleanMostKeepsSimpleFormatting() {
        assertEquals("<b>Hello</b>", HtmlUtil.cleanMost("<b>Hello</b><script>alert(1)</script>"));
    }

    @Test
    public void cleanSomeKeepsSafeLinksAndImages() {
        assertEquals("<a href=\"https://example.com\">site</a>",
                HtmlUtil.cleanSome("<a href=\"https://example.com\" onclick=\"bad()\">site</a>", ""));
    }

    @Test
    public void cleanUsesProvidedWhitelist() {
        assertEquals("<b>text</b>", HtmlUtil.clean("<b>text</b><i>more</i>",
                Whitelist.none().addTags("b"), ""));
    }

    @Test
    public void decodeHtmlEntitiesForEmailUnescapesEntities() {
        assertEquals("Tom's & Jerry", HtmlUtil.decodeHTMLEntitiesForEmail("Tom&apos;s &amp; Jerry"));
    }

    @Test
    public void linkifyUrlsInTextConvertsUrl() {
        assertEquals("Visit <a rel='nofollow' href='http://example.com'>example.com</a> now",
                HtmlUtil.linkifyUrlsInText("Visit example.com now"));
    }

    @Test
    public void linkifyUrlsInHtmlLeavesExistingAnchorAlone() {
        assertEquals("<a href=\"http://example.com\">example.com</a>",
                HtmlUtil.linkifyUrlsInHtml("<a href=\"http://example.com\">example.com</a>"));
    }

    @Test
    public void makeRelativeLinksAbsoluteAddsBaseUrl() {
        assertEquals("<a href=\"https://example.com/path\">x</a>",
                HtmlUtil.makeRelativeLinksAbsolute("<a href=\"/path\">x</a>", "https://example.com"));
    }

    @Test
    public void createLinkAddsHttpSchemeWhenMissing() {
        assertEquals("<a rel='nofollow' href='http://example.com'>Example</a>",
                HtmlUtil.createLink("example.com", "Example"));
    }

    @Test
    public void createLinkPreservesExistingScheme() {
        assertEquals("<a rel='nofollow' href='https://example.com'>Example</a>",
                HtmlUtil.createLink("https://example.com", "Example"));
    }

    @Test
    public void addHtmlLineBreaksReplacesNewlines() {
        assertEquals("first <br />\nsecond", HtmlUtil.addHtmlLineBreaks("first\nsecond"));
    }

    @Test
    public void filterAndFormatContentAddsBreaksAndLinkifies() {
        assertEquals("Visit <a rel='nofollow' href='http://example.com'>example.com</a> <br />\nnow",
                HtmlUtil.filterAndFormatContent("Visit example.com\nnow"));
    }

    @Test
    public void addNoFollowToLinkTagsReplacesLinkWithNoFollowLink() {
        Document document = Jsoup.parse("<a href=\"http://example.com\">Example</a>");
        Document result = HtmlUtil.addNoFollowToLinkTagsInDocument(document);
        assertEquals("<a rel=\"nofollow\" href=\"http://example.com\">Example</a>",
                result.body().html());
    }
}
