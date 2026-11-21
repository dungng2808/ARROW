package org.byteskript.skript.compiler;

import mx.kenzie.foundation.Type;
import mx.kenzie.foundation.language.PostCompileClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DebugSkriptCompiler_ArrowTest {

    @Test
    public void compileEmptySourcePrintsClassHeader() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DebugSkriptCompiler compiler = new DebugSkriptCompiler(output);
        Type path = new Type("example/Test");

        PostCompileClass[] result = compiler.compile("", path);

        assertNotNull(result);
        assertEquals("\n--example/Test\n\n", new String(output.toByteArray(), StandardCharsets.UTF_8));
    }
}
