package at.redeye.FrameWork.utilities;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtils_ArrowTest {

    @Test
    public void skipSpacesSkipsWhitespaceFromPosition() {
        assertEquals(3, StringUtils.skip_spaces(new StringBuilder(" \t\nx"), 0));
    }

    @Test
    public void skipSpacesReturnsPositionWhenNoWhitespaceIsPresent() {
        assertEquals(1, StringUtils.skip_spaces(new StringBuilder("ax"), 1));
    }

    @Test
    public void skipSpacesReverseSkipsWhitespaceBackwards() {
        assertEquals(1, StringUtils.skip_spaces_reverse(new StringBuilder("a \t\n"), 3));
    }

    @Test
    public void isSpaceRecognizesSupportedWhitespace() {
        assertTrue(StringUtils.is_space('\t'));
        assertFalse(StringUtils.is_space('\u000b'));
    }

    @Test
    public void splitStrSplitsAtEachDelimiter() {
        assertEquals(Arrays.asList("a", "b", "c"),
                StringUtils.split_str(new StringBuilder("a,b,c"), ","));
    }

    @Test
    public void splitStrPreservesEmptyParts() {
        assertEquals(Arrays.asList("", "a", ""),
                StringUtils.split_str(new StringBuilder(",a,"), ","));
    }

    @Test
    public void stripRemovesCharactersFromBothEnds() {
        assertEquals("abc", StringUtils.strip(new StringBuilder("xxabcxx"), "x"));
    }

    @Test
    public void stripReturnsEmptyWhenAllCharactersAreStripped() {
        assertEquals("", StringUtils.strip(new StringBuilder("xxx"), "x"));
    }

    @Test
    public void stripPostRemovesCharactersFromEnd() {
        assertEquals("abc", StringUtils.strip_post(new StringBuilder("abcxxx"), "x"));
    }

    @Test
    public void stripPostStringOverloadRemovesCharactersFromEnd() {
        assertEquals("abc", StringUtils.strip_post("abcxxx", "x"));
    }

    @Test(expected = NullPointerException.class)
    public void stripPostRejectsNullString() {
        StringUtils.strip_post((String) null, "x");
    }

    @Test
    public void stripStringOverloadRemovesCharactersFromBothEnds() {
        assertEquals("abc", StringUtils.strip("xxabcxx", "x"));
    }

    @Test
    public void autoLineBreakLeavesShortTextUnchanged() {
        assertEquals("short text", StringUtils.autoLineBreak("short text", 10));
    }

    @Test
    public void autoLineBreakBuilderOverloadLeavesShortTextUnchanged() {
        assertEquals("short text", StringUtils.autoLineBreak(new StringBuilder("short text"), 10));
    }

    @Test
    public void formatDoubleRemovesTrailingZerosAndDecimalPoint() {
        assertEquals("12.5", StringUtils.formatDouble(12.5));
        assertEquals("12", StringUtils.formatDouble(12.0));
    }

    @Test
    public void isYesRecognizesSupportedValuesIgnoringCase() {
        assertTrue(StringUtils.isYes("JA"));
        assertTrue(StringUtils.isYes("Yes"));
        assertTrue(StringUtils.isYes("TRUE"));
        assertTrue(StringUtils.isYes("1"));
        assertTrue(StringUtils.isYes("X"));
        assertTrue(StringUtils.isYes("+"));
    }

    @Test
    public void isYesRejectsNullAndOtherValues() {
        assertFalse(StringUtils.isYes(null));
        assertFalse(StringUtils.isYes("no"));
    }

    @Test
    public void skipLeadingLinesSkipsRequestedNewlines() {
        assertEquals("third", StringUtils.skipLeadingLines("first\nsecond\nthird", 2));
    }

    @Test
    public void skipLeadingLinesDoesNotSkipForNonPositiveCount() {
        assertEquals("first\nsecond", StringUtils.skipLeadingLines("first\nsecond", 0));
    }

    @Test
    public void byteArrayToStringConvertsBytesToCharacters() {
        assertEquals("ABC", StringUtils.byteArrayToString(new byte[]{65, 66, 67}));
    }

    @Test
    public void byteArrayToStringReturnsEmptyForNull() {
        assertEquals("", StringUtils.byteArrayToString(null));
    }

    @Test
    public void addLineNumbersPrefixesEachLine() {
        assertEquals("1: first\n2: second\n", StringUtils.addLineNumbers("first\nsecond"));
    }

    @Test
    public void addLineNumbersPadsNumbersToLineCountWidth() {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            if (i > 1) {
                text.append('\n');
            }
            text.append("line");
        }
        StringBuilder expected = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            expected.append(String.format("%02d: line%n", i));
        }
        assertEquals(expected.toString(), StringUtils.addLineNumbers(text.toString()));
    }
}
