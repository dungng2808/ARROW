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
    }

    @Test
    public void testThreeParamConstructor() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getFixVersion());
        assertNull(version.getPostfix());
    }

    @Test
    public void testFourParamConstructor() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getFixVersion());
        assertEquals("beta", version.getPostfix());
    }

    @Test
    public void testGetFullVersion() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);
        assertEquals(1002003, version.getFullVersion());
    }

    @Test
    public void testGetFullVersionWithPostfix() {
        SentinelVersion version = new SentinelVersion(2, 5, 10, "rc1");
        assertEquals(2005010, version.getFullVersion());
    }

    @Test
    public void testSetMajorVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setMajorVersion(5);
        assertEquals(5, version.getMajorVersion());
        assertSame(version, returned);
    }

    @Test
    public void testSetMinorVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setMinorVersion(7);
        assertEquals(7, version.getMinorVersion());
        assertSame(version, returned);
    }

    @Test
    public void testSetFixVersion() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setFixVersion(9);
        assertEquals(9, version.getFixVersion());
        assertSame(version, returned);
    }

    @Test
    public void testSetPostfix() {
        SentinelVersion version = new SentinelVersion();
        SentinelVersion returned = version.setPostfix("alpha");
        assertEquals("alpha", version.getPostfix());
        assertSame(version, returned);
    }

    @Test
    public void testGreaterThanWithNullVersion() {
        SentinelVersion version = new SentinelVersion(1, 0, 0);
        assertTrue(version.greaterThan(null));
    }

    @Test
    public void testGreaterThanTrue() {
        SentinelVersion version1 = new SentinelVersion(2, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertTrue(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterThanFalse() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(2, 0, 0);
        assertFalse(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterThanEqual() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertFalse(version1.greaterThan(version2));
    }

    @Test
    public void testGreaterOrEqualWithNullVersion() {
        SentinelVersion version = new SentinelVersion(1, 0, 0);
        assertTrue(version.greaterOrEqual(null));
    }

    @Test
    public void testGreaterOrEqualTrue() {
        SentinelVersion version1 = new SentinelVersion(2, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertTrue(version1.greaterOrEqual(version2));
    }

    @Test
    public void testGreaterOrEqualFalse() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(2, 0, 0);
        assertFalse(version1.greaterOrEqual(version2));
    }

    @Test
    public void testGreaterOrEqualEqual() {
        SentinelVersion version1 = new SentinelVersion(1, 0, 0);
        SentinelVersion version2 = new SentinelVersion(1, 0, 0);
        assertTrue(version1.greaterOrEqual(version2));
    }

    @Test
    public void testEqualsSameObject() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "test");
        assertTrue(version.equals(version));
    }

    @Test
    public void testEqualsNull() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "test");
        assertFalse(version.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "test");
        assertFalse(version.equals("not a version"));
    }

    @Test
    public void testEqualsEqualVersions() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "test");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "test");
        assertTrue(version1.equals(version2));
    }

    @Test
    public void testEqualsDifferentFullVersion() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "test");
        SentinelVersion version2 = new SentinelVersion(1, 2, 4, "test");
        assertFalse(version1.equals(version2));
    }

    @Test
    public void testEqualsDifferentPostfix() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "test");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "beta");
        assertFalse(version1.equals(version2));
    }

    @Test
    public void testEqualsBothNullPostfix() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, null);
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, null);
        assertTrue(version1.equals(version2));
    }

    @Test
    public void testHashCode() {
        SentinelVersion version1 = new SentinelVersion(1, 2, 3, "test");
        SentinelVersion version2 = new SentinelVersion(1, 2, 3, "test");
        assertEquals(version1.hashCode(), version2.hashCode());
    }

    @Test
    public void testToString() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        String expected = "SentinelVersion{majorVersion=1, minorVersion=2, fixVersion=3, postfix='beta'}";
        assertEquals(expected, version.toString());
    }
}
