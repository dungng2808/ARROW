package edu.jhuapl.dorset.routing;

import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.agents.Agent;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RegexRouter_ArrowTest {

    @Test
    public void testConstructorWithNullRegexes() {
        // Test that when regexes are null, no patterns are added to the map
        Map<String, String[]> params = new HashMap<>();
        params.put(RegexRouter.REGEX, null);
        
        MockRouterAgentConfigEntry entry = new MockRouterAgentConfigEntry(params, new MockAgent("testAgent"));
        MockRouterAgentConfig config = new MockRouterAgentConfig(new RouterAgentConfigEntry[]{entry});
        
        RegexRouter router = new RegexRouter(config);
        
        // Should have no agents registered
        assertEquals(0, router.getAgents().length);
    }

    @Test
    public void testRouteMatching() {
        // Test that a request matches a regex pattern
        Map<String, String[]> params = new HashMap<>();
        params.put(RegexRouter.REGEX, new String[]{"hello.*"});
        
        MockRouterAgentConfigEntry entry = new MockRouterAgentConfigEntry(params, new MockAgent("testAgent"));
        MockRouterAgentConfig config = new MockRouterAgentConfig(new RouterAgentConfigEntry[]{entry});
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("hello world");
        Agent[] agents = router.route(request);
        
        assertEquals(1, agents.length);
        assertEquals("testAgent", agents[0].getId());
    }

    @Test
    public void testRouteNoMatch() {
        // Test that a request doesn't match any regex pattern
        Map<String, String[]> params = new HashMap<>();
        params.put(RegexRouter.REGEX, new String[]{"hello.*"});
        
        MockRouterAgentConfigEntry entry = new MockRouterAgentConfigEntry(params, new MockAgent("testAgent"));
        MockRouterAgentConfig config = new MockRouterAgentConfig(new RouterAgentConfigEntry[]{entry});
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("goodbye world");
        Agent[] agents = router.route(request);
        
        assertEquals(0, agents.length);
    }

    @Test
    public void testGetAgents() {
        // Test that getAgents returns all registered agents
        Map<String, String[]> params1 = new HashMap<>();
        params1.put(RegexRouter.REGEX, new String[]{"hello.*"});
        
        Map<String, String[]> params2 = new HashMap<>();
        params2.put(RegexRouter.REGEX, new String[]{"world.*"});
        
        MockRouterAgentConfigEntry entry1 = new MockRouterAgentConfigEntry(params1, new MockAgent("agent1"));
        MockRouterAgentConfigEntry entry2 = new MockRouterAgentConfigEntry(params2, new MockAgent("agent2"));
        MockRouterAgentConfig config = new MockRouterAgentConfig(new RouterAgentConfigEntry[]{entry1, entry2});
        
        RegexRouter router = new RegexRouter(config);
        
        Agent[] agents = router.getAgents();
        
        assertEquals(2, agents.length);
        assertTrue(agents[0].getId().equals("agent1") || agents[0].getId().equals("agent2"));
        assertTrue(agents[1].getId().equals("agent1") || agents[1].getId().equals("agent2"));
    }

    @Test
    public void testMultipleRegexesForOneAgent() {
        // Test that one agent can have multiple regexes
        Map<String, String[]> params = new HashMap<>();
        params.put(RegexRouter.REGEX, new String[]{"hello.*", "hi.*"});
        
        MockRouterAgentConfigEntry entry = new MockRouterAgentConfigEntry(params, new MockAgent("multiAgent"));
        MockRouterAgentConfig config = new MockRouterAgentConfig(new RouterAgentConfigEntry[]{entry});
        
        RegexRouter router = new RegexRouter(config);
        
        Request request1 = new MockRequest("hello world");
        Request request2 = new MockRequest("hi there");
        Request request3 = new MockRequest("goodbye world");
        
        Agent[] agents1 = router.route(request1);
        Agent[] agents2 = router.route(request2);
        Agent[] agents3 = router.route(request3);
        
        assertEquals(1, agents1.length);
        assertEquals("multiAgent", agents1[0].getId());
        assertEquals(1, agents2.length);
        assertEquals("multiAgent", agents2[0].getId());
        assertEquals(0, agents3.length);
    }

    @Test
    public void testCaseInsensitive() {
        // Test that regex matching is case insensitive
        Map<String, String[]> params = new HashMap<>();
        params.put(RegexRouter.REGEX, new String[]{"HELLO.*"});
        
        MockRouterAgentConfigEntry entry = new MockRouterAgentConfigEntry(params, new MockAgent("caseAgent"));
        MockRouterAgentConfig config = new MockRouterAgentConfig(new RouterAgentConfigEntry[]{entry});
        
        RegexRouter router = new RegexRouter(config);
        
        Request request1 = new MockRequest("hello world");
        Request request2 = new MockRequest("Hello World");
        Request request3 = new MockRequest("HELLO WORLD");
        
        Agent[] agents1 = router.route(request1);
        Agent[] agents2 = router.route(request2);
        Agent[] agents3 = router.route(request3);
        
        assertEquals(1, agents1.length);
        assertEquals(1, agents2.length);
        assertEquals(1, agents3.length);
    }

    // Helper classes for testing
    private static class MockRequest implements Request {
        private String text;
        
        public MockRequest(String text) {
            this.text = text;
        }
        
        @Override
        public String getText() {
            return text;
        }
        
        @Override
        public String getId() {
            return "mockId";
        }
    }
    
    private static class MockAgent implements Agent {
        private String id;
        
        public MockAgent(String id) {
            this.id = id;
        }
        
        @Override
        public String getId() {
            return id;
        }
    }
    
    private static class MockRouterAgentConfigEntry implements RouterAgentConfigEntry {
        private Map<String, String[]> params;
        private Agent agent;
        
        public MockRouterAgentConfigEntry(Map<String, String[]> params, Agent agent) {
            this.params = params;
            this.agent = agent;
        }
        
        @Override
        public Agent getAgent() {
            return agent;
        }
        
        @Override
        public Map<String, String[]> getParams() {
            return params;
        }
    }
    
    private static class MockRouterAgentConfig implements RouterAgentConfig {
        private RouterAgentConfigEntry[] entries;
        
        public MockRouterAgentConfig(RouterAgentConfigEntry[] entries) {
            this.entries = entries;
        }
        
        @Override
        public RouterAgentConfigEntry[] toArray() {
            return entries;
        }
        
        @Override
        public int size() {
            return entries.length;
        }
        
        @Override
        public RouterAgentConfigEntry get(int index) {
            return entries[index];
        }
        
        @Override
        public java.util.Iterator<RouterAgentConfigEntry> iterator() {
            return new java.util.Iterator<RouterAgentConfigEntry>() {
                private int index = 0;
                
                @Override
                public boolean hasNext() {
                    return index < entries.length;
                }
                
                @Override
                public RouterAgentConfigEntry next() {
                    return entries[index++];
                }
            };
        }
    }
}
