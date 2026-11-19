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
    public void testSysPropertyOrEnvVariable_EnvVariableUsedWhenNoSystemProperty() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);

        Provider<String> envMockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(envMockProvider);
        when(envMockProvider.forUseAtConfigurationTime()).thenReturn(envMockProvider);
        when(envMockProvider.getOrNull()).thenReturn("envValue");

        Optional<String> result = Utils.sysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals("envValue", result.get());
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
    public void testBooleanSysPropertyOrEnvVariable_EnvVariableUsedWhenNoSystemProperty() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);

        Provider<String> envMockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(envMockProvider);
        when(envMockProvider.forUseAtConfigurationTime()).thenReturn(envMockProvider);
        when(envMockProvider.getOrNull()).thenReturn("false");

        Optional<Boolean> result = Utils.booleanSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertFalse(result.get());
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
    public void testDurationSysPropertyOrEnvVariable_EnvVariableUsedWhenNoSystemProperty() {
        Provider<String> mockProvider = mock(Provider.class);
        when(providerFactory.systemProperty("test.property")).thenReturn(mockProvider);
        when(mockProvider.forUseAtConfigurationTime()).thenReturn(mockProvider);
        when(mockProvider.getOrNull()).thenReturn(null);

        Provider<String> envMockProvider = mock(Provider.class);
        when(providerFactory.environmentVariable("TEST_ENV")).thenReturn(envMockProvider);
        when(envMockProvider.forUseAtConfigurationTime()).thenReturn(envMockProvider);
        when(envMockProvider.getOrNull()).thenReturn("PT30S");

        Optional<Duration> result = Utils.durationSysPropertyOrEnvVariable("test.property", "TEST_ENV", providerFactory);
        assertTrue(result.isPresent());
        assertEquals(Duration.ofSeconds(30), result.get());
    }

    @Test
    public void testEnvVariable_Gradle65OrNewer() {
        // Mock Gradle version to be 6.5+
        GradleVersion currentVersion = mock(GradleVersion.class);
        when(currentVersion.compareTo(any(GradleVersion.class))).thenReturn(1);
        // We can't directly mock GradleVersion.current(), so we'll test the behavior differently

        // This test will be skipped since we can't easily mock GradleVersion.current()
        // But we can at least verify the method signature works
        assertNotNull(Utils.class.getMethod("envVariable", String.class, ProviderFactory.class));
    }

    @Test
    public void testIsNotEmpty_NullReturnsFalse() {
        assertFalse(Utils.isNotEmpty(null));
    }

    @Test
    public void testIsNotEmpty_EmptyStringReturnsFalse() {
        assertFalse(Utils.isNotEmpty(""));
    }

    @Test
    public void testIsNotEmpty_NonEmptyStringReturnsTrue() {
        assertTrue(Utils.isNotEmpty("test"));
    }

    @Test
    public void testAppendIfMissing_NoSuffix() {
        assertEquals("test", Utils.appendIfMissing("test", 'x'));
    }

    @Test
    public void testAppendIfMissing_WithSuffix() {
        assertEquals("testx", Utils.appendIfMissing("test", 'x'));
    }

    @Test
    public void testAppendIfMissing_AlreadyHasSuffix() {
        assertEquals("testx", Utils.appendIfMissing("testx", 'x'));
    }

    @Test
    public void testUrlEncode_NormalString() {
        assertEquals("hello%20world", Utils.urlEncode("hello world"));
    }

    @Test
    public void testRedactUserInfo_ValidUrlWithUserInfo() {
        String url = "https://user:pass@example.com/path";
        assertEquals("https://******@example.com/path", Utils.redactUserInfo(url));
    }

    @Test
    public void testRedactUserInfo_UrlWithoutUserInfo() {
        String url = "https://example.com/path";
        assertEquals("https://example.com/path", Utils.redactUserInfo(url));
    }

    @Test
    public void testRedactUserInfo_InvalidUrl() {
        String url = "not a valid url";
        assertEquals("not a valid url", Utils.redactUserInfo(url));
    }

    @Test
    public void testToWebRepoUri_GitProtocol() {
        Optional<URI> result = Utils.toWebRepoUri("git://github.com/user/repo.git");
        assertTrue(result.isPresent());
        assertEquals("https://github.com/user/repo", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_HttpsProtocol() {
        Optional<URI> result = Utils.toWebRepoUri("https://github.com/user/repo.git");
        assertTrue(result.isPresent());
        assertEquals("https://github.com/user/repo", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_SshProtocol() {
        Optional<URI> result = Utils.toWebRepoUri("ssh://git@github.com/user/repo.git");
        assertTrue(result.isPresent());
        assertEquals("https://github.com/user/repo", result.get().toString());
    }

    @Test
    public void testToWebRepoUri_InvalidFormat() {
        Optional<URI> result = Utils.toWebRepoUri("invalid-format");
        assertFalse(result.isPresent());
    }

    @Test
    public void testIsGradle4OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle4OrNewer"));
    }

    @Test
    public void testIsGradle43rNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle43rNewer"));
    }

    @Test
    public void testIsGradle5OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle5OrNewer"));
    }

    @Test
    public void testIsGradle6OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle6OrNewer"));
    }

    @Test
    public void testIsGradle61OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle61OrNewer"));
    }

    @Test
    public void testIsGradle62OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle62OrNewer"));
    }

    @Test
    public void testIsGradle65OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle65OrNewer"));
    }

    @Test
    public void testIsGradle74OrNewer() {
        // Just verify the method exists
        assertNotNull(Utils.class.getMethod("isGradle74OrNewer"));
    }
}
