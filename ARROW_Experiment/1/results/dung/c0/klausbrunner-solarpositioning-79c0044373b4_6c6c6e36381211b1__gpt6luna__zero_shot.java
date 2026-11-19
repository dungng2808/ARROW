package net.e175.klaus.solarpositioning;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SPA_ArrowTest {

    @Test
    public void calculateSolarPositionReturnsPosition() {
        ZonedDateTime date = ZonedDateTime.of(2024, 3, 20, 12, 0, 0, 0, ZoneOffset.UTC);

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, 0.0, 0.0, 0.0, 69.0);

        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0.0);
        assertTrue(result.getAzimuth() < 360.0);
        assertTrue(result.getZenithAngle() >= 0.0);
        assertTrue(result.getZenithAngle() <= 180.0);
    }

    @Test
    public void calculateSolarPositionWithRefractionReturnsPosition() {
        ZonedDateTime date = ZonedDateTime.of(2024, 6, 21, 12, 0, 0, 0, ZoneOffset.UTC);

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, 40.0, -74.0, 10.0, 69.0, 1010.0, 15.0);

        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0.0);
        assertTrue(result.getAzimuth() < 360.0);
        assertTrue(result.getZenithAngle() >= 0.0);
        assertTrue(result.getZenithAngle() <= 180.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateSolarPositionRejectsLatitudeAboveRange() {
        SPA.calculateSolarPosition(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                90.1, 0.0, 0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateSolarPositionRejectsLongitudeBelowRange() {
        SPA.calculateSolarPosition(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                0.0, -180.1, 0.0, 0.0);
    }

    @Test
    public void sunriseTransitSetForNormalDayHasAllTimes() {
        ZonedDateTime day = ZonedDateTime.of(2024, 3, 20, 12, 34, 56, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 0.0, 0.0, 69.0);

        assertNotNull(result);
        assertEquals(SunriseTransitSet.Type.NORMAL, result.getType());
        assertNotNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNotNull(result.getSunset());
    }

    @Test
    public void sunriseTransitSetAtHighLatitudeReportsAllDayOrAllNight() {
        ZonedDateTime day = ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 89.0, 0.0, 69.0);

        assertNotNull(result);
        assertTrue(result.getType() == SunriseTransitSet.Type.ALL_DAY
                || result.getType() == SunriseTransitSet.Type.ALL_NIGHT);
        assertNull(result.getSunrise());
        assertNull(result.getSunset());
        assertNotNull(result.getTransit());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sunriseTransitSetRejectsLongitudeAboveRange() {
        SPA.calculateSunriseTransitSet(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                0.0, 180.1, 0.0);
    }
}
