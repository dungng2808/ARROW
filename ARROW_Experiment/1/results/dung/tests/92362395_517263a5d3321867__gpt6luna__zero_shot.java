package com.alibaba.dubbo.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class URL_ArrowTest {

    @Test
    public void parsesUrlAndBuildsStrings() {
        URL url = URL.valueOf("dubbo://user:pass@example.com:20880/service?b=2&a=1");

        assertEquals("dubbo", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("example.com", url.getHost());
        assertEquals(20880, url.getPort());
        assertEquals("service", url.getPath());
        assertEquals("user:pass", url.getAuthority());
        assertEquals("dubbo://example.com:20880/service?a=1&b=2", url.toString());
        assertEquals("dubbo://user:pass@example.com:20880/service?a=1&b=2", url.toFullString());
    }

    @Test
    public void constructorNormalizesPathAndPort() {
        URL url = new URL("http", "host", -1, "///path");

        assertEquals(0, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals("/path", url.getAbsolutePath());
        assertEquals("host", url.getAddress());
    }

    @Test
    public void parsesParameterWithoutEqualsAndTrimsInput() {
        URL url = URL.valueOf("  host/path?flag&name=value  ");

        assertEquals("host", url.getHost());
        assertEquals("path", url.getPath());
        assertEquals("flag", url.getParameter("flag"));
        assertEquals("value", url.getParameter("name"));
    }

    @Test
    public void parameterDefaultsAndTypedValues() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("count", "12");
        parameters.put("enabled", "true");
        parameters.put("letter", "xyz");
        parameters.put("empty", "");
        URL url = new URL("p", "host", 1, parameters);

        assertEquals(12, url.getParameter("count", 0));
        assertTrue(url.getParameter("enabled", false));
        assertEquals('x', url.getParameter("letter", 'z'));
        assertEquals("fallback", url.getParameter("empty", "fallback"));
        assertEquals(7, url.getParameter("missing", 7));
    }

    @Test
    public void defaultPrefixedParameterIsUsed() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("default.timeout", "30");
        URL url = new URL("p", "host", 1, parameters);

        assertEquals("30", url.getParameter("timeout"));
        assertEquals(30, url.getParameter("timeout", 0));
    }

    @Test
    public void methodParametersFallBackToGeneralParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("timeout", "10");
        parameters.put("read.timeout", "20");
        URL url = new URL("p", "host", 1, parameters);

        assertEquals("20", url.getMethodParameter("read", "timeout"));
        assertEquals(20, url.getMethodParameter("read", "timeout", 0));
        assertEquals("10", url.getMethodParameter("write", "timeout"));
        assertTrue(url.hasMethodParameter("read", "timeout"));
        assertTrue(url.hasMethodParameter(null, "timeout"));
        assertTrue(url.hasMethodParameter("read", null));
    }

    @Test
    public void positiveParameterUsesDefaultForNonPositiveValue() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("limit", "-2");
        URL url = new URL("p", "host", 1, parameters);

        assertEquals(5, url.getPositiveParameter("limit", 5));
    }

    @Test
    public void backupAddressesUseDefaultPort() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("backup", "other:0,third:9");
        URL url = new URL("p", "host", 0, parameters);

        assertEquals("host:7,other:7,third:9", url.getBackupAddress(7));
        assertEquals(3, url.getBackupUrls().size());
        assertEquals("other", url.getBackupUrls().get(1).getHost());
        assertEquals(0, url.getBackupUrls().get(1).getPort());
    }

    @Test
    public void parameterUpdatesReturnNewUrlAndPreserveOriginal() {
        URL original = new URL("p", "host", 1);
        URL updated = original.addParameter("key", "value");

        assertEquals("value", updated.getParameter("key"));
        assertNull(original.getParameter("key"));
        assertNotSame(original, updated);
        assertSame(updated, updated.addParameter("key", "value"));
    }

    @Test
    public void addParameterIfAbsentKeepsExistingValue() {
        URL url = new URL("p", "host", 1).addParameter("key", "first");

        assertEquals("first", url.addParameterIfAbsent("key", "second").getParameter("key"));
    }

    @Test
    public void removesAndClearsParameters() {
        URL url = new URL("p", "host", 1)
                .addParameter("a", "1")
                .addParameter("b", "2");

        URL removed = url.removeParameter("a");
        assertNull(removed.getParameter("a"));
        assertEquals("2", removed.getParameter("b"));
        assertEquals(0, url.clearParameters().getParameters().size());
    }

    @Test
    public void parameterAndMapStringsAreSortedAndIncludeAddressFields() {
        URL url = new URL("p", "host", 9, "service")
                .addParameter("z", "last")
                .addParameter("a", "first");

        assertEquals("a=first&z=last", url.toParameterString());
        assertEquals("p://host:9/service?a=first&z=last", url.toString());
        assertEquals("service", url.toMap().get("path"));
        assertEquals("9", url.toMap().get("port"));
    }

    @Test
    public void serviceKeyIncludesGroupAndVersion() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("interface", "example.Service");
        parameters.put("group", "blue");
        parameters.put("version", "1.2");
        URL url = new URL("p", "host", 1, "fallback", parameters);

        assertEquals("blue/example.Service:1.2", url.getServiceKey());
        assertEquals("example.Service", url.getServiceInterface());
    }

    @Test
    public void encodingAndDecodingRoundTrip() {
        String encoded = URL.encode("a b+c");
        assertEquals("a+b%2Bc", encoded);
        assertEquals("a b+c", URL.decode(encoded));
        assertEquals("", URL.decode(null));
    }

    @Test
    public void equalityAndHashCodeUseUrlFields() {
        URL first = new URL("p", "host", 9, "path").addParameter("k", "v");
        URL second = new URL("p", "host", 9, "path").addParameter("k", "v");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new URL("p", "host", 10, "path").addParameter("k", "v"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPasswordWithoutUsername() {
        new URL("p", null, "secret", "host", 1, "path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOddNumberOfParameterPairs() {
        new URL("p", "host", 1).addParameters("key");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullUrlString() {
        URL.valueOf(null);
    }
}
