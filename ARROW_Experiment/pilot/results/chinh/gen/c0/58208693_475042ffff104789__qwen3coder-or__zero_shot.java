package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

public class DateTime_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        DateTime dt = new DateTime();
        assertNotNull(dt);
        assertTrue(dt.getUtcTime() > 0);
    }

    @Test
    public void testLongConstructor() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        assertEquals(utcTime, dt.getUtcTime());
    }

    @Test
    public void testDateConstructor() {
        Date date = new Date(1234567890123L);
        DateTime dt = new DateTime(date);
        assertEquals(DateTime.javaToUtc(1234567890123L), dt.getUtcTime());
    }

    @Test
    public void testInstantConstructor() {
        Instant instant = Instant.ofEpochSecond(1234567890L, 123456789);
        DateTime dt = new DateTime(instant);
        assertEquals(DateTime.javaToUtc(instant.toEpochMilli()) + (instant.getNano() % 1_000_000) / 100, dt.getUtcTime());
    }

    @Test
    public void testGetUtcTime() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        assertEquals(utcTime, dt.getUtcTime());
    }

    @Test
    public void testGetJavaTime() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        assertEquals(DateTime.utcToJava(utcTime), dt.getJavaTime());
    }

    @Test
    public void testGetJavaDate() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        Date date = dt.getJavaDate();
        assertNotNull(date);
        assertEquals(DateTime.utcToJava(utcTime), date.getTime());
    }

    @Test
    public void testGetJavaInstant() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        Instant instant = dt.getJavaInstant();
        assertNotNull(instant);
        assertEquals(DateTime.utcToJava(utcTime) / 1_000, instant.getEpochSecond());
        assertEquals((utcTime % 10_000_000) * 100, instant.getNano());
    }

    @Test
    public void testIsNull() {
        DateTime dt = new DateTime(0L);
        assertTrue(dt.isNull());

        dt = new DateTime(1L);
        assertFalse(dt.isNull());
    }

    @Test
    public void testEquals() {
        DateTime dt1 = new DateTime(123456789012345678L);
        DateTime dt2 = new DateTime(123456789012345678L);
        DateTime dt3 = new DateTime(123456789012345679L);

        assertEquals(dt1, dt2);
        assertNotEquals(dt1, dt3);
        assertEquals(dt1, dt1);
        assertNotEquals(dt1, null);
        assertNotEquals(dt1, "not a datetime");
    }

    @Test
    public void testHashCode() {
        DateTime dt1 = new DateTime(123456789012345678L);
        DateTime dt2 = new DateTime(123456789012345678L);
        assertEquals(dt1.hashCode(), dt2.hashCode());
    }

    @Test
    public void testToString() {
        DateTime dt = new DateTime(123456789012345678L);
        String str = dt.toString();
        assertNotNull(str);
        assertTrue(str.contains("utcTime=123456789012345678"));
        assertTrue(str.contains("javaDate="));
    }

    @Test
    public void testNow() {
        DateTime dt = DateTime.now();
        assertNotNull(dt);
        assertTrue(dt.getUtcTime() > 0);
    }

    @Test
    public void testNowNanos() {
        DateTime dt = DateTime.nowNanos();
        assertNotNull(dt);
        assertTrue(dt.getUtcTime() > 0);
    }

    @Test
    public void testMinValue() {
        assertEquals(new DateTime(0L), DateTime.MIN_VALUE);
        assertEquals(DateTime.MIN_VALUE, DateTime.NULL_VALUE);
    }

    @Test
    public void testJavaToUtc() {
        long javaTime = 1234567890123L;
        long expected = (javaTime * 10_000) + DateTime.EPOCH_DELTA;
        assertEquals(expected, DateTime.javaToUtc(javaTime));
    }

    @Test
    public void testUtcToJava() {
        long utcTime = 123456789012345678L;
        long expected = (utcTime - DateTime.EPOCH_DELTA) / 10_000;
        assertEquals(expected, DateTime.utcToJava(utcTime));
    }
}
