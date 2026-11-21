package com.weibo.api.motan.transport;

import com.weibo.api.motan.exception.MotanFrameworkException;
import com.weibo.api.motan.rpc.DefaultRequest;
import com.weibo.api.motan.rpc.Provider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProviderMessageRouter_ArrowTest {

    @Test
    public void defaultRouterStartsWithNoPublicMethods() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        assertEquals(0, router.getPublicMethodCount());
    }

    @Test
    public void providerConstructorCountsPublicInterfaceMethods() {
        Provider<?> provider = new Provider<Object>() {
            @Override
            public Object call(com.weibo.api.motan.rpc.Request request) {
                return null;
            }

            @Override
            public Class<Object> getInterface() {
                return Object.class;
            }

            @Override
            public com.weibo.api.motan.rpc.URL getUrl() {
                return null;
            }

            @Override
            public java.lang.reflect.Method lookupMethod(String methodName, String paramtersDesc) {
                return null;
            }
        };
        ProviderMessageRouter router = new ProviderMessageRouter(provider);
        assertTrue(router.getPublicMethodCount() > 0);
    }

    @Test
    public void handleRejectsNullChannel() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        try {
            router.handle(null, new DefaultRequest());
        } catch (MotanFrameworkException expected) {
            assertNotNull(expected);
            return;
        }
        throw new AssertionError("Expected MotanFrameworkException");
    }

    @Test
    public void handleRejectsNullMessage() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        try {
            router.handle(null, null);
        } catch (MotanFrameworkException expected) {
            assertNotNull(expected);
            return;
        }
        throw new AssertionError("Expected MotanFrameworkException");
    }

    @Test
    public void removeProviderFromEmptyRouterLeavesCountAtZero() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        Provider<?> provider = new Provider<Object>() {
            @Override
            public Object call(com.weibo.api.motan.rpc.Request request) {
                return null;
            }

            @Override
            public Class<Object> getInterface() {
                return Object.class;
            }

            @Override
            public com.weibo.api.motan.rpc.URL getUrl() {
                return null;
            }

            @Override
            public java.lang.reflect.Method lookupMethod(String methodName, String paramtersDesc) {
                return null;
            }
        };
        try {
            router.removeProvider(provider);
        } catch (NullPointerException expected) {
            assertEquals(0, router.getPublicMethodCount());
            return;
        }
        assertEquals(-Object.class.getMethods().length, router.getPublicMethodCount());
    }
}
