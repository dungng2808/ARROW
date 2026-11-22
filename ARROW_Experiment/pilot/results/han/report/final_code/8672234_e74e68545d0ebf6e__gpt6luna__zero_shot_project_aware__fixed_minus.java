package morfologik.stemming;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;

import org.junit.Test;

public class DictionaryLookup_ArrowTest {
  @Test
  public void applyReplacementsReplacesAllOccurrencesInMapOrder() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("ab", "x");
    replacements.put("x", "yz");

    assertThat(DictionaryLookup.applyReplacements("abab", replacements)).isEqualTo("yzab");
  }

  @Test
  public void applyReplacementsLeavesTextUnchangedWhenThereAreNoReplacements() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();

    assertThat(DictionaryLookup.applyReplacements("word", replacements)).isEqualTo("word");
  }

  @Test
  public void applyReplacementsHandlesEmptyInput() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("a", "b");

    assertThat(DictionaryLookup.applyReplacements("", replacements)).isEqualTo("");
  }

  @Test
  public void applyReplacementsCanReplaceWithEmptyText() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("ab", "");

    assertThat(DictionaryLookup.applyReplacements("zababa", replacements)).isEqualTo("zaba");
  }

  @Test
  public void applyReplacementsDoesNotReplaceAnAbsentKey() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("missing", "value");

    assertThat(DictionaryLookup.applyReplacements("word", replacements)).isEqualTo("word");
  }

  @Test
  public void applyReplacementsSupportsOverlappingOccurrences() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("aa", "b");

    assertThat(DictionaryLookup.applyReplacements("aaaaa", replacements)).isEqualTo("bab");
  }
}
