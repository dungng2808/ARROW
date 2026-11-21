package org.fusesource.jansi;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnsiRenderer_ArrowTest {

    @Test
    public void testRenderSimpleText() throws Exception {
        String input = "Hello World";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderWithSingleCode() throws Exception {
        String input = "@|bold Hello|@";
        String result = AnsiRenderer.render(input);
        assertNotNull(result);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderWithMultipleCodes() throws Exception {
        String input = "@|bold,red Warning!|@";
        String result = AnsiRenderer.render(input);
        assertNotNull(result);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("Warning!"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testRenderNoBeginToken() throws Exception {
        String input = "No tokens here";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderUnmatchedBeginToken() throws Exception {
        String input = "@|bold Hello";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderUnmatchedEndToken() throws Exception {
        String input = "Hello |@";
        String result = AnsiRenderer.render(input);
        assertEquals(input, result);
    }

    @Test
    public void testRenderInvalidOrder() throws Exception {
        String input = "|@invalid@|";
        try {
            AnsiRenderer.render(input);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRenderEmptyString() throws Exception {
        String input = "";
        String result = AnsiRenderer.render(input);
        assertEquals("", result);
    }

    @Test
    public void testRenderOnlyTokens() throws Exception {
        String input = "@||@";
        String result = AnsiRenderer.render(input);
        assertEquals("", result);
    }

    @Test
    public void testRenderCodesArray() throws Exception {
        String[] codes = {"bold", "red"};
        String result = AnsiRenderer.renderCodes(codes);
        assertNotNull(result);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("\u001B[31m"));
    }

    @Test
    public void testRenderCodesString() throws Exception {
        String codes = "bold red";
        String result = AnsiRenderer.renderCodes(codes);
        assertNotNull(result);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("\u001B[31m"));
    }

    @Test
    public void testRenderTextWithCodes() throws Exception {
        String text = "Hello";
        String[] codes = {"bold", "red"};
        String result = AnsiRenderer.render(text, codes);
        assertNotNull(result);
        assertTrue(result.contains("\u001B[1m"));
        assertTrue(result.contains("\u001B[31m"));
        assertTrue(result.contains("Hello"));
        assertTrue(result.contains("\u001B[0m"));
    }

    @Test
    public void testTestMethod() {
        assertTrue(AnsiRenderer.test("@|bold Hello|@"));
        assertFalse(AnsiRenderer.test("Hello World"));
        assertFalse(AnsiRenderer.test(null));
    }

    @Test
    public void testCodeEnumValues() {
        AnsiRenderer.Code[] codes = AnsiRenderer.Code.values();
        assertNotNull(codes);
        assertTrue(codes.length > 0);
    }

    @Test
    public void testCodeIsColor() {
        assertTrue(AnsiRenderer.Code.RED.isColor());
        assertTrue(AnsiRenderer.Code.FG_RED.isColor());
        assertTrue(AnsiRenderer.Code.BG_RED.isColor());
        assertFalse(AnsiRenderer.Code.BOLD.isColor());
    }

    @Test
    public void testCodeGetColor() {
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.RED.getColor());
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.FG_RED.getColor());
        assertEquals(Ansi.Color.RED, AnsiRenderer.Code.BG_RED.getColor());
    }

    @Test
    public void testCodeIsAttribute() {
        assertTrue(AnsiRenderer.Code.BOLD.isAttribute());
        assertTrue(AnsiRenderer.Code.UNDERLINE.isAttribute());
        assertFalse(AnsiRenderer.Code.RED.isAttribute());
    }

    @Test
    public void testCodeGetAttribute() {
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, AnsiRenderer.Code.BOLD.getAttribute());
        assertEquals(Ansi.Attribute.UNDERLINE, AnsiRenderer.Code.UNDERLINE.getAttribute());
    }

    @Test
    public void testCodeIsBackground() {
        assertFalse(AnsiRenderer.Code.RED.isBackground());
        assertTrue(AnsiRenderer.Code.BG_RED.isBackground());
        assertFalse(AnsiRenderer.Code.FG_RED.isBackground());
    }
}
