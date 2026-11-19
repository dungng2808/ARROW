package net.sourceforge.pmd.lang.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
        String sourceCode = "Line1\nLine2\nLine3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.lineNumberFromOffset(5));
        assertEquals(2, positioner.lineNumberFromOffset(6));
        assertEquals(2, positioner.lineNumberFromOffset(11));
        assertEquals(3, positioner.lineNumberFromOffset(12));
        assertEquals(3, positioner.lineNumberFromOffset(17));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(6, positioner.columnFromOffset(1, 5));
        assertEquals(1, positioner.columnFromOffset(2, 6));
        assertEquals(6, positioner.columnFromOffset(2, 11));
        assertEquals(1, positioner.columnFromOffset(3, 12));
        assertEquals(6, positioner.columnFromOffset(3, 17));
        
        assertEquals(3, positioner.getLastLine());
        assertEquals(6, positioner.getLastLineColumn());
    }

    @Test
    public void testEmptyLines() {
        String sourceCode = "Line1\n\nLine3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(2, positioner.lineNumberFromOffset(6));
        assertEquals(3, positioner.lineNumberFromOffset(7));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(1, positioner.columnFromOffset(2, 6));
        assertEquals(1, positioner.columnFromOffset(3, 7));
        
        assertEquals(3, positioner.getLastLine());
        assertEquals(6, positioner.getLastLineColumn());
    }

    @Test
    public void testLineEndings() {
        String sourceCode = "Line1\r\nLine2\r\nLine3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(2, positioner.lineNumberFromOffset(7));
        assertEquals(3, positioner.lineNumberFromOffset(14));
        
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(1, positioner.columnFromOffset(2, 7));
        assertEquals(1, positioner.columnFromOffset(3, 14));
        
        assertEquals(3, positioner.getLastLine());
        assertEquals(6, positioner.getLastLineColumn());
    }

    @Test
    public void testOffsetAtLineBoundaries() {
        String sourceCode = "Line1\nLine2\nLine3";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        // Test exact line boundaries
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(2, positioner.lineNumberFromOffset(6));
        assertEquals(3, positioner.lineNumberFromOffset(12));
        
        // Test just after line boundaries
        assertEquals(2, positioner.lineNumberFromOffset(7));
        assertEquals(3, positioner.lineNumberFromOffset(13));
        
        // Test at end of source
        assertEquals(3, positioner.lineNumberFromOffset(17));
        assertEquals(3, positioner.lineNumberFromOffset(18));
    }

    @Test
    public void testInvalidLineNumber() {
        String sourceCode = "Line1\nLine2";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        // Test invalid line numbers
        assertEquals(0, positioner.columnFromOffset(0, 0));
        assertEquals(0, positioner.columnFromOffset(-1, 0));
        assertEquals(0, positioner.columnFromOffset(3, 0));
    }

    @Test
    public void testEmptySource() {
        String sourceCode = "";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.getLastLine());
        assertEquals(1, positioner.getLastLineColumn());
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.columnFromOffset(1, 0));
    }

    @Test
    public void testOnlyNewlines() {
        String sourceCode = "\n\n\n";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(4, positioner.getLastLine());
        assertEquals(1, positioner.getLastLineColumn());
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(2, positioner.lineNumberFromOffset(1));
        assertEquals(3, positioner.lineNumberFromOffset(2));
        assertEquals(4, positioner.lineNumberFromOffset(3));
    }
}
