package edu.umn.biomedicus.common.tuples;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordPos_ArrowTest {

  @Test
  public void constructorAndGettersReturnComponents() {
    WordPos wordPos = new WordPos("cat", PartOfSpeech.NN);

    assertEquals("cat", wordPos.getWord());
    assertEquals(PartOfSpeech.NN, wordPos.getTag());
  }

  @Test
  public void defaultConstructorCreatesObjectWithNullComponents() {
    WordPos wordPos = new WordPos();

    assertNotNull(wordPos);
    assertEquals(null, wordPos.getWord());
    assertEquals(null, wordPos.getTag());
  }

  @Test
  public void settersReplaceComponents() {
    WordPos wordPos = new WordPos("cat", PartOfSpeech.NN);
    wordPos.setWord("dog");
    wordPos.setTag(PartOfSpeech.VB);

    assertEquals("dog", wordPos.getWord());
    assertEquals(PartOfSpeech.VB, wordPos.getTag());
  }

  @Test
  public void forwardRemovesFirstCharacterAndPreservesTag() {
    WordPos wordPos = new WordPos("cats", PartOfSpeech.NN);

    WordPos result = wordPos.forward();

    assertEquals("ats", result.getWord());
    assertEquals(PartOfSpeech.NN, result.getTag());
  }

  @Test
  public void forwardOnSingleCharacterReturnsEmptyWord() {
    WordPos wordPos = new WordPos("x", PartOfSpeech.NN);

    assertEquals("", wordPos.forward().getWord());
  }

  @Test
  public void forwardOnEmptyWordThrows() {
    WordPos wordPos = new WordPos("", PartOfSpeech.NN);

    assertThrows(StringIndexOutOfBoundsException.class, wordPos::forward);
  }

  @Test
  public void constructorRejectsNullWord() {
    assertThrows(NullPointerException.class, () -> new WordPos(null, PartOfSpeech.NN));
  }

  @Test
  public void constructorRejectsNullTag() {
    assertThrows(NullPointerException.class, () -> new WordPos("cat", null));
  }

  @Test
  public void equalsAndHashCodeUseWordAndTag() {
    WordPos first = new WordPos("cat", PartOfSpeech.NN);
    WordPos same = new WordPos("cat", PartOfSpeech.NN);
    WordPos differentWord = new WordPos("dog", PartOfSpeech.NN);
    WordPos differentTag = new WordPos("cat", PartOfSpeech.VB);

    assertTrue(first.equals(same));
    assertEquals(first.hashCode(), same.hashCode());
    assertFalse(first.equals(differentWord));
    assertFalse(first.equals(differentTag));
    assertFalse(first.equals(null));
    assertFalse(first.equals("cat"));
  }

  @Test
  public void compareToOrdersByWordThenTag() {
    WordPos cat = new WordPos("cat", PartOfSpeech.NN);
    WordPos dog = new WordPos("dog", PartOfSpeech.NN);
    WordPos catWithDifferentTag = new WordPos("cat", PartOfSpeech.VB);

    assertTrue(cat.compareTo(dog) < 0);
    assertTrue(cat.compareTo(catWithDifferentTag) < 0);
    assertEquals(0, cat.compareTo(new WordPos("cat", PartOfSpeech.NN)));
  }

  @Test
  public void toStringIncludesWordAndTag() {
    WordPos wordPos = new WordPos("cat", PartOfSpeech.NN);

    assertEquals("WordPos{word='cat', tag=" + PartOfSpeech.NN.toString() + "}", wordPos.toString());
  }
}
