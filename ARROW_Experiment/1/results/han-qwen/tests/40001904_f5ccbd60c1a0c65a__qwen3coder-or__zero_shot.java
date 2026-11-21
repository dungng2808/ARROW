package de.tudarmstadt.ukp.wikipedia.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.wikipedia.api.hibernate.MetaData;

public class MetaData_ArrowTest {

    private Wikipedia wiki;
    private MetaData metaData;
    private Session session;
    private de.tudarmstadt.ukp.wikipedia.api.hibernate.MetaData hibernateMetaData;

    @Before
    public void setUp() {
        wiki = mock(Wikipedia.class);
        session = mock(Session.class);
        hibernateMetaData = mock(de.tudarmstadt.ukp.wikipedia.api.hibernate.MetaData.class);
        
        when(wiki.__getHibernateSession()).thenReturn(session);
        when(session.createQuery("from MetaData").uniqueResult()).thenReturn(hibernateMetaData);
        
        metaData = new MetaData(wiki);
    }

    @Test
    public void testGetId() {
        long expectedId = 123L;
        when(hibernateMetaData.getId()).thenReturn(expectedId);
        
        long actualId = metaData.getId();
        
        assertEquals(expectedId, actualId);
    }

    @Test
    public void testGetNumberOfCategories() {
        long expectedCategories = 456L;
        when(hibernateMetaData.getNrofCategories()).thenReturn(expectedCategories);
        
        long actualCategories = metaData.getNumberOfCategories();
        
        assertEquals(expectedCategories, actualCategories);
    }

    @Test
    public void testGetNumberOfPages() {
        long expectedPages = 789L;
        when(hibernateMetaData.getNrofPages()).thenReturn(expectedPages);
        
        long actualPages = metaData.getNumberOfPages();
        
        assertEquals(expectedPages, actualPages);
    }

    @Test
    public void testGetNumberOfDisambiguationPages() {
        long expectedDisambPages = 101L;
        when(hibernateMetaData.getNrofDisambiguationPages()).thenReturn(expectedDisambPages);
        
        long actualDisambPages = metaData.getNumberOfDisambiguationPages();
        
        assertEquals(expectedDisambPages, actualDisambPages);
    }

    @Test
    public void testGetNumberOfRedirectPages() {
        long expectedRedirects = 202L;
        when(hibernateMetaData.getNrofRedirects()).thenReturn(expectedRedirects);
        
        long actualRedirects = metaData.getNumberOfRedirectPages();
        
        assertEquals(expectedRedirects, actualRedirects);
    }

    @Test
    public void testGetDisambiguationCategory() throws WikiApiException {
        String expectedTitle = "Disambiguation";
        when(hibernateMetaData.getDisambiguationCategory()).thenReturn(expectedTitle);
        Category mockCategory = mock(Category.class);
        when(wiki.getCategory(expectedTitle)).thenReturn(mockCategory);
        
        Category actualCategory = metaData.getDisambiguationCategory();
        
        assertNotNull(actualCategory);
    }

    @Test
    public void testGetMainCategory() throws WikiApiException {
        String expectedTitle = "Main_Category";
        when(hibernateMetaData.getMainCategory()).thenReturn(expectedTitle);
        Category mockCategory = mock(Category.class);
        when(wiki.getCategory(expectedTitle)).thenReturn(mockCategory);
        
        Category actualCategory = metaData.getMainCategory();
        
        assertNotNull(actualCategory);
    }

    @Test
    public void testGetVersion() throws WikiApiException {
        String expectedVersion = "1.0";
        when(hibernateMetaData.getVersion()).thenReturn(expectedVersion);
        
        String actualVersion = metaData.getVersion();
        
        assertEquals(expectedVersion, actualVersion);
    }

    @Test
    public void testGetLanguage() {
        Language expectedLanguage = Language.ENGLISH;
        when(wiki.getLanguage()).thenReturn(expectedLanguage);
        
        Language actualLanguage = metaData.getLanguage();
        
        assertEquals(expectedLanguage, actualLanguage);
    }
}
