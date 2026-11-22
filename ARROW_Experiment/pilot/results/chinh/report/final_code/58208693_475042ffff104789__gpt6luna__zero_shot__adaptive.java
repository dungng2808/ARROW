package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DateTime_ArrowTest {

    @Test
    public void convertsUtcTimeToJavaTime() {
        DateTime dateTime = new DateTime(116444736000000000L + 123450000L);

        assertEquals(dateTime.getJavaTime(), 12345L);
    }

    @Test
    public void convertsJavaTimeToUtcTime() {
        DateTime dateTime = new DateTime(new Date(12345L));

        assertEquals(dateTime.getUtcTime(), 116444736000000000L + 123450000L);
    }

    @Test
    public void convertsInstantToUtcTimeIncludingSubMillisecondPrecision() {
        Instant instant = Instant.ofEpochSecond(12L, 345678900L);
        DateTime dateTime = new DateTime(instant);

        assertEquals(dateTime.getUtcTime(), 116444736000000000L + 123456789L);
    }

    @Test
    public void convertsToJavaDate() {
        DateTime dateTime = new DateTime(116444736000000000L + 123450000L);

        assertEquals(dateTime.getJavaDate(), new Date(12345L));
    }

    @Test
    public void convertsToJavaInstant() {
        DateTime dateTime = new DateTime(116444736000000000L + 123456789L);

        assertEquals(dateTime.getJavaInstant(), Instant.ofEpochSecond(12L, 345678900L));
    }

    @Test
    public void zeroUtcTimeIsNullValue() {
        DateTime dateTime = new DateTime(0L);

        assertTrue(dateTime.isNull());
        assertEquals(dateTime, DateTime.MIN_VALUE);
        assertEquals(dateTime, DateTime.NULL_VALUE);
    }

    @Test
    public void nonzeroUtcTimeIsNotNullValue() {
        DateTime dateTime = new DateTime(1L);

        assertFalse(dateTime.isNull());
    }

    @Test
    public void equalValuesHaveEqualHashCodes() {
        DateTime first = new DateTime(123456789L);
        DateTime second = new DateTime(123456789L);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentValuesAreNotEqual() {
        assertNotEquals(new DateTime(1L), new DateTime(2L));
    }

    @Test
    public void doesNotEqualNullOrAnotherType() {
        DateTime dateTime = new DateTime(1L);

        assertNotEquals(dateTime, null);
        assertNotEquals(dateTime, "1");
    }

    @Test
    public void formatsUtcTimeAndJavaDate() {
        DateTime dateTime = new DateTime(116444736000000000L);

        assertEquals(dateTime.toString(), "DateTime{utcTime=116444736000000000, javaDate=Thu Jan 01 00:00:00 UTC 1970}");
    }

    @Test
    public void nowReturnsDateTime() {
        assertNotNull(DateTime.now());
    }

    @Test
    public void nowNanosReturnsDateTime() {
        assertNotNull(DateTime.nowNanos());
    }
}
