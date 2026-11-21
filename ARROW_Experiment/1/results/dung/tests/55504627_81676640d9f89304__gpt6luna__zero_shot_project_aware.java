package io.fabric8.maven.core.handler;

import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.maven.core.config.ProbeConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProbeHandler_ArrowTest {

    @Test
    public void returnsNullForNullConfiguration() {
        ProbeHandler handler = new ProbeHandler();

        assertNull(handler.getProbe(null));
    }

    @Test
    public void createsHttpProbeAndCopiesConfiguredThresholds() {
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("http://example.com:8080/health");
        config.setInitialDelaySeconds(3);
        config.setTimeoutSeconds(4);
        config.setFailureThreshold(5);
        config.setSuccessThreshold(2);

        Probe probe = new ProbeHandler().getProbe(config);

        assertEquals(Integer.valueOf(3), probe.getInitialDelaySeconds());
        assertEquals(Integer.valueOf(4), probe.getTimeoutSeconds());
        assertEquals(Integer.valueOf(5), probe.getFailureThreshold());
        assertEquals(Integer.valueOf(2), probe.getSuccessThreshold());
        assertEquals("example.com", probe.getHttpGet().getHost());
        assertEquals("/health", probe.getHttpGet().getPath());
        assertEquals(Integer.valueOf(8080), probe.getHttpGet().getPort().getIntVal());
        assertEquals("HTTP", probe.getHttpGet().getScheme());
    }

    @Test
    public void createsExecProbeWhenCommandIsConfigured() {
        ProbeConfig config = new ProbeConfig();
        config.setExec("echo hello");

        Probe probe = new ProbeHandler().getProbe(config);

        assertEquals(2, probe.getExec().getCommand().size());
        assertEquals("echo", probe.getExec().getCommand().get(0));
        assertEquals("hello", probe.getExec().getCommand().get(1));
    }

    @Test
    public void createsTcpProbeWithNumericPortWhenUrlIsAbsent() {
        ProbeConfig config = new ProbeConfig();
        config.setTcpPort("8081");

        Probe probe = new ProbeHandler().getProbe(config);

        assertEquals(Integer.valueOf(8081), probe.getTcpSocket().getPort().getIntVal());
        assertNull(probe.getTcpSocket().getHost());
    }

    @Test
    public void createsTcpProbeWithStringPortAndUrlHost() {
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("tcp://example.com/path");
        config.setTcpPort("service-port");

        Probe probe = new ProbeHandler().getProbe(config);

        assertEquals("example.com", probe.getTcpSocket().getHost());
        assertEquals("service-port", probe.getTcpSocket().getPort().getStrVal());
    }

    @Test
    public void returnsNullWhenNoProbeActionIsConfigured() {
        ProbeConfig config = new ProbeConfig();

        assertNull(new ProbeHandler().getProbe(config));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedHttpUrl() {
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("http://");

        new ProbeHandler().getProbe(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedTcpUrl() {
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("tcp://");
        config.setTcpPort("8080");

        new ProbeHandler().getProbe(config);
    }
}
