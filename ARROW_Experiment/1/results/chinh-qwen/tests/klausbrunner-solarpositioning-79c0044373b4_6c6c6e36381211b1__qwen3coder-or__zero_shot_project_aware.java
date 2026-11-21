package net.e175.klaus.solarpositioning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.ZonedDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

public class SPA_ArrowTest {

    @Test
    @DisplayName("calculateSolarPosition should handle valid inputs correctly")
    public void testCalculateSolarPositionValidInputs() {
        ZonedDateTime date = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = -74.0;
        double elevation = 10.0;
        double deltaT = 69.0;
        double pressure = 1013.0;
        double temperature = 20.0;

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT, pressure, temperature);

        assertThat(result).isNotNull();
        assertThat(result.getAzimuth()).isBetween(0.0, 360.0);
        assertThat(result.getZenith()).isBetween(0.0, 180.0);
    }

    @Test
    @DisplayName("calculateSolarPosition should handle minimal pressure and temperature")
    public void testCalculateSolarPositionMinimalPressureTemp() {
        ZonedDateTime date = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = -74.0;
        double elevation = 10.0;
        double deltaT = 69.0;

        AzimuthZenithAngle result = SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT);

        assertThat(result).isNotNull();
        assertThat(result.getAzimuth()).isBetween(0.0, 360.0);
        assertThat(result.getZenith()).isBetween(0.0, 180.0);
    }

    @Test
    @DisplayName("calculateSolarPosition should throw IllegalArgumentException for invalid latitude")
    public void testCalculateSolarPositionInvalidLatitude() {
        ZonedDateTime date = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 91.0;
        double longitude = -74.0;
        double elevation = 10.0;
        double deltaT = 69.0;
        double pressure = 1013.0;
        double temperature = 20.0;

        assertThatThrownBy(() -> SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT, pressure, temperature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("latitude/longitude out of range");
    }

    @Test
    @DisplayName("calculateSolarPosition should throw IllegalArgumentException for invalid longitude")
    public void testCalculateSolarPositionInvalidLongitude() {
        ZonedDateTime date = ZonedDateTime.of(2020, Month.JANUARY, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = 181.0;
        double elevation = 10.0;
        double deltaT = 69.0;
        double pressure = 1013.0;
        double temperature = 20.0;

        assertThatThrownBy(() -> SPA.calculateSolarPosition(date, latitude, longitude, elevation, deltaT, pressure, temperature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("latitude/longitude out of range");
    }

    @Test
    @DisplayName("calculateSunriseTransitSet should handle valid inputs correctly")
    public void testCalculateSunriseTransitSetValidInputs() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 40.0;
        double longitude = -74.0;
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isNotNull();
        assertThat(result.getTransit()).isNotNull();
    }

    @Test
    @DisplayName("calculateSunriseTransitSet should throw IllegalArgumentException for invalid latitude")
    public void testCalculateSunriseTransitSetInvalidLatitude() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 91.0;
        double longitude = -74.0;
        double deltaT = 69.0;

        assertThatThrownBy(() -> SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("latitude/longitude out of range");
    }

    @Test
    @DisplayName("calculateSunriseTransitSet should handle all-day condition")
    public void testCalculateSunriseTransitSetAllDay() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = 80.0; // Near polar circle in winter
        double longitude = -74.0;
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(SunriseTransitSet.Type.ALL_DAY);
        assertThat(result.getSunrise()).isNull();
        assertThat(result.getSunset()).isNull();
        assertThat(result.getTransit()).isNotNull();
    }

    @Test
    @DisplayName("calculateSunriseTransitSet should handle all-night condition")
    public void testCalculateSunriseTransitSetAllNight() {
        ZonedDateTime day = ZonedDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0, 0, java.time.ZoneId.of("UTC"));
        double latitude = -80.0; // Near polar circle in winter
        double longitude = -74.0;
        double deltaT = 69.0;

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, latitude, longitude, deltaT);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(SunriseTransitSet.Type.ALL_NIGHT);
        assertThat(result.getSunrise()).isNull();
        assertThat(result.getSunset()).isNull();
        assertThat(result.getTransit()).isNotNull();
    }

    @Test
    @DisplayName("limitDegreesTo360 should properly limit degrees to 0-360 range")
    public void testLimitDegreesTo360() {
        double result1 = SPA.limitDegreesTo360(370.0);
        assertThat(result1).isEqualTo(10.0);

        double result2 = SPA.limitDegreesTo360(-10.0);
        assertThat(result2).isEqualTo(350.0);

        double result3 = SPA.limitDegreesTo360(360.0);
        assertThat(result3).isEqualTo(0.0);
    }

    @Test
    @DisplayName("limitTo should properly limit values to specified maximum")
    public void testLimitTo() {
        double result1 = SPA.limitTo(370.0, 360.0);
        assertThat(result1).isEqualTo(10.0);

        double result2 = SPA.limitTo(-10.0, 360.0);
        assertThat(result2).isEqualTo(350.0);

        double result3 = SPA.limitTo(360.0, 360.0);
        assertThat(result3).isEqualTo(0.0);
    }

    @Test
    @DisplayName("checkLatLonRange should throw exception for invalid coordinates")
    public void testCheckLatLonRangeInvalid() {
        assertThatThrownBy(() -> SPA.checkLatLonRange(91.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("latitude/longitude out of range");

        assertThatThrownBy(() -> SPA.checkLatLonRange(0.0, 181.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("latitude/longitude out of range");
    }

    @Test
    @DisplayName("checkLatLonRange should not throw exception for valid coordinates")
    public void testCheckLatLonRangeValid() {
        // Should not throw any exception
        SPA.checkLatLonRange(40.0, -74.0);
        SPA.checkLatLonRange(-90.0, 180.0);
        SPA.checkLatLonRange(90.0, -180.0);
    }
}
