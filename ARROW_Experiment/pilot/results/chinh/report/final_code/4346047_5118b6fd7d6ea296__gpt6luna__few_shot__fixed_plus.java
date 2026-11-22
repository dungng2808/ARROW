package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.URL;
import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProxyURLToContextMapping_ArrowTest {

    private OperationalContext newContext() {
        return new OperationalContext() {
            @Override
            public void activate() {
            }

            @Override
            public void deactivate() {
            }
        };
    }

    @Test
    public void registerMakesUrlAvailableAndReturnsItsContext() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = newContext();

        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);

        assertTrue(mapping.isRegistered(url));
        assertSame(context, mapping.get(url));
    }

    @Test
    public void registeringAnAlreadyRegisteredUrlThrows() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        OperationalContext context = newContext();
        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);

        try {
            mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, context);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains(url.toString()));
        }
    }

    @Test
    public void getForUnregisteredUrlThrows() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/unregistered");

        try {
            mapping.get(url);
            fail("Expected OperationalContextNotBoundException");
        } catch (ProxyURLToContextMapping.OperationalContextNotBoundException expected) {
            assertTrue(expected.getMessage().contains(url.toString()));
        }
    }

    @Test
    public void unregisterRemovesUrlsRegisteredForClass() throws Exception {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://localhost/proxy");
        mapping.register(url, ProxyURLToContextMapping_ArrowTest.class, newContext());

        mapping.unregister(ProxyURLToContextMapping_ArrowTest.class);

        assertFalse(mapping.isRegistered(url));
    }

    @Test
    public void unregisterForClassWithoutRegistrationsDoesNothing() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();

        mapping.unregister(ProxyURLToContextMapping_ArrowTest.class);
    }
}
