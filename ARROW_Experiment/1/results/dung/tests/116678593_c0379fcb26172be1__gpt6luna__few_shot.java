package biz.netcentric.aem.applysystemenvinstallhook;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class VariablesMerger_ArrowTest {

    @Test
    public void replacementSummaryStartsWithZeroCounts() {
        VariablesMerger merger = new VariablesMerger(null);

        assertEquals("Count default value used: 0\nTotal variables replaced: 0",
                merger.getReplacementSummary());
    }

    @Test
    public void appliesDefaultAndUnresolvedVariables() {
        VariablesMerger merger = new VariablesMerger(null);

        String result = merger.applyEnvVars("A=${MISSING:fallback}, B=${UNKNOWN}", null, "sample.txt");

        assertEquals("A=fallback, B=${UNKNOWN}", result);
        assertEquals("Count default value used: 1\nTotal variables replaced: 1\n"
                + "WARN: No value found for variable and no default given: 1",
                merger.getReplacementSummary());
    }

    @Test
    public void extractsVariableDeclarations() {
        VariablesMerger merger = new VariablesMerger(null);

        List<VariablesMerger.EnvVarDeclaration> declarations =
                merger.getEnvVarDeclarations("${FIRST:one} and ${SECOND}");

        assertEquals(2, declarations.size());
        assertEquals("FIRST", declarations.get(0).name);
        assertEquals("one", declarations.get(0).defaultVal);
        assertEquals("SECOND", declarations.get(1).name);
        assertEquals(null, declarations.get(1).defaultVal);
    }

    @Test
    public void emptyTextHasNoDeclarations() {
        VariablesMerger merger = new VariablesMerger(null);
        List<VariablesMerger.EnvVarDeclaration> declarations =
                merger.getEnvVarDeclarations("");

        assertEquals(new ArrayList<VariablesMerger.EnvVarDeclaration>(), declarations);
    }
}
