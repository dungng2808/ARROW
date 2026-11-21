package com.github.davidmoten.grumpy.core;

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
    public void testPositionConstructor() {
        Position pos = new Position(45.0, 90.0);
        assertEquals(45.0, pos.getLat(), 0.0);
        assertEquals(90.0, pos.getLon(), 0.0);
        assertEquals(0.0, pos.getAlt(), 0.0);
    }

    @Test
    public void testPositionConstructorWithAltitude() {
        Position pos = new Position(45.0, 90.0, 100.0);
        assertEquals(45.0, pos.getLat(), 0.0);
        assertEquals(90.0, pos.getLon(), 0.0);
        assertEquals(100.0, pos.getAlt(), 0.0);
    }

    @Test
    public void testCreate() {
        Position pos = Position.create(45.0, 90.0);
        assertEquals(45.0, pos.getLat(), 0.0);
        assertEquals(90.0, pos.getLon(), 0.0);
    }

    @Test
    public void testPositionFactoryMethod() {
        Position pos = Position.position(45.0, 90.0);
        assertEquals(45.0, pos.getLat(), 0.0);
        assertEquals(90.0, pos.getLon(), 0.0);
    }

    @Test
    public void testCreateWithAltitude() {
        Position pos = Position.create(45.0, 90.0, 100.0);
        assertEquals(45.0, pos.getLat(), 0.0);
        assertEquals(90.0, pos.getLon(), 0.0);
        assertEquals(100.0, pos.getAlt(), 0.0);
    }

    @Test
    public void testToString() {
        Position pos = new Position(45.0, 90.0);
        assertEquals("[45.0,90.0]", pos.toString());
    }

    @Test
    public void testPredict() {
        Position pos = new Position(0.0, 0.0);
        Position predicted = pos.predict(1000.0, 90.0);
        assertNotNull(predicted);
        assertEquals(0.0, predicted.getLat(), 0.1);
        assertEquals(8.98, predicted.getLon(), 0.1);
    }

    @Test
    public void testToDegrees() {
        double result = Position.toDegrees(10.0, 30.0, 45.0);
        assertEquals(10.5125, result, 0.0001);
    }

    @Test
    public void testGetLatitudeOnGreatCircleNull() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 180.0);
        Double result = pos1.getLatitudeOnGreatCircle(pos2, 90.0);
        assertNull(result);
    }

    @Test
    public void testGetLatitudeOnGreatCircle() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(90.0, 0.0);
        Double result = pos1.getLatitudeOnGreatCircle(pos2, 0.0);
        assertNotNull(result);
        assertEquals(0.0, result.doubleValue(), 0.0001);
    }

    @Test
    public void testGetLongitudeOnGreatCircleNull() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 180.0);
        Position.LongitudePair result = pos1.getLongitudeOnGreatCircle(pos2, 45.0);
        assertNull(result);
    }

    @Test
    public void testGetLongitudeOnGreatCircle() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(90.0, 0.0);
        Position.LongitudePair result = pos1.getLongitudeOnGreatCircle(pos2, 0.0);
        assertNotNull(result);
        assertEquals(0.0, result.getLon1(), 0.0001);
        assertEquals(0.0, result.getLon2(), 0.0001);
    }

    @Test
    public void testGetEarthLimb() {
        Position pos = new Position(0.0, 0.0, 1000.0);
        Position[] limb = pos.getEarthLimb(4);
        assertNotNull(limb);
        assertEquals(4, limb.length);
    }

    @Test
    public void testGetDistanceToKm() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        double distance = pos1.getDistanceToKm(pos2);
        assertEquals(111.19, distance, 0.01);
    }

    @Test
    public void testGetBearingDegrees() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        double bearing = pos1.getBearingDegrees(pos2);
        assertEquals(90.0, bearing, 0.01);
    }

    @Test
    public void testGetBearingDifferenceDegrees() {
        double diff = Position.getBearingDifferenceDegrees(350.0, 10.0);
        assertEquals(-20.0, diff, 0.01);
    }

    @Test
    public void testGetDistanceKmToPath() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        Position pos3 = new Position(1.0, 0.0);
        double distance = pos1.getDistanceKmToPath(pos2, pos3);
        assertEquals(78.63080204825093, distance, 0.01);
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
    public void testGetPositionAlongPath() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        Position result = pos1.getPositionAlongPath(pos2, 0.5);
        assertNotNull(result);
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(0.5, result.getLon(), 0.0001);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPositionAlongPathInvalidProportion() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        pos1.getPositionAlongPath(pos2, 1.5);
    }

    @Test
    public void testGetPositionsAlongPath() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        List<Position> positions = pos1.getPositionsAlongPath(pos2, 100.0);
        assertNotNull(positions);
        assertTrue(positions.size() > 0);
    }

    @Test
    public void testTo360() {
        Position pos = new Position(0.0, -10.0);
        Position result = pos.to360();
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(350.0, result.getLon(), 0.0);
    }

    @Test
    public void testEnsureContinuous() {
        Position pos1 = new Position(0.0, 179.0);
        Position pos2 = new Position(0.0, -179.0);
        Position result = pos1.ensureContinuous(pos2);
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(-181.0, result.getLon(), 0.0);
    }

    @Test
    public void testIsWithin() {
        List<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0.0, 0.0));
        positions.add(new Position(0.0, 1.0));
        positions.add(new Position(1.0, 1.0));
        positions.add(new Position(1.0, 0.0));
        Position pos = new Position(0.5, 0.5);
        boolean result = pos.isWithin(positions);
        assertTrue(result);
    }

    @Test
    public void testEquals() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 0.0);
        assertTrue(pos1.equals(pos2));
    }

    @Test
    public void testHashCode() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 0.0);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    public void testGetDistanceToPathKm() {
        List<Position> positions = new ArrayList<Position>();
        positions.add(new Position(0.0, 0.0));
        positions.add(new Position(0.0, 1.0));
        Position pos = new Position(1.0, 0.0);
        double distance = pos.getDistanceToPathKm(positions);
        assertEquals(111.19, distance, 0.01);
    }

    @Test
    public void testGetDistanceToSegmentKm() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        Position pos3 = new Position(1.0, 0.0);
        double distance = pos1.getDistanceToSegmentKm(pos2, pos3);
        assertEquals(78.6308021495952, distance, 0.01);
    }

    @Test
    public void testGetClosestIntersectionWithSegment() {
        Position pos1 = new Position(0.0, 0.0);
        Position pos2 = new Position(0.0, 1.0);
        Position pos3 = new Position(1.0, 0.0);
        Position result = pos1.getClosestIntersectionWithSegment(pos2, pos3);
        assertNotNull(result);
    }

    @Test
    public void testIsOutside() {
        List<Position> path = new ArrayList<Position>();
        path.add(new Position(0.0, 0.0));
        path.add(new Position(0.0, 1.0));
        Position pos = new Position(1.0, 0.0);
        boolean result = pos.isOutside(path, 100.0);
        assertTrue(result);
    }

    @Test
    public void testLongitudeDiff() {
        double diff = Position.longitudeDiff(10.0, 20.0);
        assertEquals(10.0, diff, 0.0001);
    }

    @Test
    public void testTo180() {
        double result = Position.to180(370.0);
        assertEquals(10.0, result, 0.0);
    }

    @Test
    public void testNormalizeLongitude() {
        Position pos = new Position(0.0, 200.0, 0.0);
        Position result = pos.normalizeLongitude();
        assertEquals(0.0, result.getLat(), 0.0);
        assertEquals(-160.0, result.getLon(), 0.0);
    }
}
