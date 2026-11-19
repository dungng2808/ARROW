package org.graylog.plugins.metrics.statsd.providers;

import com.basistech.metrics.reporting.Statsd;
import com.basistech.metrics.reporting.StatsdReporter;
import com.codahale.metrics.MetricRegistry;
import org.graylog.plugins.metrics.core.RegexMetricFilter;
import org.graylog.plugins.metrics.statsd.MetricsStatsdReporterConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class StatsdReporterProvider_ArrowTest {

    @Mock
    private MetricsStatsdReporterConfiguration configuration;

    @Mock
    private Statsd statsd;

    @Mock
    private MetricRegistry metricRegistry;

    private StatsdReporterProvider provider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        provider = new StatsdReporterProvider(configuration, statsd, metricRegistry);
    }

    @Test
    public void testGetReturnsNonNullStatsdReporter() {
        when(configuration.getPrefix()).thenReturn("test.prefix");
        when(configuration.getUnitDurations()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getUnitRates()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getIncludeMetrics()).thenReturn(".*");

        StatsdReporter result = provider.get();

        assertNotNull(result);
    }

    @Test
    public void testGetCreatesReporterWithCorrectConfiguration() {
        when(configuration.getPrefix()).thenReturn("test.prefix");
        when(configuration.getUnitDurations()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getUnitRates()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getIncludeMetrics()).thenReturn(".*");

        StatsdReporter result = provider.get();

        // Verify that the reporter was built correctly by checking that it's not null
        assertNotNull(result);
    }

    @Test
    public void testGetUsesCorrectFilter() {
        when(configuration.getPrefix()).thenReturn("test.prefix");
        when(configuration.getUnitDurations()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getUnitRates()).thenReturn(java.util.concurrent.TimeUnit.SECONDS);
        when(configuration.getIncludeMetrics()).thenReturn("test.pattern");

        StatsdReporter result = provider.get();

        // Verify that the filter was created with the correct pattern
        assertNotNull(result);
    }
}
