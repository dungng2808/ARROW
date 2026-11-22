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
  private Dictionary loadDictionary() throws Exception {
    return Dictionary.read(
        getClass().getResourceAsStream("/morfologik/stemming/pl.dict"),
        getClass().getResourceAsStream("/morfologik/stemming/pl.info"));
  }

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

  @Test
  public void lookupReturnsEmptyListForEmptyWord() throws Exception {
    Dictionary dictionary = loadDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    List<WordData> result = lookup.lookup("");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getDictionaryReturnsSuppliedDictionary() throws Exception {
    Dictionary dictionary = loadDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    assertSame(dictionary, lookup.getDictionary());
  }

  @Test
  public void iteratorReturnsIterator() throws Exception {
    Dictionary dictionary = loadDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    Iterator<WordData> iterator = lookup.iterator();

    assertNotNull(iterator);
  }

  @Test
  public void getSeparatorCharReturnsMetadataSeparator() throws Exception {
    Dictionary dictionary = loadDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    assertEquals(dictionary.metadata.getSeparatorAsChar(), lookup.getSeparatorChar());
  }
}
