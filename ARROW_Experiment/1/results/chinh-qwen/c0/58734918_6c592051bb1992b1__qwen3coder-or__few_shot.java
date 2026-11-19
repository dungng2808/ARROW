package org.graylog.plugins.metrics.statsd.providers;

import com.basistech.metrics.reporting.Statsd;
import com.basistech.metrics.reporting.StatsdReporter;
import com.codahale.metrics.MetricRegistry;
import org.graylog.plugins.metrics.core.RegexMetricFilter;
import org.graylog.plugins.metrics.statsd.MetricsStatsdReporterConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatsdReporterProvider_ArrowTest {

    private MetricsStatsdReporterConfiguration configuration;
    private Statsd statsd;
    private MetricRegistry metricRegistry;
    private StatsdReporterProvider provider;

    @Before
    public void setUp() {
        configuration = mock(MetricsStatsdReporterConfiguration.class);
        statsd = mock(Statsd.class);
        metricRegistry = mock(MetricRegistry.class);
        provider = new StatsdReporterProvider(configuration, statsd, metricRegistry);
    }

    @Test
    public void testGet() {
        // Setup mocks
        when(configuration.getPrefix()).thenReturn("test.prefix");
        when(configuration.getUnitDurations()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getUnitRates()).thenReturn(java.util.concurrent.TimeUnit.MINUTES);
        when(configuration.getIncludeMetrics()).thenReturn(".*");

        // Call the method under test
        StatsdReporter result = provider.get();

        // Verify that the correct methods were called on the mocks
        verify(metricRegistry).register("test.prefix", result);
    }
}
