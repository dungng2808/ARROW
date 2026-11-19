package at.redeye.FrameWork.utilities;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class StringUtils_ArrowTest {

    @Test
    public void skipSpacesSkipsWhitespaceFromPosition() {
        assertEquals(3, StringUtils.skip_spaces(new StringBuilder("  \tX"), 0));
    }

    @Test
    public void skipSpacesReturnsPositionAtNonWhitespace() {
        assertEquals(2, StringUtils.skip_spaces(new StringBuilder("ab c"), 2));
    }

    @Test
    public void skipSpacesReverseSkipsWhitespace() {
        assertEquals(1, StringUtils.skip_spaces_reverse(new StringBuilder("a  b"), 2));
    }

    @Test
    public void isSpaceRecognizesWhitespaceCharacters() {
        assertTrue(StringUtils.is_space('\t'));
        assertFalse(StringUtils.is_space('x'));
    }

    @Test
    public void splitStringIncludesEmptyParts() {
        List<String> result = StringUtils.split_str(new StringBuilder("a,,b,"), ",");
        assertEquals(Arrays.asList("a", "", "b", ""), result);
    }

    @Test
    public void stripRemovesCharactersFromBothEnds() {
        assertEquals("hello", StringUtils.strip("xxhellox", "x"));
    }

    @Test
    public void stripPostRemovesCharactersFromEnd() {
        assertEquals("hello", StringUtils.strip_post("helloxxx", "x"));
    }

    @Test
    public void stripPostRejectsNullString() {
        try {
            StringUtils.strip_post((String) null, "x");
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // Expected.
        }
    }

    @Test
    public void defaultAutoLineLengthIsForty() {
        assertEquals(40, StringUtils.get_defaultAutoLineLenght());
    }

    @Test
    public void autoLineBreakLeavesShortTextUnchanged() {
        assertEquals("short text", StringUtils.autoLineBreak("short text", 5));
    }

    @Test
    public void autoLineBreakAcceptsStringBuilder() {
        assertEquals("short text", StringUtils.autoLineBreak(new StringBuilder("short text"), 5));
    }

    @Test
    public void formatDoubleRemovesTrailingZeros() {
        assertEquals("12.25", StringUtils.formatDouble(12.25));
    }

    @Test
    public void isYesRecognizesSupportedValues() {
        assertTrue(StringUtils.isYes("YES"));
        assertTrue(StringUtils.isYes("ja"));
        assertTrue(StringUtils.isYes("true"));
        assertTrue(StringUtils.isYes("1"));
        assertTrue(StringUtils.isYes("x"));
        assertTrue(StringUtils.isYes("+"));
    }

    @Test
    public void isYesRejectsNullAndOtherValues() {
        assertFalse(StringUtils.isYes(null));
        assertFalse(StringUtils.isYes("no"));
    }

    @Test
    public void skipLeadingLinesRemovesRequestedLines() {
        assertEquals("third", StringUtils.skipLeadingLines("first\nsecond\nthird", 2));
    }

    @Test
    public void skipLeadingLinesLeavesTextWhenCountIsNotPositive() {
        assertEquals("first\nsecond", StringUtils.skipLeadingLines("first\nsecond", 0));
    }

    @Test
    public void byteArrayToStringConvertsBytes() {
        assertEquals("ABC", StringUtils.byteArrayToString(new byte[] {65, 66, 67}));
    }

    @Test
    public void byteArrayToStringReturnsEmptyForNull() {
        assertEquals("", StringUtils.byteArrayToString(null));
    }

    @Test
    public void addLineNumbersPrefixesEachLine() {
        assertEquals("1: first\n2: second\n", StringUtils.addLineNumbers("first\nsecond"));
    }
}
