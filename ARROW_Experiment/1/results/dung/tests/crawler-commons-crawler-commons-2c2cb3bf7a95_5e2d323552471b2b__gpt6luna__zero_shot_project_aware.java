package crawlercommons.robots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class SimpleRobotRulesParser_ArrowTest {

    @Test
    public void defaultConfigurationIsExactMatching() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        assertEquals(SimpleRobotRulesParser.DEFAULT_MAX_CRAWL_DELAY, parser.getMaxCrawlDelay());
        assertEquals(SimpleRobotRulesParser.DEFAULT_MAX_WARNINGS, parser.getMaxWarnings());
        assertTrue(parser.isExactUserAgentMatching());
    }

    @Test
    public void configuredValuesCanBeReadAndUpdated() {
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
    public void emptyContentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt", new byte[0], "text/plain",
                Collections.singleton("bot"));

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void nullContentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt", null, "text/plain",
                Collections.singleton("bot"));

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void matchingAgentDisallowAndAllowRulesAreApplied() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: bot\nDisallow: /private\nAllow: /private/open\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertFalse(rules.isAllowed("/private/page"));
        assertTrue(rules.isAllowed("/private/open/page"));
        assertTrue(rules.isAllowed("/public"));
    }

    @Test
    public void wildcardRulesAreUsedWhenNoNamedAgentMatches() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: *\nDisallow: /blocked\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("other"));

        assertFalse(rules.isAllowed("/blocked"));
        assertTrue(rules.isAllowed("/open"));
    }

    @Test
    public void namedAgentRulesTakePrecedenceOverEarlierWildcardRules() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: *\nDisallow: /wild\nUser-agent: bot\nDisallow: /named\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertFalse(rules.isAllowed("/named"));
        assertTrue(rules.isAllowed("/wild"));
    }

    @Test
    public void emptyDisallowClearsRules() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: bot\nDisallow: /blocked\nDisallow:\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertTrue(rules.isAllowed("/blocked"));
    }

    @Test
    public void crawlDelayIsConvertedToMilliseconds() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: bot\nCrawl-delay: 1.5\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertEquals(1500L, rules.getCrawlDelay());
    }

    @Test
    public void excessiveCrawlDelayDisallowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser(1000L, 5);
        String content = "User-agent: bot\nCrawl-delay: 2\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertFalse(rules.isAllowed("/anything"));
    }

    @Test
    public void sitemapRelativeUrlIsResolvedAgainstRobotsUrl() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "Sitemap: /sitemap.xml\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertEquals(Collections.singletonList("https://example.com/sitemap.xml"), rules.getSitemaps());
    }

    @Test
    public void htmlWithoutUserAgentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "<html><body>not a robots file</body></html>";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void htmlContentTypeWithoutUserAgentAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "<html><body>not a robots file</body></html>";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/html", Collections.singleton("bot"));

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void failedClientFetchAllowsAll() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        SimpleRobotRules rules = parser.failedFetch(404);

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void failedRedirectFetchDisallowsAllAndDefersVisits() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        SimpleRobotRules rules = parser.failedFetch(302);

        assertFalse(rules.isAllowed("/anything"));
        assertTrue(rules.isDeferVisits());
    }

    @Test
    public void failedServerFetchDisallowsAllAndDefersVisits() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        SimpleRobotRules rules = parser.failedFetch(503);

        assertFalse(rules.isAllowed("/anything"));
        assertTrue(rules.isDeferVisits());
    }

    @Test
    public void successfulStatusCannotBeUsedForFailedFetch() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();

        assertThrows(IllegalStateException.class, () -> parser.failedFetch(200));
    }

    @Test
    public void parseRecordsWarningsForUnknownDirective() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: bot\nUnknown-directive: value\n";
        parser.parseContent("https://example.com/robots.txt", content.getBytes(StandardCharsets.UTF_8), "text/plain",
                Collections.singleton("bot"));

        assertEquals(1, parser.getNumWarnings());
    }

    @Test
    public void parseRecordsWarningsForUnknownLine() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: bot\nunrecognized line\n";
        parser.parseContent("https://example.com/robots.txt", content.getBytes(StandardCharsets.UTF_8), "text/plain",
                Collections.singleton("bot"));

        assertEquals(1, parser.getNumWarnings());
    }

    @Test
    public void utf8BomIsIgnored() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        byte[] text = "User-agent: bot\nDisallow: /blocked\n".getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[text.length + 3];
        content[0] = (byte) 0xEF;
        content[1] = (byte) 0xBB;
        content[2] = (byte) 0xBF;
        System.arraycopy(text, 0, content, 3, text.length);

        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt", content, "text/plain",
                Collections.singleton("bot"));

        assertFalse(rules.isAllowed("/blocked"));
    }

    @Test
    public void multipleRobotNamesCanSelectMatchingGroup() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: other\nDisallow: /other\nUser-agent: bot\nDisallow: /bot\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Arrays.asList("other", "bot"));

        assertFalse(rules.isAllowed("/other"));
        assertTrue(rules.isAllowed("/bot"));
    }

    @Test
    public void unmatchedAgentAllowsAllWhenNoWildcardGroupExists() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "User-agent: someone\nDisallow: /\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertTrue(rules.isAllowed("/anything"));
    }

    @Test
    public void malformedSitemapIsIgnoredAndWarned() {
        SimpleRobotRulesParser parser = new SimpleRobotRulesParser();
        String content = "Sitemap: not a valid URL\n";
        SimpleRobotRules rules = parser.parseContent("https://example.com/robots.txt",
                content.getBytes(StandardCharsets.UTF_8), "text/plain", Collections.singleton("bot"));

        assertNotNull(rules);
        assertEquals(Collections.emptyList(), rules.getSitemaps());
        assertEquals(1, parser.getNumWarnings());
    }
}
