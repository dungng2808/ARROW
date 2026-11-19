package uk.me.mjt.log4jjson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SimpleJsonLayout_ArrowTest {

    private LoggingEvent event(Level level, Object message) {
        return new LoggingEvent(
                SimpleJsonLayout_ArrowTest.class.getName(),
                org.apache.log4j.Logger.getLogger(SimpleJsonLayout_ArrowTest.class),
                123456789L,
                level,
                message,
                Thread.currentThread().getName(),
                null,
                null,
                null,
                null);
    }

    private JsonObject formatted(SimpleJsonLayout layout, LoggingEvent event) {
        String output = layout.format(event);
        assertTrue(output.endsWith("\n"));
        return new JsonParser().parse(output.trim()).getAsJsonObject();
    }

    @Test
    public void formatsBasicEventFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = event(Level.INFO, "hello");

        JsonObject json = formatted(layout, event);

        assertEquals(123456789L, json.get("timestamp").getAsLong());
        assertEquals("1970-01-02T10:17:36.789Z", json.get("date").getAsString());
        assertEquals(Level.INFO.toString(), json.get("level").getAsString());
        assertEquals(Thread.currentThread().getName(), json.get("thread").getAsString());
        assertEquals("hello", json.get("message").getAsString());
        assertTrue(json.get("hostname").isJsonPrimitive());
        assertTrue(json.get("username").isJsonPrimitive());
        assertTrue(json.get("classname").isJsonPrimitive());
        assertTrue(json.get("filename").isJsonPrimitive());
        assertTrue(json.get("linenumber").isJsonPrimitive());
        assertTrue(json.get("methodname").isJsonPrimitive());
        assertTrue(json.get("ndc").isJsonNull());
        assertTrue(json.get("throwable").isJsonNull());
    }

    @Test
    public void formatsNullMessageAsJsonNull() {
        SimpleJsonLayout layout = new SimpleJsonLayout();

        JsonObject json = formatted(layout, event(Level.INFO, null));

        assertTrue(json.get("message").isJsonNull());
    }

    @Test
    public void omitsLocationFieldsBelowConfiguredMinimumLevel() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("ERROR");

        JsonObject json = formatted(layout, event(Level.INFO, "message"));

        assertFalse(json.has("classname"));
        assertFalse(json.has("filename"));
        assertFalse(json.has("linenumber"));
        assertFalse(json.has("methodname"));
    }

    @Test
    public void includesLocationFieldsAtConfiguredMinimumLevel() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("ERROR");

        JsonObject json = formatted(layout, event(Level.ERROR, "message"));

        assertTrue(json.has("classname"));
        assertTrue(json.has("filename"));
        assertTrue(json.has("linenumber"));
        assertTrue(json.has("methodname"));
    }

    @Test
    public void logsConfiguredMdcFieldsAndIgnoresEmptyTokens() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(" first, ,second ");

        JsonObject json = formatted(layout, event(Level.INFO, "message"));

        assertTrue(json.has("first"));
        assertTrue(json.get("first").isJsonNull());
        assertTrue(json.has("second"));
        assertTrue(json.get("second").isJsonNull());
        assertFalse(json.has(""));
    }

    @Test
    public void emptyMdcConfigurationAddsNoFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");

        JsonObject json = formatted(layout, event(Level.INFO, "message"));

        assertFalse(json.has("first"));
    }

    @Test
    public void ignoresThrowableReturnsFalse() {
        SimpleJsonLayout layout = new SimpleJsonLayout();

        assertFalse(layout.ignoresThrowable());
    }

    @Test
    public void activateOptionsCompletesNormally() {
        SimpleJsonLayout layout = new SimpleJsonLayout();

        layout.activateOptions();

        assertTrue(true);
    }
}
