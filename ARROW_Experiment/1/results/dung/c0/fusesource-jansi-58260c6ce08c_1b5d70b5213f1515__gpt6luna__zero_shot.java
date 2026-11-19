package org.fusesource.jansi;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void renderEmbeddedForegroundAndBackgroundColors() {
        assertEquals("\u001B[31;44mWarning\u001B[0m",
                AnsiRenderer.render("@|red,bg_blue Warning|@"));
    }

    @Test
    public void renderUnclosedTokenUnchanged() {
        assertEquals("before @|bold Hello", AnsiRenderer.render("before @|bold Hello"));
    }

    @Test
    public void renderTokenWithoutTextSeparatorUnchanged() {
        assertEquals("@|bold|@", AnsiRenderer.render("@|bold|@"));
    }

    @Test
    public void renderMultipleEmbeddedTokens() {
        assertEquals("A\u001B[1mB\u001B[0mC\u001B[31mD\u001B[0mE",
                AnsiRenderer.render("A@|bold B|@C@|red D|@E"));
    }

    @Test
    public void renderTextWithCodes() {
        assertEquals("\u001B[32mHello\u001B[0m", AnsiRenderer.render("Hello", "green"));
    }

    @Test
    public void renderCodesFromNames() {
        assertEquals("\u001B[1;34m", AnsiRenderer.renderCodes("bold", "blue"));
    }

    @Test
    public void renderCodesFromSpaceSeparatedNames() {
        assertEquals("\u001B[1;34m", AnsiRenderer.renderCodes("bold blue"));
    }

    @Test
    public void renderIntoAppendableReturnsTarget() throws IOException {
        StringBuilder target = new StringBuilder("prefix:");
        Appendable result = AnsiRenderer.render("plain", target);
        assertSame(target, result);
        assertEquals("prefix:plain", target.toString());
    }

    @Test
    public void testDetectsBeginToken() {
        assertTrue(AnsiRenderer.test("text @|bold text|@"));
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
    public void codeReportsColorAndBackgroundProperties() {
        AnsiRenderer.Code code = AnsiRenderer.Code.BG_RED;
        assertTrue(code.isColor());
        assertFalse(code.isAttribute());
        assertTrue(code.isBackground());
        assertEquals(Ansi.Color.RED, code.getColor());
    }

    @Test
    public void codeReportsAttributeProperties() {
        AnsiRenderer.Code code = AnsiRenderer.Code.BOLD;
        assertFalse(code.isColor());
        assertTrue(code.isAttribute());
        assertFalse(code.isBackground());
        assertEquals(Ansi.Attribute.INTENSITY_BOLD, code.getAttribute());
    }

    @Test(expected = IllegalArgumentException.class)
    public void renderRejectsUnknownCode() {
        AnsiRenderer.render("text", "not_a_code");
    }

    @Test(expected = IllegalArgumentException.class)
    public void renderRejectsEndTokenBeforeBeginToken() throws IOException {
        AnsiRenderer.render("|@@|bold text|@", new StringBuilder());
    }
}
