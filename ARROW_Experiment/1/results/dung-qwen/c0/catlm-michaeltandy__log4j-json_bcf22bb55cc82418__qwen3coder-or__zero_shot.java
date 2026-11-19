package uk.me.mjt.log4jjson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

public class SimpleJsonLayout_ArrowTest {

    @Test
    public void testFormatReturnsJsonString() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent("test", null, "test", Level.INFO, "test message", null);
        String result = layout.format(event);
        assertNotNull(result);
        assertTrue(result.contains("\"message\":\"test message\""));
        assertTrue(result.endsWith("\n"));
    }

    @Test
    public void testFormatContainsBasicFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent("test", null, "test", Level.INFO, "test message", null);
        String result = layout.format(event);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertTrue(json.has("timestamp"));
        assertTrue(json.has("date"));
        assertTrue(json.has("hostname"));
        assertTrue(json.has("username"));
        assertTrue(json.has("level"));
        assertTrue(json.has("thread"));
        assertTrue(json.has("ndc"));
        assertTrue(json.has("message"));
        assertTrue(json.has("throwable"));
    }

    @Test
    public void testFormatWithThrowable() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        Exception ex = new RuntimeException("test exception");
        LoggingEvent event = new LoggingEvent("test", null, "test", Level.INFO, "test message", ex);
        String result = layout.format(event);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertNotNull(json.get("throwable"));
        assertTrue(json.get("throwable").getAsString().contains("test exception"));
    }

    @Test
    public void testFormatWithMdcFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("field1,field2");
        LoggingEvent event = new LoggingEvent("test", null, "test", Level.INFO, "test message", null);
        event.getMDC("field1");
        event.getMDC("field2");
        String result = layout.format(event);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertTrue(json.has("field1"));
        assertTrue(json.has("field2"));
    }

    @Test
    public void testFormatWithLocationInfo() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("DEBUG");
        LoggingEvent event = new LoggingEvent("test", null, "test", Level.DEBUG, "test message", null);
        String result = layout.format(event);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertTrue(json.has("classname"));
        assertTrue(json.has("filename"));
        assertTrue(json.has("linenumber"));
        assertTrue(json.has("methodname"));
    }

    @Test
    public void testSafeToStringNull() {
        String result = SimpleJsonLayout.safeToString(null);
        assertNull(result);
    }

    @Test
    public void testSafeToStringNormal() {
        String result = SimpleJsonLayout.safeToString("test string");
        assertEquals("test string", result);
    }

    @Test
    public void testSafeToStringException() {
        Object obj = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("test exception");
            }
        };
        String result = SimpleJsonLayout.safeToString(obj);
        assertTrue(result.startsWith("Error getting message: "));
    }

    @Test
    public void testSafeParseIntValid() {
        Integer result = SimpleJsonLayout.safeParseInt("123");
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testSafeParseIntInvalid() {
        Integer result = SimpleJsonLayout.safeParseInt("invalid");
        assertNull(result);
    }

    @Test
    public void testSafeParseIntNull() {
        Integer result = SimpleJsonLayout.safeParseInt(null);
        assertNull(result);
    }

    @Test
    public void testSetMinimumLevelForSlowLogging() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("WARN");
        Field field;
        try {
            field = SimpleJsonLayout.class.getDeclaredField("minimumLevelForSlowLogging");
            field.setAccessible(true);
            Object value = field.get(layout);
            assertEquals(Level.WARN, value);
        } catch (Exception e) {
            fail("Could not access field");
        }
    }

    @Test
    public void testSetMdcFieldsToLogEmpty() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");
        Field field;
        try {
            field = SimpleJsonLayout.class.getDeclaredField("mdcFieldsToLog");
            field.setAccessible(true);
            Object value = field.get(layout);
            assertTrue(((java.util.List) value).isEmpty());
        } catch (Exception e) {
            fail("Could not access field");
        }
    }

    @Test
    public void testSetMdcFieldsToLogNull() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(null);
        Field field;
        try {
            field = SimpleJsonLayout.class.getDeclaredField("mdcFieldsToLog");
            field.setAccessible(true);
            Object value = field.get(layout);
            assertTrue(((java.util.List) value).isEmpty());
        } catch (Exception e) {
            fail("Could not access field");
        }
    }

    @Test
    public void testSetMdcFieldsToLogValid() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("field1, field2 , field3");
        Field field;
        try {
            field = SimpleJsonLayout.class.getDeclaredField("mdcFieldsToLog");
            field.setAccessible(true);
            Object value = field.get(layout);
            assertEquals(3, ((java.util.List) value).size());
            assertTrue(((java.util.List) value).contains("field1"));
            assertTrue(((java.util.List) value).contains("field2"));
            assertTrue(((java.util.List) value).contains("field3"));
        } catch (Exception e) {
            fail("Could not access field");
        }
    }

    @Test
    public void testIgnoresThrowable() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        assertFalse(layout.ignoresThrowable());
    }

    @Test
    public void testActivateOptions() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.activateOptions(); // Should not throw
    }

    @Test
    public void testAfterMethod() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent("test", null, "test", Level.INFO, "test message", null);
        // Just verify it doesn't throw
        layout.after(event, null);
    }
}
