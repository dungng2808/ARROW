package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SentinelVersion_ArrowTest {

    @Test
    public void defaultConstructorInitializesZeroVersion() {
        SentinelVersion version = new SentinelVersion();

        assertEquals(0, version.getMajorVersion());
        assertEquals(0, version.getMinorVersion());
        assertEquals(0, version.getFixVersion());
        assertEquals(0, version.getFullVersion());
        assertEquals(null, version.getPostfix());
    }

    @Test
    public void constructorsSetVersionAndPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");

        assertEquals(1, version.getMajorVersion());
        assertEquals(2, version.getMinorVersion());
        assertEquals(3, version.getFixVersion());
        assertEquals(1002003, version.getFullVersion());
        assertEquals("beta", version.getPostfix());

        SentinelVersion withoutPostfix = new SentinelVersion(2, 4, 6);
        assertEquals(null, withoutPostfix.getPostfix());
    }

    @Test
    public void settersUpdateValuesAndReturnSameInstance() {
        SentinelVersion version = new SentinelVersion();

        assertSame(version, version.setMajorVersion(3));
        assertSame(version, version.setMinorVersion(5));
        assertSame(version, version.setFixVersion(7));
        assertSame(version, version.setPostfix("rc1"));

        assertEquals(3, version.getMajorVersion());
        assertEquals(5, version.getMinorVersion());
        assertEquals(7, version.getFixVersion());
        assertEquals(3005007, version.getFullVersion());
        assertEquals("rc1", version.getPostfix());
    }

    @Test
    public void greaterThanComparesFullVersionAndHandlesNull() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);

        assertTrue(version.greaterThan(new SentinelVersion(1, 2, 2)));
        assertFalse(version.greaterThan(new SentinelVersion(1, 2, 3)));
        assertFalse(version.greaterThan(new SentinelVersion(1, 3, 0)));
        assertTrue(version.greaterThan(null));
    }

    @Test
    public void greaterOrEqualComparesFullVersionAndHandlesNull() {
        SentinelVersion version = new SentinelVersion(1, 2, 3);

        assertTrue(version.greaterOrEqual(new SentinelVersion(1, 2, 2)));
        assertTrue(version.greaterOrEqual(new SentinelVersion(1, 2, 3)));
        assertFalse(version.greaterOrEqual(new SentinelVersion(1, 3, 0)));
        assertTrue(version.greaterOrEqual(null));
    }

    @Test
    public void equalsAndHashCodeUseVersionAndPostfix() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion same = new SentinelVersion(1, 2, 3, "beta");
        SentinelVersion differentPostfix = new SentinelVersion(1, 2, 3, "rc");
        SentinelVersion differentVersion = new SentinelVersion(1, 2, 4, "beta");

        assertTrue(version.equals(version));
        assertTrue(version.equals(same));
        assertEquals(version.hashCode(), same.hashCode());
        assertFalse(version.equals(differentPostfix));
        assertFalse(version.equals(differentVersion));
        assertFalse(version.equals(null));
        assertFalse(version.equals("not a version"));
    }

    @Test
    public void equalsUsesFullVersionForVersionComponents() {
        SentinelVersion first = new SentinelVersion(1, 0, 1000);
        SentinelVersion second = new SentinelVersion(1, 1, 0);

        assertTrue(first.equals(second));
        assertNotEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toStringIncludesAllFields() {
        SentinelVersion version = new SentinelVersion(1, 2, 3, "beta");

        assertEquals("SentinelVersion{majorVersion=1, minorVersion=2, fixVersion=3, postfix='beta'}",
            version.toString());
    }
}
