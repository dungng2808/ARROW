package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void defaultConstructorCreatesEmptyVersion() {
        DeweyVersion version = new DeweyVersion();
        assertEquals(0, version.length());
        assertEquals("", version.toString());
    }

    @Test
    public void integerConstructorCreatesSingleRun() {
        DeweyVersion version = new DeweyVersion(3);
        assertEquals(1, version.length());
        assertEquals("3", version.toString());
    }

    @Test
    public void stringConstructorParsesRuns() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        assertEquals(3, version.length());
        assertEquals("1.2.3", version.toString());
    }

    @Test
    public void addRunIncrementsLastRun() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        assertEquals("1.2.4", version.addRun().toString());
        assertEquals("1.2.3", version.toString());
    }

    @Test
    public void addRunWithOffsetIncrementsSelectedRun() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        assertEquals("1.3.3", version.addRun(2).toString());
    }

    @Test
    public void addStageAppendsZeroRun() {
        DeweyVersion version = new DeweyVersion("1.2");
        DeweyVersion staged = version.addStage();
        assertEquals("1.2.0", staged.toString());
        assertEquals(3, staged.length());
        assertEquals("1.2", version.toString());
    }

    @Test
    public void compatibleWhenLongerVersionHasMatchingPrefix() {
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2")));
    }

    @Test
    public void incompatibleWhenLongerVersionPrefixDiffers() {
        assertFalse(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.3")));
    }

    @Test
    public void compatibleWhenEqualLengthPrefixesMatchAndLastRunIsGreater() {
        assertTrue(new DeweyVersion("1.2.4").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void compatibleWhenEqualLengthVersionsAreEqual() {
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenEqualLengthPrefixDiffers() {
        assertFalse(new DeweyVersion("1.3.4").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenEqualLengthLastRunIsSmaller() {
        assertFalse(new DeweyVersion("1.2.2").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenThisVersionIsShorter() {
        assertFalse(new DeweyVersion("1.2").isCompatible(new DeweyVersion("1.2.3")));
    }
}
