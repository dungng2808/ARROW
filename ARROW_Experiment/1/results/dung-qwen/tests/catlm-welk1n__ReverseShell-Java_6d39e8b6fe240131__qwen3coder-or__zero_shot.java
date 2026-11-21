package generators;

import org.junit.Test;
import static org.junit.Assert.*;

public class ContextExecGenerator_ArrowTest {

    @Test
    public void testGetEvilELCode() {
        // Create a mock listener for testing
        Listener mockListener = new Listener() {
            @Override
            public void onEvent(String event) {
                // Mock implementation
            }
        };
        
        ContextExecGenerator generator = new ContextExecGenerator(mockListener);
        
        // Get the evil EL code
        String evilELCode = generator.getEvilELCode();
        
        // Verify that the code contains the expected structure
        assertTrue(evilELCode.contains("javax.script.ScriptEngineManager"));
        assertTrue(evilELCode.contains("JavaScript"));
        assertTrue(evilELCode.contains("ProcessBuilder"));
        assertTrue(evilELCode.contains("%s"));
        assertTrue(evilELCode.contains("bash"));
    }

    @Test
    public void testGetEvilSpelCode() {
        // Create a mock listener for testing
        Listener mockListener = new Listener() {
            @Override
            public void onEvent(String event) {
                // Mock implementation
            }
        };
        
        ContextExecGenerator generator = new ContextExecGenerator(mockListener);
        
        // Get the evil SPEL code
        String evilSpelCode = generator.getEvilSpelCode();
        
        // Verify that the code contains the expected structure
        assertTrue(evilSpelCode.contains("T(java.lang.Runtime).getRuntime().exec"));
        assertTrue(evilSpelCode.contains("%s"));
    }
}
