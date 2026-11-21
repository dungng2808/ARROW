package org.simpleyaml.configuration.file;

import org.junit.Test;
import static org.junit.Assert.*;

public class YamlConfigurationOptions_ArrowTest {

    @Test
    public void testIndentDefault() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        assertEquals(2, options.indent());
    }

    @Test
    public void testIndentSet() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.indent(4);
        assertEquals(4, options.indent());
        assertSame(result, options);
    }

    @Test
    public void testIndentMinimumValid() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.indent(2);
        assertEquals(2, options.indent());
    }

    @Test
    public void testIndentMaximumValid() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.indent(9);
        assertEquals(9, options.indent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndentTooSmall() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        options.indent(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndentTooLarge() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        options.indent(10);
    }

    @Test
    public void testEqualsAndHashCode() {
        YamlConfiguration config1 = new YamlConfiguration();
        YamlConfiguration config2 = new YamlConfiguration();
        YamlConfigurationOptions options1 = config1.options();
        YamlConfigurationOptions options2 = config2.options();

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());

        // Test with different indent values
        options1.indent(3);
        assertNotEquals(options1, options2);
        assertNotEquals(options1.hashCode(), options2.hashCode());

        // Test with same indent values
        options2.indent(3);
        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());
    }

    @Test
    public void testCopyDefaults() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.copyDefaults(true);
        assertSame(result, options);
    }

    @Test
    public void testPathSeparator() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.pathSeparator('.');
        assertSame(result, options);
    }

    @Test
    public void testHeader() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.header("test header");
        assertSame(result, options);
    }

    @Test
    public void testCopyHeader() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        YamlConfigurationOptions result = options.copyHeader(true);
        assertSame(result, options);
    }

    @Test
    public void testConfigurationMethod() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = config.options();
        assertSame(config, options.configuration());
    }
}
