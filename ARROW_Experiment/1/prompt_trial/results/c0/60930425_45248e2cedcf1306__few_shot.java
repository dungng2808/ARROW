package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void constructsFromRun() {
        DeweyVersion version = new DeweyVersion(3);
        assertEquals("3", version.toString());
        assertEquals(1, version.length());
    }

    @Test
    public void constructsFromString() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        assertEquals("1.2.3", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void addsRunToLastComponent() {
        DeweyVersion version = new DeweyVersion("1.2").addRun();
        assertEquals("1.3", version.toString());
    }

    @Test
    public void addsRunAtSpecifiedOffset() {
        DeweyVersion version = new DeweyVersion("1.2.3").addRun(2);
        assertEquals("1.3.3", version.toString());
    }

    @Test
    public void addsStage() {
        DeweyVersion version = new DeweyVersion("1.2").addStage();
        assertEquals("1.2.0", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void compatibleWhenLongerVersionHasMatchingPrefix() {
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2")));
    }

    @Test
    public void incompatibleWhenLongerVersionPrefixDiffers() {
        assertFalse(new DeweyVersion("1.3.3").isCompatible(new DeweyVersion("1.2")));
    }

    @Test
    public void compatibleWhenEqualLengthAndLastComponentIsGreaterOrEqual() {
        assertTrue(new DeweyVersion("1.2.4").isCompatible(new DeweyVersion("1.2.3")));
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenEqualLengthAndLastComponentIsSmaller() {
        assertFalse(new DeweyVersion("1.2.2").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenThisVersionIsShorter() {
        assertFalse(new DeweyVersion("1.2").isCompatible(new DeweyVersion("1.2.3")));
    }
}
