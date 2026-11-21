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
    public void testExportFileTimestamp() {
        String timestamp = DateUtils.exportFileTimestamp();
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
        assertTrue(timestamp.matches("\\d{14}"));
    }

    @Test
    public void testExportFileTimestampWithInstant() {
        Instant instant = Instant.now();
        String timestamp = DateUtils.exportFileTimestamp(instant);
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
    }

    @Test
    public void testExportFileTimestampWithLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String timestamp = DateUtils.exportFileTimestamp(localDateTime);
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
    }

    @Test
    public void testExportFileTimestampWithZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String timestamp = DateUtils.exportFileTimestamp(zonedDateTime);
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
    }

    @Test
    public void testNumberTimestamp() {
        String timestamp = DateUtils.numberTimestamp();
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
        assertTrue(timestamp.matches("\\d{14}"));
    }

    @Test
    public void testNumberTimestampWithInstant() {
        Instant instant = Instant.now();
        String timestamp = DateUtils.numberTimestamp(instant);
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
    }

    @Test
    public void testNumberTimestampWithLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String timestamp = DateUtils.numberTimestamp(localDateTime);
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
    }

    @Test
    public void testNumberTimestampWithZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String timestamp = DateUtils.numberTimestamp(zonedDateTime);
        assertNotNull(timestamp);
        assertEquals(14, timestamp.length());
    }

    @Test
    public void testImpexFileTimestamp() {
        String timestamp = DateUtils.impexFileTimestamp();
        assertNotNull(timestamp);
        assertEquals(13, timestamp.length());
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2}_\\d{6}"));
    }

    @Test
    public void testImpexFileTimestampWithInstant() {
        Instant instant = Instant.now();
        String timestamp = DateUtils.impexFileTimestamp(instant);
        assertNotNull(timestamp);
        assertEquals(13, timestamp.length());
    }

    @Test
    public void testImpexFileTimestampWithLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        String timestamp = DateUtils.impexFileTimestamp(localDateTime);
        assertNotNull(timestamp);
        assertEquals(13, timestamp.length());
    }

    @Test
    public void testImpexFileTimestampWithZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String timestamp = DateUtils.impexFileTimestamp(zonedDateTime);
        assertNotNull(timestamp);
        assertEquals(13, timestamp.length());
    }

    @Test
    public void testAutoImportTimestamp() {
        String timestamp = DateUtils.autoImportTimestamp();
        assertNotNull(timestamp);
        assertTrue(timestamp.startsWith("_"));
        assertEquals(20, timestamp.length());
    }

    @Test
    public void testAutoImportTimestampWithZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String timestamp = DateUtils.autoImportTimestamp(zonedDateTime);
        assertNotNull(timestamp);
        assertTrue(timestamp.startsWith("_"));
        assertEquals(20, timestamp.length());
    }

    @Test
    public void testLdFrom() {
        long millis = System.currentTimeMillis();
        LocalDate result = DateUtils.ldFrom(millis);
        assertNotNull(result);
    }

    @Test
    public void testLdtFrom() {
        long millis = System.currentTimeMillis();
        LocalDateTime result = DateUtils.ldtFrom(millis);
        assertNotNull(result);
    }

    @Test
    public void testZdtFrom() {
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
    public void testIFromLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Instant result = DateUtils.iFrom(localDateTime);
        assertNotNull(result);
    }

    @Test
    public void testIFromZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        Instant result = DateUtils.iFrom(zonedDateTime);
        assertNotNull(result);
    }

    @Test
    public void testIFromNull() {
        assertNull(DateUtils.iFrom((LocalDate) null));
        assertNull(DateUtils.iFrom((LocalDateTime) null));
        assertNull(DateUtils.iFrom((ZonedDateTime) null));
    }

    @Test
    public void testMillisLocalDate() {
        LocalDate localDate = LocalDate.now();
        long result = DateUtils.millis(localDate);
        assertTrue(result > 0);
    }

    @Test
    public void testMillisLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        long result = DateUtils.millis(localDateTime);
        assertTrue(result > 0);
    }

    @Test
    public void testMillisZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        long result = DateUtils.millis(zonedDateTime);
        assertTrue(result > 0);
    }

    @Test
    public void testMillisNull() {
        assertEquals(0L, DateUtils.millis((LocalDate) null));
        assertEquals(0L, DateUtils.millis((LocalDateTime) null));
        assertEquals(0L, DateUtils.millis((ZonedDateTime) null));
    }

    @Test
    public void testIParse() {
        String datetime = "2023-01-01 12:00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        Instant result = DateUtils.iParse(datetime, pattern);
        assertNotNull(result);
    }

    @Test
    public void testIParseWithBlankString() {
        String datetime = "";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        Instant result = DateUtils.iParse(datetime, pattern);
        assertNull(result);
    }

    @Test
    public void testLDParse() {
        String datetime = "2023-01-01";
        String pattern = "yyyy-MM-dd";
        LocalDate result = DateUtils.ldParse(datetime, pattern);
        assertNotNull(result);
    }

    @Test
    public void testLDParseWithBlankString() {
        String datetime = "";
        String pattern = "yyyy-MM-dd";
        LocalDate result = DateUtils.ldParse(datetime, pattern);
        assertNull(result);
    }

    @Test
    public void testLDTParse() {
        String datetime = "2023-01-01 12:00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        LocalDateTime result = DateUtils.ldtParse(datetime, pattern);
        assertNotNull(result);
    }

    @Test
    public void testLDTParseWithBlankString() {
        String datetime = "";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        LocalDateTime result = DateUtils.ldtParse(datetime, pattern);
        assertNull(result);
    }

    @Test
    public void testZDTParse() {
        String datetime = "2023-01-01 12:00:00";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        ZonedDateTime result = DateUtils.zdtParse(datetime, pattern);
        assertNotNull(result);
    }

    @Test
    public void testZDTParseWithBlankString() {
        String datetime = "";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        ZonedDateTime result = DateUtils.zdtParse(datetime, pattern);
        assertNull(result);
    }

    @Test
    public void testIParseSDT() {
        String datetime = "2023-01-01 12:00:00";
        Instant result = DateUtils.iParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testLDParseSDT() {
        String datetime = "2023-01-01";
        LocalDate result = DateUtils.ldParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testLDTParseSDT() {
        String datetime = "2023-01-01 12:00:00";
        LocalDateTime result = DateUtils.ldtParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testZDTParseSDT() {
        String datetime = "2023-01-01 12:00:00";
        ZonedDateTime result = DateUtils.zdtParseSDT(datetime);
        assertNotNull(result);
    }

    @Test
    public void testFormatInstant() {
        Instant instant = Instant.now();
        String result = DateUtils.format(instant, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
    }

    @Test
    public void testFormatLocalDate() {
        LocalDate date = LocalDate.now();
        String result = DateUtils.format(date, "yyyy-MM-dd");
        assertNotNull(result);
    }

    @Test
    public void testFormatLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        String result = DateUtils.format(dateTime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
    }

    @Test
    public void testFormatZonedDateTime() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        String result = DateUtils.format(dateTime, "yyyy-MM-dd HH:mm:ss");
        assertNotNull(result);
    }

    @Test
    public void testFormatSDT() {
        String result = DateUtils.formatSDT();
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDTWithInstant() {
        Instant instant = Instant.now();
        String result = DateUtils.formatSDT(instant);
        assertNotNull(result);
        assertTrue(result.contains(" //"));
    }

    @Test
    public void testFormatSDTWithLocalDate() {
        LocalDate date = LocalDate.now();
        String result = DateUtils.formatSDT(date);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatSDTWithLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        String result = DateUtils.formatSDT(dateTime);
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSDTWithZonedDateTime() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        String result = DateUtils.formatSDT(dateTime);
        assertNotNull(result);
        assertEquals(19, result.length());
    }

    @Test
    public void testFormatSD() {
        LocalDate date = LocalDate.now();
        String result = DateUtils.formatSD(date);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatSDWithLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        String result = DateUtils.formatSD(dateTime);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testFormatSDWithZonedDateTime() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        String result = DateUtils.formatSD(dateTime);
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
    public void testFormatCustomer() {
        Instant instant = Instant.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(instant, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerWithLocalDate() {
        LocalDate date = LocalDate.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(date, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerWithLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(dateTime, locale);
        assertNotNull(result);
    }

    @Test
    public void testFormatCustomerWithZonedDateTime() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        Locale locale = Locale.getDefault();
        String result = DateUtils.formatCustomer(dateTime, locale);
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
    public void testLdtAtStartOfWeek() {
        LocalDateTime date = LocalDateTime.now();
        LocalDateTime result = DateUtils.ldtAtStartOfWeek(date);
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testZdtAtStartOfWeek() {
        ZonedDateTime date = ZonedDateTime.now();
        ZonedDateTime result = DateUtils.zdtAtStartOfWeek(date);
        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
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
    public void testZdtAtStartOfDay() {
        ZonedDateTime date = ZonedDateTime.now();
        ZonedDateTime result = DateUtils.zdtAtStartOfDay(date);
        assertNotNull(result);
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    public void testFromCalendarDaysOfWeekToISO() {
        List<Integer> calendarDays = Arrays.asList(Calendar.MONDAY, Calendar.TUESDAY);
        List<DayOfWeek> result = DateUtils.fromCalendarDaysOfWeekToISO(calendarDays);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0));
        assertEquals(DayOfWeek.TUESDAY, result.get(1));
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
