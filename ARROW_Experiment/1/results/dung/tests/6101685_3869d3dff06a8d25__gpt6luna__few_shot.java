package com.oracle.bedrock.util;

import org.junit.Test;

import java.lang.reflect.Method;

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
    public void createProxyOfObjectRoutesMethodCallToInterceptor()
    {
        final Method[] interceptedMethod = new Method[1];
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(new Sample()
        {
            @Override
            public String greet(String name)
            {
                return name;
            }

            @Override
            public String combine(String prefix, String... values)
            {
                return prefix;
            }
        }, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedMethod[0] = method;
                interceptedArguments[0] = args;
                return "hello " + args[0];
            }
        });

        assertEquals("hello world", proxy.greet("world"));
        assertNotNull(interceptedMethod[0]);
        assertEquals("greet", interceptedMethod[0].getName());
        assertEquals(1, interceptedArguments[0].length);
        assertEquals("world", interceptedArguments[0][0]);
    }

    @Test
    public void createProxyOfClassPassesVarargsAsArray()
    {
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(Sample.class, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedArguments[0] = args;
                return args[0] + ":" + ((String[]) args[1]).length;
            }
        });

        assertEquals("items:2", proxy.combine("items", "one", "two"));
        assertEquals(2, interceptedArguments[0].length);
        assertEquals("items", interceptedArguments[0][0]);
        assertEquals(2, ((String[]) interceptedArguments[0][1]).length);
        assertEquals("one", ((String[]) interceptedArguments[0][1])[0]);
        assertEquals("two", ((String[]) interceptedArguments[0][1])[1]);
    }

    @Test
    public void createProxyOfClassSupportsEmptyVarargs()
    {
        final Object[][] interceptedArguments = new Object[1][];

        Sample proxy = ProxyHelper.createProxyOf(Sample.class, new ProxyHelper.Interceptor()
        {
            @Override
            public Object intercept(Method method, Object[] args)
            {
                interceptedArguments[0] = args;
                return args[0] + ":" + ((String[]) args[1]).length;
            }
        });

        assertEquals("items:0", proxy.combine("items"));
        assertEquals(0, ((String[]) interceptedArguments[0][1]).length);
    }
}
