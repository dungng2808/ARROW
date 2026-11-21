package com.oracle.bedrock.util;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProxyHelper_ArrowTest {

    @Test
    public void shouldCreateProxyOfObject() {
        TestClass testObject = new TestClass();
        TestInterceptor interceptor = mock(TestInterceptor.class);
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("intercepted result");

        ProxyHelper.Interceptor proxyInterceptor = new ProxyHelper.Interceptor() {
            @Override
            public Object intercept(Method method, Object[] args) throws Throwable {
                return interceptor.intercept(method, args);
            }
        };

        TestClass proxy = ProxyHelper.createProxyOf(testObject, proxyInterceptor);
        
        assertNotNull(proxy);
        assertEquals("intercepted result", proxy.someMethod());
    }

    @Test
    public void shouldCreateProxyOfClass() {
        TestInterceptor interceptor = mock(TestInterceptor.class);
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("intercepted result");

        ProxyHelper.Interceptor proxyInterceptor = new ProxyHelper.Interceptor() {
            @Override
            public Object intercept(Method method, Object[] args) throws Throwable {
                return interceptor.intercept(method, args);
            }
        };

        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, proxyInterceptor);
        
        assertNotNull(proxy);
        assertEquals("intercepted result", proxy.someMethod());
    }

    @Test
    public void shouldHandleVarArgs() throws Exception {
        TestClass testObject = new TestClass();
        TestInterceptor interceptor = mock(TestInterceptor.class);
        
        Object[] varArgs = {"arg1", "arg2", "arg3"};
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments()[1];
                    assertEquals(3, args.length);
                    assertEquals("arg1", args[0]);
                    assertEquals("arg2", args[1]);
                    assertEquals("arg3", args[2]);
                    return "varargs result";
                }
            });

        ProxyHelper.Interceptor proxyInterceptor = new ProxyHelper.Interceptor() {
            @Override
            public Object intercept(Method method, Object[] args) throws Throwable {
                return interceptor.intercept(method, args);
            }
        };

        TestClass proxy = ProxyHelper.createProxyOf(testObject, proxyInterceptor);
        assertEquals("varargs result", proxy.varArgsMethod("arg1", "arg2", "arg3"));
    }

    @Test
    public void shouldHandleNoArgs() throws Exception {
        TestClass testObject = new TestClass();
        TestInterceptor interceptor = mock(TestInterceptor.class);
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("no args result");

        ProxyHelper.Interceptor proxyInterceptor = new ProxyHelper.Interceptor() {
            @Override
            public Object intercept(Method method, Object[] args) throws Throwable {
                return interceptor.intercept(method, args);
            }
        };

        TestClass proxy = ProxyHelper.createProxyOf(testObject, proxyInterceptor);
        assertEquals("no args result", proxy.noArgsMethod());
    }

    @Test
    public void shouldHandleSingleArg() throws Exception {
        TestClass testObject = new TestClass();
        TestInterceptor interceptor = mock(TestInterceptor.class);
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Object[] args = invocation.getArguments()[1];
                    assertEquals(1, args.length);
                    assertEquals("single arg", args[0]);
                    return "single arg result";
                }
            });

        ProxyHelper.Interceptor proxyInterceptor = new ProxyHelper.Interceptor() {
            @Override
            public Object intercept(Method method, Object[] args) throws Throwable {
                return interceptor.intercept(method, args);
            }
        };

        TestClass proxy = ProxyHelper.createProxyOf(testObject, proxyInterceptor);
        assertEquals("single arg result", proxy.singleArgMethod("single arg"));
    }

    // Helper classes for testing
    private static class TestClass {
        public String someMethod() {
            return "original result";
        }

        public String noArgsMethod() {
            return "no args original";
        }

        public String singleArgMethod(String arg) {
            return "single arg original";
        }

        public String varArgsMethod(String... args) {
            return "var args original";
        }
    }

    private interface TestInterceptor extends ProxyHelper.Interceptor {
    }
}
