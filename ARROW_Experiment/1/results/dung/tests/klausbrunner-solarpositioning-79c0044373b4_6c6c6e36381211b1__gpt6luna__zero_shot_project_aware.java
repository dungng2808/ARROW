package net.e175.klaus.solarpositioning;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SPA_ArrowTest {

    @Test
    public void calculatesSolarPositionForKnownExample() {
        ZonedDateTime date = ZonedDateTime.of(2003, 10, 17, 12, 30, 30, 0, ZoneOffset.ofHours(-7));

        AzimuthZenithAngle result = SPA.calculateSolarPosition(
                date, 39.742476, -105.1786, 1830.14, 67.0, 820.0, 11.0);

        assertThat(result.getAzimuth()).isCloseTo(194.34024, org.assertj.core.data.Offset.offset(0.0001));
        assertThat(result.getZenithAngle()).isCloseTo(50.11162, org.assertj.core.data.Offset.offset(0.0001));
    }

    @Test
    public void rejectsLatitudeOutsideValidRange() {
        ZonedDateTime date = ZonedDateTime.of(2020, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        assertThatThrownBy(() -> SPA.calculateSolarPosition(date, 90.1, 0.0, 0.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void rejectsLongitudeOutsideValidRange() {
        ZonedDateTime date = ZonedDateTime.of(2020, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        assertThatThrownBy(() -> SPA.calculateSunriseTransitSet(date, 0.0, 180.1, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void calculatesNormalSunriseTransitAndSunset() {
        ZonedDateTime day = ZonedDateTime.of(2020, 3, 20, 15, 45, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 0.0, 0.0, 69.0);

        assertThat(result.getType()).isEqualTo(SunriseTransitSet.Type.NORMAL);
        assertThat(result.getSunrise()).isNotNull();
        assertThat(result.getTransit()).isNotNull();
        assertThat(result.getSunset()).isNotNull();
        assertThat(result.getSunrise().toLocalDate()).isEqualTo(day.toLocalDate());
        assertThat(result.getTransit().toLocalDate()).isEqualTo(day.toLocalDate());
        assertThat(result.getSunset().toLocalDate()).isEqualTo(day.toLocalDate());
        assertThat(result.getSunrise()).isBefore(result.getTransit());
        assertThat(result.getTransit()).isBefore(result.getSunset());
    }

    @Test
    public void returnsAllDaySunForHighArcticSummer() {
        ZonedDateTime day = ZonedDateTime.of(2020, 6, 21, 0, 0, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 89.0, 0.0, 69.0);

        assertThat(result.getType()).isEqualTo(SunriseTransitSet.Type.ALL_DAY);
        assertThat(result.getSunrise()).isNull();
        assertThat(result.getTransit()).isNotNull();
        assertThat(result.getSunset()).isNull();
    }

    @Test
    public void returnsAllNightSunForHighArcticWinter() {
        ZonedDateTime day = ZonedDateTime.of(2020, 12, 21, 0, 0, 0, 0, ZoneOffset.UTC);

        SunriseTransitSet result = SPA.calculateSunriseTransitSet(day, 89.0, 0.0, 69.0);

        assertThat(result.getType()).isEqualTo(SunriseTransitSet.Type.ALL_NIGHT);
        assertThat(result.getSunrise()).isNull();
        assertThat(result.getTransit()).isNotNull();
        assertThat(result.getSunset()).isNull();
    }
}
