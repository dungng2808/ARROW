package com.airhacks.interceptor.monitoring.control;

import com.airhacks.interceptor.monitoring.entity.Invocation;
import com.airhacks.interceptor.monitoring.entity.InvocationStatistics;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.event.Observes;

public class InvocationMonitoring_ArrowTest {

    @Test
    public void testInit() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        assertNotNull(monitoring.getClass().getDeclaredField("statistics"));
    }

    @Test
    public void testOnNewCall() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        
        assertNotNull(monitoring.getClass().getDeclaredField("statistics"));
    }

    @Test
    public void testGetExceptional() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        
        Invocation invocation = new Invocation("testMethod", 100L, true);
        monitoring.onNewCall(invocation);
        
        List<InvocationStatistics> exceptional = monitoring.getExceptional();
        assertEquals(1, exceptional.size());
    }

    @Test
    public void testGetUnstable() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        
        Invocation invocation = new Invocation("testMethod", 100L, true);
        monitoring.onNewCall(invocation);
        
        List<InvocationStatistics> unstable = monitoring.getUnstable();
        assertEquals(1, unstable.size());
    }

    @Test
    public void testGetSlowest() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        
        List<InvocationStatistics> slowest = monitoring.getSlowest();
        assertEquals(1, slowest.size());
    }

    @Test
    public void testGetRecent() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        
        List<InvocationStatistics> recent = monitoring.getRecent();
        assertEquals(1, recent.size());
    }

    @Test
    public void testReset() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        
        Invocation invocation = new Invocation("testMethod", 100L, false);
        monitoring.onNewCall(invocation);
        
        monitoring.reset();
        assertTrue(monitoring.getClass().getDeclaredField("statistics").get(monitoring) instanceof ConcurrentHashMap);
    }
}
