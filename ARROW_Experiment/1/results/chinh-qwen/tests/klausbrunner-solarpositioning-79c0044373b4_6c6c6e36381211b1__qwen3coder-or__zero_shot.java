package net.e175.klaus.solarpositioning;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.ZonedDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

public class SPA_ArrowTest {

    @Test
    public void testCalculateSolarPositionNormalCase() {
        ZonedDateTime date = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = -74.0;
        double elevation = 10.0;
        double deltaT = 69.0;
        double pressure = 1013.25;
        double temperature = 15.0;

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT, pressure, temperature);
        
        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0 && result.getAzimuth() <= 360);
        assertTrue(result.getZenith() >= 0 && result.getZenith() <= 180);
    }

    @Test
    public void testCalculateSolarPositionWithoutRefraction() {
        ZonedDateTime date = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = -74.0;
        double elevation = 10.0;
        double deltaT = 69.0;

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT);
        
        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0 && result.getAzimuth() <= 360);
        assertTrue(result.getZenith() >= 0 && result.getZenith() <= 180);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckLatLonRangeInvalidLatitude() {
        SPA.checkLatLonRange(-91.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckLatLonRangeInvalidLongitude() {
        SPA.checkLatLonRange(0.0, 181.0);
    }

    @Test
    public void testCalculateSunriseTransitSetNormalDay() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = -74.0;
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);
        
        assertNotNull(result);
        assertEquals(SunriseTransitSet.Type.NORMAL, result.getType());
        assertNotNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNotNull(result.getSunset());
    }

    @Test
    public void testCalculateSunriseTransitSetAllNight() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JULY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 70.0;
        double longitude = -74.0;
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);
        
        assertNotNull(result);
        assertEquals(SunriseTransitSet.Type.ALL_NIGHT, result.getType());
        assertNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNull(result.getSunset());
    }

    @Test
    public void testCalculateSunriseTransitSetAllDay() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JULY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = -70.0;
        double longitude = -74.0;
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);
        
        assertNotNull(result);
        assertEquals(SunriseTransitSet.Type.ALL_DAY, result.getType());
        assertNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNull(result.getSunset());
    }

    @Test
    public void testLimitDegreesTo360() {
        double result = SPA.limitDegreesTo360(370.0);
        assertEquals(10.0, result, 0.0001);
    }

    @Test
    public void testLimitTo() {
        double result = SPA.limitTo(370.0, 360.0);
        assertEquals(10.0, result, 0.0001);
    }

    @Test
    public void testLimitHprime() {
        double result = SPA.limitHprime(370.0);
        assertEquals(10.0, result, 0.0001);
    }

    @Test
    public void testLimitIfNecessaryWithinBounds() {
        double result = SPA.limitIfNecessary(1.0);
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    public void testLimitIfNecessaryOutOfBounds() {
        double result = SPA.limitIfNecessary(3.0);
        assertEquals(1.0, result, 0.0001);
    }

    @Test
    public void testAddFractionOfDay() {
        ZonedDateTime base = ZonedDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double fraction = 0.5;
        ZonedDateTime result = SPA.addFractionOfDay(base, fraction);
        
        assertEquals(12, result.getHour());
        assertEquals(0, result.getMinute());
    }

    @Test
    public void testStartOfDayUT() {
        ZonedDateTime input = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 30, 45, 123000000, java.time.ZoneId.of("UTC"));
        ZonedDateTime result = SPA.startOfDayUT(input);
        
        assertEquals(2020, result.getYear());
        assertEquals(Month.JANUARY, result.getMonth());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
        assertEquals(0, result.getNano());
    }
}
