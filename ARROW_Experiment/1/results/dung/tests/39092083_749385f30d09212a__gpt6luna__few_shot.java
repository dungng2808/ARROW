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

public class DateUtils_ArrowTest {

    @Test
    public void formatsAndParsesStandardDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 5, 14, 6, 7);
        assertEquals("2024-03-05 14:06:07", DateUtils.formatSDT(dateTime));
        assertEquals(dateTime, DateUtils.ldtParseSDT("2024-03-05 14:06:07"));
        assertEquals(LocalDate.of(2024, 3, 5), DateUtils.ldParseSDT("2024-03-05 14:06:07"));
    }

    @Test
    public void parsesDateOnlyAsStartOfDay() {
        assertEquals(LocalDateTime.of(2024, 3, 5, 0, 0),
                DateUtils.ldtParseSDT("2024-03-05"));
        assertEquals(LocalDate.of(2024, 3, 5), DateUtils.ldParseSDT("2024-03-05"));
    }

    @Test
    public void formatsAndParsesCustomPattern() {
        LocalDate date = LocalDate.of(2024, 3, 5);
        assertEquals("05/03/2024", DateUtils.format(date, "dd/MM/yyyy"));
        assertEquals(date, DateUtils.ldParse("05/03/2024", "dd/MM/yyyy"));
    }

    @Test
    public void formatsTimestamps() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 5, 14, 6, 7);
        assertEquals("20240305140607", DateUtils.numberTimestamp(dateTime));
        assertEquals("2024-03-05_140607", DateUtils.impexFileTimestamp(dateTime));
        assertEquals("20240305140607", DateUtils.exportFileTimestamp(dateTime));
    }

    @Test
    public void formatsAutoImportTimestamp() {
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 3, 5, 14, 6, 7, 123000000, ZoneId.of("UTC"));
        assertEquals("_2024-03-05_140607_123", DateUtils.autoImportTimestamp(dateTime));
    }

    @Test
    public void convertsBetweenDateTimeTypes() {
        LocalDate date = LocalDate.of(2024, 3, 5);
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 5, 14, 6, 7);
        ZonedDateTime zonedDateTime = dateTime.atZone(DateUtils.zone());

        assertEquals(date.atStartOfDay(DateUtils.zone()).toInstant(), DateUtils.iFrom(date));
        assertEquals(dateTime.atZone(DateUtils.zone()).toInstant(), DateUtils.iFrom(dateTime));
        assertEquals(zonedDateTime.toInstant(), DateUtils.iFrom(zonedDateTime));
        assertEquals(Instant.ofEpochMilli(1234L), DateUtils.iFrom(1234L));
        assertEquals(dateTime, DateUtils.ldtFrom(DateUtils.millis(dateTime)));
        assertEquals(date, DateUtils.ldFrom(DateUtils.millis(date)));
        assertEquals(zonedDateTime, DateUtils.zdtFrom(DateUtils.millis(zonedDateTime)));
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
    public void formatsNullValuesAsNull() {
        assertNull(DateUtils.format((Instant) null, "yyyy-MM-dd"));
        assertNull(DateUtils.format((LocalDate) null, "yyyy-MM-dd"));
        assertNull(DateUtils.format((LocalDateTime) null, "yyyy-MM-dd"));
        assertNull(DateUtils.format((ZonedDateTime) null, "yyyy-MM-dd"));
        assertNull(DateUtils.formatSDT((Instant) null));
        assertNull(DateUtils.formatSDT((LocalDate) null));
        assertNull(DateUtils.formatSDT((LocalDateTime) null));
        assertNull(DateUtils.formatSDT((ZonedDateTime) null));
        assertNull(DateUtils.formatSD((LocalDate) null));
    }

    @Test
    public void invalidAndBlankInputsParseAsNull() {
        assertNull(DateUtils.ldParseSDT("not a date"));
        assertNull(DateUtils.ldtParseSDT(""));
        assertNull(DateUtils.zdtParseSDT("   "));
        assertNull(DateUtils.iParse("not a date", "yyyy-MM-dd"));
    }

    @Test
    public void parsesNumericInstantAndFormattedInstant() {
        Instant numeric = Instant.ofEpochMilli(1700000000000L);
        assertEquals(numeric, DateUtils.iParse("1700000000000", "yyyy-MM-dd"));
        assertEquals(LocalDate.of(2024, 3, 5).atStartOfDay(DateUtils.zone()).toInstant(),
                DateUtils.iParseSDT("2024-03-05"));
    }

    @Test
    public void formatsInstantWithStandardDateTimeRepresentation() {
        Instant instant = LocalDateTime.of(2024, 3, 5, 14, 6, 7)
                .atZone(DateUtils.zone()).toInstant();
        assertEquals(instant.toEpochMilli() + " //" + ZonedDateTime.ofInstant(instant, DateUtils.zone()),
                DateUtils.formatSDT(instant));
    }

    @Test
    public void formatsCustomerDateUsingLocale() {
        assertEquals("05 March 2024",
                DateUtils.formatCustomer(LocalDate.of(2024, 3, 5), Locale.ENGLISH));
    }

    @Test
    public void findsStartsOfYearMonthWeekAndDay() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 6, 14, 6, 7);
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), DateUtils.ldtAtStartOfYear(dateTime));
        assertEquals(LocalDateTime.of(2024, 3, 1, 0, 0), DateUtils.ldtAtStartOfMonth(dateTime));
        assertEquals(LocalDateTime.of(2024, 3, 4, 0, 0), DateUtils.ldtAtStartOfWeek(dateTime));
        assertEquals(LocalDateTime.of(2024, 3, 6, 0, 0), DateUtils.ldtAtStartOfDay(dateTime));
    }

    @Test
    public void findsZonedStartsOfYearMonthWeekAndDay() {
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 3, 6, 14, 6, 7, 0, ZoneId.of("UTC"));
        assertEquals(dateTime.withDayOfYear(1).toLocalDate().atStartOfDay(ZoneId.of("UTC")),
                DateUtils.zdtAtStartOfYear(dateTime));
        assertEquals(ZonedDateTime.of(2024, 3, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                DateUtils.zdtAtStartOfMonth(dateTime));
        assertEquals(ZonedDateTime.of(2024, 3, 4, 0, 0, 0, 0, ZoneId.of("UTC")),
                DateUtils.zdtAtStartOfWeek(dateTime));
        assertEquals(ZonedDateTime.of(2024, 3, 6, 0, 0, 0, 0, ZoneId.of("UTC")),
                DateUtils.zdtAtStartOfDay(dateTime));
    }

    @Test
    public void startAdjustmentsPreserveNull() {
        assertNull(DateUtils.ldtAtStartOfYear(null));
        assertNull(DateUtils.ldtAtStartOfMonth(null));
        assertNull(DateUtils.ldtAtStartOfWeek(null));
        assertNull(DateUtils.ldtAtStartOfDay((LocalDateTime) null));
        assertNull(DateUtils.zdtAtStartOfYear(null));
        assertNull(DateUtils.zdtAtStartOfMonth(null));
        assertNull(DateUtils.zdtAtStartOfWeek(null));
        assertNull(DateUtils.zdtAtStartOfDay((ZonedDateTime) null));
    }

    @Test
    public void convertsCalendarDaysToIsoDays() {
        assertEquals(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                DateUtils.fromCalendarDaysOfWeekToISO(
                        Arrays.asList(Calendar.SUNDAY, Calendar.MONDAY, Calendar.WEDNESDAY)));
        assertEquals(Collections.emptyList(), DateUtils.fromCalendarDaysOfWeekToISO(null));
    }

    @Test
    public void convertsIsoDaysToCalendarDays() {
        assertEquals(Calendar.MONDAY, DateUtils.fromISOtoCalendarDayOfWeek(DayOfWeek.MONDAY));
        assertEquals(Calendar.SUNDAY, DateUtils.fromISOtoCalendarDayOfWeek(DayOfWeek.SUNDAY));
        assertEquals(-1, DateUtils.fromISOtoCalendarDayOfWeek(null));
    }
}
