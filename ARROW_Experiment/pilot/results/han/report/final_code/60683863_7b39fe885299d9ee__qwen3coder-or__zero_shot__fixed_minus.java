package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;

import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConditionRouter_ArrowTest {

    private URL url;
    private List<Invoker<String>> invokers;
    private Invocation invocation;

    @Before
    public void setUp() {
        url = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        invokers = new ArrayList<Invoker<String>>();
        invocation = new Invocation() {
            @Override
            public String getMethodName() {
                return "test";
            }

            @Override
            public Class<?>[] getParameterTypes() {
                return new Class[0];
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public Map<String, String> getAttachments() {
                return new HashMap<>();
            }

            @Override
            public String getAttachment(String key) {
                return null;
            }

            @Override
            public String getAttachment(String key, String defaultValue) {
                return null;
            }

            @Override
            public Invoker getInvoker() {
                return null;
            }
        };
    }

    @Test
    public void testConstructor() {
        ConditionRouter router = new ConditionRouter(url);
        assertNotNull(router);
        assertEquals(url, router.getUrl());
    }

    @Test
    public void testRouteWithEmptyInvokers() throws RpcException {
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<String>> result = router.route(new ArrayList<Invoker<String>>(), url, invocation);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testRouteWithNullInvokers() throws RpcException {
        ConditionRouter router = new ConditionRouter(url);
        List<Invoker<String>> result = router.route(null, url, invocation);
        assertNull(result);
    }

    @Test
    public void testRouteWhenConditionNotMatch() throws RpcException {
        URL urlWithoutMatch = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.12=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(urlWithoutMatch);
        Invoker<String> invoker = new Invoker<String>() {
            @Override
            public Class<String> getInterface() {
                return String.class;
            }

            @Override
            public URL getUrl() {
                return URL.valueOf("host=10.20.153.10");
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
        };
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, url, invocation);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testRouteThenConditionIsNull() throws RpcException {
        URL urlWithoutThen = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10"));
        ConditionRouter router = new ConditionRouter(urlWithoutThen);
        Invoker<String> invoker = new Invoker<String>() {
            @Override
            public Class<String> getInterface() {
                return String.class;
            }

            @Override
            public URL getUrl() {
                return URL.valueOf("host=10.20.153.10");
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
        };
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, url, invocation);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testRouteThenConditionMatch() throws RpcException {
        ConditionRouter router = new ConditionRouter(url);
        Invoker<String> invoker = new Invoker<String>() {
            @Override
            public Class<String> getInterface() {
                return String.class;
            }

            @Override
            public URL getUrl() {
                return URL.valueOf("host=10.20.153.11");
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
        };
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, url, invocation);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testRouteThenConditionNotMatch() throws RpcException {
        ConditionRouter router = new ConditionRouter(url);
        Invoker<String> invoker = new Invoker<String>() {
            @Override
            public Class<String> getInterface() {
                return String.class;
            }

            @Override
            public URL getUrl() {
                return URL.valueOf("host=10.20.153.12");
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
        };
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, url, invocation);
        assertNotNull(result);
        assertEquals(0, result.size());
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
        URL urlWithWhen = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(urlWithWhen);
        URL testUrl = URL.valueOf("host=10.20.153.10");
        assertTrue(router.matchWhen(testUrl));
    }

    @Test
    public void testMatchThen() {
        URL urlWithThen = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(urlWithThen);
        URL testUrl = URL.valueOf("host=10.20.153.11");
        URL paramUrl = URL.valueOf("host=10.20.153.10");
        assertTrue(router.matchThen(testUrl, paramUrl));
    }

    @Test
    public void testParseRule() throws Exception {
        String rule = "host=10.20.153.10";
        URL testUrl = URL.valueOf("host=10.20.153.10");
        ConditionRouter router = new ConditionRouter(url);
        assertTrue(router.matchWhen(testUrl));
    }
}
