package com.gradle;

import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Utils_ArrowTest {

    @Test
    public void isNotEmptyHandlesNullAndEmptyValues() {
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
        assertEquals("a+b%2Fc", Utils.urlEncode("a b/c"));
    }

    @Test
    public void redactUserInfoMasksCredentials() {
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
        String url = "https://example.com/a path";
        assertEquals(url, Utils.redactUserInfo(url));
    }

    @Test
    public void toWebRepoUriParsesHttpsRepository() {
        Optional<URI> result = Utils.toWebRepoUri("https://github.com/acme-inc/my-project.git");
        assertEquals(Optional.of(URI.create("https://github.com/acme-inc/my-project")), result);
    }

    @Test
    public void toWebRepoUriParsesGitRepository() {
        Optional<URI> result = Utils.toWebRepoUri("git://gitlab.com/acme/project.git");
        assertEquals(Optional.of(URI.create("https://gitlab.com/acme/project")), result);
    }

    @Test
    public void toWebRepoUriParsesSshRepository() {
        Optional<URI> result = Utils.toWebRepoUri("git@github.com:acme/project.git");
        assertEquals(Optional.of(URI.create("https://github.com/acme/project")), result);
    }

    @Test
    public void toWebRepoUriReturnsEmptyForUnrecognizedRepository() {
        assertEquals(Optional.empty(), Utils.toWebRepoUri("https://example.com/acme/project.git"));
    }

    @Test
    public void durationSysPropertyReturnsEmptyWhenPropertyIsAbsent() {
        assertEquals(Optional.empty(), Utils.durationSysProperty("utils-arrow-test-absent-property", null));
    }

    @Test
    public void booleanSysPropertyReturnsEmptyWhenPropertyIsAbsent() {
        assertEquals(Optional.empty(), Utils.booleanSysProperty("utils-arrow-test-absent-property", null));
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
    public void execAndGetStdOutReturnsNullOnUnsuccessfulCommand() {
        assertNull(Utils.execAndGetStdOut("false"));
    }
}
