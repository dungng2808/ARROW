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
    public void constructsFromDottedString() {
        DeweyVersion version = new DeweyVersion("2.4.1");

        assertEquals("2.4.1", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void addRunIncrementsLastVersion() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertEquals("2.5", version.addRun().toString());
        assertEquals("2.4", version.toString());
    }

    @Test
    public void addRunWithOffsetIncrementsSelectedVersion() {
        DeweyVersion version = new DeweyVersion("2.4.6");

        assertEquals("2.5.6", version.addRun(2).toString());
    }

    @Test
    public void addStageAppendsZeroVersion() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertEquals("2.4.0", version.addStage().toString());
        assertEquals(3, version.addStage().length());
    }

    @Test
    public void longerVersionIsCompatibleWhenItHasThisVersionAsPrefix() {
        DeweyVersion version = new DeweyVersion("2.4");
        DeweyVersion longer = new DeweyVersion("2.4.1");

        assertTrue(longer.isCompatible(version));
    }

    @Test
    public void equalLengthVersionsAreCompatibleWhenLastDigitIsGreaterOrEqual() {
        DeweyVersion version = new DeweyVersion("2.4.5");

        assertTrue(version.isCompatible(new DeweyVersion("2.4.3")));
        assertTrue(version.isCompatible(new DeweyVersion("2.4.5")));
    }

    @Test
    public void versionsWithDifferentPrefixAreIncompatible() {
        DeweyVersion version = new DeweyVersion("2.4.5");

        assertFalse(version.isCompatible(new DeweyVersion("2.3")));
    }

    @Test
    public void shorterVersionIsIncompatible() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertFalse(version.isCompatible(new DeweyVersion("2.4.1")));
    }
}
