package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class DateTime_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        DateTime dateTime = new DateTime();
        assertFalse(dateTime.isNull());
        assertTrue(dateTime.getUtcTime() > 0);
    }

    @Test
    public void testLongConstructor() {
        long utcTime = 1234567890123L;
        DateTime dateTime = new DateTime(utcTime);
        assertEquals(utcTime, dateTime.getUtcTime());
    }

    @Test
    public void testDateConstructor() {
        Date date = new Date(1234567890123L);
        DateTime dateTime = new DateTime(date);
        assertEquals(1234567890123L, dateTime.getJavaTime());
    }

    @Test
    public void testInstantConstructor() {
        Instant instant = Instant.ofEpochMilli(1234567890123L).plusNanos(456789);
        DateTime dateTime = new DateTime(instant);
        assertEquals(1234567890123L, dateTime.getJavaTime());
    }

    @Test
    public void testGetUtcTime() {
        long utcTime = 1234567890123L;
        DateTime dateTime = new DateTime(utcTime);
        assertEquals(utcTime, dateTime.getUtcTime());
    }

    @Test
    public void testGetJavaTime() {
        long utcTime = 1234567890123L;
        DateTime dateTime = new DateTime(utcTime);
        assertEquals(1234567890123L, dateTime.getJavaTime());
    }

    @Test
    public void testGetJavaDate() {
        long utcTime = 1234567890123L;
        DateTime dateTime = new DateTime(utcTime);
        assertEquals(new Date(1234567890123L), dateTime.getJavaDate());
    }

    @Test
    public void testGetJavaInstant() {
        long utcTime = 1234567890123456L;
        DateTime dateTime = new DateTime(utcTime);
        Instant instant = dateTime.getJavaInstant();
        assertEquals(1234567890L, instant.getEpochSecond());
        assertEquals(123456000, instant.getNano());
    }

    @Test
    public void testIsNull() {
        DateTime dateTime = new DateTime(0L);
        assertTrue(dateTime.isNull());

        dateTime = new DateTime(1L);
        assertFalse(dateTime.isNull());
    }

    @Test
    public void testEquals() {
        DateTime dateTime1 = new DateTime(1234567890123L);
        DateTime dateTime2 = new DateTime(1234567890123L);
        DateTime dateTime3 = new DateTime(1234567890124L);

        assertEquals(dateTime1, dateTime2);
        assertNotEquals(dateTime1, dateTime3);
        assertNotEquals(dateTime1, null);
        assertNotEquals(dateTime1, new Object());
    }

    @Test
    public void testHashCode() {
        DateTime dateTime1 = new DateTime(1234567890123L);
        DateTime dateTime2 = new DateTime(1234567890123L);
        assertEquals(dateTime1.hashCode(), dateTime2.hashCode());
    }

    @Test
    public void testToString() {
        DateTime dateTime = new DateTime(1234567890123L);
        String str = dateTime.toString();
        assertTrue(str.contains("1234567890123"));
        assertTrue(str.contains("javaDate"));
    }

    @Test
    public void testNow() {
        DateTime dateTime = DateTime.now();
        assertFalse(dateTime.isNull());
        assertTrue(dateTime.getUtcTime() > 0);
    }

    @Test
    public void testNowNanos() {
        DateTime dateTime = DateTime.nowNanos();
        assertFalse(dateTime.isNull());
        assertTrue(dateTime.getUtcTime() > 0);
    }

    @Test
    public void testMinValue() {
        assertEquals(0L, DateTime.MIN_VALUE.getUtcTime());
        assertTrue(DateTime.MIN_VALUE.isNull());
    }

    @Test
    public void testNullValue() {
        assertEquals(DateTime.MIN_VALUE, DateTime.NULL_VALUE);
    }

    @Test
    public void testJavaToUtcConversion() {
        long javaTime = 1234567890123L;
        long utcTime = DateTime.javaToUtc(javaTime);
        assertEquals((javaTime * 10_000) + 116444736000000000L, utcTime);
    }

    @Test
    public void testUtcToJavaConversion() {
        long utcTime = 1234567890123456L;
        long javaTime = DateTime.utcToJava(utcTime);
        assertEquals((utcTime - 116444736000000000L) / 10_000, javaTime);
    }
}
