package net.revelc.code.formatter.css;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.revelc.code.formatter.ConfigurationSource;
import net.revelc.code.formatter.LineEnding;

public class CssFormatter_ArrowTest {

    private CssFormatter cssFormatter;

    @Before
    public void setUp() {
        cssFormatter = new CssFormatter();
    }

    @Test
    public void testInit_DefaultOptions() {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public boolean isTrimTrailingWhitespace() {
                return false;
            }

            @Override
            public boolean isEndWithNewline() {
                return false;
            }

            @Override
            public String getEncoding() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);
        assertTrue(cssFormatter.isInitialized());
    }

    @Test
    public void testInit_CustomOptions() {
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
            public int getTabSize() {
                return 0;
            }

            @Override
            public boolean isTrimTrailingWhitespace() {
                return false;
            }

            @Override
            public boolean isEndWithNewline() {
                return false;
            }

            @Override
            public String getEncoding() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);
        assertTrue(cssFormatter.isInitialized());
    }

    @Test
    public void testDoFormat_SimpleCss() throws IOException {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public boolean isTrimTrailingWhitespace() {
                return false;
            }

            @Override
            public boolean isEndWithNewline() {
                return false;
            }

            @Override
            public String getEncoding() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);

        String input = "body { color: red; }";
        String result = cssFormatter.doFormat(input, LineEnding.LF);

        // Should format the CSS and add newline at the end
        assertEquals("body {\n    color: red;\n}\n", result);
    }

    @Test
    public void testDoFormat_NoChangeNeeded() throws IOException {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public boolean isTrimTrailingWhitespace() {
                return false;
            }

            @Override
            public boolean isEndWithNewline() {
                return false;
            }

            @Override
            public String getEncoding() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);

        String input = "body {\n    color: red;\n}\n";
        String result = cssFormatter.doFormat(input, LineEnding.LF);

        // Should return null since no formatting changes were made
        assertNull(result);
    }

    @Test
    public void testDoFormat_IEHack() throws IOException {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public boolean isTrimTrailingWhitespace() {
                return false;
            }

            @Override
            public boolean isEndWithNewline() {
                return false;
            }

            @Override
            public String getEncoding() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);

        String input = "body { color: red; }";
        String result = cssFormatter.doFormat(input, LineEnding.LF);

        // Should contain the IE hack replacement
        assertTrue(result.contains("\\9;"));
    }

    @Test
    public void testIsInitialized() {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }

            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public boolean isTrimTrailingWhitespace() {
                return false;
            }

            @Override
            public boolean isEndWithNewline() {
                return false;
            }

            @Override
            public String getEncoding() {
                return null;
            }
        };

        assertFalse(cssFormatter.isInitialized());
        cssFormatter.init(options, cfg);
        assertTrue(cssFormatter.isInitialized());
    }
}
