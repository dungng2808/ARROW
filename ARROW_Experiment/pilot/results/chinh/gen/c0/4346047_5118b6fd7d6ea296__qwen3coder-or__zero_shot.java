package org.jboss.arquillian.warp.impl.client.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProxyURLToContextMapping_ArrowTest {

    @Test
    public void testRegisterAndGet() throws MalformedURLException {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context = new OperationalContext() {};
        
        mapping.register(url, testClass, context);
        
        assertEquals(context, mapping.get(url));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testRegisterTwiceThrowsIllegalStateException() throws MalformedURLException {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context1 = new OperationalContext() {};
        OperationalContext context2 = new OperationalContext() {};
        
        mapping.register(url, testClass, context1);
        mapping.register(url, testClass, context2);
    }
    
    @Test(expected = ProxyURLToContextMapping.OperationalContextNotBoundException.class)
    public void testGetUnregisteredUrlThrowsException() throws MalformedURLException {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://example.com/proxy");
        
        mapping.get(url);
    }
    
    @Test
    public void testUnregister() throws MalformedURLException {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context = new OperationalContext() {};
        
        mapping.register(url, testClass, context);
        assertTrue(mapping.isRegistered(url));
        
        mapping.unregister(testClass);
        assertFalse(mapping.isRegistered(url));
    }
    
    @Test
    public void testUnregisterNonExistentClassDoesNothing() {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        Class<?> testClass = String.class;
        
        mapping.unregister(testClass); // Should not throw exception
    }
    
    @Test
    public void testIsRegistered() throws MalformedURLException {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url = new URL("http://example.com/proxy");
        Class<?> testClass = String.class;
        OperationalContext context = new OperationalContext() {};
        
        assertFalse(mapping.isRegistered(url));
        
        mapping.register(url, testClass, context);
        assertTrue(mapping.isRegistered(url));
    }
    
    @Test
    public void testMultipleUrlsForSameTestClass() throws MalformedURLException {
        ProxyURLToContextMapping mapping = new ProxyURLToContextMapping();
        URL url1 = new URL("http://example.com/proxy1");
        URL url2 = new URL("http://example.com/proxy2");
        Class<?> testClass = String.class;
        OperationalContext context = new OperationalContext() {};
        
        mapping.register(url1, testClass, context);
        mapping.register(url2, testClass, context);
        
        assertEquals(context, mapping.get(url1));
        assertEquals(context, mapping.get(url2));
        
        assertTrue(mapping.isRegistered(url1));
        assertTrue(mapping.isRegistered(url2));
    }
}
