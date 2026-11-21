package at.redeye.FrameWork.utilities;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class StringUtils_ArrowTest {

    @Test
    public void testContains() {
        assertTrue(StringUtils.contains('a', "abc"));
        assertFalse(StringUtils.contains('d', "abc"));
        assertFalse(StringUtils.contains('a', ""));
    }

    @Test
    public void testSkipChar() {
        StringBuilder s = new StringBuilder("  abc");
        assertEquals(3, StringUtils.skip_char(s, " ", 0));
        assertEquals(0, StringUtils.skip_char(s, " ", 3));
    }

    @Test
    public void testSkipCharReverse() {
        StringBuilder s = new StringBuilder("abc  ");
        assertEquals(2, StringUtils.skip_char_reverse(s, " ", 4));
        assertEquals(4, StringUtils.skip_char_reverse(s, " ", 2));
    }

    @Test
    public void testSkipSpaces() {
        StringBuilder s = new StringBuilder("  abc");
        assertEquals(2, StringUtils.skip_spaces(s, 0));
        assertEquals(0, StringUtils.skip_spaces(s, 2));
    }

    @Test
    public void testSkipSpacesReverse() {
        StringBuilder s = new StringBuilder("abc  ");
        assertEquals(2, StringUtils.skip_spaces_reverse(s, 4));
        assertEquals(4, StringUtils.skip_spaces_reverse(s, 2));
    }

    @Test
    public void testIsSpace() {
        assertTrue(StringUtils.is_space(' '));
        assertTrue(StringUtils.is_space('\t'));
        assertTrue(StringUtils.is_space('\n'));
        assertTrue(StringUtils.is_space('\r'));
        assertFalse(StringUtils.is_space('a'));
    }

    @Test
    public void testSplitStr() {
        StringBuilder s = new StringBuilder("a,b,c");
        List<String> result = StringUtils.split_str(s, ",");
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }

    @Test
    public void testStrip() {
        StringBuilder s = new StringBuilder("  abc  ");
        assertEquals("abc", StringUtils.strip(s, " "));
        assertEquals("abc", StringUtils.strip("  abc  ", " "));
    }

    @Test
    public void testStripPost() {
        StringBuilder s = new StringBuilder("abc  ");
        assertEquals("abc", StringUtils.strip_post(s, " "));
        assertEquals("abc", StringUtils.strip_post("abc  ", " "));
    }

    @Test
    public void testAutoLineBreak() {
        String input = "This is a very long sentence that should be broken automatically.";
        String result = StringUtils.autoLineBreak(input, 10);
        assertNotNull(result);
    }

    @Test
    public void testFormatDouble() {
        assertEquals("12.234", StringUtils.formatDouble(12.2340000, 3));
        assertEquals("0.03", StringUtils.formatDouble(0.03, 2));
        assertEquals("12", StringUtils.formatDouble(12.000, 3));
    }

    @Test
    public void testIsYes() {
        assertTrue(StringUtils.isYes("ja"));
        assertTrue(StringUtils.isYes("yes"));
        assertTrue(StringUtils.isYes("true"));
        assertTrue(StringUtils.isYes("1"));
        assertTrue(StringUtils.isYes("x"));
        assertTrue(StringUtils.isYes("+"));
        assertFalse(StringUtils.isYes("no"));
        assertFalse(StringUtils.isYes(null));
    }

    @Test
    public void testSkipLeadingLines() {
        String input = "line1\nline2\nline3";
        assertEquals("line3", StringUtils.skipLeadingLines(input, 2));
        assertEquals("line1\nline2\nline3", StringUtils.skipLeadingLines(input, 0));
    }

    @Test
    public void testByteArrayToString() {
        byte[] data = {'a', 'b', 'c'};
        assertEquals("abc", StringUtils.byteArrayToString(data));
        assertEquals("", StringUtils.byteArrayToString(null));
    }

    @Test
    public void testAddLineNumbers() {
        String input = "line1\nline2";
        String result = StringUtils.addLineNumbers(input);
        assertTrue(result.contains("1: line1"));
        assertTrue(result.contains("2: line2"));
    }
}
