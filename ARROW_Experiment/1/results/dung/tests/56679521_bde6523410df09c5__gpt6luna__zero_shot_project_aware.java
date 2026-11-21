package com.weibo.api.motan.transport;

import com.weibo.api.motan.exception.MotanFrameworkException;
import com.weibo.api.motan.rpc.Provider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProviderMessageRouter_ArrowTest {

    @Test
    public void emptyRouterHasNoPublicMethods() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        assertEquals(0, router.getPublicMethodCount());
    }

    @Test
    public void routerConstructedWithNullProviderHasNoPublicMethods() {
        ProviderMessageRouter router = new ProviderMessageRouter((Provider<?>) null);
        assertEquals(0, router.getPublicMethodCount());
    }

    @Test
    public void removeProviderDoesNotLeavePublicMethodCount() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        router.removeProvider(null);
        assertEquals(0, router.getPublicMethodCount());
    }

    @Test
    public void handleRejectsNullChannel() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        try {
            router.handle(null, new Object());
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
    public void handleRejectsUnsupportedMessageType() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        try {
            router.handle(new Channel() {
            }, new Object());
        } catch (MotanFrameworkException expected) {
            assertNotNull(expected);
            return;
        }
        throw new AssertionError("Expected MotanFrameworkException");
    }
}
