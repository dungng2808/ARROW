package de.tudarmstadt.ukp.wikipedia.api;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class MetaData_ArrowTest {

    @Mock
    private Wikipedia wiki;

    @Mock
    private Session session;

    @Mock
    private de.tudarmstadt.ukp.wikipedia.api.hibernate.MetaData hibernateMetaData;

    private MetaData metaData;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(wiki.__getHibernateSession()).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(mock(org.hibernate.Query.class));
        when(session.uniqueResult()).thenReturn(hibernateMetaData);
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
        long expectedCount = 456L;
        when(hibernateMetaData.getNrofCategories()).thenReturn(expectedCount);
        
        long actualCount = metaData.getNumberOfCategories();
        
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testGetNumberOfPages() {
        long expectedCount = 789L;
        when(hibernateMetaData.getNrofPages()).thenReturn(expectedCount);
        
        long actualCount = metaData.getNumberOfPages();
        
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testGetNumberOfDisambiguationPages() {
        long expectedCount = 101L;
        when(hibernateMetaData.getNrofDisambiguationPages()).thenReturn(expectedCount);
        
        long actualCount = metaData.getNumberOfDisambiguationPages();
        
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testGetNumberOfRedirectPages() {
        long expectedCount = 202L;
        when(hibernateMetaData.getNrofRedirects()).thenReturn(expectedCount);
        
        long actualCount = metaData.getNumberOfRedirectPages();
        
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void testGetDisambiguationCategory() throws WikiApiException {
        String expectedTitle = "Disambiguation";
        when(hibernateMetaData.getDisambiguationCategory()).thenReturn(expectedTitle);
        when(wiki.getCategory(expectedTitle)).thenReturn(mock(Category.class));
        
        Category category = metaData.getDisambiguationCategory();
        
        assertNotNull(category);
    }

    @Test
    public void testGetMainCategory() throws WikiApiException {
        String expectedTitle = "Main_Category";
        when(hibernateMetaData.getMainCategory()).thenReturn(expectedTitle);
        when(wiki.getCategory(expectedTitle)).thenReturn(mock(Category.class));
        
        Category category = metaData.getMainCategory();
        
        assertNotNull(category);
    }

    @Test
    public void testGetVersion() throws WikiApiException {
        String expectedVersion = "1.0";
        when(hibernateMetaData.getVersion()).thenReturn(expectedVersion);
        
        String version = metaData.getVersion();
        
        assertEquals(expectedVersion, version);
    }

    @Test
    public void testGetLanguage() {
        Language expectedLanguage = Language.ENGLISH;
        when(wiki.getLanguage()).thenReturn(expectedLanguage);
        
        Language language = metaData.getLanguage();
        
        assertEquals(expectedLanguage, language);
    }
}
