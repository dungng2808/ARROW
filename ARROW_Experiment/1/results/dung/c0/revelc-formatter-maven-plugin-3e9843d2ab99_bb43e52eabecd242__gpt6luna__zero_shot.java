package net.revelc.code.formatter.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CssFormatter_ArrowTest {

    @Test
    public void isInitializedIsFalseBeforeInit() {
        CssFormatter formatter = new CssFormatter();

        assertFalse(formatter.isInitialized());
    }

    @Test
    public void initCreatesFormatter() {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<String, String>();

        formatter.init(options, null);

        assertTrue(formatter.isInitialized());
    }

    @Test
    public void formatsCssAndAddsTrailingNewline() throws Exception {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<String, String>();
        formatter.init(options, null);

        String result = formatter.format("a{color:red}", null);

        assertEquals("a {\n    color: #f00;\n}\n", result);
    }

    @Test
    public void returnsNullWhenCodeIsAlreadyFormatted() throws Exception {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<String, String>();
        formatter.init(options, null);

        String formatted = formatter.format("a {\n    color: #f00;\n}\n", null);

        assertNull(formatted);
    }
}
