package example;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class Utils_ArrowTest {

    @Test
    public void testResolveEnvVarsNullInput() {
        assertNull(Utils.resolveEnvVars(null));
    }

    @Test
    public void testResolveEnvVarsNoEnvVars() {
        String input = "hello world";
        assertEquals(input, Utils.resolveEnvVars(input));
    }

    @Test
    public void testResolveEnvVarsWithEnvVar() {
        // Set up environment variable for testing
        System.setProperty("TEST_VAR", "test_value");
        String input = "hello $TEST_VAR world";
        String expected = "hello test_value world";
        assertEquals(expected, Utils.resolveEnvVars(input));
    }

    @Test
    public void testResolveEnvVarsWithEnvVarInBraces() {
        System.setProperty("TEST_VAR2", "test_value2");
        String input = "hello ${TEST_VAR2} world";
        String expected = "hello test_value2 world";
        assertEquals(expected, Utils.resolveEnvVars(input));
    }

    @Test
    public void testResolveEnvVarsWithEnvVarInBracesWithEnvPrefix() {
        System.setProperty("TEST_VAR3", "test_value3");
        String input = "hello ${env:TEST_VAR3} world";
        String expected = "hello test_value3 world";
        assertEquals(expected, Utils.resolveEnvVars(input));
    }

    @Test
    public void testReplaceXPathNullInput() {
        assertNull(Utils.replaceXPath(null, "name", false));
    }

    @Test
    public void testReplaceXPathWithoutDebugMatch() {
        String input = "some text [text()='old'] more text";
        String expected = "some text [text()='name'] more text";
        assertEquals(expected, Utils.replaceXPath(input, "name", false));
    }

    @Test
    public void testReplaceXPathWithDebugNoMatch() {
        String input = "some text without match";
        assertNull(Utils.replaceXPath(input, "name", true));
    }

    @Test
    public void testReplaceXPathWithDebugMatch() {
        String input = "some text [text()='old'] more text";
        String expected = "some text [text()='name'] more text";
        assertEquals(expected, Utils.replaceXPath(input, "name", true));
    }

    @Test
    public void testReplaceXPathThreeArgs() {
        String input = "some text [text()='old'] more text";
        String expected = "some text [text()='name'] more text";
        assertEquals(expected, Utils.replaceXPath(input, "tag1", "tag2", "name", false));
    }

    @Test
    public void testReplaceXPathFiveArgsNullInput() {
        assertNull(Utils.replaceXPath(null, "prefix", "tag1", "tag2", "name", false));
    }

    @Test
    public void testReplaceXPathFiveArgsWithoutDebugMatch() {
        String input = "some text word[word[text()='old']] more text";
        String expected = "xxx:tag1[xxx:tag2[text()='name']] more text";
        assertEquals(expected, Utils.replaceXPath(input, "prefix", "tag1", "tag2", "name", false));
    }

    @Test
    public void testReplaceXPathFiveArgsWithDebugNoMatch() {
        String input = "some text without match";
        assertNull(Utils.replaceXPath(input, "prefix", "tag1", "tag2", "name", true));
    }

    @Test
    public void testReplaceXPathFiveArgsWithDebugMatch() {
        String input = "some text word[word[text()='old']] more text";
        String expected = "xxx:tag1[xxx:tag2[text()='name']] more text";
        assertEquals(expected, Utils.replaceXPath(input, "prefix", "tag1", "tag2", "name", true));
    }
}
