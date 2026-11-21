package com.vaadin.flow.internal.nodefeature;

import org.junit.Test;
import static org.junit.Assert.*;
import com.vaadin.flow.internal.StateNode;

public class PollConfigurationMap_ArrowTest {

    @Test
    public void testConstructor() {
        StateNode node = new StateNode();
        PollConfigurationMap map = new PollConfigurationMap(node);
        assertNotNull(map);
    }

    @Test
    public void testSetAndGetPollInterval() {
        StateNode node = new StateNode();
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        int interval = 5000;
        map.setPollInterval(interval);
        
        int result = map.getPollInterval();
        assertEquals(interval, result);
    }

    @Test
    public void testGetPollIntervalWhenNotSet() {
        StateNode node = new StateNode();
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        int result = map.getPollInterval();
        assertEquals(-1, result);
    }

    @Test
    public void testGetPollIntervalAfterSettingAndGetting() {
        StateNode node = new StateNode();
        PollConfigurationMap map = new PollConfigurationMap(node);
        
        // Set a value
        map.setPollInterval(3000);
        int result1 = map.getPollInterval();
        assertEquals(3000, result1);
        
        // Set a different value
        map.setPollInterval(7000);
        int result2 = map.getPollInterval();
        assertEquals(7000, result2);
    }
}
