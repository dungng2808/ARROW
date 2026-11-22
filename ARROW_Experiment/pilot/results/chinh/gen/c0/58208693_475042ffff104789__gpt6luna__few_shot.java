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
    public void convertsJavaEpochMillisecondsToUtcTime() {
        DateTime dateTime = new DateTime(0L);
        assertEquals(116444736000000000L, dateTime.getUtcTime());
        assertEquals(0L, dateTime.getJavaTime());
        assertEquals(new Date(0L), dateTime.getJavaDate());
        assertEquals(Instant.EPOCH, dateTime.getJavaInstant());
    }

    @Test
    public void convertsDateToUtcTime() {
        DateTime dateTime = new DateTime(new Date(1234L));
        assertEquals(116444736012340000L, dateTime.getUtcTime());
        assertEquals(1234L, dateTime.getJavaTime());
        assertEquals(new Date(1234L), dateTime.getJavaDate());
    }

    @Test
    public void convertsInstantIncludingSubMillisecondPrecision() {
        Instant instant = Instant.ofEpochSecond(1L, 234567800L);
        DateTime dateTime = new DateTime(instant);
        assertEquals(116444736012345678L, dateTime.getUtcTime());
        assertEquals(1234L, dateTime.getJavaTime());
        assertEquals(instant, dateTime.getJavaInstant());
    }

    @Test
    public void zeroUtcTimeIsNullValue() {
        DateTime dateTime = new DateTime(0L);
        assertTrue(dateTime.isNull());
        assertEquals(DateTime.MIN_VALUE, dateTime);
        assertEquals(DateTime.NULL_VALUE, dateTime);
    }

    @Test
    public void nonzeroUtcTimeIsNotNull() {
        DateTime dateTime = new DateTime(1L);
        assertFalse(dateTime.isNull());
    }

    @Test
    public void equalityAndHashCodeUseUtcTime() {
        DateTime first = new DateTime(42L);
        DateTime equal = new DateTime(42L);
        DateTime different = new DateTime(43L);

        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, new Object());
    }

    @Test
    public void stringRepresentationIncludesUtcTimeAndJavaDate() {
        DateTime dateTime = new DateTime(116444736000000000L);
        assertEquals("DateTime{utcTime=116444736000000000, javaDate=Thu Jan 01 00:00:00 UTC 1970}", dateTime.toString());
    }
}
