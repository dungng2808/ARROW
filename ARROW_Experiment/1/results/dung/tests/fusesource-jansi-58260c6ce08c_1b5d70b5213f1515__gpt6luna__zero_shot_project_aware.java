package org.fusesource.jansi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class AnsiRenderer_ArrowTest {

    @Test
    public void renderPlainText() {
        assertEquals("plain text", AnsiRenderer.render("plain text"));
    }

    @Test
    public void renderEmbeddedBoldText() {
        assertEquals("\u001B[1mHello\u001B[0m", AnsiRenderer.render("@|bold Hello|@"));
    }

    @Test
    public void renderMultipleEmbeddedCodes() {
        assertEquals("\u001B[1;31mWarning!\u001B[0m", AnsiRenderer.render("@|bold,red Warning!|@"));
    }

    @Test
    public void renderPreservesTextAroundEmbeddedCode() {
        assertEquals("before \u001B[31malert\u001B[0m after",
                AnsiRenderer.render("before @|red alert|@ after"));
    }

    @Test
    public void renderUnclosedBeginTokenPreservesInput() {
        assertEquals("before @|red alert", AnsiRenderer.render("before @|red alert"));
    }

    @Test
    public void renderCodeWithoutTextSeparatorPreservesInput() {
        assertEquals("@|red|@", AnsiRenderer.render("@|red|@"));
    }

    @Test
    public void renderEndTokenBeforeBeginTokenThrows() {
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.render("|@@|red text|@"));
    }

    @Test
    public void renderToAppendableReturnsSameTargetAndAppendsResult() throws IOException {
        StringBuilder target = new StringBuilder("prefix:");
        Appendable result = AnsiRenderer.render("@|red alert|@", target);

        assertSame(target, result);
        assertEquals("prefix:\u001B[31malert\u001B[0m", target.toString());
    }

    @Test
    public void renderTextWithCodes() {
        assertEquals("\u001B[31mhello\u001B[0m", AnsiRenderer.render("hello", "red"));
    }

    @Test
    public void renderCodesFromVarargs() {
        assertEquals("\u001B[1;31m", AnsiRenderer.renderCodes("bold", "red"));
    }

    @Test
    public void renderCodesFromWhitespaceSeparatedString() {
        assertEquals("\u001B[1;31m", AnsiRenderer.renderCodes("bold red"));
    }

    @Test
    public void renderUnknownCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> AnsiRenderer.renderCodes("unknown"));
    }

    @Test
    public void testDetectsBeginToken() {
        assertTrue(AnsiRenderer.test("text @|red value|@"));
    }

    @Test
    public void testReturnsFalseWithoutBeginToken() {
        assertFalse(AnsiRenderer.test("ordinary text"));
    }

    @Test
    public void testReturnsFalseForNull() {
        assertFalse(AnsiRenderer.test(null));
    }

    @Test
    public void codePropertiesIdentifyBackgroundColor() {
        AnsiRenderer.Code code = AnsiRenderer.Code.BG_RED;

        assertTrue(code.isColor());
        assertFalse(code.isAttribute());
        assertTrue(code.isBackground());
        assertEquals(Ansi.Color.RED, code.getColor());
    }

    @Test
    public void codePropertiesIdentifyAttribute() {
        AnsiRenderer.Code code = AnsiRenderer.Code.BOLD;

        assertFalse(code.isColor());
        assertTrue(code.isAttribute());
        assertFalse(code.isBackground());
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, code.getAttribute());
    }

    @Test
    public void foregroundCodeIsNotBackground() {
        AnsiRenderer.Code code = AnsiRenderer.Code.FG_BLUE;

        assertTrue(code.isColor());
        assertFalse(code.isBackground());
        assertEquals(Ansi.Color.BLUE, code.getColor());
    }
}
