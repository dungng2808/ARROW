package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class ProxyURLToContextMapping_ArrowTest {

    @Test
    public void registerAndGetReturnsRegisteredContext() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);

        assertSame(context, mapping.get(url));
    }

    @Test
    public void isRegisteredReflectsRegistration() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = mock(OperationalContext.class);

        assertFalse(mapping.isRegistered(url));
        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
        assertTrue(mapping.isRegistered(url));
    }

    @Test
    public void unregisterRemovesUrlsRegisteredForClass() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
        mapping.unregister(ProxyURLToContextMapping_ArrowTest.class);

        assertFalse(mapping.isRegistered(url));
    }

    @Test
    public void getThrowsWhenUrlHasNoContext() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");

        try {
            mapping.get(url);
            fail("Expected OperationalContextNotBoundException");
        } catch (ProxyURLToContextMapping.OperationalContextNotBoundException expected) {
            assertTrue(expected.getMessage().contains(url.toString()));
        }
    }

    @Test
    public void registerThrowsWhenUrlIsAlreadyRegistered() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);

        try {
            mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains(url.toString()));
        }
    }

    @Test
    public void unregisterForUnregisteredClassDoesNothing() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = mock(OperationalContext.class);

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
        mapping.unregister(String.class);

        assertTrue(mapping.isRegistered(url));
    }
}
