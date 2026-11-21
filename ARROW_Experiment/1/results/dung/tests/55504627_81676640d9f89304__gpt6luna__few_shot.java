package io.fabric8.maven.core.handler;

import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.maven.core.config.ProbeConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class ProbeHandler_ArrowTest {

    @Test
    public void nullConfigReturnsNull() {
        ProbeHandler handler = new ProbeHandler();
        assertNull(handler.getProbe(null));
    }

    @Test
    public void httpUrlCreatesHttpProbe() {
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("http://example.com:8080/health");
        config.setInitialDelaySeconds(5);
        config.setTimeoutSeconds(2);
        config.setFailureThreshold(3);
        config.setSuccessThreshold(1);

        Probe probe = new ProbeHandler().getProbe(config);

        assertNotNull(probe);
        assertEquals(Integer.valueOf(5), probe.getInitialDelaySeconds());
        assertEquals(Integer.valueOf(2), probe.getTimeoutSeconds());
        assertEquals(Integer.valueOf(3), probe.getFailureThreshold());
        assertEquals(Integer.valueOf(1), probe.getSuccessThreshold());
        assertEquals("example.com", probe.getHttpGet().getHost());
        assertEquals("/health", probe.getHttpGet().getPath());
        assertEquals(Integer.valueOf(8080), probe.getHttpGet().getPort().getIntVal());
        assertEquals("HTTP", probe.getHttpGet().getScheme());
    }

    @Test
    public void execCommandCreatesExecProbe() {
        ProbeConfig config = new ProbeConfig();
        config.setExec("echo hello");

        Probe probe = new ProbeHandler().getProbe(config);

        assertNotNull(probe);
        assertEquals(2, probe.getExec().getCommand().size());
        assertEquals("echo", probe.getExec().getCommand().get(0));
        assertEquals("hello", probe.getExec().getCommand().get(1));
    }

    @Test
    public void tcpPortCreatesTcpProbe() {
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("https://example.com/health");
        config.setTcpPort("443");

        Probe probe = new ProbeHandler().getProbe(config);

        assertNotNull(probe);
        assertEquals("example.com", probe.getTcpSocket().getHost());
        assertEquals(Integer.valueOf(443), probe.getTcpSocket().getPort().getIntVal());
    }

    @Test
    public void nonNumericTcpPortIsPreserved() {
        ProbeConfig config = new ProbeConfig();
        config.setTcpPort("service-port");

        Probe probe = new ProbeHandler().getProbe(config);

        assertNotNull(probe);
        assertEquals("service-port", probe.getTcpSocket().getPort().getStrVal());
    }

    @Test
    public void configWithoutProbeActionReturnsNull() {
        ProbeConfig config = new ProbeConfig();
        assertNull(new ProbeHandler().getProbe(config));
    }
}
