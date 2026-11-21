package net.revelc.code.formatter.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.revelc.code.formatter.ConfigurationSource;
import net.revelc.code.formatter.LineEnding;

public class CssFormatter_ArrowTest {

    @Test
    public void isNotInitializedBeforeInit() {
        CssFormatter formatter = new CssFormatter();
        assertFalse(formatter.isInitialized());
    }

    @Test
    public void initializesWithDefaultOptions() {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        formatter.init(options, null);
        assertTrue(formatter.isInitialized());
    }

    @Test
    public void formatsCssAndAddsLineEnding() throws Exception {
        CssFormatter formatter = new CssFormatter();
        formatter.init(new HashMap<String, String>(), null);

        String result = formatter.doFormat("a{color:red}", LineEnding.UNIX);

        assertEquals("a {\n    color: #f00;\n}\n", result);
    }

    @Test
    public void returnsNullWhenInputIsAlreadyFormatted() throws Exception {
        CssFormatter formatter = new CssFormatter();
        formatter.init(new HashMap<String, String>(), null);

        String result = formatter.doFormat("a {\n    color: #f00;\n}\n", LineEnding.UNIX);

        assertNull(result);
    }

    @Test
    public void formatsWithConfiguredIndent() throws Exception {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        formatter.init(options, null);

        String result = formatter.doFormat("a{color:red}", LineEnding.UNIX);

        assertEquals("a {\n  color: #f00;\n}\n", result);
    }
}
