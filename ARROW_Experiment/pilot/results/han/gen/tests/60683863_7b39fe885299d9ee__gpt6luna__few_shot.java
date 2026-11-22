package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ConditionRouter_ArrowTest {

    private URL routerUrl(String rule) {
        return URL.valueOf("condition://0.0.0.0/service?"
                + Constants.RULE_KEY + "=" + URL.encode(rule));
    }

    @Test
    public void matchWhenMatchesConfiguredProviderCondition() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.0.0.1=>host=10.0.0.2"));
        URL provider = URL.valueOf("dubbo://10.0.0.1:20880/service");

        assertTrue(router.matchWhen(provider));
    }

    @Test
    public void matchWhenRejectsDifferentConfiguredProviderCondition() {
        ConditionRouter router = new ConditionRouter(routerUrl("host=10.0.0.1=>host=10.0.0.2"));
        URL provider = URL.valueOf("dubbo://10.0.0.3:20880/service");

        assertFalse(router.matchWhen(provider));
    }

    @Test
    public void matchWhenWithTrueConditionMatchesAnyUrl() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.0.0.2"));
        URL provider = URL.valueOf("dubbo://10.0.0.3:20880/service");

        assertTrue(router.matchWhen(provider));
    }

    @Test
    public void matchThenMatchesConfiguredProviderCondition() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.0.0.2"));
        URL provider = URL.valueOf("dubbo://10.0.0.2:20880/service");

        assertTrue(router.matchThen(provider, URL.valueOf("consumer://10.0.0.1/service")));
    }

    @Test
    public void matchThenRejectsNonMatchingProviderCondition() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.0.0.2"));
        URL provider = URL.valueOf("dubbo://10.0.0.3:20880/service");

        assertFalse(router.matchThen(provider, URL.valueOf("consumer://10.0.0.1/service")));
    }

    @Test
    public void falseThenConditionNeverMatches() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>false"));
        URL provider = URL.valueOf("dubbo://10.0.0.2:20880/service");

        assertFalse(router.matchThen(provider, URL.valueOf("consumer://10.0.0.1/service")));
    }

    @Test
    public void getUrlReturnsConfiguredUrl() {
        URL url = routerUrl("true=>host=10.0.0.2");
        ConditionRouter router = new ConditionRouter(url);

        assertSame(url, router.getUrl());
    }

    @Test
    public void compareToOrdersByPriority() {
        URL lowerPriorityUrl = URL.valueOf("condition://0.0.0.0/service?"
                + Constants.RULE_KEY + "=" + URL.encode("true=>host=10.0.0.1")
                + "&" + Constants.PRIORITY_KEY + "=1");
        URL higherPriorityUrl = URL.valueOf("condition://0.0.0.0/service?"
                + Constants.RULE_KEY + "=" + URL.encode("true=>host=10.0.0.1")
                + "&" + Constants.PRIORITY_KEY + "=2");
        ConditionRouter lowerPriority = new ConditionRouter(lowerPriorityUrl);
        ConditionRouter higherPriority = new ConditionRouter(higherPriorityUrl);

        assertTrue(higherPriority.compareTo(lowerPriority) > 0);
        assertTrue(lowerPriority.compareTo(higherPriority) < 0);
    }

    @Test
    public void compareToOrdersEqualPriorityByFullUrl() {
        URL firstUrl = URL.valueOf("condition://0.0.0.0/service?"
                + Constants.RULE_KEY + "=" + URL.encode("true=>host=10.0.0.1")
                + "&" + Constants.PRIORITY_KEY + "=1");
        URL secondUrl = URL.valueOf("condition://0.0.0.0/service?"
                + Constants.RULE_KEY + "=" + URL.encode("true=>host=10.0.0.2")
                + "&" + Constants.PRIORITY_KEY + "=1");
        ConditionRouter first = new ConditionRouter(firstUrl);
        ConditionRouter second = new ConditionRouter(secondUrl);

        assertEquals(firstUrl.toFullString().compareTo(secondUrl.toFullString()),
                first.compareTo(second));
    }

    @Test
    public void compareToReturnsOneForDifferentRouterType() {
        ConditionRouter router = new ConditionRouter(routerUrl("true=>host=10.0.0.2"));

        assertEquals(1, router.compareTo(null));
    }

    @Test
    public void emptyRuleIsRejected() {
        try {
            new ConditionRouter(routerUrl(" "));
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected an IllegalArgumentException");
    }
}
