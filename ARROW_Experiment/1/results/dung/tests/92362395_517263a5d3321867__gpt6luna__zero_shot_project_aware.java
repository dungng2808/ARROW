package com.alibaba.dubbo.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class URL_ArrowTest {

    @Test
    public void parsesUrlAndParameters() {
        URL url = URL.valueOf("  dubbo://user:pass@localhost:20880/service?b=2&a=1  ");

        assertEquals("dubbo", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("localhost", url.getHost());
        assertEquals(20880, url.getPort());
        assertEquals("service", url.getPath());
        assertEquals("1", url.getParameter("a"));
        assertEquals("2", url.getParameter("b"));
    }

    @Test
    public void constructorNormalizesPortAndPath() {
        URL url = new URL("http", "host", -1, "///service");

        assertEquals(0, url.getPort());
        assertEquals("service", url.getPath());
        assertEquals("/service", url.getAbsolutePath());
        assertEquals("host", url.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPasswordWithoutUsername() {
        new URL("http", null, "secret", "host", 80, null);
    }

    @Test
    public void parameterDefaultsAndTypedValues() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("count", "12");
        parameters.put("enabled", "true");
        parameters.put("letter", "xyz");
        parameters.put("empty", "");
        URL url = new URL("http", "host", 80, parameters);

        assertEquals(12, url.getParameter("count", 0));
        assertTrue(url.getParameter("enabled", false));
        assertEquals('x', url.getParameter("letter", 'z'));
        assertEquals("fallback", url.getParameter("missing", "fallback"));
        assertEquals("fallback", url.getParameter("empty", "fallback"));
        assertFalse(url.hasParameter("empty"));
    }

    @Test
    public void methodParametersFallBackToGeneralParameter() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("timeout", "30");
        parameters.put("read.timeout", "45");
        URL url = new URL("http", "host", 80, parameters);

        assertEquals("45", url.getMethodParameter("read", "timeout"));
        assertEquals(30, url.getMethodParameter("write", "timeout", 0));
        assertTrue(url.hasMethodParameter("read", "timeout"));
        assertTrue(url.hasMethodParameter(null, "timeout"));
        assertTrue(url.hasMethodParameter("read", null));
    }

    @Test
    public void parameterUpdatesReturnNewUrlAndPreserveOriginal() {
        URL original = new URL("http", "host", 80);
        URL updated = original.addParameter("key", "value");

        assertNull(original.getParameter("key"));
        assertEquals("value", updated.getParameter("key"));
        assertNotSame(original, updated);
        assertSame(updated, updated.addParameter("key", "value"));
    }

    @Test
    public void addParameterIfAbsentKeepsExistingValue() {
        URL url = new URL("http", "host", 80).addParameter("key", "first")
                .addParameterIfAbsent("key", "second");

        assertEquals("first", url.getParameter("key"));
    }

    @Test
    public void removesAndClearsParameters() {
        URL url = new URL("http", "host", 80)
                .addParameters("a", "1", "b", "2");

        URL removed = url.removeParameter("a");
        assertNull(removed.getParameter("a"));
        assertEquals("2", removed.getParameter("b"));

        URL cleared = url.clearParameters();
        assertTrue(cleared.getParameters().isEmpty());
        assertEquals("1", url.getParameter("a"));
    }

    @Test
    public void rendersStringsInSortedParameterOrder() {
        URL url = new URL("http", "host", 80, "service")
                .addParameter("z", "last")
                .addParameter("a", "first");

        assertEquals("http://host:80/service?a=first&z=last", url.toString());
        assertEquals("http://host:80/service?z=last", url.toString("z"));
        assertEquals("a=first&z=last", url.toParameterString());
    }

    @Test
    public void serviceKeyIncludesGroupAndVersion() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("interface", "example.Service");
        parameters.put("group", "blue");
        parameters.put("version", "1.2");
        URL url = new URL("dubbo", "host", 20880, "fallback", parameters);

        assertEquals("blue/example.Service:1.2", url.getServiceKey());
        assertEquals("dubbo://host:20880/blue/example.Service:1.2", url.toServiceString());
    }

    @Test
    public void encodesAndDecodesValues() {
        assertEquals("a+b%2Fc", URL.encode("a b/c"));
        assertEquals("a b/c", URL.decode("a+b%2Fc"));
        assertEquals("", URL.decode(null));
    }

    @Test
    public void parsesFileStyleUrl() {
        URL url = URL.valueOf("file:/D:/1/router.js?type=script");

        assertEquals("file", url.getProtocol());
        assertNull(url.getHost());
        assertEquals("D:/1/router.js", url.getPath());
        assertEquals("script", url.getParameter("type"));
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsUrlWithoutProtocolBeforeSchemeSeparator() {
        URL.valueOf("://host/path");
    }

    @Test
    public void positiveParameterUsesDefaultForNonPositiveValue() {
        URL url = new URL("http", "host", 80).addParameter("limit", "-2");

        assertEquals(10, url.getPositiveParameter("limit", 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void positiveParameterRejectsNonPositiveDefault() {
        new URL("http", "host", 80).getPositiveParameter("limit", 0);
    }
}
