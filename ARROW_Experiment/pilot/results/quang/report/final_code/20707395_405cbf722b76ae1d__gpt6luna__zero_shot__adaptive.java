package com.github.davidmoten.grumpy.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class Position_ArrowTest {

    @Test
    public void constructorsAndFactoriesPreserveCoordinates() {
        Position p = new Position(12.5, -45.25);
        assertEquals(12.5, p.getLat(), 0.0);
        assertEquals(-45.25, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);

        Position q = Position.create(-1.0, 2.0, 300.0);
        assertEquals(-1.0, q.getLat(), 0.0);
        assertEquals(2.0, q.getLon(), 0.0);
        assertEquals(300.0, q.getAlt(), 0.0);
        assertEquals(new Position(3.0, 4.0), Position.position(3.0, 4.0));
    }

    @Test
    public void stringRepresentationContainsLatitudeAndLongitude() {
        assertEquals("[12.5,-45.25]", new Position(12.5, -45.25).toString());
    }

    @Test
    public void degreesMinutesSecondsConversion() {
        assertEquals(10.5, Position.toDegrees(10.0, 30.0, 0.0), 0.0);
    }

    @Test
    public void predictTravelsNorthAlongMeridian() {
        Position p = new Position(0.0, 0.0).predict(1000.0, 0.0);
        assertEquals(8.9932, p.getLat(), 0.001);
        assertEquals(0.0, p.getLon(), 1e-10);
    }

    @Test
    public void latitudeOnGreatCircleReturnsNullForSameLongitude() {
        assertNull(new Position(0.0, 0.0).getLatitudeOnGreatCircle(new Position(10.0, 0.0), 5.0));
    }

    @Test
    public void latitudeOnGreatCircleForEquatorialArcIsZero() {
        Double latitude = new Position(0.0, 0.0).getLatitudeOnGreatCircle(new Position(0.0, 90.0), 45.0);
        assertNotNull(latitude);
        assertEquals(0.0, latitude.doubleValue(), 1e-10);
    }

    @Test
    public void longitudeOnGreatCircleReturnsCandidates() {
        Position.LongitudePair pair = new Position(0.0, 0.0)
                .getLongitudeOnGreatCircle(new Position(10.0, 90.0), 5.0);
        assertNotNull(pair);
        assertEquals(29.747120281201713, pair.getLon1(), 1e-10);
        assertEquals(150.25287971879828, pair.getLon2(), 1e-10);
        assertEquals("LongitudePair [lon1=29.747120281201713, lon2=150.25287971879828]", pair.toString());
    }

    @Test
    public void longitudeOnGreatCircleReturnsNullWhenLatitudeDoesNotCross() {
        assertNull(new Position(0.0, 0.0).getLongitudeOnGreatCircle(new Position(0.0, 90.0), 90.0));
    }

    @Test
    public void earthLimbHasRequestedNumberOfPoints() {
        Position[] points = new Position(0.0, 0.0, 1000.0).getEarthLimb(4);
        assertEquals(4, points.length);
        assertEquals(90.0, points[0].getLat(), 1e-10);
        assertEquals(0.0, points[0].getLon(), 1e-10);
        assertEquals(0.0, points[1].getLat(), 1e-10);
        assertEquals(90.0, points[1].getLon(), 1e-10);
    }

    @Test
    public void distanceAndBearingForEquatorialPoints() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 1.0);
        assertEquals(111.195, start.getDistanceToKm(end), 0.01);
        assertEquals(90.0, start.getBearingDegrees(end), 1e-10);
    }

    @Test
    public void bearingDifferenceNormalizesAsImplemented() {
        assertEquals(20.0, Position.getBearingDifferenceDegrees(-10.0, 330.0), 0.0);
        assertEquals(-170.0, Position.getBearingDifferenceDegrees(10.0, 180.0), 0.0);
    }

    @Test
    public void distanceToPathIsZeroForPointOnEquatorialPath() {
        Position point = new Position(0.0, 5.0);
        assertEquals(0.0, point.getDistanceKmToPath(new Position(0.0, 0.0), new Position(0.0, 10.0)), 1e-8);
    }

    @Test
    public void coordinateFormattingUsesHemisphereSuffixes() {
        assertEquals("12°30.00'N", Position.toDegreesMinutesDecimalMinutesLatitude(12.5));
        assertEquals("12°30.00'S", Position.toDegreesMinutesDecimalMinutesLatitude(-12.5));
        assertEquals("45°15.00'W", Position.toDegreesMinutesDecimalMinutesLongitude(-45.25));
        assertEquals("45°15.00'E", Position.toDegreesMinutesDecimalMinutesLongitude(45.25));
    }

    @Test
    public void assertWithMsgDoesNotThrowWhenTrue() {
        Position.assertWithMsg(true, "ok");
        assertTrue(true);
    }

    @Test(expected = RuntimeException.class)
    public void assertWithMsgThrowsWhenFalse() {
        Position.assertWithMsg(false, "failure");
    }

    @Test
    public void positionAlongPathReturnsEndpointsAtZeroAndOne() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 10.0);
        Position atStart = start.getPositionAlongPath(end, 0.0);
        Position atEnd = start.getPositionAlongPath(end, 1.0);
        assertEquals(start.getLat(), atStart.getLat(), 1e-10);
        assertEquals(start.getLon(), atStart.getLon(), 1e-10);
        assertEquals(0.0, atStart.getAlt(), 0.0);
        assertEquals(end.getLat(), atEnd.getLat(), 1e-10);
        assertEquals(end.getLon(), atEnd.getLon(), 1e-10);
        assertEquals(0.0, atEnd.getAlt(), 0.0);
    }

    @Test(expected = RuntimeException.class)
    public void positionAlongPathRejectsProportionBelowZero() {
        new Position(0.0, 0.0).getPositionAlongPath(new Position(0.0, 1.0), -0.1);
    }

    @Test(expected = RuntimeException.class)
    public void positionAlongPathRejectsProportionAboveOne() {
        new Position(0.0, 0.0).getPositionAlongPath(new Position(0.0, 1.0), 1.1);
    }

    @Test
    public void positionsAlongPathIncludesBothEndpoints() {
        Position start = new Position(0.0, 0.0);
        Position end = new Position(0.0, 1.0);
        List<Position> points = start.getPositionsAlongPath(end, 1000.0);
        assertEquals(2, points.size());
        assertSame(start, points.get(0));
        assertSame(end, points.get(1));
    }

    @Test
    public void to360Adds360ToNegativeLongitude() {
        Position p = new Position(10.0, -20.0).to360();
        assertEquals(10.0, p.getLat(), 0.0);
        assertEquals(340.0, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);
    }

    @Test
    public void ensureContinuousAdjustsLongitudeAcrossDateline() {
        Position adjusted = new Position(0.0, -179.0).ensureContinuous(new Position(0.0, 179.0));
        assertEquals(181.0, adjusted.getLon(), 0.0);
        assertEquals(0.0, adjusted.getLat(), 0.0);
    }

    @Test
    public void ensureContinuousReturnsSamePositionWhenNoAdjustmentNeeded() {
        Position p = new Position(0.0, 10.0);
        assertSame(p, p.ensureContinuous(new Position(0.0, 20.0)));
    }

    @Test
    public void isWithinDetectsPointInsidePolygon() {
        List<Position> polygon = Arrays.asList(new Position(-1.0, -1.0), new Position(-1.0, 1.0),
                new Position(1.0, 1.0), new Position(1.0, -1.0));
        assertTrue(new Position(0.0, 0.0).isWithin(polygon));
    }

    @Test
    public void equalityIgnoresAltitude() {
        assertEquals(new Position(1.0, 2.0, 10.0), new Position(1.0, 2.0, 20.0));
        assertNotEquals(new Position(1.0, 2.0), new Position(1.0, 3.0));
        assertFalse(new Position(1.0, 2.0).equals(null));
        assertEquals(3, new Position(1.0, 2.0).hashCode());
    }

    @Test
    public void distanceToPathWithOnePointIsDistanceToThatPoint() {
        Position p = new Position(0.0, 0.0);
        assertEquals(p.getDistanceToKm(new Position(0.0, 1.0)),
                p.getDistanceToPathKm(Collections.singletonList(new Position(0.0, 1.0))), 0.0);
    }

    @Test(expected = RuntimeException.class)
    public void distanceToPathRejectsEmptyList() {
        new Position(0.0, 0.0).getDistanceToPathKm(Collections.<Position>emptyList());
    }

    @Test
    public void closestIntersectionForIdenticalSegmentEndpointsReturnsEndpoint() {
        Position endpoint = new Position(0.0, 1.0);
        assertSame(endpoint, new Position(1.0, 1.0).getClosestIntersectionWithSegment(endpoint, endpoint));
    }

    @Test
    public void distanceToSegmentForIdenticalEndpointsIsDistanceToEndpoint() {
        Position point = new Position(0.0, 0.0);
        Position endpoint = new Position(0.0, 1.0);
        assertEquals(point.getDistanceToKm(endpoint), point.getDistanceToSegmentKm(endpoint, endpoint), 0.0);
    }

    @Test
    public void isOutsideUsesPolygonAndMinimumDistance() {
        List<Position> polygon = Arrays.asList(new Position(-1.0, -1.0), new Position(-1.0, 1.0),
                new Position(1.0, 1.0), new Position(1.0, -1.0));
        assertFalse(new Position(0.0, 0.0).isOutside(polygon, 0.0));
    }

    @Test
    public void longitudeDifferenceIsNonnegative() {
        assertEquals(20.0, Position.longitudeDiff(10.0, 350.0), 0.0);
        assertEquals(340.0, Position.longitudeDiff(350.0, 10.0), 0.0);
    }

    @Test
    public void to180NormalizesAngles() {
        assertEquals(180.0, Position.to180(180.0), 0.0);
        assertEquals(-179.0, Position.to180(181.0), 0.0);
        assertEquals(179.0, Position.to180(-181.0), 0.0);
    }

    @Test
    public void normalizeLongitudePreservesLatitudeAndAltitude() {
        Position p = new Position(12.0, 190.0, 50.0).normalizeLongitude();
        assertEquals(12.0, p.getLat(), 0.0);
        assertEquals(-170.0, p.getLon(), 0.0);
        assertEquals(50.0, p.getAlt(), 0.0);
    }
}
