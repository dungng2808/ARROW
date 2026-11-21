package org.pf4j;

import org.junit.Before;
import org.junit.Test;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultPluginStatusProvider_ArrowTest {

    private Path tempDir;
    private Path enabledFile;
    private Path disabledFile;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("pf4j-test");
        enabledFile = tempDir.resolve("enabled.txt");
        disabledFile = tempDir.resolve("disabled.txt");
    }

    @Test
    public void testConstructorWithEmptyFiles() throws IOException {
        // Create empty files
        Files.createFile(enabledFile);
        Files.createFile(disabledFile);

        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        assertNotNull(provider);
        assertNotNull(provider.getClass().getMethod("getEnabledPlugins").invoke(provider));
        assertNotNull(provider.getClass().getMethod("getDisabledPlugins").invoke(provider));
    }

    @Test
    public void testIsPluginDisabled_WhenPluginInDisabledList() throws IOException {
        // Setup disabled.txt with some plugins
        Files.write(disabledFile, Arrays.asList("plugin1", "plugin2"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        assertTrue(provider.isPluginDisabled("plugin1"));
        assertTrue(provider.isPluginDisabled("plugin2"));
        assertFalse(provider.isPluginDisabled("plugin3"));
    }

    @Test
    public void testIsPluginDisabled_WhenNoDisabledListButEnabledListExists() throws IOException {
        // Setup enabled.txt with some plugins
        Files.write(enabledFile, Arrays.asList("plugin1", "plugin2"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        assertFalse(provider.isPluginDisabled("plugin1"));
        assertFalse(provider.isPluginDisabled("plugin2"));
        assertTrue(provider.isPluginDisabled("plugin3"));
    }

    @Test
    public void testIsPluginDisabled_WhenBothListsEmpty() throws IOException {
        // Create empty files
        Files.createFile(enabledFile);
        Files.createFile(disabledFile);
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        assertFalse(provider.isPluginDisabled("plugin1"));
    }

    @Test
    public void testDisablePlugin_WhenPluginNotDisabledAndEnabledFileExists() throws IOException {
        // Setup enabled.txt with some plugins
        Files.write(enabledFile, Arrays.asList("plugin1", "plugin2", "plugin3"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        // Disable plugin1
        provider.disablePlugin("plugin1");
        
        // Check that plugin1 is removed from enabled list
        List<String> updatedEnabled = FileUtils.readLines(enabledFile, true);
        assertFalse(updatedEnabled.contains("plugin1"));
        assertTrue(updatedEnabled.contains("plugin2"));
        assertTrue(updatedEnabled.contains("plugin3"));
    }

    @Test
    public void testDisablePlugin_WhenPluginNotDisabledAndEnabledFileDoesNotExist() throws IOException {
        // Ensure enabled.txt doesn't exist
        if (Files.exists(enabledFile)) {
            Files.delete(enabledFile);
        }
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        // Disable plugin1
        provider.disablePlugin("plugin1");
        
        // Check that plugin1 is added to disabled list
        List<String> updatedDisabled = FileUtils.readLines(disabledFile, true);
        assertTrue(updatedDisabled.contains("plugin1"));
    }

    @Test
    public void testEnablePlugin_WhenPluginDisabledAndEnabledFileExists() throws IOException {
        // Setup enabled.txt with some plugins
        Files.write(enabledFile, Arrays.asList("plugin1", "plugin2"));
        // Setup disabled.txt with plugin1
        Files.write(disabledFile, Arrays.asList("plugin1"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        // Enable plugin1
        provider.enablePlugin("plugin1");
        
        // Check that plugin1 is added to enabled list
        List<String> updatedEnabled = FileUtils.readLines(enabledFile, true);
        assertTrue(updatedEnabled.contains("plugin1"));
        assertFalse(updatedEnabled.contains("plugin2"));
    }

    @Test
    public void testEnablePlugin_WhenPluginDisabledAndEnabledFileDoesNotExist() throws IOException {
        // Ensure enabled.txt doesn't exist
        if (Files.exists(enabledFile)) {
            Files.delete(enabledFile);
        }
        // Setup disabled.txt with plugin1
        Files.write(disabledFile, Arrays.asList("plugin1"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(tempDir);
        
        // Enable plugin1
        provider.enablePlugin("plugin1");
        
        // Check that plugin1 is removed from disabled list
        List<String> updatedDisabled = FileUtils.readLines(disabledFile, true);
        assertFalse(updatedDisabled.contains("plugin1"));
    }

    @Test
    public void testGetEnabledFilePath() {
        Path result = DefaultPluginStatusProvider.getEnabledFilePath(tempDir);
        assertEquals(tempDir.resolve("enabled.txt"), result);
    }

    @Test
    public void testGetDisabledFilePath() {
        Path result = DefaultPluginStatusProvider.getDisabledFilePath(tempDir);
        assertEquals(tempDir.resolve("disabled.txt"), result);
    }
}
