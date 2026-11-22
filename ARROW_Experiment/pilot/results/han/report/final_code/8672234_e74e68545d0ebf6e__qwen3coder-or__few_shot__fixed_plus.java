package morfologik.stemming;

import morfologik.fsa.FSA;
import morfologik.fsa.FSATraversal;
import morfologik.fsa.MatchResult;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.*;

import static org.junit.Assert.*;

public class DictionaryLookup_ArrowTest {

    @Test
    public void testConstructor() throws Exception {
        Dictionary dictionary = new Dictionary();
        dictionary.metadata = new DictionaryMetadata();
        dictionary.metadata.setSeparator((byte) '/');
        dictionary.metadata.setDecoder(CharsetDecoder.forName("UTF-8"));
        dictionary.metadata.setEncoder(CharsetEncoder.forName("UTF-8"));
        dictionary.metadata.setInputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setOutputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setSequenceEncoderType(SequenceEncoderType.NONE);
        dictionary.fsa = new FSA(new byte[]{1, 2, 3});
        
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        assertNotNull(lookup);
    }

    @Test
    public void testLookupEmptyWord() throws Exception {
        Dictionary dictionary = new Dictionary();
        dictionary.metadata = new DictionaryMetadata();
        dictionary.metadata.setSeparator((byte) '/');
        dictionary.metadata.setDecoder(CharsetDecoder.forName("UTF-8"));
        dictionary.metadata.setEncoder(CharsetEncoder.forName("UTF-8"));
        dictionary.metadata.setInputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setOutputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setSequenceEncoderType(SequenceEncoderType.NONE);
        dictionary.fsa = new FSA(new byte[]{1, 2, 3});
        
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        List<WordData> result = lookup.lookup("");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testLookupWithSeparatorInWord() throws Exception {
        Dictionary dictionary = new Dictionary();
        dictionary.metadata = new DictionaryMetadata();
        dictionary.metadata.setSeparator((byte) '/');
        dictionary.metadata.setDecoder(CharsetDecoder.forName("UTF-8"));
        dictionary.metadata.setEncoder(CharsetEncoder.forName("UTF-8"));
        dictionary.metadata.setInputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setOutputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setSequenceEncoderType(SequenceEncoderType.NONE);
        dictionary.fsa = new FSA(new byte[]{1, 2, 3});
        
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        List<WordData> result = lookup.lookup("test/word");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetSeparatorChar() throws Exception {
        Dictionary dictionary = new Dictionary();
        dictionary.metadata = new DictionaryMetadata();
        dictionary.metadata.setSeparator((byte) '/');
        dictionary.metadata.setDecoder(CharsetDecoder.forName("UTF-8"));
        dictionary.metadata.setEncoder(CharsetEncoder.forName("UTF-8"));
        dictionary.metadata.setInputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setOutputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setSequenceEncoderType(SequenceEncoderType.NONE);
        dictionary.fsa = new FSA(new byte[]{1, 2, 3});
        
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        char separator = lookup.getSeparatorChar();
        assertEquals('/', separator);
    }

    @Test
    public void testApplyReplacements() throws Exception {
        LinkedHashMap<String, String> replacements = new LinkedHashMap<>();
        replacements.put("a", "b");
        replacements.put("c", "d");
        
        String result = DictionaryLookup.applyReplacements("abcabc", replacements);
        assertEquals("bbdbbd", result);
    }

    @Test
    public void testIterator() throws Exception {
        Dictionary dictionary = new Dictionary();
        dictionary.metadata = new DictionaryMetadata();
        dictionary.metadata.setSeparator((byte) '/');
        dictionary.metadata.setDecoder(CharsetDecoder.forName("UTF-8"));
        dictionary.metadata.setEncoder(CharsetEncoder.forName("UTF-8"));
        dictionary.metadata.setInputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setOutputConversionPairs(new LinkedHashMap<String, String>());
        dictionary.metadata.setSequenceEncoderType(SequenceEncoderType.NONE);
        dictionary.fsa = new FSA(new byte[]{1, 2, 3});
        
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        Iterator<WordData> iterator = lookup.iterator();
        assertNotNull(iterator);
    }
}
