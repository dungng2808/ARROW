package uk.me.mjt.log4jjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

public class SimpleJsonLayout_ArrowTest {

    @Test
    public void formatsBasicEventAsJson() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "example.Logger",
                org.apache.log4j.Logger.getLogger("example.Logger"),
                123456789L,
                Level.INFO,
                "hello",
                "test-thread");

        String formatted = layout.format(event);
        assertTrue(formatted.endsWith("\n"));

        JsonObject json = new JsonParser().parse(formatted.trim()).getAsJsonObject();
        assertEquals(123456789L, json.get("timestamp").getAsLong());
        assertEquals("INFO", json.get("level").getAsString());
        assertEquals("test-thread", json.get("thread").getAsString());
        assertEquals("hello", json.get("message").getAsString());
        assertTrue(json.get("date").isJsonObject());
        assertTrue(json.get("hostname").getAsString().length() > 0);
        assertTrue(json.get("username").getAsString().length() > 0);
        assertTrue(json.get("throwable").isJsonNull());
    }

    @Test
    public void omitsLocationFieldsBelowConfiguredMinimumLevel() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("ERROR");
        LoggingEvent event = new LoggingEvent(
                "example.Logger",
                org.apache.log4j.Logger.getLogger("example.Logger"),
                1000L,
                Level.INFO,
                "message",
                "thread");

        JsonObject json = new JsonParser().parse(layout.format(event).trim()).getAsJsonObject();
        assertFalse(json.has("classname"));
        assertFalse(json.has("filename"));
        assertFalse(json.has("linenumber"));
        assertFalse(json.has("methodname"));
    }

    @Test
    public void includesConfiguredMdcFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(" first, ,second ");
        LoggingEvent event = new LoggingEvent(
                "example.Logger",
                org.apache.log4j.Logger.getLogger("example.Logger"),
                1000L,
                Level.INFO,
                "message",
                "thread");

        JsonObject json = new JsonParser().parse(layout.format(event).trim()).getAsJsonObject();
        assertTrue(json.has("first"));
        assertTrue(json.get("first").isJsonNull());
        assertTrue(json.has("second"));
        assertTrue(json.get("second").isJsonNull());
    }

    @Test
    public void ignoresThrowableReturnsFalse() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        assertFalse(layout.ignoresThrowable());
    }

    @Test
    public void emptyMdcConfigurationAddsNoFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");
        LoggingEvent event = new LoggingEvent(
                "example.Logger",
                org.apache.log4j.Logger.getLogger("example.Logger"),
                1000L,
                Level.INFO,
                "message",
                "thread");

        JsonObject json = new JsonParser().parse(layout.format(event).trim()).getAsJsonObject();
        assertFalse(json.has("first"));
    }
}
