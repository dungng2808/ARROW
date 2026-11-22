package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ProxyURLToContextMapping_ArrowTest {

    @Test
    public void registerAndGetContext() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = org.mockito.Mockito.mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);

        assertTrue(mapping.isRegistered(url));
        assertSame(context, mapping.get(url));
    }

    @Test
    public void unregisterRemovesRegisteredUrl() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = org.mockito.Mockito.mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
        mapping.unregister(ProxyURLToContextMapping_ArrowTest.class);

        assertFalse(mapping.isRegistered(url));
    }

    @Test(expected = IllegalStateException.class)
    public void registeringSameUrlTwiceThrows() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = org.mockito.Mockito.mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
    }

    @Test(expected = ProxyURLToContextMapping.OperationalContextNotBoundException.class)
    public void getUnregisteredUrlThrows() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();

        mapping.get(new URL("http://localhost/proxy"));
    }

    @Test
    public void unregisterUnknownClassLeavesUrlRegistered() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = org.mockito.Mockito.mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
        mapping.unregister(String.class);

        assertTrue(mapping.isRegistered(url));
    }
}
