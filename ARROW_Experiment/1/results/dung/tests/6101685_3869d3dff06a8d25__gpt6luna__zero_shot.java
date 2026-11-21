package com.oracle.bedrock.util;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ProxyHelper_ArrowTest
{
    public interface Sample
    {
        String greet(String name);

        String combine(String prefix, String... values);
    }

    @Test
    public void createProxyOfObjectRoutesMethodToInterceptor()
    {
        final Method[] interceptedMethod = new Method[1];
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(new Sample()
        {
            @Override
            public String greet(String name)
            {
                return "original";
            }

            @Override
            public String combine(String prefix, String... values)
            {
                return "original";
            }
        }, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedMethod[0] = method;
                interceptedArguments[0] = args;
                return "intercepted";
            }
        });

        assertEquals("intercepted", proxy.greet("Ada"));
        assertEquals("greet", interceptedMethod[0].getName());
        assertArrayEquals(new Object[]{"Ada"}, interceptedArguments[0]);
    }

    @Test
    public void createProxyOfClassRoutesMethodToInterceptor()
    {
        final Method[] interceptedMethod = new Method[1];
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(Sample.class, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedMethod[0] = method;
                interceptedArguments[0] = args;
                return "class proxy";
            }
        });

        assertEquals("class proxy", proxy.greet("Grace"));
        assertEquals("greet", interceptedMethod[0].getName());
        assertArrayEquals(new Object[]{"Grace"}, interceptedArguments[0]);
    }

    @Test
    public void varargsArePassedAsArrayAfterFixedArguments()
    {
        final Method[] interceptedMethod = new Method[1];
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(Sample.class, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedMethod[0] = method;
                interceptedArguments[0] = args;
                return "combined";
            }
        });

        assertEquals("combined", proxy.combine("start", "one", "two"));
        assertEquals("combine", interceptedMethod[0].getName());
        assertEquals(2, interceptedArguments[0].length);
        assertEquals("start", interceptedArguments[0][0]);
        assertArrayEquals(new Object[]{"one", "two"}, (Object[]) interceptedArguments[0][1]);
    }

    @Test
    public void varargsCanBeEmpty()
    {
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(Sample.class, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedArguments[0] = args;
                return "empty";
            }
        });

        assertEquals("empty", proxy.combine("start"));
        assertEquals(2, interceptedArguments[0].length);
        assertEquals("start", interceptedArguments[0][0]);
        assertArrayEquals(new Object[0], (Object[]) interceptedArguments[0][1]);
    }
}
