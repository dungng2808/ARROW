package org.pf4j;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultPluginStatusProvider_ArrowTest {

    @Test
    public void staticFilePathsResolveExpectedNames() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-paths");
        try {
            assertEquals(root.resolve("enabled.txt"), DefaultPluginStatusProvider.getEnabledFilePath(root));
            assertEquals(root.resolve("disabled.txt"), DefaultPluginStatusProvider.getDisabledFilePath(root));
        } finally {
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void instanceFilePathsResolveExpectedNames() throws Exception {
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
            Files.write(root.resolve("enabled.txt"), new byte[0]);
            Files.write(root.resolve("disabled.txt"), new byte[0]);
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            assertFalse(provider.isPluginDisabled("alpha"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void disabledFileMarksPluginDisabled() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disabled");
        try {
            Files.write(root.resolve("disabled.txt"), Arrays.asList("alpha"));
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            assertTrue(provider.isPluginDisabled("alpha"));
            assertFalse(provider.isPluginDisabled("beta"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void enabledFileActsAsWhitelist() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enabled");
        try {
            Files.write(root.resolve("enabled.txt"), Arrays.asList("alpha"));
            Files.write(root.resolve("disabled.txt"), Arrays.asList("beta"));
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            assertFalse(provider.isPluginDisabled("alpha"));
            assertTrue(provider.isPluginDisabled("beta"));
            assertTrue(provider.isPluginDisabled("gamma"));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void disablePluginRemovesPluginFromExistingEnabledFile() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disable-enabled");
        try {
            Files.write(root.resolve("enabled.txt"), Arrays.asList("alpha", "beta"));
            Files.write(root.resolve("disabled.txt"), new byte[0]);
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            provider.disablePlugin("alpha");

            assertTrue(provider.isPluginDisabled("alpha"));
            assertEquals(Arrays.asList("beta"), Files.readAllLines(root.resolve("enabled.txt")));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void enablePluginAddsPluginToExistingEnabledFile() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enable-enabled");
        try {
            Files.write(root.resolve("enabled.txt"), Arrays.asList("alpha"));
            Files.write(root.resolve("disabled.txt"), new byte[0]);
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            provider.enablePlugin("beta");

            assertFalse(provider.isPluginDisabled("beta"));
            assertEquals(Arrays.asList("alpha", "beta"), Files.readAllLines(root.resolve("enabled.txt")));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void disablePluginAddsPluginToDisabledFileWhenEnabledFileIsAbsent() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disable-disabled");
        try {
            Files.write(root.resolve("disabled.txt"), new byte[0]);
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            provider.disablePlugin("alpha");

            assertTrue(provider.isPluginDisabled("alpha"));
            assertEquals(Arrays.asList("alpha"), Files.readAllLines(root.resolve("disabled.txt")));
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }

    @Test
    public void enablePluginRemovesPluginFromDisabledFileWhenEnabledFileIsAbsent() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enable-disabled");
        try {
            Files.write(root.resolve("disabled.txt"), Arrays.asList("alpha"));
            DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);

            provider.enablePlugin("alpha");

            assertFalse(provider.isPluginDisabled("alpha"));
            assertEquals(0, Files.readAllLines(root.resolve("disabled.txt")).size());
        } finally {
            Files.deleteIfExists(root.resolve("enabled.txt"));
            Files.deleteIfExists(root.resolve("disabled.txt"));
            Files.deleteIfExists(root);
        }
    }
}
