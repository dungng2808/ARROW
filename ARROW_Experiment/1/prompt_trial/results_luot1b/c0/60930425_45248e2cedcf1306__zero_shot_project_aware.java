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
    public void addsOneToLastRunByDefault() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertEquals("2.5", version.addRun().toString());
        assertEquals("2.4", version.toString());
    }

    @Test
    public void addsOneAtSpecifiedOffset() {
        DeweyVersion version = new DeweyVersion("2.4.6");

        assertEquals("2.5.6", version.addRun(2).toString());
    }

    @Test
    public void addsStageWithZeroRun() {
        DeweyVersion version = new DeweyVersion("2.4");

        DeweyVersion nextStage = version.addStage();

        assertEquals(3, nextStage.length());
        assertEquals("2.4.0", nextStage.toString());
        assertEquals("2.4", version.toString());
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
    public void compatibleAtEqualLengthWhenLastRunIsGreaterOrEqual() {
        assertTrue(new DeweyVersion("1.2.4").isCompatible(new DeweyVersion("1.2.3")));
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleAtEqualLengthWhenPrefixDiffersOrLastRunIsSmaller() {
        assertFalse(new DeweyVersion("1.3.4").isCompatible(new DeweyVersion("1.2.3")));
        assertFalse(new DeweyVersion("1.2.2").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenThisVersionIsShorter() {
        assertFalse(new DeweyVersion("1.2").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void defaultVersionHasEmptyRepresentation() {
        DeweyVersion version = new DeweyVersion();

        assertEquals(0, version.length());
        assertEquals("", version.toString());
    }
}
