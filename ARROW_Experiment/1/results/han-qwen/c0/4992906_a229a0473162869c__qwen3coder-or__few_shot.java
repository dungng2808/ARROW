package net.sourceforge.pmd.lang.ast;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SourceCodePositioner_ArrowTest {

    @Test
    public void testSingleLine() {
        String sourceCode = "Hello World";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(1, positioner.lineNumberFromOffset(0));
        assertEquals(1, positioner.columnFromOffset(1, 0));
        assertEquals(11, positioner.getLastLine());
        assertEquals(11, positioner.getLastLineColumn());
    }

    @Test
    public void testMultiLine() {
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
    public void testOffsetAtEnd() {
        String sourceCode = "Line1\nLine2";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(2, positioner.lineNumberFromOffset(11));
        assertEquals(6, positioner.columnFromOffset(2, 11));
    }

    @Test
    public void testOffsetBeyondEnd() {
        String sourceCode = "Line1\nLine2";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(0, positioner.columnFromOffset(10, 100));
    }

    @Test
    public void testOffsetBeforeStart() {
        String sourceCode = "Line1\nLine2";
        SourceCodePositioner positioner = new SourceCodePositioner(sourceCode);
        
        assertEquals(0, positioner.columnFromOffset(-1, 0));
    }
}
