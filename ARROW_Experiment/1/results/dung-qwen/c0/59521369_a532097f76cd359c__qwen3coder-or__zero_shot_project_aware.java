package com.nurkiewicz.java8.holidays;

import org.junit.Test;
import org.junit.Before;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static org.fest.assertions.api.Assertions.assertThat;

public class PolishHolidays_ArrowTest {

    private PolishHolidays holidays;

    @Before
    public void setUp() {
        holidays = new PolishHolidays();
    }

    @Test
    public void shouldRecognizeFixedHolidays() {
        // Given
        LocalDate newYear = LocalDate.of(2015, Month.JANUARY, 1);
        LocalDate epiphany = LocalDate.of(2015, Month.JANUARY, 6);
        LocalDate laborDay = LocalDate.of(2015, Month.MAY, 1);
        LocalDate constitutionDay = LocalDate.of(2015, Month.MAY, 3);
        LocalDate assumptionDay = LocalDate.of(2015, Month.AUGUST, 15);
        LocalDate allSaintsDay = LocalDate.of(2015, Month.NOVEMBER, 1);
        LocalDate independenceDay = LocalDate.of(2015, Month.NOVEMBER, 11);
        LocalDate christmas1 = LocalDate.of(2015, Month.DECEMBER, 25);
        LocalDate christmas2 = LocalDate.of(2015, Month.DECEMBER, 26);

        // When & Then
        assertThat(holidays.isHoliday(newYear)).isTrue();
        assertThat(holidays.isHoliday(epiphany)).isTrue();
        assertThat(holidays.isHoliday(laborDay)).isTrue();
        assertThat(holidays.isHoliday(constitutionDay)).isTrue();
        assertThat(holidays.isHoliday(assumptionDay)).isTrue();
        assertThat(holidays.isHoliday(allSaintsDay)).isTrue();
        assertThat(holidays.isHoliday(independenceDay)).isTrue();
        assertThat(holidays.isHoliday(christmas1)).isTrue();
        assertThat(holidays.isHoliday(christmas2)).isTrue();
    }

    @Test
    public void shouldRecognizeWeekends() {
        // Given
        LocalDate saturday = LocalDate.of(2015, Month.JANUARY, 3);
        LocalDate sunday = LocalDate.of(2015, Month.JANUARY, 4);

        // When & Then
        assertThat(holidays.isHoliday(saturday)).isTrue();
        assertThat(holidays.isHoliday(sunday)).isTrue();
    }

    @Test
    public void shouldRecognizeEasterMonday() {
        // Given
        LocalDate easterMonday2015 = LocalDate.of(2015, Month.APRIL, 6);
        LocalDate easterMonday2016 = LocalDate.of(2016, Month.MARCH, 28);
        LocalDate easterMonday2017 = LocalDate.of(2017, Month.APRIL, 17);

        // When & Then
        assertThat(holidays.isHoliday(easterMonday2015)).isTrue();
        assertThat(holidays.isHoliday(easterMonday2016)).isTrue();
        assertThat(holidays.isHoliday(easterMonday2017)).isTrue();
    }

    @Test
    public void shouldRecognizeCorpusChristi() {
        // Given
        LocalDate corpusChristi2015 = LocalDate.of(2015, Month.JUNE, 15);
        LocalDate corpusChristi2016 = LocalDate.of(2016, Month.MAY, 26);
        LocalDate corpusChristi2017 = LocalDate.of(2017, Month.JUNE, 15);

        // When & Then
        assertThat(holidays.isHoliday(corpusChristi2015)).isTrue();
        assertThat(holidays.isHoliday(corpusChristi2016)).isTrue();
        assertThat(holidays.isHoliday(corpusChristi2017)).isTrue();
    }

    @Test
    public void shouldRecognizeWorkingDays() {
        // Given
        LocalDate monday = LocalDate.of(2015, Month.JANUARY, 5);
        LocalDate tuesday = LocalDate.of(2015, Month.JANUARY, 6);
        LocalDate wednesday = LocalDate.of(2015, Month.JANUARY, 7);

        // When & Then
        assertThat(holidays.isWorkingDay(monday)).isTrue();
        assertThat(holidays.isWorkingDay(tuesday)).isFalse(); // Epiphany
        assertThat(holidays.isWorkingDay(wednesday)).isTrue();
    }

    @Test
    public void shouldFindNextHolidayAfter() {
        // Given
        LocalDate january4 = LocalDate.of(2015, Month.JANUARY, 4); // Sunday
        LocalDate january5 = LocalDate.of(2015, Month.JANUARY, 5); // Monday
        LocalDate january6 = LocalDate.of(2015, Month.JANUARY, 6); // Tuesday (Epiphany)

        // When
        LocalDate nextHoliday1 = holidays.nextHolidayAfter(january4);
        LocalDate nextHoliday2 = holidays.nextHolidayAfter(january5);
        LocalDate nextHoliday3 = holidays.nextHolidayAfter(january6);

        // Then
        assertThat(nextHoliday1).isEqualTo(LocalDate.of(2015, Month.JANUARY, 5)); // Monday
        assertThat(nextHoliday2).isEqualTo(LocalDate.of(2015, Month.JANUARY, 6)); // Tuesday (Epiphany)
        assertThat(nextHoliday3).isEqualTo(LocalDate.of(2015, Month.MAY, 1)); // Labor Day
    }

    @Test
    public void shouldFindNextWorkingDayAfter() {
        // Given
        LocalDate january4 = LocalDate.of(2015, Month.JANUARY, 4); // Sunday
        LocalDate january5 = LocalDate.of(2015, Month.JANUARY, 5); // Monday
        LocalDate january6 = LocalDate.of(2015, Month.JANUARY, 6); // Tuesday (Epiphany)

        // When
        LocalDate nextWorkingDay1 = holidays.nextWorkingDayAfter(january4);
        LocalDate nextWorkingDay2 = holidays.nextWorkingDayAfter(january5);
        LocalDate nextWorkingDay3 = holidays.nextWorkingDayAfter(january6);

        // Then
        assertThat(nextWorkingDay1).isEqualTo(LocalDate.of(2015, Month.JANUARY, 5)); // Monday
        assertThat(nextWorkingDay2).isEqualTo(LocalDate.of(2015, Month.JANUARY, 7)); // Wednesday
        assertThat(nextWorkingDay3).isEqualTo(LocalDate.of(2015, Month.JANUARY, 7)); // Wednesday
    }
}
