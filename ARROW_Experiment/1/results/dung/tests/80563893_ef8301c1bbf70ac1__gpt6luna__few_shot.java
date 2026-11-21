package org.mortbay.jetty.load.generator.starter;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

public class LoadGeneratorStarter_ArrowTest {

    @Test
    public void parseReturnsArgumentsForEmptyInput() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[0]);
        assertNotNull(args);
    }

    @Test
    public void parseReturnsNullWhenHelpIsRequested() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[]{"--help"});
        assertNull(args);
    }

    @Test
    public void parseUsesProvidedArgumentsSupplier() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[0], LoadGeneratorStarterArgs::new);
        assertNotNull(args);
    }

    @Test
    public void parseAcceptsUnknownOptions() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[]{"--unknown-option"});
        assertNotNull(args);
    }

    @Test
    public void prepareReturnsBuilder() {
        LoadGeneratorStarterArgs args = new LoadGeneratorStarterArgs();
        assertNotNull(LoadGeneratorStarter.prepare(args));
    }
}
