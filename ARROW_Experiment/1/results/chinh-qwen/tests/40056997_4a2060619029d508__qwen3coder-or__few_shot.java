package example;

import org.junit.Test;
import static org.junit.Assert.*;

public class Utils_ArrowTest {

    @Test
    public void testResolveEnvVarsWithNullInput() {
        assertNull(Utils.resolveEnvVars(null));
    }

    @Test
    public void testResolveEnvVarsWithNoEnvVars() {
        String input = "hello world";
        assertEquals(input, Utils.resolveEnvVars(input));
    }

    @Test
    public void testResolveEnvVarsWithExistingEnvVar() {
        String input = "hello $USER";
        String result = Utils.resolveEnvVars(input);
        // Should not throw exception, but we don't know the actual USER value
        assertNotNull(result);
    }

    @Test
    public void testResolveEnvVarsWithNonExistingEnvVar() {
        String input = "hello $NONEXISTENTVAR";
        assertEquals("hello ", Utils.resolveEnvVars(input));
    }

    @Test
    public void testReplaceXPathWithNullInput() {
        assertNull(Utils.replaceXPath(null, "test", false));
    }

    @Test
    public void testReplaceXPathWithoutDebug() {
        String input = "some text [text()='old'] more text";
        String result = Utils.replaceXPath(input, "new", false);
        assertEquals("some text [text()='new'] more text", result);
    }

    @Test
    public void testReplaceXPathWithDebugNoMatch() {
        String input = "some text without match";
        assertNull(Utils.replaceXPath(input, "test", true));
    }

    @Test
    public void testReplaceXPathWithDebugMatch() {
        String input = "some text [text()='old'] more text";
        String result = Utils.replaceXPath(input, "new", true);
        assertEquals("some text [text()='new'] more text", result);
    }

    @Test
    public void testReplaceXPathThreeArgs() {
        String input = "some text [text()='old'] more text";
        String result = Utils.replaceXPath(input, "tag1", "tag2", "new", false);
        // Should delegate to the six-arg version with prefix "xxx"
        assertEquals("some text [text()='new'] more text", result);
    }

    @Test
    public void testReplaceXPathSixArgsWithNullInput() {
        assertNull(Utils.replaceXPath(null, "prefix", "tag1", "tag2", "name", false));
    }

    @Test
    public void testReplaceXPathSixArgsWithoutDebug() {
        String input = "some text tag1:tag2[tag1:tag2[text()='old']] more text";
        String result = Utils.replaceXPath(input, "prefix", "tag1", "tag2", "new", false);
        assertEquals("some text xxx:tag1[xxx:tag2[text()='new']] more text", result);
    }

    @Test
    public void testReplaceXPathSixArgsWithDebugNoMatch() {
        String input = "some text without match";
        assertNull(Utils.replaceXPath(input, "prefix", "tag1", "tag2", "name", true));
    }

    @Test
    public void testReplaceXPathSixArgsWithDebugMatch() {
        String input = "some text tag1:tag2[tag1:tag2[text()='old']] more text";
        String result = Utils.replaceXPath(input, "prefix", "tag1", "tag2", "new", true);
        assertEquals("some text xxx:tag1[xxx:tag2[text()='new']] more text", result);
    }
}
