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
    public void calculatesSolarPositionWithoutRefraction() {
        ZonedDateTime date = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, 39.742476, -105.1786, 1830.14, 67.0);

        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0.0);
        assertTrue(result.getAzimuth() < 360.0);
        assertTrue(result.getZenithAngle() >= 0.0);
        assertTrue(result.getZenithAngle() <= 180.0);
    }

    @Test
    public void calculatesSolarPositionWithRefraction() {
        ZonedDateTime date = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, 39.742476, -105.1786, 1830.14, 67.0, 820.0, 11.0);

        assertNotNull(result);
        assertTrue(result.getAzimuth() >= 0.0);
        assertTrue(result.getAzimuth() < 360.0);
        assertTrue(result.getZenithAngle() >= 0.0);
        assertTrue(result.getZenithAngle() <= 180.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsLatitudeOutsideRange() {
        SPA.calculateSolarPosition(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                90.1, 0.0, 0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsLongitudeOutsideRange() {
        SPA.calculateSolarPosition(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC),
                0.0, -180.1, 0.0, 0.0);
    }

    @Test
    public void calculatesNormalSunriseTransitAndSunset() {
        ZonedDateTime day = ZonedDateTime.of(2024, 3, 20, 12, 0, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 0.0, 0.0, 69.0);

        assertNotNull(result);
        assertEquals(SunriseTransitSet.Type.NORMAL, result.getType());
        assertNotNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNotNull(result.getSunset());
        assertTrue(result.getSunrise().isBefore(result.getTransit()));
        assertTrue(result.getTransit().isBefore(result.getSunset()));
    }

    @Test
    public void polarDayHasNoSunriseOrSunset() {
        ZonedDateTime day = ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 89.0, 0.0, 69.0);

        assertEquals(SunriseTransitSet.Type.ALL_DAY, result.getType());
        assertNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNull(result.getSunset());
    }

    @Test
    public void polarNightHasNoSunriseOrSunset() {
        ZonedDateTime day = ZonedDateTime.of(2024, 12, 21, 0, 0, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 89.0, 0.0, 69.0);

        assertEquals(SunriseTransitSet.Type.ALL_NIGHT, result.getType());
        assertNull(result.getSunrise());
        assertNotNull(result.getTransit());
        assertNull(result.getSunset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sunriseCalculationRejectsLatitudeOutsideRange() {
        SPA.calculateSunriseTransitSet(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                -90.1, 0.0, 69.0);
    }
}
