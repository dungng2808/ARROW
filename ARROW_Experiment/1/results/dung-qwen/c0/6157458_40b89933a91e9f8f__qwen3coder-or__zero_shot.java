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
    private Path pluginsRoot;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("pf4j-test");
        pluginsRoot = tempDir.resolve("plugins");
        Files.createDirectories(pluginsRoot);
    }

    @Test
    public void testConstructorWithEmptyDirectories() {
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        assertNotNull(provider);
        assertNotNull(provider.getEnabledFilePath());
        assertNotNull(provider.getDisabledFilePath());
    }

    @Test
    public void testIsPluginDisabledWhenNoListsExist() {
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        assertFalse(provider.isPluginDisabled("test-plugin"));
    }

    @Test
    public void testIsPluginDisabledWhenPluginInDisabledList() throws IOException {
        Path disabledFile = pluginsRoot.resolve("disabled.txt");
        Files.write(disabledFile, Arrays.asList("disabled-plugin"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        assertTrue(provider.isPluginDisabled("disabled-plugin"));
        assertFalse(provider.isPluginDisabled("other-plugin"));
    }

    @Test
    public void testIsPluginDisabledWhenPluginNotInEnabledListButEnabledListExists() throws IOException {
        Path enabledFile = pluginsRoot.resolve("enabled.txt");
        Files.write(enabledFile, Arrays.asList("enabled-plugin"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        assertFalse(provider.isPluginDisabled("enabled-plugin"));
        assertTrue(provider.isPluginDisabled("other-plugin"));
    }

    @Test
    public void testDisablePluginWhenAlreadyDisabled() throws IOException {
        Path disabledFile = pluginsRoot.resolve("disabled.txt");
        Files.write(disabledFile, Arrays.asList("test-plugin"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        // Should not throw any exception
        provider.disablePlugin("test-plugin");
        
        // File content should remain unchanged
        List<String> lines = Files.readAllLines(disabledFile);
        assertEquals(1, lines.size());
        assertEquals("test-plugin", lines.get(0));
    }

    @Test
    public void testDisablePluginWhenEnabledListExists() throws IOException {
        Path enabledFile = pluginsRoot.resolve("enabled.txt");
        Files.write(enabledFile, Arrays.asList("test-plugin", "other-plugin"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        provider.disablePlugin("test-plugin");
        
        // Check that test-plugin was removed from enabled.txt
        List<String> lines = Files.readAllLines(enabledFile);
        assertEquals(1, lines.size());
        assertEquals("other-plugin", lines.get(0));
    }

    @Test
    public void testDisablePluginWhenEnabledListDoesNotExist() throws IOException {
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        provider.disablePlugin("test-plugin");
        
        // Check that test-plugin was added to disabled.txt
        Path disabledFile = pluginsRoot.resolve("disabled.txt");
        assertTrue(Files.exists(disabledFile));
        List<String> lines = Files.readAllLines(disabledFile);
        assertEquals(1, lines.size());
        assertEquals("test-plugin", lines.get(0));
    }

    @Test
    public void testEnablePluginWhenAlreadyEnabled() throws IOException {
        Path enabledFile = pluginsRoot.resolve("enabled.txt");
        Files.write(enabledFile, Arrays.asList("test-plugin"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        // Should not throw any exception
        provider.enablePlugin("test-plugin");
        
        // File content should remain unchanged
        List<String> lines = Files.readAllLines(enabledFile);
        assertEquals(1, lines.size());
        assertEquals("test-plugin", lines.get(0));
    }

    @Test
    public void testEnablePluginWhenDisabledListExists() throws IOException {
        Path disabledFile = pluginsRoot.resolve("disabled.txt");
        Files.write(disabledFile, Arrays.asList("test-plugin", "other-plugin"));
        
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        provider.enablePlugin("test-plugin");
        
        // Check that test-plugin was removed from disabled.txt
        List<String> lines = Files.readAllLines(disabledFile);
        assertEquals(1, lines.size());
        assertEquals("other-plugin", lines.get(0));
    }

    @Test
    public void testEnablePluginWhenDisabledListDoesNotExist() throws IOException {
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        provider.enablePlugin("test-plugin");
        
        // Check that test-plugin was added to enabled.txt
        Path enabledFile = pluginsRoot.resolve("enabled.txt");
        assertTrue(Files.exists(enabledFile));
        List<String> lines = Files.readAllLines(enabledFile);
        assertEquals(1, lines.size());
        assertEquals("test-plugin", lines.get(0));
    }

    @Test
    public void testGetEnabledFilePath() {
        Path expectedPath = pluginsRoot.resolve("enabled.txt");
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        assertEquals(expectedPath, provider.getEnabledFilePath());
    }

    @Test
    public void testGetDisabledFilePath() {
        Path expectedPath = pluginsRoot.resolve("disabled.txt");
        DefaultPluginStatusProvider provider = new DefaultPluginStatusProvider(pluginsRoot);
        
        assertEquals(expectedPath, provider.getDisabledFilePath());
    }

    @Test
    public void testStaticGetEnabledFilePath() {
        Path expectedPath = pluginsRoot.resolve("enabled.txt");
        Path actualPath = DefaultPluginStatusProvider.getEnabledFilePath(pluginsRoot);
        
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testStaticGetDisabledFilePath() {
        Path expectedPath = pluginsRoot.resolve("disabled.txt");
        Path actualPath = DefaultPluginStatusProvider.getDisabledFilePath(pluginsRoot);
        
        assertEquals(expectedPath, actualPath);
    }
}
