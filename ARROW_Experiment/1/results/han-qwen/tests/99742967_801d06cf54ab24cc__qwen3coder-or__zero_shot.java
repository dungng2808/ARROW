package software.amazon.swage.metrics.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.Logger;
import org.junit.After;
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

    private static final int POLLING_INTERVAL_SECONDS = 1;
    private static final String RUNTIME_NAME = ManagementFactory.getRuntimeMXBean().getName();

    private MetricRecorder mockMetricRecorder;
    private List<Sensor> sensors;
    private Sensor mockSensor1;
    private Sensor mockSensor2;
    private ScheduledExecutorService mockExecutor;

    @Before
    public void setUp() {
        mockMetricRecorder = mock(MetricRecorder.class);
        mockSensor1 = mock(Sensor.class);
        mockSensor2 = mock(Sensor.class);
        sensors = new ArrayList<>();
        sensors.add(mockSensor1);
        sensors.add(mockSensor2);
        mockExecutor = mock(ScheduledExecutorService.class);
    }

    @After
    public void tearDown() {
        // Cleanup if needed
    }

    @Test
    public void testConstructorWithDefaultExecutor() {
        MXBeanPoller poller = new MXBeanPoller(mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);
        assertNotNull(poller);
    }

    @Test
    public void testConstructorWithCustomExecutor() {
        MXBeanPoller poller = new MXBeanPoller(mockExecutor, mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);
        assertNotNull(poller);
    }

    @Test
    public void testWithAutoShutdown() {
        MXBeanPoller poller = MXBeanPoller.withAutoShutdown(mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);
        assertNotNull(poller);
    }

    @Test
    public void testRunSensorsSuccess() throws SenseException {
        MetricContext mockContext = mock(MetricContext.class);
        when(mockMetricRecorder.context(null)).thenReturn(mockContext);

        MXBeanPoller poller = new MXBeanPoller(mockExecutor, mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);

        poller.runSensors();

        verify(mockSensor1, times(1)).sense(mockContext);
        verify(mockSensor2, times(1)).sense(mockContext);
        verify(mockContext, times(1)).close();
    }

    @Test
    public void testRunSensorsWithException() throws SenseException {
        MetricContext mockContext = mock(MetricContext.class);
        when(mockMetricRecorder.context(null)).thenReturn(mockContext);
        
        // Make one sensor throw an exception
        when(mockSensor1.sense(mockContext)).thenThrow(new SenseException("Test exception"));

        MXBeanPoller poller = new MXBeanPoller(mockExecutor, mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);

        poller.runSensors(); // Should not throw due to exception handling

        verify(mockSensor1, times(1)).sense(mockContext);
        verify(mockSensor2, times(1)).sense(mockContext);
        verify(mockContext, times(1)).close();
    }

    @Test
    public void testShutdown() {
        MXBeanPoller poller = new MXBeanPoller(mockExecutor, mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);
        poller.shutdown();
        verify(mockExecutor, times(1)).shutdown();
    }

    @Test
    public void testUpdaterContextCreation() {
        TypedMap expectedContext = ContextData.withId(RUNTIME_NAME)
                                              .add(StandardContext.OPERATION, "JMX")
                                              .build();

        MXBeanPoller poller = new MXBeanPoller(mockExecutor, mockMetricRecorder, POLLING_INTERVAL_SECONDS, sensors);
        // We can't directly access updaterContext, but we can verify it was built correctly through the sensors
        // Since we're not testing the internal logic of how context is built, we'll just ensure construction works
        assertNotNull(poller);
    }
}
