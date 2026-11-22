package morfologik.stemming;

import morfologik.fsa.FSA;
import morfologik.fsa.FSATraversal;
import morfologik.fsa.MatchResult;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.*;

import static org.junit.Assert.*;

public class DictionaryLookup_ArrowTest {

    @Test
    public void testConstructor() throws Exception {
        DictionaryMetadata metadata = DictionaryMetadata.builder()
                .separator('/')
                .encoding("UTF-8")
                .withInputConversionPairs(new LinkedHashMap<String, String>())
                .withOutputConversionPairs(new LinkedHashMap<String, String>())
                .encoder(EncoderType.NONE)
                .build();
        
        FSA fsa = new FSA.Builder().build();
        
        Dictionary dictionary = new Dictionary(fsa, metadata);
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        assertNotNull(lookup);
    }

    @Test
    public void testLookupEmptyWord() throws Exception {
        DictionaryMetadata metadata = DictionaryMetadata.builder()
                .separator('/')
                .encoding("UTF-8")
                .withInputConversionPairs(new LinkedHashMap<String, String>())
                .withOutputConversionPairs(new LinkedHashMap<String, String>())
                .encoder(EncoderType.NONE)
                .build();
        
        FSA fsa = new FSA.Builder().build();
        
        Dictionary dictionary = new Dictionary(fsa, metadata);
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        List<WordData> result = lookup.lookup("");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testLookupWithSeparatorInWord() throws Exception {
        DictionaryMetadata metadata = DictionaryMetadata.builder()
                .separator('/')
                .encoding("UTF-8")
                .withInputConversionPairs(new LinkedHashMap<String, String>())
                .withOutputConversionPairs(new LinkedHashMap<String, String>())
                .encoder(EncoderType.NONE)
                .build();
        
        FSA fsa = new FSA.Builder().build();
        
        Dictionary dictionary = new Dictionary(fsa, metadata);
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        List<WordData> result = lookup.lookup("test/word");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetSeparatorChar() throws Exception {
        DictionaryMetadata metadata = DictionaryMetadata.builder()
                .separator('/')
                .encoding("UTF-8")
                .withInputConversionPairs(new LinkedHashMap<String, String>())
                .withOutputConversionPairs(new LinkedHashMap<String, String>())
                .encoder(EncoderType.NONE)
                .build();
        
        FSA fsa = new FSA.Builder().build();
        
        Dictionary dictionary = new Dictionary(fsa, metadata);
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
        DictionaryMetadata metadata = DictionaryMetadata.builder()
                .separator('/')
                .encoding("UTF-8")
                .withInputConversionPairs(new LinkedHashMap<String, String>())
                .withOutputConversionPairs(new LinkedHashMap<String, String>())
                .encoder(EncoderType.NONE)
                .build();
        
        FSA fsa = new FSA.Builder().build();
        
        Dictionary dictionary = new Dictionary(fsa, metadata);
        DictionaryLookup lookup = new DictionaryLookup(dictionary);
        Iterator<WordData> iterator = lookup.iterator();
        assertNotNull(iterator);
    }
}
