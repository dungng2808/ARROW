package com.homeadvisor.robusto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApiCommand_ArrowTest {

    @Test
    public void builderCreatesCommandWithDefaultGroup() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withUriProvider(callback -> "result")
                .withRemoteServiceCallback(null)
                .build();

        assertNotNull(command);
        assertEquals("ApiCommand", command.getCommandKey().name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildRejectsMissingUriProvider() {
        ApiCommand.<String>builder()
                .withRemoteServiceCallback(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildRejectsMissingRemoteServiceCallback() {
        ApiCommand.<String>builder()
                .withUriProvider(callback -> "result")
                .build();
    }

    @Test
    public void builderUsesConfiguredCommandGroup() {
        ApiCommand<String> command = ApiCommand.<String>builder()
                .withCommandGroup("custom-group")
                .withUriProvider(callback -> "result")
                .withRemoteServiceCallback(null)
                .build();

        assertEquals("custom-group", command.getCommandKey().name());
    }
}
