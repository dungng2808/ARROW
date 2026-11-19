package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void constructsFromRunAndFormatsVersion() {
        DeweyVersion version = new DeweyVersion(3);

        assertEquals(1, version.length());
        assertEquals("3", version.toString());
    }

    @Test
    public void constructsFromDottedString() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertEquals(3, version.length());
        assertEquals("2.4.7", version.toString());
    }

    @Test
    public void addRunIncrementsLastRunWithoutChangingOriginal() {
        DeweyVersion original = new DeweyVersion("2.4");
        DeweyVersion incremented = original.addRun();

        assertEquals("2.5", incremented.toString());
        assertEquals("2.4", original.toString());
    }

    @Test
    public void addRunWithOffsetIncrementsEarlierRun() {
        DeweyVersion original = new DeweyVersion("2.4.6");
        DeweyVersion incremented = original.addRun(2);

        assertEquals("2.5.6", incremented.toString());
        assertEquals("2.4.6", original.toString());
    }

    @Test
    public void addStageAppendsZeroRun() {
        DeweyVersion original = new DeweyVersion("2.4");
        DeweyVersion staged = original.addStage();

        assertEquals(3, staged.length());
        assertEquals("2.4.0", staged.toString());
        assertEquals("2.4", original.toString());
    }

    @Test
    public void isCompatibleWhenThisIsMatchingPrefixOfLongerVersion() {
        DeweyVersion version = new DeweyVersion("2.4");
        DeweyVersion longer = new DeweyVersion("2.4.1");

        assertTrue(version.isCompatible(longer));
    }

    @Test
    public void isCompatibleWhenLongerVersionStartsWithThisVersion() {
        DeweyVersion longer = new DeweyVersion("2.4.1");
        DeweyVersion version = new DeweyVersion("2.4");

        assertTrue(longer.isCompatible(version));
    }

    @Test
    public void isCompatibleForEqualLengthVersionsWhenLastRunIsGreaterOrEqual() {
        DeweyVersion greater = new DeweyVersion("2.5");
        DeweyVersion lesser = new DeweyVersion("2.4");

        assertTrue(greater.isCompatible(lesser));
        assertFalse(lesser.isCompatible(greater));
    }

    @Test
    public void isIncompatibleWhenPrefixRunsDiffer() {
        DeweyVersion version = new DeweyVersion("2.4.1");
        DeweyVersion other = new DeweyVersion("3.4");

        assertFalse(version.isCompatible(other));
    }

    @Test
    public void isIncompatibleWhenThisVersionIsShorter() {
        DeweyVersion shorter = new DeweyVersion("2.4");
        DeweyVersion longer = new DeweyVersion("2.4.1");

        assertFalse(shorter.isCompatible(longer));
    }
}
