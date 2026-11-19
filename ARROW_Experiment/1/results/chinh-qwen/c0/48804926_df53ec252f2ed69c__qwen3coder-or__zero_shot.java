package edu.jhuapl.dorset.routing;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.jhuapl.dorset.Request;
import edu.jhuapl.dorset.agents.Agent;

public class RegexRouter_ArrowTest {

    private MockAgent agent1;
    private MockAgent agent2;
    private MockAgent agent3;
    
    @Before
    public void setUp() {
        agent1 = new MockAgent("agent1");
        agent2 = new MockAgent("agent2");
        agent3 = new MockAgent("agent3");
    }
    
    @Test
    public void testConstructorWithNullRegexes() {
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, null);
        
        RegexRouter router = new RegexRouter(config);
        
        Agent[] agents = router.getAgents();
        assertEquals(0, agents.length);
    }
    
    @Test
    public void testConstructorWithMultipleRegexes() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(RegexRouter.REGEX, new String[]{"hello", "hi"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params);
        
        RegexRouter router = new RegexRouter(config);
        
        Agent[] agents = router.getAgents();
        assertEquals(1, agents.length);
        assertEquals(agent1, agents[0]);
    }
    
    @Test
    public void testRouteMatchingSingleRegex() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(RegexRouter.REGEX, new String[]{"hello"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("Hello");
        Agent[] result = router.route(request);
        
        assertEquals(1, result.length);
        assertEquals(agent1, result[0]);
    }
    
    @Test
    public void testRouteNoMatch() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(RegexRouter.REGEX, new String[]{"hello"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("goodbye");
        Agent[] result = router.route(request);
        
        assertEquals(0, result.length);
    }
    
    @Test
    public void testRouteMultipleMatches() {
        Map<String, String[]> params1 = new HashMap<String, String[]>();
        params1.put(RegexRouter.REGEX, new String[]{"hello"});
        
        Map<String, String[]> params2 = new HashMap<String, String[]>();
        params2.put(RegexRouter.REGEX, new String[]{"world"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params1);
        config.addEntry(agent2, params2);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("Hello world");
        Agent[] result = router.route(request);
        
        assertEquals(2, result.length);
        assertTrue(result[0] == agent1 || result[1] == agent1);
        assertTrue(result[0] == agent2 || result[1] == agent2);
    }
    
    @Test
    public void testRouteCaseInsensitive() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(RegexRouter.REGEX, new String[]{"HELLO"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("hello");
        Agent[] result = router.route(request);
        
        assertEquals(1, result.length);
        assertEquals(agent1, result[0]);
    }
    
    @Test
    public void testGetAgents() {
        Map<String, String[]> params1 = new HashMap<String, String[]>();
        params1.put(RegexRouter.REGEX, new String[]{"hello"});
        
        Map<String, String[]> params2 = new HashMap<String, String[]>();
        params2.put(RegexRouter.REGEX, new String[]{"world"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params1);
        config.addEntry(agent2, params2);
        
        RegexRouter router = new RegexRouter(config);
        
        Agent[] agents = router.getAgents();
        assertEquals(2, agents.length);
        assertTrue(agents[0] == agent1 || agents[1] == agent1);
        assertTrue(agents[0] == agent2 || agents[1] == agent2);
    }
    
    @Test
    public void testRouteWithEmptyRequestText() {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(RegexRouter.REGEX, new String[]{"^$"});
        
        RouterAgentConfig config = new MockRouterAgentConfig();
        config.addEntry(agent1, params);
        
        RegexRouter router = new RegexRouter(config);
        
        Request request = new MockRequest("");
        Agent[] result = router.route(request);
        
        assertEquals(1, result.length);
        assertEquals(agent1, result[0]);
    }
    
    // Helper classes
    
    private static class MockAgent extends Agent {
        private String id;
        
        public MockAgent(String id) {
            this.id = id;
        }
        
        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public String getName() {
            return id;
        }
    }
    
    private static class MockRouterAgentConfig implements RouterAgentConfig {
        private java.util.List<RouterAgentConfigEntry> entries = new java.util.ArrayList<RouterAgentConfigEntry>();
        
        public void addEntry(Agent agent, Map<String, String[]> params) {
            entries.add(new MockRouterAgentConfigEntry(agent, params));
        }
        
        @Override
        public java.util.Iterator<RouterAgentConfigEntry> iterator() {
            return entries.iterator();
        }
    }
    
    private static class MockRouterAgentConfigEntry implements RouterAgentConfigEntry {
        private Agent agent;
        private Map<String, String[]> params;
        
        public MockRouterAgentConfigEntry(Agent agent, Map<String, String[]> params) {
            this.agent = agent;
            this.params = params;
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
        public String getUtteranceId() {
            return null;
        }
        
        @Override
        public Object getParameter(String name) {
            return null;
        }
    }
}
