package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void constructsFromRun() {
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
    public void addsDefaultRunToLastComponent() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertEquals("2.5", version.addRun().toString());
        assertEquals("2.4", version.toString());
    }

    @Test
    public void addsRunAtSpecifiedOffset() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertEquals("2.5.7", version.addRun(2).toString());
    }

    @Test
    public void addsStageAsZeroComponent() {
        DeweyVersion version = new DeweyVersion("2.4");

        DeweyVersion staged = version.addStage();

        assertEquals(3, staged.length());
        assertEquals("2.4.0", staged.toString());
        assertEquals("2.4", version.toString());
    }

    @Test
    public void compatibleWhenLongerVersionHasMatchingPrefix() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertTrue(version.isCompatible(new DeweyVersion("2.4")));
    }

    @Test
    public void incompatibleWhenLongerVersionPrefixDiffers() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertFalse(version.isCompatible(new DeweyVersion("2.5")));
    }

    @Test
    public void compatibleAtEqualLengthWhenPrefixMatchesAndLastIsGreater() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertTrue(version.isCompatible(new DeweyVersion("2.4.6")));
    }

    @Test
    public void compatibleAtEqualLengthWhenVersionsAreEqual() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertTrue(version.isCompatible(new DeweyVersion("2.4.7")));
    }

    @Test
    public void incompatibleAtEqualLengthWhenLastIsSmaller() {
        DeweyVersion version = new DeweyVersion("2.4.6");

        assertFalse(version.isCompatible(new DeweyVersion("2.4.7")));
    }

    @Test
    public void incompatibleWhenThisVersionIsShorter() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertFalse(version.isCompatible(new DeweyVersion("2.4.7")));
    }
}
