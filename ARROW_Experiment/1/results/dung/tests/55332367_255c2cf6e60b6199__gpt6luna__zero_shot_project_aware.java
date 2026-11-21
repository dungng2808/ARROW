package org.simpleyaml.configuration.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class YamlConfigurationOptions_ArrowTest {

    private static class TestYamlConfiguration extends YamlConfiguration {
        @Override
        public YamlConfigurationOptions options() {
            return new YamlConfigurationOptions(this);
        }
    }

    private YamlConfigurationOptions options() {
        return new YamlConfigurationOptions(new TestYamlConfiguration());
    }

    @Test
    public void defaultIndentIsTwo() {
        assertEquals(2, options().indent());
    }

    @Test
    public void indentCanBeSetWithinRange() {
        YamlConfigurationOptions options = options();
        assertSame(options, options.indent(5));
        assertEquals(5, options.indent());
    }

    @Test
    public void minimumIndentIsAccepted() {
        assertEquals(2, options().indent(2).indent());
    }

    @Test
    public void maximumIndentIsAccepted() {
        assertEquals(9, options().indent(9).indent());
    }

    @Test
    public void indentBelowMinimumIsRejected() {
        assertThrows(RuntimeException.class, () -> options().indent(1));
    }

    @Test
    public void indentAboveMaximumIsRejected() {
        assertThrows(RuntimeException.class, () -> options().indent(10));
    }

    @Test
    public void equalOptionsHaveEqualHashCodes() {
        YamlConfigurationOptions first = options();
        YamlConfigurationOptions second = options();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentIndentMakesOptionsUnequal() {
        YamlConfigurationOptions first = options();
        YamlConfigurationOptions second = options().indent(3);

        assertNotEquals(first, second);
    }

    @Test
    public void optionsAreNotEqualToNull() {
        assertNotEquals(options(), null);
    }
}
