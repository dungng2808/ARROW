package com.oracle.bedrock.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ProxyHelper_ArrowTest {

    private ProxyHelper.Interceptor interceptor;

    @Before
    public void setUp() {
        interceptor = mock(ProxyHelper.Interceptor.class);
    }

    @Test
    public void shouldCreateProxyOfObject() {
        // Given
        String originalString = "test";
        
        // When
        String proxy = ProxyHelper.createProxyOf(originalString, interceptor);
        
        // Then
        assertNotNull(proxy);
        assertEquals(String.class, proxy.getClass());
    }

    @Test
    public void shouldCreateProxyOfClass() {
        // Given
        Class<String> clazz = String.class;
        
        // When
        String proxy = ProxyHelper.createProxyOf(clazz, interceptor);
        
        // Then
        assertNotNull(proxy);
        assertEquals(String.class, proxy.getClass());
    }

    @Test
    public void shouldInterceptMethodCall() throws Throwable {
        // Given
        String proxy = ProxyHelper.createProxyOf(String.class, interceptor);
        Method method = String.class.getMethod("toString");
        Object[] args = new Object[0];
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("intercepted");
        
        // When
        String result = proxy.toString();
        
        // Then
        verify(interceptor).intercept(eq(method), eq(args));
        assertEquals("intercepted", result);
    }

    @Test
    public void shouldHandleVarArgs() throws Throwable {
        // Given
        Class<TestInterface> clazz = TestInterface.class;
        TestInterface proxy = ProxyHelper.createProxyOf(clazz, interceptor);
        
        Method method = TestInterface.class.getMethod("varArgsMethod", String[].class);
        Object[] args = new Object[]{"arg1", "arg2"};
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("varargs result");
        
        // When
        String result = proxy.varArgsMethod("arg1", "arg2");
        
        // Then
        verify(interceptor).intercept(eq(method), any(Object[].class));
        assertEquals("varargs result", result);
    }

    @Test
    public void shouldHandleEmptyVarArgs() throws Throwable {
        // Given
        Class<TestInterface> clazz = TestInterface.class;
        TestInterface proxy = ProxyHelper.createProxyOf(clazz, interceptor);
        
        Method method = TestInterface.class.getMethod("varArgsMethod", String[].class);
        Object[] args = new Object[0];
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("empty varargs result");
        
        // When
        String result = proxy.varArgsMethod();
        
        // Then
        verify(interceptor).intercept(eq(method), any(Object[].class));
        assertEquals("empty varargs result", result);
    }

    @Test
    public void shouldHandleNormalMethodWithoutArgs() throws Throwable {
        // Given
        Class<TestInterface> clazz = TestInterface.class;
        TestInterface proxy = ProxyHelper.createProxyOf(clazz, interceptor);
        
        Method method = TestInterface.class.getMethod("normalMethod");
        Object[] args = new Object[0];
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("normal result");
        
        // When
        String result = proxy.normalMethod();
        
        // Then
        verify(interceptor).intercept(eq(method), eq(args));
        assertEquals("normal result", result);
    }

    @Test
    public void shouldHandleMethodWithArgs() throws Throwable {
        // Given
        Class<TestInterface> clazz = TestInterface.class;
        TestInterface proxy = ProxyHelper.createProxyOf(clazz, interceptor);
        
        Method method = TestInterface.class.getMethod("methodWithArgs", String.class, int.class);
        Object[] args = new Object[]{"test", 42};
        
        when(interceptor.intercept(any(Method.class), any(Object[].class)))
            .thenReturn("args result");
        
        // When
        String result = proxy.methodWithArgs("test", 42);
        
        // Then
        verify(interceptor).intercept(eq(method), eq(args));
        assertEquals("args result", result);
    }

    interface TestInterface {
        String normalMethod();
        String methodWithArgs(String arg1, int arg2);
        String varArgsMethod(String... args);
    }
}
