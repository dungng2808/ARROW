package org.fusesource.jansi;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class AnsiRenderer_ArrowTest {

    @Test
    public void testRenderSimpleText() {
        String input = "Hello World";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderWithBold() {
        String input = "@|bold Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithRed() {
        String input = "@|red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithBoldAndRed() {
        String input = "@|bold,red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1;31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithBackground() {
        String input = "@|bg_red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[41m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithMultipleCodes() {
        String input = "@|bold,underline,red Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1;4;31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithNoCodes() {
        String input = "@| Hello|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("@| Hello|@"));
    }

    @Test
    public void testRenderWithUnknownCode() {
        String input = "@|unknown Hello|@";
        try {
            AnsiRenderer.render(input);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRenderWithInvalidSyntax() {
        String input = "@|bold Hello"; // Missing end token
        try {
            AnsiRenderer.render(input);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRenderWithEndTokenBeforeBegin() {
        String input = "|@bold Hello|@";
        try {
            AnsiRenderer.render(input);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRenderWithEmptyInput() {
        String input = "";
        String result = AnsiRenderer.render(input);
        assertEquals("", result);
    }

    @Test
    public void testRenderWithOnlyTokens() {
        String input = "@||@";
        String result = AnsiRenderer.render(input);
        assertEquals("", result);
    }

    @Test
    public void testRenderWithNoTextAfterCode() {
        String input = "@|bold|@";
        String result = AnsiRenderer.render(input);
        assertEquals("", result);
    }

    @Test
    public void testRenderWithSpecialCharacters() {
        String input = "@|bold Hello\nWorld|@";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("World"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithMultipleSections() {
        String input = "Start @|bold Hello|@ middle @|red World|@ end";
        String result = AnsiRenderer.render(input);
        assertTrue(result.contains("Start "));
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
        assertTrue(result.contains(" middle "));
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("World"));
        assertTrue(result.contains("\u001B[0m"));
        assertTrue(result.contains(" end"));
    }

    @Test
    public void testRenderAppendable() throws IOException {
        String input = "@|bold Hello|@";
        StringWriter writer = new StringWriter();
        AnsiRenderer.render(input, writer);
        String result = writer.toString();
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderCodes() {
        String result = AnsiRenderer.renderCodes("bold", "red");
        assertTrue(result.contains("\u001B[1;31m"));
    }

    @Test
    public void testRenderCodesFromString() {
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
        assertEquals(AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.valueOf("BG_RED"));
        assertEquals(AnsiRenderer.Code.RESET, AnsiRenderer.Code.valueOf("RESET"));
    }

    @Test
    public void testCodeIsColor() {
        assertTrue(AnsiRenderer.Code.RED.isColor());
        assertTrue(AnsiRenderer.Code.FG_RED.isColor());
        assertTrue(AnsiRenderer.Code.BG_RED.isColor());
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
        assertFalse(AnsiRenderer.Code.RED.isBackground());
        assertTrue(AnsiRenderer.Code.BG_RED.isBackground());
        assertFalse(AnsiRenderer.Code.FG_RED.isBackground());
    }

    @Test
    public void testCodeGetColor() {
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.RED.getColor());
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.FG_RED.getColor());
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.BG_RED.getColor());
    }

    @Test
    public void testCodeGetAttribute() {
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, AnsiRenderer.Code.BOLD.getAttribute());
        assertEquals(Ansi.Attribute.UNDERLINE, AnsiRenderer.Code.UNDERLINE.getAttribute());
    }
}
