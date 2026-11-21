package crawlercommons.robots;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleRobotRulesParser_ArrowTest {

    @Test
    public void defaultConfiguration() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        assertEquals(SimpleRobotRulesParser.DEFAULT_MAX_CRAWL_DELAY, parser.getMaxCrawlDelay());
        assertEquals(SimpleRobotRulesParser.DEFAULT_MAX_WARNINGS, parser.getMaxWarnings());
        assertTrue(parser.isExactUserAgentMatching());
    }

    @Test
    public void configurableLimitsAndMatching() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser(1200L, 3);

        assertEquals(1200L, parser.getMaxCrawlDelay());
        assertEquals(3, parser.getMaxWarnings());

        parser.setMaxCrawlDelay(2500L);
        parser.setMaxWarnings(7);
        parser.setExactUserAgentMatching(false);

        assertEquals(2500L, parser.getMaxCrawlDelay());
        assertEquals(7, parser.getMaxWarnings());
        assertFalse(parser.isExactUserAgentMatching());
    }

    @Test
    public void failedFetchAllowsClientErrors() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        assertTrue(parser.failedFetch(404).isAllowed("/anything"));
    }

    @Test
    public void failedFetchDefersTemporaryErrors() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        assertFalse(parser.failedFetch(503).isAllowed("/anything"));
        assertTrue(parser.failedFetch(503).isDeferVisits());
    }

    @Test(expected = IllegalStateException.class)
    public void failedFetchRejectsSuccessfulStatus() {
        new SimpleRobotRulesParser().failedFetch(200);
    }

    @Test
    public void emptyContentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        Collection<String> agents = Arrays.asList("bot");

        SimpleRobotRules rules = parser.parseContent(
                "https://example.com/robots.txt", new byte[0], "text/plain", agents);

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void parsesRulesForMatchingAgent() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: bot\nDisallow: /private\nAllow: /private/open\n";
        Collection<String> agents = Arrays.asList("bot");

        SimpleRobotRules rules = parser.parseContent(
                "https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8),
                "text/plain",
                agents);

        assertFalse(rules.isAllowed("/private/page"));
        assertTrue(rules.isAllowed("/private/open"));
        assertTrue(rules.isAllowed("/public"));
    }

    @Test
    public void parsesWildcardRulesWhenNoAgentMatches() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: *\nDisallow: /blocked\n";
        Collection<String> agents = Arrays.asList("otherbot");

        SimpleRobotRules rules = parser.parseContent(
                "https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8),
                "text/plain",
                agents);

        assertFalse(rules.isAllowed("/blocked"));
        assertTrue(rules.isAllowed("/open"));
    }

    @Test
    public void htmlWithoutUserAgentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "<html><body>Not a robots file</body></html>";

        SimpleRobotRules rules = parser.parseContent(
                "https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8),
                "text/html",
                Arrays.asList("bot"));

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void crawlDelayAboveMaximumDisallowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser(1000L, 5);
        String content = "User-agent: bot\nCrawl-delay: 2\n";

        SimpleRobotRules rules = parser.parseContent(
                "https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8),
                "text/plain",
                Arrays.asList("bot"));

        assertFalse(rules.isAllowed("/anything"));
    }

    @Test
    public void parsesSitemapAndResolvesRelativeUrl() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "Sitemap: /sitemap.xml\n";

        SimpleRobotRules rules = parser.parseContent(
                "https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8),
                "text/plain",
                Arrays.asList("bot"));

        assertEquals(Arrays.asList("https://example.com/sitemap.xml"), rules.getSitemaps());
    }
}
