package morfologik.stemming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import morfologik.fsa.FSA;
import morfologik.fsa.FSATraversal;

public class DictionaryLookup_ArrowTest {

    private Dictionary dictionary;
    private DictionaryLookup lookup;

    @Before
    public void setUp() throws Exception {
        // Create a minimal dictionary for testing
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        
        DictionaryMetadata metadata = new DictionaryMetadata(encoder, decoder);
        
        FSA fsa = new FSA() {
            @Override
            public int getRootNode() {
                return 1;
            }
            
            @Override
            public int getArc(int node, int label) {
                return 0;
            }
            
            @Override
            public boolean isArcFinal(int arc) {
                return false;
            }
            
            @Override
            public int getEndNode(int arc) {
                return 0;
            }
            
            @Override
            public void setRootNode(int node) {
                // No-op
            }
            
            @Override
            public FSATraversal getTraversal() {
                return new FSATraversal(this);
            }
            
            @Override
            public int getFlags(int node) {
                return 0;
            }
        };
        
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
