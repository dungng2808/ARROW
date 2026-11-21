package edu.emory.mathcs.nlp.component.tokenizer.dictionary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class Abbreviation_ArrowTest
{
	@Test
	public void recognizesLoadedAbbreviation()
	{
		InputStream input = new ByteArrayInputStream("Dr.\nMr.\n".getBytes(StandardCharsets.UTF_8));
		Abbreviation abbreviation = new Abbreviation(input);

		assertTrue(abbreviation.isAbbreviationEndingWithPeriod("dr."));
		assertTrue(abbreviation.isAbbreviationEndingWithPeriod("mr."));
	}

	@Test
	public void doesNotRecognizeAbsentAbbreviation()
	{
		InputStream input = new ByteArrayInputStream("Dr.\nMr.\n".getBytes(StandardCharsets.UTF_8));
		Abbreviation abbreviation = new Abbreviation(input);

		assertFalse(abbreviation.isAbbreviationEndingWithPeriod("prof."));
	}

	@Test
	public void initReplacesPreviouslyLoadedEntries()
	{
		Abbreviation abbreviation = new Abbreviation(
				new ByteArrayInputStream("Dr.\n".getBytes(StandardCharsets.UTF_8)));
		abbreviation.init(new ByteArrayInputStream("Prof.\n".getBytes(StandardCharsets.UTF_8)));

		assertTrue(abbreviation.isAbbreviationEndingWithPeriod("prof."));
		assertFalse(abbreviation.isAbbreviationEndingWithPeriod("dr."));
	}
}
