package edu.umn.biomedicus.common.tuples;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class WordPos_ArrowTest {

  @Test
  public void constructsAndGetsComponents() {
    WordPos wordPos = new WordPos("running", PartOfSpeech.VERB);

    assertEquals("running", wordPos.getWord());
    assertEquals(PartOfSpeech.VERB, wordPos.getTag());
  }

  @Test
  public void forwardRemovesFirstCharacter() {
    WordPos wordPos = new WordPos("running", PartOfSpeech.VERB);

    WordPos forwarded = wordPos.forward();

    assertEquals("unning", forwarded.getWord());
    assertSame(PartOfSpeech.VERB, forwarded.getTag());
  }

  @Test
  public void settersUpdateComponents() {
    WordPos wordPos = new WordPos("run", PartOfSpeech.VERB);
    wordPos.setWord("walk");
    wordPos.setTag(PartOfSpeech.NOUN);

    assertEquals("walk", wordPos.getWord());
    assertEquals(PartOfSpeech.NOUN, wordPos.getTag());
  }

  @Test
  public void equalValuesHaveEqualHashCodes() {
    WordPos first = new WordPos("word", PartOfSpeech.NOUN);
    WordPos second = new WordPos("word", PartOfSpeech.NOUN);

    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void unequalValuesAreNotEqual() {
    WordPos first = new WordPos("word", PartOfSpeech.NOUN);
    WordPos differentWord = new WordPos("other", PartOfSpeech.NOUN);
    WordPos differentTag = new WordPos("word", PartOfSpeech.VERB);

    assertNotEquals(first, differentWord);
    assertNotEquals(first, differentTag);
    assertFalse(first.equals(null));
    assertFalse(first.equals("word"));
  }

  @Test
  public void comparesByWordThenTag() {
    WordPos first = new WordPos("apple", PartOfSpeech.NOUN);
    WordPos laterWord = new WordPos("banana", PartOfSpeech.NOUN);
    WordPos sameWordDifferentTag = new WordPos("apple", PartOfSpeech.VERB);

    assertTrue(first.compareTo(laterWord) < 0);
    assertTrue(first.compareTo(sameWordDifferentTag) < 0);
    assertEquals(0, first.compareTo(new WordPos("apple", PartOfSpeech.NOUN)));
  }

  @Test
  public void formatsAsString() {
    WordPos wordPos = new WordPos("word", PartOfSpeech.NOUN);

    assertEquals("WordPos{word='word', tag=NOUN}", wordPos.toString());
  }

  @Test(expected = NullPointerException.class)
  public void constructorRejectsNullWord() {
    new WordPos(null, PartOfSpeech.NOUN);
  }

  @Test(expected = NullPointerException.class)
  public void constructorRejectsNullTag() {
    new WordPos("word", null);
  }
}
