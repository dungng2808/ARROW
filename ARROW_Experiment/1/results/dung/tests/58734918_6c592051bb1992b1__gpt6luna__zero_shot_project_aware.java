package org.graylog.plugins.metrics.statsd.providers;

import com.basistech.metrics.reporting.Statsd;
import com.basistech.metrics.reporting.StatsdReporter;
import com.codahale.metrics.MetricRegistry;
import org.graylog.plugins.metrics.statsd.MetricsStatsdReporterConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatsdReporterProvider_ArrowTest {
    @Test
    public void getBuildsReporter() {
        MetricsStatsdReporterConfiguration configuration = mock(MetricsStatsdReporterConfiguration.class);
        Statsd statsd = mock(Statsd.class);
        MetricRegistry metricRegistry = new MetricRegistry();

        when(configuration.getPrefix()).thenReturn("test");
        when(configuration.getUnitDurations()).thenReturn(java.util.concurrent.TimeUnit.MILLISECONDS);
        when(configuration.getUnitRates()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getIncludeMetrics()).thenReturn(".*");

        StatsdReporterProvider provider = new StatsdReporterProvider(configuration, statsd, metricRegistry);

        StatsdReporter reporter = provider.get();

        assertNotNull(reporter);
        assertSame(StatsdReporter.class, reporter.getClass());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullConfiguration() {
        new StatsdReporterProvider(null, mock(Statsd.class), new MetricRegistry());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullStatsd() {
        new StatsdReporterProvider(mock(MetricsStatsdReporterConfiguration.class), null, new MetricRegistry());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullMetricRegistry() {
        new StatsdReporterProvider(mock(MetricsStatsdReporterConfiguration.class), mock(Statsd.class), null);
    }
}
