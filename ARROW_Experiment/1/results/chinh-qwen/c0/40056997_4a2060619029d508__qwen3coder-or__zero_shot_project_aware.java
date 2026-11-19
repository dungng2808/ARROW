package example;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Utils_ArrowTest {

    @Test
    public void testResolveEnvVarsWithNullInput() {
        assertThat(Utils.resolveEnvVars(null), is(nullValue()));
    }

    @Test
    public void testResolveEnvVarsWithNoVariables() {
        String input = "Hello World";
        assertThat(Utils.resolveEnvVars(input), is(input));
    }

    @Test
    public void testResolveEnvVarsWithSimpleVariable() {
        String input = "Hello $USER";
        String result = Utils.resolveEnvVars(input);
        // Should not change since USER environment variable might not be set
        assertThat(result, is("Hello "));
    }

    @Test
    public void testResolveEnvVarsWithBracedVariable() {
        String input = "Hello ${USER}";
        String result = Utils.resolveEnvVars(input);
        // Should not change since USER environment variable might not be set
        assertThat(result, is("Hello "));
    }

    @Test
    public void testResolveEnvVarsWithEnvPrefix() {
        String input = "Hello ${env:USER}";
        String result = Utils.resolveEnvVars(input);
        // Should not change since USER environment variable might not be set
        assertThat(result, is("Hello "));
    }

    @Test
    public void testReplaceXPathWithNullInput() {
        assertThat(Utils.replaceXPath(null, "test", false), is(nullValue()));
    }

    @Test
    public void testReplaceXPathWithoutDebug() {
        String input = "//div[@id='test'][text()='old']";
        String result = Utils.replaceXPath(input, "new", false);
        assertThat(result, is("//div[@id='test'][text()='new']"));
    }

    @Test
    public void testReplaceXPathWithDebugAndMatchFound() {
        String input = "//div[@id='test'][text()='old']";
        String result = Utils.replaceXPath(input, "new", true);
        assertThat(result, is("//div[@id='test'][text()='new']"));
    }

    @Test
    public void testReplaceXPathWithDebugAndNoMatch() {
        String input = "//div[@id='test']";
        String result = Utils.replaceXPath(input, "new", true);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testReplaceXPathThreeArgs() {
        String input = "some input";
        String result = Utils.replaceXPath(input, "tag1", "tag2", "name", false);
        // This should delegate to the six-argument version with prefix="xxx"
        // But since there's no match, it should return the original input
        assertThat(result, is(input));
    }

    @Test
    public void testReplaceXPathSixArgsWithNullInput() {
        assertThat(Utils.replaceXPath(null, "prefix", "tag1", "tag2", "name", false), is(nullValue()));
    }

    @Test
    public void testReplaceXPathSixArgsWithoutDebug() {
        String input = "ns1:element[ns2:subelement[text()='old']]";
        String result = Utils.replaceXPath(input, "xxx", "tag1", "tag2", "name", false);
        assertThat(result, is("xxx:tag1[xxx:tag2[text()='name']]"));
    }

    @Test
    public void testReplaceXPathSixArgsWithDebugAndMatchFound() {
        String input = "ns1:element[ns2:subelement[text()='old']]";
        String result = Utils.replaceXPath(input, "xxx", "tag1", "tag2", "name", true);
        assertThat(result, is("xxx:tag1[xxx:tag2[text()='name']]"));
    }

    @Test
    public void testReplaceXPathSixArgsWithDebugAndNoMatch() {
        String input = "ns1:element[ns2:subelement]";
        String result = Utils.replaceXPath(input, "xxx", "tag1", "tag2", "name", true);
        assertThat(result, is(nullValue()));
    }
}
