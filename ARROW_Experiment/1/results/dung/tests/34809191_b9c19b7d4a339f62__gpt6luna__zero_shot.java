package com.vaadin.flow.internal.nodefeature;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.flow.internal.StateNode;

public class PollConfigurationMap_ArrowTest {

    @Test
    public void getPollIntervalWhenUnsetReturnsMinusOne() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());

        assertEquals(-1, map.getPollInterval());
    }

    @Test
    public void setPollIntervalStoresValue() {
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
