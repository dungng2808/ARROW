package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

import morfologik.fsa.FSA;
import morfologik.fsa.FSATraversal;
import morfologik.fsa.MatchResult;

import org.junit.Before;
import org.junit.Test;

public class DictionaryLookup_ArrowTest {

  private Dictionary dictionary;
  private DictionaryLookup lookup;

  @Before
  public void setUp() throws Exception {
    // Create a minimal dictionary for testing
    FSA fsa = new FSA(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    DictionaryMetadata metadata = new DictionaryMetadata();
    metadata.setSeparator((byte) '/');
    metadata.setDecoder(Charset.defaultCharset().newDecoder());
    metadata.setEncoder(Charset.defaultCharset().newEncoder());
    metadata.setInputConversionPairs(new LinkedHashMap<String, String>());
    metadata.setOutputConversionPairs(new LinkedHashMap<String, String>());
    metadata.setSequenceEncoderType(SequenceEncoderType.NONE);
    
    dictionary = new Dictionary(fsa, metadata);
    lookup = new DictionaryLookup(dictionary);
  }

  @Test
  public void testConstructor() throws Exception {
    assertNotNull(lookup);
    assertEquals(dictionary, lookup.getDictionary());
  }

  @Test
  public void testLookupEmptyWord() {
    List<WordData> result = lookup.lookup("");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testLookupWithSeparatorInWord() {
    List<WordData> result = lookup.lookup("test/");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testApplyReplacements() {
    LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
    replacements.put("a", "b");
    replacements.put("b", "c");
    
    String result = DictionaryLookup.applyReplacements("abc", replacements);
    assertEquals("bcc", result);
  }

  @Test
  public void testGetSeparatorChar() {
    char separator = lookup.getSeparatorChar();
    assertEquals('/', separator);
  }

  @Test
  public void testIterator() {
    Iterable<WordData> iterable = lookup;
    assertNotNull(iterable);
    
    // Just verify it doesn't throw
    iterable.iterator();
  }
}
