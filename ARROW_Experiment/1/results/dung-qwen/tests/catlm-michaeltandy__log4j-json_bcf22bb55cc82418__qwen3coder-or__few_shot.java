package uk.me.mjt.log4jjson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

public class SimpleJsonLayout_ArrowTest {

    @Test
    public void testFormatWithMinimalLoggingEvent() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = Mockito.mock(LoggingEvent.class);
        Mockito.when(event.timeStamp).thenReturn(1234567890L);
        Mockito.when(event.getLevel()).thenReturn(Level.INFO);
        Mockito.when(event.getThreadName()).thenReturn("main");
        Mockito.when(event.getNDC()).thenReturn(null);
        Mockito.when(event.getMessage()).thenReturn("Test message");
        Mockito.when(event.getThrowableInformation()).thenReturn(null);
        Mockito.when(event.getLocationInformation()).thenReturn(null);

        String result = layout.format(event);
        assertNotNull(result);
        assertTrue(result.contains("\"timestamp\":1234567890"));
        assertTrue(result.contains("\"level\":\"INFO\""));
        assertTrue(result.contains("\"thread\":\"main\""));
        assertTrue(result.contains("\"message\":\"Test message\""));
        assertTrue(result.contains("\"throwable\":null"));
    }

    @Test
    public void testFormatWithLocationInfo() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("DEBUG");

        LoggingEvent event = Mockito.mock(LoggingEvent.class);
        Mockito.when(event.timeStamp).thenReturn(1234567890L);
        Mockito.when(event.getLevel()).thenReturn(Level.DEBUG);
        Mockito.when(event.getThreadName()).thenReturn("test-thread");
        Mockito.when(event.getNDC()).thenReturn("test-ndc");
        Mockito.when(event.getMessage()).thenReturn("Debug message");
        Mockito.when(event.getThrowableInformation()).thenReturn(null);

        org.apache.log4j.spi.LocationInfo locationInfo = Mockito.mock(org.apache.log4j.spi.LocationInfo.class);
        Mockito.when(locationInfo.getClassName()).thenReturn("TestClass");
        Mockito.when(locationInfo.getFileName()).thenReturn("TestClass.java");
        Mockito.when(locationInfo.getLineNumber()).thenReturn("42");
        Mockito.when(locationInfo.getMethodName()).thenReturn("testMethod");
        Mockito.when(event.getLocationInformation()).thenReturn(locationInfo);

        String result = layout.format(event);
        assertNotNull(result);
        assertTrue(result.contains("\"classname\":\"TestClass\""));
        assertTrue(result.contains("\"filename\":\"TestClass.java\""));
        assertTrue(result.contains("\"linenumber\":42"));
        assertTrue(result.contains("\"methodname\":\"testMethod\""));
    }

    @Test
    public void testSafeToStringWithNull() {
        String result = SimpleJsonLayout.safeToString(null);
        assertNull(result);
    }

    @Test
    public void testSafeToStringWithNormalObject() {
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
        assertTrue(result.startsWith("Error getting message: "));
    }

    @Test
    public void testSafeParseIntWithValidNumber() {
        Integer result = SimpleJsonLayout.safeParseInt("123");
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testSafeParseIntWithInvalidNumber() {
        Integer result = SimpleJsonLayout.safeParseInt("not_a_number");
        assertNull(result);
    }

    @Test
    public void testSafeParseIntWithNull() {
        Integer result = SimpleJsonLayout.safeParseInt(null);
        assertNull(result);
    }

    @Test
    public void testFormatThrowableWithNull() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = Mockito.mock(LoggingEvent.class);
        Mockito.when(event.getThrowableInformation()).thenReturn(null);
        String result = layout.formatThrowable(event);
        assertNull(result);
    }

    @Test
    public void testFormatThrowableWithException() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = Mockito.mock(LoggingEvent.class);
        Throwable throwable = new RuntimeException("Test exception");
        Mockito.when(event.getThrowableInformation()).thenReturn(new org.apache.log4j.spi.ThrowableInformation(throwable));
        String result = layout.formatThrowable(event);
        assertNotNull(result);
        assertTrue(result.contains("java.lang.RuntimeException: Test exception"));
    }

    @Test
    public void testSetMinimumLevelForSlowLogging() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMinimumLevelForSlowLogging("WARN");
        assertEquals(Level.WARN, layout.minimumLevelForSlowLogging);
    }

    @Test
    public void testSetMdcFieldsToLogEmptyString() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("");
        assertEquals(Collections.EMPTY_LIST, layout.mdcFieldsToLog);
    }

    @Test
    public void testSetMdcFieldsToLogNull() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog(null);
        assertEquals(Collections.EMPTY_LIST, layout.mdcFieldsToLog);
    }

    @Test
    public void testSetMdcFieldsToLogValidString() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.setMdcFieldsToLog("field1, field2 , field3 ");
        assertEquals(3, layout.mdcFieldsToLog.size());
        assertTrue(layout.mdcFieldsToLog.contains("field1"));
        assertTrue(layout.mdcFieldsToLog.contains("field2"));
        assertTrue(layout.mdcFieldsToLog.contains("field3"));
    }

    @Test
    public void testIgnoresThrowable() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        assertFalse(layout.ignoresThrowable());
    }

    @Test
    public void testActivateOptions() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        layout.activateOptions(); // Should not throw any exception
    }

    @Test
    public void testAfterMethod() {
        SimpleJsonLayout layout = new SimpleJsonLayout();
        LoggingEvent event = Mockito.mock(LoggingEvent.class);
        Map<String, Object> map = new LinkedHashMap<>();
        layout.after(event, map); // Should not throw any exception
    }
}
