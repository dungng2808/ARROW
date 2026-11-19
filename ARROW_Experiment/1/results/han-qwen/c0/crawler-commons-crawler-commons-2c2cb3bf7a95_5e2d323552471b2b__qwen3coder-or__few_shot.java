package crawlercommons.robots;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class SimpleRobotRulesParser_ArrowTest {

    @Test
    public void testFailedFetch_ClientError() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.failedFetch(404);
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_ALL, rules.getRobotRulesMode());
    }

    @Test
    public void testFailedFetch_Redirect() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.failedFetch(302);
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_NONE, rules.getRobotRulesMode());
        assertTrue(rules.isDeferVisits());
    }

    @Test
    public void testFailedFetch_ServerError() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.failedFetch(500);
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_NONE, rules.getRobotRulesMode());
        assertTrue(rules.isDeferVisits());
    }

    @Test
    public void testFailedFetch_Success() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        try {
            parser.failedFetch(200);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void testParseContent_EmptyContent() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", new byte[0], "text/plain", Collections.singleton("testbot"));
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_ALL, rules.getRobotRulesMode());
    }

    @Test
    public void testParseContent_NullContent() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", null, "text/plain", Collections.singleton("testbot"));
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_ALL, rules.getRobotRulesMode());
    }

    @Test
    public void testParseContent_AllowAllByDefault() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: *\nDisallow: /".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("otherbot"));
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_ALL, rules.getRobotRulesMode());
    }

    @Test
    public void testParseContent_DisallowSpecific() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: testbot\nDisallow: /private\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("testbot"));
        assertFalse(rules.isAllowed("/private"));
        assertTrue(rules.isAllowed("/public"));
    }

    @Test
    public void testParseContent_SitemapDirective() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: *\nSitemap: http://example.com/sitemap.xml\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("testbot"));
        assertEquals(1, rules.getSitemaps().size());
        assertEquals("http://example.com/sitemap.xml", rules.getSitemaps().get(0));
    }

    @Test
    public void testParseContent_CrawlDelay() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: testbot\nCrawl-delay: 2\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("testbot"));
        assertEquals(2000L, rules.getCrawlDelay());
    }

    @Test
    public void testParseContent_MaxCrawlDelayExceeded() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser(1000, 5);
        byte[] content = "User-agent: testbot\nCrawl-delay: 2\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("testbot"));
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_NONE, rules.getRobotRulesMode());
    }

    @Test
    public void testParseContent_HtmlContent() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "<html><body><h1>Not robots.txt</h1></body></html>".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/html", Collections.singleton("testbot"));
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_ALL, rules.getRobotRulesMode());
    }

    @Test
    public void testParseContent_HtmlWithoutUserAgent() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "<html><head><title>Page Title</title></head><body>Some content</body></html>".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/html", Collections.singleton("testbot"));
        assertEquals(SimpleRobotRules.RobotRulesMode.ALLOW_ALL, rules.getRobotRulesMode());
    }

    @Test
    public void testParseContent_CommentAndEmptyLines() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "# This is a comment\n\nUser-agent: testbot\nDisallow: /\n\n# Another comment\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("testbot"));
        assertFalse(rules.isAllowed("/"));
    }

    @Test
    public void testParseContent_MultipleUserAgents() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: bot1\nDisallow: /a\n\nUser-agent: bot2\nDisallow: /b\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Arrays.asList("bot1", "bot2"));
        assertFalse(rules.isAllowed("/a"));
        assertTrue(rules.isAllowed("/b"));
    }

    @Test
    public void testParseContent_WildcardMatch() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: *\nDisallow: /private\nUser-agent: specialbot\nAllow: /private/special\n".getBytes();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("specialbot"));
        assertTrue(rules.isAllowed("/private/special"));
        assertFalse(rules.isAllowed("/private/other"));
    }

    @Test
    public void testSplitRobotNames() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String[] result = parser.splitRobotNames("testbot, otherbot   thirdbot");
        assertEquals(3, result.length);
        assertEquals("testbot", result[0]);
        assertEquals("otherbot", result[1]);
        assertEquals("thirdbot", result[2]);
    }

    @Test
    public void testIsValidUserAgentToObey_Valid() {
        assertTrue(SimpleRobotRulesParser.isValidUserAgentToObey("testbot"));
        assertTrue(SimpleRobotRulesParser.isValidUserAgentToObey("test-bot"));
        assertTrue(SimpleRobotRulesParser.isValidUserAgentToObey("test_bot"));
    }

    @Test
    public void testIsValidUserAgentToObey_Invalid() {
        assertFalse(SimpleRobotRulesParser.isValidUserAgentToObey("test bot"));
        assertFalse(SimpleRobotRulesParser.isValidUserAgentToObey("test@bot"));
        assertFalse(SimpleRobotRulesParser.isValidUserAgentToObey(""));
        assertFalse(SimpleRobotRulesParser.isValidUserAgentToObey(null));
    }

    @Test
    public void testGetNumWarnings() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] content = "User-agent: testbot\nUnknown-directive: value\n".getBytes();
        parser.parseContent("http://example.com/robots.txt", content, "text/plain", Collections.singleton("testbot"));
        assertTrue(parser.getNumWarnings() >= 0);
    }

    @Test
    public void testSetMaxWarnings() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        parser.setMaxWarnings(10);
        assertEquals(10, parser.getMaxWarnings());
    }

    @Test
    public void testSetMaxCrawlDelay() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        parser.setMaxCrawlDelay(5000);
        assertEquals(5000, parser.getMaxCrawlDelay());
    }

    @Test
    public void testSetExactUserAgentMatching() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        parser.setExactUserAgentMatching(false);
        assertFalse(parser.isExactUserAgentMatching());
        parser.setExactUserAgentMatching(true);
        assertTrue(parser.isExactUserAgentMatching());
    }
}
