package io.fabric8.maven.core.handler;

import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.maven.core.config.ProbeConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProbeHandler_ArrowTest {

    @Test
    public void getProbeReturnsNullForNullConfig() {
        ProbeHandler handler = new ProbeHandler();

        assertNull(handler.getProbe(null));
    }

    @Test
    public void getProbeReturnsNullWhenNoActionIsConfigured() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();

        assertNull(handler.getProbe(config));
    }

    @Test
    public void getProbeUsesHttpGetAndCopiesConfiguredThresholds() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setInitialDelaySeconds(3);
        config.setTimeoutSeconds(4);
        config.setFailureThreshold(5);
        config.setSuccessThreshold(2);
        config.setGetUrl("http://example.com/health");

        Probe probe = handler.getProbe(config);

        assertEquals(Integer.valueOf(3), probe.getInitialDelaySeconds());
        assertEquals(Integer.valueOf(4), probe.getTimeoutSeconds());
        assertEquals(Integer.valueOf(5), probe.getFailureThreshold());
        assertEquals(Integer.valueOf(2), probe.getSuccessThreshold());
        assertEquals("example.com", probe.getHttpGet().getHost());
        assertEquals("/health", probe.getHttpGet().getPath());
        assertEquals("HTTP", probe.getHttpGet().getScheme());
        assertEquals(Integer.valueOf(-1), probe.getHttpGet().getPort().getIntVal());
    }

    @Test
    public void getProbeUsesExecWhenHttpGetIsNotConfigured() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setExec("echo hello");

        Probe probe = handler.getProbe(config);

        assertEquals(2, probe.getExec().getCommand().size());
        assertEquals("echo", probe.getExec().getCommand().get(0));
        assertEquals("hello", probe.getExec().getCommand().get(1));
    }

    @Test
    public void getProbeUsesTcpSocketWhenOnlyTcpPortIsConfigured() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setTcpPort("8080");

        Probe probe = handler.getProbe(config);

        assertEquals(Integer.valueOf(8080), probe.getTcpSocket().getPort().getIntVal());
        assertNull(probe.getTcpSocket().getHost());
    }

    @Test
    public void getProbeUsesTcpSocketHostFromUrl() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("tcp://example.com/path");
        config.setTcpPort("service-port");

        Probe probe = handler.getProbe(config);

        assertEquals("example.com", probe.getTcpSocket().getHost());
        assertEquals("service-port", probe.getTcpSocket().getPort().getStrVal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProbeRejectsMalformedHttpUrl() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("http://[invalid");

        handler.getProbe(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProbeRejectsMalformedTcpUrl() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("tcp://[invalid");
        config.setTcpPort("80");

        handler.getProbe(config);
    }
}
