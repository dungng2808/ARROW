package edu.jhuapl.dorset.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.agents.Agent;

public class RegexRouter_ArrowTest {

    @Test
    public void routeMatchesRegexWithoutCaseSensitivity() {
        Agent agent = new Agent("agent");
        RouterAgentConfig config = new RouterAgentConfig();
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
        Agent agent = new Agent("agent");
        RouterAgentConfig config = new RouterAgentConfig();
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);

        assertEquals(0, router.route(new Request("hello there")).length);
    }

    @Test
    public void routeReturnsEachMatchingAgentOnlyOnce() {
        Agent agent = new Agent("agent");
        RouterAgentConfig config = new RouterAgentConfig();
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, new String[] { "hello", "HELLO" });
        config.add(entry);

        RegexRouter router = new RegexRouter(config);
        Agent[] result = router.route(new Request("hello"));

        assertEquals(1, result.length);
        assertEquals(agent, result[0]);
    }

    @Test
    public void getAgentsContainsAgentsWithRegexConfiguration() {
        Agent configured = new Agent("configured");
        Agent unconfigured = new Agent("unconfigured");
        RouterAgentConfig config = new RouterAgentConfig();

        RouterAgentConfigEntry configuredEntry = new RouterAgentConfigEntry(configured);
        configuredEntry.getParams().put(RegexRouter.REGEX, ".*");
        config.add(configuredEntry);
        config.add(new RouterAgentConfigEntry(unconfigured));

        RegexRouter router = new RegexRouter(config);
        Set<Agent> result = new HashSet<Agent>();
        for (Agent agent : router.getAgents()) {
            result.add(agent);
        }

        assertEquals(1, result.size());
        assertTrue(result.contains(configured));
        assertFalse(result.contains(unconfigured));
    }

    @Test
    public void routeReturnsNoAgentsWhenNothingMatches() {
        Agent agent = new Agent("agent");
        RouterAgentConfig config = new RouterAgentConfig();
        RouterAgentConfigEntry entry = new RouterAgentConfigEntry(agent);
        entry.getParams().put(RegexRouter.REGEX, "hello");
        config.add(entry);

        RegexRouter router = new RegexRouter(config);

        assertEquals(0, router.route(new Request("goodbye")).length);
    }
}
