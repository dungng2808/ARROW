package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import morfologik.fsa.FSA;
import org.junit.Test;

public class DictionaryLookup_ArrowTest {
  private Dictionary readDictionary() throws Exception {
    String metadata = "fsa.dict.separator=\\n\n"
        + "fsa.dict.encoding=UTF-8\n"
        + "fsa.dict.encoder=SUFFIX\n";
    InputStream metadataStream = new ByteArrayInputStream(
        metadata.getBytes("UTF-8"));
    InputStream dictionaryStream = new ByteArrayInputStream(createEmptyFsa());
    return Dictionary.read(dictionaryStream, metadataStream);
  }

  private byte[] createEmptyFsa() throws Exception {
    return new byte[] {
        (byte) 0x5D, (byte) 0xF9, (byte) 0x2A, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
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
    Dictionary dictionary = readDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    List<WordData> result = lookup.lookup("");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getDictionaryReturnsSuppliedDictionary() throws Exception {
    Dictionary dictionary = readDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    assertSame(dictionary, lookup.getDictionary());
  }

  @Test
  public void iteratorReturnsIterator() throws Exception {
    Dictionary dictionary = readDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    Iterator<WordData> iterator = lookup.iterator();

    assertNotNull(iterator);
  }

  @Test
  public void getSeparatorCharReturnsMetadataSeparator() throws Exception {
    Dictionary dictionary = readDictionary();
    DictionaryLookup lookup = new DictionaryLookup(dictionary);

    assertEquals(dictionary.metadata.getSeparatorAsChar(), lookup.getSeparatorChar());
  }
}
