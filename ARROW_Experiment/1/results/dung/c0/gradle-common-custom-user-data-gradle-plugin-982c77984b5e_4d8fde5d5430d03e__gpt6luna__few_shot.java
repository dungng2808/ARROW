package com.gradle;

import org.gradle.api.provider.ProviderFactory;
import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Utils_ArrowTest {

    @Test
    public void isNotEmptyHandlesNullEmptyAndNonEmptyValues() {
        assertFalse(Utils.isNotEmpty(null));
        assertFalse(Utils.isNotEmpty(""));
        assertTrue(Utils.isNotEmpty("value"));
    }

    @Test
    public void appendIfMissingAddsSuffixOnlyWhenNeeded() {
        assertEquals("path/", Utils.appendIfMissing("path", '/'));
        assertEquals("path/", Utils.appendIfMissing("path/", '/'));
        assertEquals("/", Utils.appendIfMissing("", '/'));
    }

    @Test
    public void urlEncodeUsesUtf8FormEncoding() {
        assertEquals("a+b%26c", Utils.urlEncode("a b&c"));
    }

    @Test
    public void redactUserInfoRedactsCredentials() {
        assertEquals("https://******@example.com/path",
            Utils.redactUserInfo("https://user:pass@example.com/path"));
    }

    @Test
    public void redactUserInfoLeavesUrlWithoutCredentialsUnchanged() {
        String url = "https://example.com/path";
        assertEquals(url, Utils.redactUserInfo(url));
    }

    @Test
    public void redactUserInfoLeavesInvalidUrlUnchanged() {
        String url = "not a valid url";
        assertEquals(url, Utils.redactUserInfo(url));
    }

    @Test
    public void toWebRepoUriParsesHttpsGithubUrl() {
        Optional<URI> result = Utils.toWebRepoUri("https://github.com/acme-inc/my-project.git");
        assertTrue(result.isPresent());
        assertEquals(URI.create("https://github.com/acme-inc/my-project"), result.get());
    }

    @Test
    public void toWebRepoUriParsesSshGitlabUrl() {
        Optional<URI> result = Utils.toWebRepoUri("git@gitlab.com:acme/project.git");
        assertTrue(result.isPresent());
        assertEquals(URI.create("https://gitlab.com/acme/project"), result.get());
    }

    @Test
    public void toWebRepoUriReturnsEmptyForUnrecognizedUrl() {
        assertEquals(Optional.empty(), Utils.toWebRepoUri("https://example.com/acme/project.git"));
    }

    @Test
    public void sysPropertyReturnsEmptyForMissingProperty() {
        Optional<String> result = Utils.sysProperty("utils.arrow.missing.property", null);
        assertFalse(result.isPresent());
    }

    @Test
    public void booleanSysPropertyReturnsEmptyForMissingProperty() {
        Optional<Boolean> result = Utils.booleanSysProperty("utils.arrow.missing.boolean", null);
        assertFalse(result.isPresent());
    }

    @Test
    public void durationSysPropertyReturnsEmptyForMissingProperty() {
        Optional<Duration> result = Utils.durationSysProperty("utils.arrow.missing.duration", null);
        assertFalse(result.isPresent());
    }

    @Test
    public void sysPropertyOrEnvVariableReturnsEmptyWhenBothAreMissing() {
        Optional<String> result =
            Utils.sysPropertyOrEnvVariable("utils.arrow.missing.combined", "UTILS_ARROW_MISSING_COMBINED", null);
        assertFalse(result.isPresent());
    }

    @Test
    public void booleanSysPropertyOrEnvVariableReturnsEmptyWhenBothAreMissing() {
        Optional<Boolean> result =
            Utils.booleanSysPropertyOrEnvVariable("utils.arrow.missing.boolean.combined",
                "UTILS_ARROW_MISSING_BOOLEAN_COMBINED", null);
        assertFalse(result.isPresent());
    }

    @Test
    public void durationSysPropertyOrEnvVariableReturnsEmptyWhenBothAreMissing() {
        Optional<Duration> result =
            Utils.durationSysPropertyOrEnvVariable("utils.arrow.missing.duration.combined",
                "UTILS_ARROW_MISSING_DURATION_COMBINED", null);
        assertFalse(result.isPresent());
    }

    @Test
    public void execAndCheckSuccessReturnsTrueForSuccessfulCommand() {
        assertTrue(Utils.execAndCheckSuccess("true"));
    }

    @Test
    public void execAndCheckSuccessReturnsFalseForUnsuccessfulCommand() {
        assertFalse(Utils.execAndCheckSuccess("false"));
    }

    @Test
    public void execAndGetStdOutReturnsTrimmedOutputOnSuccess() {
        assertEquals("hello", Utils.execAndGetStdOut("printf", "  hello  \n"));
    }

    @Test
    public void execAndGetStdOutReturnsNullOnFailure() {
        assertNull(Utils.execAndGetStdOut("false"));
    }

    @Test
    public void gradleVersionChecksAreConsistent() {
        assertTrue(Utils.isGradle4OrNewer());
        assertTrue(Utils.isGradle43rNewer());
        assertTrue(Utils.isGradle5OrNewer());
        assertTrue(Utils.isGradle6OrNewer());
        assertTrue(Utils.isGradle61OrNewer());
        assertTrue(Utils.isGradle62OrNewer());
        assertTrue(Utils.isGradle65OrNewer());
        assertTrue(Utils.isGradle74OrNewer());
    }
}
