package org.davidmoten.text.utils;

import com.github.davidmoten.guavamini.Preconditions;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

public class WordWrap_ArrowTest {

    @Test
    public void testFromCharSequence() {
        String text = "This is a test sentence.";
        String result = WordWrap.from(text).wrap();
        assertEquals("This is a test sentence.", result);
    }

    @Test
    public void testFromCharSequenceWithWrap() {
        String text = "This is a very long sentence that should be wrapped.";
        String result = WordWrap.from(text).maxWidth(10).wrap();
        assertEquals("This is a\nvery long\nsentence that\nshould be\nwrapped.", result);
    }

    @Test
    public void testFromCharSequenceWithHyphens() {
        String text = "Supercalifragilisticexpialidocious is a very long word.";
        String result = WordWrap.from(text).maxWidth(10).insertHyphens(true).wrap();
        assertEquals("Supercalifragilisticexpialidocious is a\nvery long word.", result);
    }

    @Test
    public void testFromCharSequenceNoBreakWords() {
        String text = "Supercalifragilisticexpialidocious is a very long word.";
        String result = WordWrap.from(text).maxWidth(10).breakWords(false).wrap();
        assertEquals("Supercalifragilisticexpialidocious is a\nvery long word.", result);
    }

    @Test
    public void testFromCharSequenceWithCustomNewLine() {
        String text = "Line1\nLine2";
        String result = WordWrap.from(text).newLine("\r\n").wrap();
        assertEquals("Line1\r\nLine2", result);
    }

    @Test
    public void testFromCharSequenceWithExtraWordChars() {
        String text = "Hello,world!";
        String result = WordWrap.from(text).extraWordChars(",").wrap();
        assertEquals("Hello,world!", result);
    }

    @Test
    public void testFromCharSequenceWithIncludeExtraWordChars() {
        String text = "Hello-world";
        String result = WordWrap.from(text).includeExtraWordChars("-").wrap();
        assertEquals("Hello-world", result);
    }

    @Test
    public void testFromCharSequenceWithExcludeExtraWordChars() {
        String text = "Hello,world";
        String result = WordWrap.from(text).excludeExtraWordChars(",").wrap();
        assertEquals("Hello,\nworld", result);
    }

    @Test
    public void testFromCharSequenceWithCustomStringWidth() {
        String text = "a";
        String result = WordWrap.from(text).stringWidth(s -> 10).maxWidth(5).wrap();
        assertEquals("a", result);
    }

    @Test
    public void testWrapToList() {
        String text = "Line1\nLine2";
        List<String> result = WordWrap.from(text).wrapToList();
        assertEquals(Arrays.asList("Line1", "Line2"), result);
    }

