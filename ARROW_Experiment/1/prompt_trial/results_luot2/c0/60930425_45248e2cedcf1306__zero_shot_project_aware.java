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
    public void addsDefaultRun() {
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
    public void reportsLengthAfterAddingStage() {
        DeweyVersion version = new DeweyVersion("2.4");

        DeweyVersion staged = version.addStage();

        assertEquals(3, staged.length());
        assertEquals("2.4.0", staged.toString());
        assertEquals(2, version.length());
    }

    @Test
    public void checksCompatibilityForLongerMatchingPrefix() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertTrue(version.isCompatible(new DeweyVersion("2.4")));
    }

    @Test
    public void rejectsCompatibilityForLongerMismatchedPrefix() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertFalse(version.isCompatible(new DeweyVersion("2.5")));
    }

    @Test
    public void checksCompatibilityForEqualLengths() {
        assertTrue(new DeweyVersion("2.4.7").isCompatible(new DeweyVersion("2.4.6")));
        assertFalse(new DeweyVersion("2.4.5").isCompatible(new DeweyVersion("2.4.6")));
        assertFalse(new DeweyVersion("2.4.7").isCompatible(new DeweyVersion("2.5.6")));
    }

    @Test
    public void rejectsCompatibilityWhenThisVersionIsShorter() {
        assertFalse(new DeweyVersion("2.4").isCompatible(new DeweyVersion("2.4.7")));
    }

    @Test
    public void emptyVersionHasEmptyStringAndZeroLength() {
        DeweyVersion version = new DeweyVersion();

        assertEquals(0, version.length());
        assertEquals("", version.toString());
    }
}
