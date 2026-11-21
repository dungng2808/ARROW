package com.oracle.bedrock.util;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProxyHelper_ArrowTest {

    @Test
    public void shouldCreateProxyOfObject() {
        // Given
        TestClass original = new TestClass();
        TestInterceptor interceptor = new TestInterceptor();

        // When
        TestClass proxy = ProxyHelper.createProxyOf(original, interceptor);

        // Then
        assertNotNull(proxy);
        assertNotSame(original, proxy);
    }

    @Test
    public void shouldCreateProxyOfClass() {
        // Given
        TestInterceptor interceptor = new TestInterceptor();

        // When
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // Then
        assertNotNull(proxy);
    }

    @Test
    public void shouldInterceptMethodCall() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor();
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When
        String result = proxy.someMethod("test");

        // Then
        assertEquals("test", result);
        assertTrue(interceptor.wasCalled);
    }

    @Test
    public void shouldHandleVarArgs() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor();
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When
        String result = proxy.varArgsMethod("a", "b", "c");

        // Then
        assertEquals("abc", result);
        assertTrue(interceptor.wasCalled);
    }

    @Test
    public void shouldHandleNoArgs() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor();
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When
        String result = proxy.noArgsMethod();

        // Then
        assertEquals("no args", result);
        assertTrue(interceptor.wasCalled);
    }

    @Test
    public void shouldHandleNullArgs() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor();
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When
        String result = proxy.nullArgsMethod(null);

        // Then
        assertEquals("null args", result);
        assertTrue(interceptor.wasCalled);
    }

    @Test
    public void shouldHandlePrimitiveArgs() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor();
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When
        int result = proxy.primitiveArgsMethod(5, 10);

        // Then
        assertEquals(15, result);
        assertTrue(interceptor.wasCalled);
    }

    @Test
    public void shouldHandleArrayArgs() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor();
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When
        int result = proxy.arrayArgsMethod(new int[]{1, 2, 3});

        // Then
        assertEquals(6, result);
        assertTrue(interceptor.wasCalled);
    }

    @Test
    public void shouldHandleExceptionFromInterceptor() throws Exception {
        // Given
        TestInterceptor interceptor = new TestInterceptor() {
            @Override
            public Object intercept(Method method, Object[] args) throws Throwable {
                throw new RuntimeException("Test exception");
            }
        };
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // When & Then
        try {
            proxy.someMethod("test");
            fail("Expected exception was not thrown");
        } catch (RuntimeException e) {
            assertEquals("Test exception", e.getMessage());
        }
    }

    private static class TestClass {
        public String someMethod(String arg) {
            return arg;
        }

        public String varArgsMethod(String... args) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg);
            }
            return sb.toString();
        }

        public String noArgsMethod() {
            return "no args";
        }

        public String nullArgsMethod(String arg) {
            return "null args";
        }

        public int primitiveArgsMethod(int a, int b) {
            return a + b;
        }

        public int arrayArgsMethod(int[] arr) {
            int sum = 0;
            for (int val : arr) {
                sum += val;
            }
            return sum;
        }
    }

    private static class TestInterceptor implements ProxyHelper.Interceptor {
        boolean wasCalled = false;

        @Override
        public Object intercept(Method method, Object[] args) throws Throwable {
            wasCalled = true;
            if (method.getName().equals("someMethod")) {
                return args[0];
            } else if (method.getName().equals("varArgsMethod")) {
                StringBuilder sb = new StringBuilder();
                for (Object arg : args) {
                    sb.append(arg);
                }
                return sb.toString();
            } else if (method.getName().equals("noArgsMethod")) {
                return "no args";
            } else if (method.getName().equals("nullArgsMethod")) {
                return "null args";
            } else if (method.getName().equals("primitiveArgsMethod")) {
                return (Integer) args[0] + (Integer) args[1];
            } else if (method.getName().equals("arrayArgsMethod")) {
                int[] arr = (int[]) args[0];
                int sum = 0;
                for (int val : arr) {
                    sum += val;
                }
                return sum;
            }
            return null;
        }
    }
}
