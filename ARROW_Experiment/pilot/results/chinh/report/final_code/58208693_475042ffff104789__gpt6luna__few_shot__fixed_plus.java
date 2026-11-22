package org.eclipse.milo.opcua.stack.core.types.builtin;

import java.time.Instant;
import java.util.Date;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class DateTime_ArrowTest {

    @Test
    public void convertsJavaEpochMillisecondsToUtcTime() {
        DateTime dateTime = new DateTime(0L);
        assertEquals(dateTime.getUtcTime(), 0L);
        assertEquals(dateTime.getJavaTime(), -11644473600000L);
        assertEquals(dateTime.getJavaDate(), new Date(-11644473600000L));
        assertEquals(dateTime.getJavaInstant(), Instant.ofEpochMilli(-11644473600000L));
    }

    @Test
    public void convertsDateToUtcTime() {
        DateTime dateTime = new DateTime(new Date(1234L));
        assertEquals(dateTime.getUtcTime(), 116444736012340000L);
        assertEquals(dateTime.getJavaTime(), 1234L);
        assertEquals(dateTime.getJavaDate(), new Date(1234L));
    }

    @Test
    public void convertsInstantIncludingSubMillisecondPrecision() {
        Instant instant = Instant.ofEpochSecond(1L, 234567800L);
        DateTime dateTime = new DateTime(instant);
        assertEquals(dateTime.getUtcTime(), 116444736012345678L);
        assertEquals(dateTime.getJavaTime(), 1234L);
        assertEquals(dateTime.getJavaInstant(), instant);
    }

    @Test
    public void zeroUtcTimeIsNullValue() {
        DateTime dateTime = new DateTime(0L);
        assertTrue(dateTime.isNull());
        assertEquals(dateTime, DateTime.MIN_VALUE);
        assertEquals(dateTime, DateTime.NULL_VALUE);
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
        assertEquals(dateTime.toString(),
            "DateTime{utcTime=116444736000000000, javaDate=Thu Jan 01 00:00:00 UTC 1970}");
    }
}
