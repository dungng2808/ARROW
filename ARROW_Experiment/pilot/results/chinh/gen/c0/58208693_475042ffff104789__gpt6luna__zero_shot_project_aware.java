package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateTime_ArrowTest {

    @Test
    public void testLongConstructorAndTimeConversions() {
        DateTime dateTime = new DateTime(116444736000000000L + 123450000L);

        Assert.assertEquals(dateTime.getUtcTime(), 116444736000123450L);
        Assert.assertEquals(dateTime.getJavaTime(), 12L);
        Assert.assertEquals(dateTime.getJavaDate(), new Date(12L));
        Assert.assertEquals(dateTime.getJavaInstant(), Instant.ofEpochSecond(0, 1_234_500L));
    }

    @Test
    public void testDateConstructor() {
        DateTime dateTime = new DateTime(new Date(1234L));

        Assert.assertEquals(dateTime.getUtcTime(), 116444736000000000L + 1234L * 10_000L);
        Assert.assertEquals(dateTime.getJavaTime(), 1234L);
    }

    @Test
    public void testInstantConstructorIncludesSubmillisecondPrecision() {
        DateTime dateTime = new DateTime(Instant.ofEpochSecond(1L, 234_567_800L));

        Assert.assertEquals(dateTime.getUtcTime(), 116444736000000000L + 12_345_678L);
        Assert.assertEquals(dateTime.getJavaInstant(), Instant.ofEpochSecond(1L, 234_567_800L));
    }

    @Test
    public void testNullValue() {
        Assert.assertSame(DateTime.NULL_VALUE, DateTime.MIN_VALUE);
        Assert.assertTrue(DateTime.MIN_VALUE.isNull());
        Assert.assertEquals(DateTime.MIN_VALUE.getUtcTime(), 0L);
        Assert.assertEquals(DateTime.MIN_VALUE.getJavaTime(), -11644473600000L);
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTime first = new DateTime(42L);
        DateTime second = new DateTime(42L);
        DateTime different = new DateTime(43L);

        Assert.assertTrue(first.equals(second));
        Assert.assertEquals(first.hashCode(), second.hashCode());
        Assert.assertFalse(first.equals(different));
        Assert.assertFalse(first.equals(null));
        Assert.assertFalse(first.equals(Long.valueOf(42L)));
    }

    @Test
    public void testToString() {
        DateTime dateTime = new DateTime(116444736000000000L);

        Assert.assertEquals(dateTime.toString(),
            "DateTime{utcTime=116444736000000000, javaDate=Thu Jan 01 00:00:00 UTC 1970}");
    }

    @Test
    public void testNowReturnsDateTime() {
        Assert.assertNotNull(DateTime.now());
    }

    @Test
    public void testNowNanosReturnsDateTime() {
        Assert.assertNotNull(DateTime.nowNanos());
    }
}
