package at.redeye.FrameWork.utilities;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringUtils_ArrowTest {

    @Test
    public void skipSpacesSkipsWhitespaceFromPosition() {
        StringBuilder text = new StringBuilder("  \tword");
        assertEquals(3, StringUtils.skip_spaces(text, 0));
    }

    @Test
    public void skipSpacesReverseSkipsWhitespaceFromPosition() {
        StringBuilder text = new StringBuilder("word  ");
        assertEquals(3, StringUtils.skip_spaces_reverse(text, 5));
    }

    @Test
    public void isSpaceRecognizesWhitespaceCharacters() {
        assertTrue(StringUtils.is_space('\t'));
        assertFalse(StringUtils.is_space('a'));
    }

    @Test
    public void splitStringIncludesEmptyParts() {
        List<String> parts = StringUtils.split_str(new StringBuilder("a,,b,"), ",");
        assertEquals(Arrays.asList("a", "", "b", ""), parts);
    }

    @Test
    public void stripRemovesCharactersFromBothEnds() {
        assertEquals("word", StringUtils.strip("xxwordxx", "x"));
    }

    @Test
    public void stripPostRemovesCharactersFromEnd() {
        assertEquals("word", StringUtils.strip_post("wordxx", "x"));
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
    public void formatDoubleRemovesTrailingZeros() {
        assertEquals("12.25", StringUtils.formatDouble(12.25));
    }

    @Test
    public void isYesRecognizesSupportedValues() {
        assertTrue(StringUtils.isYes("YES"));
        assertFalse(StringUtils.isYes("no"));
        assertFalse(StringUtils.isYes(null));
    }

    @Test
    public void skipLeadingLinesRemovesRequestedLines() {
        assertEquals("third", StringUtils.skipLeadingLines("first\nsecond\nthird", 2));
    }

    @Test
    public void skipLeadingLinesWithNonPositiveCountReturnsOriginal() {
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
