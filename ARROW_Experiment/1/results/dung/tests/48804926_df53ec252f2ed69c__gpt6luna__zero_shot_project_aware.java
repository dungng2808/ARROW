package edu.jhuapl.dorset.routing;

import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.agents.Agent;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RegexRouter_ArrowTest {

    @Test
    public void routesMatchingTextWithoutCaseSensitivity() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);
        Agent[] result = router.route(new Request("HELLO"));

        assertArrayEquals(new Agent[] { agent }, result);
    }

    @Test
    public void doesNotRouteTextThatDoesNotMatchEntirePattern() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);
        Agent[] result = router.route(new Request("hello there"));

        assertEquals(0, result.length);
    }

    @Test
    public void getAgentsIncludesAgentsWithRegexConfiguration() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);

        assertArrayEquals(new Agent[] { agent }, router.getAgents());
    }

    @Test
    public void getAgentsExcludesAgentsWithoutRegexConfiguration() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        config.add(new RouterAgentConfigEntry(agent));

        RegexRouter router = new RegexRouter(config);

        assertEquals(0, router.getAgents().length);
    }
}
