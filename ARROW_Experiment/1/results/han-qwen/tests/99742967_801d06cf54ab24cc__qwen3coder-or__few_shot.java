package software.amazon.swage.metrics.jmx;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import software.amazon.swage.collection.TypedMap;
import software.amazon.swage.metrics.ContextData;
import software.amazon.swage.metrics.MetricContext;
import software.amazon.swage.metrics.MetricRecorder;
import software.amazon.swage.metrics.StandardContext;
import software.amazon.swage.metrics.jmx.sensor.SenseException;
import software.amazon.swage.metrics.jmx.sensor.Sensor;

public class MXBeanPoller_ArrowTest {

    private MetricRecorder metricRecorder;
    private List<Sensor> sensors;
    private ScheduledExecutorService executor;
    private Sensor mockSensor1;
    private Sensor mockSensor2;

    @Before
    public void setUp() {
        metricRecorder = mock(MetricRecorder.class);
        mockSensor1 = mock(Sensor.class);
        mockSensor2 = mock(Sensor.class);
        sensors = new ArrayList<>();
        sensors.add(mockSensor1);
        sensors.add(mockSensor2);
        executor = mock(ScheduledExecutorService.class);
    }

    @Test
    public void testConstructorWithExecutor() throws Exception {
        MXBeanPoller poller = new MXBeanPoller(executor, metricRecorder, 10, sensors);
        
        verify(executor).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(10L), eq(java.util.concurrent.TimeUnit.SECONDS));
        assertTrue(poller.getClass().getDeclaredField("running").get(poller) instanceof java.util.concurrent.atomic.AtomicBoolean);
    }

    @Test
    public void testConstructorWithoutExecutor() throws Exception {
        MXBeanPoller poller = new MXBeanPoller(metricRecorder, 10, sensors);
        
        assertNotNull(poller);
    }

    @Test
    public void testWithAutoShutdown() throws Exception {
        MXBeanPoller poller = MXBeanPoller.withAutoShutdown(metricRecorder, 10, sensors);
        
        assertNotNull(poller);
    }

    @Test
    public void testShutdown() throws Exception {
        MXBeanPoller poller = new MXBeanPoller(executor, metricRecorder, 10, sensors);
        
        poller.shutdown();
        
        verify(executor).shutdown();
    }

    @Test
    public void testRunSensors() throws Exception {
        MetricContext mockContext = mock(MetricContext.class);
        when(metricRecorder.context(any(TypedMap.class))).thenReturn(mockContext);
        
        MXBeanPoller poller = new MXBeanPoller(executor, metricRecorder, 10, sensors);
        
        poller.getClass().getDeclaredMethod("runSensors").invoke(poller);
        
        verify(mockSensor1).sense(mockContext);
        verify(mockSensor2).sense(mockContext);
        verify(mockContext).close();
    }

    @Test
    public void testRunSensorsWithException() throws Exception {
        MetricContext mockContext = mock(MetricContext.class);
        when(metricRecorder.context(any(TypedMap.class))).thenReturn(mockContext);
        
        doThrow(new SenseException("test")).when(mockSensor1).sense(mockContext);
        
        MXBeanPoller poller = new MXBeanPoller(executor, metricRecorder, 10, sensors);
        
        poller.getClass().getDeclaredMethod("runSensors").invoke(poller);
        
        verify(mockSensor1).sense(mockContext);
        verify(mockSensor2).sense(mockContext);
        verify(mockContext).close();
    }
}
