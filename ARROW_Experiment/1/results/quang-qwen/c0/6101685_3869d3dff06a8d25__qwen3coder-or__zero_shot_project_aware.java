package com.oracle.bedrock.util;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.lang.reflect.Method;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProxyHelper_ArrowTest
{
    @Mock
    private ProxyHelper.Interceptor interceptor;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateProxyOfObject() throws Exception
    {
        String originalString = "test";
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("proxy");

        String proxy = ProxyHelper.createProxyOf(originalString, interceptor);

        assertNotNull(proxy);
        verify(interceptor).intercept(any(Method.class), any(Object[].class));
    }

    @Test
    public void shouldCreateProxyOfClass() throws Exception
    {
        when(interceptor.intercept(any(Method.class), any(Object[].class))).thenReturn("proxy");

        String proxy = ProxyHelper.createProxyOf(String.class, interceptor);

        assertNotNull(proxy);
        verify(interceptor).intercept(any(Method.class), any(Object[].class));
    }

    @Test
    public void shouldHandleMethodWithVarArgs() throws Exception
    {
        Object[] args = new Object[]{"arg1", "arg2", "arg3"};
        Method method = String.class.getMethod("format", String.class, Object[].class);
        when(interceptor.intercept(eq(method), any(Object[].class))).thenReturn("formatted");

        Object result = interceptor.intercept(method, args);

        assertNotNull(result);
        verify(interceptor).intercept(eq(method), any(Object[].class));
    }

    @Test
    public void shouldHandleMethodWithoutVarArgs() throws Exception
    {
        Object[] args = new Object[]{"arg1", "arg2"};
        Method method = String.class.getMethod("substring", int.class, int.class);
        when(interceptor.intercept(eq(method), any(Object[].class))).thenReturn("sub");

        Object result = interceptor.intercept(method, args);

        assertNotNull(result);
        verify(interceptor).intercept(eq(method), any(Object[].class));
    }
}
