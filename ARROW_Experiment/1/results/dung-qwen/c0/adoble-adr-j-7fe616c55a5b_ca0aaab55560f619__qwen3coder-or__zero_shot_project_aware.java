package org.doble.commands;

import org.doble.adr.*;
import org.doble.adr.model.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class CommandNew_ArrowTest {

    private CommandNew commandNew;
    private Environment mockEnv;
    private ADRProperties mockProperties;
    private Path tempDir;
    private Path docsPath;

    @BeforeEach
    public void setUp() throws IOException {
        commandNew = new CommandNew();
        mockEnv = mock(Environment.class);
        mockProperties = mock(ADRProperties.class);
        
        // Create a temporary directory for testing
        tempDir = Files.createTempDirectory("adr_test");
        docsPath = tempDir.resolve("docs");
        Files.createDirectories(docsPath);
        
        // Mock environment setup
        mockEnv.fileSystem = FileSystems.getDefault();
        mockEnv.author = "Test Author";
        mockEnv.editorCommand = "test-editor";
        mockEnv.editorRunner = mock(EditorRunner.class);
        mockEnv.out = System.out;
        mockEnv.err = System.err;
        
        // Mock ADR.getRootPath to return our temp directory
        try (var mockStatic = mockStatic(ADR.class)) {
            mockStatic.when(() -> ADR.getRootPath(any(Environment.class))).thenReturn(tempDir);
            
            // Mock properties loading
            when(mockProperties.getProperty("docPath")).thenReturn(docsPath.toString());
            when(mockProperties.getProperty("templateFile")).thenReturn(null);
            when(mockProperties.getProperty("dateFormat")).thenReturn(null);
            
            // Inject mocks into command
            commandNew.commandADR = mock(CommandADR.class);
            when(commandNew.commandADR.getEnvironment()).thenReturn(mockEnv);
        }
    }

    @Test
    public void testCallWithNoArgumentsReturnsExitCode() throws Exception {
        commandNew.adrTitleParts = new ArrayList<>();
        commandNew.supersedes = new ArrayList<>();
        commandNew.links = new ArrayList<>();
        
        Integer result = commandNew.call();
        assertThat(result, equalTo(0));
    }

    @Test
    public void testDetermineDateFormatterWithNullFormat() throws Exception {
        DateTimeFormatter formatter = commandNew.determineDateFormatter(null);
        assertThat(formatter, notNullValue());
        assertThat(formatter, instanceOf(DateTimeFormatter.class));
    }

    @Test
    public void testDetermineDateFormatterWithEmptyFormat() throws Exception {
        DateTimeFormatter formatter = commandNew.determineDateFormatter("");
        assertThat(formatter, notNullValue());
        assertThat(formatter, instanceOf(DateTimeFormatter.class));
    }

    @Test
    public void testDetermineDateFormatterWithValidFormat() throws Exception {
        DateTimeFormatter formatter = commandNew.determineDateFormatter("ISO_LOCAL_DATE");
        assertThat(formatter, notNullValue());
        assertThat(formatter, instanceOf(DateTimeFormatter.class));
    }

    @Test
    public void testWellFormedADRWithValidName() {
        Path validPath = Paths.get("0001-test-file.md");
        boolean result = CommandNew.wellFormedADR(validPath);
        assertThat(result, is(true));
    }

    @Test
    public void testWellFormedADRWithInvalidNameTooShort() {
        Path invalidPath = Paths.get("001-test.md");
        boolean result = CommandNew.wellFormedADR(invalidPath);
        assertThat(result, is(false));
    }

    @Test
    public void testWellFormedADRWithInvalidNameNoDash() {
        Path invalidPath = Paths.get("0001test.md");
        boolean result = CommandNew.wellFormedADR(invalidPath);
        assertThat(result, is(false));
    }

    @Test
    public void testWellFormedADRWithInvalidNameNonDigits() {
        Path invalidPath = Paths.get("abcd-test.md");
        boolean result = CommandNew.wellFormedADR(invalidPath);
        assertThat(result, is(false));
    }

    @Test
    public void testToInt() {
        Path path = Paths.get("0005-test.md");
        int result = CommandNew.toInt(path);
        assertThat(result, is(5));
    }

    @Test
    public void testHighestIndexWithNoFiles() throws Exception {
        try (var mockStatic = mockStatic(ADR.class)) {
            mockStatic.when(() -> ADR.getRootPath(any(Environment.class))).thenReturn(tempDir);
            
            int result = commandNew.highestIndex();
            assertThat(result, is(0));
        }
    }

    @Test
    public void testHighestIndexWithExistingFiles() throws Exception {
        // Create some test ADR files
        Files.createFile(docsPath.resolve("0001-first.md"));
        Files.createFile(docsPath.resolve("0003-third.md"));
        Files.createFile(docsPath.resolve("0002-second.md"));
        
        try (var mockStatic = mockStatic(ADR.class)) {
            mockStatic.when(() -> ADR.getRootPath(any(Environment.class))).thenReturn(tempDir);
            
            int result = commandNew.highestIndex();
            assertThat(result, is(3));
        }
    }

    @Test
    public void testCheckADRExistsWhenExists() throws Exception {
        Files.createFile(docsPath.resolve("0001-test.md"));
        
        try (var mockStatic = mockStatic(ADR.class)) {
            mockStatic.when(() -> ADR.getRootPath(any(Environment.class))).thenReturn(tempDir);
            
            boolean result = commandNew.checkADRExists(1);
            assertThat(result, is(true));
        }
    }

    @Test
    public void testCheckADRExistsWhenNotExists() throws Exception {
        try (var mockStatic = mockStatic(ADR.class)) {
            mockStatic.when(() -> ADR.getRootPath(any(Environment.class))).thenReturn(tempDir);
            
            boolean result = commandNew.checkADRExists(999);
            assertThat(result, is(false));
        }
    }

    @Test
    public void testCallWithValidTitleAndNoOptions() throws Exception {
        commandNew.adrTitleParts = new ArrayList<>();
        commandNew.adrTitleParts.add("Test");
        commandNew.adrTitleParts.add("Title");
        commandNew.supersedes = new ArrayList<>();
        commandNew.links = new ArrayList<>();
        
        // Mock the record creation
        Record mockRecord = mock(Record.class);
        when(mockRecord.createPeristentRepresentation()).thenReturn(Paths.get("/tmp/test.md"));
        
        try (var mockStatic = mockStatic(ADR.class)) {
            mockStatic.when(() -> ADR.getRootPath(any(Environment.class))).thenReturn(tempDir);
            when(mockProperties.load()).thenReturn(null);
            
            Integer result = commandNew.call();
            assertThat(result, is(0));
        }
    }
}
