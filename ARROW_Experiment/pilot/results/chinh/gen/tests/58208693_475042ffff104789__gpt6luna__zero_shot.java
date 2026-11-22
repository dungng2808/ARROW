package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DateTime_ArrowTest {

    @Test
    public void convertsUtcTimeToJavaTime() {
        DateTime dateTime = new DateTime(116444736000000000L + 123450000L);

        assertEquals(12345L, dateTime.getJavaTime());
    }

    @Test
    public void convertsJavaTimeToUtcTime() {
        DateTime dateTime = new DateTime(new Date(12345L));

        assertEquals(116444736000000000L + 123450000L, dateTime.getUtcTime());
    }

    @Test
    public void convertsInstantToUtcTimeIncludingSubMillisecondPrecision() {
        Instant instant = Instant.ofEpochSecond(12L, 345678900L);
        DateTime dateTime = new DateTime(instant);

        assertEquals(116444736000000000L + 123456789L, dateTime.getUtcTime());
    }

    @Test
    public void convertsToJavaDate() {
        DateTime dateTime = new DateTime(116444736000000000L + 123450000L);

        assertEquals(new Date(12345L), dateTime.getJavaDate());
    }

    @Test
    public void convertsToJavaInstant() {
        DateTime dateTime = new DateTime(116444736000000000L + 123456789L);

        assertEquals(Instant.ofEpochSecond(12L, 345678900L), dateTime.getJavaInstant());
    }

    @Test
    public void zeroUtcTimeIsNullValue() {
        DateTime dateTime = new DateTime(0L);

        assertTrue(dateTime.isNull());
        assertEquals(DateTime.MIN_VALUE, dateTime);
        assertEquals(DateTime.NULL_VALUE, dateTime);
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

        assertNotEquals(null, dateTime);
        assertNotEquals("1", dateTime);
    }

    @Test
    public void formatsUtcTimeAndJavaDate() {
        DateTime dateTime = new DateTime(116444736000000000L);

        assertEquals("DateTime{utcTime=116444736000000000, javaDate=Thu Jan 01 00:00:00 UTC 1970}", dateTime.toString());
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
