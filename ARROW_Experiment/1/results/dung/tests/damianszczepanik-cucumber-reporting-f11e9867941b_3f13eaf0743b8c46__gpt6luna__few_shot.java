package net.masterthought.cucumber.generators;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.json.Feature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FeatureReportPage_ArrowTest {

    @Test
    public void getWebPageReturnsFeatureReportFileName() {
        Feature feature = new Feature();
        FeatureReportPage page = new FeatureReportPage(null, null, feature);

        assertEquals(feature.getReportFileName(), page.getWebPage());
    }

    @Test
    public void prepareReportCompletesWithoutError() {
        Feature feature = new Feature();
        Configuration configuration = new Configuration();
        FeatureReportPage page = new FeatureReportPage((ReportResult) null, configuration, feature);

        page.prepareReport();

        assertNotNull(page);
    }
}
