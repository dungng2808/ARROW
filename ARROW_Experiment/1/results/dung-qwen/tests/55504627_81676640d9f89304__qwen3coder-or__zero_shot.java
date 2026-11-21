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
        config.setFailureThreshold(3);
        config.setSuccessThreshold(2);

        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        assertEquals(Integer.valueOf(5), probe.getInitialDelaySeconds());
        assertEquals(Integer.valueOf(10), probe.getTimeoutSeconds());
        assertEquals(Integer.valueOf(3), probe.getFailureThreshold());
        assertEquals(Integer.valueOf(2), probe.getSuccessThreshold());
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
        HTTPGetAction httpGetAction = probe.getHttpGet();
        assertNotNull(httpGetAction);
        assertEquals("example.com", httpGetAction.getHost());
        assertEquals("/path", httpGetAction.getPath());
        assertEquals("HTTP", httpGetAction.getScheme());
        assertNull(httpGetAction.getHttpHeaders());
        assertNull(probe.getExec());
        assertNull(probe.getTcpSocket());
    }

    @Test
    public void testGetProbe_ExecAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setExec("ls -l");

        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        ExecAction execAction = probe.getExec();
        assertNotNull(execAction);
        assertEquals(2, execAction.getCommand().size());
        assertEquals("ls", execAction.getCommand().get(0));
        assertEquals("-l", execAction.getCommand().get(1));
        assertNull(probe.getHttpGet());
        assertNull(probe.getTcpSocket());
    }

    @Test
    public void testGetProbe_TCPSocketAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();
        config.setGetUrl("https://example.com");
        config.setTcpPort("8080");

        Probe probe = handler.getProbe(config);
        assertNotNull(probe);
        TCPSocketAction tcpSocketAction = probe.getTcpSocket();
        assertNotNull(tcpSocketAction);
        assertEquals("example.com", tcpSocketAction.getHost());
        assertEquals("8080", tcpSocketAction.getPort().getStrVal());
        assertNull(probe.getHttpGet());
        assertNull(probe.getExec());
    }

    @Test
    public void testGetProbe_NoAction() {
        ProbeHandler handler = new ProbeHandler();
        ProbeConfig config = new ProbeConfig();

        Probe probe = handler.getProbe(config);
        assertNull(probe);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHTTPGetAction_InvalidURL() {
        ProbeHandler handler = new ProbeHandler();
        handler.getHTTPGetAction("invalid-url");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTCPSocketAction_InvalidURL() {
        ProbeHandler handler = new ProbeHandler();
        handler.getTCPSocketAction("invalid-url", "8080");
    }
}
