package uk.me.mjt.log4jjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

public class SimpleJsonLayout_ArrowTest {

    @Test
    public void ignoresThrowableReturnsFalse() {
        SimpleJsonLayout layout = new SimpleJsonLayout();

        assertFalse(layout.ignoresThrowable());
    }

    @Test
    public void formatsBasicEventAsJson() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = new LoggingEvent(
                "test.logger",
                SimpleJsonLayout_ArrowTest.class.getName(),
                Level.INFO,
                "hello",
                null);

        String json = layout.format(event);

        assertTrue(json.startsWith("{\"timestamp\":"));
        assertTrue(json.contains("\"hostname\":"));
        assertTrue(json.contains("\"username\":"));
        assertTrue(json.contains("\"level\":\"INFO\""));
        assertTrue(json.contains("\"thread\":"));
        assertTrue(json.contains("\"ndc\":null"));
        assertTrue(json.contains("\"message\":\"hello\""));
        assertTrue(json.contains("\"throwable\":null"));
        assertTrue(json.endsWith("}\n"));
    }

    @Test
    public void formatsConfiguredMdcFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(" first, ,second ");
        LoggingEvent event = new LoggingEvent(
                "test.logger",
                SimpleJsonLayout_ArrowTest.class.getName(),
                Level.INFO,
                "message",
                null);

        String json = layout.format(event);

        assertTrue(json.contains("\"first\":null"));
        assertTrue(json.contains("\"second\":null"));
    }

    @Test
    public void emptyMdcConfigurationAddsNoFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");
        LoggingEvent event = new LoggingEvent(
                "test.logger",
                SimpleJsonLayout_ArrowTest.class.getName(),
                Level.INFO,
                "message",
                null);

        String json = layout.format(event);

        assertFalse(json.contains("\"first\":"));
        assertFalse(json.contains("\"second\":"));
    }

    @Test
    public void afterCanOverrideFields() {
        SimpleJsonLayout layout = new SimpleJsonLayout() {
            @Override
            public void after(LoggingEvent event, Map<String, Object> fields) {
                fields.put("message", "overridden");
            }
        };
        LoggingEvent event = new LoggingEvent(
                "test.logger",
                SimpleJsonLayout_ArrowTest.class.getName(),
                Level.INFO,
                "original",
                null);

        String json = layout.format(event);

        assertTrue(json.contains("\"message\":\"overridden\""));
        assertFalse(json.contains("\"message\":\"original\""));
    }

    @Test
    public void settingNullMdcConfigurationIsAccepted() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(null);
        LoggingEvent event = new LoggingEvent(
                "test.logger",
                SimpleJsonLayout_ArrowTest.class.getName(),
                Level.INFO,
                "message",
                null);

        String json = layout.format(event);

        assertTrue(json.contains("\"message\":\"message\""));
    }
}
