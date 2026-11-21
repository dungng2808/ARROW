package org.mortbay.jetty.load.generator.starter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LoadGeneratorStarter_ArrowTest {
    @Test
    public void parseReturnsArgumentsForEmptyInput() {
        LoadGeneratorStarterArgs args = LoadGeneratorStarter.parse(new String[0]);

        assertNotNull(args);
        assertEquals(false, args.isHelp());
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
    public void parseUsesSuppliedArgumentsInstance() {
        final LoadGeneratorStarterArgs supplied = new LoadGeneratorStarterArgs();

        LoadGeneratorStarterArgs parsed = LoadGeneratorStarter.parse(new String[0], () -> supplied);

        assertEquals(supplied, parsed);
    }
}
