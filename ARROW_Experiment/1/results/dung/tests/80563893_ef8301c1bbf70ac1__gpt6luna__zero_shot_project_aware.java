package org.mortbay.jetty.load.generator.starter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LoadGeneratorStarter_ArrowTest {
    @Test
    public void parseReturnsDefaultArgumentsForEmptyInput() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[0]);

        assertNotNull(args);
    }

    @Test
    public void parseAcceptsUnknownOptions() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[]{"--unknown-option"});

        assertNotNull(args);
    }

    @Test
    public void parseReturnsNullWhenHelpIsRequested() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[]{"--help"});

        assertNull(args);
    }

    @Test
    public void parseUsesProvidedArgumentsSupplier() {
        LoadGeneratorStarterArgs expected = new LoadGeneratorStarterArgs();

        LoadGeneratorStarterArgs actual = LoadGeneratorStarter.parse(
                new String[0],
                () -> expected);

        assertEquals(expected, actual);
    }
}
