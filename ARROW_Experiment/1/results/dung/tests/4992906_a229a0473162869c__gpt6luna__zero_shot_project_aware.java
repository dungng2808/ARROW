package net.sourceforge.pmd.lang.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceCodePositioner_ArrowTest {

    @Test
    public void lineNumberFromOffsetFindsLines() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef\ngh");

        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.lineNumberFromOffset(3));
        assertEquals(2, positioner.lineNumberFromOffset(4));
        assertEquals(3, positioner.lineNumberFromOffset(8));
    }

    @Test
    public void columnFromOffsetIsOneBased() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef");

        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(4, positioner.columnFromOffset(1, 3));
        assertEquals(1, positioner.columnFromOffset(2, 4));
        assertEquals(3, positioner.columnFromOffset(2, 6));
    }

    @Test
    public void columnFromOffsetReturnsZeroForInvalidLine() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef");

        assertEquals(0, positioner.columnFromOffset(0, 0));
        assertEquals(0, positioner.columnFromOffset(3, 4));
    }

    @Test
    public void reportsLastLineAndColumn() {
        SourceCodePositioner positioner = new SourceCodePositioner("abc\ndef");

        assertEquals(2, positioner.getLastLine());
        assertEquals(3, positioner.getLastLineColumn());
    }

    @Test
    public void emptySourceHasOneEmptyLine() {
        SourceCodePositioner positioner = new SourceCodePositioner("");

        assertEquals(1, positioner.getLastLine());
        assertEquals(0, positioner.getLastLineColumn());
        assertEquals(1, positioner.lineNumberFromOffset(0));
    }

    @Test
    public void trailingNewlineDoesNotCreateAnAdditionalLine() {
        SourceCodePositioner positioner = new SourceCodePositioner("a\n");

        assertEquals(1, positioner.getLastLine());
        assertEquals(2, positioner.getLastLineColumn());
    }
}
