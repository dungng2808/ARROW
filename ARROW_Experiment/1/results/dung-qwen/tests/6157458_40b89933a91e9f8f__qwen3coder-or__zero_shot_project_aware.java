package org.pf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultPluginStatusProvider_ArrowTest {

    @TempDir
    Path tempDir;

    private DefaultPluginStatusProvider provider;

    @BeforeEach
    public void setUp() {
        provider = new DefaultPluginStatusProvider(tempDir);
    }

    @Test
    public void testConstructorWithEmptyDirectory() {
        assertNotNull(provider);
        assertThat(provider.getClass().getSimpleName(), equalTo("DefaultPluginStatusProvider"));
    }

    @Test
    public void testIsPluginDisabledWhenNoFilesExist() {
        assertFalse(provider.isPluginDisabled("test-plugin"));
    }

    @Test
    public void testIsPluginDisabledWhenPluginInDisabledList() throws IOException {
        Path disabledFile = tempDir.resolve("disabled.txt");
        Files.write(disabledFile, Arrays.asList("plugin1", "plugin2"));

        DefaultPluginStatusProvider providerWithDisabled = new DefaultPluginStatusProvider(tempDir);
        assertTrue(providerWithDisabled.isPluginDisabled("plugin1"));
        assertTrue(providerWithDisabled.isPluginDisabled("plugin2"));
        assertFalse(providerWithDisabled.isPluginDisabled("plugin3"));
    }

    @Test
    public void testIsPluginDisabledWhenPluginNotInEnabledList() throws IOException {
        Path enabledFile = tempDir.resolve("enabled.txt");
        Files.write(enabledFile, Arrays.asList("plugin1", "plugin2"));

        DefaultPluginStatusProvider providerWithEnabled = new DefaultPluginStatusProvider(tempDir);
        assertFalse(providerWithEnabled.isPluginDisabled("plugin1"));
        assertFalse(providerWithEnabled.isPluginDisabled("plugin2"));
        assertTrue(providerWithEnabled.isPluginDisabled("plugin3"));
    }

    @Test
    public void testIsPluginDisabledWhenPluginInBothLists() throws IOException {
        Path enabledFile = tempDir.resolve("enabled.txt");
        Path disabledFile = tempDir.resolve("disabled.txt");
        Files.write(enabledFile, Arrays.asList("plugin1", "plugin2"));
        Files.write(disabledFile, Arrays.asList("plugin2", "plugin3"));

        DefaultPluginStatusProvider providerWithBoth = new DefaultPluginStatusProvider(tempDir);
        assertFalse(providerWithBoth.isPluginDisabled("plugin1"));
        assertTrue(providerWithBoth.isPluginDisabled("plugin2"));
        assertTrue(providerWithBoth.isPluginDisabled("plugin3"));
    }

    @Test
    public void testDisablePluginWhenNotInAnyList() throws IOException {
        Path enabledFile = tempDir.resolve("enabled.txt");
        Path disabledFile = tempDir.resolve("disabled.txt");

        // Initially both files don't exist
        assertFalse(Files.exists(enabledFile));
        assertFalse(Files.exists(disabledFile));

        provider.disablePlugin("test-plugin");

        // After disabling, disabled.txt should be created and contain the plugin
        assertTrue(Files.exists(disabledFile));
        List<String> lines = Files.readAllLines(disabledFile);
        assertThat(lines, contains("test-plugin"));
    }

    @Test
    public void testDisablePluginWhenInEnabledList() throws IOException {
        Path enabledFile = tempDir.resolve("enabled.txt");
        Files.write(enabledFile, Arrays.asList("plugin1", "plugin2", "plugin3"));

        DefaultPluginStatusProvider providerWithEnabled = new DefaultPluginStatusProvider(tempDir);
        providerWithEnabled.disablePlugin("plugin2");

        List<String> lines = Files.readAllLines(enabledFile);
        assertThat(lines, containsInAnyOrder("plugin1", "plugin3"));
    }

    @Test
    public void testEnablePluginWhenNotInAnyList() throws IOException {
        Path enabledFile = tempDir.resolve("enabled.txt");
        Path disabledFile = tempDir.resolve("disabled.txt");

        // Initially both files don't exist
        assertFalse(Files.exists(enabledFile));
        assertFalse(Files.exists(disabledFile));

        provider.enablePlugin("test-plugin");

        // After enabling, enabled.txt should be created and contain the plugin
        assertTrue(Files.exists(enabledFile));
        List<String> lines = Files.readAllLines(enabledFile);
        assertThat(lines, contains("test-plugin"));
    }

    @Test
    public void testEnablePluginWhenInDisabledList() throws IOException {
        Path disabledFile = tempDir.resolve("disabled.txt");
        Files.write(disabledFile, Arrays.asList("plugin1", "plugin2", "plugin3"));

        DefaultPluginStatusProvider providerWithDisabled = new DefaultPluginStatusProvider(tempDir);
        providerWithDisabled.enablePlugin("plugin2");

        List<String> lines = Files.readAllLines(disabledFile);
        assertThat(lines, containsInAnyOrder("plugin1", "plugin3"));
    }

    @Test
    public void testGetEnabledFilePath() {
        Path expectedPath = tempDir.resolve("enabled.txt");
        assertEquals(expectedPath, provider.getEnabledFilePath());
    }

    @Test
    public void testGetDisabledFilePath() {
        Path expectedPath = tempDir.resolve("disabled.txt");
        assertEquals(expectedPath, provider.getDisabledFilePath());
    }

    @Test
    public void testStaticGetEnabledFilePath() {
        Path expectedPath = tempDir.resolve("enabled.txt");
        assertEquals(expectedPath, DefaultPluginStatusProvider.getEnabledFilePath(tempDir));
    }

    @Test
    public void testStaticGetDisabledFilePath() {
        Path expectedPath = tempDir.resolve("disabled.txt");
        assertEquals(expectedPath, DefaultPluginStatusProvider.getDisabledFilePath(tempDir));
    }
}
