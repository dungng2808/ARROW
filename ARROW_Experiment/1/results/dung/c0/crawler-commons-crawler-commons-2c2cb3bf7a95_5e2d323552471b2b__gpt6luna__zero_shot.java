package crawlercommons.robots;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleRobotRulesParser_ArrowTest {

    @Test
    public void parsesMatchingAgentRules() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String robots = "User-agent: testbot\nDisallow: /private\nAllow: /private/open\n";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                robots.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("testbot"));

        assertFalse(rules.isAllowed("http://example.com/private"));
        assertTrue(rules.isAllowed("http://example.com/private/open"));
        assertTrue(rules.isAllowed("http://example.com/public"));
    }

    @Test
    public void fallsBackToWildcardRules() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String robots = "User-agent: *\nDisallow: /blocked\n";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                robots.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("otherbot"));

        assertFalse(rules.isAllowed("http://example.com/blocked"));
        assertTrue(rules.isAllowed("http://example.com/open"));
    }

    @Test
    public void emptyContentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", new byte[0], "text/plain",
                Collections.singleton("testbot"));

        assertTrue(rules.isAllowed("http://example.com/anything"));
    }

    @Test
    public void nullContentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", null, "text/plain",
                Collections.singleton("testbot"));

        assertTrue(rules.isAllowed("http://example.com/anything"));
    }

    @Test
    public void emptyDisallowClearsRestrictions() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String robots = "User-agent: testbot\nDisallow: /blocked\nDisallow:\n";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                robots.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("testbot"));

        assertTrue(rules.isAllowed("http://example.com/blocked"));
    }

    @Test
    public void parsesCrawlDelayInMilliseconds() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String robots = "User-agent: testbot\nCrawl-delay: 0.5\n";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                robots.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("testbot"));

        assertEquals(500L, rules.getCrawlDelay());
    }

    @Test
    public void excessiveCrawlDelayDisallowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser(1000L, 5);
        String robots = "User-agent: testbot\nCrawl-delay: 2\n";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                robots.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("testbot"));

        assertFalse(rules.isAllowed("http://example.com/anything"));
    }

    @Test
    public void resolvesRelativeSitemap() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String robots = "Sitemap: /sitemap.xml\n";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                robots.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("testbot"));

        assertEquals(Collections.singletonList("http://example.com/sitemap.xml"), rules.getSitemaps());
    }

    @Test
    public void failedClientFetchAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.failedFetch(404);

        assertTrue(rules.isAllowed("http://example.com/anything"));
    }

    @Test
    public void failedServerFetchDisallowsAllAndDefersVisits() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        SimpleRobotRules rules = parser.failedFetch(500);

        assertFalse(rules.isAllowed("http://example.com/anything"));
        assertTrue(rules.isDeferVisits());
    }

    @Test(expected = IllegalStateException.class)
    public void successfulStatusCannotBeUsedForFailedFetch() {
        new SimpleRobotRulesParser().failedFetch(200);
    }

    @Test
    public void parserConfigurationCanBeChanged() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser(1234L, 7);

        assertEquals(1234L, parser.getMaxCrawlDelay());
        assertEquals(7, parser.getMaxWarnings());
        assertTrue(parser.isExactUserAgentMatching());

        parser.setMaxCrawlDelay(4321L);
        parser.setMaxWarnings(3);
        parser.setExactUserAgentMatching(false);

        assertEquals(4321L, parser.getMaxCrawlDelay());
        assertEquals(3, parser.getMaxWarnings());
        assertFalse(parser.isExactUserAgentMatching());
    }

    @Test
    public void parsesUtf8Bom() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] text = "User-agent: testbot\nDisallow: /blocked\n".getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[text.length + 3];
        content[0] = (byte) 0xEF;
        content[1] = (byte) 0xBB;
        content[2] = (byte) 0xBF;
        System.arraycopy(text, 0, content, 3, text.length);

        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt", content, "text/plain",
                Collections.singleton("testbot"));

        assertFalse(rules.isAllowed("http://example.com/blocked"));
    }

    @Test
    public void htmlWithoutUserAgentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String html = "<html><body>Not a robots file</body></html>";
        SimpleRobotRules rules = parser.parseContent("http://example.com/robots.txt",
                html.getBytes(StandardCharsets.UTF_8), "text/html", Collections.singleton("testbot"));

        assertTrue(rules.isAllowed("http://example.com/anything"));
    }

    @Test
    public void recordsWarningsForUnknownLines() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String robots = "User-agent: testbot\nnot a directive\n";
        parser.parseContent("http://example.com/robots.txt", robots.getBytes(StandardCharsets.UTF_8), "text/plain",
                Arrays.asList("testbot"));

        assertEquals(1, parser.getNumWarnings());
    }
}
