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
    public void testCleanForLogging() {
        assertEquals("null", StringHelper.cleanForLogging(null));
        assertEquals("hello_world", StringHelper.cleanForLogging("hello\nworld"));
        assertEquals("hello_world", StringHelper.cleanForLogging("hello\rworld"));
        assertEquals("hello_world", StringHelper.cleanForLogging("hello\tworld"));
        assertEquals("hello_nrt_world", StringHelper.cleanForLogging("hello\n\r\tworld"));
        assertEquals("test", StringHelper.cleanForLogging("test"));
    }

    @Test
    public void testCleanForLoggingObject() {
        assertEquals("null", StringHelper.cleanForLogging((Object) null));
        assertEquals("123", StringHelper.cleanForLogging(123));
        assertEquals("hello", StringHelper.cleanForLogging("hello"));
    }

    @Test
    public void testEscapeForStringConstant() {
        assertEquals("''", StringHelper.escapeForStringConstant("'"));
        assertEquals("hello''world", StringHelper.escapeForStringConstant("hello'world"));
        assertEquals("hello''''world", StringHelper.escapeForStringConstant("hello''world"));
        assertEquals("", StringHelper.escapeForStringConstant(""));
    }

    @Test
    public void testUrlEncode() {
        assertEquals("hello%20world", StringHelper.urlEncode("hello world"));
        assertEquals("test%2Bcase", StringHelper.urlEncode("test+case"));
        assertEquals("", StringHelper.urlEncode(""));
    }

    @Test
    public void testUrlDecode() {
        assertEquals("hello world", StringHelper.urlDecode("hello%20world"));
        assertEquals("test+case", StringHelper.urlDecode("test%2Bcase"));
        assertEquals("", StringHelper.urlDecode(""));
    }

    @Test
    public void testUrlEncodeWithNotSlashes() {
        assertEquals("hello%2Fworld", StringHelper.urlEncode("hello/world", true));
        assertEquals("hello%20world", StringHelper.urlEncode("hello world", true));
        assertEquals("test%2Bcase", StringHelper.urlEncode("test+case", true));
        assertEquals("", StringHelper.urlEncode("", true));
    }

    @Test
    public void testUrlEncodeNotSlashes() {
        assertEquals("hello/world", StringHelper.urlEncodeNotSlashes("hello/world"));
        assertEquals("hello%20world", StringHelper.urlEncodeNotSlashes("hello world"));
        assertEquals("test%2Bcase", StringHelper.urlEncodeNotSlashes("test+case"));
        assertEquals("", StringHelper.urlEncodeNotSlashes(""));
    }
}
