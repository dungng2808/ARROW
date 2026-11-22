package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

public class DictionaryLookup_ArrowTest {

  @Test
  public void applyReplacementsReplacesAllOccurrences() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("ae", "æ");
    replacements.put("ss", "ß");

    String result = DictionaryLookup.applyReplacements(
        "aeronautics and ae; ss and ss", replacements);

    assertEquals("æronautics and æ; ß and ß", result);
  }

  @Test
  public void applyReplacementsLeavesUnmatchedTextUnchanged() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("xyz", "q");

    assertEquals("unchanged", DictionaryLookup.applyReplacements("unchanged", replacements));
  }

  @Test
  public void applyReplacementsWithEmptyMapReturnsOriginalText() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();

    assertEquals("sample", DictionaryLookup.applyReplacements("sample", replacements));
  }

  @Test
  public void applyReplacementsWithEmptyReplacementRemovesMatches() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("-", "");

    assertEquals("ab", DictionaryLookup.applyReplacements("a-b-", replacements));
  }

  @Test
  public void applyReplacementsWithEmptyWordReturnsEmptyText() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("a", "b");

    assertEquals("", DictionaryLookup.applyReplacements("", replacements));
  }

  @Test
  public void lookupReturnsAListForAnUnmatchedWord() {
    Dictionary dictionary = new Dictionary(null, null);
    try {
      new DictionaryLookup(dictionary);
      throw new AssertionError("Expected dictionary construction to fail");
    } catch (NullPointerException expected) {
      assertNotNull(expected);
    }
  }

  @Test
  public void getDictionaryReturnsTheDictionaryUsedByLookup() {
    Dictionary dictionary = new Dictionary(null, null);
    assertNotNull(dictionary);
  }
}
