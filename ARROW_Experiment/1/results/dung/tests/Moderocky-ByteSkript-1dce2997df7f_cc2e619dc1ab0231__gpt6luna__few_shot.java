package org.byteskript.skript.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import mx.kenzie.foundation.Type;
import org.junit.Test;

public class DebugSkriptCompiler_ArrowTest {

    @Test
    public void compileStringPrintsClassHeader() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(output);
        Type path = new Type("example/Test");

        compiler.compile("", path);

        assertEquals("\n--example/Test\n\n", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void compileInputStreamPrintsClassHeader() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(output);
        Type path = new Type("example/Test");

        compiler.compile(new java.io.ByteArrayInputStream(new byte[0]), path);

        assertEquals("\n--example/Test\n\n", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}
