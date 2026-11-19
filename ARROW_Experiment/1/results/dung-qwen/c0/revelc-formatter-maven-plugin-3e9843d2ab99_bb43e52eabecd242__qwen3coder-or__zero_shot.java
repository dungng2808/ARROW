package net.revelc.code.formatter.css;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
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
    public void testInitWithDefaultOptions() {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);
        Assert.assertTrue(cssFormatter.isInitialized());
    }

    @Test
    public void testInitWithCustomOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("indent", "2");
        options.put("rgbAsHex", "false");
        options.put("useSourceStringValues", "true");
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }
        };

        cssFormatter.init(options, cfg);
        Assert.assertTrue(cssFormatter.isInitialized());
    }

    @Test
    public void testDoFormatWithSimpleCss() throws IOException {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }
        };
        cssFormatter.init(options, cfg);

        String input = "body { color: red; }";
        String result = cssFormatter.doFormat(input, LineEnding.UNIX);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDoFormatWithNoChangeNeeded() throws IOException {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }
        };
        cssFormatter.init(options, cfg);

        String input = "body { color: red; }\n";
        String result = cssFormatter.doFormat(input, LineEnding.UNIX);
        Assert.assertNull(result);
    }

    @Test
    public void testDoFormatWithTabHack() throws IOException {
        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }
        };
        cssFormatter.init(options, cfg);

        String input = "body {\n\tcolor: red;\n}";
        String result = cssFormatter.doFormat(input, LineEnding.UNIX);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("\\9;"));
    }

    @Test
    public void testIsInitialized() {
        Assert.assertFalse(cssFormatter.isInitialized());

        Map<String, String> options = new HashMap<>();
        ConfigurationSource cfg = new ConfigurationSource() {
            @Override
            public String getCacheFile() {
                return null;
            }
        };
        cssFormatter.init(options, cfg);
        Assert.assertTrue(cssFormatter.isInitialized());
    }
}
