package uk.me.mjt.log4jjson;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class SimpleJsonLayout_ArrowTest {

    @Test
    public void testFormatWithNullMessage() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                null,
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(null, json.get("message").getAsString());
    }

    @Test
    public void testFormatWithNormalMessage() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("Test message", json.get("message").getAsString());
    }

    @Test
    public void testFormatWithThrowable() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        Throwable throwable = new RuntimeException("Test exception");
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.ERROR,
                "Test message",
                "test thread",
                throwable
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertNotNull(json.get("throwable"));
        assertTrue(json.get("throwable").getAsString().contains("Test exception"));
    }

    @Test
    public void testFormatWithNullThrowable() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.ERROR,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertNull(json.get("throwable"));
    }

    @Test
    public void testFormatWithMdcFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("field1, field2");

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );
        event.getMDC("field1");
        event.getMDC("field2");

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertTrue(json.has("field1"));
        assertTrue(json.has("field2"));
    }

    @Test
    public void testFormatWithEmptyMdcFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertFalse(json.has("field1"));
    }

    @Test
    public void testFormatWithNullMdcFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(null);

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertFalse(json.has("field1"));
    }

    @Test
    public void testFormatWithMinimumLevelForSlowLogging() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("WARN");

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.WARN,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertTrue(json.has("classname"));
        assertTrue(json.has("filename"));
        assertTrue(json.has("linenumber"));
        assertTrue(json.has("methodname"));
    }

    @Test
    public void testFormatWithoutSlowLogging() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("ERROR");

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertFalse(json.has("classname"));
        assertFalse(json.has("filename"));
        assertFalse(json.has("linenumber"));
        assertFalse(json.has("methodname"));
    }

    @Test
    public void testSafeToStringWithNull() {
        String result = SimpleJsonLayout.safeToString(null);
        assertNull(result);
    }

    @Test
    public void testSafeToStringWithNormalString() {
        String result = SimpleJsonLayout.safeToString("test string");
        assertEquals("test string", result);
    }

    @Test
    public void testSafeToStringWithExceptionInToString() {
        Object obj = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("Test exception");
            }
        };
        String result = SimpleJsonLayout.safeToString(obj);
        assertNotNull(result);
        assertTrue(result.contains("Error getting message"));
    }

    @Test
    public void testSafeParseIntWithValidNumber() {
        Integer result = SimpleJsonLayout.safeParseInt("123");
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testSafeParseIntWithInvalidNumber() {
        Integer result = SimpleJsonLayout.safeParseInt("abc");
        assertNull(result);
    }

    @Test
    public void testSafeParseIntWithNull() {
        Integer result = SimpleJsonLayout.safeParseInt(null);
        assertNull(result);
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
        SimpleJsonLayout layout = new SimpleJsonLayout() {
            @Override
            public void after(LoggingEvent le, Map<String, Object> r) {
                r.put("customField", "customValue");
            }
        };

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("customValue", json.get("customField").getAsString());
    }

    @Test
    public void testGetHostname() {
        String hostname = SimpleJsonLayout.getHostname();
        assertNotNull(hostname);
        assertTrue(hostname.length() > 0);
    }

    @Test
    public void testSetMinimumLevelForSlowLogging() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("DEBUG");
        assertEquals(Level.DEBUG, layout.minimumLevelForSlowLogging);
    }

    @Test
    public void testSetMdcFieldsToLogWithCommaSeparatedValues() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("field1, field2, field3");
        assertEquals(3, layout.mdcFieldsToLog.size());
        assertTrue(layout.mdcFieldsToLog.contains("field1"));
        assertTrue(layout.mdcFieldsToLog.contains("field2"));
        assertTrue(layout.mdcFieldsToLog.contains("field3"));
    }

    @Test
    public void testSetMdcFieldsToLogWithWhitespace() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(" field1 , field2 ");
        assertEquals(2, layout.mdcFieldsToLog.size());
        assertTrue(layout.mdcFieldsToLog.contains("field1"));
        assertTrue(layout.mdcFieldsToLog.contains("field2"));
    }

    @Test
    public void testSetMdcFieldsToLogWithEmptyString() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");
        assertEquals(Collections.EMPTY_LIST, layout.mdcFieldsToLog);
    }

    @Test
    public void testSetMdcFieldsToLogWithNull() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(null);
        assertEquals(Collections.EMPTY_LIST, layout.mdcFieldsToLog);
    }

    @Test
    public void testFormatIncludesTimestampAndDate() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                1234567890L,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals(1234567890L, json.get("timestamp").getAsLong());
        assertNotNull(json.get("date"));
    }

    @Test
    public void testFormatIncludesHostnameAndUsername() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertNotNull(json.get("hostname"));
        assertNotNull(json.get("username"));
    }

    @Test
    public void testFormatIncludesLevelAndThread() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("INFO", json.get("level").getAsString());
        assertEquals("test thread", json.get("thread").getAsString());
    }

    @Test
    public void testFormatIncludesNdc() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );
        event.setNDC("test ndc");

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("test ndc", json.get("ndc").getAsString());
    }

    @Test
    public void testFormatIncludesMessage() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("Test message", json.get("message").getAsString());
    }

    @Test
    public void testFormatIncludesThrowableFormatted() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        RuntimeException ex = new RuntimeException("Test exception");
        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.ERROR,
                "Test message",
                "test thread",
                ex
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertNotNull(json.get("throwable"));
        assertTrue(json.get("throwable").getAsString().contains("Test exception"));
    }

    @Test
    public void testFormatWithMdcFieldAlreadyPresent() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("message"); // This field already exists

        LoggingEvent event = new LoggingEvent(
                "test.class",
                null,
                0,
                Level.INFO,
                "Test message",
                "test thread",
                null
        );

        String result = layout.format(event);
        assertNotNull(result);
        JsonObject json = JsonParser.parseString(result).getAsJsonObject();
        assertEquals("Test message", json.get("message").getAsString());
    }
}
