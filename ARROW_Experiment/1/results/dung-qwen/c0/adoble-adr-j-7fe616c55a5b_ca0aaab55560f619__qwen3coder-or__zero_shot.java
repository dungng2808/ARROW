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

import static org.junit.Assert.*;

public class CommandNew_ArrowTest {

    private CommandNew commandNew;
    private Environment mockEnv;
    private ADRProperties mockProperties;
    private Path tempDir;
    private Path docsPath;

    @Before
    public void setUp() throws IOException {
        commandNew = new CommandNew();
        tempDir = Files.createTempDirectory("adr_test");
        docsPath = tempDir.resolve("docs");
        Files.createDirectories(docsPath);

        mockEnv = new Environment();
        mockEnv.fileSystem = FileSystems.getDefault();
        mockEnv.author = "Test Author";
        mockEnv.editorCommand = "echo";
        mockEnv.editorRunner = new EditorRunner() {
            @Override
            public void run(Path filePath, String editorCommand) {
                // Mock implementation
            }
        };
        mockEnv.out = System.out;
        mockEnv.err = System.err;

        // Mock the ADR root path
        Path rootPath = tempDir;
        Path adrRootPath = rootPath.resolve(".adr");
        Files.createDirectories(adrRootPath);

        // Mock properties
        mockProperties = new ADRProperties(mockEnv);
        mockProperties.setProperty("docPath", "docs");
        mockProperties.setProperty("dateFormat", null);
        mockProperties.setProperty("templateFile", null);
    }

    @Test
    public void testCallWithNoArguments() throws Exception {
        commandNew.adrTitleParts = new ArrayList<>();
        commandNew.commandADR = new CommandADR();
        commandNew.commandADR.setEnvironment(mockEnv);

        try {
            commandNew.call();
            fail("Should have thrown an exception");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testDetermineDateFormatterWithNullFormat() throws Exception {
        String dateFormat = null;
        DateTimeFormatter formatter = commandNew.determineDateFormatter(dateFormat);
        assertEquals(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM), formatter);
    }

    @Test
    public void testDetermineDateFormatterWithEmptyFormat() throws Exception {
        String dateFormat = "";
        DateTimeFormatter formatter = commandNew.determineDateFormatter(dateFormat);
        assertEquals(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM), formatter);
    }

    @Test
    public void testWellFormedADRValid() {
        Path validPath = Paths.get("0001-test-file.md");
        assertTrue(CommandNew.wellFormedADR(validPath));
    }

    @Test
    public void testWellFormedADRInvalidLength() {
        Path invalidPath = Paths.get("001-test-file.md");
        assertFalse(CommandNew.wellFormedADR(invalidPath));
    }

    @Test
    public void testWellFormedADRInvalidSeparator() {
        Path invalidPath = Paths.get("0001test-file.md");
        assertFalse(CommandNew.wellFormedADR(invalidPath));
    }

    @Test
    public void testWellFormedADRInvalidDigits() {
        Path invalidPath = Paths.get("abcd-test-file.md");
        assertFalse(CommandNew.wellFormedADR(invalidPath));
    }

    @Test
    public void testToInt() {
        Path path = Paths.get("0005-test-file.md");
        assertEquals(5, CommandNew.toInt(path));
    }

    @Test
    public void testHighestIndexWithNoFiles() throws Exception {
        // Mock the environment to point to our temp directory
        mockEnv.fileSystem = FileSystems.getDefault();
        mockProperties.setProperty("docPath", docsPath.toString());
        
        // Set up the command with mocked environment
        commandNew.commandADR = new CommandADR();
        commandNew.commandADR.setEnvironment(mockEnv);
        commandNew.properties = mockProperties;
        
        int result = commandNew.highestIndex();
        assertEquals(0, result);
    }

    @Test
    public void testHighestIndexWithExistingFiles() throws Exception {
        // Create some test ADR files
        Files.createFile(docsPath.resolve("0001-first.md"));
        Files.createFile(docsPath.resolve("0003-third.md"));
        Files.createFile(docsPath.resolve("0002-second.md"));

        mockProperties.setProperty("docPath", docsPath.toString());
        commandNew.commandADR = new CommandADR();
        commandNew.commandADR.setEnvironment(mockEnv);
        commandNew.properties = mockProperties;

        int result = commandNew.highestIndex();
        assertEquals(3, result);
    }

    @Test
    public void testCheckADRExistsWhenExists() throws Exception {
        // Create a test ADR file
        Files.createFile(docsPath.resolve("0001-test.md"));

        mockProperties.setProperty("docPath", docsPath.toString());
        commandNew.commandADR = new CommandADR();
        commandNew.commandADR.setEnvironment(mockEnv);
        commandNew.properties = mockProperties;

        boolean result = commandNew.checkADRExists(1);
        assertTrue(result);
    }

    @Test
    public void testCheckADRExistsWhenNotExists() throws Exception {
        mockProperties.setProperty("docPath", docsPath.toString());
        commandNew.commandADR = new CommandADR();
        commandNew.commandADR.setEnvironment(mockEnv);
        commandNew.properties = mockProperties;

        boolean result = commandNew.checkADRExists(1);
        assertFalse(result);
    }

    @Test
    public void testCreateADR() throws Exception {
        // Mock the environment
        mockProperties.setProperty("docPath", docsPath.toString());
        commandNew.commandADR = new CommandADR();
        commandNew.commandADR.setEnvironment(mockEnv);
        commandNew.properties = mockProperties;

        // Create a mock record
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        Record record = new Record.Builder(docsPath, formatter)
                .id(1)
                .name("Test Title")
                .date(LocalDate.now())
                .author("Test Author")
                .build();

        // This should not throw an exception
        commandNew.createADR(record);
    }
}
