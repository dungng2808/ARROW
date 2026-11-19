package edu.jhuapl.dorset.routing;

import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.agents.Agent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexRouter_ArrowTest {

    @Test
    public void routeMatchesRegexWithoutCaseSensitivity() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello.*");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);
        Agent[] result = router.route(new Request("HELLO world"));

        assertEquals(1, result.length);
        assertEquals(agent, result[0]);
    }

    @Test
    public void routeRequiresTheEntireTextToMatch() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);

        assertEquals(0, router.route(new Request("hello there")).length);
    }

    @Test
    public void getAgentsContainsAgentsWithRegexConfiguration() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent configuredAgent = new Agent("configured");
        RouterAgentConfigEntry configuredEntry = new RouterAgentConfigEntry(configuredAgent);
        configuredEntry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(configuredEntry);

        Agent unconfiguredAgent = new Agent("unconfigured");
        config.add(new RouterAgentConfigEntry(unconfiguredAgent));

        RegexRouter router = new RegexRouter(config);
        Agent[] result = router.getAgents();

        assertEquals(1, result.length);
        assertTrue(result[0] == configuredAgent);
        assertFalse(result[0] == unconfiguredAgent);
    }

    @Test
    public void routeReturnsNoAgentsWhenNoRegexMatches() {
        RouterAgentConfig config = new RouterAgentConfig();
        Agent agent = new Agent("agent");
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);

        assertEquals(0, router.route(new Request("goodbye")).length);
    }
}
