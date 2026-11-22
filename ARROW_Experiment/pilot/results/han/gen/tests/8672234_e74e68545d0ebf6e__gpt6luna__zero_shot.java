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
  public void applyReplacementsReplacesEveryOccurrenceInMapOrder() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("ab", "x");
    replacements.put("x", "yz");

    assertEquals("yzyz", DictionaryLookup.applyReplacements("abab", replacements));
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
    Dictionary dictionary = Dictionary.read("pl".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    List<WordData> result = lookup.lookup("");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getDictionaryReturnsSuppliedDictionary() {
    Dictionary dictionary = Dictionary.read("pl".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    assertSame(dictionary, lookup.getDictionary());
  }

  @Test
  public void iteratorReturnsIterator() {
    Dictionary dictionary = Dictionary.read("pl".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    Iterator<WordData> iterator = lookup.iterator();

    assertNotNull(iterator);
  }

  @Test
  public void getSeparatorCharReturnsMetadataSeparator() {
    Dictionary dictionary = Dictionary.read("pl".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    assertEquals(dictionary.metadata.getSeparatorAsChar(), lookup.getSeparatorChar());
  }
}
