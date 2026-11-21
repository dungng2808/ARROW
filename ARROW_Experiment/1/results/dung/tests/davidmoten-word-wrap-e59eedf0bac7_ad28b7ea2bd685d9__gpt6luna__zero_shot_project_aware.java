package org.davidmoten.text.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class WordWrap_ArrowTest {

    @Test
    public void wrapsTextAtConfiguredWidth() {
        List<String> lines = WordWrap.from("one two three").maxWidth(7).wrapToList();

        assertEquals(Arrays.asList("one two", "three"), lines);
    }

    @Test
    public void preservesExplicitLineBreaks() {
        List<String> lines = WordWrap.from("one\ntwo").wrapToList();

        assertEquals(Arrays.asList("one", "two"), lines);
    }

    @Test
    public void emptyInputProducesNoLines() {
        List<String> lines = WordWrap.from("").wrapToList();

        assertTrue(lines.isEmpty());
    }

    @Test
    public void whitespaceOnlyInputProducesNoLines() {
        List<String> lines = WordWrap.from("   ").wrapToList();

        assertTrue(lines.isEmpty());
    }

    @Test
    public void wrapsLongWordsWithHyphensByDefault() {
        List<String> lines = WordWrap.from("abcdefgh").maxWidth(4).wrapToList();

        assertEquals(Arrays.asList("ab-", "cd-", "efgh"), lines);
    }

    @Test
    public void wrapsLongWordsWithoutHyphensWhenDisabled() {
        List<String> lines = WordWrap.from("abcdefgh").maxWidth(4).insertHyphens(false).wrapToList();

        assertEquals(Arrays.asList("abcd", "efgh"), lines);
    }

    @Test
    public void doesNotBreakLongWordsWhenDisabled() {
        List<String> lines = WordWrap.from("abcdefgh").maxWidth(4).breakWords(false).wrapToList();

        assertEquals(Collections.singletonList("abcdefgh"), lines);
    }

    @Test
    public void usesCustomWidthFunction() {
        List<String> lines = WordWrap.from("ab cd").maxWidth(2)
                .stringWidth(s -> s.toString().replace(" ", "").length()).wrapToList();

        assertEquals(Arrays.asList("ab", "cd"), lines);
    }

    @Test
    public void usesConfiguredNewLine() {
        String result = WordWrap.from("one two").maxWidth(3).newLine("|").wrap();

        assertEquals("one|two", result);
    }

    @Test
    public void treatsIncludedCharacterAsPartOfWord() {
        List<String> lines = WordWrap.from("ab-cd").maxWidth(3).includeExtraWordChars("-").wrapToList();

        assertEquals(Arrays.asList("ab-", "cd"), lines);
    }

    @Test
    public void treatsExcludedCharacterAsWordBoundary() {
        List<String> lines = WordWrap.from("ab-cd").maxWidth(3).excludeExtraWordChars("-").wrapToList();

        assertEquals(Arrays.asList("ab-", "cd"), lines);
    }

    @Test
    public void acceptsCustomExtraWordCharacterSet() {
        List<String> lines = WordWrap.from("ab-cd").maxWidth(3)
                .extraWordChars(new HashSet<Character>(Arrays.asList(Character.valueOf('-'))))
                .wrapToList();

        assertEquals(Arrays.asList("ab-", "cd"), lines);
    }

    @Test
    public void maxWidthReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.maxWidth(10));
    }

    @Test
    public void stringWidthReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.stringWidth(s -> s.length()));
    }

    @Test
    public void newLineReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.newLine("\r\n"));
    }

    @Test
    public void extraWordCharsStringReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.extraWordChars("-"));
    }

    @Test
    public void includeExtraWordCharsReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.includeExtraWordChars("-"));
    }

    @Test
    public void excludeExtraWordCharsReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.excludeExtraWordChars("-"));
    }

    @Test
    public void insertHyphensReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.insertHyphens(false));
    }

    @Test
    public void breakWordsReturnsSameBuilder() {
        WordWrap.Builder builder = WordWrap.from("text");

        assertSame(builder, builder.breakWords(false));
    }

    @Test
    public void carriageReturnsAreIgnored() {
        String result = WordWrap.from("one\r\ntwo").wrap();

        assertEquals("one\ntwo", result);
    }

    @Test
    public void defaultSpecialWordCharactersIncludeApostrophe() {
        assertTrue(WordWrap.SPECIAL_WORD_CHARS_SET_DEFAULT.contains(Character.valueOf('\'')));
    }

    @Test
    public void defaultSpecialWordCharactersDoNotIncludeHyphen() {
        assertFalse(WordWrap.SPECIAL_WORD_CHARS_SET_DEFAULT.contains(Character.valueOf('-')));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroMaximumWidth() {
        WordWrap.from("text").maxWidth(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeMaximumWidth() {
        WordWrap.from("text").maxWidth(-1);
    }

    @Test
    public void readerInputCanBeWrapped() {
        String result = WordWrap.from(new StringReader("hello world")).maxWidth(5).wrap();

        assertEquals("hello\nworld", result);
    }
}
