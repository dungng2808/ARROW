package morfologik.stemming;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.junit.Test;

public class DictionaryLookup_ArrowTest {
  @Test
  public void applyReplacementsReplacesEveryOccurrenceInMapOrder() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("ab", "x");
    replacements.put("x", "yz");

    assertEquals("yzab", DictionaryLookup.applyReplacements("abab", replacements));
  }

  @Test
  public void applyReplacementsReturnsUnchangedTextWhenNoKeysMatch() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("z", "q");

    assertEquals("abc", DictionaryLookup.applyReplacements("abc", replacements));
  }

  @Test
  public void applyReplacementsHandlesEmptyInput() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("a", "b");

    assertEquals("", DictionaryLookup.applyReplacements("", replacements));
  }

  @Test
  public void applyReplacementsHandlesEmptyReplacement() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("a", "");

    assertEquals("bc", DictionaryLookup.applyReplacements("abc", replacements));
  }
}
