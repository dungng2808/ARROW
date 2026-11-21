package com.github.davidmoten.grumpy.core;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class Position_ArrowTest {

    @Test
    public void testConstructor() {
        Position p = new Position(45.0, 90.0);
        assertEquals(45.0, p.getLat(), 0.0);
        assertEquals(90.0, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);
    }

    @Test
    public void testConstructorWithAltitude() {
        Position p = new Position(45.0, 90.0, 100.0);
        assertEquals(45.0, p.getLat(), 0.0);
        assertEquals(90.0, p.getLon(), 0.0);
        assertEquals(100.0, p.getAlt(), 0.0);
    }

    @Test
    public void testCreate() {
        Position p = Position.create(45.0, 90.0);
        assertEquals(45.0, p.getLat(), 0.0);
        assertEquals(90.0, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);
    }

    @Test
    public void testPosition() {
        Position p = Position.position(45.0, 90.0);
        assertEquals(45.0, p.getLat(), 0.0);
        assertEquals(90.0, p.getLon(), 0.0);
        assertEquals(0.0, p.getAlt(), 0.0);
    }

    @Test
    public void testCreateWithAltitude() {
        Position p = Position.create(45.0, 90.0, 100.0);
        assertEquals(45.0, p.getLat(), 0.0);
        assertEquals(90.0, p.getLon(), 0.0);
        assertEquals(100.0, p.getAlt(), 0.0);
    }

    @Test
    public void testToString() {
        Position p = new Position(45.0, 90.0);
        assertEquals("[45.0,90.0]", p.toString());
    }

    @Test
    public void testPredict() {
        Position p = new Position(0.0, 0.0);
        Position predicted = p.predict(1000.0, 90.0);
        assertNotNull(predicted);
        assertEquals(0.0, predicted.getLat(), 0.01);
        assertEquals(8.99, predicted.getLon(), 0.01);
    }

    @Test(expected = RuntimeException.class)
    public void testPredictWithAltitude() {
        Position p = new Position(0.0, 0.0, 100.0);
        p.predict(1000.0, 90.0);
    }

    @Test
    public void testToDegrees() {
        double result = Position.toDegrees(10.0, 30.0, 45.0);
        assertEquals(10.5125, result, 0.0001);
    }

    @Test
    public void testGetLatitudeOnGreatCircleNull() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 180.0);
        Double result = p1.getLatitudeOnGreatCircle(p2, 90.0);
        assertNull(result);
    }

    @Test
    public void testGetLatitudeOnGreatCircle() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(90.0, 0.0);
        Double result = p1.getLatitudeOnGreatCircle(p2, 0.0);
        assertNotNull(result);
        assertEquals(0.0, result.doubleValue(), 0.01);
    }

    @Test
    public void testGetLongitudeOnGreatCircleNull() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 180.0);
        Position.LongitudePair result = p1.getLongitudeOnGreatCircle(p2, 45.0);
        assertNull(result);
    }

    @Test
    public void testGetLongitudeOnGreatCircle() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        Position.LongitudePair result = p1.getLongitudeOnGreatCircle(p2, 0.0);
        assertNotNull(result);
        assertEquals(-180.0, result.getLon1(), 0.01);
        assertEquals(0.0, result.getLon2(), 0.01);
    }

    @Test
    public void testGetEarthLimb() {
        Position p = new Position(0.0, 0.0, 1000.0);
        Position[] result = p.getEarthLimb(4);
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals(0.0, result[0].getLat(), 0.01);
        assertEquals(0.0, result[0].getLon(), 0.01);
    }

    @Test
    public void testGetDistanceToKm() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        double distance = p1.getDistanceToKm(p2);
        assertEquals(10007.559105973554, distance, 0.01);
    }

    @Test
    public void testGetBearingDegrees() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        double bearing = p1.getBearingDegrees(p2);
        assertEquals(90.0, bearing, 0.01);
    }

    @Test
    public void testGetBearingDifferenceDegrees() {
        double diff = Position.getBearingDifferenceDegrees(350.0, 10.0);
        assertEquals(-20.0, diff, 0.01);
    }

    @Test
    public void testGetDistanceKmToPath() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        Position p3 = new Position(45.0, 0.0);
        double distance = p1.getDistanceKmToPath(p2, p3);
        assertEquals(5003.77, distance, 0.01);
    }

    @Test
    public void testToDegreesMinutesDecimalMinutesLatitude() {
        String result = Position.toDegreesMinutesDecimalMinutesLatitude(45.5);
        assertEquals("45°30.00'N", result);
    }

    @Test
    public void testToDegreesMinutesDecimalMinutesLongitude() {
        String result = Position.toDegreesMinutesDecimalMinutesLongitude(-90.5);
        assertEquals("90°30.00'W", result);
    }

    @Test
    public void testMod() {
        double result = Position.to180(370.0);
        assertEquals(10.0, result, 0.01);
    }

    @Test
    public void testGetPositionAlongPath() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        Position result = p1.getPositionAlongPath(p2, 0.5);
        assertNotNull(result);
        assertEquals(0.0, result.getLat(), 0.01);
        assertEquals(45.0, result.getLon(), 0.01);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPositionAlongPathInvalidProportion() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        p1.getPositionAlongPath(p2, 1.5);
    }

    @Test
    public void testGetPositionsAlongPath() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        List<Position> result = p1.getPositionsAlongPath(p2, 1000.0);
        assertNotNull(result);
        assertEquals(12, result.size());
        assertEquals(0.0, result.get(0).getLat(), 0.01);
        assertEquals(0.0, result.get(0).getLon(), 0.01);
        assertEquals(0.0, result.get(1).getLat(), 0.01);
        assertEquals(8.18, result.get(1).getLon(), 0.01);
        assertEquals(0.0, result.get(2).getLat(), 0.01);
        assertEquals(16.36, result.get(2).getLon(), 0.01);
        assertEquals(0.0, result.get(3).getLat(), 0.01);
        assertEquals(24.55, result.get(3).getLon(), 0.01);
        assertEquals(0.0, result.get(4).getLat(), 0.01);
        assertEquals(32.73, result.get(4).getLon(), 0.01);
        assertEquals(0.0, result.get(5).getLat(), 0.01);
        assertEquals(40.91, result.get(5).getLon(), 0.01);
        assertEquals(0.0, result.get(6).getLat(), 0.01);
        assertEquals(49.09, result.get(6).getLon(), 0.01);
        assertEquals(0.0, result.get(7).getLat(), 0.01);
        assertEquals(57.27, result.get(7).getLon(), 0.01);
        assertEquals(0.0, result.get(8).getLat(), 0.01);
        assertEquals(65.45, result.get(8).getLon(), 0.01);
        assertEquals(0.0, result.get(9).getLat(), 0.01);
        assertEquals(73.64, result.get(9).getLon(), 0.01);
        assertEquals(0.0, result.get(10).getLat(), 0.01);
        assertEquals(81.82, result.get(10).getLon(), 0.01);
        assertEquals(0.0, result.get(11).getLat(), 0.01);
        assertEquals(90.0, result.get(11).getLon(), 0.01);
    }

    @Test
    public void testTo360() {
        Position p = new Position(0.0, -10.0);
        Position result = p.to360();
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(350.0, result.getLon(), 0.0);
    }

    @Test
    public void testEnsureContinuous() {
        Position p1 = new Position(0.0, 170.0);
        Position p2 = new Position(0.0, -170.0);
        Position result = p1.ensureContinuous(p2);
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(-190.0, result.getLon(), 0.0);
    }

    @Test
    public void testIsWithin() {
        List<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0.0, 0.0));
        positions.add(new Position(0.0, 10.0));
        positions.add(new Position(10.0, 10.0));
        positions.add(new Position(10.0, 0.0));
        Position p = new Position(5.0, 5.0);
        boolean result = p.isWithin(positions);
        assertTrue(result);
    }

    @Test
    public void testIsWithinFalse() {
        List<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0.0, 0.0));
        positions.add(new Position(0.0, 10.0));
        positions.add(new Position(10.0, 10.0));
        positions.add(new Position(10.0, 0.0));
        Position p = new Position(15.0, 15.0);
        boolean result = p.isWithin(positions);
        assertFalse(result);
    }

    @Test
    public void testGetDistanceToPathKmSinglePosition() {
        List<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0.0, 0.0));
        Position p = new Position(0.0, 90.0);
        double distance = p.getDistanceToPathKm(positions);
        assertEquals(10007.559105973554, distance, 0.01);
    }

    @Test
    public void testGetDistanceToPathKmMultiplePositions() {
        List<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0.0, 0.0));
        positions.add(new Position(0.0, 90.0));
        Position p = new Position(45.0, 0.0);
        double distance = p.getDistanceToPathKm(positions);
        assertEquals(5003.77, distance, 0.01);
    }

    @Test(expected = RuntimeException.class)
    public void testGetDistanceToPathKmEmptyList() {
        List<Position> positions = new ArrayList<Position>();
        Position p = new Position(0.0, 0.0);
        p.getDistanceToPathKm(positions);
    }

    @Test
    public void testGetDistanceToSegmentKm() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        Position p3 = new Position(45.0, 0.0);
        double distance = p1.getDistanceToSegmentKm(p2, p3);
        assertEquals(5635.94, distance, 0.01);
    }

    @Test
    public void testGetClosestIntersectionWithSegment() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 90.0);
        Position p3 = new Position(45.0, 0.0);
        Position result = p1.getClosestIntersectionWithSegment(p2, p3);
        assertNotNull(result);
        assertEquals(0.0, result.getLat(), 0.01);
        assertEquals(0.0, result.getLon(), 0.01);
    }

    @Test
    public void testIsOutsideTrue() {
        List<Position> path = new ArrayList<Position>();
        path.add(new Position(0.0, 0.0));
        path.add(new Position(0.0, 90.0));
        Position p = new Position(45.0, 0.0);
        boolean result = p.isOutside(path, 1000.0);
        assertTrue(result);
    }

    @Test
    public void testIsOutsideFalse() {
        List<Position> path = new ArrayList<Position>();
        path.add(new Position(0.0, 0.0));
        path.add(new Position(0.0, 90.0));
        Position p = new Position(0.0, 45.0);
        boolean result = p.isOutside(path, 1000.0);
        assertFalse(result);
    }

    @Test
    public void testLongitudeDiff() {
        double diff = Position.longitudeDiff(10.0, 20.0);
        assertEquals(350.0, diff, 0.01);
    }

    @Test
    public void testTo180() {
        double result = Position.to180(370.0);
        assertEquals(10.0, result, 0.01);
    }

    @Test
    public void testNormalizeLongitude() {
        Position p = new Position(0.0, 200.0, 0.0);
        Position result = p.normalizeLongitude();
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(-160.0, result.getLon(), 0.0);
    }

    @Test
    public void testEquals() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 0.0);
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsFalse() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(1.0, 0.0);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsNull() {
        Position p1 = new Position(0.0, 0.0);
        assertFalse(p1.equals(null));
    }

    @Test
    public void testEqualsNotPosition() {
        Position p1 = new Position(0.0, 0.0);
        assertFalse(p1.equals("not a position"));
    }

    @Test
    public void testHashCode() {
        Position p1 = new Position(0.0, 0.0);
        Position p2 = new Position(0.0, 0.0);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
