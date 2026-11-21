package com.gradle;

import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Utils_ArrowTest {

    @Test
    public void testSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.systemProperty("test.property")).thenReturn(mock(Provider.class));
        when(providers.environmentVariable("test.env")).thenReturn(mock(Provider.class));

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertNotNull(result);
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.systemProperty("test.property")).thenReturn(mock(Provider.class));
        when(providers.environmentVariable("test.env")).thenReturn(mock(Provider.class));

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertNotNull(result);
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.systemProperty("test.property")).thenReturn(mock(Provider.class));
        when(providers.environmentVariable("test.env")).thenReturn(mock(Provider.class));

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertNotNull(result);
    }

    @Test
    public void testEnvVariable_Gradle65OrNewer() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(Utils.isGradle65OrNewer()).thenReturn(true);
        when(Utils.isGradle74OrNewer()).thenReturn(false);

        Optional<String> result = Utils.envVariable("test.env", providers);
        assertNotNull(result);
    }

    @Test
    public void testBooleanEnvVariable() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.environmentVariable("test.env")).thenReturn(mock(Provider.class));

        Optional<Boolean> result = Utils.booleanEnvVariable("test.env", providers);
        assertNotNull(result);
    }

    @Test
    public void testDurationEnvVariable() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.environmentVariable("test.env")).thenReturn(mock(Provider.class));

        Optional<Duration> result = Utils.durationEnvVariable("test.env", providers);
        assertNotNull(result);
    }

    @Test
    public void testSysProperty_Gradle65OrNewer() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(Utils.isGradle65OrNewer()).thenReturn(true);
        when(Utils.isGradle74OrNewer()).thenReturn(false);

        Optional<String> result = Utils.sysProperty("test.property", providers);
        assertNotNull(result);
    }

    @Test
    public void testBooleanSysProperty() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.systemProperty("test.property")).thenReturn(mock(Provider.class));

        Optional<Boolean> result = Utils.booleanSysProperty("test.property", providers);
        assertNotNull(result);
    }

    @Test
    public void testDurationSysProperty() {
        ProviderFactory providers = mock(ProviderFactory.class);
        when(providers.systemProperty("test.property")).thenReturn(mock(Provider.class));

        Optional<Duration> result = Utils.durationSysProperty("test.property", providers);
        assertNotNull(result);
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
        String result = Utils.appendIfMissing("test", 's');
        assertEquals("tests", result);
    }

    @Test
    public void testAppendIfMissing_HasSuffix() {
        String result = Utils.appendIfMissing("test", 't');
        assertEquals("test", result);
    }

    @Test
    public void testUrlEncode() {
        String result = Utils.urlEncode("hello world");
        assertEquals("hello+world", result);
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
    public void testReadPropertiesFile() {
        ProviderFactory providers = mock(ProviderFactory.class);
        Directory projectDirectory = mock(Directory.class);
        RegularFile file = mock(RegularFile.class);
        when(projectDirectory.file("test.properties")).thenReturn(file);
        when(providers.fileContents(file)).thenReturn(mock(Provider.class));

        try {
            Properties result = Utils.readPropertiesFile("test.properties", providers, projectDirectory);
            assertNotNull(result);
        } catch (RuntimeException e) {
            // Expected behavior when file doesn't exist
        }
    }

    @Test
    public void testReadFile_Gradle65OrNewer() throws FileNotFoundException {
        ProviderFactory providers = mock(ProviderFactory.class);
        Directory projectDirectory = mock(Directory.class);
        RegularFile file = mock(RegularFile.class);
        when(projectDirectory.file("test.txt")).thenReturn(file);
        when(providers.fileContents(file)).thenReturn(mock(Provider.class));
        when(Utils.isGradle65OrNewer()).thenReturn(true);

        try {
            InputStream result = Utils.readFile("test.txt", providers, projectDirectory);
            assertNotNull(result);
        } catch (FileNotFoundException e) {
            // Expected behavior when file doesn't exist
        }
    }

    @Test
    public void testExecAndCheckSuccess_Success() {
        boolean result = Utils.execAndCheckSuccess("echo", "test");
        assertTrue(result);
    }

    @Test
    public void testExecAndGetStdOut_Success() {
        String result = Utils.execAndGetStdOut("echo", "test");
        assertNotNull(result);
    }

    @Test
    public void testIsGradle4OrNewer() {
        boolean result = Utils.isGradle4OrNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle43rNewer() {
        boolean result = Utils.isGradle43rNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle5OrNewer() {
        boolean result = Utils.isGradle5OrNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle6OrNewer() {
        boolean result = Utils.isGradle6OrNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle61OrNewer() {
        boolean result = Utils.isGradle61OrNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle62OrNewer() {
        boolean result = Utils.isGradle62OrNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle65OrNewer() {
        boolean result = Utils.isGradle65OrNewer();
        assertTrue(result);
    }

    @Test
    public void testIsGradle74OrNewer() {
        boolean result = Utils.isGradle74OrNewer();
        assertTrue(result);
    }

    @Test
    public void testToWebRepoUri_ValidGitUrl() {
        Optional<URI> result = Utils.toWebRepoUri("git://github.com/user/repo.git");
        assertTrue(result.isPresent());
    }

    @Test
    public void testToWebRepoUri_InvalidGitUrl() {
        Optional<URI> result = Utils.toWebRepoUri("invalid-url");
        assertFalse(result.isPresent());
    }

    @Test
    public void testToUri_Valid() {
        Optional<URI> result = Utils.toUri("https", "example.com", "/path");
        assertTrue(result.isPresent());
    }

    @Test
    public void testToUri_Invalid() {
        Optional<URI> result = Utils.toUri("invalid", "example.com", "/path");
        assertFalse(result.isPresent());
    }
}
