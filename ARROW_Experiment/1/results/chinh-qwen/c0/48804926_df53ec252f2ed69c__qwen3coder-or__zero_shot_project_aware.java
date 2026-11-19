package edu.jhuapl.dorset.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.agents.Agent;

public class RegexRouter_ArrowTest {

    @Test
    public void testConstructorWithNullRegexes() {
        // Test that when regexes are null, no patterns are added to the map
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(java.util.Collections.singletonList(entry).iterator());
        Mockito.when(entry.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry.getParams().getStrings(RegexRouter.REGEX)).thenReturn(null);
        Mockito.when(entry.getAgent()).thenReturn(agent);
        
        RegexRouter router = new RegexRouter(config);
        
        // Verify that no agents were added to the internal structures
        Agent[] returnedAgents = router.getAgents();
        assertEquals(0, returnedAgents.length);
    }

    @Test
    public void testRouteMatching() {
        // Test routing with matching regex
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(java.util.Collections.singletonList(entry).iterator());
        Mockito.when(entry.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"hello.*"});
        Mockito.when(entry.getAgent()).thenReturn(agent);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getText()).thenReturn("hello world");
        
        Agent[] result = router.route(request);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(agent, result[0]);
    }

    @Test
    public void testRouteNoMatch() {
        // Test routing with no matching regex
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(java.util.Collections.singletonList(entry).iterator());
        Mockito.when(entry.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"goodbye.*"});
        Mockito.when(entry.getAgent()).thenReturn(agent);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getText()).thenReturn("hello world");
        
        Agent[] result = router.route(request);
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testGetAgents() {
        // Test getting all agents
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry1 = Mockito.mock(RouterAgentConfigEntry.class);
        RouterAgentConfigEntry entry2 = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent1 = Mockito.mock(Agent.class);
        Agent agent2 = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(
            java.util.Arrays.asList(entry1, entry2).iterator());
        Mockito.when(entry1.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry1.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"test.*"});
        Mockito.when(entry1.getAgent()).thenReturn(agent1);
        Mockito.when(entry2.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry2.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"another.*"});
        Mockito.when(entry2.getAgent()).thenReturn(agent2);
        
        RegexRouter router = new RegexRouter(config);
        
        Agent[] result = router.getAgents();
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertTrue(result[0] == agent1 || result[1] == agent1);
        assertTrue(result[0] == agent2 || result[1] == agent2);
    }

    @Test
    public void testMultipleRegexesForSameAgent() {
        // Test multiple regexes for the same agent
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(java.util.Collections.singletonList(entry).iterator());
        Mockito.when(entry.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"hello.*", "hi.*"});
        Mockito.when(entry.getAgent()).thenReturn(agent);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request1 = Mockito.mock(Request.class);
        Mockito.when(request1.getText()).thenReturn("hello world");
        
        Request request2 = Mockito.mock(Request.class);
        Mockito.when(request2.getText()).thenReturn("hi there");
        
        Agent[] result1 = router.route(request1);
        Agent[] result2 = router.route(request2);
        
        assertNotNull(result1);
        assertEquals(1, result1.length);
        assertEquals(agent, result1[0]);
        
        assertNotNull(result2);
        assertEquals(1, result2.length);
        assertEquals(agent, result2[0]);
    }

    @Test
    public void testCaseInsensitiveRouting() {
        // Test that routing is case insensitive
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(java.util.Collections.singletonList(entry).iterator());
        Mockito.when(entry.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"HELLO.*"});
        Mockito.when(entry.getAgent()).thenReturn(agent);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getText()).thenReturn("hello world");
        
        Agent[] result = router.route(request);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(agent, result[0]);
    }

    @Test
    public void testEmptyConfig() {
        // Test with empty configuration
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        Mockito.when(config.iterator()).thenReturn(java.util.Collections.emptyIterator());
        
        RegexRouter router = new RegexRouter(config);
        
        Agent[] result = router.getAgents();
        assertEquals(0, result.length);
    }

    @Test
    public void testDuplicateAgents() {
        // Test that duplicate agents don't cause issues
        RouterAgentConfig config = Mockito.mock(RouterAgentConfig.class);
        RouterAgentConfigEntry entry1 = Mockito.mock(RouterAgentConfigEntry.class);
        RouterAgentConfigEntry entry2 = Mockito.mock(RouterAgentConfigEntry.class);
        Agent agent = Mockito.mock(Agent.class);
        
        Mockito.when(config.iterator()).thenReturn(
            java.util.Arrays.asList(entry1, entry2).iterator());
        Mockito.when(entry1.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry1.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"test.*"});
        Mockito.when(entry1.getAgent()).thenReturn(agent);
        Mockito.when(entry2.getParams()).thenReturn(Mockito.mock(RouterAgentConfigParams.class));
        Mockito.when(entry2.getParams().getStrings(RegexRouter.REGEX)).thenReturn(new String[]{"another.*"});
        Mockito.when(entry2.getAgent()).thenReturn(agent); // Same agent
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getText()).thenReturn("test string");
        
        Agent[] result = router.route(request);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(agent, result[0]);
    }
}
