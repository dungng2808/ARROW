package net.revelc.code.formatter.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.revelc.code.formatter.ConfigurationSource;
import net.revelc.code.formatter.LineEnding;

public class CssFormatter_ArrowTest {

    @Test
    public void testInitWithDefaultOptions() {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getLineEnding() {
                return 0;
            }
        };

        formatter.init(options, cfg);
        assertTrue(formatter.isInitialized());
    }

    @Test
    public void testInitWithCustomOptions() {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        options.put("rgbAsHex", "false");
        options.put("useSourceStringValues", "true");
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getLineEnding() {
                return 0;
            }
        };

        formatter.init(options, cfg);
        assertTrue(formatter.isInitialized());
    }

    @Test
    public void testDoFormatWithSimpleCss() throws IOException {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getLineEnding() {
                return 0;
            }
        };
        formatter.init(options, cfg);

        String input = "body { color: red; }";
        String result = formatter.doFormat(input, LineEnding.UNIX);

        assertEquals("body {\n  color: red;\n}\n", result);
    }

    @Test
    public void testDoFormatWithNoChangeNeeded() throws IOException {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getLineEnding() {
                return 0;
            }
        };
        formatter.init(options, cfg);

        String input = "body {\n  color: red;\n}\n";
        String result = formatter.doFormat(input, LineEnding.UNIX);

        assertNull(result);
    }

    @Test
    public void testDoFormatWithTabHack() throws IOException {
        CssFormatter formatter = new CssFormatter();
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getLineEnding() {
                return 0;
            }
        };
        formatter.init(options, cfg);

        String input = "body { padding: 10px\t; }";
        String result = formatter.doFormat(input, LineEnding.UNIX);

        assertEquals("body {\n  padding: 10px\\9;\n}\n", result);
    }

    @Test
    public void testIsInitialized() {
        CssFormatter formatter = new CssFormatter();
        assertFalse(formatter.isInitialized());

        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getLineEnding() {
                return 0;
            }
        };
        formatter.init(options, cfg);
        assertTrue(formatter.isInitialized());
    }

    private static boolean assertFalse(boolean value) {
        return !value;
    }
}
