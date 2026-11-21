package com.vaadin.flow.internal.nodefeature;

import com.vaadin.flow.internal.StateNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PollConfigurationMap_ArrowTest {

    @Test
    public void getPollIntervalReturnsMinusOneWhenUnset() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());

        assertEquals(-1, map.getPollInterval());
    }

    @Test
    public void setPollIntervalStoresTheInterval() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());

        map.setPollInterval(1000);

        assertEquals(1000, map.getPollInterval());
    }

    @Test
    public void setPollIntervalStoresZero() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());

        map.setPollInterval(0);

        assertEquals(0, map.getPollInterval());
    }
}
