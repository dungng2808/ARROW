package net.masterthought.cucumber.generators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.presentation.PresentationMode;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FeatureReportPage_ArrowTest {

    @Test
    public void testGetWebPage() {
        // Given
        ReportResult reportResult = mock(ReportResult.class);
        Configuration configuration = mock(Configuration.class);
        Feature feature = mock(Feature.class);
        
        String expectedFileName = "feature-report.html";
        when(feature.getReportFileName()).thenReturn(expectedFileName);
        
        FeatureReportPage page = new FeatureReportPage(reportResult, configuration, feature);
        
        // When
        String actualFileName = page.getWebPage();
        
        // Then
        assertEquals(expectedFileName, actualFileName);
    }

    @Test
    public void testPrepareReport() {
        // Given
        ReportResult reportResult = mock(ReportResult.class);
        Configuration configuration = mock(Configuration.class);
        Feature feature = mock(Feature.class);
        
        when(configuration.containsPresentationMode(PresentationMode.PARALLEL_TESTING)).thenReturn(true);
        
        FeatureReportPage page = new FeatureReportPage(reportResult, configuration, feature);
        
        // When
        page.prepareReport();
        
        // Then
        assertTrue(page.context.get("parallel_testing"));
    }

    @Test
    public void testPrepareReportWithoutParallelTesting() {
        // Given
        ReportResult reportResult = mock(ReportResult.class);
        Configuration configuration = mock(Configuration.class);
        Feature feature = mock(Feature.class);
        
        when(configuration.containsPresentationMode(PresentationMode.PARALLEL_TESTING)).thenReturn(false);
        
        FeatureReportPage page = new FeatureReportPage(reportResult, configuration, feature);
        
        // When
        page.prepareReport();
        
        // Then
        assertFalse(page.context.get("parallel_testing"));
    }

    @Test
    public void testPrepareReportFeatureInContext() {
        // Given
        ReportResult reportResult = mock(ReportResult.class);
        Configuration configuration = mock(Configuration.class);
        Feature feature = mock(Feature.class);
        
        when(configuration.containsPresentationMode(PresentationMode.PARALLEL_TESTING)).thenReturn(false);
        
        FeatureReportPage page = new FeatureReportPage(reportResult, configuration, feature);
        
        // When
        page.prepareReport();
        
        // Then
        assertEquals(feature, page.context.get("feature"));
    }
}
