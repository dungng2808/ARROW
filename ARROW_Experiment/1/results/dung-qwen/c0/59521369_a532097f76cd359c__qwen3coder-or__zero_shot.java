package com.nurkiewicz.java8.holidays;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static org.junit.Assert.*;

public class PolishHolidays_ArrowTest {

    @Test
    public void shouldReturnTrueForWeekends() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isHolidaySaturday = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 1));
        boolean isHolidaySunday = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 2));
        
        // Then
        assertTrue(isHolidaySaturday);
        assertTrue(isHolidaySunday);
    }

    @Test
    public void shouldReturnTrueForFixedHolidays() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isHolidayNewYear = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 1));
        boolean isHolidayEpiphany = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 6));
        boolean isHolidayLaborDay = holidays.isHoliday(LocalDate.of(2023, Month.MAY, 1));
        boolean isHolidayConstitutionDay = holidays.isHoliday(LocalDate.of(2023, Month.MAY, 3));
        boolean isHolidayAssumption = holidays.isHoliday(LocalDate.of(2023, Month.AUGUST, 15));
        boolean isHolidayAllSaints = holidays.isHoliday(LocalDate.of(2023, Month.NOVEMBER, 1));
        boolean isHolidayIndependenceDay = holidays.isHoliday(LocalDate.of(2023, Month.NOVEMBER, 11));
        boolean isHolidayChristmas1 = holidays.isHoliday(LocalDate.of(2023, Month.DECEMBER, 25));
        boolean isHolidayChristmas2 = holidays.isHoliday(LocalDate.of(2023, Month.DECEMBER, 26));
        
        // Then
        assertTrue(isHolidayNewYear);
        assertTrue(isHolidayEpiphany);
        assertTrue(isHolidayLaborDay);
        assertTrue(isHolidayConstitutionDay);
        assertTrue(isHolidayAssumption);
        assertTrue(isHolidayAllSaints);
        assertTrue(isHolidayIndependenceDay);
        assertTrue(isHolidayChristmas1);
        assertTrue(isHolidayChristmas2);
    }

    @Test
    public void shouldReturnTrueForEasterMonday() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isEasterMonday2023 = holidays.isHoliday(LocalDate.of(2023, Month.APRIL, 17));
        boolean isEasterMonday2022 = holidays.isHoliday(LocalDate.of(2022, Month.APRIL, 25));
        boolean isEasterMonday2021 = holidays.isHoliday(LocalDate.of(2021, Month.APRIL, 12));
        
        // Then
        assertTrue(isEasterMonday2023);
        assertTrue(isEasterMonday2022);
        assertTrue(isEasterMonday2021);
    }

    @Test
    public void shouldReturnTrueForCorpusChristi() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isCorpusChristi2023 = holidays.isHoliday(LocalDate.of(2023, Month.JUNE, 8));
        boolean isCorpusChristi2022 = holidays.isHoliday(LocalDate.of(2022, Month.JUNE, 16));
        boolean isCorpusChristi2021 = holidays.isHoliday(LocalDate.of(2021, Month.JUNE, 3));
        
        // Then
        assertTrue(isCorpusChristi2023);
        assertTrue(isCorpusChristi2022);
        assertTrue(isCorpusChristi2021);
    }

    @Test
    public void shouldReturnFalseForRegularWeekdays() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isHolidayMonday = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 3));
        boolean isHolidayTuesday = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 4));
        boolean isHolidayWednesday = holidays.isHoliday(LocalDate.of(2023, Month.JANUARY, 5));
        
        // Then
        assertFalse(isHolidayMonday);
        assertFalse(isHolidayTuesday);
        assertFalse(isHolidayWednesday);
    }

    @Test
    public void shouldReturnTrueForWorkingDay() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isWorkingDayMonday = holidays.isWorkingDay(LocalDate.of(2023, Month.JANUARY, 3));
        boolean isWorkingDayTuesday = holidays.isWorkingDay(LocalDate.of(2023, Month.JANUARY, 4));
        boolean isWorkingDayWednesday = holidays.isWorkingDay(LocalDate.of(2023, Month.JANUARY, 5));
        
        // Then
        assertTrue(isWorkingDayMonday);
        assertTrue(isWorkingDayTuesday);
        assertTrue(isWorkingDayWednesday);
    }

    @Test
    public void shouldReturnFalseForNonWorkingDay() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        boolean isWorkingDayNewYear = holidays.isWorkingDay(LocalDate.of(2023, Month.JANUARY, 1));
        boolean isWorkingDayEasterMonday = holidays.isWorkingDay(LocalDate.of(2023, Month.APRIL, 17));
        
        // Then
        assertFalse(isWorkingDayNewYear);
        assertFalse(isWorkingDayEasterMonday);
    }

    @Test
    public void shouldFindNextHolidayAfter() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        LocalDate nextHoliday = holidays.nextHolidayAfter(LocalDate.of(2023, Month.JANUARY, 1));
        
        // Then
        assertEquals(LocalDate.of(2023, Month.JANUARY, 6), nextHoliday);
    }

    @Test
    public void shouldFindNextWorkingDayAfter() {
        // Given
        PolishHolidays holidays = new PolishHolidays();
        
        // When
        LocalDate nextWorkingDay = holidays.nextWorkingDayAfter(LocalDate.of(2023, Month.JANUARY, 1));
        
        // Then
        assertEquals(LocalDate.of(2023, Month.JANUARY, 3), nextWorkingDay);
    }
}
