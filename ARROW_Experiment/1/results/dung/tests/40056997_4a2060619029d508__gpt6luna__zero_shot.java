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
	public void resolveEnvVarsLeavesTextWithoutVariablesUnchanged() {
		assertEquals("plain text", Utils.resolveEnvVars("plain text"));
	}

	@Test
	public void resolveEnvVarsRemovesUnknownVariables() {
		assertEquals("beforeafter",
				Utils.resolveEnvVars("before$UTILS_ARROW_TEST_UNKNOWN_VARIABLEafter"));
	}

	@Test
	public void replaceXPathReturnsNullForNullInput() {
		assertNull(Utils.replaceXPath(null, "name", false));
	}

	@Test
	public void replaceXPathReplacesMatchingTextPredicate() {
		assertEquals("item[text()='newName']",
				Utils.replaceXPath("item[text()='oldName']", "newName", false));
	}

	@Test
	public void replaceXPathDebugReturnsNullWhenNoPredicateMatches() {
		assertNull(Utils.replaceXPath("item[@id='oldName']", "newName", true));
	}

	@Test
	public void replaceXPathWithTagsReturnsNullForNullInput() {
		assertNull(Utils.replaceXPath(null, "tag1", "tag2", "name", false));
	}

	@Test
	public void replaceXPathWithTagsUsesDefaultPrefix() {
		assertEquals("xxx:tag1[xxx:tag2[text()='newName']]",
				Utils.replaceXPath("a:b[c:d[text()='oldName']]", "tag1", "tag2",
						"newName", false));
	}

	@Test
	public void replaceXPathWithExplicitPrefixReplacesMatchingExpression() {
		assertEquals("p:tag1[p:tag2[text()='newName']]",
				Utils.replaceXPath("a:b[c:d[text()='oldName']]", "p", "tag1",
						"tag2", "newName", false));
	}

	@Test
	public void replaceXPathWithExplicitPrefixDebugReturnsNullWhenNoMatch() {
		assertNull(Utils.replaceXPath("item[text()='oldName']", "p", "tag1",
				"tag2", "newName", true));
	}
}
