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
    public void cleanAllReturnsEmptyForEmptyInput() {
        assertEquals("", HtmlUtil.cleanAll(""));
    }

    @Test
    public void cleanMostKeepsSimpleTextFormatting() {
        assertEquals("<b>Hello</b>", HtmlUtil.cleanMost("<b>Hello</b><script>bad</script>"));
    }

    @Test
    public void cleanSomeKeepsBasicLinks() {
        assertEquals("<a href=\"https://example.com\">site</a>",
                HtmlUtil.cleanSome("<a href=\"https://example.com\">site</a>", ""));
    }

    @Test
    public void cleanUsesProvidedWhitelist() {
        assertEquals("<b>Hello</b>", HtmlUtil.clean("<b>Hello</b><i>there</i>",
                Whitelist.basic(), ""));
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
    public void linkifyUrlsInTextLeavesTextWithoutUrlUnchanged() {
        assertEquals("plain text", HtmlUtil.linkifyUrlsInText("plain text"));
    }

    @Test
    public void linkifyUrlsInHtmlDoesNotLinkifyExistingAnchor() {
        assertEquals("<a href=\"http://example.com\">example.com</a>",
                HtmlUtil.linkifyUrlsInHtml("<a href=\"http://example.com\">example.com</a>"));
    }

    @Test
    public void makeRelativeLinksAbsoluteAddsBaseUrl() {
        assertEquals("href=\"https://example.com/path\"",
                HtmlUtil.makeRelativeLinksAbsolute("href=\"/path\"", "https://example.com"));
    }

    @Test
    public void createLinkAddsHttpSchemeWhenMissing() {
        assertEquals("<a rel='nofollow' href='http://example.com'>Example</a>",
                HtmlUtil.createLink("example.com", "Example"));
    }

    @Test
    public void createLinkKeepsExistingHttpsScheme() {
        assertEquals("<a rel='nofollow' href='https://example.com'>Example</a>",
                HtmlUtil.createLink("https://example.com", "Example"));
    }

    @Test
    public void addHtmlLineBreaksReplacesNewlines() {
        assertEquals("first <br />\nsecond", HtmlUtil.addHtmlLineBreaks("first\nsecond"));
    }

    @Test
    public void filterAndFormatContentFormatsNewlinesAndUrls() {
        assertEquals("Visit <a rel='nofollow' href='http://example.com'>example.com</a> <br />\nnow",
                HtmlUtil.filterAndFormatContent("Visit example.com\nnow"));
    }

    @Test
    public void addNoFollowToLinkTagsInDocumentAddsNoFollowLink() {
        Document document = Jsoup.parse("<a href=\"https://example.com\">Example</a>");
        Document result = HtmlUtil.addNoFollowToLinkTagsInDocument(document);
        assertEquals(1, result.select("a[rel=nofollow]").size());
        assertEquals("https://example.com", result.select("a[rel=nofollow]").first().attr("href"));
        assertEquals("Example", result.select("a[rel=nofollow]").first().text());
    }
}
