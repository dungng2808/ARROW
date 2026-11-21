package edu.emory.mathcs.nlp.component.tokenizer.dictionary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class Abbreviation_ArrowTest
{
	@Test
	public void recognizesEntriesFromInputStream()
	{
		String entries = "Dr.\nMr.\n";
		Abbreviation abbreviation = new Abbreviation(
				new ByteArrayInputStream(entries.getBytes(StandardCharsets.UTF_8)));

		assertTrue(abbreviation.isAbbreviationEndingWithPeriod("dr."));
		assertTrue(abbreviation.isAbbreviationEndingWithPeriod("mr."));
		assertFalse(abbreviation.isAbbreviationEndingWithPeriod("ms."));
	}

	@Test
	public void initReplacesExistingEntries()
	{
		Abbreviation abbreviation = new Abbreviation(
				new ByteArrayInputStream("Dr.\n".getBytes(StandardCharsets.UTF_8)));

		abbreviation.init(new ByteArrayInputStream("Prof.\n".getBytes(StandardCharsets.UTF_8)));

		assertTrue(abbreviation.isAbbreviationEndingWithPeriod("prof."));
		assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
	}

	@Test
	public void emptyInputContainsNoEntries()
	{
		Abbreviation abbreviation = new Abbreviation(new ByteArrayInputStream(new byte[0]));

		assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
	}
}
