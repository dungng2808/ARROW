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
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        assertEquals(word, wordPos.getWord());
    }

    @Test
    public void testSetWord() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos("old", tag);
        
        wordPos.setWord(word);
        assertEquals(word, wordPos.getWord());
    }

    @Test
    public void testGetTag() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        assertEquals(tag, wordPos.getTag());
    }

    @Test
    public void testSetTag() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, PartOfSpeech.VERB);
        
        wordPos.setTag(tag);
        assertEquals(tag, wordPos.getTag());
    }

    @Test
    public void testEquals() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos1 = new WordPos(word, tag);
        WordPos wordPos2 = new WordPos(word, tag);
        
        assertEquals(wordPos1, wordPos2);
    }

    @Test
    public void testEqualsSelf() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        assertEquals(wordPos, wordPos);
    }

    @Test
    public void testNotEqualsNull() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        assertNotEquals(wordPos, null);
    }

    @Test
    public void testNotEqualsDifferentClass() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        assertNotEquals(wordPos, "not a wordpos");
    }

    @Test
    public void testNotEqualsDifferentWord() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        WordPos wordPos1 = new WordPos("test", tag);
        WordPos wordPos2 = new WordPos("different", tag);
        
        assertNotEquals(wordPos1, wordPos2);
    }

    @Test
    public void testNotEqualsDifferentTag() {
        String word = "test";
        WordPos wordPos1 = new WordPos(word, PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos(word, PartOfSpeech.VERB);
        
        assertNotEquals(wordPos1, wordPos2);
    }

    @Test
    public void testHashCode() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos1 = new WordPos(word, tag);
        WordPos wordPos2 = new WordPos(word, tag);
        
        assertEquals(wordPos1.hashCode(), wordPos2.hashCode());
    }

    @Test
    public void testCompareTo() {
        PartOfSpeech noun = PartOfSpeech.NOUN;
        PartOfSpeech verb = PartOfSpeech.VERB;
        WordPos wordPos1 = new WordPos("apple", noun);
        WordPos wordPos2 = new WordPos("banana", noun);
        WordPos wordPos3 = new WordPos("apple", verb);
        
        assertTrue(wordPos1.compareTo(wordPos2) < 0);
        assertTrue(wordPos2.compareTo(wordPos1) > 0);
        assertTrue(wordPos1.compareTo(wordPos3) < 0);
        assertTrue(wordPos3.compareTo(wordPos1) > 0);
    }

    @Test
    public void testToString() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);
        
        String expected = "WordPos{word='" + word + '\'' + ", tag=" + tag.toString() + '}';
        assertEquals(expected, wordPos.toString());
    }
}
