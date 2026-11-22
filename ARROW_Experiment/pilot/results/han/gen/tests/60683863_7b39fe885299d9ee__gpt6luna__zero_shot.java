package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.Router;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConditionRouter_ArrowTest {

    @Test
    public void getUrlReturnsConstructorUrl() {
        URL url = URL.valueOf("condition://0.0.0.0/service?rule=host%3D10.0.0.1%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(url);
        assertSame(url, router.getUrl());
    }

    @Test
    public void emptyWhenConditionMatchesAnyUrl() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(routerUrl);
        URL target = URL.valueOf("dubbo://10.0.0.1:20880/service");
        assertTrue(router.matchWhen(target));
    }

    @Test
    public void matchWhenChecksConfiguredHost() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=host%3D10.0.0.1%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(routerUrl);
        URL matching = URL.valueOf("dubbo://10.0.0.1:20880/service");
        URL nonmatching = URL.valueOf("dubbo://10.0.0.3:20880/service");

        assertTrue(router.matchWhen(matching));
        assertFalse(router.matchWhen(nonmatching));
    }

    @Test
    public void matchThenChecksProviderCondition() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(routerUrl);
        URL matchingProvider = URL.valueOf("dubbo://10.0.0.2:20880/service");
        URL nonmatchingProvider = URL.valueOf("dubbo://10.0.0.3:20880/service");

        assertTrue(router.matchThen(matchingProvider, null));
        assertFalse(router.matchThen(nonmatchingProvider, null));
    }

    @Test
    public void matchThenIsFalseForBlacklistRule() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=%3D%3Efalse");
        ConditionRouter router = new ConditionRouter(routerUrl);
        URL provider = URL.valueOf("dubbo://10.0.0.2:20880/service");

        assertFalse(router.matchThen(provider, null));
    }

    @Test
    public void matchWhenSupportsMismatchValues() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=host!%3D10.0.0.1%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(routerUrl);

        assertFalse(router.matchWhen(URL.valueOf("dubbo://10.0.0.1:20880/service")));
        assertTrue(router.matchWhen(URL.valueOf("dubbo://10.0.0.3:20880/service")));
    }

    @Test
    public void routeReturnsOriginalListWhenNoInvokers() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(routerUrl);
        List<Invoker<Object>> invokers = new ArrayList<Invoker<Object>>();

        assertSame(invokers, router.route(invokers, URL.valueOf("consumer://10.0.0.1/service"), null));
    }

    @Test
    public void routeKeepsAllInvokersWhenWhenConditionDoesNotMatch() {
        URL routerUrl = URL.valueOf("condition://0.0.0.0/service?rule=host%3D10.0.0.1%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(routerUrl);
        List<Invoker<Object>> invokers = new ArrayList<Invoker<Object>>();
        invokers.add(null);
        URL consumer = URL.valueOf("consumer://10.0.0.3/service");

        assertSame(invokers, router.route(invokers, consumer, null));
    }

    @Test
    public void compareToUsesPriority() {
        URL lowPriorityUrl = URL.valueOf("condition://0.0.0.0/service?priority=1&rule=%3D%3Ehost%3D10.0.0.2");
        URL highPriorityUrl = URL.valueOf("condition://0.0.0.0/service?priority=2&rule=%3D%3Ehost%3D10.0.0.2");
        ConditionRouter lowPriority = new ConditionRouter(lowPriorityUrl);
        ConditionRouter highPriority = new ConditionRouter(highPriorityUrl);

        assertTrue(highPriority.compareTo(lowPriority) > 0);
        assertTrue(lowPriority.compareTo(highPriority) < 0);
    }

    @Test
    public void compareToPlacesOtherRouterAfterThisRouter() {
        URL url = URL.valueOf("condition://0.0.0.0/service?rule=%3D%3Ehost%3D10.0.0.2");
        ConditionRouter router = new ConditionRouter(url);

        assertEquals(1, router.compareTo(null));
    }
}
