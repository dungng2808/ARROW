package org.simpleyaml.configuration.file;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class YamlConfigurationOptions_ArrowTest {

    private YamlConfigurationOptions newOptions() {
        return new YamlConfigurationOptions(new YamlConfiguration());
    }

    @Test
    public void indentDefaultsToTwo() {
        YamlConfigurationOptions options = newOptions();

        assertEquals(2, options.indent());
    }

    @Test
    public void indentAcceptsMinimumValue() {
        YamlConfigurationOptions options = newOptions();

        assertSame(options, options.indent(2));
        assertEquals(2, options.indent());
    }

    @Test
    public void indentAcceptsMaximumValue() {
        YamlConfigurationOptions options = newOptions();

        assertSame(options, options.indent(9));
        assertEquals(9, options.indent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void indentRejectsValueBelowMinimum() {
        newOptions().indent(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void indentRejectsValueAboveMaximum() {
        newOptions().indent(10);
    }

    @Test
    public void configurationReturnsAssociatedConfiguration() {
        YamlConfiguration configuration = new YamlConfiguration();
        YamlConfigurationOptions options = new YamlConfigurationOptions(configuration);

        assertSame(configuration, options.configuration());
    }

    @Test
    public void fluentMethodsReturnThis() {
        YamlConfigurationOptions options = newOptions();

        assertSame(options, options.copyDefaults(true));
        assertSame(options, options.pathSeparator('.'));
        assertSame(options, options.header("header"));
        assertSame(options, options.copyHeader(true));
    }

    @Test
    public void equalOptionsHaveEqualHashCodes() {
        YamlConfigurationOptions first = newOptions();
        YamlConfigurationOptions second = newOptions();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentIndentMakesOptionsUnequal() {
        YamlConfigurationOptions first = newOptions();
        YamlConfigurationOptions second = newOptions();
        second.indent(3);

        assertNotEquals(first, second);
    }

    @Test
    public void optionsAreNotEqualToNullOrOtherTypes() {
        YamlConfigurationOptions options = newOptions();

        assertNotEquals(options, null);
        assertNotEquals(options, "options");
    }
}
