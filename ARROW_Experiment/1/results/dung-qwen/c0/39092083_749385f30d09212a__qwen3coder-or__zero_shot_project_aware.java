package org.yes.cart.utils;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.time.*;
import java.util.*;

public class DateUtils_ArrowTest {

    private static final long TEST_MILLIS = 1514764800000L; // 2018-01-01 00:00:00 UTC

    @Before
    public void setUp() {
        // Ensure we're using a fixed time for testing
        // This is just to ensure the tests are deterministic
    }

    @Test
    public void testZone() {
        assertNotNull(DateUtils.zone());
    }

    @Test
    public void testExportFileTimestampNow() {
        String result = DateUtils.exportFileTimestamp();
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testExportFileTimestampInstant() {
        Instant instant = Instant.ofEpochMilli(TEST_MILLIS);
        String result = DateUtils.exportFileTimestamp(instant);
        assertEquals("20180101000000", result);
    }

    @Test
    public void testExportFileTimestampLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        String result = DateUtils.exportFileTimestamp(ldt);
        assertEquals("20180101000000", result);
    }

    @Test
    public void testExportFileTimestampZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone());
        String result = DateUtils.exportFileTimestamp(zdt);
        assertEquals("20180101000000", result);
    }

    @Test
    public void testNumberTimestampInstant() {
        Instant instant = Instant.ofEpochMilli(TEST_MILLIS);
        String result = DateUtils.numberTimestamp(instant);
        assertEquals("20180101000000", result);
    }

    @Test
    public void testNumberTimestampLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        String result = DateUtils.numberTimestamp(ldt);
        assertEquals("20180101000000", result);
    }

    @Test
    public void testNumberTimestampZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone());
        String result = DateUtils.numberTimestamp(zdt);
        assertEquals("20180101000000", result);
    }

    @Test
    public void testImpexFileTimestampNow() {
        String result = DateUtils.impexFileTimestamp();
        assertNotNull(result);
        assertTrue(result.matches("20\\d{2}-\\d{2}-\\d{2}_\\d{4}"));
    }

    @Test
    public void testImpexFileTimestampInstant() {
        Instant instant = Instant.ofEpochMilli(TEST_MILLIS);
        String result = DateUtils.impexFileTimestamp(instant);
        assertEquals("2018-01-01_000000", result);
    }

    @Test
    public void testImpexFileTimestampLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        String result = DateUtils.impexFileTimestamp(ldt);
        assertEquals("2018-01-01_000000", result);
    }

    @Test
    public void testImpexFileTimestampZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone());
        String result = DateUtils.impexFileTimestamp(zdt);
        assertEquals("2018-01-01_000000", result);
    }

    @Test
    public void testAutoImportTimestampNow() {
        String result = DateUtils.autoImportTimestamp();
        assertNotNull(result);
        assertTrue(result.matches("_20\\d{2}-\\d{2}-\\d{2}_\\d{4}_\\d{3}"));
    }

    @Test
    public void testAutoImportTimestampZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone());
        String result = DateUtils.autoImportTimestamp(zdt);
        assertEquals("_2018-01-01_000000_000", result);
    }

    @Test
    public void testLdFromMillis() {
        LocalDate result = DateUtils.ldFrom(TEST_MILLIS);
        assertEquals(LocalDate.of(2018, 1, 1), result);
    }

    @Test
    public void testLdtFromMillis() {
        LocalDateTime result = DateUtils.ldtFrom(TEST_MILLIS);
        assertEquals(LocalDateTime.of(2018, 1, 1, 0, 0, 0), result);
    }

    @Test
    public void testZdtFromMillis() {
        ZonedDateTime result = DateUtils.zdtFrom(TEST_MILLIS);
        assertEquals(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone()), result);
    }

    @Test
    public void testIFromMillis() {
        Instant result = DateUtils.iFrom(TEST_MILLIS);
        assertEquals(Instant.ofEpochMilli(TEST_MILLIS), result);
    }

    @Test
    public void testIFromLocalDate() {
        LocalDate localDate = LocalDate.of(2018, 1, 1);
        Instant result = DateUtils.iFrom(localDate);
        assertNotNull(result);
    }

    @Test
    public void testIFromLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        Instant result = DateUtils.iFrom(localDateTime);
        assertNotNull(result);
    }

    @Test
    public void testIFromZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone());
        Instant result = DateUtils.iFrom(zonedDateTime);
        assertNotNull(result);
    }

    @Test
    public void testMillisLocalDate() {
        LocalDate localDate = LocalDate.of(2018, 1, 1);
        long result = DateUtils.millis(localDate);
        assertEquals(TEST_MILLIS, result);
    }

    @Test
    public void testMillisLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0);
        long result = DateUtils.millis(localDateTime);
        assertEquals(TEST_MILLIS, result);
    }

    @Test
    public void testMillisZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone());
        long result = DateUtils.millis(zonedDateTime);
        assertEquals(TEST_MILLIS, result);
    }

    @Test
    public void testIParseValidStringWithTime() {
        String datetime = "2018-01-01 12:30:45";
        Instant result = DateUtils.iParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
    }

    @Test
    public void testIParseValidStringWithoutTime() {
        String datetime = "2018-01-01";
        Instant result = DateUtils.iParse(datetime, "yyyy-MM-dd");
        assertNotNull(result);
    }

    @Test
    public void testIParseInvalidString() {
        String datetime = "invalid-date";
        Instant result = DateUtils.iParse(datetime, "yyyy-MM-dd");
        assertNull(result);
    }

    @Test
    public void testLdParseValidString() {
        String datetime = "2018-01-01";
        LocalDate result = DateUtils.ldParse(datetime, "yyyy-MM-dd");
        assertEquals(LocalDate.of(2018, 1, 1), result);
    }

    @Test
    public void testLdParseInvalidString() {
        String datetime = "invalid-date";
        LocalDate result = DateUtils.ldParse(datetime, "yyyy-MM-dd");
        assertNull(result);
    }

    @Test
    public void testLdtParseValidStringWithTime() {
        String datetime = "2018-01-01 12:30:45";
        LocalDateTime result = DateUtils.ldtParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertEquals(LocalDateTime.of(2018, 1, 1, 12, 30, 45), result);
    }

    @Test
    public void testLdtParseValidStringWithoutTime() {
        String datetime = "2018-01-01";
        LocalDateTime result = DateUtils.ldtParse(datetime, "yyyy-MM-dd");
        assertEquals(LocalDateTime.of(2018, 1, 1, 0, 0, 0), result);
    }

    @Test
    public void testLdtParseInvalidString() {
        String datetime = "invalid-date";
        LocalDateTime result = DateUtils.ldtParse(datetime, "yyyy-MM-dd");
        assertNull(result);
    }

    @Test
    public void testZdtParseValidStringWithTime() {
        String datetime = "2018-01-01 12:30:45";
        ZonedDateTime result = DateUtils.zdtParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertEquals(ZonedDateTime.of(2018, 1, 1, 12, 30, 45, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtParseValidStringWithoutTime() {
        String datetime = "2018-01-01";
        ZonedDateTime result = DateUtils.zdtParse(datetime, "yyyy-MM-dd");
        assertEquals(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtParseInvalidString() {
        String datetime = "invalid-date";
        ZonedDateTime result = DateUtils.zdtParse(datetime, "yyyy-MM-dd");
        assertNull(result);
    }

    @Test
    public void testIParseSDTValidStringWithTime() {
        String datetime = "2018-01-01 12:30:45";
        Instant result = DateUtils.iParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testIParseSDTValidStringWithoutTime() {
        String datetime = "2018-01-01";
        Instant result = DateUtils.iParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testIParseSDTInvalidString() {
        String datetime = "invalid-date";
        Instant result = DateUtils.iParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testLdParseSDTValidString() {
        String datetime = "2018-01-01";
        LocalDate result = DateUtils.ldParseSDT(datetime);
        assertEquals(LocalDate.of(2018, 1, 1), result);
    }

    @Test
    public void testLdParseSDTInvalidString() {
        String datetime = "invalid-date";
        LocalDate result = DateUtils.ldParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testLdtParseSDTValidStringWithTime() {
        String datetime = "2018-01-01 12:30:45";
        LocalDateTime result = DateUtils.ldtParseSDT(datetime);
        assertEquals(LocalDateTime.of(2018, 1, 1, 12, 30, 45), result);
    }

    @Test
    public void testLdtParseSDTValidStringWithoutTime() {
        String datetime = "2018-01-01";
        LocalDateTime result = DateUtils.ldtParseSDT(datetime);
        assertEquals(LocalDateTime.of(2018, 1, 1, 0, 0, 0), result);
    }

    @Test
    public void testLdtParseSDTInvalidString() {
        String datetime = "invalid-date";
        LocalDateTime result = DateUtils.ldtParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testZdtParseSDTValidStringWithTime() {
        String datetime = "2018-01-01 12:30:45";
        ZonedDateTime result = DateUtils.zdtParseSDT(datetime);
        assertEquals(ZonedDateTime.of(2018, 1, 1, 12, 30, 45, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtParseSDTValidStringWithoutTime() {
        String datetime = "2018-01-01";
        ZonedDateTime result = DateUtils.zdtParseSDT(datetime);
        assertEquals(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtParseSDTInvalidString() {
        String datetime = "invalid-date";
        ZonedDateTime result = DateUtils.zdtParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testFormatInstantWithPattern() {
        Instant instant = Instant.ofEpochMilli(TEST_MILLIS);
        String result = DateUtils.format(instant, "yyyy-MM-dd");
        assertEquals("2018-01-01", result);
    }

    @Test
    public void testFormatLocalDateWithPattern() {
        LocalDate date = LocalDate.of(2018, 1, 1);
        String result = DateUtils.format(date, "yyyy-MM-dd");
        assertEquals("2018-01-01", result);
    }

    @Test
    public void testFormatLocalDateTimeWithPattern() {
        LocalDateTime datetime = LocalDateTime.of(2018, 1, 1, 12, 30, 45);
        String result = DateUtils.format(datetime, "yyyy-MM-dd HH:mm:ss");
        assertEquals("2018-01-01 12:30:45", result);
    }

    @Test
    public void testFormatZonedDateTimeWithPattern() {
        ZonedDateTime datetime = ZonedDateTime.of(2018, 1, 1, 12, 30, 45, 0, DateUtils.zone());
        String result = DateUtils.format(datetime, "yyyy-MM-dd HH:mm:ss");
        assertEquals("2018-01-01 12:30:45", result);
    }

    @Test
    public void testFormatSDTNow() {
        String result = DateUtils.formatSDT();
        assertNotNull(result);
        assertTrue(result.matches("20\\d{2}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    public void testFormatSDTInstant() {
        Instant instant = Instant.ofEpochMilli(TEST_MILLIS);
        String result = DateUtils.formatSDT(instant);
        assertNotNull(result);
        assertTrue(result.startsWith("1514764800000"));
    }

    @Test
    public void testFormatSDTLocalDate() {
        LocalDate date = LocalDate.of(2018, 1, 1);
        String result = DateUtils.formatSDT(date);
        assertEquals("2018-01-01 00:00:00", result);
    }

    @Test
    public void testFormatSDTLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.of(2018, 1, 1, 12, 30, 45);
        String result = DateUtils.formatSDT(datetime);
        assertEquals("2018-01-01 12:30:45", result);
    }

    @Test
    public void testFormatSDTZonedDateTime() {
        ZonedDateTime datetime = ZonedDateTime.of(2018, 1, 1, 12, 30, 45, 0, DateUtils.zone());
        String result = DateUtils.formatSDT(datetime);
        assertEquals("2018-01-01 12:30:45", result);
    }

    @Test
    public void testFormatSDLocalDate() {
        LocalDate date = LocalDate.of(2018, 1, 1);
        String result = DateUtils.formatSD(date);
        assertEquals("2018-01-01", result);
    }

    @Test
    public void testFormatSDLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.of(2018, 1, 1, 12, 30, 45);
        String result = DateUtils.formatSD(datetime);
        assertEquals("2018-01-01", result);
    }

    @Test
    public void testFormatSDZonedDateTime() {
        ZonedDateTime datetime = ZonedDateTime.of(2018, 1, 1, 12, 30, 45, 0, DateUtils.zone());
        String result = DateUtils.formatSD(datetime);
        assertEquals("2018-01-01", result);
    }

    @Test
    public void testFormatYear() {
        String result = DateUtils.formatYear();
        assertNotNull(result);
        assertTrue(result.matches("20\\d{2}"));
    }

    @Test
    public void testFormatCustomerInstant() {
        Instant instant = Instant.ofEpochMilli(TEST_MILLIS);
        Locale locale = Locale.ENGLISH;
        String result = DateUtils.formatCustomer(instant, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerLocalDate() {
        LocalDate date = LocalDate.of(2018, 1, 1);
        Locale locale = Locale.ENGLISH;
        String result = DateUtils.formatCustomer(date, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.of(2018, 1, 1, 12, 30, 45);
        Locale locale = Locale.ENGLISH;
        String result = DateUtils.formatCustomer(datetime, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerZonedDateTime() {
        ZonedDateTime datetime = ZonedDateTime.of(2018, 1, 1, 12, 30, 45, 0, DateUtils.zone());
        Locale locale = Locale.ENGLISH;
        String result = DateUtils.formatCustomer(datetime, locale);
        assertNotNull(result);
    }

    @Test
    public void testLdtAtStartOfYear() {
        LocalDateTime date = LocalDateTime.of(2018, 6, 15, 12, 30, 45);
        LocalDateTime result = DateUtils.ldtAtStartOfYear(date);
        assertEquals(LocalDateTime.of(2018, 1, 1, 0, 0, 0), result);
    }

    @Test
    public void testLdtAtStartOfYearNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfYear(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfYear() {
        ZonedDateTime date = ZonedDateTime.of(2018, 6, 15, 12, 30, 45, 0, DateUtils.zone());
        ZonedDateTime result = DateUtils.zdtAtStartOfYear(date);
        assertEquals(ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtAtStartOfYearNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfYear(null);
        assertNull(result);
    }

    @Test
    public void testLdtAtStartOfMonth() {
        LocalDateTime date = LocalDateTime.of(2018, 6, 15, 12, 30, 45);
        LocalDateTime result = DateUtils.ldtAtStartOfMonth(date);
        assertEquals(LocalDateTime.of(2018, 6, 1, 0, 0, 0), result);
    }

    @Test
    public void testLdtAtStartOfMonthNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfMonth(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfMonth() {
        ZonedDateTime date = ZonedDateTime.of(2018, 6, 15, 12, 30, 45, 0, DateUtils.zone());
        ZonedDateTime result = DateUtils.zdtAtStartOfMonth(date);
        assertEquals(ZonedDateTime.of(2018, 6, 1, 0, 0, 0, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtAtStartOfMonthNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfMonth(null);
        assertNull(result);
    }

    @Test
    public void testLdtAtStartOfWeek() {
        LocalDateTime date = LocalDateTime.of(2018, 6, 15, 12, 30, 45); // Friday
        LocalDateTime result = DateUtils.ldtAtStartOfWeek(date);
        assertEquals(LocalDateTime.of(2018, 6, 11, 0, 0, 0), result); // Monday
    }

    @Test
    public void testLdtAtStartOfWeekNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfWeek(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfWeek() {
        ZonedDateTime date = ZonedDateTime.of(2018, 6, 15, 12, 30, 45, 0, DateUtils.zone()); // Friday
        ZonedDateTime result = DateUtils.zdtAtStartOfWeek(date);
        assertEquals(ZonedDateTime.of(2018, 6, 11, 0, 0, 0, 0, DateUtils.zone()), result); // Monday
    }

    @Test
    public void testZdtAtStartOfWeekNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfWeek(null);
        assertNull(result);
    }

    @Test
    public void testLdtAtStartOfDay() {
        LocalDateTime date = LocalDateTime.of(2018, 6, 15, 12, 30, 45);
        LocalDateTime result = DateUtils.ldtAtStartOfDay(date);
        assertEquals(LocalDateTime.of(2018, 6, 15, 0, 0, 0), result);
    }

    @Test
    public void testLdtAtStartOfDayNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfDay(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfDay() {
        ZonedDateTime date = ZonedDateTime.of(2018, 6, 15, 12, 30, 45, 0, DateUtils.zone());
        ZonedDateTime result = DateUtils.zdtAtStartOfDay(date);
        assertEquals(ZonedDateTime.of(2018, 6, 15, 0, 0, 0, 0, DateUtils.zone()), result);
    }

    @Test
    public void testZdtAtStartOfDayNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfDay(null);
        assertNull(result);
    }

    @Test
    public void testFromCalendarDaysOfWeekToISO() {
        List<Integer> calendarDays = Arrays.asList(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY);
        List<DayOfWeek> result = DateUtils.fromCalendarDaysOfWeekToISO(calendarDays);
        assertEquals(3, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0));
        assertEquals(DayOfWeek.WEDNESDAY, result.get(1));
        assertEquals(DayOfWeek.FRIDAY, result.get(2));
    }

    @Test
    public void testFromCalendarDaysOfWeekToISONull() {
        List<DayOfWeek> result = DateUtils.fromCalendarDaysOfWeekToISO(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFromISOtoCalendarDayOfWeek() {
        int result = DateUtils.fromISOtoCalendarDayOfWeek(DayOfWeek.MONDAY);
        assertEquals(Calendar.MONDAY, result);
    }

    @Test
    public void testFromISOtoCalendarDayOfWeekNull() {
        int result = DateUtils.fromISOtoCalendarDayOfWeek(null);
        assertEquals(-1, result);
    }
}
