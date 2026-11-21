package com.vaadin.flow.internal.nodefeature;

import com.vaadin.flow.internal.StateNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PollConfigurationMap_ArrowTest {

    @Test
    public void returnsMinusOneWhenPollIntervalIsNotSet() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());

        assertEquals(-1, map.getPollInterval());
    }

    @Test
    public void returnsConfiguredPollInterval() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());
        map.setPollInterval(250);

        assertEquals(250, map.getPollInterval());
    }

    @Test
    public void returnsZeroWhenZeroPollIntervalIsSet() {
        PollConfigurationMap map = new PollConfigurationMap(new StateNode());
        map.setPollInterval(0);

        assertEquals(0, map.getPollInterval());
    }
}
