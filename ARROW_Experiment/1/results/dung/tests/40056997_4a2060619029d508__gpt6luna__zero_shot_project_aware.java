package example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Utils_ArrowTest {

    @Test
    public void resolveEnvVarsReturnsNullForNullInput() {
        assertNull(Utils.resolveEnvVars(null));
    }

    @Test
    public void resolveEnvVarsLeavesTextWithoutVariablesUnchanged() {
        assertEquals("plain text", Utils.resolveEnvVars("plain text"));
    }

    @Test
    public void resolveEnvVarsRemovesUnknownVariables() {
        assertEquals("before-after", Utils.resolveEnvVars("before${UTILS_ARROW_UNKNOWN_VARIABLE}-after"));
    }

    @Test
    public void replaceXPathReturnsNullForNullInput() {
        assertNull(Utils.replaceXPath(null, "replacement", false));
    }

    @Test
    public void replaceXPathReplacesMatchingTextPredicate() {
        assertEquals("item[text()='replacement']",
                Utils.replaceXPath("item[text()='original']", "replacement", false));
    }

    @Test
    public void replaceXPathLeavesNonmatchingInputUnchangedWhenDebugIsFalse() {
        assertEquals("item[@id='original']",
                Utils.replaceXPath("item[@id='original']", "replacement", false));
    }

    @Test
    public void replaceXPathReturnsNullForNonmatchingInputWhenDebugIsTrue() {
        assertNull(Utils.replaceXPath("item[@id='original']", "replacement", true));
    }

    @Test
    public void replaceXPathWithTagsReplacesMatchingPredicate() {
        assertEquals("xxx:book[xxx:title[text()='replacement']]",
                Utils.replaceXPath("ns:book[ns:title[text()='original']]",
                        "book", "title", "replacement", false));
    }

    @Test
    public void replaceXPathWithTagsReturnsNullForNullInput() {
        assertNull(Utils.replaceXPath(null, "book", "title", "replacement", false));
    }

    @Test
    public void replaceXPathWithTagsReturnsNullForNonmatchingInputWhenDebugIsTrue() {
        assertNull(Utils.replaceXPath("ns:book[@id='original']",
                "book", "title", "replacement", true));
    }

    @Test
    public void replaceXPathWithPrefixReplacesMatchingPredicate() {
        assertEquals("p:book[p:title[text()='replacement']]",
                Utils.replaceXPath("ns:book[ns:title[text()='original']]",
                        "p", "book", "title", "replacement", false));
    }

    @Test
    public void replaceXPathWithPrefixReturnsNullForNullInput() {
        assertNull(Utils.replaceXPath(null, "p", "book", "title", "replacement", false));
    }

    @Test
    public void replaceXPathWithPrefixLeavesNonmatchingInputUnchangedWhenDebugIsFalse() {
        assertEquals("ns:book[@id='original']",
                Utils.replaceXPath("ns:book[@id='original']",
                        "p", "book", "title", "replacement", false));
    }
}
