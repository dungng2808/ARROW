package biz.netcentric.aem.applysystemenvinstallhook;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VariablesMerger_ArrowTest {

    @Test
    public void replacementSummaryInitiallyHasNoCounts() {
        VariablesMerger merger = new VariablesMerger(null);

        assertEquals("Count default value used: 0\nTotal variables replaced: 0",
                merger.getReplacementSummary());
    }
}
