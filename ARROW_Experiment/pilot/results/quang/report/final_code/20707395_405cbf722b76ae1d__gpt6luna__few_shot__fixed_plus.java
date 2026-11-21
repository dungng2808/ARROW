package com.github.davidmoten.grumpy.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Position_ArrowTest {

    @Test
    public void constructorsFactoriesAndAccessors() {
        Position p = new Position(12.5, -45.25);
        assertEquals(12.5, p.getLat(), 0.0);
        assertEquals(-45.25, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);
        assertEquals("[12.5,-45.25]", p.toString());

        Position q = Position.create(1.0, 2.0, 3.0);
        assertEquals(1.0, q.getLat(), 0.0);
        assertEquals(2.0, q.getLon(), 0.0);
        assertEquals(3.0, q.getAlt(), 0.0);
        assertEquals(Position.create(4.0, 5.0), Position.position(4.0, 5.0));
    }

    @Test
    public void convertsDegreesMinutesSeconds() {
        assertEquals(10.5, Position.toDegrees(10.0, 30.0, 0.0), 0.0);
        assertEquals(-1.0, Position.toDegrees(-2.0, 60.0, 0.0), 0.0);
    }

    @Test
    public void predictsAlongEquator() {
        Position p = new Position(0.0, 0.0).predict(1000.0, 90.0);
        assertEquals(0.0, p.getLat(), 1e-10);
        assertEquals(8.9932, p.getLon(), 0.001);
    }

    @Test
    public void predictionRequiresSurfaceAltitude() {
        try {
            new Position(0.0, 0.0, 1.0).predict(1.0, 0.0);
            fail("Expected a runtime exception");
        } catch (RuntimeException expected) {
            assertEquals("Assertion failed: Predictions only valid for Earth's surface", expected.getMessage());
        }
    }

    @Test
    public void latitudeOnGreatCircleReturnsNullForSameLongitude() {
        assertNull(new Position(0.0, 10.0).getLatitudeOnGreatCircle(new Position(20.0, 10.0), 30.0));
    }

    @Test
    public void latitudeOnGreatCircleCalculatesCrossing() {
        Double latitude = new Position(0.0, 0.0)
                .getLatitudeOnGreatCircle(new Position(0.0, 90.0), 45.0);
        assertNotNull(latitude);
        assertEquals(0.0, latitude.doubleValue(), 1e-10);
    }

    @Test
    public void longitudeOnGreatCircleReturnsCandidates() {
        Position.LongitudePair pair = new Position(10.0, 0.0)
                .getLongitudeOnGreatCircle(new Position(20.0, 90.0), 15.0);
        assertNotNull(pair);
        assertEquals(15.645156661951619, pair.getLon1(), 1e-10);
        assertEquals(112.65869911429004, pair.getLon2(), 1e-10);
        assertEquals("LongitudePair [lon1=15.645156661951619, lon2=112.65869911429004]", pair.toString());
    }

    @Test
    public void longitudeOnGreatCircleReturnsNullWhenNoCrossing() {
        assertNull(new Position(0.0, 0.0)
                .getLongitudeOnGreatCircle(new Position(0.0, 90.0), 90.0));
    }

    @Test
    public void earthLimbReturnsRequestedRadials() {
        Position[] limb = new Position(10.0, 20.0, 1000.0).getEarthLimb(4);
        assertEquals(4, limb.length);
        assertEquals(80.0, limb[0].getLat(), 1e-10);
        assertEquals(-160.00000000000003, limb[0].getLon(), 1e-10);
        assertEquals(0.0, limb[1].getLat(), 1e-10);
        assertEquals(110.0, limb[1].getLon(), 1e-10);
    }

    @Test
    public void distanceAndBearing() {
        Position origin = new Position(0.0, 0.0);
        Position east = new Position(0.0, 1.0);
        assertEquals(111.195, origin.getDistanceToKm(east), 0.01);
        assertEquals(90.0, origin.getBearingDegrees(east), 1e-10);
        assertEquals(0.0, origin.getDistanceToKm(origin), 0.0);
    }

    @Test
    public void bearingDifferenceNormalizesInputs() {
        assertEquals(20.0, Position.getBearingDifferenceDegrees(10.0, 350.0), 0.0);
        assertEquals(-20.0, Position.getBearingDifferenceDegrees(-10.0, 10.0), 0.0);
        assertEquals(180.0, Position.getBearingDifferenceDegrees(180.0, 0.0), 0.0);
    }

    @Test
    public void distanceToGreatCirclePath() {
        Position point = new Position(1.0, 45.0);
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 90.0);
        assertEquals(78.62481404625223, point.getDistanceKmToPath(start, end), 0.01);
    }

    @Test
    public void formatsLatitudeAndLongitude() {
        assertEquals("12°30.00'N", Position.toDegreesMinutesDecimalMinutesLatitude(12.5));
        assertEquals("12°30.00'S", Position.toDegreesMinutesDecimalMinutesLatitude(-12.5));
        assertEquals("45°30.00'E", Position.toDegreesMinutesDecimalMinutesLongitude(45.5));
        assertEquals("45°30.00'W", Position.toDegreesMinutesDecimalMinutesLongitude(-45.5));
    }

    @Test
    public void positionAlongPathAndInvalidProportion() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 2.0);
        Position middle = start.getPositionAlongPath(end, 0.5);
        assertEquals(0.0, middle.getLat(), 1e-10);
        assertEquals(1.0, middle.getLon(), 1e-8);

        try {
            start.getPositionAlongPath(end, 1.1);
            fail("Expected a runtime exception");
        } catch (RuntimeException expected) {
            assertEquals("Proportion must be between 0 and 1 inclusive", expected.getMessage());
        }
    }

    @Test
    public void positionsAlongPathIncludeEndpoints() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 2.0);
        List<Position> positions = start.getPositionsAlongPath(end, 100.0);
        assertEquals(4, positions.size());
        assertEquals(start, positions.get(0));
        assertEquals(end, positions.get(3));
        assertEquals(2.0 / 3.0, positions.get(1).getLon(), 1e-8);
        assertEquals(4.0 / 3.0, positions.get(2).getLon(), 1e-8);
    }

    @Test
    public void convertsLongitudeTo360Range() {
        Position p = new Position(10.0, -20.0).to360();
        assertEquals(10.0, p.getLat(), 0.0);
        assertEquals(340.0, p.getLon(), 0.0);
        assertEquals(0.0, new Position(0.0, 20.0).to360().getLon(), 0.0 + 20.0);
    }

    @Test
    public void ensuresLongitudeContinuity() {
        Position adjusted = new Position(0.0, -179.0).ensureContinuous(new Position(0.0, 179.0));
        assertEquals(181.0, adjusted.getLon(), 0.0);
        Position unchanged = new Position(0.0, 10.0);
        assertTrue(unchanged == unchanged.ensureContinuous(new Position(0.0, 20.0)));
    }

    @Test
    public void checksPolygonContainment() {
        List<Position> polygon = Arrays.asList(new Position(0.0, 0.0), new Position(0.0, 2.0),
                new Position(2.0, 2.0), new Position(2.0, 0.0));
        assertTrue(new Position(1.0, 1.0).isWithin(polygon));
        assertFalse(new Position(3.0, 1.0).isWithin(polygon));
    }

    @Test
    public void equalityAndHashCodeUseLatitudeAndLongitude() {
        Position p = new Position(1.0, 2.0, 100.0);
        assertEquals(new Position(1.0, 2.0, 0.0), p);
        assertEquals(3, p.hashCode());
        assertNotEquals(new Position(1.0, 2.1), p);
        assertFalse(p.equals(null));
        assertFalse(p.equals("position"));
    }

    @Test
    public void distanceToPathHandlesEmptySingleAndMultiplePositions() {
        Position point = new Position(1.0, 1.0);
        try {
            point.getDistanceToPathKm(new ArrayList<Position>());
            fail("Expected a runtime exception");
        } catch (RuntimeException expected) {
            assertEquals("positions must not be empty", expected.getMessage());
        }

        List<Position> single = Arrays.asList(new Position(0.0, 0.0));
        assertEquals(point.getDistanceToKm(single.get(0)), point.getDistanceToPathKm(single), 0.0);

        List<Position> path = Arrays.asList(new Position(0.0, 0.0), new Position(0.0, 2.0));
        assertEquals(111.195, point.getDistanceToPathKm(path), 0.02);
    }

    @Test
    public void closestIntersectionAndDistanceToSegment() {
        Position point = new Position(1.0, 1.0);
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 2.0);
        Position closest = point.getClosestIntersectionWithSegment(start, end);
        assertEquals(0.0, closest.getLat(), 1e-8);
        assertEquals(0.9998984546739478, closest.getLon(), 1e-8);
        assertEquals(111.195, point.getDistanceToSegmentKm(start, end), 0.02);

        Position same = new Position(3.0, 4.0);
        assertTrue(same == point.getClosestIntersectionWithSegment(same, same));
    }

    @Test
    public void isOutsideUsesPolygonAndDistanceThreshold() {
        List<Position> polygon = Arrays.asList(new Position(0.0, 0.0), new Position(0.0, 2.0),
                new Position(2.0, 2.0), new Position(2.0, 0.0));
        assertFalse(new Position(1.0, 1.0).isOutside(polygon, 0.0));
        assertTrue(new Position(3.0, 1.0).isOutside(polygon, 100.0));
    }

    @Test
    public void longitudeDifferenceAndNormalization() {
        assertEquals(340.0, Position.longitudeDiff(170.0, -170.0), 0.0);
        assertEquals(20.0, Position.longitudeDiff(-170.0, 170.0), 0.0);
        assertEquals(180.0, Position.to180(180.0), 0.0);
        assertEquals(-170.0, Position.to180(190.0), 0.0);
        assertEquals(170.0, Position.to180(-190.0), 0.0);

        Position normalized = new Position(1.0, 190.0, 5.0).normalizeLongitude();
        assertEquals(1.0, normalized.getLat(), 0.0);
        assertEquals(-170.0, normalized.getLon(), 0.0);
        assertEquals(5.0, normalized.getAlt(), 0.0);
    }

    @Test
    public void assertWithMessageThrowsForFalseAssertion() {
        try {
            Position.assertWithMsg(false, "invalid");
            fail("Expected a runtime exception");
        } catch (RuntimeException expected) {
            assertEquals("Assertion failed: invalid", expected.getMessage());
        }
    }
}
