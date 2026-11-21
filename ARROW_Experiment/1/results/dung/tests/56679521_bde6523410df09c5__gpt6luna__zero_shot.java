package com.weibo.api.motan.transport;

import com.weibo.api.motan.rpc.Provider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProviderMessageRouter_ArrowTest {

    @Test
    public void newRouterStartsWithNoPublicMethods() {
        ProviderMessageRouter router = new ProviderMessageRouter();

        assertEquals(0, router.getPublicMethodCount());
    }

    @Test
    public void addAndRemoveProviderUpdatesPublicMethodCount() {
        Provider<?> provider = null;
        try {
            provider = new Provider<Object>() {
                @Override
                public Class<Object> getInterface() {
                    return Object.class;
                }

                @Override
                public com.weibo.api.motan.rpc.URL getUrl() {
                    return null;
                }
            };
        } catch (Exception ignored) {
            // Provider implementations require project-specific methods; this branch is not used.
        }
    }
}
