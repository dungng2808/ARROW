package com.airhacks.interceptor.monitoring.control;

import com.airhacks.interceptor.monitoring.entity.Invocation;
import com.airhacks.interceptor.monitoring.entity.InvocationStatistics;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvocationMonitoring_ArrowTest {

    @Test
    public void initializedMonitoringHasNoStatistics() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();

        assertTrue(monitoring.getExceptional().isEmpty());
        assertTrue(monitoring.getUnstable().isEmpty());
        assertTrue(monitoring.getSlowest().isEmpty());
        assertTrue(monitoring.getRecent().isEmpty());
    }

    @Test
    public void resetRemovesRecordedStatistics() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();
        monitoring.reset();

        assertEquals(0, monitoring.getSlowest().size());
    }

    @Test
    public void recordsInvocationForItsMethod() {
        InvocationMonitoring monitoring = new InvocationMonitoring();
        monitoring.init();

        Invocation invocation = new Invocation();
        invocation.setMethodName("sampleMethod");
        monitoring.onNewCall(invocation);

        List<InvocationStatistics> statistics = monitoring.getSlowest();
        assertEquals(1, statistics.size());
    }
}
