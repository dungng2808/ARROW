package org.simpleyaml.configuration.file;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class YamlConfigurationOptions_ArrowTest {

    @Test
    public void testIndentGetter() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertEquals(2, options.indent());
    }

    @Test
    public void testIndentSetter() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        YamlConfigurationOptions result = options.indent(4);
        
        assertEquals(4, options.indent());
        assertSame(result, options);
    }

    @Test
    public void testIndentMinimumValue() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertDoesNotThrow(() -> options.indent(2));
    }

    @Test
    public void testIndentMaximumValue() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertDoesNotThrow(() -> options.indent(9));
    }

    @Test
    public void testIndentTooSmall() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertThrows(IllegalArgumentException.class, () -> options.indent(1));
    }

    @Test
    public void testIndentTooLarge() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertThrows(IllegalArgumentException.class, () -> options.indent(10));
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
    public void testEquals() {
        YamlConfiguration config1 = new YamlConfiguration();
        YamlConfiguration config2 = new YamlConfiguration();
        YamlConfigurationOptions options1 = new YamlConfigurationOptions(config1);
        YamlConfigurationOptions options2 = new YamlConfigurationOptions(config2);
        
        assertTrue(options1.equals(options1));
        assertFalse(options1.equals(null));
        assertFalse(options1.equals(new Object()));
        
        // Different indent values
        options1.indent(3);
        assertFalse(options1.equals(options2));
        
        // Same indent values
        options2.indent(3);
        assertTrue(options1.equals(options2));
        
        // Test with different configurations but same options
        YamlConfigurationOptions options3 = new YamlConfigurationOptions(config1);
        options3.indent(2);
        assertTrue(options1.equals(options3));
    }

    @Test
    public void testHashCode() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options1 = new YamlConfigurationOptions(config);
        YamlConfigurationOptions options2 = new YamlConfigurationOptions(config);
        
        assertEquals(options1.hashCode(), options2.hashCode());
        
        options1.indent(3);
        assertNotEquals(options1.hashCode(), options2.hashCode());
        
        options2.indent(3);
        assertEquals(options1.hashCode(), options2.hashCode());
    }

    @Test
    public void testConfigurationMethod() {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(config);
        
        assertSame(config, options.configuration());
    }
}
