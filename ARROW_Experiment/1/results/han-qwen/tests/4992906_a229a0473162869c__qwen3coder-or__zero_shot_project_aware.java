package net.sourceforge.pmd.lang.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SourceCodePositioner_ArrowTest {

    @Test
    public void testSingleLine() {
        String sourceCode = "Hello World";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(11, positioner.columnFromOffset(1, 10));
        assertEquals(1, positioner.getLastLine());
        assertEquals(11, positioner.getLastLineColumn());
    }

    @Test
    public void testMultipleLines() {
        String sourceCode = "Line 1\nLine 2\nLine 3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.lineNumberFromOffset(6));
        assertEquals(2, positioner.lineNumberFromOffset(7));
        assertEquals(2, positioner.lineNumberFromOffset(13));
        assertEquals(3, positioner.lineNumberFromOffset(14));
        assertEquals(3, positioner.lineNumberFromOffset(20));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(7, positioner.columnFromOffset(1, 6));
        assertEquals(1, positioner.columnFromOffset(2, 7));
        assertEquals(7, positioner.columnFromOffset(2, 13));
        assertEquals(1, positioner.columnFromOffset(3, 14));
        assertEquals(7, positioner.columnFromOffset(3, 20));
        
        assertEquals(3, positioner.getLastLine());
        assertEquals(7, positioner.getLastLineColumn());
    }

    @Test
    public void testEmptyLines() {
        String sourceCode = "Line 1\n\nLine 3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(2, positioner.lineNumberFromOffset(7));
        assertEquals(3, positioner.lineNumberFromOffset(8));
        assertEquals(3, positioner.lineNumberFromOffset(14));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(7, positioner.columnFromOffset(1, 6));
        assertEquals(1, positioner.columnFromOffset(2, 7));
        assertEquals(1, positioner.columnFromOffset(3, 8));
        assertEquals(7, positioner.columnFromOffset(3, 14));
        
        assertEquals(3, positioner.getLastLine());
        assertEquals(7, positioner.getLastLineColumn());
    }

    @Test
    public void testLineEndings() {
        String sourceCode = "Line 1\r\nLine 2\r\nLine 3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.lineNumberFromOffset(6));
        assertEquals(2, positioner.lineNumberFromOffset(8));
        assertEquals(2, positioner.lineNumberFromOffset(14));
        assertEquals(3, positioner.lineNumberFromOffset(16));
        assertEquals(3, positioner.lineNumberFromOffset(22));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(7, positioner.columnFromOffset(1, 6));
        assertEquals(1, positioner.columnFromOffset(2, 8));
        assertEquals(7, positioner.columnFromOffset(2, 14));
        assertEquals(1, positioner.columnFromOffset(3, 16));
        assertEquals(7, positioner.columnFromOffset(3, 22));
        
        assertEquals(3, positioner.getLastLine());
        assertEquals(7, positioner.getLastLineColumn());
    }

    @Test
    public void testOffsetAtEnd() {
        String sourceCode = "Line 1\nLine 2";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(2, positioner.lineNumberFromOffset(13));
        assertEquals(6, positioner.columnFromOffset(2, 13));
        assertEquals(2, positioner.getLastLine());
        assertEquals(6, positioner.getLastLineColumn());
    }

    @Test
    public void testOffsetBeyondSource() {
        String sourceCode = "Line 1\nLine 2";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(2, positioner.lineNumberFromOffset(15));
        assertEquals(0, positioner.columnFromOffset(1, 15));
        assertEquals(2, positioner.getLastLine());
        assertEquals(6, positioner.getLastLineColumn());
    }

    @Test
    public void testZeroLengthSource() {
        String sourceCode = "";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(1, positioner.getLastLine());
        assertEquals(1, positioner.getLastLineColumn());
    }

    @Test
    public void testOnlyNewlines() {
        String sourceCode = "\n\n\n";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(2, positioner.lineNumberFromOffset(1));
        assertEquals(3, positioner.lineNumberFromOffset(2));
        assertEquals(4, positioner.lineNumberFromOffset(3));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(1, positioner.columnFromOffset(2, 1));
        assertEquals(1, positioner.columnFromOffset(3, 2));
        assertEquals(1, positioner.columnFromOffset(4, 3));
        
        assertEquals(4, positioner.getLastLine());
        assertEquals(1, positioner.getLastLineColumn());
    }
}
