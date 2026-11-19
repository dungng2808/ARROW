package net.sourceforge.pmd.lang.ast;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SourceCodePositioner_ArrowTest {

    @Test
    public void lineNumberFromOffsetAtLineStartsAndWithinLines() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef\nxy");

        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.lineNumberFromOffset(2));
        assertEquals(2, positioner.lineNumberFromOffset(4));
        assertEquals(3, positioner.lineNumberFromOffset(8));
    }

    @Test
    public void columnFromOffsetIsOneBased() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef");

        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(3, positioner.columnFromOffset(1, 2));
        assertEquals(2, positioner.columnFromOffset(2, 4));
    }

    @Test
    public void columnFromOffsetReturnsZeroForInvalidLineNumbers() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef");

        assertEquals(0, positioner.columnFromOffset(0, 0));
        assertEquals(0, positioner.columnFromOffset(3, 4));
    }

    @Test
    public void lastLineAndLastLineColumn() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef");

        assertEquals(2, positioner.getLastLine());
        assertEquals(3, positioner.getLastLineColumn());
    }

    @Test
    public void trailingNewlineDoesNotCreateAnAdditionalLine() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\n");

        assertEquals(1, positioner.getLastLine());
        assertEquals(4, positioner.getLastLineColumn());
    }

    @Test
    public void emptySourceHasOneLineAndZeroLastColumn() {
        SourceCodePositioner positioner = new SourceCodePositioner("");

        assertEquals(1, positioner.getLastLine());
        assertEquals(0, positioner.getLastLineColumn());
    }
}
