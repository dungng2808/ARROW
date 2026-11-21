package org.fusesource.jansi;

import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AnsiRenderer_ArrowTest {

    @Test
    public void renderPlainText() {
        assertEquals("plain text", AnsiRenderer.render("plain text"));
    }

    @Test
    public void renderEmbeddedBoldText() {
        assertEquals("\u001B[1mbold\u001B[0m", AnsiRenderer.render("@|bold bold|@"));
    }

    @Test
    public void renderEmbeddedTextWithSurroundingText() {
        assertEquals("before \u001B[31mwarning\u001B[0m after",
                AnsiRenderer.render("before @|red warning|@ after"));
    }

    @Test
    public void renderUnclosedTokenAsOriginalInput() {
        assertEquals("before @|red warning", AnsiRenderer.render("before @|red warning"));
    }

    @Test
    public void renderTokenWithoutTextAsOriginalInput() {
        assertEquals("@|red|@", AnsiRenderer.render("@|red|@"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void renderEndTokenBeforeBeginToken() {
        AnsiRenderer.render("|@@|red text|@");
    }

    @Test
    public void renderIntoAppendable() throws IOException {
        StringBuilder target = new StringBuilder("prefix:");
        Appendable result = AnsiRenderer.render("text", target);
        assertSame(target, result);
        assertEquals("prefix:text", target.toString());
    }

    @Test
    public void renderTextWithCodes() {
        assertEquals("\u001B[31mwarning\u001B[0m", AnsiRenderer.render("warning", "RED"));
    }

    @Test
    public void renderCodesFromArray() {
        assertEquals("\u001B[1m\u001B[31m", AnsiRenderer.renderCodes("BOLD", "RED"));
    }

    @Test
    public void renderCodesFromSpaceSeparatedString() {
        assertEquals("\u001B[1m\u001B[31m", AnsiRenderer.renderCodes("BOLD RED"));
    }

    @Test
    public void testDetectsBeginToken() {
        assertTrue(AnsiRenderer.test("text @|red text|@"));
    }

    @Test
    public void testReturnsFalseWithoutBeginToken() {
        assertFalse(AnsiRenderer.test("plain text"));
    }

    @Test
    public void testReturnsFalseForNull() {
        assertFalse(AnsiRenderer.test(null));
    }

    @Test
    public void codeProperties() {
        AnsiRenderer.Code code = AnsiRenderer.Code.BG_RED;
        assertTrue(code.isColor());
        assertTrue(code.isBackground());
        assertFalse(code.isAttribute());
        assertEquals(Ansi.Color.RED, code.getColor());
    }

    @Test
    public void attributeCodeProperties() {
        AnsiRenderer.Code code = AnsiRenderer.Code.BOLD;
        assertFalse(code.isColor());
        assertFalse(code.isBackground());
        assertTrue(code.isAttribute());
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, code.getAttribute());
    }
}
