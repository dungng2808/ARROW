package de.tudarmstadt.ukp.wikipedia.api;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class MetaData_ArrowTest
{
	@Test
	public void metadataRequiresWikipediaInstance()
	{
		try {
			new MetaData(null);
			throw new AssertionError("Expected a null dereference");
		}
		catch (NullPointerException expected) {
			assertNotNull(expected);
		}
	}
}
