package edu.umn.biomedicus.common.tuples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.util.Objects;
import org.junit.Test;

public class WordPos_ArrowTest {

  @Test
  public void testDefaultConstructor() {
    WordPos wordPos = new WordPos();
    assertEquals(null, wordPos.getWord());
    assertEquals(null, wordPos.getTag());
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
    WordPos forwardWordPos = wordPos.forward();
    assertEquals("ello", forwardWordPos.getWord());
    assertEquals(tag, forwardWordPos.getTag());
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
    WordPos wordPos = new WordPos(word, tag);
    wordPos.setWord("new");
    assertEquals("new", wordPos.getWord());
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
    WordPos wordPos = new WordPos(word, tag);
    PartOfSpeech newTag = PartOfSpeech.VERB;
    wordPos.setTag(newTag);
    assertEquals(newTag, wordPos.getTag());
  }

  @Test
  public void testEquals() {
    PartOfSpeech tag = PartOfSpeech.NOUN;
    String word = "test";
    WordPos wordPos1 = new WordPos(word, tag);
    WordPos wordPos2 = new WordPos(word, tag);
    assertTrue(wordPos1.equals(wordPos2));
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
    assertFalse(wordPos.equals("string"));
  }

  @Test
  public void testEqualsDifferentWord() {
    PartOfSpeech tag = PartOfSpeech.NOUN;
    WordPos wordPos1 = new WordPos("test", tag);
    WordPos wordPos2 = new WordPos("other", tag);
    assertFalse(wordPos1.equals(wordPos2));
  }

  @Test
  public void testEqualsDifferentTag() {
    String word = "test";
    WordPos wordPos1 = new WordPos(word, PartOfSpeech.NOUN);
    WordPos wordPos2 = new WordPos(word, PartOfSpeech.VERB);
    assertFalse(wordPos1.equals(wordPos2));
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
