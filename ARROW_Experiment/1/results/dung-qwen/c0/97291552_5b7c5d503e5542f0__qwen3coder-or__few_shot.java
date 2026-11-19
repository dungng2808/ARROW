package org.asn1s.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NRxUtils_ArrowTest {

    @Test
    public void testToCanonicalNR3InfinityValues() {
        assertEquals("Infinity", NRxUtils.toCanonicalNR3("Infinity"));
        assertEquals("-Infinity", NRxUtils.toCanonicalNR3("-Infinity"));
        assertEquals("NaN", NRxUtils.toCanonicalNR3("NaN"));
    }

    @Test
    public void testToCanonicalNR3ValidNumbers() {
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.000"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23E2"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23E+2"));
        assertEquals("+123.E-2", NRxUtils.toCanonicalNR3("1.23E-2"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123E0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123E+0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123E-0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.0E0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.000E0"));
    }

    @Test
    public void testToCanonicalNR3WithLeadingZeros() {
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("0123"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("00123"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("00000123"));
    }

    @Test
    public void testToCanonicalNR3WithDecimalPoint() {
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123."));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.000"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123.000000"));
    }

    @Test
    public void testToCanonicalNR3WithNegativeNumbers() {
        assertEquals("-123.E+0", NRxUtils.toCanonicalNR3("-123"));
        assertEquals("-123.E+0", NRxUtils.toCanonicalNR3("-123.0"));
        assertEquals("-123.E+2", NRxUtils.toCanonicalNR3("-1.23E2"));
        assertEquals("-123.E-2", NRxUtils.toCanonicalNR3("-1.23E-2"));
    }

    @Test
    public void testToCanonicalNR3WithScientificNotation() {
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23E2"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23E+2"));
        assertEquals("+123.E-2", NRxUtils.toCanonicalNR3("1.23E-2"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123E0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123E+0"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123E-0"));
    }

    @Test
    public void testToCanonicalNR3WithZero() {
        assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0"));
        assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0.0"));
        assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0.000"));
        assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0E0"));
        assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0E+0"));
        assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0E-0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat() {
        NRxUtils.toCanonicalNR3("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat2() {
        NRxUtils.toCanonicalNR3("123.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat3() {
        NRxUtils.toCanonicalNR3(".123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat4() {
        NRxUtils.toCanonicalNR3("123..456");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat5() {
        NRxUtils.toCanonicalNR3("123E");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat6() {
        NRxUtils.toCanonicalNR3("123E+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat7() {
        NRxUtils.toCanonicalNR3("123E-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat8() {
        NRxUtils.toCanonicalNR3("123E++2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToCanonicalNR3InvalidFormat9() {
        NRxUtils.toCanonicalNR3("123E--2");
    }

    @Test
    public void testToCanonicalNR3CaseInsensitive() {
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123"));
        assertEquals("+123.E+0", NRxUtils.toCanonicalNR3("123"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23E2"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23e2"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23E2"));
        assertEquals("+123.E+2", NRxUtils.toCanonicalNR3("1.23e2"));
    }
}
