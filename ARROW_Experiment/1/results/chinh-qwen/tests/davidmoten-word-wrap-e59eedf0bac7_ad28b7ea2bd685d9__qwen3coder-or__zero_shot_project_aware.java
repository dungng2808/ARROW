package org.davidmoten.text.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class WordWrap_ArrowTest {

    @Test
    public void testFromCharSequence() {
        String text = "This is a test sentence.";
        String result = WordWrap.from(text).wrap();
        assertEquals("This is a test sentence.", result);
    }

    @Test
    public void testMaxWidth() {
        String text = "This is a very long sentence that should be wrapped.";
        String result = WordWrap.from(text).maxWidth(10).wrap();
        assertTrue(result.contains("\n"));
    }

    @Test
    public void testStringWidth() {
        String text = "aaa bbb ccc";
        String result = WordWrap.from(text).stringWidth(s -> s.length() / 3).wrap();
        assertEquals("aaa bbb ccc", result);
    }

    @Test
    public void testNewLine() {
        String text = "Line1\nLine2";
        String result = WordWrap.from(text).newLine("\r\n").wrap();
        assertTrue(result.contains("\r\n"));
    }

    @Test
    public void testExtraWordChars() {
        String text = "Hello,world!";
        Set<Character> extraChars = WordWrap.SPECIAL_WORD_CHARS_SET_DEFAULT;
        String result = WordWrap.from(text).extraWordChars(extraChars).wrap();
        assertEquals("Hello,world!", result);
    }

    @Test
    public void testIncludeExtraWordChars() {
        String text = "Hello-world";
        String result = WordWrap.from(text).includeExtraWordChars("-").wrap();
        assertEquals("Hello-world", result);
    }

    @Test
    public void testExcludeExtraWordChars() {
        String text = "Hello,world";
        String result = WordWrap.from(text).excludeExtraWordChars(",").wrap();
        assertEquals("Hello,world", result);
    }

    @Test
    public void testInsertHyphens() {
        String text = "supercalifragilisticexpialidocious";
        String result = WordWrap.from(text).maxWidth(5).insertHyphens(true).wrap();
        assertTrue(result.contains("-"));
    }

    @Test
    public void testBreakWords() {
        String text = "supercalifragilisticexpialidocious";
        String result = WordWrap.from(text).maxWidth(5).breakWords(false).wrap();
        assertTrue(result.contains("supercalifragilisticexpialidocious"));
    }

    @Test
    public void testWrapToList() {
        String text = "Line1\nLine2";
        List<String> result = WordWrap.from(text).wrapToList();
        assertEquals(2, result.size());
        assertEquals("Line1", result.get(0));
        assertEquals("Line2", result.get(1));
    }

    @Test
    public void testWrapToFile() throws IOException {
        String text = "Hello World";
        File tempFile = File.createTempFile("test", ".txt");
        try {
            WordWrap.from(text).wrap(tempFile, StandardCharsets.UTF_8);
            String result = readFile(tempFile);
            assertEquals("Hello World", result);
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
    public void testWrapWithSpecialCharacters() {
        String text = "Hello \"world\"!";
        String result = WordWrap.from(text).wrap();
        assertEquals("Hello \"world\"!", result);
    }

    @Test
    public void testWrapWithPunctuation() {
        String text = "Hello, world!";
        String result = WordWrap.from(text).wrap();
        assertEquals("Hello, world!", result);
    }

    @Test
    public void testWrapWithNewlines() {
        String text = "Line1\nLine2\nLine3";
        String result = WordWrap.from(text).wrap();
        assertEquals("Line1\nLine2\nLine3", result);
    }

    @Test
    public void testWrapWithCarriageReturn() {
        String text = "Line1\r\nLine2";
        String result = WordWrap.from(text).wrap();
        assertEquals("Line1\nLine2", result);
    }

    @Test
    public void testWrapWithMultipleSpaces() {
        String text = "Line   with   multiple   spaces";
        String result = WordWrap.from(text).wrap();
        assertEquals("Line with multiple spaces", result);
    }

    @Test
    public void testWrapWithTab() {
        String text = "Line\twith\ttab";
        String result = WordWrap.from(text).wrap();
        assertEquals("Line with tab", result);
    }

    @Test
    public void testWrapWithMixedWhitespace() {
        String text = "Line \t\n  with \t\n  mixed whitespace";
        String result = WordWrap.from(text).wrap();
        assertEquals("Line with mixed whitespace", result);
    }

    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (StringReader reader = new StringReader(new String(java.nio.file.Files.readAllBytes(file.toPath())))) {
            int c;
            while ((c = reader.read()) != -1) {
                content.append((char) c);
            }
        }
        return content.toString();
    }
}
