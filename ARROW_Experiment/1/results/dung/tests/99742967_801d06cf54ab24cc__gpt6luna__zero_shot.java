package software.amazon.swage.metrics.jmx;

import org.junit.Test;
import software.amazon.swage.metrics.record.MetricRecorder;
import software.amazon.swage.metrics.jmx.sensor.Sensor;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertNotNull;

public class MXBeanPoller_ArrowTest {

    @Test
    public void constructsWithProvidedExecutorAndCanShutdown() {
        ScheduledExecutorService executor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        MetricRecorder recorder = null;
        MXBeanPoller poller = new MXBeanPoller(executor, recorder, 60, Collections.<Sensor>emptyList());

        assertNotNull(poller);
        poller.shutdown();
    }

    @Test
    public void constructsWithDefaultExecutorAndCanShutdown() {
        MetricRecorder recorder = null;
        MXBeanPoller poller = new MXBeanPoller(recorder, 60, Collections.<Sensor>emptyList());

        assertNotNull(poller);
        poller.shutdown();
    }
}
