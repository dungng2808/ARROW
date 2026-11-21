package org.byteskript.skript.compiler;

import mx.kenzie.foundation.Type;
import org.byteskript.skript.api.Library;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class DebugSkriptCompiler_ArrowTest {

    @Test
    public void constructorCreatesCompilerWithoutWritingOutput() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new DebugSkriptCompiler(output);

        assertEquals("", output.toString());
    }

    @Test
    public void compileStringPrintsClassHeader() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(output);
        Type path = new Type("example.Test");

        compiler.compile("", path);

        assertEquals("\n--example/Test\n\n", output.toString());
    }

    @Test
    public void compileInputStreamPrintsClassHeader() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(output);
        Type path = new Type("example.Test");

        compiler.compile(new java.io.ByteArrayInputStream(new byte[0]), path);

        assertEquals("\n--example/Test\n\n", output.toString());
    }
}
