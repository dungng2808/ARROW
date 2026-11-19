package edu.umn.biomedicus.common.tuples;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import org.junit.Test;
import static org.junit.Assert.*;

public class WordPos_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        WordPos wordPos = new WordPos();
        assertNull(wordPos.getWord());
        assertNull(wordPos.getTag());
    }

    @Test
    public void testParameterizedConstructor() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        assertEquals(word, wordPos.getWord());
        assertEquals(tag, wordPos.getTag());
    }

    @Test(expected = NullPointerException.class)
    public void testParameterizedConstructorNullWord() {
        new WordPos(null, PartOfSpeech.NOUN);
    }

    @Test(expected = NullPointerException.class)
    public void testParameterizedConstructorNullTag() {
        new WordPos("test", null);
    }

    @Test
    public void testForward() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "hello";
        WordPos original = new WordPos(word, tag);
        WordPos forwarded = original.forward();
        
        assertEquals("ello", forwarded.getWord());
        assertEquals(tag, forwarded.getTag());
    }

    @Test
    public void testGetWord() {
        String word = "test";
        WordPos wordPos = new WordPos(word, PartOfSpeech.NOUN);
        assertEquals(word, wordPos.getWord());
    }

    @Test
    public void testSetWord() {
        WordPos wordPos = new WordPos("old", PartOfSpeech.NOUN);
        wordPos.setWord("new");
        assertEquals("new", wordPos.getWord());
    }

    @Test
    public void testGetTag() {
        PartOfSpeech tag = PartOfSpeech.VERB;
        WordPos wordPos = new WordPos("test", tag);
        assertEquals(tag, wordPos.getTag());
    }

    @Test
    public void testSetTag() {
        WordPos wordPos = new WordPos("test", PartOfSpeech.NOUN);
        wordPos.setTag(PartOfSpeech.VERB);
        assertEquals(PartOfSpeech.VERB, wordPos.getTag());
    }

    @Test
    public void testEqualsSameObject() {
        WordPos wordPos = new WordPos("test", PartOfSpeech.NOUN);
        assertTrue(wordPos.equals(wordPos));
    }

    @Test
    public void testEqualsNull() {
        WordPos wordPos = new WordPos("test", PartOfSpeech.NOUN);
        assertFalse(wordPos.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        WordPos wordPos = new WordPos("test", PartOfSpeech.NOUN);
        assertFalse(wordPos.equals("not a wordpos"));
    }

    @Test
    public void testEqualsSameValues() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("test", PartOfSpeech.NOUN);
        assertTrue(wordPos1.equals(wordPos2));
        assertTrue(wordPos2.equals(wordPos1));
    }

    @Test
    public void testEqualsDifferentWords() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("different", PartOfSpeech.NOUN);
        assertFalse(wordPos1.equals(wordPos2));
    }

    @Test
    public void testEqualsDifferentTags() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("test", PartOfSpeech.VERB);
        assertFalse(wordPos1.equals(wordPos2));
    }

    @Test
    public void testHashCode() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("test", PartOfSpeech.NOUN);
        assertEquals(wordPos1.hashCode(), wordPos2.hashCode());
    }

    @Test
    public void testCompareTo() {
        WordPos wordPos1 = new WordPos("apple", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("banana", PartOfSpeech.NOUN);
        assertTrue(wordPos1.compareTo(wordPos2) < 0);
        assertTrue(wordPos2.compareTo(wordPos1) > 0);
    }

    @Test
    public void testCompareToSameWord() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("test", PartOfSpeech.VERB);
        assertTrue(wordPos1.compareTo(wordPos2) < 0);
        assertTrue(wordPos2.compareTo(wordPos1) > 0);
    }

    @Test
    public void testCompareToEqual() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("test", PartOfSpeech.NOUN);
        assertEquals(0, wordPos1.compareTo(wordPos2));
        assertEquals(0, wordPos2.compareTo(wordPos1));
    }

    @Test
    public void testToString() {
        WordPos wordPos = new WordPos("hello", PartOfSpeech.NOUN);
        String expected = "WordPos{word='hello', tag=" + PartOfSpeech.NOUN.toString() + '}';
        assertEquals(expected, wordPos.toString());
    }
}
