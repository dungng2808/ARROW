package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DictionaryLookup_ArrowTest {

    private Dictionary dictionary;
    private DictionaryLookup lookup;

    @Before
    public void setUp() throws Exception {
        // Create a minimal dictionary for testing
        Charset charset = Charset.forName("UTF-8");
        
        DictionaryMetadata metadata = new DictionaryMetadataBuilder()
                .encoder(EncoderType.NONE)
                .separator('/')
                .withInputConversionPairs(new LinkedHashMap<String, String>())
                .withOutputConversionPairs(new LinkedHashMap<String, String>())
                .build();
        
        morfologik.fsa.FSA fsa = morfologik.fsa.FSA.read(this.getClass().getResourceAsStream("/morfologik/stemming/dictionary.fsa"));
        
        dictionary = new Dictionary(metadata, fsa);
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
        List<WordData> result = lookup.lookup("test/word");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testApplyReplacements() {
        LinkedHashMap<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("a", "A");
        replacements.put("b", "B");
        
        String result = DictionaryLookup.applyReplacements("abcabc", replacements);
        assertEquals("ABcABc", result);
    }

    @Test
    public void testGetSeparatorChar() {
        char separator = lookup.getSeparatorChar();
        assertEquals('/', separator);
    }
}
