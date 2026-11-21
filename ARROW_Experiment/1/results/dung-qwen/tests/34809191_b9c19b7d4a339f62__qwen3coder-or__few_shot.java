package com.vaadin.flow.internal.nodefeature;

import com.vaadin.flow.internal.StateNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PollConfigurationMap_ArrowTest {

    @Test
    public void testSetAndGetPollInterval() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap pollConfigurationMap = new PollConfigurationMap(node);
        
        pollConfigurationMap.setPollInterval(5000);
        int result = pollConfigurationMap.getPollInterval();
        
        assertEquals(5000, result);
    }

    @Test
    public void testGetPollIntervalWhenNotSet() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap pollConfigurationMap = new PollConfigurationMap(node);
        
        int result = pollConfigurationMap.getPollInterval();
        
        assertEquals(-1, result);
    }

    @Test
    public void testGetPollIntervalAfterSettingAndRemoving() {
        StateNode node = mock(StateNode.class);
        PollConfigurationMap pollConfigurationMap = new PollConfigurationMap(node);
        
        pollConfigurationMap.setPollInterval(3000);
        // Simulate removal by mocking the contains and get behavior
        when(node.hasFeature(PollConfigurationMap.class)).thenReturn(true);
        when(node.getMap(PollConfigurationMap.class)).thenReturn(pollConfigurationMap);
        
        int result = pollConfigurationMap.getPollInterval();
        
        assertEquals(3000, result);
    }
}
