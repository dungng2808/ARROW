package com.gradle;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Utils_ArrowTest {

    @Test
    public void isNotEmptyReturnsTrueForNonEmptyString() {
        assertTrue(Utils.isNotEmpty("value"));
    }

    @Test
    public void isNotEmptyReturnsFalseForEmptyString() {
        assertFalse(Utils.isNotEmpty(""));
    }

    @Test
    public void isNotEmptyReturnsFalseForNull() {
        assertFalse(Utils.isNotEmpty(null));
    }

    @Test
    public void appendIfMissingAddsSuffix() {
        assertEquals("value/", Utils.appendIfMissing("value", '/'));
    }

    @Test
    public void appendIfMissingPreservesExistingSuffix() {
        assertEquals("value/", Utils.appendIfMissing("value/", '/'));
    }

    @Test
    public void appendIfMissingAddsSuffixToEmptyString() {
        assertEquals("/", Utils.appendIfMissing("", '/'));
    }

    @Test
    public void urlEncodeEncodesUsingUtf8() {
        assertEquals("a+b%2Fc", Utils.urlEncode("a b/c"));
    }

    @Test
    public void redactUserInfoRedactsCredentials() {
        assertEquals("https://******@example.com/path",
            Utils.redactUserInfo("https://user:pass@example.com/path"));
    }

    @Test
    public void redactUserInfoPreservesUrlWithoutCredentials() {
        assertEquals("https://example.com/path",
            Utils.redactUserInfo("https://example.com/path"));
    }

    @Test
    public void redactUserInfoPreservesInvalidUrl() {
        assertEquals("not a valid url", Utils.redactUserInfo("not a valid url"));
    }

    @Test
    public void execAndCheckSuccessReturnsTrueForSuccessfulCommand() {
        assertTrue(Utils.execAndCheckSuccess("sh", "-c", "exit 0"));
    }

    @Test
    public void execAndCheckSuccessReturnsFalseForFailedCommand() {
        assertFalse(Utils.execAndCheckSuccess("sh", "-c", "exit 1"));
    }

    @Test
    public void execAndGetStdOutReturnsTrimmedOutputOnSuccess() {
        assertEquals("hello", Utils.execAndGetStdOut("sh", "-c", "printf '  hello  \\n'"));
    }

    @Test
    public void execAndGetStdOutReturnsNullOnFailure() {
        assertEquals(null, Utils.execAndGetStdOut("sh", "-c", "printf 'hello'; exit 1"));
    }

    @Test
    public void toWebRepoUriParsesHttpsRepository() {
        assertEquals(Optional.of(URI.create("https://github.com/acme-inc/my-project")),
            Utils.toWebRepoUri("https://github.com/acme-inc/my-project.git"));
    }

    @Test
    public void toWebRepoUriParsesGitRepository() {
        assertEquals(Optional.of(URI.create("https://github.com/acme-inc/my-project")),
            Utils.toWebRepoUri("git://github.com/acme-inc/my-project.git"));
    }

    @Test
    public void toWebRepoUriParsesSshRepository() {
        assertEquals(Optional.of(URI.create("https://github.com/acme-inc/my-project")),
            Utils.toWebRepoUri("git@github.com:acme-inc/my-project.git"));
    }

    @Test
    public void toWebRepoUriReturnsEmptyForUnrecognizedRepository() {
        assertEquals(Optional.empty(), Utils.toWebRepoUri("https://example.com/acme-inc/my-project.git"));
    }

    @Test
    public void durationSysPropertyParsesPresentProperty() {
        String name = "utils.arrow.test.duration";
        System.setProperty(name, "PT2S");
        try {
            assertEquals(Optional.of(Duration.ofSeconds(2)), Utils.durationSysProperty(name, null));
        } finally {
            System.clearProperty(name);
        }
    }

    @Test
    public void booleanSysPropertyParsesPresentProperty() {
        String name = "utils.arrow.test.boolean";
        System.setProperty(name, "true");
        try {
            assertEquals(Optional.of(Boolean.TRUE), Utils.booleanSysProperty(name, null));
        } finally {
            System.clearProperty(name);
        }
    }
}
