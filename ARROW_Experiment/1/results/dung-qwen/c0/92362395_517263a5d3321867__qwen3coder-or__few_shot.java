package com.alibaba.dubbo.common;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class URL_ArrowTest {

    @Test
    public void testURLConstructors() {
        URL url1 = new URL("http", "localhost", 8080);
        assertEquals("http", url1.getProtocol());
        assertEquals("localhost", url1.getHost());
        assertEquals(8080, url1.getPort());

        URL url2 = new URL("http", "localhost", 8080, "path");
        assertEquals("http", url2.getProtocol());
        assertEquals("localhost", url2.getHost());
        assertEquals(8080, url2.getPort());
        assertEquals("path", url2.getPath());

        URL url3 = new URL("http", "user", "pass", "localhost", 8080, "path");
        assertEquals("http", url3.getProtocol());
        assertEquals("user", url3.getUsername());
        assertEquals("pass", url3.getPassword());
        assertEquals("localhost", url3.getHost());
        assertEquals(8080, url3.getPort());
        assertEquals("path", url3.getPath());
    }

    @Test
    public void testValueOf() {
        URL url = URL.valueOf("http://user:pass@localhost:8080/path?key1=value1&key2=value2");
        assertEquals("http", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("path", url.getPath());
        assertEquals("value1", url.getParameter("key1"));
        assertEquals("value2", url.getParameter("key2"));
    }

    @Test
    public void testGetAuthority() {
        URL url1 = new URL("http", "user", "pass", "localhost", 8080, "path");
        assertEquals("user:pass", url1.getAuthority());

        URL url2 = new URL("http", null, null, "localhost", 8080, "path");
        assertNull(url2.getAuthority());
    }

    @Test
    public void testGetIp() {
        URL url = new URL("http", "localhost", 8080, "path");
        assertNotNull(url.getIp());
    }

    @Test
    public void testGetAddress() {
        URL url1 = new URL("http", "localhost", 8080, "path");
        assertEquals("localhost:8080", url1.getAddress());

        URL url2 = new URL("http", "localhost", 0, "path");
        assertEquals("localhost", url2.getAddress());
    }

    @Test
    public void testGetAbsolutePath() {
        URL url1 = new URL("http", "localhost", 8080, "/path");
        assertEquals("/path", url1.getAbsolutePath());

        URL url2 = new URL("http", "localhost", 8080, "path");
        assertEquals("/path", url2.getAbsolutePath());
    }

    @Test
    public void testSetters() {
        URL url = new URL("http", "localhost", 8080, "path");
        URL newUrl = url.setProtocol("https");
        assertEquals("https", newUrl.getProtocol());

        URL newUrl2 = url.setUsername("user");
        assertEquals("user", newUrl2.getUsername());

        URL newUrl3 = url.setPassword("pass");
        assertEquals("pass", newUrl3.getPassword());

        URL newUrl4 = url.setHost("newhost");
        assertEquals("newhost", newUrl4.getHost());

        URL newUrl5 = url.setPort(9090);
        assertEquals(9090, newUrl5.getPort());

        URL newUrl6 = url.setPath("newpath");
        assertEquals("newpath", newUrl6.getPath());
    }

    @Test
    public void testGetParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        URL url = new URL("http", "localhost", 8080, "path", params);

        assertEquals("value1", url.getParameter("key1"));
        assertEquals("default", url.getParameter("nonexistent", "default"));
    }

    @Test
    public void testAddParameter() {
        URL url = new URL("http", "localhost", 8080, "path");
        URL newUrl = url.addParameter("key1", "value1");
        assertEquals("value1", newUrl.getParameter("key1"));

        URL newUrl2 = url.addParameter("key2", true);
        assertEquals("true", newUrl2.getParameter("key2"));

        URL newUrl3 = url.addParameter("key3", 123);
        assertEquals("123", newUrl3.getParameter("key3"));
    }

    @Test
    public void testRemoveParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        URL url = new URL("http", "localhost", 8080, "path", params);

        URL newUrl = url.removeParameter("key1");
        assertNull(newUrl.getParameter("key1"));
        assertEquals("value2", newUrl.getParameter("key2"));
    }

    @Test
    public void testClearParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        URL url = new URL("http", "localhost", 8080, "path", params);

        URL newUrl = url.clearParameters();
        assertTrue(newUrl.getParameters().isEmpty());
    }

    @Test
    public void testToString() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "path");
        String str = url.toString();
        assertNotNull(str);
        assertTrue(str.contains("http"));
        assertTrue(str.contains("localhost"));
        assertTrue(str.contains("8080"));
    }

    @Test
    public void testToIdentityString() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "path");
        String str = url.toIdentityString();
        assertNotNull(str);
    }

    @Test
    public void testToFullString() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "path");
        String str = url.toFullString();
        assertNotNull(str);
    }

    @Test
    public void testToJavaURL() {
        URL url = new URL("http", "localhost", 8080, "path");
        java.net.URL javaUrl = url.toJavaURL();
        assertNotNull(javaUrl);
    }

    @Test
    public void testToInetSocketAddress() {
        URL url = new URL("http", "localhost", 8080, "path");
        InetSocketAddress socketAddress = url.toInetSocketAddress();
        assertNotNull(socketAddress);
        assertEquals("localhost", socketAddress.getHostName());
        assertEquals(8080, socketAddress.getPort());
    }

    @Test
    public void testGetServiceKey() {
        Map<String, String> params = new HashMap<>();
        params.put("interface", "com.example.Service");
        params.put("group", "testGroup");
        params.put("version", "1.0.0");
        URL url = new URL("dubbo", "localhost", 8080, "path", params);

        assertEquals("testGroup/com.example.Service:1.0.0", url.getServiceKey());
    }

    @Test
    public void testGetServiceInterface() {
        Map<String, String> params = new HashMap<>();
        params.put("interface", "com.example.Service");
        URL url = new URL("dubbo", "localhost", 8080, "path", params);

        assertEquals("com.example.Service", url.getServiceInterface());
    }

    @Test
    public void testSetServiceInterface() {
        URL url = new URL("dubbo", "localhost", 8080, "path");
        URL newUrl = url.setServiceInterface("com.example.Service");
        assertEquals("com.example.Service", newUrl.getServiceInterface());
    }

    @Test
    public void testEqualsAndHashCode() {
        URL url1 = new URL("http", "localhost", 8080, "path");
        URL url2 = new URL("http", "localhost", 8080, "path");
        URL url3 = new URL("http", "localhost", 8081, "path");

        assertEquals(url1, url2);
        assertEquals(url1.hashCode(), url2.hashCode());
        assertNotEquals(url1, url3);
    }

    @Test
    public void testEncodeDecode() {
        String original = "hello world & test";
        String encoded = URL.encode(original);
        String decoded = URL.decode(encoded);
        assertEquals(original, decoded);
    }
}
