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
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithRed() throws Exception {
        String input = "@|red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithBoldAndRed() throws Exception {
        String input = "@|bold,red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1;31m"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithMultipleTokens() throws Exception {
        String input = "@|bold Hello|@ and @|red World|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithoutEndingToken() throws Exception {
        String input = "@|bold Hello";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderWithEndingBeforeBeginning() throws Exception {
        String input = "|@bold Hello|@";
        try {
            AnsiRenderer.render(input);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid input string found.", e.getMessage());
        }
    }

    @Test
    public void testRenderWithNoTextAfterCode() throws Exception {
        String input = "@|bold|@";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderEmptyString() throws Exception {
        String input = "";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderNull() throws Exception {
        try {
            AnsiRenderer.render(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getCause());
        }
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
        assertEquals(AnsiRenderer.Code.BOLD, AnsiRenderer.Code.valueOf("BOLD"));
        assertEquals(AnsiRenderer.Code.FG_RED, AnsiRenderer.Code.valueOf("FG_RED"));
        assertEquals(AnsiRenderer.Code.BG_WHITE, AnsiRenderer.Code.valueOf("BG_WHITE"));
        assertEquals(AnsiRenderer.Code.RESET, AnsiRenderer.Code.valueOf("RESET"));
    }

    @Test
    public void testCodeIsColor() {
        assertTrue(AnsiRenderer.Code.RED.isColor());
        assertTrue(AnsiRenderer.Code.FG_BLUE.isColor());
        assertTrue(AnsiRenderer.Code.BG_CYAN.isColor());
        assertFalse(AnsiRenderer.Code.BOLD.isColor());
    }

    @Test
    public void testCodeIsAttribute() {
        assertTrue(AnsiRenderer.Code.BOLD.isAttribute());
        assertTrue(AnsiRenderer.Code.UNDERLINE.isAttribute());
        assertFalse(AnsiRenderer.Code.RED.isAttribute());
    }

    @Test
    public void testCodeIsBackground() {
        assertTrue(AnsiRenderer.Code.BG_BLACK.isBackground());
        assertTrue(AnsiRenderer.Code.BG_WHITE.isBackground());
        assertFalse(AnsiRenderer.Code.FG_BLACK.isBackground());
        assertFalse(AnsiRenderer.Code.RED.isBackground());
    }

    @Test
    public void testCodeGetColor() {
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.RED.getColor());
        assertEquals(Ansi.Color.BLUE, AnsiRenderer.Code.FG_BLUE.getColor());
        assertEquals(Ansi.Color.CYAN, AnsiRenderer.Code.BG_CYAN.getColor());
    }

    @Test
    public void testCodeGetAttribute() {
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, AnsiRenderer.Code.BOLD.getAttribute());
        assertEquals(Ansi.Attribute.UNDERLINE, AnsiRenderer.Code.UNDERLINE.getAttribute());
    }
}
