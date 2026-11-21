package com.vaadin.flow.internal.nodefeature;

import com.vaadin.flow.internal.StateNode;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PollConfigurationMap_ArrowTest {

    @Test
    public void testSetAndGetPollInterval() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        int interval = 5000;
        map.setPollInterval(interval);
        
        assertEquals(interval, map.getPollInterval());
    }

    @Test
    public void testGetPollIntervalDefault() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        assertEquals(-1, map.getPollInterval());
    }

    @Test
    public void testGetPollIntervalAfterSet() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        int interval = 10000;
        map.setPollInterval(interval);
        
        assertEquals(interval, map.getPollInterval());
    }

    @Test
    public void testContainsKey() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        assertFalse(map.contains("pollInterval"));
        
        map.setPollInterval(5000);
        assertTrue(map.contains("pollInterval"));
    }
}
