package com.airhacks.interceptor.monitoring.control;

import com.airhacks.interceptor.monitoring.entity.Invocation;
import com.airhacks.interceptor.monitoring.entity.InvocationStatistics;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class InvocationMonitoring_ArrowTest {

    private InvocationMonitoring monitoring;

    @Before
    public void setUp() {
        monitoring = new InvocationMonitoring();
        monitoring.init();
    }

    @Test
    public void testInit() {
        assertNotNull(monitoring);
        assertNotNull(monitoring.getClass().getField("statistics").get(monitoring));
    }

    @Test
    public void testOnNewCall() {
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        
        List<InvocationStatistics> exceptional = monitoring.getExceptional();
        assertTrue(exceptional.isEmpty());
        
        List<InvocationStatistics> unstable = monitoring.getUnstable();
        assertTrue(unstable.isEmpty());
        
        List<InvocationStatistics> slowest = monitoring.getSlowest();
        assertFalse(slowest.isEmpty());
        assertEquals("testMethod", slowest.get(0).getMethodName());
    }

    @Test
    public void testOnNewCallWithException() {
        Invocation invocation = new Invocation("exceptionalMethod", 100L, true);
        monitoring.onNewCall(invocation);
        
        List<InvocationStatistics> exceptional = monitoring.getExceptional();
        assertFalse(exceptional.isEmpty());
        assertEquals("exceptionalMethod", exceptional.get(0).getMethodName());
        
        List<InvocationStatistics> unstable = monitoring.getUnstable();
        assertFalse(unstable.isEmpty());
        assertEquals("exceptionalMethod", unstable.get(0).getMethodName());
    }

    @Test
    public void testGetExceptional() {
        Invocation invocation1 = new Invocation("method1", 100L, true);
        Invocation invocation2 = new Invocation("method2", 200L, false);
        
        monitoring.onNewCall(invocation1);
        monitoring.onNewCall(invocation2);
        
        List<InvocationStatistics> exceptional = monitoring.getExceptional();
        assertEquals(1, exceptional.size());
        assertEquals("method1", exceptional.get(0).getMethodName());
    }

    @Test
    public void testGetUnstable() {
        Invocation invocation1 = new Invocation("method1", 100L, true);
        Invocation invocation2 = new Invocation("method2", 200L, true);
        
        monitoring.onNewCall(invocation1);
        monitoring.onNewCall(invocation2);
        
        List<InvocationStatistics> unstable = monitoring.getUnstable();
        assertEquals(2, unstable.size());
        assertEquals("method2", unstable.get(0).getMethodName());
    }

    @Test
    public void testGetSlowest() {
        Invocation invocation1 = new Invocation("method1", 100L, false);
        Invocation invocation2 = new Invocation("method2", 200L, false);
        
        monitoring.onNewCall(invocation1);
        monitoring.onNewCall(invocation2);
        
        List<InvocationStatistics> slowest = monitoring.getSlowest();
        assertEquals(2, slowest.size());
        assertEquals("method2", slowest.get(0).getMethodName());
    }

    @Test
    public void testGetRecent() {
        Invocation invocation1 = new Invocation("method1", 100L, false);
        Invocation invocation2 = new Invocation("method2", 200L, false);
        
        monitoring.onNewCall(invocation1);
        monitoring.onNewCall(invocation2);
        
        List<InvocationStatistics> recent = monitoring.getRecent();
        assertEquals(2, recent.size());
        assertEquals("method2", recent.get(0).getMethodName());
    }

    @Test
    public void testReset() {
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        
        assertFalse(monitoring.getSlowest().isEmpty());
        
        monitoring.reset();
        
        assertTrue(monitoring.getSlowest().isEmpty());
    }
}
