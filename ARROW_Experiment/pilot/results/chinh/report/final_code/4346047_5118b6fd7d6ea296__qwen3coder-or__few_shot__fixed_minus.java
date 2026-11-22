package org.jboss.arquillian.warp.impl.client.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.jboss.arquillian.warp.impl.client.context.operation.OperationalContext;
import org.junit.Before;
import org.junit.Test;

public class ProxyURLToContextMapping_ArrowTest {

    private ProxyURLToContextMapping mapping;
    private URL url1;
    private URL url2;
    private Class<?> testClass1;
    private Class<?> testClass2;
    private OperationalContext context1;
    private OperationalContext context2;

    @Before
    public void setUp() throws MalformedURLException {
        mapping = new ProxyURLToContextMapping();
        url1 = new URL("http://example.com/proxy1");
        url2 = new URL("http://example.com/proxy2");
        testClass1 = String.class;
        testClass2 = Integer.class;
        context1 = new OperationalContext() {
            @Override
            public void activate() {
            }
            
            @Override
            public void deactivate() {
            }
        };
        context2 = new OperationalContext() {
            @Override
            public void activate() {
            }
            
            @Override
            public void deactivate() {
            }
        };
    }

    @Test
    public void should_register_and_retrieve_context() {
        mapping.register(url1, testClass1, context1);
        
        OperationalContext retrieved = mapping.get(url1);
        assertEquals(context1, retrieved);
    }

    @Test
    public void should_throw_exception_when_getting_nonexistent_context() {
        try {
            mapping.get(url1);
            fail("Expected OperationalContextNotBoundException");
        } catch (ProxyURLToContextMapping.OperationalContextNotBoundException e) {
            // Expected
        }
    }

    @Test
    public void should_register_multiple_urls_for_same_class() {
        mapping.register(url1, testClass1, context1);
        mapping.register(url2, testClass1, context2);
        
        OperationalContext retrieved1 = mapping.get(url1);
        OperationalContext retrieved2 = mapping.get(url2);
        
        assertEquals(context1, retrieved1);
        assertEquals(context2, retrieved2);
    }

    @Test
    public void should_check_if_url_is_registered() {
        assertFalse(mapping.isRegistered(url1));
        
        mapping.register(url1, testClass1, context1);
        assertTrue(mapping.isRegistered(url1));
    }

    @Test
    public void should_unregister_context_by_class() {
        mapping.register(url1, testClass1, context1);
        mapping.register(url2, testClass1, context2);
        
        assertTrue(mapping.isRegistered(url1));
        assertTrue(mapping.isRegistered(url2));
        
        mapping.unregister(testClass1);
        
        assertFalse(mapping.isRegistered(url1));
        assertFalse(mapping.isRegistered(url2));
    }

    @Test
    public void should_not_throw_exception_when_unregistering_nonexistent_class() {
        mapping.unregister(testClass1);
        // Should not throw exception
    }

    @Test
    public void should_prevent_duplicate_registration() {
        mapping.register(url1, testClass1, context1);
        
        try {
            mapping.register(url1, testClass1, context2);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void should_handle_different_classes_with_same_url() {
        mapping.register(url1, testClass1, context1);
        
        try {
            mapping.register(url1, testClass2, context2);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test
    public void should_return_correct_urls_for_test_class() {
        mapping.register(url1, testClass1, context1);
        mapping.register(url2, testClass1, context2);
        
        Set<URL> urls = mapping.testClassToUrls.get(testClass1);
        assertTrue(urls.contains(url1));
        assertTrue(urls.contains(url2));
    }
}
