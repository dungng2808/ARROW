package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SentinelVersion_ArrowTest {

    @Test
    public void defaultConstructorInitializesZeroVersionsAndNullPostfix() {
        SentinelVersion version = new SentinelVersion();

        assertEquals(0, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(0, version.getFixVersion());
        assertEquals(0, version.getFullVersion());
        assertEquals(null, version.getPostfix());
    }

    @Test
    public void constructorsSetVersionComponentsAndPostfix() {
        SentinelVersion withoutPostfix = new SentinelVersion(1, 2, 3);
        SentinelVersion withPostfix = new SentinelVersion(4, 5, 6, "beta");

        assertEquals(1, withoutPostfix.getMajorVersion());
        assertEquals(2, withoutPostfix.getMinorVersion());
        assertEquals(3, withoutPostfix.getFixVersion());
        assertEquals(null, withoutPostfix.getPostfix());

        assertEquals(4, withPostfix.getMajorVersion());
        assertEquals(5, withPostfix.getMinorVersion());
        assertEquals(6, withPostfix.getFixVersion());
        assertEquals("beta", withPostfix.getPostfix());
    }

    @Test
    public void fullVersionCombinesComponents() {
        SentinelVersion version = new SentinelVersion(2, 34, 567);

        assertEquals(2034567, version.getFullVersion());
    }

    @Test
    public void settersUpdateValuesAndReturnSameInstance() {
        SentinelVersion version = new SentinelVersion();

        assertSame(version, version.setMajorVersion(3));
        assertSame(version, version.setMinorVersion(12));
        assertSame(version, version.setFixVersion(45));
        assertSame(version, version.setPostfix("rc1"));

        assertEquals(3, version.getMajorVersion());
        assertEquals(12, version.getMinorVersion());
        assertEquals(45, version.getFixVersion());
        assertEquals("rc1", version.getPostfix());
    }

    @Test
    public void greaterThanComparesFullVersionAndTreatsNullAsLower() {
        SentinelVersion version = new SentinelVersion(2, 0, 0);

        assertTrue(version.greaterThan(new SentinelVersion(1, 999, 999)));
        assertFalse(version.greaterThan(new SentinelVersion(2, 0, 0)));
        assertFalse(version.greaterThan(new SentinelVersion(3, 0, 0)));
        assertTrue(version.greaterThan(null));
    }

    @Test
    public void greaterOrEqualComparesFullVersionAndTreatsNullAsLower() {
        SentinelVersion version = new SentinelVersion(2, 0, 0);

        assertTrue(version.greaterOrEqual(new SentinelVersion(1, 999, 999)));
        assertTrue(version.greaterOrEqual(new SentinelVersion(2, 0, 0)));
        assertFalse(version.greaterOrEqual(new SentinelVersion(3, 0, 0)));
        assertTrue(version.greaterOrEqual(null));
    }

    @Test
    public void equalsAndHashCodeUseVersionAndPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion equalVersion = new SentinelVersion(1, 2, 3, "beta");

        assertTrue(version.equals(version));
        assertTrue(version.equals(equalVersion));
        assertEquals(version.hashCode(), equalVersion.hashCode());
        assertFalse(version.equals(new SentinelVersion(1, 2, 3, "rc")));
        assertFalse(version.equals(new SentinelVersion(1, 2, 4, "beta")));
        assertFalse(version.equals(null));
        assertFalse(version.equals("1.2.3"));
    }

    @Test
    public void equalsUsesFullVersionForComponents() {
        SentinelVersion first = new SentinelVersion(1, 0, 1000, null);
        SentinelVersion second = new SentinelVersion(1, 1, 0, null);

        assertTrue(first.equals(second));
        assertNotEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toStringIncludesComponentsAndPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");

        assertEquals("SentinelVersion{majorVersion=1, minorVersion=2, fixVersion=3, postfix='beta'}",
            version.toString());
    }
}
