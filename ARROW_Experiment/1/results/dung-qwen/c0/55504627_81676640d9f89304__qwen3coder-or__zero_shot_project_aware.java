package io.fabric8.maven.core.handler;

import io.fabric8.kubernetes.api.model.ExecAction;
import io.fabric8.kubernetes.api.model.HTTPGetAction;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.maven.core.config.ProbeConfig;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ProbeHandler_ArrowTest {

    @Test
    public void testGetProbeNullConfig() {
        ProbeHandler handler = new ProbeHandler();
        Probe probe = handler.getProbe(null);
        assertNull(probe);
    }

    @Test
    public void testGetProbeWithAllFieldsSet() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setInitialDelaySeconds(5);
        config.setTimeoutSeconds(10);
        config.setFailureThreshold(3);
        config.setSuccessThreshold(2);
        config.setGetUrl("http://example.com/health");
        config.setExec("echo hello");
        config.setTcpPort("8080");

        Probe probe = handler.getProbe(config);

        assertNotNull(probe);
        assertEquals(Integer.valueOf(5), probe.getInitialDelaySeconds());
        assertEquals(Integer.valueOf(10), probe.getTimeoutSeconds());
        assertEquals(Integer.valueOf(3), probe.getFailureThreshold());
        assertEquals(Integer.valueOf(2), probe.getSuccessThreshold());
        assertNotNull(probe.getHttpGet());
        assertNull(probe.getExec());
        assertNull(probe.getTcpSocket());
    }

    @Test
    public void testGetProbeWithHttpGetOnly() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("http://example.com/health");

        Probe probe = handler.getProbe(config);

        assertNotNull(probe);
        assertNotNull(probe.getHttpGet());
        assertNull(probe.getExec());
        assertNull(probe.getTcpSocket());
    }

    @Test
    public void testGetProbeWithExecOnly() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setExec("ls -la");

        Probe probe = handler.getProbe(config);

        assertNotNull(probe);
        assertNotNull(probe.getExec());
        assertNull(probe.getHttpGet());
        assertNull(probe.getTcpSocket());
    }

    @Test
    public void testGetProbeWithTcpSocketOnly() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("https://example.com");
        config.setTcpPort("8080");

        Probe probe = handler.getProbe(config);

        assertNotNull(probe);
        assertNotNull(probe.getTcpSocket());
        assertNull(probe.getHttpGet());
        assertNull(probe.getExec());
    }

    @Test
    public void testGetProbeNoValidAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("invalid-url");
        config.setExec("");
        config.setTcpPort(null);

        Probe probe = handler.getProbe(config);

        assertNull(probe);
    }

    @Test
    public void testGetHTTPGetActionValidUrl() {
        ProbeHandler handler = new ProbeHandler();
        HTTPGetAction action = handler.getHTTPGetAction("http://example.com/path");

        assertNotNull(action);
        assertEquals("example.com", action.getHost());
        assertEquals("/path", action.getPath());
        assertEquals("HTTP", action.getScheme());
        assertNotNull(action.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHTTPGetActionInvalidUrl() {
        ProbeHandler handler = new ProbeHandler();
        handler.getHTTPGetAction("invalid-url");
    }

    @Test
    public void testGetTCPSocketActionWithPortOnly() {
        ProbeHandler handler = new ProbeHandler();
        TCPSocketAction action = handler.getTCPSocketAction(null, "8080");

        assertNotNull(action);
        assertEquals("8080", action.getPort().getStrVal());
        assertNull(action.getHost());
    }

    @Test
    public void testGetTCPSocketActionWithUrlAndPort() {
        ProbeHandler handler = new ProbeHandler();
        TCPSocketAction action = handler.getTCPSocketAction("https://example.com:8080/path", "9090");

        assertNotNull(action);
        assertEquals("example.com", action.getHost());
        assertEquals("9090", action.getPort().getStrVal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTCPSocketActionInvalidUrl() {
        ProbeHandler handler = new ProbeHandler();
        handler.getTCPSocketAction("invalid-url", "8080");
    }

    @Test
    public void testGetExecActionValidCommand() {
        ProbeHandler handler = new ProbeHandler();
        ExecAction action = handler.getExecAction("ls -la");

        assertNotNull(action);
        assertEquals(Arrays.asList("ls", "-la"), action.getCommand());
    }

    @Test
    public void testGetExecActionEmptyCommand() {
        ProbeHandler handler = new ProbeHandler();
        ExecAction action = handler.getExecAction("");

        assertNull(action);
    }

    @Test
    public void testGetExecActionNullCommand() {
        ProbeHandler handler = new ProbeHandler();
        ExecAction action = handler.getExecAction(null);

        assertNull(action);
    }
}
