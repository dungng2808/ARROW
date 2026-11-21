package com.alibaba.dubbo.common;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class URL_ArrowTest {

    @Test
    public void parsesUrlAndBuildsStrings() {
        URL url = URL.valueOf("http://user:pass@example.com:8080/service?b=2&a=1");

        assertEquals("http", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("user:pass", url.getAuthority());
        assertEquals("example.com", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("service", url.getPath());
        assertEquals("http://example.com:8080/service?a=1&b=2", url.toString());
        assertEquals("http://user:pass@example.com:8080/service?a=1&b=2", url.toFullString());
        assertEquals("a=1&b=2", url.toParameterString());
    }

    @Test
    public void constructorNormalizesPathAndPort() {
        URL url = new URL("http", "example.com", -1, "//service");

        assertEquals(0, url.getPort());
        assertEquals("example.com", url.getAddress());
        assertEquals("service", url.getPath());
        assertEquals("/service", url.getAbsolutePath());
        assertEquals("http://example.com/service", url.toString());
    }

    @Test
    public void parameterDefaultsAndTypedValues() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("count", "12");
        parameters.put("enabled", "true");
        parameters.put("letter", "xyz");
        parameters.put("empty", "");
        parameters.put("default.timeout", "30");
        URL url = new URL("test", "host", 1, parameters);

        assertEquals(12, url.getParameter("count", 0));
        assertTrue(url.getParameter("enabled", false));
        assertEquals('x', url.getParameter("letter", 'z'));
        assertEquals("fallback", url.getParameter("missing", "fallback"));
        assertEquals("30", url.getParameter("timeout"));
        assertEquals("fallback", url.getParameter("empty", "fallback"));
        assertTrue(url.hasParameter("count"));
        assertFalse(url.hasParameter("empty"));
    }

    @Test
    public void methodParametersFallBackToGeneralParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("timeout", "15");
        parameters.put("read.timeout", "20");
        URL url = new URL("test", "host", 1, parameters);

        assertEquals("20", url.getMethodParameter("read", "timeout"));
        assertEquals(20, url.getMethodParameter("read", "timeout", 0));
        assertEquals("15", url.getMethodParameter("write", "timeout"));
        assertTrue(url.hasMethodParameter("read", "timeout"));
        assertTrue(url.hasMethodParameter(null, "timeout"));
        assertTrue(url.hasMethodParameter("read", null));
    }

    @Test
    public void parameterUpdatesReturnNewUrlsWithoutChangingOriginal() {
        URL original = new URL("test", "host", 1);
        URL updated = original.addParameter("key", "value");

        assertEquals(null, original.getParameter("key"));
        assertEquals("value", updated.getParameter("key"));
        assertNotSame(original, updated);
        assertSame(original, original.addParameter("key", ""));
        assertEquals("value", updated.addParameterIfAbsent("key", "other").getParameter("key"));
    }

    @Test
    public void removesAndClearsParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("a", "1");
        parameters.put("b", "2");
        URL url = new URL("test", "host", 1, parameters);

        URL removed = url.removeParameter("a");
        assertEquals(null, removed.getParameter("a"));
        assertEquals("2", removed.getParameter("b"));
        assertEquals("1", url.getParameter("a"));
        assertTrue(removed.clearParameters().getParameters().isEmpty());
    }

    @Test
    public void backupAddressesUseDefaultPort() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("backup", "backup.example,other.example:9");
        URL url = new URL("test", "primary.example", 0, parameters);

        assertEquals("primary.example:7,backup.example:7,other.example:9", url.getBackupAddress(7));
        assertEquals(3, url.getBackupUrls().size());
        assertEquals("backup.example", url.getBackupUrls().get(1).getHost());
    }

    @Test
    public void serviceKeyUsesInterfaceGroupAndVersion() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("interface", "example.Service");
        parameters.put("group", "blue");
        parameters.put("version", "1.2");
        URL url = new URL("dubbo", "host", 20880, "ignored", parameters);

        assertEquals("blue/example.Service:1.2", url.getServiceKey());
        assertEquals("example.Service", url.getServiceInterface());
        assertEquals("dubbo://host:20880/blue/example.Service:1.2", url.toServiceString());
    }

    @Test
    public void encodingAndDecodingRoundTrip() {
        String encoded = URL.encode("hello world");
        assertEquals("hello+world", encoded);
        assertEquals("hello world", URL.decode(encoded));
        assertEquals("", URL.decode(null));
    }

    @Test
    public void equalityAndHashCodeUseUrlFieldsAndParameters() {
        URL first = URL.valueOf("test://host:10/path?a=1");
        URL second = URL.valueOf("test://host:10/path?a=1");
        URL different = URL.valueOf("test://host:10/path?a=2");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(different));
    }

    @Test
    public void urlParameterIsParsed() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("nested", "http://other:80/path");
        URL url = new URL("test", "host", 1, parameters);

        URL nested = url.getUrlParameter("nested");
        assertEquals("http", nested.getProtocol());
        assertEquals("other", nested.getHost());
        assertEquals(80, nested.getPort());
        assertEquals("path", nested.getPath());
    }

    @Test
    public void positiveParameterUsesDefaultForNonPositiveValue() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("count", "-2");
        URL url = new URL("test", "host", 1, parameters);

        assertEquals(5, url.getPositiveParameter("count", 5));
        assertEquals(5, url.getPositiveParameter("missing", 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPasswordWithoutUsername() {
        new URL("test", null, "secret", "host", 1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveDefaultForPositiveParameter() {
        new URL("test", "host", 1).getPositiveParameter("count", 0);
    }
}
