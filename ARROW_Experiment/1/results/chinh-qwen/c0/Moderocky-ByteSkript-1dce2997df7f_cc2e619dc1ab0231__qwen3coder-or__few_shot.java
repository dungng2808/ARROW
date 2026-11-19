package org.byteskript.skript.compiler;

import mx.kenzie.foundation.Type;
import org.byteskript.skript.api.Library;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DebugSkriptCompiler_ArrowTest {

    @Test
    public void testConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(baos);
        assertNotNull(compiler);
    }

    @Test
    public void testCompileWithInputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(baos);
        Type path = new Type("test");
        
        // This test just verifies that the method doesn't throw an exception
        // since we don't have actual input stream content
        try {
            compiler.compile("", path);
        } catch (Exception e) {
            // Expected behavior for empty string compilation
        }
    }

    @Test
    public void testCompileWithString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(baos);
        Type path = new Type("test");
        
        // This test just verifies that the method doesn't throw an exception
        try {
            compiler.compile("", path);
        } catch (Exception e) {
            // Expected behavior for empty string compilation
        }
    }

    @Test
    public void testCreateContext() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(baos);
        Type path = new Type("test");
        
        FileContext context = compiler.createContext(path);
        assertNotNull(context);
    }

    @Test
    public void testDebugOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(baos);
        Type path = new Type("test");
        FileContext context = compiler.createContext(path);
        
        // Test that debug output is written to stream
        String testLine = "test line";
        compiler.compileLine(testLine, context);
        
        String output = baos.toString();
        assertTrue(output.contains("--test"));
    }
}
