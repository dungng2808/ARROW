package org.asn1s.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NRxUtils_ArrowTest
{
	@Test
	public void canonicalizesInteger()
	{
		assertEquals("+12.E0", NRxUtils.toCanonicalNR3("12"));
	}

	@Test
	public void canonicalizesSignedAndZeroValues()
	{
		assertEquals("-12.E0", NRxUtils.toCanonicalNR3("-12"));
		assertEquals("+0.E0", NRxUtils.toCanonicalNR3("0"));
	}

	@Test
	public void canonicalizesFractionByRemovingTrailingZeros()
	{
		assertEquals("+123.E-2", NRxUtils.toCanonicalNR3("1.2300"));
	}

	@Test
	public void canonicalizesFractionWithExponent()
	{
		assertEquals("+123.E-1", NRxUtils.toCanonicalNR3("1.23E+1"));
	}

	@Test
	public void canonicalizesExponentWithoutFraction()
	{
		assertEquals("+12.E3", NRxUtils.toCanonicalNR3("12e3"));
	}

	@Test
	public void canonicalizesZeroExponent()
	{
		assertEquals("+1.E+0", NRxUtils.toCanonicalNR3("1E1"));
	}

	@Test
	public void preservesSpecialValues()
	{
		assertEquals("Infinity", NRxUtils.toCanonicalNR3("Infinity"));
		assertEquals("-Infinity", NRxUtils.toCanonicalNR3("-Infinity"));
		assertEquals("NaN", NRxUtils.toCanonicalNR3("NaN"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsLeadingZeroInteger()
	{
		NRxUtils.toCanonicalNR3("01");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingIntegerPart()
	{
		NRxUtils.toCanonicalNR3(".5");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNull()
	{
		NRxUtils.toCanonicalNR3(null);
	}
}
