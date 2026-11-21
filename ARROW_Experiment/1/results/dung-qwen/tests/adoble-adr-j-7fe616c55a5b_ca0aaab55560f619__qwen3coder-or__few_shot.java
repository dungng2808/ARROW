package org.doble.commands;

import org.doble.adr.*;
import org.doble.adr.model.Record;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandNew_ArrowTest {

    private CommandNew commandNew;
    private Environment mockEnv;
    private ADRProperties mockProperties;
    private Path mockRootPath;
    private Path mockDocsPath;
    private Path mockTemplatePath;

    @Before
    public void setUp() throws Exception {
        commandNew = new CommandNew();
        mockEnv = mock(Environment.class);
        mockProperties = mock(ADRProperties.class);
        mockRootPath = mock(Path.class);
        mockDocsPath = mock(Path.class);
        mockTemplatePath = mock(Path.class);

        // Mock the parent command
        CommandADR mockCommandADR = mock(CommandADR.class);
        when(mockCommandADR.getEnvironment()).thenReturn(mockEnv);
        commandNew.commandADR = mockCommandADR;
    }

    @Test
    public void testCallWithNoEditorSet() throws Exception {
        when(mockEnv.editorCommand).thenReturn(null);
        
        int result = commandNew.call();
        
        assertEquals(ADR.ERRORENVIRONMENT, result);
        verify(mockEnv.err).println(anyString());
    }

    @Test
    public void testCallWithNonExistentTemplate() throws Exception {
        when(mockEnv.editorCommand).thenReturn("vim");
        when(ADR.getRootPath(mockEnv)).thenReturn(mockRootPath);
        when(mockProperties.getProperty("docPath")).thenReturn("docs");
        when(mockProperties.getProperty("templateFile")).thenReturn("/nonexistent/template.md");
        when(mockProperties.getProperty("dateFormat")).thenReturn(null);
        when(mockEnv.fileSystem.getPath("/nonexistent/template.md")).thenReturn(mockTemplatePath);
        when(Files.exists(mockTemplatePath)).thenReturn(false);
        
        // Mock the parent command to return our mocked properties
        CommandADR mockCommandADR = mock(CommandADR.class);
        when(mockCommandADR.getEnvironment()).thenReturn(mockEnv);
        commandNew.commandADR = mockCommandADR;
        
        // Mock the properties loading
        doAnswer(invocation -> {
            commandNew.properties = mockProperties;
            return null;
        }).when(mockProperties).load();
        
        try {
            commandNew.call();
            fail("Expected ADRException to be thrown");
        } catch (ADRException e) {
            // Expected
        }
    }

    @Test
    public void testDetermineDateFormatterDefault() throws ADRException {
        DateTimeFormatter formatter = commandNew.determineDateFormatter(null);
        assertNotNull(formatter);
        assertEquals(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM), formatter);
        
        formatter = commandNew.determineDateFormatter("");
        assertNotNull(formatter);
        assertEquals(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM), formatter);
    }

    @Test
    public void testCheckADRExistsWhenExists() throws Exception {
        when(ADR.getRootPath(mockEnv)).thenReturn(mockRootPath);
        when(mockProperties.getProperty("docPath")).thenReturn("docs");
        when(mockRootPath.resolve("docs")).thenReturn(mockDocsPath);
        
        // Mock DirectoryStream to return a matching file
        DirectoryStream<Path> mockStream = mock(DirectoryStream.class);
        when(Files.newDirectoryStream(mockDocsPath)).thenReturn(mockStream);
        
        Path mockAdrFile = mock(Path.class);
        when(mockAdrFile.getFileName()).thenReturn(Paths.get("0001-some-title.md"));
        when(mockStream.iterator()).thenReturn(new java.util.ArrayList<Path>() {{
            add(mockAdrFile);
        }}.iterator());
        
        boolean result = commandNew.checkADRExists(1);
        assertTrue(result);
    }

    @Test
    public void testCheckADRExistsWhenNotExists() throws Exception {
        when(ADR.getRootPath(mockEnv)).thenReturn(mockRootPath);
        when(mockProperties.getProperty("docPath")).thenReturn("docs");
        when(mockRootPath.resolve("docs")).thenReturn(mockDocsPath);
        
        // Mock DirectoryStream to return no matching files
        DirectoryStream<Path> mockStream = mock(DirectoryStream.class);
        when(Files.newDirectoryStream(mockDocsPath)).thenReturn(mockStream);
        
        when(mockStream.iterator()).thenReturn(new java.util.ArrayList<Path>().iterator());
        
        boolean result = commandNew.checkADRExists(999);
        assertFalse(result);
    }

    @Test
    public void testHighestIndexEmptyDirectory() throws Exception {
        when(ADR.getRootPath(mockEnv)).thenReturn(mockRootPath);
        when(mockProperties.getProperty("docPath")).thenReturn("docs");
        when(mockRootPath.resolve("docs")).thenReturn(mockDocsPath);
        
        // Mock Files.list to return empty stream
        java.util.stream.Stream<Path> mockStream = mock(java.util.stream.Stream.class);
        when(Files.list(mockDocsPath)).thenReturn(mockStream);
        when(mockStream.filter(any())).thenReturn(mockStream);
        when(mockStream.mapToInt(any())).thenReturn(OptionalInt.empty());
        
        int result = commandNew.highestIndex();
        assertEquals(0, result);
    }

    @Test
    public void testWellFormedADRValid() {
        Path validPath = Paths.get("0001-test-title.md");
        assertTrue(CommandNew.wellFormedADR(validPath));
    }

    @Test
    public void testWellFormedADRInvalidLength() {
        Path invalidPath = Paths.get("001-test-title.md"); // Only 3 digits
        assertFalse(CommandNew.wellFormedADR(invalidPath));
    }

    @Test
    public void testWellFormedADRInvalidSeparator() {
        Path invalidPath = Paths.get("0001_test-title.md"); // Underscore instead of dash
        assertFalse(CommandNew.wellFormedADR(invalidPath));
    }

    @Test
    public void testWellFormedADRInvalidChars() {
        Path invalidPath = Paths.get("000a-test-title.md"); // Non-digit character
        assertFalse(CommandNew.wellFormedADR(invalidPath));
    }

    @Test
    public void testToInt() {
        Path path = Paths.get("0005-test-title.md");
        assertEquals(5, CommandNew.toInt(path));
    }
}
