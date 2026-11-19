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
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals("1.2.3", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void addsRunByDefault() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals("1.2.4", version.addRun().toString());
    }

    @Test
    public void addsRunAtSpecifiedOffset() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals("1.3.3", version.addRun(2).toString());
    }

    @Test
    public void addsStage() {
        DeweyVersion version = new DeweyVersion("1.2");

        assertEquals("1.2.0", version.addStage().toString());
        assertEquals(3, version.addStage().length());
    }

    @Test
    public void compatibleWhenThisIsMatchingPrefix() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertTrue(version.isCompatible(new DeweyVersion("1.2")));
    }

    @Test
    public void incompatibleWhenPrefixDiffers() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertFalse(version.isCompatible(new DeweyVersion("1.4")));
    }

    @Test
    public void compatibleWhenEqualLengthAndLastDigitIsGreater() {
        DeweyVersion version = new DeweyVersion("1.2.4");

        assertTrue(version.isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenEqualLengthAndLastDigitIsSmaller() {
        DeweyVersion version = new DeweyVersion("1.2.2");

        assertFalse(version.isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void incompatibleWhenOtherVersionIsLonger() {
        DeweyVersion version = new DeweyVersion("1.2");

        assertFalse(version.isCompatible(new DeweyVersion("1.2.0")));
    }

    @Test
    public void emptyVersionHasEmptyStringAndZeroLength() {
        DeweyVersion version = new DeweyVersion();

        assertEquals("", version.toString());
        assertEquals(0, version.length());
    }
}
