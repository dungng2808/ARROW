package org.fusesource.jansi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnsiRenderer_ArrowTest {

    @Test
    public void testRenderSimpleText() throws Exception {
        String input = "Hello World";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderWithBold() throws Exception {
        String input = "@|bold Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithRed() throws Exception {
        String input = "@|red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithBoldAndRed() throws Exception {
        String input = "@|bold,red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1;31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithMultipleTokens() throws Exception {
        String input = "@|bold Hello|@ and @|red World|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithNoMatchingToken() throws Exception {
        String input = "@|invalid Hello|@";
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.render(input));
    }

    @Test
    public void testRenderWithInvalidSyntax() throws Exception {
        String input = "@|bold Hello"; // Missing end token
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.render(input));
    }

    @Test
    public void testRenderWithEmptyInput() throws Exception {
        String input = "";
        String result = AnsiRenderer.render(input);
        assertEquals("", result);
    }

    @Test
    public void testRenderWithOnlyBeginToken() throws Exception {
        String input = "@|";
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.render(input));
    }

    @Test
    public void testRenderWithOnlyEndToken() throws Exception {
        String input = "|@";
        String result = AnsiRenderer.render(input);
        assertEquals("|@", result);
    }

    @Test
    public void testRenderWithBeginBeforeEnd() throws Exception {
        String input = "|@|@|@";
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.render(input));
    }

    @Test
    public void testRenderWithNoTextAfterCode() throws Exception {
        String input = "@|bold|@";
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.render(input));
    }

    @Test
    public void testRenderCodes() throws Exception {
        String result = AnsiRenderer.renderCodes("bold", "red");
        assertTrue(result.contains("\u001B[1;31m"));
    }

    @Test
    public void testRenderCodesFromString() throws Exception {
        String result = AnsiRenderer.renderCodes("bold red");
        assertTrue(result.contains("\u001B[1;31m"));
    }

    @Test
    public void testTestMethod() {
        assertTrue(AnsiRenderer.test("@|bold Hello|@"));
        assertFalse(AnsiRenderer.test("Hello World"));
        assertFalse(AnsiRenderer.test(null));
    }

    @Test
    public void testCodeEnumValues() {
        assertNotNull(AnsiRenderer.Code.values());
        assertTrue(AnsiRenderer.Code.values().length > 0);
    }

    @Test
    public void testCodeIsColor() {
        assertTrue(AnsiRenderer.Code.RED.isColor());
        assertFalse(AnsiRenderer.Code.BOLD.isColor());
    }

    @Test
    public void testCodeGetColor() {
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.RED.getColor());
    }

    @Test
    public void testCodeIsAttribute() {
        assertTrue(AnsiRenderer.Code.BOLD.isAttribute());
        assertFalse(AnsiRenderer.Code.RED.isAttribute());
    }

    @Test
    public void testCodeGetAttribute() {
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, AnsiRenderer.Code.BOLD.getAttribute());
    }

    @Test
    public void testCodeIsBackground() {
        assertTrue(AnsiRenderer.Code.BG_RED.isBackground());
        assertFalse(AnsiRenderer.Code.RED.isBackground());
    }
}
