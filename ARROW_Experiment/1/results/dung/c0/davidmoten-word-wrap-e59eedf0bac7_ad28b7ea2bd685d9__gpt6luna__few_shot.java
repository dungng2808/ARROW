package org.davidmoten.text.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class WordWrap_ArrowTest {

    @Test
    public void wrapsAtWordBoundary() {
        String result = WordWrap.from("one two three").maxWidth(7).wrap();
        assertEquals("one two\nthree", result);
    }

    @Test
    public void breaksLongWordsWithHyphens() {
        String result = WordWrap.from("abcdefgh").maxWidth(4).wrap();
        assertEquals("abc-\ndefgh", result);
    }

    @Test
    public void breaksLongWordsWithoutHyphens() {
        String result = WordWrap.from("abcdefgh").maxWidth(4).insertHyphens(false).wrap();
        assertEquals("abcd\nefgh", result);
    }

    @Test
    public void leavesLongWordsUnbrokenWhenDisabled() {
        String result = WordWrap.from("abcdefgh").maxWidth(4).breakWords(false).wrap();
        assertEquals("abcdefgh", result);
    }

    @Test
    public void usesConfiguredNewLine() {
        String result = WordWrap.from("one two").maxWidth(3).newLine("|").wrap();
        assertEquals("one|two", result);
    }

    @Test
    public void wrapsEachInputLine() {
        String result = WordWrap.from("one two\nthree").maxWidth(5).wrap();
        assertEquals("one\ntwo\nthree", result);
    }

    @Test
    public void emptyInputProducesEmptyOutput() {
        String result = WordWrap.from("").wrap();
        assertEquals("", result);
    }

    @Test
    public void wrapToListReturnsLines() {
        assertEquals(Arrays.asList("one", "two"),
                WordWrap.from("one two").maxWidth(3).wrapToList());
    }

    @Test
    public void customWidthFunctionControlsWrapping() {
        String result = WordWrap.from("ab cd").maxWidth(3)
                .stringWidth(s -> s.length() * 2).wrap();
        assertEquals("ab\ncd", result);
    }

    @Test
    public void extraWordCharactersKeepPunctuationInWords() {
        String result = WordWrap.from("ab-cd").maxWidth(3).extraWordChars("-").wrap();
        assertEquals("ab-\ncd", result);
    }

    @Test
    public void excludedWordCharactersBecomeBoundaries() {
        String result = WordWrap.from("ab-cd").maxWidth(3).excludeExtraWordChars("-").wrap();
        assertEquals("ab-\ncd", result);
    }

    @Test
    public void rightTrimRemovesTrailingWhitespace() {
        assertEquals("text", WordWrap.rightTrim("text \t").toString());
    }

    @Test
    public void rightTrimReturnsOriginalWhenNoWhitespaceAtEnd() {
        CharSequence value = "text";
        assertSame(value, WordWrap.rightTrim(value));
    }

    @Test
    public void isWhitespaceRecognizesWhitespace() {
        assertTrue(WordWrap.isWhitespace(" \t\n"));
    }

    @Test
    public void isWhitespaceRejectsNonWhitespace() {
        assertFalse(WordWrap.isWhitespace(" x "));
    }

    @Test
    public void defaultSpecialWordCharactersContainQuestionMark() {
        assertTrue(WordWrap.SPECIAL_WORD_CHARS_SET_DEFAULT.contains(Character.valueOf('?')));
    }

    @Test
    public void customExtraWordCharacterSetIsAccepted() {
        Set<Character> chars = new HashSet<Character>();
        chars.add(Character.valueOf('-'));
        String result = WordWrap.from("ab-cd").maxWidth(3).extraWordChars(chars).wrap();
        assertEquals("ab-\ncd", result);
    }

    @Test
    public void readerInputCanBeWrapped() {
        String result = WordWrap.from(new StringReader("one two")).maxWidth(3).wrap();
        assertEquals("one\ntwo", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroMaximumWidth() {
        WordWrap.from("text").maxWidth(0);
    }
}
