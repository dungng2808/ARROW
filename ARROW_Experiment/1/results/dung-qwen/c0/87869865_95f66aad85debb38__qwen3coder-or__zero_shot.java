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
        
        s = new StringBuilder("abc");
        assertEquals(0, StringUtils.skip_char(s, " ", 0));
        
        s = new StringBuilder("   ");
        assertEquals(3, StringUtils.skip_char(s, " ", 0));
    }

    @Test
    public void testSkipCharReverse() {
        StringBuilder s = new StringBuilder("abc  ");
        assertEquals(2, StringUtils.skip_char_reverse(s, " ", 4));
        
        s = new StringBuilder("abc");
        assertEquals(2, StringUtils.skip_char_reverse(s, " ", 2));
        
        s = new StringBuilder("   ");
        assertEquals(0, StringUtils.skip_char_reverse(s, " ", 2));
    }

    @Test
    public void testSkipSpaces() {
        StringBuilder s = new StringBuilder("  abc");
        assertEquals(2, StringUtils.skip_spaces(s, 0));
        
        s = new StringBuilder("abc");
        assertEquals(0, StringUtils.skip_spaces(s, 0));
    }

    @Test
    public void testSkipSpacesReverse() {
        StringBuilder s = new StringBuilder("abc  ");
        assertEquals(2, StringUtils.skip_spaces_reverse(s, 4));
        
        s = new StringBuilder("abc");
        assertEquals(2, StringUtils.skip_spaces_reverse(s, 2));
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
        
        s = new StringBuilder("a");
        result = StringUtils.split_str(s, ",");
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));
    }

    @Test
    public void testStrip() {
        StringBuilder s = new StringBuilder("  abc  ");
        assertEquals("abc", StringUtils.strip(s, " "));
        
        s = new StringBuilder("abc");
        assertEquals("abc", StringUtils.strip(s, " "));
        
        s = new StringBuilder("   ");
        assertEquals("", StringUtils.strip(s, " "));
    }

    @Test
    public void testStripPost() {
        StringBuilder s = new StringBuilder("abc  ");
        assertEquals("abc", StringUtils.strip_post(s, " "));
        
        s = new StringBuilder("abc");
        assertEquals("abc", StringUtils.strip_post(s, " "));
        
        s = new StringBuilder("   ");
        assertEquals("", StringUtils.strip_post(s, " "));
    }

    @Test
    public void testStripPostWithString() {
        assertEquals("abc", StringUtils.strip_post("  abc  ", " "));
        assertEquals("abc", StringUtils.strip_post("abc", " "));
        assertEquals("", StringUtils.strip_post("   ", " "));
    }

    @Test
    public void testStripWithString() {
        assertEquals("abc", StringUtils.strip("  abc  ", " "));
        assertEquals("abc", StringUtils.strip("abc", " "));
        assertEquals("", StringUtils.strip("   ", " "));
    }

    @Test
    public void testSetAndGetDefaultAutoLineLenght() {
        int original = StringUtils.get_defaultAutoLineLenght();
        StringUtils.set_defaultAutoLineLenght(50);
        assertEquals(50, StringUtils.get_defaultAutoLineLenght());
        StringUtils.set_defaultAutoLineLenght(original);
    }

    @Test
    public void testAutoLineBreak() {
        String input = "This is a very long sentence that should be broken into multiple lines.";
        String result = StringUtils.autoLineBreak(input, 10);
        assertNotNull(result);
    }

    @Test
    public void testAutoLineBreakWithBuilder() {
        StringBuilder input = new StringBuilder("This is a very long sentence that should be broken into multiple lines.");
        String result = StringUtils.autoLineBreak(input, 10);
        assertNotNull(result);
    }

    @Test
    public void testAutoLineBreakWithDefault() {
        String input = "This is a very long sentence that should be broken into multiple lines.";
        String result = StringUtils.autoLineBreak(input);
        assertNotNull(result);
    }

    @Test
    public void testFormatDouble() {
        assertEquals("12.234", StringUtils.formatDouble(12.2340000, 3));
        assertEquals("12", StringUtils.formatDouble(12.000, 3));
        assertEquals("0.03", StringUtils.formatDouble(0.03, 2));
    }

    @Test
    public void testFormatDoubleWithoutRounding() {
        assertEquals("12.234", StringUtils.formatDouble(12.234));
        assertEquals("12", StringUtils.formatDouble(12.0));
        assertEquals("0.03", StringUtils.formatDouble(0.03));
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
        assertFalse(StringUtils.isYes(""));
    }

    @Test
    public void testSkipLeadingLines() {
        String input = "line1\nline2\nline3";
        assertEquals("line2\nline3", StringUtils.skipLeadingLines(input, 1));
        assertEquals("line3", StringUtils.skipLeadingLines(input, 2));
        assertEquals(input, StringUtils.skipLeadingLines(input, 0));
    }

    @Test
    public void testByteArrayToString() {
        byte[] data = {65, 66, 67};
        assertEquals("ABC", StringUtils.byteArrayToString(data));
        assertNull(StringUtils.byteArrayToString(null));
    }

    @Test
    public void testAddLineNumbers() {
        String input = "line1\nline2\nline3";
        String result = StringUtils.addLineNumbers(input);
        assertNotNull(result);
        assertTrue(result.contains("1: line1"));
        assertTrue(result.contains("2: line2"));
        assertTrue(result.contains("3: line3"));
    }
}
