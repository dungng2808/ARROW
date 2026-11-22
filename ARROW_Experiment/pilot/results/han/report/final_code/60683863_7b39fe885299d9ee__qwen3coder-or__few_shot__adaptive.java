package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ConditionRouter_ArrowTest {

    @Test
    public void testConstructor() {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        assertNotNull(router);
    }

    @Test
    public void testRouteWithEmptyInvokers() throws RpcException {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<String>> invokers = new ArrayList<>();
        List<Invoker<String>> result = router.route(invokers, url, null);
        assertEquals(0, result.size());
    }

    @Test
    public void testRouteWhenConditionNotMatch() throws RpcException {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<String>> invokers = new ArrayList<>();
        Invoker<String> invoker = new MockInvoker<>("test");
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, URL.valueOf("consumer://10.20.153.12/com.foo.BarService"), null);
        assertEquals(1, result.size());
    }

    @Test
    public void testRouteThenConditionNull() throws RpcException {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10"));
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<String>> invokers = new ArrayList<>();
        Invoker<String> invoker = new MockInvoker<>("test");
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, URL.valueOf("consumer://10.20.153.10/com.foo.BarService"), null);
        assertEquals(1, result.size());
    }

    @Test
    public void testRouteThenConditionMatch() throws RpcException {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<String>> invokers = new ArrayList<>();
        Invoker<String> invoker = new MockInvoker<>("test");
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, URL.valueOf("consumer://10.20.153.10/com.foo.BarService"), null);
        assertEquals(1, result.size());
    }

    @Test
    public void testCompareTo() {
        URL url1 = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        URL url2 = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.12=>host=10.20.153.13"));
        ConditionRouter router1 = new ConditionRouter(url1);
        ConditionRouter router2 = new ConditionRouter(url2);
        assertEquals(-2, router1.compareTo(router2));
    }

    @Test
    public void testMatchWhen() {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        assertTrue(router.matchWhen(URL.valueOf("provider://10.20.153.10/com.foo.BarService")));
    }

    @Test
    public void testMatchThen() {
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(url);
        assertTrue(router.matchThen(URL.valueOf("provider://10.20.153.11/com.foo.BarService"),
                URL.valueOf("consumer://10.20.153.10/com.foo.BarService")));
    }

    @Test
    public void testParseRule() throws Exception {
        String rule = "host=10.20.153.10=>host=10.20.153.11";
        URL url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode(rule));
        ConditionRouter router = new ConditionRouter(url);
        assertNotNull(router);
    }

    private static class MockInvoker<T> implements Invoker<T> {
        private String url;

        public MockInvoker(String url) {
            this.url = url;
        }

        @Override
        public URL getUrl() {
            return URL.valueOf(url);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public com.alibaba.dubbo.rpc.Result invoke(Invocation invocation) throws RpcException {
            return null;
        }

        @Override
        public void destroy() {
        }

        @Override
        public Class<T> getInterface() {
            return null;
        }
    }
}
