package org.jboss.arquillian.warp.impl.client.proxy;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.junit.Test;
import static org.junit.Assert.*;
import java.net.URL;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyURLToContextMapping_ArrowTest {

    @Test
    public void testRegister() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = mockURL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context = mockContext();

        mapping.register(url, testClass, context);

        assertEquals(context, mapping.get(url));
        assertTrue(mapping.isRegistered(url));
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterTwice() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = mockURL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context1 = mockContext();
        OperationalContext context2 = mockContext();

        mapping.register(url, testClass, context1);
        mapping.register(url, testClass, context2);
    }

    @Test
    public void testGet() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = mockURL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context = mockContext();

        mapping.register(url, testClass, context);

        assertEquals(context, mapping.get(url));
    }

    @Test(expected = ProxyURLToContextMapping.OperationalContextNotBoundException.class)
    public void testGetUnregisteredUrl() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = mockURL("http://example.com/proxy");

        mapping.get(url);
    }

    @Test
    public void testUnregister() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url1 = mockURL("http://example.com/proxy1");
        URL url2 = mockURL("http://example.com/proxy2");
        Class<?> testClass = String.class;
        OperationalContext context = mockContext();

        mapping.register(url1, testClass, context);
        mapping.register(url2, testClass, context);

        mapping.unregister(testClass);

        assertFalse(mapping.isRegistered(url1));
        assertFalse(mapping.isRegistered(url2));
    }

    @Test
    public void testUnregisterNonExistentClass() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        Class<?> testClass = String.class;

        mapping.unregister(testClass);
    }

    @Test
    public void testIsRegistered() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = mockURL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context = mockContext();

        assertFalse(mapping.isRegistered(url));

        mapping.register(url, testClass, context);

        assertTrue(mapping.isRegistered(url));
    }

    @Test
    public void testMultipleClasses() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url1 = mockURL("http://example.com/proxy1");
        URL url2 = mockURL("http://example.com/proxy2");
        Class<?> testClass1 = String.class;
        Class<?> testClass2 = Integer.class;
        OperationalContext context1 = mockContext();
        OperationalContext context2 = mockContext();

        mapping.register(url1, testClass1, context1);
        mapping.register(url2, testClass2, context2);

        assertEquals(context1, mapping.get(url1));
        assertEquals(context2, mapping.get(url2));
        assertTrue(mapping.isRegistered(url1));
        assertTrue(mapping.isRegistered(url2));
    }

    private URL mockURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OperationalContext mockContext() {
        return new OperationalContext() {
            public void execute(Runnable operation) {
                // Mock implementation
            }

            public void deactivate() {
                // Mock implementation
            }

            public void activate() {
                // Mock implementation
            }
        };
    }
}
