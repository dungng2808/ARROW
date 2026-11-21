package com.nurkiewicz.java8.holidays;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PolishHolidays_ArrowTest {

    @Test
    public void fixedDateHolidayIsHoliday() {
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
    public void workingDayIsTrueForOrdinaryWeekday() {
        PolishHolidays holidays = new PolishHolidays();

        assertTrue(holidays.isWorkingDay(LocalDate.of(2024, 1, 2)));
    }

    @Test
    public void workingDayIsFalseForHoliday() {
        PolishHolidays holidays = new PolishHolidays();

        assertFalse(holidays.isWorkingDay(LocalDate.of(2024, 1, 1)));
    }

    @Test
    public void nextHolidayAfterOrdinaryWeekdayIsWeekend() {
        PolishHolidays holidays = new PolishHolidays();

        assertEquals(LocalDate.of(2024, 1, 6),
                holidays.nextHolidayAfter(LocalDate.of(2024, 1, 2)));
    }

    @Test
    public void nextHolidayAfterHolidaySkipsToFollowingHoliday() {
        PolishHolidays holidays = new PolishHolidays();

        assertEquals(LocalDate.of(2024, 1, 7),
                holidays.nextHolidayAfter(LocalDate.of(2024, 1, 6)));
    }

    @Test
    public void nextWorkingDayAfterWeekendIsMonday() {
        PolishHolidays holidays = new PolishHolidays();

        assertEquals(LocalDate.of(2024, 1, 8),
                holidays.nextWorkingDayAfter(LocalDate.of(2024, 1, 6)));
    }

    @Test
    public void nextWorkingDayAfterFixedHolidaySkipsToNextDay() {
        PolishHolidays holidays = new PolishHolidays();

        assertEquals(LocalDate.of(2024, 1, 2),
                holidays.nextWorkingDayAfter(LocalDate.of(2024, 1, 1)));
    }
}
