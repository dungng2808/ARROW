package org.xcolab.commons.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlUtil_ArrowTest {

    @Test
    public void cleanAllRemovesTags() {
        assertEquals("Hello", HtmlUtil.cleanAll("<b>Hello</b>"));
    }

    @Test
    public void cleanAllReturnsEmptyForNull() {
        assertEquals("", HtmlUtil.cleanAll(null));
    }

    @Test
    public void cleanMostKeepsTextAndRemovesFormattingTags() {
        assertEquals("Hello", HtmlUtil.cleanMost("<b>Hello</b>"));
    }

    @Test
    public void cleanSomeKeepsBasicFormatting() {
        assertEquals("<p><b>Hello</b></p>", HtmlUtil.cleanSome("<p><b>Hello</b></p>", ""));
    }

    @Test
    public void cleanReturnsEmptyForEmptyInput() {
        assertEquals("", HtmlUtil.clean("", Whitelist.none(), ""));
    }

    @Test
    public void decodeHtmlEntitiesForEmailUnescapesApostrophe() {
        assertEquals("Tom's & Jerry", HtmlUtil.decodeHTMLEntitiesForEmail("Tom&apos;s &amp; Jerry"));
    }

    @Test
    public void linkifyUrlsInTextConvertsUrl() {
        assertEquals("Visit <a rel='nofollow' href='http://example.com'>http://example.com</a> now",
                HtmlUtil.linkifyUrlsInText("Visit example.com now"));
    }

    @Test
    public void linkifyUrlsInTextLeavesTextWithoutUrlUnchanged() {
        assertEquals("plain text", HtmlUtil.linkifyUrlsInText("plain text"));
    }

    @Test
    public void linkifyUrlsInHtmlDoesNotLinkifyExistingAnchor() {
        assertEquals("<a href='http://example.com'>example.com</a> and "
                        + "<a rel='nofollow' href='http://other.com'>http://other.com</a>",
                HtmlUtil.linkifyUrlsInHtml(
                        "<a href='http://example.com'>example.com</a> and other.com"));
    }

    @Test
    public void makeRelativeLinksAbsoluteAddsBaseUrl() {
        assertEquals("<a href='https://example.com/path'>x</a>",
                HtmlUtil.makeRelativeLinksAbsolute("<a href='/path'>x</a>", "https://example.com"));
    }

    @Test
    public void createLinkAddsHttpSchemeWhenMissing() {
        assertEquals("<a rel='nofollow' href='http://example.com'>Example</a>",
                HtmlUtil.createLink("example.com", "Example"));
    }

    @Test
    public void createLinkPreservesHttpScheme() {
        assertEquals("<a rel='nofollow' href='https://example.com'>Example</a>",
                HtmlUtil.createLink("https://example.com", "Example"));
    }

    @Test
    public void addHtmlLineBreaksReplacesNewlines() {
        assertEquals("first <br />\nsecond",
                HtmlUtil.addHtmlLineBreaks("first\nsecond"));
    }

    @Test
    public void filterAndFormatContentAddsBreaksAndLinks() {
        assertEquals("Visit <a rel='nofollow' href='http://example.com'>http://example.com</a> <br />\nnext",
                HtmlUtil.filterAndFormatContent("Visit example.com\nnext"));
    }

    @Test
    public void addNoFollowToLinkTagsInDocumentReplacesAnchorWithNofollowLink() {
        Document document = Jsoup.parseBodyFragment("<a href='http://example.com'>Example</a>");
        Document result = HtmlUtil.addNoFollowToLinkTagsInDocument(document);
        assertEquals("<a rel=\"nofollow\" href=\"http://example.com\">Example</a>",
                result.body().html());
    }
}
