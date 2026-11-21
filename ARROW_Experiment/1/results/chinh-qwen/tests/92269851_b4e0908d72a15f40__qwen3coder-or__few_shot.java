package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class SentinelVersion_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        SentinelVersion version = new SentinelVersion();
        assertEquals(0, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(0, version.getFixVersion());
        assertNull(version.getPostfix());
        assertEquals(0, version.getFullVersion());
    }

    @Test
    public void testThreeParamConstructor() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getFixVersion());
        assertNull(version.getPostfix());
        assertEquals(1002003, version.getFullVersion());
    }

    @Test
    public void testFourParamConstructor() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getFixVersion());
        assertEquals("beta", version.getPostfix());
        assertEquals(1002003, version.getFullVersion());
    }

    @Test
    public void testGetFullVersion() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertEquals(1002003, version.getFullVersion());

        SentinelVersion version2 = new SentinelVersion(0, 0, 0);
        assertEquals(0, version2.getFullVersion());

        SentinelVersion version3 = new SentinelVersion(9, 9, 9);
        assertEquals(9009009, version3.getFullVersion());
    }

    @Test
    public void testSetMajorVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setMajorVersion(5);
        assertSame(version, returned);
        assertEquals(5, version.getMajorVersion());
    }

    @Test
    public void testSetMinorVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setMinorVersion(5);
        assertSame(version, returned);
        assertEquals(5, version.getMinorVersion());
    }

    @Test
    public void testSetFixVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setFixVersion(5);
        assertSame(version, returned);
        assertEquals(5, version.getFixVersion());
    }

    @Test
    public void testSetPostfix() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setPostfix("alpha");
        assertSame(version, returned);
        assertEquals("alpha", version.getPostfix());
    }

    @Test
    public void testGreaterThanWithNullVersion() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertTrue(version.greaterThan(null));
    }

    @Test
    public void testGreaterThanWithGreaterVersion() {
        SentinelVersion version1 = new SentinelVersion(2, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertTrue(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterThanWithLesserVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(2, 0, 0);
        assertFalse(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterThanWithEqualVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertFalse(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterOrEqualWithNullVersion() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertTrue(version.greaterOrEqual(null));
    }

    @Test
    public void testGreaterOrEqualWithGreaterVersion() {
        SentinelVersion version1 = new SentinelVersion(2, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertTrue(version1.greaterOrEqual(version2));
    }

    @Test
    public void testGreaterOrEqualWithLesserVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(2, 0, 0);
        assertFalse(version1.greaterOrEqual(version2));
    }

    @Test
    public void testGreaterOrEqualWithEqualVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertTrue(version1.greaterOrEqual(version2));
    }

    @Test
    public void testEqualsWithSameObject() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        assertTrue(version.equals(version));
    }

    @Test
    public void testEqualsWithNull() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        assertFalse(version.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        assertFalse(version.equals("not a version"));
    }

    @Test
    public void testEqualsWithEqualVersions() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "beta");
        assertTrue(version1.equals(version2));
    }

    @Test
    public void testEqualsWithDifferentPostfix() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "alpha");
        assertFalse(version1.equals(version2));
    }

    @Test
    public void testEqualsWithDifferentFullVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion version2 = new SentinelVersion(1, 2, 4, "beta");
        assertFalse(version1.equals(version2));
    }

    @Test
    public void testHashCode() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "beta");
        assertEquals(version1.hashCode(), version2.hashCode());
    }

    @Test
    public void testToString() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        String expected = "SentinelVersion{majorVersion=1, minorVersion=2, fixVersion=3, postfix='beta'}";
        assertEquals(expected, version.toString());
    }
}
