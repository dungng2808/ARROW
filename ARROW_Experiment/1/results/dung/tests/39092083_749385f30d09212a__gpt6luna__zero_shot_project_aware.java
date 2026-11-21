package org.yes.cart.utils;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DateUtils_ArrowTest {

    @Test
    public void formatsTimestamps() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        assertEquals("20240203040506", DateUtils.numberTimestamp(dateTime));
        assertEquals("2024-02-03_040506", DateUtils.impexFileTimestamp(dateTime));
        assertEquals("_2024-02-03_040506_000",
                DateUtils.autoImportTimestamp(dateTime.atZone(DateUtils.zone())));
    }

    @Test
    public void parsesStandardDateAndDateTime() {
        assertEquals(LocalDate.of(2024, 2, 3), DateUtils.ldParseSDT("2024-02-03"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6),
                DateUtils.ldtParseSDT("2024-02-03 04:05:06"));
        assertEquals(Instant.parse("2024-02-03T00:00:00Z"),
                DateUtils.iParseSDT("2024-02-03").atZone(DateUtils.zone()).toInstant());
    }

    @Test
    public void parsesInvalidAndBlankValuesAsNull() {
        assertNull(DateUtils.ldParseSDT("not a date"));
        assertNull(DateUtils.ldtParseSDT(" "));
        assertNull(DateUtils.iParseSDT(null));
    }

    @Test
    public void parsesAndFormatsCustomPatterns() {
        LocalDate date = LocalDate.of(2024, 2, 3);
        assertEquals(date, DateUtils.ldParse("03/02/2024", "dd/MM/yyyy"));
        assertEquals("03/02/2024", DateUtils.format(date, "dd/MM/yyyy"));
    }

    @Test
    public void convertsEpochMillisAndDateValues() {
        long millis = 1706933106000L;
        assertEquals(Instant.ofEpochMilli(millis), DateUtils.iFrom(millis));
        assertEquals(ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), DateUtils.zone()),
                DateUtils.zdtFrom(millis));
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DateUtils.zone()),
                DateUtils.ldtFrom(millis));
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DateUtils.zone()).toLocalDate(),
                DateUtils.ldFrom(millis));
    }

    @Test
    public void nullConversionsReturnNullOrZero() {
        assertNull(DateUtils.iFrom((LocalDate) null));
        assertNull(DateUtils.iFrom((LocalDateTime) null));
        assertNull(DateUtils.iFrom((ZonedDateTime) null));
        assertEquals(0L, DateUtils.millis((LocalDate) null));
        assertEquals(0L, DateUtils.millis((LocalDateTime) null));
        assertEquals(0L, DateUtils.millis((ZonedDateTime) null));
    }

    @Test
    public void formatsStandardValues() {
        LocalDate date = LocalDate.of(2024, 2, 3);
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        assertEquals("2024-02-03", DateUtils.formatSD(date));
        assertEquals("2024-02-03 04:05:06", DateUtils.formatSDT(dateTime));
        assertEquals("2024-02-03 00:00:00", DateUtils.formatSDT(date));
        assertEquals("2024-02-03", DateUtils.formatSD(dateTime));
    }

    @Test
    public void formatsInstantWithCommentAndNull() {
        Instant instant = Instant.parse("2024-02-03T04:05:06Z");
        assertEquals(instant.toEpochMilli() + " //" + instant.atZone(DateUtils.zone()),
                DateUtils.formatSDT(instant));
        assertNull(DateUtils.formatSDT((Instant) null));
    }

    @Test
    public void formatsCustomerDateInLocale() {
        assertEquals("03 February 2024",
                DateUtils.formatCustomer(LocalDate.of(2024, 2, 3), Locale.ENGLISH));
    }

    @Test
    public void findsStartsOfCalendarPeriods() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 7, 15, 16, 17);
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), DateUtils.ldtAtStartOfYear(dateTime));
        assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0), DateUtils.ldtAtStartOfMonth(dateTime));
        assertEquals(LocalDateTime.of(2024, 2, 5, 0, 0), DateUtils.ldtAtStartOfWeek(dateTime));
        assertEquals(LocalDateTime.of(2024, 2, 7, 0, 0), DateUtils.ldtAtStartOfDay(dateTime));
    }

    @Test
    public void nullPeriodInputsReturnNull() {
        assertNull(DateUtils.ldtAtStartOfYear(null));
        assertNull(DateUtils.zdtAtStartOfMonth(null));
        assertNull(DateUtils.ldtAtStartOfWeek(null));
        assertNull(DateUtils.zdtAtStartOfDay(null));
    }

    @Test
    public void convertsCalendarDaysToIsoDays() {
        assertEquals(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                DateUtils.fromCalendarDaysOfWeekToISO(
                        Arrays.asList(Calendar.SUNDAY, Calendar.MONDAY, Calendar.WEDNESDAY)));
        assertEquals(Collections.emptyList(), DateUtils.fromCalendarDaysOfWeekToISO(null));
        assertEquals(Collections.emptyList(), DateUtils.fromCalendarDaysOfWeekToISO(Collections.<Integer>emptyList()));
    }

    @Test
    public void convertsIsoDaysToCalendarDays() {
        assertEquals(Calendar.MONDAY, DateUtils.fromISOtoCalendarDayOfWeek(DayOfWeek.MONDAY));
        assertEquals(Calendar.SUNDAY, DateUtils.fromISOtoCalendarDayOfWeek(DayOfWeek.SUNDAY));
        assertEquals(-1, DateUtils.fromISOtoCalendarDayOfWeek(null));
    }

    @Test
    public void zoneIsAvailable() {
        assertTrue(DateUtils.zone() instanceof ZoneId);
    }
}
