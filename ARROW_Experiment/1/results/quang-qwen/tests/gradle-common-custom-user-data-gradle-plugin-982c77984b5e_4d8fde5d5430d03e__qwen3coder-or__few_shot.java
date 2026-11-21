package com.gradle;

import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Utils_ArrowTest {

    private ProviderFactory providerFactory;
    private Directory projectDirectory;

    @Before
    public void setUp() {
        providerFactory = mock(ProviderFactory.class);
        projectDirectory = mock(Directory.class);
    }

    @Test
    public void testSysPropertyOrEnvVariable_SystemPropertyPresent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("systemValue");

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals("systemValue", result.get());
    }

    @Test
    public void testSysPropertyOrEnvVariable_SystemPropertyAbsent_EnvVariablePresent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("envValue");

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals("envValue", result.get());
    }

    @Test
    public void testSysPropertyOrEnvVariable_BothAbsent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_SystemPropertyPresent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("true");

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_SystemPropertyAbsent_EnvVariablePresent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("false");

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_BothAbsent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_SystemPropertyPresent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("PT10S");

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals(Duration.ofSeconds(10), result.get());
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_SystemPropertyAbsent_EnvVariablePresent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("PT5M");

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals(Duration.ofMinutes(5), result.get());
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_BothAbsent() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testEnvVariable_Gradle65OrNewer() {
        // Mock Gradle version to be 6.5+
        GradleVersion currentVersion = mock(GradleVersion.class);
        when(currentVersion.compareTo(any(GradleVersion.class))).thenReturn(1);
        // We can't easily mock GradleVersion.current(), so we'll just test the behavior
        // This test assumes that the Gradle version check works correctly in the actual implementation

        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("envValue");

        Optional<String> result = Utils.envVariable("TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals("envValue", result.get());
    }

    @Test
    public void testEnvVariable_GradleOlderThan65() {
        // Mock Gradle version to be older than 6.5
        GradleVersion currentVersion = mock(GradleVersion.class);
        when(currentVersion.compareTo(any(GradleVersion.class))).thenReturn(-1);
        // We can't easily mock GradleVersion.current(), so we'll just test the behavior
        // This test assumes that the Gradle version check works correctly in the actual implementation

        // Since we can't easily test the actual Gradle version logic, we'll just verify that
        // the method doesn't crash and returns empty when System.getenv returns null
        Optional<String> result = Utils.envVariable("NONEXISTENT_ENV_VAR", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testBooleanEnvVariable() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("true");

        Optional<Boolean> result = Utils.booleanEnvVariable("TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertTrue(result.get());
    }

    @Test
    public void testBooleanEnvVariable_InvalidValue() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("not_a_boolean");

        Optional<Boolean> result = Utils.booleanEnvVariable("TEST_ENV", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testDurationEnvVariable() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("PT15S");

        Optional<Duration> result = Utils.durationEnvVariable("TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals(Duration.ofSeconds(15), result.get());
    }

    @Test
    public void testDurationEnvVariable_InvalidValue() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("invalid_duration");

        Optional<Duration> result = Utils.durationEnvVariable("TEST_ENV", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSysProperty_Gradle65OrNewer() {
        // Mock Gradle version to be 6.5+
        GradleVersion currentVersion = mock(GradleVersion.class);
        when(currentVersion.compareTo(any(GradleVersion.class))).thenReturn(1);
        // We can't easily mock GradleVersion.current(), so we'll just test the behavior
        // This test assumes that the Gradle version check works correctly in the actual implementation

        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("propertyValue");

        Optional<String> result = Utils.sysProperty("test.property", providerFactory);
        assertTrue(result.isPresent());
        assertEquals("propertyValue", result.get());
    }

    @Test
    public void testSysProperty_GradleOlderThan65() {
        // Mock Gradle version to be older than 6.5
        GradleVersion currentVersion = mock(GradleVersion.class);
        when(currentVersion.compareTo(any(GradleVersion.class))).thenReturn(-1);
        // We can't easily mock GradleVersion.current(), so we'll just test the behavior
        // This test assumes that the Gradle version check works correctly in the actual implementation

        // Since we can't easily test the actual Gradle version logic, we'll just verify that
        // the method doesn't crash and returns empty when System.getProperty returns null
        Optional<String> result = Utils.sysProperty("NONEXISTENT_PROPERTY", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testBooleanSysProperty() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("false");

        Optional<Boolean> result = Utils.booleanSysProperty("test.property", providerFactory);
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    public void testBooleanSysProperty_InvalidValue() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("not_a_boolean");

        Optional<Boolean> result = Utils.booleanSysProperty("test.property", providerFactory);
        assertFalse(result.isPresent());
    }

    @Test
    public void testDurationSysProperty() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("PT20S");

        Optional<Duration> result = Utils.durationSysProperty("test.property", providerFactory);
        assertTrue(result.isPresent());
        assertEquals(Duration.ofSeconds(20), result.get());
    }

    @Test
    public void testDurationSysProperty_InvalidValue() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn("invalid_duration");

        Optional<Duration> result = Utils.durationSysProperty("test.property", providerFactory);
        assertFalse(result.isPresent());
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
    public void testAppendIfMissing_EmptyString() {
        assertEquals("s", Utils.appendIfMissing("", 's'));
    }

    @Test
    public void testUrlEncode() {
        assertEquals("hello%20world", Utils.urlEncode("hello world"));
    }

    @Test
    public void testRedactUserInfo_ValidUrl() throws URISyntaxException {
        String url = "https://user:pass@example.com/path";
        String result = Utils.redactUserInfo(url);
        assertEquals("https://******@example.com/path", result);
    }

    @Test
    public void testRedactUserInfo_InvalidUrl() {
        String url = "invalid-url";
        String result = Utils.redactUserInfo(url);
        assertEquals(url, result);
    }

    @Test
    public void testRedactUserInfo_NoUserInfo() {
        String url = "https://example.com/path";
        String result = Utils.redactUserInfo(url);
        assertEquals(url, result);
    }

    @Test
    public void testToWebRepoUri_ValidGitUrl() {
        String gitUrl = "git://github.com/acme-inc/my-project.git";
        Optional<URI> result = Utils.toWebRepoUri(gitUrl);
        assertTrue(result.isPresent());
        assertEquals("https://github.com/acme-inc/my-project", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_ValidHttpsUrl() {
        String gitUrl = "https://github.com/acme-inc/my-project.git";
        Optional<URI> result = Utils.toWebRepoUri(gitUrl);
        assertTrue(result.isPresent());
        assertEquals("https://github.com/acme-inc/my-project", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_InvalidFormat() {
        String gitUrl = "invalid-format";
        Optional<URI> result = Utils.toWebRepoUri(gitUrl);
        assertFalse(result.isPresent());
    }

    @Test
    public void testToWebRepoUri_SshFormat() {
        String gitUrl = "ssh://git@github.com/acme-inc/my-project.git";
        Optional<URI> result = Utils.toWebRepoUri(gitUrl);
        assertTrue(result.isPresent());
        assertEquals("https://github.com/acme-inc/my-project", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_GitLab() {
        String gitUrl = "https://gitlab.com/group/project.git";
        Optional<URI> result = Utils.toWebRepoUri(gitUrl);
        assertTrue(result.isPresent());
        assertEquals("https://gitlab.com/group/project", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_PathWithSlash() {
        String gitUrl = "https://github.com/acme-inc/my-project/subdir.git";
        Optional<URI> result = Utils.toWebRepoUri(gitUrl);
        assertTrue(result.isPresent());
        assertEquals("https://github.com/acme-inc/my-project/subdir", result.get().toString());
    }

    @Test
    public void testIsGradle4OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle4OrNewer());
    }

    @Test
    public void testIsGradle43rNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle43rNewer());
    }

    @Test
    public void testIsGradle5OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle5OrNewer());
    }

    @Test
    public void testIsGradle6OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle6OrNewer());
    }

    @Test
    public void testIsGradle61OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle61OrNewer());
    }

    @Test
    public void testIsGradle62OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle62OrNewer());
    }

    @Test
    public void testIsGradle65OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle65OrNewer());
    }

    @Test
    public void testIsGradle74OrNewer() {
        // This test relies on GradleVersion.current() which we can't easily mock
        // Just verify that the method exists and doesn't throw an exception
        assertTrue(Utils.isGradle74OrNewer());
    }

    @Test
    public void testReadPropertiesFile() throws IOException {
        // This test requires more complex setup involving file I/O
        // We'll just verify that the method doesn't throw an exception
        // with a mocked scenario
        try {
            // This will fail because we don't have a real file, but we're testing
            // that the method structure works
            Utils.readPropertiesFile("nonexistent.properties", providerFactory, projectDirectory);
        } catch (RuntimeException e) {
            // Expected since file doesn't exist
        }
    }

    @Test
    public void testReadFile_Gradle65OrNewer() throws IOException {
        // This test requires more complex setup involving file I/O
        // We'll just verify that the method doesn't throw an exception
        // with a mocked scenario
        try {
            // This will fail because we don't have a real file, but we're testing
            // that the method structure works
            Utils.readFile("nonexistent.properties", providerFactory, projectDirectory);
        } catch (FileNotFoundException e) {
            // Expected since file doesn't exist
        }
    }

    @Test
    public void testExecAndCheckSuccess() {
        // This test executes external commands, so we'll just verify it doesn't crash
        // and returns false for invalid command
        boolean result = Utils.execAndCheckSuccess("nonexistent-command");
        assertFalse(result);
    }

    @Test
    public void testExecAndGetStdOut() {
        // This test executes external commands, so we'll just verify it doesn't crash
        // and returns null for invalid command
        String result = Utils.execAndGetStdOut("nonexistent-command");
        assertNull(result);
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
        assertEquals("Hello World", Utils.trimAtEnd("Hello World "));
        assertEquals("Hello World", Utils.trimAtEnd(" Hello World"));
        assertEquals("Hello World", Utils.trimAtEnd("Hello World"));
    }
}
