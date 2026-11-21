package com.nurkiewicz.java8.holidays;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;
import static java.time.Month.*;

public class PolishHolidays_ArrowTest {

    private final PolishHolidays holidays = new PolishHolidays();

    @Test
    public void shouldIdentifyWeekendsAsHolidays() {
        // Saturday
        LocalDate saturday = LocalDate.of(2016, 1, 2);
        assertTrue(holidays.isHoliday(saturday));
        
        // Sunday
        LocalDate sunday = LocalDate.of(2016, 1, 3);
        assertTrue(holidays.isHoliday(sunday));
    }

    @Test
    public void shouldIdentifyFixedHolidays() {
        // New Year's Day
        LocalDate newYearsDay = LocalDate.of(2016, JANUARY, 1);
        assertTrue(holidays.isHoliday(newYearsDay));
        
        // Epiphany
        LocalDate epiphany = LocalDate.of(2016, JANUARY, 6);
        assertTrue(holidays.isHoliday(epiphany));
        
        // Labor Day
        LocalDate laborDay = LocalDate.of(2016, MAY, 1);
        assertTrue(holidays.isHoliday(laborDay));
        
        // Constitution Day
        LocalDate constitutionDay = LocalDate.of(2016, MAY, 3);
        assertTrue(holidays.isHoliday(constitutionDay));
        
        // Assumption Day
        LocalDate assumptionDay = LocalDate.of(2016, AUGUST, 15);
        assertTrue(holidays.isHoliday(assumptionDay));
        
        // All Saints' Day
        LocalDate allSaintsDay = LocalDate.of(2016, NOVEMBER, 1);
        assertTrue(holidays.isHoliday(allSaintsDay));
        
        // Independence Day
        LocalDate independenceDay = LocalDate.of(2016, NOVEMBER, 11);
        assertTrue(holidays.isHoliday(independenceDay));
        
        // Christmas
        LocalDate christmas = LocalDate.of(2016, DECEMBER, 25);
        assertTrue(holidays.isHoliday(christmas));
        
        // Boxing Day
        LocalDate boxingDay = LocalDate.of(2016, DECEMBER, 26);
        assertTrue(holidays.isHoliday(boxingDay));
    }

    @Test
    public void shouldIdentifyEasterMonday() {
        // In 2016, Easter Sunday was on March 27, so Easter Monday was on March 28
        LocalDate easterMonday = LocalDate.of(2016, 3, 28);
        assertTrue(holidays.isHoliday(easterMonday));
        
        // In 2017, Easter Sunday was on April 16, so Easter Monday was on April 17
        LocalDate easterMonday2017 = LocalDate.of(2017, 4, 17);
        assertTrue(holidays.isHoliday(easterMonday2017));
    }

    @Test
    public void shouldIdentifyCorpusChristi() {
        // In 2016, Easter Sunday was on March 27, so Corpus Christi was on June 16
        LocalDate corpusChristi = LocalDate.of(2016, 6, 16);
        assertTrue(holidays.isHoliday(corpusChristi));
        
        // In 2017, Easter Sunday was on April 16, so Corpus Christi was on June 25
        LocalDate corpusChristi2017 = LocalDate.of(2017, 6, 25);
        assertTrue(holidays.isHoliday(corpusChristi2017));
    }

    @Test
    public void shouldIdentifyWorkingDays() {
        // Saturday
        LocalDate saturday = LocalDate.of(2016, 1, 2);
        assertFalse(holidays.isWorkingDay(saturday));
        
        // Sunday
        LocalDate sunday = LocalDate.of(2016, 1, 3);
        assertFalse(holidays.isWorkingDay(sunday));
        
        // New Year's Day
        LocalDate newYearsDay = LocalDate.of(2016, JANUARY, 1);
        assertFalse(holidays.isWorkingDay(newYearsDay));
        
        // Regular weekday
        LocalDate monday = LocalDate.of(2016, 1, 4);
        assertTrue(holidays.isWorkingDay(monday));
    }

    @Test
    public void shouldFindNextHolidayAfter() {
        // Start from a regular weekday
        LocalDate start = LocalDate.of(2016, 1, 4);
        LocalDate nextHoliday = holidays.nextHolidayAfter(start);
        // Should be New Year's Day (Jan 1) which is before our start date
        // But since we're looking after, it should be Jan 6 (Epiphany)
        assertEquals(LocalDate.of(2016, JANUARY, 6), nextHoliday);
        
        // Start from a weekend
        LocalDate saturday = LocalDate.of(2016, 1, 2);
        LocalDate nextHolidayFromWeekend = holidays.nextHolidayAfter(saturday);
        // Next holiday after Saturday would be Monday (Jan 4)
        assertEquals(LocalDate.of(2016, 1, 4), nextHolidayFromWeekend);
    }

    @Test
    public void shouldFindNextWorkingDayAfter() {
        // Start from a holiday (New Year's Day)
        LocalDate newYearsDay = LocalDate.of(2016, JANUARY, 1);
        LocalDate nextWorkingDay = holidays.nextWorkingDayAfter(newYearsDay);
        // Next working day after Jan 1 should be Jan 4 (Monday)
        assertEquals(LocalDate.of(2016, JANUARY, 4), nextWorkingDay);
        
        // Start from a weekend
        LocalDate saturday = LocalDate.of(2016, 1, 2);
        LocalDate nextWorkingDayFromWeekend = holidays.nextWorkingDayAfter(saturday);
        // Next working day after Saturday should be Monday (Jan 4)
        assertEquals(LocalDate.of(2016, JANUARY, 4), nextWorkingDayFromWeekend);
    }
}
