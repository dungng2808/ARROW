package com.airhacks.interceptor.monitoring.control;

import com.airhacks.interceptor.monitoring.entity.Invocation;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InvocationMonitoring_ArrowTest {

    private InvocationMonitoring monitoring;

    @Before
    public void setUp() {
        monitoring = new InvocationMonitoring();
        monitoring.init();
    }

    @Test
    public void exceptionalIsEmptyInitially() {
        List<?> result = monitoring.getExceptional();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void unstableIsEmptyInitially() {
        List<?> result = monitoring.getUnstable();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void slowestIsEmptyInitially() {
        List<?> result = monitoring.getSlowest();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void recentIsEmptyInitially() {
        List<?> result = monitoring.getRecent();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void resetClearsStatistics() {
        monitoring.reset();
        assertEquals(0, monitoring.getExceptional().size());
    }
}
