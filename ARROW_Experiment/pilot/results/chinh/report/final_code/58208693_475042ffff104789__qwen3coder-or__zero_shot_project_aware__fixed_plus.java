package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateTime_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        DateTime dt = new DateTime();
        Assert.assertNotNull(dt);
        Assert.assertTrue(dt.getUtcTime() > 0);
    }

    @Test
    public void testLongConstructor() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        Assert.assertEquals(dt.getUtcTime(), utcTime);
    }

    @Test
    public void testDateConstructor() {
        Date date = new Date(1234567890123L);
        DateTime dt = new DateTime(date);
        Assert.assertEquals(dt.getUtcTime(), 1234567890123L * 10000 + 116444736000000000L);
    }

    @Test
    public void testInstantConstructor() {
        Instant instant = Instant.ofEpochSecond(1234567890L, 123456789);
        DateTime dt = new DateTime(instant);
        long expected = 1234567890L * 10000 + 116444736000000000L + 123456789 / 100;
        Assert.assertEquals(dt.getUtcTime(), expected);
    }

    @Test
    public void testGetUtcTime() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        Assert.assertEquals(dt.getUtcTime(), utcTime);
    }

    @Test
    public void testGetJavaTime() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        long expected = (utcTime - 116444736000000000L) / 10000;
        Assert.assertEquals(dt.getJavaTime(), expected);
    }

    @Test
    public void testGetJavaDate() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        Date date = dt.getJavaDate();
        long expected = (utcTime - 116444736000000000L) / 10000;
        Assert.assertEquals(date.getTime(), expected);
    }

    @Test
    public void testGetJavaInstant() {
        long utcTime = 123456789012345678L;
        DateTime dt = new DateTime(utcTime);
        Instant instant = dt.getJavaInstant();
        long expectedSeconds = (utcTime - 116444736000000000L) / 10000;
        long expectedNanos = (utcTime % 10000000) * 100;
        Assert.assertEquals(instant.getEpochSecond(), expectedSeconds);
        Assert.assertEquals(instant.getNano(), expectedNanos);
    }

    @Test
    public void testIsNull() {
        DateTime dt = new DateTime(0L);
        Assert.assertTrue(dt.isNull());

        DateTime dt2 = new DateTime(1L);
        Assert.assertFalse(dt2.isNull());
    }

    @Test
    public void testEquals() {
        DateTime dt1 = new DateTime(123456789012345678L);
        DateTime dt2 = new DateTime(123456789012345678L);
        DateTime dt3 = new DateTime(123456789012345679L);

        Assert.assertEquals(dt1, dt2);
        Assert.assertNotEquals(dt1, dt3);
        Assert.assertNotEquals(dt1, null);
        Assert.assertNotEquals(dt1, "not a datetime");
    }

    @Test
    public void testHashCode() {
        DateTime dt1 = new DateTime(123456789012345678L);
        DateTime dt2 = new DateTime(123456789012345678L);
        Assert.assertEquals(dt1.hashCode(), dt2.hashCode());
    }

    @Test
    public void testToString() {
        DateTime dt = new DateTime(123456789012345678L);
        String str = dt.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("utcTime=123456789012345678"));
    }

    @Test
    public void testNow() {
        DateTime dt = DateTime.now();
        Assert.assertNotNull(dt);
        Assert.assertTrue(dt.getUtcTime() > 0);
    }

    @Test
    public void testNowNanos() {
        DateTime dt = DateTime.nowNanos();
        Assert.assertNotNull(dt);
        Assert.assertTrue(dt.getUtcTime() > 0);
    }

    @Test
    public void testMinValue() {
        Assert.assertEquals(DateTime.MIN_VALUE.getUtcTime(), 0L);
    }

    @Test
    public void testNullValue() {
        Assert.assertEquals(DateTime.NULL_VALUE.getUtcTime(), 0L);
    }

    @Test
    public void testJavaToUtc() {
        long javaTime = 1234567890123L;
        long expected = javaTime * 10000 + 116444736000000000L;
        DateTime dt = new DateTime(javaTime);
        Assert.assertEquals(dt.getUtcTime(), expected);
    }

    @Test
    public void testUtcToJava() {
        long utcTime = 123456789012345678L;
        long expected = (utcTime - 116444736000000000L) / 10000;
        DateTime dt = new DateTime(utcTime);
        Assert.assertEquals(dt.getJavaTime(), expected);
    }
}
