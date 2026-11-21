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
    public void zoneReturnsSystemDefaultZone() {
        assertEquals(ZoneId.systemDefault(), DateUtils.zone());
    }

    @Test
    public void timestampsFormatDateTimeValues() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        assertEquals("20240203040506", DateUtils.numberTimestamp(dateTime));
        assertEquals("20240203040506", DateUtils.exportFileTimestamp(dateTime));
        assertEquals("2024-02-03_040506", DateUtils.impexFileTimestamp(dateTime));
    }

    @Test
    public void autoImportTimestampFormatsMilliseconds() {
        ZonedDateTime dateTime = ZonedDateTime.of(2024, 2, 3, 4, 5, 6, 123000000, ZoneId.of("UTC"));
        assertEquals("_2024-02-03_040506_123", DateUtils.autoImportTimestamp(dateTime));
    }

    @Test
    public void conversionsFromEpochMillis() {
        long millis = 1706933106000L;
        Instant instant = Instant.ofEpochMilli(millis);
        assertEquals(instant, DateUtils.iFrom(millis));
        assertEquals(LocalDateTime.ofInstant(instant, DateUtils.zone()), DateUtils.ldtFrom(millis));
        assertEquals(LocalDateTime.ofInstant(instant, DateUtils.zone()).toLocalDate(), DateUtils.ldFrom(millis));
        assertEquals(ZonedDateTime.ofInstant(instant, DateUtils.zone()), DateUtils.zdtFrom(millis));
    }

    @Test
    public void instantConversionsAndMillisHandleNull() {
        LocalDate date = LocalDate.of(2024, 2, 3);
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        ZonedDateTime zonedDateTime = dateTime.atZone(DateUtils.zone());

        assertEquals(date.atStartOfDay(DateUtils.zone()).toInstant(), DateUtils.iFrom(date));
        assertEquals(dateTime.atZone(DateUtils.zone()).toInstant(), DateUtils.iFrom(dateTime));
        assertEquals(zonedDateTime.toInstant(), DateUtils.iFrom(zonedDateTime));
        assertNull(DateUtils.iFrom((LocalDate) null));
        assertNull(DateUtils.iFrom((LocalDateTime) null));
        assertNull(DateUtils.iFrom((ZonedDateTime) null));

        assertEquals(date.atStartOfDay(DateUtils.zone()).toInstant().toEpochMilli(), DateUtils.millis(date));
        assertEquals(dateTime.atZone(DateUtils.zone()).toInstant().toEpochMilli(), DateUtils.millis(dateTime));
        assertEquals(zonedDateTime.toInstant().toEpochMilli(), DateUtils.millis(zonedDateTime));
        assertEquals(0L, DateUtils.millis((LocalDate) null));
        assertEquals(0L, DateUtils.millis((LocalDateTime) null));
        assertEquals(0L, DateUtils.millis((ZonedDateTime) null));
    }

    @Test
    public void parsesStandardDateAndDateTime() {
        assertEquals(LocalDate.of(2024, 2, 3), DateUtils.ldParseSDT("2024-02-03"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtils.ldtParseSDT("2024-02-03"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6), DateUtils.ldtParseSDT("2024-02-03 04:05:06"));
        assertEquals(ZonedDateTime.of(2024, 2, 3, 4, 5, 6, 0, DateUtils.zone()),
                DateUtils.zdtParseSDT("2024-02-03 04:05:06"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 4, 5, 6).atZone(DateUtils.zone()).toInstant(),
                DateUtils.iParseSDT("2024-02-03 04:05:06"));
    }

    @Test
    public void parsingHandlesEpochCommentsBlankAndInvalidValues() {
        assertEquals(Instant.ofEpochMilli(1706933106000L),
                DateUtils.iParse("1706933106000 // timestamp", "yyyy-MM-dd"));
        assertNull(DateUtils.iParseSDT(""));
        assertNull(DateUtils.ldParseSDT("   "));
        assertNull(DateUtils.ldtParseSDT("not a date"));
        assertNull(DateUtils.zdtParseSDT("not a date"));
    }

    @Test
    public void customPatternsParseDateAndDateTime() {
        assertEquals(LocalDate.of(2024, 2, 3), DateUtils.ldParse("03/02/2024", "dd/MM/yyyy"));
        assertEquals(LocalDateTime.of(2024, 2, 3, 0, 0), DateUtils.ldtParse("2024-02-03", "yyyy-MM-dd"));
        assertEquals(ZonedDateTime.of(2024, 2, 3, 0, 0, 0, 0, DateUtils.zone()),
                DateUtils.zdtParse("2024-02-03", "yyyy-MM-dd"));
    }

    @Test
    public void formatsValuesWithPatternsAndStandardFormats() {
        LocalDate date = LocalDate.of(2024, 2, 3);
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        ZonedDateTime zonedDateTime = dateTime.atZone(DateUtils.zone());
        Instant instant = dateTime.atZone(DateUtils.zone()).toInstant();

        assertEquals("2024-02-03 04:05:06", DateUtils.format(instant, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("2024-02-03", DateUtils.formatSD(date));
        assertEquals("2024-02-03", DateUtils.formatSD(dateTime));
        assertEquals("2024-02-03", DateUtils.formatSD(zonedDateTime));
        assertEquals("2024-02-03 04:05:06", DateUtils.formatSDT(dateTime));
        assertEquals("2024-02-03 04:05:06", DateUtils.formatSDT(zonedDateTime));
        assertEquals("2024-02-03 04:05:06", DateUtils.formatSDT(instant).substring(15));
        assertNull(DateUtils.format((Instant) null, "yyyy-MM-dd"));
        assertNull(DateUtils.formatSDT((LocalDate) null));
    }

    @Test
    public void customerFormattingUsesLocale() {
        LocalDate date = LocalDate.of(2024, 2, 3);
        assertEquals("03 February 2024", DateUtils.formatCustomer(date, Locale.ENGLISH));
        assertEquals("03 février 2024", DateUtils.formatCustomer(date, Locale.FRENCH));
    }

    @Test
    public void startOfYearMonthWeekAndDay() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 5, 15, 12, 34, 56);
        ZonedDateTime zonedDateTime = dateTime.atZone(DateUtils.zone());

        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), DateUtils.ldtAtStartOfYear(dateTime));
        assertEquals(LocalDateTime.of(2024, 5, 1, 0, 0), DateUtils.ldtAtStartOfMonth(dateTime));
        assertEquals(LocalDateTime.of(2024, 5, 13, 0, 0), DateUtils.ldtAtStartOfWeek(dateTime));
        assertEquals(LocalDateTime.of(2024, 5, 15, 0, 0), DateUtils.ldtAtStartOfDay(dateTime));

        assertEquals(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, DateUtils.zone()),
                DateUtils.zdtAtStartOfYear(zonedDateTime));
        assertEquals(ZonedDateTime.of(2024, 5, 1, 0, 0, 0, 0, DateUtils.zone()),
                DateUtils.zdtAtStartOfMonth(zonedDateTime));
        assertEquals(ZonedDateTime.of(2024, 5, 13, 0, 0, 0, 0, DateUtils.zone()),
                DateUtils.zdtAtStartOfWeek(zonedDateTime));
        assertEquals(ZonedDateTime.of(2024, 5, 15, 0, 0, 0, 0, DateUtils.zone()),
                DateUtils.zdtAtStartOfDay(zonedDateTime));
    }

    @Test
    public void startAdjustmentsReturnNullForNullInput() {
        assertNull(DateUtils.ldtAtStartOfYear(null));
        assertNull(DateUtils.zdtAtStartOfYear(null));
        assertNull(DateUtils.ldtAtStartOfMonth(null));
        assertNull(DateUtils.zdtAtStartOfMonth(null));
        assertNull(DateUtils.ldtAtStartOfWeek(null));
        assertNull(DateUtils.zdtAtStartOfWeek(null));
        assertNull(DateUtils.ldtAtStartOfDay(null));
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
}
