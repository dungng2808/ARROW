package generators;

import org.junit.Test;
import static org.junit.Assert.*;

public class ContextExecGenerator_ArrowTest {

    @Test
    public void testGetEvilELCode() {
        // Create a mock listener for testing
        Listener mockListener = new Listener() {
            @Override
            public String getReverseShellCommand() {
                return "test_command";
            }
        };
        
        ContextExecGenerator generator = new ContextExecGenerator(mockListener);
        String result = generator.getEvilELCode();
        
        // Check that the result contains the expected structure
        assertTrue(result.contains("javax.script.ScriptEngineManager"));
        assertTrue(result.contains("JavaScript"));
        assertTrue(result.contains("ProcessBuilder"));
        assertTrue(result.contains("test_command"));
    }

    @Test
    public void testGetEvilSpelCode() {
        // Create a mock listener for testing
        Listener mockListener = new Listener() {
            @Override
            public String getReverseShellCommand() {
                return "test_command";
            }
        };
        
        ContextExecGenerator generator = new ContextExecGenerator(mockListener);
        String result = generator.getEvilSpelCode();
        
        // Check that the result contains the expected structure
        assertTrue(result.contains("T(java.lang.Runtime).getRuntime().exec"));
        assertTrue(result.contains("test_command"));
    }
}
