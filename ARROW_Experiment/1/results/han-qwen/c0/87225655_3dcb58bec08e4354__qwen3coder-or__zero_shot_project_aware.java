package com.airhacks.interceptor.monitoring.control;

import com.airhacks.interceptor.monitoring.entity.Invocation;
import com.airhacks.interceptor.monitoring.entity.InvocationStatistics;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class InvocationMonitoring_ArrowTest {

    private InvocationMonitoring monitoring;

    @Before
    public void setUp() {
        monitoring = new InvocationMonitoring();
        monitoring.init();
    }

    @Test
    public void testInitCreatesConcurrentHashMap() {
        assertNotNull(monitoring);
        assertNotNull(monitoring.getClass().getDeclaredField("statistics"));
    }

    @Test
    public void testOnNewCallAddsInvocationStatistics() {
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);

        assertNotNull(monitoring.getClass().getDeclaredField("statistics"));
        assertEquals(1, monitoring.getClass().getDeclaredField("statistics").get(monitoring).size());
    }

    @Test
    public void testGetExceptionalReturnsEmptyListWhenNoInvocations() {
        List<InvocationStatistics> exceptional = monitoring.getExceptional();
        assertNotNull(exceptional);
        assertEquals(0, exceptional.size());
    }

    @Test
    public void testGetUnstableReturnsEmptyListWhenNoInvocations() {
        List<InvocationStatistics> unstable = monitoring.getUnstable();
        assertNotNull(unstable);
        assertEquals(0, unstable.size());
    }

    @Test
    public void testGetSlowestReturnsEmptyListWhenNoInvocations() {
        List<InvocationStatistics> slowest = monitoring.getSlowest();
        assertNotNull(slowest);
        assertEquals(0, slowest.size());
    }

    @Test
    public void testGetRecentReturnsEmptyListWhenNoInvocations() {
        List<InvocationStatistics> recent = monitoring.getRecent();
        assertNotNull(recent);
        assertEquals(0, recent.size());
    }

    @Test
    public void testResetClearsStatistics() {
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        assertEquals(1, monitoring.getClass().getDeclaredField("statistics").get(monitoring).size());

        monitoring.reset();
        assertEquals(0, monitoring.getClass().getDeclaredField("statistics").get(monitoring).size());
    }
}
