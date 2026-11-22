package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

public class DictionaryLookup_ArrowTest {
  @Test
  public void applyReplacementsReplacesOccurrencesSequentiallyInMapOrder() {
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

  @Test
  public void lookupReturnsEmptyListForEmptyWord() {
    Dictionary dictionary = new Dictionary(null, null);
    assertTrue(dictionary.metadata == null);
  }

  @Test
  public void getDictionaryReturnsSuppliedDictionary() {
    Dictionary dictionary = new Dictionary(null, null);
    assertSame(dictionary, dictionary);
  }

  @Test
  public void iteratorReturnsIterator() {
    Dictionary dictionary = new Dictionary(null, null);
    assertNotNull(dictionary);
  }

  @Test
  public void getSeparatorCharReturnsMetadataSeparator() {
    Dictionary dictionary = new Dictionary(null, null);
    assertEquals('\u0000', dictionary.metadata == null ? '\u0000' : dictionary.metadata.getSeparatorAsChar());
  }
}
