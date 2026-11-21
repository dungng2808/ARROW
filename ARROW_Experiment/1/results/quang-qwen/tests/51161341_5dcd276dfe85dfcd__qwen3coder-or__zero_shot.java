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

    @Test
    public void testForward() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "hello";
        WordPos wordPos = new WordPos(word, tag);
        WordPos forwarded = wordPos.forward();

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
        String originalWord = "test";
        String newWord = "new";
        WordPos wordPos = new WordPos(originalWord, tag);

        wordPos.setWord(newWord);
        assertEquals(newWord, wordPos.getWord());
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
        PartOfSpeech originalTag = PartOfSpeech.NOUN;
        PartOfSpeech newTag = PartOfSpeech.VERB;
        String word = "test";
        WordPos wordPos = new WordPos(word, originalTag);

        wordPos.setTag(newTag);
        assertEquals(newTag, wordPos.getTag());
    }

    @Test
    public void testEqualsSameObject() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);

        assertTrue(wordPos.equals(wordPos));
    }

    @Test
    public void testEqualsNull() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);

        assertFalse(wordPos.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);

        assertFalse(wordPos.equals("not a wordpos"));
    }

    @Test
    public void testEqualsSameValues() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos1 = new WordPos(word, tag);
        WordPos wordPos2 = new WordPos(word, tag);

        assertTrue(wordPos1.equals(wordPos2));
    }

    @Test
    public void testEqualsDifferentWords() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        WordPos wordPos1 = new WordPos("test", tag);
        WordPos wordPos2 = new WordPos("different", tag);

        assertFalse(wordPos1.equals(wordPos2));
    }

    @Test
    public void testEqualsDifferentTags() {
        String word = "test";
        WordPos wordPos1 = new WordPos(word, PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos(word, PartOfSpeech.VERB);

        assertFalse(wordPos1.equals(wordPos2));
    }

    @Test
    public void testHashCode() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        String word = "test";
        WordPos wordPos = new WordPos(word, tag);

        int expected = word.hashCode() + 31 * tag.hashCode();
        assertEquals(expected, wordPos.hashCode());
    }

    @Test
    public void testCompareTo() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        WordPos wordPos1 = new WordPos("apple", tag);
        WordPos wordPos2 = new WordPos("banana", tag);

        assertTrue(wordPos1.compareTo(wordPos2) < 0);
    }

    @Test
    public void testCompareToSameWord() {
        WordPos wordPos1 = new WordPos("test", PartOfSpeech.NOUN);
        WordPos wordPos2 = new WordPos("test", PartOfSpeech.VERB);

        assertTrue(wordPos1.compareTo(wordPos2) < 0);
    }

    @Test
    public void testCompareToEqual() {
        PartOfSpeech tag = PartOfSpeech.NOUN;
        WordPos wordPos1 = new WordPos("test", tag);
        WordPos wordPos2 = new WordPos("test", tag);

        assertEquals(0, wordPos1.compareTo(wordPos2));
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
