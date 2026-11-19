package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void defaultConstructorHasEmptyVersion() {
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
    public void stringConstructorParsesVersion() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals(3, version.length());
        assertEquals("1.2.3", version.toString());
    }

    @Test
    public void addRunIncrementsLastRun() {
        DeweyVersion version = new DeweyVersion("1.2");

        assertEquals("1.3", version.addRun().toString());
        assertEquals("1.2", version.toString());
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

        assertEquals(3, staged.length());
        assertEquals("1.2.0", staged.toString());
        assertEquals("1.2", version.toString());
    }

    @Test
    public void longerVersionIsCompatibleWhenItStartsWithThisVersion() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        DeweyVersion longer = new DeweyVersion("1.2.3.4");

        assertTrue(version.isCompatible(longer));
    }

    @Test
    public void longerVersionIsIncompatibleWhenPrefixDiffers() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        DeweyVersion longer = new DeweyVersion("1.4.3.4");

        assertFalse(version.isCompatible(longer));
    }

    @Test
    public void equalLengthVersionsCompareLastRun() {
        DeweyVersion greater = new DeweyVersion("1.2.4");
        DeweyVersion lesser = new DeweyVersion("1.2.3");

        assertTrue(greater.isCompatible(lesser));
        assertFalse(lesser.isCompatible(greater));
    }

    @Test
    public void shorterVersionIsIncompatible() {
        DeweyVersion longer = new DeweyVersion("1.2.3");
        DeweyVersion shorter = new DeweyVersion("1.2");

        assertFalse(longer.isCompatible(shorter));
    }
}
