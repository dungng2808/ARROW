package generators;

import org.junit.Test;
import static org.junit.Assert.*;

public class ContextExecGenerator_ArrowTest {

    @Test
    public void testGetEvilELCode() {
        // Create a mock listener for testing
        ContextExecGenerator.Listener mockListener = new ContextExecGenerator.Listener() {
            @Override
            public void onCodeGenerated(String code) {
                // Mock implementation
            }
        };

        ContextExecGenerator generator = new ContextExecGenerator(mockListener);
        
        // Get the evil EL code
        String evilELCode = generator.getEvilELCode();
        
        // Verify that the generated code contains the expected elements
        assertTrue(evilELCode.contains("javax.script.ScriptEngineManager"));
        assertTrue(evilELCode.contains("JavaScript"));
        assertTrue(evilELCode.contains("ProcessBuilder"));
        assertTrue(evilELCode.contains("%s"));
        assertTrue(evilELCode.contains("bash"));
    }

    @Test
    public void testGetEvilSpelCode() {
        // Create a mock listener for testing
        ContextExecGenerator.Listener mockListener = new ContextExecGenerator.Listener() {
            @Override
            public void onCodeGenerated(String code) {
                // Mock implementation
            }
        };

        ContextExecGenerator generator = new ContextExecGenerator(mockListener);
        
        // Get the evil SPEL code
        String evilSpelCode = generator.getEvilSpelCode();
        
        // Verify that the generated code contains the expected elements
        assertTrue(evilSpelCode.contains("T(java.lang.Runtime).getRuntime().exec"));
        assertTrue(evilSpelCode.contains("%s"));
    }
}
