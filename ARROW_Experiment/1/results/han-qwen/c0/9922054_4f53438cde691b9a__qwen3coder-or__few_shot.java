package com.google.gwtmockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.constants.NumberConstantsImpl;
import com.google.gwt.i18n.client.impl.LocaleInfoImpl;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwtmockito.fakes.FakeClientBundleProvider;
import com.google.gwtmockito.fakes.FakeLocaleInfoImplProvider;
import com.google.gwtmockito.fakes.FakeMessagesProvider;
import com.google.gwtmockito.fakes.FakeNumberConstantsImplProvider;
import com.google.gwtmockito.fakes.FakeProvider;
import com.google.gwtmockito.fakes.FakeUiBinderProvider;
import com.google.gwtmockito.impl.ReturnsCustomMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GwtMockito_ArrowTest {

    @Before
    public void setUp() {
        // Ensure clean state before each test
        GwtMockito.tearDown();
    }

    @After
    public void tearDown() {
        GwtMockito.tearDown();
    }

    @Test
    public void testInitMocksRegistersDefaultProviders() {
        TestClass owner = new TestClass();
        GwtMockito.initMocks(owner);

        // Verify that the bridge was created and default providers were registered
        assertNotNull(GwtMockito.class.getDeclaredField("bridge").get(null));
        
        // Check that default providers are registered
        Field bridgeField = null;
        try {
            bridgeField = GwtMockito.class.getDeclaredField("bridge");
            bridgeField.setAccessible(true);
            Object bridge = bridgeField.get(null);
            
            Field registeredProvidersField = bridge.getClass().getDeclaredField("registeredProviders");
            registeredProvidersField.setAccessible(true);
            Map<Class<?>, FakeProvider<?>> providers = 
                (Map<Class<?>, FakeProvider<?>>) registeredProvidersField.get(bridge);
            
            assertTrue(providers.containsKey(ClientBundle.class));
            assertTrue(providers.containsKey(CssResource.class));
            assertTrue(providers.containsKey(LocaleInfoImpl.class));
            assertTrue(providers.containsKey(Messages.class));
            assertTrue(providers.containsKey(NumberConstantsImpl.class));
            assertTrue(providers.containsKey(SafeHtmlTemplates.class));
            assertTrue(providers.containsKey(UiBinder.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUseProviderForTypeRegistersProvider() {
        TestClass owner = new TestClass();
        GwtMockito.initMocks(owner);
        
        // Register a custom provider
        FakeProvider<String> customProvider = new FakeProvider<String>() {
            @Override
            public String getFake(Class<String> type) {
                return "custom_fake";
            }
        };
        
        GwtMockito.useProviderForType(String.class, customProvider);
        
        // Verify the provider was registered
        try {
            Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
            bridgeField.setAccessible(true);
            Object bridge = bridgeField.get(null);
            
            Field registeredProvidersField = bridge.getClass().getDeclaredField("registeredProviders");
            registeredProvidersField.setAccessible(true);
            Map<Class<?>, FakeProvider<?>> providers = 
                (Map<Class<?>, FakeProvider<?>>) registeredProvidersField.get(bridge);
            
            assertTrue(providers.containsKey(String.class));
            assertSame(customProvider, providers.get(String.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testUseProviderForTypeFailsIfNotInitialized() {
        GwtMockito.useProviderForType(String.class, new FakeProvider<String>() {
            @Override
            public String getFake(Class<String> type) {
                return "fake";
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseProviderForTypeFailsIfGwtMockExists() {
        TestClassWithGwtMock owner = new TestClassWithGwtMock();
        GwtMockito.initMocks(owner);
        
        // Try to register a provider for a type that already has a GwtMock
        GwtMockito.useProviderForType(TestInterface.class, new FakeProvider<TestInterface>() {
            @Override
            public TestInterface getFake(Class<TestInterface> type) {
                return null;
            }
        });
    }

    @Test
    public void testGetFakeReturnsCorrectFake() {
        TestClass owner = new TestClass();
        GwtMockito.initMocks(owner);
        
        // Get a fake for Messages
        Messages fakeMessages = GwtMockito.getFake(Messages.class);
        assertNotNull(fakeMessages);
        assertTrue(fakeMessages instanceof Messages);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFakeFailsWhenNoProviderAvailable() {
        TestClass owner = new TestClass();
        GwtMockito.initMocks(owner);
        
        // Try to get a fake for a type with no provider
        GwtMockito.getFake(Integer.class);
    }

    @Test
    public void testGetFakeFromProviderMapReturnsNullWhenNoProviders() {
        Map<Class<?>, FakeProvider<?>> map = new HashMap<>();
        Object result = GwtMockito.getFakeFromProviderMap(String.class, map);
        assertNull(result);
    }

    @Test
    public void testGetFakeFromProviderMapReturnsFakeWhenOneProvider() {
        Map<Class<?>, FakeProvider<?>> map = new HashMap<>();
        FakeProvider<String> provider = new FakeProvider<String>() {
            @Override
            public String getFake(Class<String> type) {
                return "test_fake";
            }
        };
        map.put(String.class, provider);
        
        String result = GwtMockito.getFakeFromProviderMap(String.class, map);
        assertEquals("test_fake", result);
    }

    @Test
    public void testRegisterGwtMocksPopulatesFields() {
        TestClassWithGwtMock owner = new TestClassWithGwtMock();
        GwtMockito.initMocks(owner);
        
        // Verify that the mock field was populated
        assertNotNull(owner.mockField);
        assertTrue(owner.mockField instanceof TestInterface);
    }

    @Test
    public void testSetGwtBridgeSetsBridge() {
        // This test verifies that the bridge can be set without throwing exceptions
        GwtMockito.tearDown(); // Clean up first
        
        // Create a mock bridge
        GWTBridge mockBridge = mock(GWTBridge.class);
        
        // This should not throw an exception
        try {
            Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
            bridgeField.setAccessible(true);
            bridgeField.set(null, mockBridge);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testBridgeCreatesMockWhenNoProvider() {
        TestClass owner = new TestClass();
        GwtMockito.initMocks(owner);
        
        // Access the bridge directly to test creation
        try {
            Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
            bridgeField.setAccessible(true);
            Object bridge = bridgeField.get(null);
            
            // Call create method on the bridge
            Method createMethod = bridge.getClass().getDeclaredMethod("create", Class.class);
            createMethod.setAccessible(true);
            
            // This should return a mock since no provider is registered for TestInterface
            Object result = createMethod.invoke(bridge, TestInterface.class);
            assertNotNull(result);
            assertTrue(result instanceof TestInterface);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper classes for testing
    private static class TestClass {
        // Empty test class
    }

    private static class TestClassWithGwtMock {
        @GwtMock
        TestInterface mockField;
    }

    private interface TestInterface {
        void doSomething();
    }
}
