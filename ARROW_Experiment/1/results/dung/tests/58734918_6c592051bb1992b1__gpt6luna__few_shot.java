package org.graylog.plugins.metrics.statsd.providers;

import com.basistech.metrics.reporting.Statsd;
import com.basistech.metrics.reporting.StatsdReporter;
import com.codahale.metrics.MetricRegistry;
import org.graylog.plugins.metrics.statsd.MetricsStatsdReporterConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class StatsdReporterProvider_ArrowTest {

    @Test
    public void getBuildsReporter() {
        MetricsStatsdReporterConfiguration configuration = new MetricsStatsdReporterConfiguration();
        Statsd statsd = new Statsd();
        MetricRegistry metricRegistry = new MetricRegistry();

        StatsdReporterProvider provider = new StatsdReporterProvider(configuration, statsd, metricRegistry);

        StatsdReporter reporter = provider.get();

        assertNotNull(reporter);
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullConfiguration() {
        new StatsdReporterProvider(null, new Statsd(), new MetricRegistry());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullStatsd() {
        new StatsdReporterProvider(new MetricsStatsdReporterConfiguration(), null, new MetricRegistry());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullMetricRegistry() {
        new StatsdReporterProvider(new MetricsStatsdReporterConfiguration(), new Statsd(), null);
    }
}
