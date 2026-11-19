package com.oracle.bedrock.util;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProxyHelper_ArrowTest {

    @Test
    public void shouldCreateProxyOfObject() {
        // Given
        TestClass testObject = new TestClass();
        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.intercept(testObject.getTestMethod(), new Object[]{"test"}))
                .thenReturn("intercepted");

        // When
        TestClass proxy = ProxyHelper.createProxyOf(testObject, interceptor);

        // Then
        assertEquals("intercepted", proxy.testMethod("test"));
    }

    @Test
    public void shouldCreateProxyOfClass() {
        // Given
        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.intercept(TestClass.getTestMethod(), new Object[]{"test"}))
                .thenReturn("intercepted");

        // When
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // Then
        assertEquals("intercepted", proxy.testMethod("test"));
    }

    @Test
    public void shouldHandleVarArgs() throws Throwable {
        // Given
        Interceptor interceptor = mock(Interceptor.class);
        Object[] varArgs = new Object[]{"arg1", "arg2"};
        when(interceptor.intercept(TestClass.getVarArgsMethod(), new Object[]{"test", varArgs}))
                .thenReturn("intercepted");

        // When
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // Then
        assertEquals("intercepted", proxy.varArgsMethod("test", "arg1", "arg2"));
    }

    @Test
    public void shouldReturnNullWhenInterceptReturnsNull() {
        // Given
        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.intercept(TestClass.getTestMethod(), new Object[]{"test"}))
                .thenReturn(null);

        // When
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // Then
        assertEquals(null, proxy.testMethod("test"));
    }

    @Test
    public void shouldHandleNoArgsMethod() {
        // Given
        Interceptor interceptor = mock(Interceptor.class);
        when(interceptor.intercept(TestClass.getNoArgsMethod(), new Object[0]))
                .thenReturn("noArgsResult");

        // When
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);

        // Then
        assertEquals("noArgsResult", proxy.noArgsMethod());
    }

    public static class TestClass {
        public String testMethod(String arg) {
            return "original:" + arg;
        }

        public String varArgsMethod(String arg, String... varArgs) {
            return "original:" + arg;
        }

        public String noArgsMethod() {
            return "original";
        }

        public static Method getTestMethod() {
            try {
                return TestClass.class.getMethod("testMethod", String.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public static Method getVarArgsMethod() {
            try {
                return TestClass.class.getMethod("varArgsMethod", String.class, String[].class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public static Method getNoArgsMethod() {
            try {
                return TestClass.class.getMethod("noArgsMethod");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
