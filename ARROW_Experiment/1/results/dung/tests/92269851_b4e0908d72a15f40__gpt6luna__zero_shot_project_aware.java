package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class SentinelVersion_ArrowTest {

    @Test
    public void defaultConstructorInitializesZeroVersions() {
        SentinelVersion version = new SentinelVersion();

        assertEquals(0, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(0, version.getFixVersion());
        assertNull(version.getPostfix());
        assertEquals(0, version.getFullVersion());
    }

    @Test
    public void threeArgumentConstructorSetsVersions() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);

        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getFixVersion());
        assertNull(version.getPostfix());
        assertEquals(1002003, version.getFullVersion());
    }

    @Test
    public void fourArgumentConstructorSetsPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");

        assertEquals("beta", version.getPostfix());
        assertEquals(1002003, version.getFullVersion());
    }

    @Test
    public void settersUpdateValuesAndReturnSameInstance() {
        SentinelVersion version = new SentinelVersion();

        assertSame(version, version.setMajorVersion(2));
        assertSame(version, version.setMinorVersion(4));
        assertSame(version, version.setFixVersion(6));
        assertSame(version, version.setPostfix("rc1"));

        assertEquals(2, version.getMajorVersion());
        assertEquals(4, version.getMinorVersion());
        assertEquals(6, version.getFixVersion());
        assertEquals("rc1", version.getPostfix());
        assertEquals(2004006, version.getFullVersion());
    }

    @Test
    public void greaterThanComparesFullVersions() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);

        assertTrue(version.greaterThan(new SentinelVersion(1, 2, 2)));
        assertFalse(version.greaterThan(new SentinelVersion(1, 2, 3)));
        assertFalse(version.greaterThan(new SentinelVersion(1, 2, 4)));
    }

    @Test
    public void greaterThanNullIsTrue() {
        assertTrue(new SentinelVersion().greaterThan(null));
    }

    @Test
    public void greaterOrEqualComparesFullVersions() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);

        assertTrue(version.greaterOrEqual(new SentinelVersion(1, 2, 2)));
        assertTrue(version.greaterOrEqual(new SentinelVersion(1, 2, 3)));
        assertFalse(version.greaterOrEqual(new SentinelVersion(1, 2, 4)));
    }

    @Test
    public void greaterOrEqualNullIsTrue() {
        assertTrue(new SentinelVersion().greaterOrEqual(null));
    }

    @Test
    public void equalsUsesFullVersionAndPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");

        assertEquals(version, new SentinelVersion(1, 2, 3, "beta"));
        assertNotEquals(version, new SentinelVersion(1, 2, 3, "rc"));
        assertNotEquals(version, new SentinelVersion(1, 2, 4, "beta"));
        assertNotEquals(version, null);
        assertNotEquals(version, "1.2.3");
    }

    @Test
    public void equalsTreatsDifferentComponentsWithSameFullVersionAsEqual() {
        SentinelVersion first = new SentinelVersion(0, 1000, 0);
        SentinelVersion second = new SentinelVersion(1, 0, 0);

        assertEquals(first, second);
    }

    @Test
    public void hashCodeUsesVersionComponentsAndPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        int expected = 31 * (31 * (31 * 1 + 2) + 3) + "beta".hashCode();

        assertEquals(expected, version.hashCode());
    }

    @Test
    public void toStringIncludesAllFields() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");

        assertEquals("SentinelVersion{majorVersion=1, minorVersion=2, fixVersion=3, postfix='beta'}",
            version.toString());
    }
}
