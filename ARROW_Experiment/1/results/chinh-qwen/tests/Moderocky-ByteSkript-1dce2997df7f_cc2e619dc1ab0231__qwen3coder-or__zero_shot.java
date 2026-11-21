package org.byteskript.skript.compiler;

import mx.kenzie.foundation.Type;
import org.byteskript.skript.api.Library;
import org.junit.Test;
import org.junit.Before;
import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DebugSkriptCompiler_ArrowTest {
    
    private ByteArrayOutputStream outputStream;
    private DebugSkriptCompiler compiler;
    
    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        compiler = new DebugSkriptCompiler(outputStream, new Library[0]);
    }
    
    @Test
    public void testConstructor() {
        assertNotNull(compiler);
        assertNotNull(compiler.stream);
    }
    
    @Test
    public void testCreateContext() {
        Type path = new Type("test");
        FileContext context = compiler.createContext(path);
        assertNotNull(context);
        assertEquals(path, context.path);
        assertEquals(1, context.lineNumber);
    }
    
    @Test
    public void testCompileInputStream() throws IOException {
        String source = "set {_a} to 1";
        InputStream inputStream = new ByteArrayInputStream(source.getBytes());
        Type path = new Type("test");
        
        PostCompileClass[] result = compiler.compile(inputStream, path);
        assertNotNull(result);
        assertTrue(outputStream.toString().contains("--test"));
    }
    
    @Test
    public void testCompileString() {
        String source = "set {_a} to 1";
        Type path = new Type("test");
        
        PostCompileClass[] result = compiler.compile(source, path);
        assertNotNull(result);
        assertTrue(outputStream.toString().contains("--test"));
    }
    
    @Test
    public void testDebugWithTree() {
        Type path = new Type("test");
        FileContext context = compiler.createContext(path);
        // Create a simple ElementTree mock
        ElementTree tree = new ElementTree() {
            @Override
            public String toString(Context context) {
                return "MockElement";
            }
            
            @Override
            public void preCompile(Context context) {}
            
            @Override
            public void compile(Context context) {}
        };
        
        compiler.debug(tree, context);
        String output = outputStream.toString();
        assertTrue(output.contains("MockElement"));
    }
    
    @Test
    public void testCompileLineBlank() {
        String line = "   ";
        Type path = new Type("test");
        FileContext context = compiler.createContext(path);
        
        compiler.compileLine(line, context);
        String output = outputStream.toString();
        assertTrue(output.contains("\n"));
    }
    
    @Test
    public void testCompileLineNullTree() {
        String line = "invalid syntax";
        Type path = new Type("test");
        FileContext context = compiler.createContext(path);
        
        compiler.compileLine(line, context);
        // Should not crash, just return early
        assertNotNull(context);
    }
}
