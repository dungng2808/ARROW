package com.airhacks.interceptor.monitoring.control;

import com.airhacks.interceptor.monitoring.entity.Invocation;
import com.airhacks.interceptor.monitoring.entity.InvocationStatistics;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvocationMonitoring_ArrowTest {

    private InvocationMonitoring monitoring;

    @Before
    public void setUp() {
        monitoring = new InvocationMonitoring();
        monitoring.init();
    }

    @Test
    public void resetClearsAllStatistics() {
        monitoring.reset();

        assertTrue(monitoring.getExceptional().isEmpty());
        assertTrue(monitoring.getUnstable().isEmpty());
        assertTrue(monitoring.getSlowest().isEmpty());
        assertTrue(monitoring.getRecent().isEmpty());
    }

    @Test
    public void statisticsListsAreEmptyInitially() {
        assertTrue(monitoring.getExceptional().isEmpty());
        assertTrue(monitoring.getUnstable().isEmpty());
        assertTrue(monitoring.getSlowest().isEmpty());
        assertTrue(monitoring.getRecent().isEmpty());
    }

    @Test
    public void recordsInvocationByMethodName() {
        Invocation invocation = new Invocation();
        invocation.setMethodName("sampleMethod");

        monitoring.onNewCall(invocation);

        List<InvocationStatistics> slowest = monitoring.getSlowest();
        assertEquals(1, slowest.size());
    }
}
