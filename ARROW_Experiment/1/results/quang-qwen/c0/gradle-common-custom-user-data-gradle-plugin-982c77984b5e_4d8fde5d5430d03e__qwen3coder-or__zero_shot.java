package com.gradle;

import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

import org.junit.Test;
import org.mockito.Mockito;

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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Utils_ArrowTest {

    @Test
    public void testSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Mockito.when(providers.systemProperty("test.property").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));
        Mockito.when(providers.environmentVariable("test.env").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSysPropertyOrEnvVariable_EnvVariablePresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Mockito.when(providers.systemProperty("test.property").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));
        Mockito.when(providers.environmentVariable("test.env").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertFalse(result.isPresent());
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Mockito.when(providers.systemProperty("test.property").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));
        Mockito.when(providers.environmentVariable("test.env").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertFalse(result.isPresent());
    }

    @Test
    public void testBooleanSysPropertyOrEnvVariable_EnvVariablePresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Mockito.when(providers.systemProperty("test.property").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));
        Mockito.when(providers.environmentVariable("test.env").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertFalse(result.isPresent());
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_SystemPropertyPresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Mockito.when(providers.systemProperty("test.property").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));
        Mockito.when(providers.environmentVariable("test.env").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "test.env", providers);
        assertFalse(result.isPresent());
    }

    @Test
    public void testDurationSysPropertyOrEnvVariable_EnvVariablePresent() {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Mockito.when(providers.systemProperty("test.property").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));
        Mockito.when(providers.environmentVariable("test.env").forUseAtConfigurationTime()).thenReturn(Mockito.mock(Provider.class));

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "test.env", providers);
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
    public void testUrlEncode_NormalString() {
        assertEquals("hello%20world", Utils.urlEncode("hello world"));
    }

    @Test
    public void testRedactUserInfo_NormalURL() throws URISyntaxException {
        assertEquals("https://example.com/path", Utils.redactUserInfo("https://user:pass@example.com/path"));
    }

    @Test
    public void testRedactUserInfo_URLWithoutUserInfo() {
        assertEquals("https://example.com/path", Utils.redactUserInfo("https://example.com/path"));
    }

    @Test
    public void testRedactUserInfo_InvalidURL() {
        assertEquals("invalid-url", Utils.redactUserInfo("invalid-url"));
    }

    @Test
    public void testToWebRepoUri_ValidGitURL() {
        Optional<URI> result = Utils.toWebRepoUri("git://github.com/acme-inc/my-project.git");
        assertTrue(result.isPresent());
        assertEquals("https://github.com/acme-inc/my-project", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_InvalidGitURL() {
        Optional<URI> result = Utils.toWebRepoUri("invalid-url");
        assertFalse(result.isPresent());
    }

    @Test
    public void testIsGradle4OrNewer() {
        assertTrue(Utils.isGradle4OrNewer());
    }

    @Test
    public void testIsGradle43rNewer() {
        assertTrue(Utils.isGradle43rNewer());
    }

    @Test
    public void testIsGradle5OrNewer() {
        assertTrue(Utils.isGradle5OrNewer());
    }

    @Test
    public void testIsGradle6OrNewer() {
        assertTrue(Utils.isGradle6OrNewer());
    }

    @Test
    public void testIsGradle61OrNewer() {
        assertTrue(Utils.isGradle61OrNewer());
    }

    @Test
    public void testIsGradle62OrNewer() {
        assertTrue(Utils.isGradle62OrNewer());
    }

    @Test
    public void testIsGradle65OrNewer() {
        assertTrue(Utils.isGradle65OrNewer());
    }

    @Test
    public void testIsGradle74OrNewer() {
        assertTrue(Utils.isGradle74OrNewer());
    }

    @Test
    public void testReadPropertiesFile_Success() throws IOException {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Directory projectDirectory = Mockito.mock(Directory.class);
        RegularFile regularFile = Mockito.mock(RegularFile.class);
        Provider<byte[]> fileContent = Mockito.mock(Provider.class);

        Mockito.when(projectDirectory.file("test.properties")).thenReturn(regularFile);
        Mockito.when(providers.fileContents(regularFile).getAsBytes()).thenReturn(fileContent);
        Mockito.when(fileContent.getOrElse(new byte[0])).thenReturn("key=value".getBytes());

        Properties result = Utils.readPropertiesFile("test.properties", providers, projectDirectory);
        assertNotNull(result);
        assertEquals("value", result.getProperty("key"));
    }

    @Test
    public void testReadFile_Gradle65OrNewer() throws IOException {
        ProviderFactory providers = Mockito.mock(ProviderFactory.class);
        Directory projectDirectory = Mockito.mock(Directory.class);
        RegularFile regularFile = Mockito.mock(RegularFile.class);
        Provider<byte[]> fileContent = Mockito.mock(Provider.class);

        Mockito.when(projectDirectory.file("test.txt")).thenReturn(regularFile);
        Mockito.when(providers.fileContents(regularFile).getAsBytes()).thenReturn(fileContent);
        Mockito.when(fileContent.getOrElse(new byte[0])).thenReturn("test content".getBytes());

        InputStream result = Utils.readFile("test.txt", providers, projectDirectory);
        assertNotNull(result);
        assertTrue(result instanceof ByteArrayInputStream);
    }

    @Test
    public void testExecAndCheckSuccess_Success() {
        boolean result = Utils.execAndCheckSuccess("echo", "test");
        assertTrue(result);
    }

    @Test
    public void testExecAndCheckSuccess_Failure() {
        boolean result = Utils.execAndCheckSuccess("false");
        assertFalse(result);
    }

    @Test
    public void testExecAndGetStdOut_Success() {
        String result = Utils.execAndGetStdOut("echo", "test");
        assertNotNull(result);
        assertEquals("test", result.trim());
    }

    @Test
    public void testExecAndGetStdOut_Failure() {
        String result = Utils.execAndGetStdOut("false");
        assertNull(result);
    }
}
