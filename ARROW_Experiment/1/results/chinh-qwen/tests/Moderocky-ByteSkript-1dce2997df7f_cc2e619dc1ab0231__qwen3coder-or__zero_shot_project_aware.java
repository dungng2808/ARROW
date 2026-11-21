package org.byteskript.skript.compiler;

import mx.kenzie.foundation.Type;
import mx.kenzie.foundation.language.PostCompileClass;
import org.byteskript.skript.api.Library;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.*;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DebugSkriptCompiler_ArrowTest {
    
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private Library[] libraries;
    
    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        libraries = new Library[0];
    }
    
    @After
    public void tearDown() {
        outputStream = null;
        printStream = null;
        libraries = null;
    }
    
    @Test
    public void testConstructor() {
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(printStream, libraries);
        assertNotNull(compiler);
    }
    
    @Test
    public void testCompileInputStream() throws IOException {
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(printStream, libraries);
        Type path = Type.from("test/path");
        
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        PostCompileClass[] result = compiler.compile(inputStream, path);
        
        assertNotNull(result);
        assertTrue(outputStream.toString().contains("--test/path"));
    }
    
    @Test
    public void testCompileString() {
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(printStream, libraries);
        Type path = Type.from("test/path");
        
        String source = "test";
        PostCompileClass[] result = compiler.compile(source, path);
        
        assertNotNull(result);
        assertTrue(outputStream.toString().contains("--test/path"));
    }
    
    @Test
    public void testCreateContext() {
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(printStream, libraries);
        Type path = Type.from("test/path");
        
        FileContext context = compiler.createContext(path);
        
        assertNotNull(context);
        assertEquals(path, context.path);
        assertEquals(1, context.lineNumber);
    }
    
    @Test
    public void testCompileLineWithBlankLine() {
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(printStream, libraries);
        FileContext context = new FileContext(Type.from("test/path"), 1);
        
        compiler.compileLine("", context);
        
        assertTrue(outputStream.toString().contains("\n"));
    }
    
    @Test
    public void testDebug() {
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(printStream, libraries);
        FileContext context = new FileContext(Type.from("test/path"), 1);
        
        // Create a mock ElementTree for testing
        ElementTree tree = new ElementTree() {
            @Override
            public String toString(Context context) {
                return "MockElementTree";
            }
            
            @Override
            public void preCompile(Context context) {
                // Mock implementation
            }
            
            @Override
            public void compile(Context context) {
                // Mock implementation
            }
        };
        
        compiler.debug(tree, context);
        
        assertTrue(outputStream.toString().contains("MockElementTree"));
    }
}
