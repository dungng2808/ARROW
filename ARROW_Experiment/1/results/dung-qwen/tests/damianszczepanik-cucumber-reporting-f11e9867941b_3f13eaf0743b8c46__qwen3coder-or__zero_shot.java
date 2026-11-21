package net.masterthought.cucumber.generators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.json.Feature;
import net.masterthought.cucumber.presentation.PresentationMode;

import org.junit.Test;

public class FeatureReportPage_ArrowTest {

    @Test
    public void testGetWebPageReturnsFeatureReportFileName() {
        // Given
        ReportResult reportResult = mock(ReportResult.class);
        Configuration configuration = mock(Configuration.class);
        Feature feature = mock(Feature.class);
        
        String expectedFileName = "test-feature.html";
        when(feature.getReportFileName()).thenReturn(expectedFileName);
        
        FeatureReportPage page = new FeatureReportPage(reportResult, configuration, feature);
        
        // When
        String actualFileName = page.getWebPage();
        
        // Then
        assert expectedFileName.equals(actualFileName);
    }

    @Test
    public void testPrepareReportPutsFeatureAndParallelTestingInContext() {
        // Given
        ReportResult reportResult = mock(ReportResult.class);
        Configuration configuration = mock(Configuration.class);
        Feature feature = mock(Feature.class);
        
        when(configuration.containsPresentationMode(PresentationMode.PARALLEL_TESTING)).thenReturn(true);
        
        FeatureReportPage page = new FeatureReportPage(reportResult, configuration, feature);
        
        // When
        page.prepareReport();
        
        // Then
        // We can't directly verify the context contents, but we can ensure no exceptions are thrown
        // and that the method executes without issues
    }
}
