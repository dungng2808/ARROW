package com.github.fhuss.kafka.streams.cep.core.nfa;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeweyVersion_ArrowTest {

    @Test
    public void constructsFromRunAndString() {
        DeweyVersion single = new DeweyVersion(3);
        DeweyVersion multiple = new DeweyVersion("2.4.6");

        assertEquals(1, single.length());
        assertEquals("3", single.toString());
        assertEquals(3, multiple.length());
        assertEquals("2.4.6", multiple.toString());
    }

    @Test
    public void addsRunToLastVersionByDefault() {
        DeweyVersion result = new DeweyVersion("2.4.6").addRun();

        assertEquals("2.4.7", result.toString());
    }

    @Test
    public void addsRunAtSpecifiedOffset() {
        DeweyVersion result = new DeweyVersion("2.4.6").addRun(2);

        assertEquals("2.5.6", result.toString());
    }

    @Test
    public void addsStageWithZeroVersion() {
        DeweyVersion result = new DeweyVersion("2.4").addStage();

        assertEquals(3, result.length());
        assertEquals("2.4.0", result.toString());
    }

    @Test
    public void checksCompatibilityForLongerPrefix() {
        assertTrue(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2")));
        assertFalse(new DeweyVersion("1.3.3").isCompatible(new DeweyVersion("1.2")));
    }

    @Test
    public void checksCompatibilityForEqualLengthVersions() {
        assertTrue(new DeweyVersion("1.2.5").isCompatible(new DeweyVersion("1.2.4")));
        assertFalse(new DeweyVersion("1.2.3").isCompatible(new DeweyVersion("1.2.4")));
        assertFalse(new DeweyVersion("1.3.5").isCompatible(new DeweyVersion("1.2.4")));
    }

    @Test
    public void shorterVersionIsNotCompatible() {
        assertFalse(new DeweyVersion("1.2").isCompatible(new DeweyVersion("1.2.3")));
    }

    @Test
    public void defaultConstructorCreatesEmptyVersion() {
        DeweyVersion version = new DeweyVersion();

        assertEquals(0, version.length());
        assertEquals("", version.toString());
    }
}
