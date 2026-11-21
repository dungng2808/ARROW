package com.alibaba.dubbo.common;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class URL_ArrowTest {

    @Test
    public void testConstructorWithProtocolHostPort() {
        URL url = new URL("http", "localhost", 8080);
        assertEquals("http", url.getProtocol());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertNull(url.getUsername());
        assertNull(url.getPassword());
        assertNull(url.getPath());
    }

    @Test
    public void testConstructorWithProtocolHostPortPath() {
        URL url = new URL("http", "localhost", 8080, "/test/path");
        assertEquals("http", url.getProtocol());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/test/path", url.getPath());
    }

    @Test
    public void testConstructorWithProtocolUsernamePasswordHostPortPath() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        assertEquals("http", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/test/path", url.getPath());
    }

    @Test
    public void testConstructorWithProtocolUsernamePasswordHostPortPathParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path", params);
        assertEquals("http", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/test/path", url.getPath());
        assertEquals("value1", url.getParameter("key1"));
        assertEquals("value2", url.getParameter("key2"));
    }

    @Test
    public void testValueOf() {
        URL url = URL.valueOf("http://user:pass@localhost:8080/test/path?key1=value1&key2=value2");
        assertEquals("http", url.getProtocol());
        assertEquals("user", url.getUsername());
        assertEquals("pass", url.getPassword());
        assertEquals("localhost", url.getHost());
        assertEquals(8080, url.getPort());
        assertEquals("/test/path", url.getPath());
        assertEquals("value1", url.getParameter("key1"));
        assertEquals("value2", url.getParameter("key2"));
    }

    @Test
    public void testGetAuthority() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        assertEquals("user:pass", url.getAuthority());

        URL url2 = new URL("http", null, null, "localhost", 8080, "/test/path");
        assertNull(url2.getAuthority());
    }

    @Test
    public void testGetAddress() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        assertEquals("localhost:8080", url.getAddress());

        URL url2 = new URL("http", "user", "pass", "localhost", 0, "/test/path");
        assertEquals("localhost", url2.getAddress());
    }

    @Test
    public void testGetIp() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        assertNotNull(url.getIp());
    }

    @Test
    public void testGetAbsolutePath() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        assertEquals("/test/path", url.getAbsolutePath());

        URL url2 = new URL("http", "user", "pass", "localhost", 8080, "test/path");
        assertEquals("/test/path", url2.getAbsolutePath());
    }

    @Test
    public void testSetters() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");

        URL newUrl = url.setProtocol("https");
        assertEquals("https", newUrl.getProtocol());

        newUrl = url.setUsername("newuser");
        assertEquals("newuser", newUrl.getUsername());

        newUrl = url.setPassword("newpass");
        assertEquals("newpass", newUrl.getPassword());

        newUrl = url.setHost("newhost");
        assertEquals("newhost", newUrl.getHost());

        newUrl = url.setPort(9090);
        assertEquals(9090, newUrl.getPort());

        newUrl = url.setPath("/newpath");
        assertEquals("/newpath", newUrl.getPath());
    }

    @Test
    public void testSetAddress() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        URL newUrl = url.setAddress("newhost:9090");
        assertEquals("newhost", newUrl.getHost());
        assertEquals(9090, newUrl.getPort());
    }

    @Test
    public void testGetParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path", params);

        assertEquals("value1", url.getParameter("key1"));
        assertEquals(null, url.getParameter("nonexistent"));
        assertEquals("default", url.getParameter("nonexistent", "default"));
    }

    @Test
    public void testHasParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path", params);

        assertTrue(url.hasParameter("key1"));
        assertFalse(url.hasParameter("nonexistent"));
    }

    @Test
    public void testAddParameter() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        URL newUrl = url.addParameter("key1", "value1");
        assertEquals("value1", newUrl.getParameter("key1"));

        // Test adding duplicate parameter
        URL newUrl2 = newUrl.addParameter("key1", "value1");
        assertEquals(newUrl, newUrl2);
    }

    @Test
    public void testRemoveParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path", params);

        URL newUrl = url.removeParameter("key1");
        assertEquals("value2", newUrl.getParameter("key2"));
        assertNull(newUrl.getParameter("key1"));
    }

    @Test
    public void testClearParameters() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path", params);

        URL newUrl = url.clearParameters();
        assertNull(newUrl.getParameter("key1"));
    }

    @Test
    public void testToString() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        String str = url.toString();
        assertNotNull(str);
        assertTrue(str.contains("http://user:pass@localhost:8080/test/path"));
    }

    @Test
    public void testToIdentityString() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        String str = url.toIdentityString();
        assertNotNull(str);
        assertTrue(str.contains("http://localhost:8080/test/path"));
    }

    @Test
    public void testToFullString() {
        URL url = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        String str = url.toFullString();
        assertNotNull(str);
        assertTrue(str.contains("http://user:pass@localhost:8080/test/path"));
    }

    @Test
    public void testEqualsAndHashCode() {
        URL url1 = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        URL url2 = new URL("http", "user", "pass", "localhost", 8080, "/test/path");
        URL url3 = new URL("http", "user", "pass", "localhost", 8081, "/test/path");

        assertEquals(url1, url2);
        assertEquals(url1.hashCode(), url2.hashCode());
        assertNotEquals(url1, url3);
    }

    @Test
    public void testGetServiceKey() {
        Map<String, String> params = new HashMap<>();
        params.put("interface", "com.example.TestService");
        params.put("group", "testGroup");
        params.put("version", "1.0.0");
        URL url = new URL("dubbo", "user", "pass", "localhost", 8080, "/test/path", params);

        assertEquals("testGroup/com.example.TestService:1.0.0", url.getServiceKey());
    }

    @Test
    public void testGetServiceInterface() {
        Map<String, String> params = new HashMap<>();
        params.put("interface", "com.example.TestService");
        URL url = new URL("dubbo", "user", "pass", "localhost", 8080, "/test/path", params);

        assertEquals("com.example.TestService", url.getServiceInterface());
    }

    @Test
    public void testSetServiceInterface() {
        URL url = new URL("dubbo", "user", "pass", "localhost", 8080, "/test/path");
        URL newUrl = url.setServiceInterface("com.example.TestService");
        assertEquals("com.example.TestService", newUrl.getServiceInterface());
    }

    @Test
    public void testEncodeDecode() {
        String original = "hello world & special chars";
        String encoded = URL.encode(original);
        String decoded = URL.decode(encoded);
        assertEquals(original, decoded);
    }
}
