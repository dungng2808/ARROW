package org.asn1s.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NRxUtils_ArrowTest
{
	@Test
	public void canonicalizesInteger()
	{
		assertEquals("+123.E0", NRxUtils.toCanonicalNR3("123"));
	}

	@Test
	public void canonicalizesNegativeInteger()
	{
		assertEquals("-12.E0", NRxUtils.toCanonicalNR3("-12"));
	}

	@Test
	public void canonicalizesFraction()
	{
		assertEquals("+1234.E-2", NRxUtils.toCanonicalNR3("12.34"));
	}

	@Test
	public void removesTrailingFractionalZeros()
	{
		assertEquals("+12.E0", NRxUtils.toCanonicalNR3("12.00"));
	}

	@Test
	public void canonicalizesExponent()
	{
		assertEquals("+123.E2", NRxUtils.toCanonicalNR3("1.23E4"));
	}

	@Test
	public void canonicalizesZero()
	{
		assertEquals("+0.E+0", NRxUtils.toCanonicalNR3("0"));
	}

	@Test
	public void preservesSpecialValues()
	{
		assertEquals("Infinity", NRxUtils.toCanonicalNR3("Infinity"));
		assertEquals("-Infinity", NRxUtils.toCanonicalNR3("-Infinity"));
		assertEquals("NaN", NRxUtils.toCanonicalNR3("NaN"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsInvalidFormat()
	{
		NRxUtils.toCanonicalNR3("01");
	}

	@Test(expected = NullPointerException.class)
	public void rejectsNull()
	{
		NRxUtils.toCanonicalNR3(null);
	}
}
