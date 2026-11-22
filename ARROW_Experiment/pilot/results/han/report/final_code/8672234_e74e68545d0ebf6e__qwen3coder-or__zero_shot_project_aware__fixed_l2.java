package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import morfologik.fsa.FSA;
import morfologik.fsa.FSATraversal;
import morfologik.fsa.MatchResult;
import morfologik.fsa.builders.FSABuilder;

import org.junit.Before;
import org.junit.Test;

public class DictionaryLookup_ArrowTest {

  private Dictionary dictionary;
  private DictionaryLookup lookup;

  @Before
  public void setUp() throws Exception {
    // Create a minimal dictionary for testing
    FSABuilder builder = new FSABuilder();
    builder.add("test".getBytes());
    FSA fsa = builder.build();
    DictionaryMetadata metadata = DictionaryMetadata.builder()
        .separator('/')
        .encoding(Charset.defaultCharset())
        .withInputConversionPairs(new LinkedHashMap<String, String>())
        .withOutputConversionPairs(new LinkedHashMap<String, String>())
        .encoder(EncoderType.NONE)
        .build();
    
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
