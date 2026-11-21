package com.gradle;

import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Utils_ArrowTest {

    @Test
    public void testSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        System.setProperty("test.property", "value");
        try {
            Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "TEST_ENV", providers);
            assertEquals(Optional.of("value"), result);
        } finally {
            System.clearProperty("test.property");
        }
    }

    @Test
    public void testSysPropertyOrEnvVariable_EnvVariablePresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        System.setProperty("test.property", null);
        try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
            mockedSystem.when(() -> System.getenv("TEST_ENV")).thenReturn("env_value");
            Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "TEST_ENV", providers);
            assertEquals(Optional.of("env_value"), result);
        }
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        System.setProperty("test.property", "true");
        try {
            Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "TEST_ENV", providers);
            assertEquals(Optional.of(true), result);
        } finally {
            System.clearProperty("test.property");
        }
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_EnvVariablePresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        System.setProperty("test.property", null);
        try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
            mockedSystem.when(() -> System.getenv("TEST_ENV")).thenReturn("false");
            Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "TEST_ENV", providers);
            assertEquals(Optional.of(false), result);
        }
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        System.setProperty("test.property", "PT10S");
        try {
            Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "TEST_ENV", providers);
            assertEquals(Optional.of(Duration.ofSeconds(10)), result);
        } finally {
            System.clearProperty("test.property");
        }
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_EnvVariablePresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        System.setProperty("test.property", null);
        try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
            mockedSystem.when(() -> System.getenv("TEST_ENV")).thenReturn("PT5M");
            Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "TEST_ENV", providers);
            assertEquals(Optional.of(Duration.ofMinutes(5)), result);
        }
    }

    @Test
    public void testEnvVariable_Gradle65OrNewer() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::isGradle65OrNewer).thenReturn(true);
            mockedUtils.when(Utils::isGradle74OrNewer).thenReturn(false);
            Provider<String> mockProvider = Mockito.mock(Provider.class);
            when(providers.environmentVariable("TEST_VAR")).thenReturn(mockProvider);
            when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
            when(mockProvider.getOrNull()).thenReturn("env_value");
            Optional<String> result = Utils.envVariable("TEST_VAR", providers);
            assertEquals(Optional.of("env_value"), result);
        }
    }

    @Test
    public void testEnvVariable_NotGradle65OrNewer() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::isGradle65OrNewer).thenReturn(false);
            try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
                mockedSystem.when(() -> System.getenv("TEST_VAR")).thenReturn("env_value");
                Optional<String> result = Utils.envVariable("TEST_VAR", providers);
                assertEquals(Optional.of("env_value"), result);
            }
        }
    }

    @Test
    public void testSysProperty_Gradle65OrNewer() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::isGradle65OrNewer).thenReturn(true);
            mockedUtils.when(Utils::isGradle74OrNewer).thenReturn(false);
            Provider<String> mockProvider = Mockito.mock(Provider.class);
            when(providers.systemProperty("TEST_PROP")).thenReturn(mockProvider);
            when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
            when(mockProvider.getOrNull()).thenReturn("prop_value");
            Optional<String> result = Utils.sysProperty("TEST_PROP", providers);
            assertEquals(Optional.of("prop_value"), result);
        }
    }

    @Test
    public void testSysProperty_NotGradle65OrNewer() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::isGradle65OrNewer).thenReturn(false);
            try (MockedStatic<System> mockedSystem = Mockito.mockStatic(System.class)) {
                mockedSystem.when(() -> System.getProperty("TEST_PROP")).thenReturn("prop_value");
                Optional<String> result = Utils.sysProperty("TEST_PROP", providers);
                assertEquals(Optional.of("prop_value"), result);
            }
        }
    }

    @Test
    public void testIsNotEmpty_Null() {
        assertFalse(Utils.isNotEmpty(null));
    }

    @Test
    public void testIsNotEmpty_EmptyString() {
        assertFalse(Utils.isNotEmpty(""));
    }

    @Test
    public void testIsNotEmpty_NonEmptyString() {
        assertTrue(Utils.isNotEmpty("test"));
    }

    @Test
    public void testAppendIfMissing_NoSuffix() {
        assertEquals("test", Utils.appendIfMissing("test", 's'));
    }

    @Test
    public void testAppendIfMissing_WithSuffix() {
        assertEquals("tests", Utils.appendIfMissing("test", 's'));
    }

    @Test
    public void testUrlEncode_NormalString() {
        assertEquals("hello%20world", Utils.urlEncode("hello world"));
    }

    @Test
    public void testRedactUserInfo_NormalUrl() throws URISyntaxException {
        assertEquals("https://example.com/path", Utils.redactUserInfo("https://user:pass@example.com/path"));
    }

    @Test
    public void testRedactUserInfo_NoUserInfo() {
        assertEquals("https://example.com/path", Utils.redactUserInfo("https://example.com/path"));
    }

    @Test
    public void testRedactUserInfo_InvalidUrl() {
        assertEquals("invalid-url", Utils.redactUserInfo("invalid-url"));
    }

    @Test
    public void testToWebRepoUri_ValidGitUrl() {
        Optional<URI> result = Utils.toWebRepoUri("git://github.com/acme-inc/my-project.git");
        assertTrue(result.isPresent());
        try {
            assertEquals(new URI("https://github.com/acme-inc/my-project"), result.get());
        } catch (URISyntaxException e) {
            fail("Should not throw URISyntaxException");
        }
    }

    @Test
    public void testToWebRepoUri_InvalidFormat() {
        Optional<URI> result = Utils.toWebRepoUri("invalid-format");
        assertFalse(result.isPresent());
    }

    @Test
    public void testToWebRepoUri_HttpsFormat() {
        Optional<URI> result = Utils.toWebRepoUri("https://github.com/acme-inc/my-project.git");
        assertTrue(result.isPresent());
        try {
            assertEquals(new URI("https://github.com/acme-inc/my-project"), result.get());
        } catch (URISyntaxException e) {
            fail("Should not throw URISyntaxException");
        }
    }

    @Test
    public void testToWebRepoUri_SshFormat() {
        Optional<URI> result = Utils.toWebRepoUri("ssh://git@github.com/acme-inc/my-project.git");
        assertTrue(result.isPresent());
        try {
            assertEquals(new URI("https://github.com/acme-inc/my-project"), result.get());
        } catch (URISyntaxException e) {
            fail("Should not throw URISyntaxException");
        }
    }

    @Test
    public void testIsGradle4OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle4OrNewer());
        }
    }

    @Test
    public void testIsGradle43rNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle43rNewer());
        }
    }

    @Test
    public void testIsGradle5OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle5OrNewer());
        }
    }

    @Test
    public void testIsGradle6OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle6OrNewer());
        }
    }

    @Test
    public void testIsGradle61OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle61OrNewer());
        }
    }

    @Test
    public void testIsGradle62OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle62OrNewer());
        }
    }

    @Test
    public void testIsGradle65OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle65OrNewer());
        }
    }

    @Test
    public void testIsGradle74OrNewer() {
        try (MockedStatic<GradleVersion> mockedGradleVersion = Mockito.mockStatic(GradleVersion.class)) {
            GradleVersion currentVersion = Mockito.mock(GradleVersion.class);
            mockedGradleVersion.when(GradleVersion::current).thenReturn(currentVersion);
            when(currentVersion.compareTo(Mockito.any(GradleVersion.class))).thenReturn(1);
            assertTrue(Utils.isGradle74OrNewer());
        }
    }

    @Test
    public void testReadPropertiesFile_Success() throws IOException {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Directory projectDirectory = Mockito.mock(Directory.class);
        RegularFile regularFile = Mockito.mock(RegularFile.class);
        Provider<byte[]> fileContentProvider = Mockito.mock(Provider.class);
        when(projectDirectory.file("test.properties")).thenReturn(regularFile);
        when(providers.fileContents(regularFile)).thenReturn(fileContentProvider);
        when(fileContentProvider.getAsBytes()).thenReturn(fileContentProvider);
        when(fileContentProvider.getOrElse(any(byte[].class))).thenReturn("key=value\n".getBytes(StandardCharsets.UTF_8));

        Properties result = Utils.readPropertiesFile("test.properties", providers, projectDirectory);
        assertNotNull(result);
        assertEquals("value", result.getProperty("key"));
    }

    @Test
    public void testReadFile_Gradle65OrNewer() throws IOException {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Directory projectDirectory = Mockito.mock(Directory.class);
        RegularFile regularFile = Mockito.mock(RegularFile.class);
        Provider<byte[]> fileContentProvider = Mockito.mock(Provider.class);
        when(projectDirectory.file("test.txt")).thenReturn(regularFile);
        when(providers.fileContents(regularFile)).thenReturn(fileContentProvider);
        when(fileContentProvider.getAsBytes()).thenReturn(fileContentProvider);
        when(fileContentProvider.getOrElse(any(byte[].class))).thenReturn("test content".getBytes(StandardCharsets.UTF_8));

        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::isGradle65OrNewer).thenReturn(true);
            InputStream result = Utils.readFile("test.txt", providers, projectDirectory);
            assertNotNull(result);
            byte[] buffer = new byte[1024];
            int bytesRead = result.read(buffer);
            assertEquals("test content", new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testReadFile_NotGradle65OrNewer() throws IOException {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Directory projectDirectory = Mockito.mock(Directory.class);
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(Utils::isGradle65OrNewer).thenReturn(false);
            try (MockedStatic<FileInputStream> mockedFIS = Mockito.mockStatic(FileInputStream.class)) {
                FileInputStream mockFIS = Mockito.mock(FileInputStream.class);
                mockedFIS.when(() -> new FileInputStream("test.txt")).thenReturn(mockFIS);
                InputStream result = Utils.readFile("test.txt", providers, projectDirectory);
                assertEquals(mockFIS, result);
            }
        }
    }

    @Test
    public void testExecAndCheckSuccess_Success() {
        try (MockedStatic<Runtime> mockedRuntime = Mockito.mockStatic(Runtime.class)) {
            Runtime mockRuntime = Mockito.mock(Runtime.class);
            Process mockProcess = Mockito.mock(Process.class);
            mockedRuntime.when(Runtime::getRuntime).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);
            when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(mockProcess.exitValue()).thenReturn(0);
            boolean result = Utils.execAndCheckSuccess("echo", "test");
            assertTrue(result);
        }
    }

    @Test
    public void testExecAndCheckSuccess_Failure() {
        try (MockedStatic<Runtime> mockedRuntime = Mockito.mockStatic(Runtime.class)) {
            Runtime mockRuntime = Mockito.mock(Runtime.class);
            Process mockProcess = Mockito.mock(Process.class);
            mockedRuntime.when(Runtime::getRuntime).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);
            when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);
            boolean result = Utils.execAndCheckSuccess("echo", "test");
            assertFalse(result);
        }
    }

    @Test
    public void testExecAndGetStdOut_Success() {
        try (MockedStatic<Runtime> mockedRuntime = Mockito.mockStatic(Runtime.class)) {
            Runtime mockRuntime = Mockito.mock(Runtime.class);
            Process mockProcess = Mockito.mock(Process.class);
            mockedRuntime.when(Runtime::getRuntime).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);
            InputStream mockInputStream = new ByteArrayInputStream("output".getBytes(StandardCharsets.UTF_8));
            when(mockProcess.getInputStream()).thenReturn(mockInputStream);
            when(mockProcess.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
            when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(mockProcess.exitValue()).thenReturn(0);
            String result = Utils.execAndGetStdOut("echo", "test");
            assertEquals("output", result);
        }
    }

    @Test
    public void testExecAndGetStdOut_Failure() {
        try (MockedStatic<Runtime> mockedRuntime = Mockito.mockStatic(Runtime.class)) {
            Runtime mockRuntime = Mockito.mock(Runtime.class);
            Process mockProcess = Mockito.mock(Process.class);
            mockedRuntime.when(Runtime::getRuntime).thenReturn(mockRuntime);
            when(mockRuntime.exec(any(String[].class))).thenReturn(mockProcess);
            when(mockProcess.waitFor(anyLong(), any(TimeUnit.class))).thenReturn(false);
            String result = Utils.execAndGetStdOut("echo", "test");
            assertNull(result);
        }
    }

    @Test
    public void testReadFully() throws IOException {
        String input = "Hello World";
        Reader reader = new StringReader(input);
        String result = Utils.readFully(reader);
        assertEquals(input, result);
    }

    @Test
    public void testTrimAtEnd() {
        assertEquals("Hello World", Utils.trimAtEnd(" Hello World "));
    }
}
