package de.fraunhofer.iosb.ilt.frostserver.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StringHelper_ArrowTest {

    @Test
    public void testCapitalize() {
        assertEquals("Hello", StringHelper.capitalize("hello"));
        assertEquals("World", StringHelper.capitalize("world"));
        assertEquals("A", StringHelper.capitalize("a"));
        assertEquals("", StringHelper.capitalize(""));
    }

    @Test
    public void testDecapitalize() {
        assertEquals("hello", StringHelper.deCapitalize("Hello"));
        assertEquals("world", StringHelper.deCapitalize("World"));
        assertEquals("a", StringHelper.deCapitalize("A"));
        assertEquals("", StringHelper.deCapitalize(""));
    }

    @Test
    public void testIsNullOrEmpty() {
        assertTrue(StringHelper.isNullOrEmpty(null));
        assertTrue(StringHelper.isNullOrEmpty(""));
        assertFalse(StringHelper.isNullOrEmpty("test"));
        assertFalse(StringHelper.isNullOrEmpty(" "));
    }

    @Test
    public void testCleanForLogging_String() {
        assertEquals("null", StringHelper.cleanForLogging(null));
        assertEquals("hello_world", StringHelper.cleanForLogging("hello\nworld"));
        assertEquals("hello_world", StringHelper.cleanForLogging("hello\rworld"));
        assertEquals("hello_world", StringHelper.cleanForLogging("hello\tworld"));
        assertEquals("hello_world_test", StringHelper.cleanForLogging("hello\nworld\ntest"));
        assertEquals("test", StringHelper.cleanForLogging("test"));
    }

    @Test
    public void testCleanForLogging_Object() {
        assertEquals("null", StringHelper.cleanForLogging((Object) null));
        assertEquals("hello_world", StringHelper.cleanForLogging((Object) "hello\nworld"));
        assertEquals("123", StringHelper.cleanForLogging((Object) 123));
    }

    @Test
    public void testEscapeForStringConstant() {
        assertEquals("''", StringHelper.escapeForStringConstant("'"));
        assertEquals("hello''world", StringHelper.escapeForStringConstant("hello'world"));
        assertEquals("''hello''world''", StringHelper.escapeForStringConstant("'hello'world'"));
        assertEquals("hello world", StringHelper.escapeForStringConstant("hello world"));
    }

    @Test
    public void testUrlEncode() {
        assertEquals("hello%20world", StringHelper.urlEncode("hello world"));
        assertEquals("test%2Bstring", StringHelper.urlEncode("test+string"));
        assertEquals("", StringHelper.urlEncode(""));
    }

    @Test
    public void testUrlDecode() {
        assertEquals("hello world", StringHelper.urlDecode("hello%20world"));
        assertEquals("test+string", StringHelper.urlDecode("test%2Bstring"));
        assertEquals("", StringHelper.urlDecode(""));
    }

    @Test
    public void testUrlEncodeWithNotSlashes() {
        assertEquals("hello%2Fworld", StringHelper.urlEncode("hello/world", true));
        assertEquals("hello%20world", StringHelper.urlEncode("hello world", true));
        assertEquals("test%2Bstring", StringHelper.urlEncode("test+string", true));
        assertEquals("", StringHelper.urlEncode("", true));
    }

    @Test
    public void testUrlEncodeNotSlashes() {
        assertEquals("hello%2Fworld", StringHelper.urlEncodeNotSlashes("hello/world"));
        assertEquals("hello%20world", StringHelper.urlEncodeNotSlashes("hello world"));
        assertEquals("test%2Bstring", StringHelper.urlEncodeNotSlashes("test+string"));
        assertEquals("", StringHelper.urlEncodeNotSlashes(""));
        assertEquals("path/to/resource", StringHelper.urlEncodeNotSlashes("path/to/resource"));
    }
}
