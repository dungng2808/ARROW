package com.github.davidmoten.grumpy.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Position_ArrowTest {

    @Test
    public void constructorsAndFactoriesPreserveCoordinates() {
        Position p = new Position(12.5, -34.25);
        assertEquals(12.5, p.getLat(), 0.0);
        assertEquals(-34.25, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);

        Position q = Position.create(-1.0, 2.0, 300.0);
        assertEquals(-1.0, q.getLat(), 0.0);
        assertEquals(2.0, q.getLon(), 0.0);
        assertEquals(300.0, q.getAlt(), 0.0);
        assertEquals(new Position(3.0, 4.0), Position.position(3.0, 4.0));
    }

    @Test
    public void toStringFormatsCoordinates() {
        assertEquals("[1.5,-2.0]", new Position(1.5, -2.0).toString());
    }

    @Test
    public void degreeMinuteSecondConversion() {
        assertEquals(12.5125, Position.toDegrees(12.0, 30.0, 45.0), 0.0);
    }

    @Test
    public void greatCirclePredictionDueNorth() {
        Position result = new Position(0.0, 0.0).predict(1000.0, 0.0);
        assertEquals(8.9932, result.getLat(), 0.001);
        assertEquals(0.0, result.getLon(), 1e-10);
    }

    @Test
    public void predictionRequiresSurfaceAltitude() {
        try {
            new Position(0.0, 0.0, 1.0).predict(10.0, 0.0);
        } catch (RuntimeException e) {
            assertEquals("Assertion failed: Predictions only valid for Earth's surface", e.getMessage());
            return;
        }
        throw new AssertionError("Expected RuntimeException");
    }

    @Test
    public void latitudeOnGreatCircleReturnsNullForSameLongitude() {
        Position p = new Position(10.0, 20.0);
        assertNull(p.getLatitudeOnGreatCircle(new Position(30.0, 20.0), 25.0));
    }

    @Test
    public void latitudeOnGreatCircleCalculatesEquator() {
        Position p = new Position(0.0, 0.0);
        assertEquals(0.0, p.getLatitudeOnGreatCircle(new Position(0.0, 90.0), 45.0), 1e-10);
    }

    @Test
    public void longitudeOnGreatCircleReturnsCandidates() {
        Position.LongitudePair pair = new Position(10.0, 0.0)
                .getLongitudeOnGreatCircle(new Position(20.0, 90.0), 15.0);
        assertTrue(pair != null);
        assertEquals(15.645156661951619, pair.getLon1(), 1e-10);
        assertEquals(112.65869911429004, pair.getLon2(), 1e-10);
    }

    @Test
    public void longitudeOnGreatCircleReturnsNullWhenLatitudeIsNotCrossed() {
        assertNull(new Position(0.0, 0.0)
                .getLongitudeOnGreatCircle(new Position(0.0, 90.0), 90.0));
    }

    @Test
    public void earthLimbReturnsRequestedNumberOfPositions() {
        Position[] limb = new Position(0.0, 0.0, 1000.0).getEarthLimb(4);
        assertEquals(4, limb.length);
        assertEquals(63.434948822922, limb[0].getLat(), 1e-10);
        assertEquals(0.0, limb[0].getLon(), 1e-10);
        assertEquals(0.0, limb[1].getLat(), 1e-10);
        assertEquals(90.0, limb[1].getLon(), 1e-10);
        assertEquals(-63.434948822922, limb[2].getLat(), 1e-10);
        assertEquals(0.0, limb[2].getLon(), 1e-10);
    }

    @Test
    public void distanceAndBearingBetweenEquatorialPositions() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 1.0);
        assertEquals(111.195, start.getDistanceToKm(end), 0.01);
        assertEquals(90.0, start.getBearingDegrees(end), 1e-10);
    }

    @Test
    public void bearingDifferenceIsNormalized() {
        assertEquals(-20.0, Position.getBearingDifferenceDegrees(10.0, 30.0), 0.0);
        assertEquals(-20.0, Position.getBearingDifferenceDegrees(350.0, 10.0), 0.0);
    }

    @Test
    public void distanceToGreatCirclePathIsZeroOnPath() {
        Position point = new Position(0.0, 1.0);
        assertEquals(0.0, point.getDistanceKmToPath(new Position(0.0, 0.0),
                new Position(0.0, 2.0)), 1e-8);
    }

    @Test
    public void latitudeAndLongitudeDecimalMinutesFormatting() {
        assertEquals("12°30.00'N", Position.toDegreesMinutesDecimalMinutesLatitude(12.5));
        assertEquals("12°30.00'S", Position.toDegreesMinutesDecimalMinutesLatitude(-12.5));
        assertEquals("123°30.00'W", Position.toDegreesMinutesDecimalMinutesLongitude(-123.5));
        assertEquals("0°00.00'E", Position.toDegreesMinutesDecimalMinutesLongitude(0.0));
    }

    @Test
    public void positionAlongPathAtEndpointsAndMidpoint() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 10.0);
        Position actualStart = start.getPositionAlongPath(end, 0.0);
        assertEquals(start.getLat(), actualStart.getLat(), 1e-10);
        assertEquals(start.getLon(), actualStart.getLon(), 1e-10);

        Position actualEnd = start.getPositionAlongPath(end, 1.0);
        assertEquals(end.getLat(), actualEnd.getLat(), 1e-10);
        assertEquals(end.getLon(), actualEnd.getLon(), 1e-10);

        Position midpoint = start.getPositionAlongPath(end, 0.5);
        assertEquals(0.0, midpoint.getLat(), 1e-10);
        assertEquals(5.0, midpoint.getLon(), 1e-8);
    }

    @Test
    public void positionAlongPathRejectsInvalidProportion() {
        try {
            new Position(0.0, 0.0).getPositionAlongPath(new Position(1.0, 1.0), 1.1);
        } catch (RuntimeException e) {
            assertEquals("Proportion must be between 0 and 1 inclusive", e.getMessage());
            return;
        }
        throw new AssertionError("Expected RuntimeException");
    }

    @Test
    public void positionsAlongPathIncludesEndpoints() {
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
    public void to360AdjustsNegativeLongitude() {
        Position result = new Position(-10.0, -90.0, 5.0).to360();
        assertEquals(-10.0, result.getLat(), 0.0);
        assertEquals(270.0, result.getLon(), 0.0);
        assertEquals(0.0, result.getAlt(), 0.0);
    }

    @Test
    public void ensureContinuousAdjustsLongitudeAcrossDateLine() {
        Position result = new Position(0.0, -179.0)
                .ensureContinuous(new Position(0.0, 179.0));
        assertEquals(181.0, result.getLon(), 0.0);
        assertEquals(0.0, result.getLat(), 0.0);
    }

    @Test
    public void ensureContinuousReturnsSamePositionWhenNoJump() {
        Position p = new Position(0.0, 10.0);
        assertTrue(p == p.ensureContinuous(new Position(0.0, 20.0)));
    }

    @Test
    public void polygonContainment() {
        List<Position> polygon = Arrays.asList(new Position(0.0, 0.0),
                new Position(0.0, 10.0), new Position(10.0, 10.0),
                new Position(10.0, 0.0));
        assertTrue(new Position(5.0, 5.0).isWithin(polygon));
        assertFalse(new Position(20.0, 5.0).isWithin(polygon));
    }

    @Test
    public void equalityAndHashCodeUseLatitudeAndLongitude() {
        Position p = new Position(1.2, 3.4, 100.0);
        Position equal = new Position(1.2, 3.4, 500.0);
        assertTrue(p.equals(equal));
        assertEquals(4, p.hashCode());
        assertFalse(p.equals(null));
        assertFalse(p.equals("position"));
        assertNotEquals(p, new Position(1.2, 3.5));
    }

    @Test
    public void distanceToPathRequiresNonemptyPositions() {
        try {
            new Position(0.0, 0.0).getDistanceToPathKm(new ArrayList<Position>());
        } catch (RuntimeException e) {
            assertEquals("positions must not be empty", e.getMessage());
            return;
        }
        throw new AssertionError("Expected RuntimeException");
    }

    @Test
    public void distanceToPathWithOnePositionIsPointDistance() {
        Position point = new Position(0.0, 0.0);
        Position target = new Position(0.0, 1.0);
        assertEquals(target.getDistanceToKm(point),
                target.getDistanceToPathKm(Arrays.asList(point)), 1e-10);
    }

    @Test
    public void closestIntersectionWithDegenerateSegmentReturnsEndpoint() {
        Position endpoint = new Position(0.0, 0.0);
        Position result = new Position(1.0, 1.0)
                .getClosestIntersectionWithSegment(endpoint, endpoint);
        assertTrue(endpoint == result);
    }

    @Test
    public void distanceToSegmentForPointBeyondEndpointUsesClosestEndpoint() {
        Position point = new Position(0.0, 2.0);
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 1.0);
        assertEquals(point.getDistanceToKm(end), point.getDistanceToSegmentKm(start, end), 1e-8);
    }

    @Test
    public void outsideChecksPolygonAndDistance() {
        List<Position> polygon = Arrays.asList(new Position(0.0, 0.0),
                new Position(0.0, 10.0), new Position(10.0, 10.0),
                new Position(10.0, 0.0));
        assertFalse(new Position(5.0, 5.0).isOutside(polygon, 1.0));
        assertTrue(new Position(20.0, 5.0).isOutside(polygon, 100.0));
    }

    @Test
    public void longitudeDifferenceIsNonnegativeAcrossWrap() {
        assertEquals(340.0, Position.longitudeDiff(170.0, -170.0), 0.0);
        assertEquals(20.0, Position.longitudeDiff(-170.0, 170.0), 0.0);
    }

    @Test
    public void to180NormalizesAngles() {
        assertEquals(180.0, Position.to180(180.0), 0.0);
        assertEquals(-179.0, Position.to180(181.0), 0.0);
        assertEquals(170.0, Position.to180(-190.0), 0.0);
    }

    @Test
    public void normalizeLongitudePreservesAltitude() {
        Position p = new Position(10.0, 200.0, 300.0).normalizeLongitude();
        assertEquals(10.0, p.getLat(), 0.0);
        assertEquals(-160.0, p.getLon(), 0.0);
        assertEquals(300.0, p.getAlt(), 0.0);
    }
}
