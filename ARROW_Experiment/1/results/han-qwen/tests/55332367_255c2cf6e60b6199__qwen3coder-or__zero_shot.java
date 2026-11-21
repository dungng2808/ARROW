package org.simpleyaml.configuration.file;

import org.junit.Test;
import static org.junit.Assert.*;

public class YamlConfigurationOptions_ArrowTest {

    @Test
    public void testIndentGetter() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertEquals(2, options.indent());
    }

    @Test
    public void testIndentSetterValidValue() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.indent(4);
        
        assertEquals(4, options.indent());
        assertSame(result, options);
    }

    @Test
    public void testIndentSetterMinimumValue() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.indent(2);
        
        assertEquals(2, options.indent());
        assertSame(result, options);
    }

    @Test
    public void testIndentSetterMaximumValue() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.indent(9);
        
        assertEquals(9, options.indent());
        assertSame(result, options);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndentSetterLessThanMinimum() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        options.indent(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndentSetterGreaterThanMaximum() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        options.indent(10);
    }

    @Test
    public void testCopyDefaults() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.copyDefaults(true);
        
        assertSame(result, options);
    }

    @Test
    public void testPathSeparator() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.pathSeparator('.');
        
        assertSame(result, options);
    }

    @Test
    public void testHeader() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.header("test header");
        
        assertSame(result, options);
    }

    @Test
    public void testCopyHeader() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.copyHeader(true);
        
        assertSame(result, options);
    }

    @Test
    public void testConfiguration() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfiguration result = options.configuration();
        
        assertSame(config, result);
    }

    @Test
    public void testEqualsAndHashCode() {
        YamlConfiguration config1 = new YamlConfiguration();
        YamlConfiguration config2 = new YamlConfiguration();
        YamlConfigurationOptions options1 = new YamlConfigurationOptions(config1);
        YamlConfigurationOptions options2 = new YamlConfigurationOptions(config2);
        YamlConfigurationOptions options3 = new YamlConfigurationOptions(config1);
        
        // Test equality with same indent
        assertEquals(options1, options3);
        assertEquals(options1.hashCode(), options3.hashCode());
        
        // Test inequality with different indent
        options1.indent(3);
        assertNotEquals(options1, options3);
        
        // Test equality with same indent after modification
        options3.indent(3);
        assertEquals(options1, options3);
        assertEquals(options1.hashCode(), options3.hashCode());
        
        // Test inequality with different objects
        assertNotEquals(options1, new Object());
        assertNotEquals(options1, null);
    }
}
