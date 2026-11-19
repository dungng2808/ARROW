package net.masterthought.cucumber.generators;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportResult;
import net.masterthought.cucumber.json.Feature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeatureReportPage_ArrowTest {

    @Test
    public void getWebPageReturnsFeatureReportFileName() {
        Feature feature = new Feature();
        String reportFileName = feature.getReportFileName();
        FeatureReportPage page = new FeatureReportPage(null, null, feature);

        assertEquals(reportFileName, page.getWebPage());
    }

    @Test
    public void prepareReportCanBeCalled() {
        FeatureReportPage page = new FeatureReportPage(
                null,
                new Configuration(),
                new Feature());

        page.prepareReport();
    }
}
