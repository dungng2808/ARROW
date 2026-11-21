package org.graylog.plugins.metrics.statsd.providers;

import com.basistech.metrics.reporting.Statsd;
import com.basistech.metrics.reporting.StatsdReporter;
import com.codahale.metrics.MetricRegistry;
import org.graylog.plugins.metrics.core.RegexMetricFilter;
import org.graylog.plugins.metrics.statsd.MetricsStatsdReporterConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatsdReporterProvider_ArrowTest {

    @Test
    public void testGetReturnsNonNullStatsdReporter() {
        // Create mock dependencies
        MetricsStatsdReporterConfiguration config = mock(MetricsStatsdReporterConfiguration.class);
        Statsd statsd = mock(Statsd.class);
        MetricRegistry metricRegistry = mock(MetricRegistry.class);

        // Set up configuration mock behavior
        when(config.getPrefix()).thenReturn("test.prefix");
        when(config.getUnitDurations()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(config.getUnitRates()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(config.getIncludeMetrics()).thenReturn(".*");

        // Create the provider instance
        StatsdReporterProvider provider = new StatsdReporterProvider(config, statsd, metricRegistry);

        // Call the method under test
        StatsdReporter result = provider.get();

        // Verify the result is not null
        assertNotNull("StatsdReporter should not be null", result);
    }
}
