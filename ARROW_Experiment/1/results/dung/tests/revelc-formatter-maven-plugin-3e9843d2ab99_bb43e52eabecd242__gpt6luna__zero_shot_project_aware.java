package net.revelc.code.formatter.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class CssFormatter_ArrowTest {

    @Test
    public void isInitializedIsFalseBeforeInit() {
        CssFormatter formatter = new CssFormatter();

        assertFalse(formatter.isInitialized());
    }

    @Test
    public void initMarksFormatterInitialized() {
        CssFormatter formatter = new CssFormatter();

        formatter.init(new HashMap<String, String>(), null);

        assertTrue(formatter.isInitialized());
    }

    @Test
    public void formatsCssAndAddsTrailingNewline() throws Exception {
        CssFormatter formatter = new CssFormatter();
        formatter.init(new HashMap<String, String>(), null);

        String result = formatter.format("a{color:red}", null);

        assertNotNull(result);
        assertEquals("a {\n    color: #f00;\n}\n", result);
    }

    @Test
    public void returnsNullWhenInputIsAlreadyFormatted() throws Exception {
        CssFormatter formatter = new CssFormatter();
        formatter.init(new HashMap<String, String>(), null);
        String formatted = formatter.format("a{color:red}", null);

        assertNull(formatter.format(formatted, null));
    }

    @Test
    public void acceptsCustomIndentOption() throws Exception {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        formatter.init(options, null);

        assertEquals("a {\n  color: #f00;\n}\n", formatter.format("a{color:red}", null));
    }
}
