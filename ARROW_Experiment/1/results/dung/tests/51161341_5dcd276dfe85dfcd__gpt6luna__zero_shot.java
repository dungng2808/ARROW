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
  public void constructorAndGettersReturnComponents() {
    PartOfSpeech tag = PartOfSpeech.values()[0];
    WordPos wordPos = new WordPos("hello", tag);

    assertEquals("hello", wordPos.getWord());
    assertSame(tag, wordPos.getTag());
  }

  @Test
  public void settersReplaceComponents() {
    PartOfSpeech firstTag = PartOfSpeech.values()[0];
    PartOfSpeech secondTag = PartOfSpeech.values().length > 1
        ? PartOfSpeech.values()[1] : firstTag;
    WordPos wordPos = new WordPos("first", firstTag);

    wordPos.setWord("second");
    wordPos.setTag(secondTag);

    assertEquals("second", wordPos.getWord());
    assertSame(secondTag, wordPos.getTag());
  }

  @Test
  public void forwardRemovesFirstCharacterAndPreservesTag() {
    PartOfSpeech tag = PartOfSpeech.values()[0];
    WordPos wordPos = new WordPos("word", tag);

    WordPos forwarded = wordPos.forward();

    assertEquals("ord", forwarded.getWord());
    assertSame(tag, forwarded.getTag());
  }

  @Test
  public void equalValuesAreEqualAndHaveSameHashCode() {
    PartOfSpeech tag = PartOfSpeech.values()[0];
    WordPos first = new WordPos("word", tag);
    WordPos second = new WordPos("word", tag);

    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void equalityRejectsDifferentValuesAndTypes() {
    PartOfSpeech tag = PartOfSpeech.values()[0];
    WordPos wordPos = new WordPos("word", tag);

    assertFalse(wordPos.equals(null));
    assertFalse(wordPos.equals("word"));
    assertFalse(wordPos.equals(new WordPos("other", tag)));
    if (PartOfSpeech.values().length > 1) {
      assertFalse(wordPos.equals(new WordPos("word", PartOfSpeech.values()[1])));
    }
  }

  @Test
  public void compareToOrdersWordsAndThenTags() {
    PartOfSpeech firstTag = PartOfSpeech.values()[0];
    PartOfSpeech lastTag = PartOfSpeech.values()[PartOfSpeech.values().length - 1];
    WordPos alpha = new WordPos("alpha", lastTag);
    WordPos beta = new WordPos("beta", firstTag);

    assertTrue(alpha.compareTo(beta) < 0);
    assertTrue(beta.compareTo(alpha) > 0);

    WordPos sameWordFirstTag = new WordPos("same", firstTag);
    WordPos sameWordLastTag = new WordPos("same", lastTag);
    assertTrue(sameWordFirstTag.compareTo(sameWordLastTag) <= 0);
    assertTrue(sameWordLastTag.compareTo(sameWordFirstTag) >= 0);
  }

  @Test
  public void toStringIncludesWordAndTag() {
    PartOfSpeech tag = PartOfSpeech.values()[0];
    WordPos wordPos = new WordPos("word", tag);

    assertEquals("WordPos{word='word', tag=" + tag.toString() + "}", wordPos.toString());
  }

  @Test(expected = NullPointerException.class)
  public void constructorRejectsNullWord() {
    new WordPos(null, PartOfSpeech.values()[0]);
  }

  @Test(expected = NullPointerException.class)
  public void constructorRejectsNullTag() {
    new WordPos("word", null);
  }
}
