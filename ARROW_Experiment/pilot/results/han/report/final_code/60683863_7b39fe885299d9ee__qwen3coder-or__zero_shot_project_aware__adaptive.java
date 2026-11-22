package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.*;

public class ConditionRouter_ArrowTest {

    private URL url;
    private ConditionRouter router;

    @Before
    public void setUp() {
        url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        router = new ConditionRouter(url);
    }

    @Test
    public void testConstructor() {
        assertNotNull(router);
        assertEquals(url, router.getUrl());
    }

    @Test
    public void testRouteWithEmptyInvokers() throws RpcException {
        List<Invoker<String>> invokers = new ArrayList<>();
        List<Invoker<String>> result = router.route(invokers, url, null);
        assertEquals(invokers, result);
    }

    @Test
    public void testRouteWithNullInvokers() throws RpcException {
        List<Invoker<String>> result = router.route(null, url, null);
        assertNull(result);
    }

    @Test
    public void testRouteWhenConditionNotMatch() throws RpcException {
        URL testUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.12=>host=10.20.153.11"));
        ConditionRouter testRouter = new ConditionRouter(testUrl);
        List<Invoker<String>> invokers = Arrays.asList(createMockInvoker("host", "10.20.153.10"));
        List<Invoker<String>> result = testRouter.route(invokers, url, null);
        assertEquals(invokers, result);
    }

    @Test
    public void testRouteThenConditionNull() throws RpcException {
        URL testUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>false"));
        ConditionRouter testRouter = new ConditionRouter(testUrl);
        List<Invoker<String>> invokers = Arrays.asList(createMockInvoker("host", "10.20.153.10"));
        List<Invoker<String>> result = testRouter.route(invokers, url, null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRouteThenConditionMatch() throws RpcException {
        URL testUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter testRouter = new ConditionRouter(testUrl);
        Invoker<String> invoker = createMockInvoker("host", "10.20.153.11");
        List<Invoker<String>> invokers = Arrays.asList(invoker);
        List<Invoker<String>> result = testRouter.route(invokers, url, null);
        assertEquals(1, result.size());
        assertEquals(invoker, result.get(0));
    }

    @Test
    public void testRouteForceExecute() throws RpcException {
        URL testUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&force=true&rule=" +
                URL.encode("host=10.20.153.10=>false"));
        ConditionRouter testRouter = new ConditionRouter(testUrl);
        List<Invoker<String>> invokers = Arrays.asList(createMockInvoker("host", "10.20.153.10"));
        List<Invoker<String>> result = testRouter.route(invokers, url, null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCompareTo() {
        URL url1 = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        URL url2 = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.12=>host=10.20.153.13"));
        ConditionRouter router1 = new ConditionRouter(url1);
        ConditionRouter router2 = new ConditionRouter(url2);
        assertEquals(0, router1.compareTo(router2));
    }

    @Test
    public void testMatchWhen() {
        URL testUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter testRouter = new ConditionRouter(testUrl);
        URL matchUrl = URL.valueOf("dubbo://10.20.153.10:20880/com.foo.BarService");
        assertTrue(testRouter.matchWhen(matchUrl));
    }

    @Test
    public void testMatchThen() {
        URL testUrl = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter testRouter = new ConditionRouter(testUrl);
        URL matchUrl = URL.valueOf("dubbo://10.20.153.11:20880/com.foo.BarService");
        URL paramUrl = URL.valueOf("dubbo://10.20.153.10:20880/com.foo.BarService");
        assertTrue(testRouter.matchThen(matchUrl, paramUrl));
    }

    @Test
    public void testParseRule() throws Exception {
        String rule = "host=10.20.153.10,10.20.153.11&port!=20880";
        Map<String, ConditionRouter.MatchPair> result = ConditionRouter.parseRule(rule);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("host"));
        assertTrue(result.containsKey("port"));
    }

    private Invoker<String> createMockInvoker(String host, String port) {
        Invoker<String> invoker = new Invoker<String>() {
            @Override
            public Class<String> getInterface() {
                return String.class;
            }

            @Override
            public URL getUrl() {
                return URL.valueOf("dubbo://" + host + ":" + port + "/com.foo.BarService");
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String invoke(Invocation invocation) throws RpcException {
                return "test";
            }

            @Override
            public void destroy() {
            }
        };
        return invoker;
    }
}
