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
    public void constructsFromString() {
        DeweyVersion version = new DeweyVersion("1.2.3");

        assertEquals("1.2.3", version.toString());
        assertEquals(3, version.length());
    }

    @Test
    public void addsDefaultRun() {
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
    public void addsStage() {
        DeweyVersion version = new DeweyVersion("1.2");

        assertEquals("1.2.0", version.addStage().toString());
        assertEquals(3, version.addStage().length());
    }

    @Test
    public void checksCompatibilityWhenThisIsLongerPrefix() {
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2")));
        assertFalse(new DeweyVersion("1.3.3").isCompatible(new DeweyVersion("1.2")));
    }

    @Test
    public void checksCompatibilityForEqualLengths() {
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2.2")));
        assertFalse(new DeweyVersion("1.2.1").isCompatible(new DeweyVersion("1.2.2")));
        assertFalse(new DeweyVersion("1.3.3").isCompatible(new DeweyVersion("1.2.2")));
    }

    @Test
    public void checksCompatibilityWhenThisIsShorter() {
        assertFalse(new DeweyVersion("1.2").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void emptyVersionHasEmptyStringAndZeroLength() {
        DeweyVersion version = new DeweyVersion();

        assertEquals("", version.toString());
        assertEquals(0, version.length());
    }
}
