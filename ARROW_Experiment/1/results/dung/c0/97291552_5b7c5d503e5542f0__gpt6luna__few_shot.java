package org.asn1s.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NRxUtils_ArrowTest
{
	@Test
	public void canonicalizesInteger()
	{
		assertEquals("+12.E0", NRxUtils.toCanonicalNR3("12"));
	}

	@Test
	public void canonicalizesDecimal()
	{
		assertEquals("+123.E-2", NRxUtils.toCanonicalNR3("1.23"));
	}

	@Test
	public void canonicalizesExponent()
	{
		assertEquals("+12.E3", NRxUtils.toCanonicalNR3("1.2E4"));
	}

	@Test
	public void removesTrailingFractionalZeros()
	{
		assertEquals("+12.E0", NRxUtils.toCanonicalNR3("12.00"));
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

	@Test
	public void rejectsInvalidFormat()
	{
		try
		{
			NRxUtils.toCanonicalNR3("01");
			fail("Expected IllegalArgumentException");
		}
		catch( IllegalArgumentException expected )
		{
			// Expected.
		}
	}
}
