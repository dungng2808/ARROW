package io.fabric8.maven.core.handler;

import io.fabric8.kubernetes.api.model.ExecAction;
import io.fabric8.kubernetes.api.model.HTTPGetAction;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.fabric8.maven.core.config.ProbeConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProbeHandler_ArrowTest {

    @Test
    public void testGetProbe_NullConfig() {
        ProbeHandler handler = new ProbeHandler();
        Probe probe = handler.getProbe(null);
        assertNull(probe);
    }

    @Test
    public void testGetProbe_OnlyDelays() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setInitialDelaySeconds(5);
        config.setTimeoutSeconds(10);
        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        assertEquals(Integer.valueOf(5), probe.getInitialDelaySeconds());
        assertEquals(Integer.valueOf(10), probe.getTimeoutSeconds());
        assertNull(probe.getHttpGet());
        assertNull(probe.getExec());
        assertNull(probe.getTcpSocket());
    }

    @Test
    public void testGetProbe_HTTPGetAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("http://example.com/path");
        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        assertNotNull(probe.getHttpGet());
        assertNull(probe.getExec());
        assertNull(probe.getTcpSocket());
        HTTPGetAction action = probe.getHttpGet();
        assertEquals("example.com", action.getHost());
        assertEquals("/path", action.getPath());
        assertEquals("HTTP", action.getScheme());
    }

    @Test
    public void testGetProbe_ExecAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setExec("ls -la");
        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        assertNotNull(probe.getExec());
        assertNull(probe.getHttpGet());
        assertNull(probe.getTcpSocket());
        ExecAction action = probe.getExec();
        assertEquals(2, action.getCommand().size());
        assertEquals("ls", action.getCommand().get(0));
        assertEquals("-la", action.getCommand().get(1));
    }

    @Test
    public void testGetProbe_TCPSocketAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("https://example.com:8080/path");
        config.setTcpPort("8080");
        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        assertNotNull(probe.getTcpSocket());
        assertNull(probe.getHttpGet());
        assertNull(probe.getExec());
        TCPSocketAction action = probe.getTcpSocket();
        assertEquals("example.com", action.getHost());
    }

    @Test
    public void testGetProbe_NoAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setInitialDelaySeconds(5);
        Probe probe = handler.getProbe(config);
        assertNull(probe);
    }

    @Test
    public void testGetHTTPGetAction_ValidURL() {
        ProbeHandler handler = new ProbeHandler();
        HTTPGetAction action = handler.getHTTPGetAction("http://example.com:8080/path");
        assertNotNull(action);
        assertEquals("example.com", action.getHost());
        assertEquals("/path", action.getPath());
        assertEquals("HTTP", action.getScheme());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHTTPGetAction_InvalidURL() {
        ProbeHandler handler = new ProbeHandler();
        handler.getHTTPGetAction("invalid-url");
    }

    @Test
    public void testGetTCPSocketAction_ValidPortAndURL() {
        ProbeHandler handler = new ProbeHandler();
        TCPSocketAction action = handler.getTCPSocketAction("https://example.com:8080/path", "8080");
        assertNotNull(action);
        assertEquals("example.com", action.getHost());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTCPSocketAction_InvalidURL() {
        ProbeHandler handler = new ProbeHandler();
        handler.getTCPSocketAction("invalid-url", "8080");
    }

    @Test
    public void testGetExecAction_ValidCommand() {
        ProbeHandler handler = new ProbeHandler();
        ExecAction action = handler.getExecAction("ls -la /tmp");
        assertNotNull(action);
        assertEquals(4, action.getCommand().size());
    }

    @Test
    public void testGetExecAction_EmptyCommand() {
        ProbeHandler handler = new ProbeHandler();
        ExecAction action = handler.getExecAction("");
        assertNull(action);
    }
}
