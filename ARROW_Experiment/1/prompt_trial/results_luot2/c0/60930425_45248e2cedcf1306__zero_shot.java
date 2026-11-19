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
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals(3, version.length());
        assertEquals("1.2.3", version.toString());
    }

    @Test
    public void addsOneToLastRunByDefault() {
        DeweyVersion version = new DeweyVersion("1.2");

        assertEquals("1.3", version.addRun().toString());
        assertEquals("1.2", version.toString());
    }

    @Test
    public void addsRunAtSpecifiedOffset() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals("1.3.3", version.addRun(2).toString());
    }

    @Test
    public void addsStageAsTrailingZero() {
        DeweyVersion version = new DeweyVersion("1.2");

        DeweyVersion staged = version.addStage();

        assertEquals(3, staged.length());
        assertEquals("1.2.0", staged.toString());
        assertEquals("1.2", version.toString());
    }

    @Test
    public void compatibleWhenLongerVersionHasMatchingPrefix() {
        DeweyVersion version = new DeweyVersion("1.2.3");
        DeweyVersion shorter = new DeweyVersion("1.2");

        assertTrue(version.isCompatible(shorter));
    }

    @Test
    public void compatibleWhenEqualLengthAndLastRunIsGreater() {
        DeweyVersion version = new DeweyVersion("1.2.4");
        DeweyVersion other = new DeweyVersion("1.2.3");

        assertTrue(version.isCompatible(other));
    }

    @Test
    public void compatibleWhenEqualLengthAndVersionsAreEqual() {
        DeweyVersion version = new DeweyVersion("1.2");
        DeweyVersion other = new DeweyVersion("1.2");

        assertTrue(version.isCompatible(other));
    }

    @Test
    public void incompatibleWhenLongerVersionPrefixDiffers() {
        DeweyVersion version = new DeweyVersion("1.3.3");
        DeweyVersion shorter = new DeweyVersion("1.2");

        assertFalse(version.isCompatible(shorter));
    }

    @Test
    public void incompatibleWhenEqualLengthPrefixDiffers() {
        DeweyVersion version = new DeweyVersion("1.3.4");
        DeweyVersion other = new DeweyVersion("1.2.3");

        assertFalse(version.isCompatible(other));
    }

    @Test
    public void incompatibleWhenEqualLengthLastRunIsSmaller() {
        DeweyVersion version = new DeweyVersion("1.2.2");
        DeweyVersion other = new DeweyVersion("1.2.3");

        assertFalse(version.isCompatible(other));
    }

    @Test
    public void incompatibleWhenVersionIsShorter() {
        DeweyVersion version = new DeweyVersion("1.2");
        DeweyVersion other = new DeweyVersion("1.2.3");

        assertFalse(version.isCompatible(other));
    }
}
