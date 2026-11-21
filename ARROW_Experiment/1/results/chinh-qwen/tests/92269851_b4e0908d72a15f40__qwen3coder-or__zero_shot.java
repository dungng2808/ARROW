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
        SentinelVersion version = new SentinelVersion(2, 3, 4);
        assertEquals(2003004, version.getFullVersion());
    }

    @Test
    public void testGetMajorVersion() {
        SentinelVersion version = new SentinelVersion(5, 6, 7);
        assertEquals(5, version.getMajorVersion());
    }

    @Test
    public void testSetMajorVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setMajorVersion(8);
        assertEquals(8, version.getMajorVersion());
        assertSame(version, returned);
    }

    @Test
    public void testGetMinorVersion() {
        SentinelVersion version = new SentinelVersion(1, 9, 2);
        assertEquals(9, version.getMinorVersion());
    }

    @Test
    public void testSetMinorVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setMinorVersion(10);
        assertEquals(10, version.getMinorVersion());
        assertSame(version, returned);
    }

    @Test
    public void testGetFixVersion() {
        SentinelVersion version = new SentinelVersion(3, 4, 5);
        assertEquals(5, version.getFixVersion());
    }

    @Test
    public void testSetFixVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setFixVersion(11);
        assertEquals(11, version.getFixVersion());
        assertSame(version, returned);
    }

    @Test
    public void testGetPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "alpha");
        assertEquals("alpha", version.getPostfix());
    }

    @Test
    public void testSetPostfix() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setPostfix("rc");
        assertEquals("rc", version.getPostfix());
        assertSame(version, returned);
    }

    @Test
    public void testGreaterThanWithNullVersion() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertTrue(version.greaterThan(null));
    }

    @Test
    public void testGreaterThanTrueCase() {
        SentinelVersion version1 = new SentinelVersion(2, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 9, 9);
        assertTrue(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterThanFalseCase() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 1, 0);
        assertFalse(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterOrEqualWithNullVersion() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertTrue(version.greaterOrEqual(null));
    }

    @Test
    public void testGreaterOrEqualTrueCase() {
        SentinelVersion version1 = new SentinelVersion(2, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 9, 9);
        assertTrue(version1.greaterOrEqual(version2));
    }

    @Test
    public void testGreaterOrEqualEqualCase() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3);
        SentinelVersion version2 = new SentinelVersion(1, 2, 3);
        assertTrue(version1.greaterOrEqual(version2));
    }

    @Test
    public void testGreaterOrEqualFalseCase() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 1, 0);
        assertFalse(version1.greaterOrEqual(version2));
    }

    @Test
    public void testEqualsSameObject() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertTrue(version.equals(version));
    }

    @Test
    public void testEqualsNull() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertFalse(version.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertFalse(version.equals("not a version"));
    }

    @Test
    public void testEqualsSameValuesNoPostfix() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3);
        SentinelVersion version2 = new SentinelVersion(1, 2, 3);
        assertTrue(version1.equals(version2));
    }

    @Test
    public void testEqualsSameValuesWithPostfix() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "beta");
        assertTrue(version1.equals(version2));
    }

    @Test
    public void testEqualsDifferentPostfix() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "alpha");
        assertFalse(version1.equals(version2));
    }

    @Test
    public void testEqualsDifferentFullVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3);
        SentinelVersion version2 = new SentinelVersion(1, 2, 4);
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
