package software.amazon.swage.metrics.jmx;

import org.junit.Test;
import software.amazon.swage.metrics.MetricContext;
import software.amazon.swage.metrics.jmx.sensor.Sensor;
import software.amazon.swage.metrics.record.MetricRecorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MXBeanPoller_ArrowTest {

    @Test
    public void constructorSchedulesSensorPolling() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        MetricRecorder recorder = mock(MetricRecorder.class);
        List<Sensor> sensors = Collections.emptyList();

        new MXBeanPoller(executor, recorder, 5, sensors);

        verify(executor).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(5L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.SECONDS));
    }

    @Test
    public void shutdownStopsExecutor() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        MetricRecorder recorder = mock(MetricRecorder.class);
        when(executor.awaitTermination(60, TimeUnit.SECONDS)).thenReturn(true);

        MXBeanPoller poller = new MXBeanPoller(executor, recorder, 1, Collections.<Sensor>emptyList());
        poller.shutdown();

        verify(executor).shutdown();
        verify(executor).awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void constructorCopiesSensorList() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        MetricRecorder recorder = mock(MetricRecorder.class);
        List<Sensor> sensors = new ArrayList<>();
        sensors.add(mock(Sensor.class));

        new MXBeanPoller(executor, recorder, 1, sensors);
        sensors.clear();

        assertTrue(sensors.isEmpty());
        verify(executor).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(TimeUnit.SECONDS));
    }
}