    @Test
    public void testWrapToFile() throws IOException {
        String text = "Hello World";
        File tempFile = File.createTempFile("test", ".txt");
        try {
            WordWrap.from(text).wrap(tempFile, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            String result = reader.readLine();
            assertEquals("Hello World", result);
            reader.close();
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testWrapToString() {
        String text = "Hello World";
        String result = WordWrap.from(text).wrap();
        assertEquals("Hello World", result);
    }

    @Test
    public void testMaxWidthZero() {
        try {
            WordWrap.from("test").maxWidth(0);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testMaxWidthNegative() {
        try {
            WordWrap.from("test").maxWidth(-1);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSpecialWordCharsSet() {
        Set<Character> specialChars = WordWrap.SPECIAL_WORD_CHARS_SET_DEFAULT;
        assertTrue(specialChars.contains('\"'));
        assertTrue(specialChars.contains('\''));
        assertTrue(specialChars.contains('?'));
        assertTrue(specialChars.contains('.'));
        assertTrue(specialChars.contains('/'));
        assertTrue(specialChars.contains('!'));
        assertTrue(specialChars.contains(','));
        assertTrue(specialChars.contains(';'));
        assertTrue(specialChars.contains(':'));
        assertTrue(specialChars.contains('_'));
    }

    @Test
    public void testToSet() {
        String chars = "abc";
        Set<Character> result = WordWrap.toSet(chars);
        assertTrue(result.contains('a'));
        assertTrue(result.contains('b'));
        assertTrue(result.contains('c'));
        assertEquals(3, result.size());
    }

    @Test
    public void testIsPunctuation() {
        assertTrue(WordWrap.isPunctuation('!'));
        assertTrue(WordWrap.isPunctuation('.'));
        assertTrue(WordWrap.isPunctuation(','));
        assertFalse(WordWrap.isPunctuation('a'));
    }

    @Test
    public void testTooLong() {
        Function<CharSequence, Number> widthFunction = s -> s.length();
        assertTrue(WordWrap.tooLong(widthFunction, "hello", 3.0));
        assertFalse(WordWrap.tooLong(widthFunction, "hi", 3.0));
    }

    @Test
    public void testRightTrim() {
        CharSequence result = WordWrap.rightTrim("  hello  ");
        assertEquals("  hello", result.toString());
    }

    @Test
    public void testIsWhitespace() {
        assertTrue(WordWrap.isWhitespace("   "));
        assertFalse(WordWrap.isWhitespace(" a "));
    }

    @Test
    public void testLeftTrim() {
        StringBuilder2 sb = new StringBuilder2("  hello");
        WordWrap.leftTrim(sb);
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testAppendWordToLine() {
        StringBuilder2 line = new StringBuilder2("hello ");
        StringBuilder2 word = new StringBuilder2("world");
        WordWrap.appendWordToLine(line, word);
        assertEquals("hello world", line.toString());
        assertEquals(0, word.length());
    }

    @Test
    public void testWriteBrokenWord() throws IOException {
        StringBuilder2 word = new StringBuilder2("hello");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        LineConsumer consumer = new LineConsumer() {
            @Override
            public void write(char[] chars, int offset, int length) throws IOException {
                writer.write(chars, offset, length);
            }

            @Override
            public void writeNewLine() throws IOException {
                writer.write("\n");
            }

            @Override
            public void write(String s) throws IOException {
                writer.write(s);
            }
        };

        WordWrap.writeBrokenWord(consumer, word, true);
        writer.flush();
        String result = baos.toString();
        assertEquals("hell\no", result);
    }

    @Test
    public void testWriteLine() throws IOException {
        StringBuilder2 line = new StringBuilder2("hello");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
        LineConsumer consumer = new LineConsumer() {
            @Override
            public void write(char[] chars, int offset, int length) throws IOException {
                writer.write(chars, offset, length);
            }

            @Override
            public void writeNewLine() throws IOException {
                writer.write("\n");
            }

            @Override
            public void write(String s) throws IOException {
                writer.write(s);
            }
        };

        WordWrap.writeLine(consumer, line);
        writer.flush();
        String result = baos.toString();
        assertEquals("hello\n", result);
    }

    @Test
    public void testClose() {
        try {
            WordWrap.close(new StringReader(""));
        } catch (IORuntimeException e) {
            fail("Should not throw exception");
        }
    }

    @Test
    public void testConcatRightTrim() {
        CharSequence result = WordWrap.concatRightTrim("hello ", " world");
        assertEquals("hello  world", result.toString());
    }

    // Helper class for testing
    private static class StringBuilder2 {
        private final StringBuilder sb;

        public StringBuilder2() {
            this.sb = new StringBuilder();
        }

        public StringBuilder2(String s) {
            this.sb = new StringBuilder(s);
        }

        public void append(char c) {
            sb.append(c);
        }

        public void append(CharSequence csq) {
            sb.append(csq);
        }

        public void setLength(int newLength) {
            sb.setLength(newLength);
        }

        public int length() {
            return sb.length();
        }

        public char charAt(int index) {
            return sb.charAt(index);
        }

        public CharSequence subSequence(int start, int end) {
            return sb.subSequence(start, end);
        }

        public void delete(int start, int end) {
            sb.delete(start, end);
        }

        public String toString() {
            return sb.toString();
        }

        public char[] internalArray() {
            return sb.toString().toCharArray();
        }

        public String substring(int start, int end) {
            return sb.substring(start, end);
        }
    }

    // Helper interface for testing
    private interface LineConsumer {
        void write(char[] chars, int offset, int length) throws IOException;
        void writeNewLine() throws IOException;
        void write(String s) throws IOException;
    }

    // Helper exception class
    private static class IORuntimeException extends RuntimeException {
        public IORuntimeException(IOException cause) {
            super(cause);
        }
    }
}
