package example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Utils_ArrowTest {

    @Test
    public void resolveEnvVarsReturnsNullForNullInput() {
        assertNull(Utils.resolveEnvVars(null));
    }

    @Test
    public void resolveEnvVarsReplacesUnsetVariablesWithEmptyStrings() {
        assertEquals("before--after", Utils.resolveEnvVars("before$UTILS_ARROW_UNSET_9F3A-after"));
    }

    @Test
    public void resolveEnvVarsLeavesOrdinaryTextUnchanged() {
        assertEquals("plain text", Utils.resolveEnvVars("plain text"));
    }

    @Test
    public void replaceXPathReturnsNullForNullInput() {
        assertNull(Utils.replaceXPath(null, "name", false));
    }

    @Test
    public void replaceXPathReplacesMatchingTextPredicate() {
        assertEquals("/root/item[text()='newName']",
                Utils.replaceXPath("/root/item[text()='oldName']", "newName", false));
    }

    @Test
    public void replaceXPathDebugReturnsNullWhenThereIsNoMatch() {
        assertNull(Utils.replaceXPath("/root/item[@id='oldName']", "newName", true));
    }

    @Test
    public void replaceXPathWithTagsReplacesMatchingPredicate() {
        assertEquals("xxx:item[xxx:value[text()='newName']]",
                Utils.replaceXPath("old:item[old:value[text()='oldName']]",
                        "item", "value", "newName", false));
    }

    @Test
    public void replaceXPathWithPrefixAndTagsReplacesMatchingPredicate() {
        assertEquals("p:item[p:value[text()='newName']]",
                Utils.replaceXPath("old:item[old:value[text()='oldName']]",
                        "p", "item", "value", "newName", false));
    }

    @Test
    public void replaceXPathWithPrefixAndTagsReturnsNullForNullInput() {
        assertNull(Utils.replaceXPath(null, "p", "item", "value", "name", false));
    }

    @Test
    public void replaceXPathWithPrefixAndTagsDebugReturnsNullWhenThereIsNoMatch() {
        assertNull(Utils.replaceXPath("item[value[text()='oldName']]",
                "p", "item", "value", "newName", true));
    }
}
