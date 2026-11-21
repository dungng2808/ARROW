package com.nurkiewicz.java8.holidays;

import java.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolishHolidays_ArrowTest {

    @Test
    public void fixedHolidayIsHoliday() {
        PolishHolidays holidays = new PolishHolidays();
        assertTrue(holidays.isHoliday(LocalDate.of(2024, 1, 1)));
    }

    @Test
    public void ordinaryWeekdayIsNotHoliday() {
        PolishHolidays holidays = new PolishHolidays();
        assertFalse(holidays.isHoliday(LocalDate.of(2024, 1, 2)));
    }

    @Test
    public void weekendIsHoliday() {
        PolishHolidays holidays = new PolishHolidays();
        assertTrue(holidays.isHoliday(LocalDate.of(2024, 1, 6)));
    }

    @Test
    public void easterMondayIsHoliday() {
        PolishHolidays holidays = new PolishHolidays();
        assertTrue(holidays.isHoliday(LocalDate.of(2024, 4, 1)));
    }

    @Test
    public void corpusChristiIsHoliday() {
        PolishHolidays holidays = new PolishHolidays();
        assertTrue(holidays.isHoliday(LocalDate.of(2024, 5, 30)));
    }

    @Test
    public void workingDayIsRecognized() {
        PolishHolidays holidays = new PolishHolidays();
        assertTrue(holidays.isWorkingDay(LocalDate.of(2024, 1, 2)));
    }

    @Test
    public void holidayIsNotWorkingDay() {
        PolishHolidays holidays = new PolishHolidays();
        assertFalse(holidays.isWorkingDay(LocalDate.of(2024, 1, 1)));
    }

    @Test
    public void nextHolidayAfterDateIsReturned() {
        PolishHolidays holidays = new PolishHolidays();
        assertEquals(LocalDate.of(2024, 1, 6),
                holidays.nextHolidayAfter(LocalDate.of(2024, 1, 5)));
    }

    @Test
    public void nextWorkingDayAfterWeekendIsReturned() {
        PolishHolidays holidays = new PolishHolidays();
        assertEquals(LocalDate.of(2024, 1, 8),
                holidays.nextWorkingDayAfter(LocalDate.of(2024, 1, 5)));
    }
}
