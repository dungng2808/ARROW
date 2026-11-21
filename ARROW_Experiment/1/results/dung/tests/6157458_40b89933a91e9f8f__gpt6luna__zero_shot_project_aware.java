package org.pf4j;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultPluginStatusProvider_ArrowTest {

    @Test
    public void staticFilePathsUseExpectedNames() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-paths");
        try {
            assertEquals(root.resolve("enabled.txt"), DefaultPluginStatusProvider.getEnabledFilePath(root));
            assertEquals(root.resolve("disabled.txt"), DefaultPluginStatusProvider.getDisabledFilePath(root));
        } finally {
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void instanceFilePathsUseExpectedNames() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-instance-paths");
        try {
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
            assertEquals(root.resolve("enabled.txt"), provider.getEnabledFilePath());
            assertEquals(root.resolve("disabled.txt"), provider.getDisabledFilePath());
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void emptyStatusFilesLeavePluginsEnabledByDefault() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-empty");
        try {
            Files.write(root.resolve("enabled.txt"), Collections.<String>emptyList());
            Files.write(root.resolve("disabled.txt"), Collections.<String>emptyList());

            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            assertFalse(provider.isPluginDisabled("plugin-a"));
            assertFalse(provider.isPluginDisabled("plugin-b"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void enabledFileActsAsWhitelistAndDisabledFileAsBlacklist() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-lists");
        try {
            Files.write(root.resolve("enabled.txt"), Arrays.asList("plugin-a", "plugin-b"));
            Files.write(root.resolve("disabled.txt"), Collections.singletonList("plugin-b"));

            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            assertFalse(provider.isPluginDisabled("plugin-a"));
            assertTrue(provider.isPluginDisabled("plugin-b"));
            assertTrue(provider.isPluginDisabled("plugin-c"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void disablingPluginRemovesItFromExistingEnabledFile() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disable-enabled");
        try {
            Path enabled = root.resolve("enabled.txt");
            Path disabled = root.resolve("disabled.txt");
            Files.write(enabled, Arrays.asList("plugin-a", "plugin-b"));
            Files.write(disabled, Collections.<String>emptyList());

            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
            provider.disablePlugin("plugin-a");

            assertEquals(Arrays.asList("plugin-b"), Files.readAllLines(enabled));
            assertTrue(provider.isPluginDisabled("plugin-a"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void enablingPluginAddsItToExistingEnabledFile() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enable-enabled");
        try {
            Path enabled = root.resolve("enabled.txt");
            Path disabled = root.resolve("disabled.txt");
            Files.write(enabled, Collections.singletonList("plugin-a"));
            Files.write(disabled, Collections.<String>emptyList());

            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
            provider.enablePlugin("plugin-b");

            assertEquals(Arrays.asList("plugin-a", "plugin-b"), Files.readAllLines(enabled));
            assertFalse(provider.isPluginDisabled("plugin-b"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void disablingPluginAddsItToDisabledFileWhenEnabledFileIsAbsent() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disable-disabled");
        try {
            Path disabled = root.resolve("disabled.txt");
            Files.write(disabled, Collections.<String>emptyList());

            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
            provider.disablePlugin("plugin-a");

            assertEquals(Collections.singletonList("plugin-a"), Files.readAllLines(disabled));
            assertTrue(provider.isPluginDisabled("plugin-a"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void enablingPluginRemovesItFromDisabledFileWhenEnabledFileIsAbsent() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enable-disabled");
        try {
            Path disabled = root.resolve("disabled.txt");
            Files.write(disabled, Collections.singletonList("plugin-a"));

            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
            provider.enablePlugin("plugin-a");

            assertEquals(Collections.<String>emptyList(), Files.readAllLines(disabled));
            assertFalse(provider.isPluginDisabled("plugin-a"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }
}
