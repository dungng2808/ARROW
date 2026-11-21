package net.e175.klaus.solarpositioning;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.Assert.*;

public class SPA_ArrowTest {

    @Test
    public void testCalculateSolarPosition() {
        ZonedDateTime date = ZonedDateTime.parse("2020-06-21T12:00:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        double latitude = 40.7128; // New York City
        double longitude = -74.0060; // New York City
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
        ZonedDateTime date = ZonedDateTime.parse("2020-06-21T12:00:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        double latitude = 40.7128; // New York City
        double longitude = -74.0060; // New York City
        double elevation = 10.0;
        double deltaT = 69.0;

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT);

        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0 && result.getAzimuth() <= 360);
        assertTrue(result.getZenith() >= 0 && result.getZenith() <= 180);
    }

    @Test
    public void testCalculateSunriseTransitSet() {
        ZonedDateTime day = ZonedDateTime.parse("2020-06-21T00:00:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        double latitude = 40.7128; // New York City
        double longitude = -74.0060; // New York City
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);

        assertNotNull(result);
        assertNotNull(result.getTransit());
        assertTrue(result.getType() == SunriseTransitSet.Type.NORMAL ||
                result.getType() == SunriseTransitSet.Type.ALL_DAY ||
                result.getType() == SunriseTransitSet.Type.ALL_NIGHT);
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
    public void testCalculateTopocentricSolarPosition() {
        double p = 1013.25;
        double t = 15.0;
        double phi = Math.toRadians(40.7128);
        double deltaPrime = Math.toRadians(69.0);
        double hPrime = Math.toRadians(180.0);

        AzimuthZenithAngle result = SPA.calculateTopocentricSolarPosition(p, t, phi, deltaPrime, hPrime);

        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0 && result.getAzimuth() <= 360);
        assertTrue(result.getZenith() >= 0 && result.getZenith() <= 180);
    }

    @Test
    public void testCalculateGeocentricSunDeclination() {
        double betaRad = Math.toRadians(-1.0);
        double epsilonRad = Math.toRadians(23.44);
        double lambdaRad = Math.toRadians(180.0);

        double result = SPA.calculateGeocentricSunDeclination(betaRad, epsilonRad, lambdaRad);
        assertTrue(result >= -90.0 && result <= 90.0);
    }

    @Test
    public void testCalculateGeocentricSunRightAscension() {
        double betaRad = Math.toRadians(-1.0);
        double epsilonRad = Math.toRadians(23.44);
        double lambdaRad = Math.toRadians(180.0);

        double result = SPA.calculateGeocentricSunRightAscension(betaRad, epsilonRad, lambdaRad);
        assertTrue(result >= 0 && result <= 360);
    }
}
