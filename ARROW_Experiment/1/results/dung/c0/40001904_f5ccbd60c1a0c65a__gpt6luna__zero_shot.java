package de.tudarmstadt.ukp.wikipedia.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetaData_ArrowTest
{
    @Test
    public void metadataCanBeConstructedFromWikipedia() throws Exception
    {
        Wikipedia wiki = new Wikipedia();
        MetaData metadata = new MetaData(wiki);

        assertNotNull(metadata);
    }

    @Test
    public void languageIsObtainedFromWikipedia() throws Exception
    {
        Wikipedia wiki = new Wikipedia();
        MetaData metadata = new MetaData(wiki);

        assertEquals(wiki.getLanguage(), metadata.getLanguage());
    }
}
