package org.yes.cart.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.*;
import java.util.*;

public class DateUtils_ArrowTest {

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
        Instant instant = Instant.now();
        String result = DateUtils.exportFileTimestamp(instant);
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testExportFileTimestampLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String result = DateUtils.exportFileTimestamp(localDateTime);
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testExportFileTimestampZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String result = DateUtils.exportFileTimestamp(zonedDateTime);
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testNumberTimestampInstant() {
        Instant instant = Instant.now();
        String result = DateUtils.numberTimestamp(instant);
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testNumberTimestampLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String result = DateUtils.numberTimestamp(localDateTime);
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testNumberTimestampZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String result = DateUtils.numberTimestamp(zonedDateTime);
        assertNotNull(result);
        assertEquals(14, result.length());
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    public void testImpexFileTimestampNow() {
        String result = DateUtils.impexFileTimestamp();
        assertNotNull(result);
        assertEquals(13, result.length());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}_\\d{4}\\d{2}"));
    }

    @Test
    public void testImpexFileTimestampInstant() {
        Instant instant = Instant.now();
        String result = DateUtils.impexFileTimestamp(instant);
        assertNotNull(result);
        assertEquals(13, result.length());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}_\\d{4}\\d{2}"));
    }

    @Test
    public void testImpexFileTimestampLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String result = DateUtils.impexFileTimestamp(localDateTime);
        assertNotNull(result);
        assertEquals(13, result.length());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}_\\d{4}\\d{2}"));
    }

    @Test
    public void testImpexFileTimestampZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String result = DateUtils.impexFileTimestamp(zonedDateTime);
        assertNotNull(result);
        assertEquals(13, result.length());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}_\\d{4}\\d{2}"));
    }

    @Test
    public void testAutoImportTimestampNow() {
        String result = DateUtils.autoImportTimestamp();
        assertNotNull(result);
        assertTrue(result.startsWith("_"));
        assertEquals(20, result.length());
        assertTrue(result.matches("_\\d{4}-\\d{2}-\\d{2}_\\d{4}\\d{2}_\\d{3}"));
    }

    @Test
    public void testAutoImportTimestampZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String result = DateUtils.autoImportTimestamp(zonedDateTime);
        assertNotNull(result);
        assertTrue(result.startsWith("_"));
        assertEquals(20, result.length());
        assertTrue(result.matches("_\\d{4}-\\d{2}-\\d{2}_\\d{4}\\d{2}_\\d{3}"));
    }

    @Test
    public void testLdFromMillis() {
        long millis = System.currentTimeMillis();
        LocalDate result = DateUtils.ldFrom(millis);
        assertNotNull(result);
    }

    @Test
    public void testLdtFromMillis() {
        long millis = System.currentTimeMillis();
        LocalDateTime result = DateUtils.ldtFrom(millis);
        assertNotNull(result);
    }

    @Test
    public void testZdtFromMillis() {
        long millis = System.currentTimeMillis();
        ZonedDateTime result = DateUtils.zdtFrom(millis);
        assertNotNull(result);
    }

    @Test
    public void testIFromMillis() {
        long millis = System.currentTimeMillis();
        Instant result = DateUtils.iFrom(millis);
        assertNotNull(result);
    }

    @Test
    public void testIFromLocalDate() {
        LocalDate localDate = LocalDate.now();
        Instant result = DateUtils.iFrom(localDate);
        assertNotNull(result);
    }

    @Test
    public void testIFromLocalDateNull() {
        Instant result = DateUtils.iFrom((LocalDate) null);
        assertNull(result);
    }

    @Test
    public void testIFromLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Instant result = DateUtils.iFrom(localDateTime);
        assertNotNull(result);
    }

    @Test
    public void testIFromLocalDateTimeNull() {
        Instant result = DateUtils.iFrom((LocalDateTime) null);
        assertNull(result);
    }

    @Test
    public void testIFromZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        Instant result = DateUtils.iFrom(zonedDateTime);
        assertNotNull(result);
    }

    @Test
    public void testIFromZonedDateTimeNull() {
        Instant result = DateUtils.iFrom((ZonedDateTime) null);
        assertNull(result);
    }

    @Test
    public void testMillisLocalDate() {
        LocalDate localDate = LocalDate.now();
        long result = DateUtils.millis(localDate);
        assertTrue(result > 0);
    }

    @Test
    public void testMillisLocalDateNull() {
        long result = DateUtils.millis((LocalDate) null);
        assertEquals(0L, result);
    }

    @Test
    public void testMillisLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        long result = DateUtils.millis(localDateTime);
        assertTrue(result > 0);
    }

    @Test
    public void testMillisLocalDateTimeNull() {
        long result = DateUtils.millis((LocalDateTime) null);
        assertEquals(0L, result);
    }

    @Test
    public void testMillisZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        long result = DateUtils.millis(zonedDateTime);
        assertTrue(result > 0);
    }

    @Test
    public void testMillisZonedDateTimeNull() {
        long result = DateUtils.millis((ZonedDateTime) null);
        assertEquals(0L, result);
    }

    @Test
    public void testIParseValid() {
        String datetime = "1234567890123";
        Instant result = DateUtils.iParse(datetime, "yyyyMMddHHmmss");
        assertNotNull(result);
    }

    @Test
    public void testIParseInvalid() {
        String datetime = "invalid";
        Instant result = DateUtils.iParse(datetime, "yyyyMMddHHmmss");
        assertNull(result);
    }

    @Test
    public void testLdParseValid() {
        String datetime = "2023-01-01";
        LocalDate result = DateUtils.ldParse(datetime, "yyyy-MM-dd");
        assertNotNull(result);
    }

    @Test
    public void testLdParseInvalid() {
        String datetime = "invalid";
        LocalDate result = DateUtils.ldParse(datetime, "yyyy-MM-dd");
        assertNull(result);
    }

    @Test
    public void testLdtParseValid() {
        String datetime = "2023-01-01 12:30:45";
        LocalDateTime result = DateUtils.ldtParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
    }

    @Test
    public void testLdtParseInvalid() {
        String datetime = "invalid";
        LocalDateTime result = DateUtils.ldtParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNull(result);
    }

    @Test
    public void testZdtParseValid() {
        String datetime = "2023-01-01 12:30:45";
        ZonedDateTime result = DateUtils.zdtParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
    }

    @Test
    public void testZdtParseInvalid() {
        String datetime = "invalid";
        ZonedDateTime result = DateUtils.zdtParse(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNull(result);
    }

    @Test
    public void testIParseSDTValid() {
        String datetime = "2023-01-01 12:30:45";
        Instant result = DateUtils.iParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testIParseSDTInvalid() {
        String datetime = "invalid";
        Instant result = DateUtils.iParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testLdParseSDTValid() {
        String datetime = "2023-01-01";
        LocalDate result = DateUtils.ldParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testLdParseSDTInvalid() {
        String datetime = "invalid";
        LocalDate result = DateUtils.ldParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testLdtParseSDTValid() {
        String datetime = "2023-01-01 12:30:45";
        LocalDateTime result = DateUtils.ldtParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testLdtParseSDTInvalid() {
        String datetime = "invalid";
        LocalDateTime result = DateUtils.ldtParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testZdtParseSDTValid() {
        String datetime = "2023-01-01 12:30:45";
        ZonedDateTime result = DateUtils.zdtParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testZdtParseSDTInvalid() {
        String datetime = "invalid";
        ZonedDateTime result = DateUtils.zdtParseSDT(datetime);
        assertNull(result);
    }

    @Test
    public void testFormatInstantString() {
        Instant instant = Instant.now();
        String result = DateUtils.format(instant, "yyyyMMddHHmmss");
        assertNotNull(result);
        assertEquals(14, result.length());
    }

    @Test
    public void testFormatLocalDateString() {
        LocalDate date = LocalDate.now();
        String result = DateUtils.format(date, "yyyy-MM-dd");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatLocalDateTimeString() {
        LocalDateTime datetime = LocalDateTime.now();
        String result = DateUtils.format(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatZonedDateTimeString() {
        ZonedDateTime datetime = ZonedDateTime.now();
        String result = DateUtils.format(datetime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDT() {
        String result = DateUtils.formatSDT();
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDTInstant() {
        Instant instant = Instant.now();
        String result = DateUtils.formatSDT(instant);
        assertNotNull(result);
        assertTrue(result.contains(" //"));
    }

    @Test
    public void testFormatSDTLocalDate() {
        LocalDate date = LocalDate.now();
        String result = DateUtils.formatSDT(date);
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDTLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.now();
        String result = DateUtils.formatSDT(datetime);
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDTZonedDateTime() {
        ZonedDateTime datetime = ZonedDateTime.now();
        String result = DateUtils.formatSDT(datetime);
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDLocalDate() {
        LocalDate date = LocalDate.now();
        String result = DateUtils.formatSD(date);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatSDLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.now();
        String result = DateUtils.formatSD(datetime);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatSDZonedDateTime() {
        ZonedDateTime datetime = ZonedDateTime.now();
        String result = DateUtils.formatSD(datetime);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatYear() {
        String result = DateUtils.formatYear();
        assertNotNull(result);
        assertEquals(4, result.length());
    }

    @Test
    public void testFormatCustomerInstant() {
        Instant instant = Instant.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(instant, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerLocalDate() {
        LocalDate date = LocalDate.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(date, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerLocalDateTime() {
        LocalDateTime datetime = LocalDateTime.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(datetime, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerZonedDateTime() {
        ZonedDateTime datetime = ZonedDateTime.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(datetime, locale);
        assertNotNull(result);
    }

    @Test
    public void testLdtAtStartOfYear() {
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime result = DateUtils.ldtAtStartOfYear(date);
        assertNotNull(result);
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testLdtAtStartOfYearNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfYear(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfYear() {
        ZonedDateTime date = ZonedDateTime.now();
        ZonedDateTime result = DateUtils.zdtAtStartOfYear(date);
        assertNotNull(result);
        assertEquals(1, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testZdtAtStartOfYearNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfYear(null);
        assertNull(result);
    }

    @Test
    public void testLdtAtStartOfMonth() {
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime result = DateUtils.ldtAtStartOfMonth(date);
        assertNotNull(result);
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testLdtAtStartOfMonthNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfMonth(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfMonth() {
        ZonedDateTime date = ZonedDateTime.now();
        ZonedDateTime result = DateUtils.zdtAtStartOfMonth(date);
        assertNotNull(result);
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testZdtAtStartOfMonthNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfMonth(null);
        assertNull(result);
    }

    @Test
    public void testLdtAtStartOfWeek() {
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime result = DateUtils.ldtAtStartOfWeek(date);
        assertNotNull(result);
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testLdtAtStartOfWeekNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfWeek(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfWeek() {
        ZonedDateTime date = ZonedDateTime.now();
        ZonedDateTime result = DateUtils.zdtAtStartOfWeek(date);
        assertNotNull(result);
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testZdtAtStartOfWeekNull() {
        ZonedDateTime result = DateUtils.zdtAtStartOfWeek(null);
        assertNull(result);
    }

    @Test
    public void testLdtAtStartOfDay() {
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime result = DateUtils.ldtAtStartOfDay(date);
        assertNotNull(result);
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testLdtAtStartOfDayNull() {
        LocalDateTime result = DateUtils.ldtAtStartOfDay(null);
        assertNull(result);
    }

    @Test
    public void testZdtAtStartOfDay() {
        ZonedDateTime date = ZonedDateTime.now();
        ZonedDateTime result = DateUtils.zdtAtStartOfDay(date);
        assertNotNull(result);
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
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
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(DayOfWeek.MONDAY));
        assertTrue(result.contains(DayOfWeek.WEDNESDAY));
        assertTrue(result.contains(DayOfWeek.FRIDAY));
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
