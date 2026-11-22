package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;

import org.junit.Test;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

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
        invocation = org.easymock.EasyMock.createMock(Invocation.class);
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
        Invoker<String> invoker = org.easymock.EasyMock.createMock(Invoker.class);
        org.easymock.EasyMock.expect(invoker.getUrl()).andReturn(URL.valueOf("host=10.20.153.10"));
        org.easymock.EasyMock.replay(invoker);
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
        Invoker<String> invoker = org.easymock.EasyMock.createMock(Invoker.class);
        org.easymock.EasyMock.expect(invoker.getUrl()).andReturn(URL.valueOf("host=10.20.153.10"));
        org.easymock.EasyMock.replay(invoker);
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, url, invocation);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testRouteThenConditionMatch() throws RpcException {
        ConditionRouter router = new ConditionRouter(url);
        Invoker<String> invoker = org.easymock.EasyMock.createMock(Invoker.class);
        org.easymock.EasyMock.expect(invoker.getUrl()).andReturn(URL.valueOf("host=10.20.153.11"));
        org.easymock.EasyMock.replay(invoker);
        invokers.add(invoker);
        List<Invoker<String>> result = router.route(invokers, url, invocation);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testRouteThenConditionNotMatch() throws RpcException {
        URL urlWithRule = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10=>host=10.20.153.11"));
        ConditionRouter router = new ConditionRouter(urlWithRule);
        Invoker<String> invoker = org.easymock.EasyMock.createMock(Invoker.class);
        org.easymock.EasyMock.expect(invoker.getUrl()).andReturn(URL.valueOf("host=10.20.153.12"));
        org.easymock.EasyMock.replay(invoker);
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
        assertEquals(-1, router1.compareTo(router2));
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
        URL urlWithRule = URL.valueOf("condition://0.0.0.0/com.foo.BarService?category=routers&dynamic=false&rule=" +
                URL.encode("host=10.20.153.10"));
        ConditionRouter router = new ConditionRouter(urlWithRule);
        URL testUrl = URL.valueOf("host=10.20.153.10");
        assertTrue(router.matchWhen(testUrl));
    }
}
