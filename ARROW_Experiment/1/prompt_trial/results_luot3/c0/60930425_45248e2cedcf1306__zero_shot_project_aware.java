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
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertEquals("2.4.7", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void addsRunByDefault() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertEquals("2.5", version.addRun().toString());
    }

    @Test
    public void addsRunAtSpecifiedOffset() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertEquals("2.5.7", version.addRun(2).toString());
    }

    @Test
    public void addsStageWithZeroVersion() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertEquals("2.4.0", version.addStage().toString());
        assertEquals(3, version.addStage().length());
    }

    @Test
    public void checksCompatibilityWhenThisVersionIsLongerPrefix() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertTrue(version.isCompatible(new DeweyVersion("2.4")));
        assertFalse(version.isCompatible(new DeweyVersion("2.5")));
    }

    @Test
    public void checksCompatibilityForEqualLengthVersions() {
        DeweyVersion version = new DeweyVersion("2.4.7");

        assertTrue(version.isCompatible(new DeweyVersion("2.4.6")));
        assertTrue(version.isCompatible(new DeweyVersion("2.4.7")));
        assertFalse(version.isCompatible(new DeweyVersion("2.4.8")));
        assertFalse(version.isCompatible(new DeweyVersion("2.3.9")));
    }

    @Test
    public void rejectsCompatibilityWhenThisVersionIsShorter() {
        DeweyVersion version = new DeweyVersion("2.4");

        assertFalse(version.isCompatible(new DeweyVersion("2.4.7")));
    }

    @Test
    public void emptyVersionHasEmptyStringAndZeroLength() {
        DeweyVersion version = new DeweyVersion();

        assertEquals("", version.toString());
        assertEquals(0, version.length());
    }
}
