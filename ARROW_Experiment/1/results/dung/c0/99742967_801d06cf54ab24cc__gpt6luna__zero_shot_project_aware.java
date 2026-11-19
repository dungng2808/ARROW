package software.amazon.swage.metrics.jmx;

import org.junit.Test;
import software.amazon.swage.metrics.record.MetricRecorder;
import software.amazon.swage.metrics.jmx.sensor.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MXBeanPoller_ArrowTest {

    @Test
    public void shutdownStopsTheProvidedExecutor() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        when(executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)).thenReturn(true);

        MXBeanPoller poller = new MXBeanPoller(
                executor, mock(MetricRecorder.class), 1, Collections.<Sensor>emptyList());

        poller.shutdown();

        verify(executor).shutdown();
        verify(executor).awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Test
    public void constructorSchedulesPollingImmediatelyAtTheRequestedInterval() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);

        new MXBeanPoller(executor, mock(MetricRecorder.class), 7, Collections.<Sensor>emptyList());

        verify(executor).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.<Runnable>any(),
                org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    public void constructorAcceptsAnEmptySensorList() {
        ScheduledExecutorService executor = mock(ScheduledExecutorService.class);

        MXBeanPoller poller = new MXBeanPoller(
                executor, mock(MetricRecorder.class), 1, new ArrayList<Sensor>());

        poller.shutdown();

        verify(executor).shutdown();
    }
}
