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
    public void wrapsTextAtWordBoundaries() {
        assertEquals("one two\nthree", WordWrap.from("one two three").maxWidth(7).wrap());
    }

    @Test
    public void wrapsLongWordsWithHyphens() {
        assertEquals("abc-\ndef", WordWrap.from("abcdef").maxWidth(4).wrap());
    }

    @Test
    public void wrapsLongWordsWithoutHyphens() {
        assertEquals("abcd\nef", WordWrap.from("abcdef").maxWidth(4).insertHyphens(false).wrap());
    }

    @Test
    public void leavesLongWordsUnbrokenWhenDisabled() {
        assertEquals("abcdef", WordWrap.from("abcdef").maxWidth(4).breakWords(false).wrap());
    }

    @Test
    public void usesConfiguredNewLine() {
        assertEquals("one two|three",
                WordWrap.from("one two three").maxWidth(7).newLine("|").wrap());
    }

    @Test
    public void wrapsToList() {
        assertEquals(Arrays.asList("one two", "three"),
                WordWrap.from("one two three").maxWidth(7).wrapToList());
    }

    @Test
    public void emptyInputProducesNoLines() {
        assertTrue(WordWrap.from("").wrapToList().isEmpty());
    }

    @Test
    public void whitespaceOnlyInputProducesNoLines() {
        assertTrue(WordWrap.from("   ").wrapToList().isEmpty());
    }

    @Test
    public void preservesExplicitEmptyLine() {
        assertEquals(Arrays.asList(""), WordWrap.from("\n").wrapToList());
    }

    @Test
    public void ignoresCarriageReturns() {
        assertEquals("ab", WordWrap.from("a\rb").wrap());
    }

    @Test
    public void usesCustomStringWidth() {
        assertEquals("ab cd", WordWrap.from("ab cd").maxWidth(2)
                .stringWidth(s -> s.length() / 2).wrap());
    }

    @Test
    public void acceptsPositiveFractionalWidth() {
        assertEquals("a", WordWrap.from("a").maxWidth(0.5).wrap());
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
    public void extraWordCharactersKeepPunctuationInsideWords() {
        assertEquals("a-b", WordWrap.from("a-b").extraWordChars("-").wrap());
    }

    @Test
    public void excludingDefaultWordCharactersChangesBoundaries() {
        assertEquals("a-\nb", WordWrap.from("a-b").excludeExtraWordChars("-").maxWidth(2).wrap());
    }

    @Test
    public void includeExtraWordCharactersReturnsBuilder() {
        WordWrap.Builder builder = WordWrap.from("a-b");
        assertSame(builder, builder.includeExtraWordChars("#"));
    }

    @Test
    public void extraWordCharacterSetCanBeConfigured() {
        Set<Character> chars = new HashSet<Character>();
        chars.add(Character.valueOf('-'));
        assertEquals("a-b", WordWrap.from("a-b").extraWordChars(chars).wrap());
    }

    @Test
    public void rightTrimRemovesTrailingWhitespace() {
        assertEquals("text", WordWrap.rightTrim("text \t").toString());
    }

    @Test
    public void rightTrimReturnsOriginalWhenNoWhitespaceIsPresent() {
        CharSequence text = "text";
        assertSame(text, WordWrap.rightTrim(text));
    }

    @Test
    public void recognizesWhitespace() {
        assertTrue(WordWrap.isWhitespace(" \t\n"));
    }

    @Test
    public void emptySequenceIsWhitespace() {
        assertTrue(WordWrap.isWhitespace(""));
    }

    @Test
    public void recognizesNonWhitespace() {
        assertFalse(WordWrap.isWhitespace(" x "));
    }

    @Test
    public void closesReaderWhenConfigured() {
        final boolean[] closed = new boolean[1];
        StringReader reader = new StringReader("text") {
            @Override
            public void close() {
                closed[0] = true;
                super.close();
            }
        };
        WordWrap.from(reader, true).wrap();
        assertTrue(closed[0]);
    }

    @Test
    public void doesNotCloseReaderWhenNotConfigured() {
        final boolean[] closed = new boolean[1];
        StringReader reader = new StringReader("text") {
            @Override
            public void close() {
                closed[0] = true;
                super.close();
            }
        };
        WordWrap.from(reader).wrap();
        assertFalse(closed[0]);
    }
}
