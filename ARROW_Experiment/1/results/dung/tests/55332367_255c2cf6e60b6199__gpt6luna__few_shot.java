package org.simpleyaml.configuration.file;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class YamlConfigurationOptions_ArrowTest {

    @Test
    public void defaultIndentIsTwo() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = configuration.options();

        assertEquals(2, options.indent());
    }

    @Test
    public void indentCanBeSetWithinBounds() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = configuration.options();

        assertSame(options, options.indent(5));
        assertEquals(5, options.indent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void indentCannotBeLessThanTwo() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().indent(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void indentCannotBeGreaterThanNine() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.options().indent(10);
    }

    @Test
    public void equalOptionsHaveEqualHashCodes() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions first = configuration.options();
        YamlConfigurationOptions second = configuration.options();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentIndentMakesOptionsUnequal() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions first = configuration.options();
        YamlConfigurationOptions second = configuration.options().indent(3);

        assertNotEquals(first, second);
    }
}
