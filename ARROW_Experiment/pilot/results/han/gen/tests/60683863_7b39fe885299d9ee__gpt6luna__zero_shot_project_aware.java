package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ConditionRouter_ArrowTest {

    private URL routerUrl(String rule) {
        return URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&rule=" + URL.encode(rule));
    }

    private URL providerUrl(String host) {
        return URL.valueOf("dubbo://" + host + ":20880/com.foo.BarService");
    }

    @Test
    public void matchWhenMatchesConfiguredHost() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.20.153.10=>host=10.20.153.11"));

        assertTrue(router.matchWhen(providerUrl("10.20.153.10")));
    }

    @Test
    public void matchWhenRejectsDifferentConfiguredHost() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.20.153.10=>host=10.20.153.11"));

        assertFalse(router.matchWhen(providerUrl("10.20.153.12")));
    }

    @Test
    public void matchWhenAllowsUnspecifiedHost() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.20.153.10=>host=10.20.153.11"));

        assertTrue(router.matchWhen(providerUrl("10.20.153.11")));
    }

    @Test
    public void matchWhenSupportsNegatedHost() {
        ConditionRouter router = new ConditionRouter(routerUrl("host!=10.20.153.10=>host=10.20.153.11"));

        assertFalse(router.matchWhen(providerUrl("10.20.153.10")));
        assertTrue(router.matchWhen(providerUrl("10.20.153.12")));
    }

    @Test
    public void matchWhenSupportsMultipleValues() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.20.153.10,10.20.153.11=>host=10.20.153.12"));

        assertTrue(router.matchWhen(providerUrl("10.20.153.10")));
        assertTrue(router.matchWhen(providerUrl("10.20.153.11")));
        assertFalse(router.matchWhen(providerUrl("10.20.153.12")));
    }

    @Test
    public void matchWhenAllowsEmptyCondition() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.20.153.11"));

        assertTrue(router.matchWhen(providerUrl("10.20.153.10")));
    }

    @Test
    public void matchThenMatchesConfiguredHost() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.20.153.11"));

        assertTrue(router.matchThen(providerUrl("10.20.153.11"), providerUrl("10.20.153.10")));
        assertFalse(router.matchThen(providerUrl("10.20.153.12"), providerUrl("10.20.153.10")));
    }

    @Test
    public void matchThenUsesConsumerUrlForGlobParameter() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=$host"));

        assertTrue(router.matchThen(providerUrl("10.20.153.10"), providerUrl("10.20.153.10")));
        assertFalse(router.matchThen(providerUrl("10.20.153.11"), providerUrl("10.20.153.10")));
    }

    @Test
    public void matchThenIsFalseWhenThenConditionIsAbsent() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>false"));

        assertFalse(router.matchThen(providerUrl("10.20.153.10"), providerUrl("10.20.153.10")));
    }

    @Test
    public void routeReturnsInputWhenInvokerListIsEmpty() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.20.153.11"));
        List<Invoker<Object>> invokers = Collections.emptyList();

        assertSame(invokers, router.route(invokers, providerUrl("10.20.153.10"), null));
    }

    @Test
    public void routeReturnsInputWhenInvokerListIsNull() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.20.153.11"));

        assertEquals(null, router.route(null, providerUrl("10.20.153.10"), null));
    }

    @Test
    public void routeReturnsInputWhenWhenConditionDoesNotMatch() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.20.153.10=>host=10.20.153.11"));
        List<Invoker<Object>> invokers = new ArrayList<Invoker<Object>>();

        assertSame(invokers, router.route(invokers, providerUrl("10.20.153.12"), null));
    }

    @Test
    public void routeReturnsEmptyListForBlacklistRule() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>false"));
        List<Invoker<Object>> invokers = new ArrayList<Invoker<Object>>();

        assertTrue(router.route(invokers, providerUrl("10.20.153.10"), null).isEmpty());
    }

    @Test
    public void routeReturnsInputWhenNoProviderMatchesAndNotForced() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.20.153.11"));
        List<Invoker<Object>> invokers = new ArrayList<Invoker<Object>>();

        assertSame(invokers, router.route(invokers, providerUrl("10.20.153.10"), null));
    }

    @Test
    public void routeReturnsEmptyListWhenNoProviderMatchesAndForced() {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&force=true&rule="
                + URL.encode("true=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<Object>> invokers = new ArrayList<Invoker<Object>>();

        assertNotSame(invokers, router.route(invokers, providerUrl("10.20.153.10"), null));
        assertTrue(router.route(invokers, providerUrl("10.20.153.10"), null).isEmpty());
    }

    @Test
    public void getUrlReturnsConstructorUrl() {
        URL url = routerUrl("true=>host=10.20.153.11");
        ConditionRouter router = new ConditionRouter(url);

        assertSame(url, router.getUrl());
    }

    @Test
    public void compareToOrdersByPriority() {
        URL lowerUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?priority=1&rule="
                + URL.encode("true=>host=10.20.153.11"));
        URL higherUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?priority=2&rule="
                + URL.encode("true=>host=10.20.153.11"));
        ConditionRouter lower = new ConditionRouter(lowerUrl);
        ConditionRouter higher = new ConditionRouter(higherUrl);

        assertTrue(higher.compareTo(lower) > 0);
        assertTrue(lower.compareTo(higher) < 0);
    }

    @Test
    public void compareToReturnsOneForDifferentRouterType() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.20.153.11"));

        assertEquals(1, router.compareTo(null));
    }

    @Test
    public void constructorRejectsMissingRule() {
        try {
            new ConditionRouter(URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers"));
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
