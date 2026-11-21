package org.pf4j;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultPluginStatusProvider_ArrowTest {

    @Test
    public void staticFilePathsUseExpectedNames() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-paths");
        assertEquals(root.resolve("enabled.txt"), DefaultPluginStatusProvider.getEnabledFilePath(root));
        assertEquals(root.resolve("disabled.txt"), DefaultPluginStatusProvider.getDisabledFilePath(root));
        Files.deleteIfExists(root);
    }

    @Test
    public void instanceFilePathsUseProvidedRoot() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-instance-paths");
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        assertEquals(root.resolve("enabled.txt"), provider.getEnabledFilePath());
        assertEquals(root.resolve("disabled.txt"), provider.getDisabledFilePath());
        Files.deleteIfExists(root);
    }

    @Test
    public void emptyStatusFilesAllowPluginsByDefault() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-empty");
        Files.write(DefaultPluginStatusProvider.getEnabledFilePath(root), Collections.<String>emptyList());
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Collections.<String>emptyList());

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        assertFalse(provider.isPluginDisabled("plugin"));
    }

    @Test
    public void enabledListActsAsWhitelist() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enabled");
        Files.write(DefaultPluginStatusProvider.getEnabledFilePath(root), Arrays.asList("allowed"));
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Collections.<String>emptyList());

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        assertFalse(provider.isPluginDisabled("allowed"));
        assertTrue(provider.isPluginDisabled("other"));
    }

    @Test
    public void disabledListTakesPrecedence() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disabled");
        Files.write(DefaultPluginStatusProvider.getEnabledFilePath(root), Collections.<String>emptyList());
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Arrays.asList("blocked"));

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        assertTrue(provider.isPluginDisabled("blocked"));
        assertFalse(provider.isPluginDisabled("other"));
    }

    @Test
    public void disablePluginAddsToDisabledFileWhenNoEnabledFileExists() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disable");
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Collections.<String>emptyList());

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        provider.disablePlugin("plugin");

        assertTrue(provider.isPluginDisabled("plugin"));
        assertEquals(Arrays.asList("plugin"), Files.readAllLines(DefaultPluginStatusProvider.getDisabledFilePath(root)));
    }

    @Test
    public void enablePluginRemovesFromDisabledFileWhenNoEnabledFileExists() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enable");
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Arrays.asList("plugin"));

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        provider.enablePlugin("plugin");

        assertFalse(provider.isPluginDisabled("plugin"));
        assertEquals(Collections.emptyList(), Files.readAllLines(DefaultPluginStatusProvider.getDisabledFilePath(root)));
    }

    @Test
    public void disablePluginRemovesFromEnabledWhitelist() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-disable-enabled");
        Files.write(DefaultPluginStatusProvider.getEnabledFilePath(root), Arrays.asList("plugin", "other"));
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Collections.<String>emptyList());

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        provider.disablePlugin("plugin");

        assertTrue(provider.isPluginDisabled("plugin"));
        assertEquals(Arrays.asList("other"), Files.readAllLines(DefaultPluginStatusProvider.getEnabledFilePath(root)));
    }

    @Test
    public void enablePluginAddsToEnabledWhitelist() throws Exception {
        Path root = Files.createTempDirectory("plugin-status-enable-enabled");
        Files.write(DefaultPluginStatusProvider.getEnabledFilePath(root), Arrays.asList("other"));
        Files.write(DefaultPluginStatusProvider.getDisabledFilePath(root), Collections.<String>emptyList());

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(root);
        provider.enablePlugin("plugin");

        assertFalse(provider.isPluginDisabled("plugin"));
        assertEquals(Arrays.asList("other", "plugin"), Files.readAllLines(DefaultPluginStatusProvider.getEnabledFilePath(root)));
    }
}
