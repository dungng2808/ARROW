package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void constructsFromRunAndFormatsVersion() {
        DeweyVersion version = new DeweyVersion(3);
        assertEquals("3", version.toString());
        assertEquals(1, version.length());
    }

    @Test
    public void constructsFromDottedString() {
        DeweyVersion version = new DeweyVersion("2.4.7");
        assertEquals("2.4.7", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void addRunIncrementsLastComponent() {
        DeweyVersion version = new DeweyVersion("2.4");
        assertEquals("2.5", version.addRun().toString());
        assertEquals("2.4", version.toString());
    }

    @Test
    public void addRunWithOffsetIncrementsSelectedComponent() {
        DeweyVersion version = new DeweyVersion("2.4.7");
        assertEquals("2.5.7", version.addRun(2).toString());
    }

    @Test
    public void addStageAppendsZeroComponent() {
        DeweyVersion version = new DeweyVersion("2.4");
        assertEquals("2.4.0", version.addStage().toString());
        assertEquals(3, version.addStage().length());
    }

    @Test
    public void compatibleWhenThisVersionIsMatchingPrefix() {
        assertTrue(new DeweyVersion("2.4.1").isCompatible(new DeweyVersion("2.4")));
    }

    @Test
    public void incompatibleWhenPrefixComponentsDiffer() {
        assertFalse(new DeweyVersion("2.5.1").isCompatible(new DeweyVersion("2.4")));
    }

    @Test
    public void compatibleAtEqualLengthWhenInitialComponentsMatchAndLastIsGreater() {
        assertTrue(new DeweyVersion("2.4.5").isCompatible(new DeweyVersion("2.4.3")));
    }

    @Test
    public void incompatibleAtEqualLengthWhenInitialComponentsDiffer() {
        assertFalse(new DeweyVersion("2.5.5").isCompatible(new DeweyVersion("2.4.3")));
    }

    @Test
    public void incompatibleAtEqualLengthWhenLastComponentIsSmaller() {
        assertFalse(new DeweyVersion("2.4.2").isCompatible(new DeweyVersion("2.4.3")));
    }

    @Test
    public void incompatibleWhenThisVersionIsShorter() {
        assertFalse(new DeweyVersion("2.4").isCompatible(new DeweyVersion("2.4.1")));
    }
}
