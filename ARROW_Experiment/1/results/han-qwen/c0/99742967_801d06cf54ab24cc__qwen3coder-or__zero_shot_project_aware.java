package software.amazon.swage.metrics.jmx;

import com.amazonaws.util.json.Jackson;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.swage.collection.TypedMap;
import software.amazon.swage.metrics.ContextData;
import software.amazon.swage.metrics.MetricContext;
import software.amazon.swage.metrics.MetricRecorder;
import software.amazon.swage.metrics.StandardContext;
import software.amazon.swage.metrics.jmx.sensor.SenseException;
import software.amazon.swage.metrics.jmx.sensor.Sensor;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MXBeanPoller_ArrowTest {

    @Mock
    private MetricRecorder metricRecorder;

    @Mock
    private Sensor sensor1;

    @Mock
    private Sensor sensor2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructorWithDefaultExecutor() {
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(sensor1);
        sensors.add(sensor2);

        MXBeanPoller poller = new MXBeanPoller(metricRecorder, 1, sensors);

        assertNotNull(poller);
        assertTrue(poller.getClass().getDeclaredField("running").getBoolean(poller));
    }

    @Test
    public void testConstructorWithCustomExecutor() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(sensor1);

        MXBeanPoller poller = new MXBeanPoller(executor, metricRecorder, 1, sensors);

        assertNotNull(poller);
        assertTrue(poller.getClass().getDeclaredField("running").getBoolean(poller));
    }

    @Test
    public void testWithAutoShutdown() {
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(sensor1);

        MXBeanPoller poller = MXBeanPoller.withAutoShutdown(metricRecorder, 1, sensors);

        assertNotNull(poller);
        assertTrue(poller.getClass().getDeclaredField("running").getBoolean(poller));
    }

    @Test
    public void testShutdown() throws Exception {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(sensor1);

        MXBeanPoller poller = new MXBeanPoller(executor, metricRecorder, 1, sensors);

        poller.shutdown();

        assertFalse(poller.getClass().getDeclaredField("running").getBoolean(poller));
    }

    @Test
    public void testRunSensors() throws Exception {
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(sensor1);
        sensors.add(sensor2);

        MXBeanPoller poller = new MXBeanPoller(metricRecorder, 1, sensors);

        MetricContext context = mock(MetricContext.class);
        when(metricRecorder.context(any(TypedMap.class))).thenReturn(context);

        poller.getClass().getDeclaredMethod("runSensors").invoke(poller);

        verify(sensor1, times(1)).sense(context);
        verify(sensor2, times(1)).sense(context);
        verify(context, times(1)).close();
    }

    @Test
    public void testRunSensorsWithException() throws Exception {
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(sensor1);
        sensors.add(sensor2);

        MXBeanPoller poller = new MXBeanPoller(metricRecorder, 1, sensors);

        MetricContext context = mock(MetricContext.class);
        when(metricRecorder.context(any(TypedMap.class))).thenReturn(context);
        doThrow(new SenseException("test")).when(sensor1).sense(context);

        poller.getClass().getDeclaredMethod("runSensors").invoke(poller);

        verify(sensor1, times(1)).sense(context);
        verify(sensor2, times(1)).sense(context);
        verify(context, times(1)).close();
    }
}
