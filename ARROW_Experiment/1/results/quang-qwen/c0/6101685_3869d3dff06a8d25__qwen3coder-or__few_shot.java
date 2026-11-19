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
        // Create a test object
        TestClass testObject = new TestClass();
        
        // Create an interceptor
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("intercepted");
        
        // Create proxy
        TestClass proxy = ProxyHelper.createProxyOf(testObject, interceptor);
        
        // Verify that the proxy works
        assertNotNull(proxy);
        assertEquals("intercepted", proxy.someMethod());
        
        // Verify that the interceptor was called
        verify(interceptor).intercept(any(Method.class), any(Object[].class));
    }

    @Test
    public void shouldCreateProxyOfClass() {
        // Create an interceptor
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("intercepted");
        
        // Create proxy
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);
        
        // Verify that the proxy works
        assertNotNull(proxy);
        assertEquals("intercepted", proxy.someMethod());
        
        // Verify that the interceptor was called
        verify(interceptor).intercept(any(Method.class), any(Object[].class));
    }

    @Test
    public void shouldHandleVarArgs() throws Exception {
        // Create an interceptor
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Object[] expectedArgs = new Object[]{"arg1", "arg2", "arg3"};
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("varargs_result");
        
        // Create proxy
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);
        
        // Call method with varargs
        String result = proxy.varArgsMethod("arg1", "arg2", "arg3");
        
        // Verify result
        assertEquals("varargs_result", result);
        
        // Verify that the interceptor was called with correct arguments
        verify(interceptor).intercept(any(Method.class), eq(expectedArgs));
    }

    @Test
    public void shouldHandleNoArgs() throws Exception {
        // Create an interceptor
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("no_args_result");
        
        // Create proxy
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);
        
        // Call method with no args
        String result = proxy.noArgsMethod();
        
        // Verify result
        assertEquals("no_args_result", result);
        
        // Verify that the interceptor was called with correct arguments
        verify(interceptor).intercept(any(Method.class), any(Object[].class));
    }

    @Test
    public void shouldHandleSingleArg() throws Exception {
        // Create an interceptor
        ProxyHelper.Interceptor interceptor = mock(ProxyHelper.Interceptor.class);
        Object[] expectedArgs = new Object[]{"single_arg"};
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("single_arg_result");
        
        // Create proxy
        TestClass proxy = ProxyHelper.createProxyOf(TestClass.class, interceptor);
        
        // Call method with single arg
        String result = proxy.singleArgMethod("single_arg");
        
        // Verify result
        assertEquals("single_arg_result", result);
        
        // Verify that the interceptor was called with correct arguments
        verify(interceptor).intercept(any(Method.class), eq(expectedArgs));
    }

    // Helper class for testing
    private static class TestClass {
        public String someMethod() {
            return "original";
        }
        
        public String noArgsMethod() {
            return "original_no_args";
        }
        
        public String singleArgMethod(String arg) {
            return "original_single_arg";
        }
        
        public String varArgsMethod(String... args) {
            return "original_varargs";
        }
    }
}
