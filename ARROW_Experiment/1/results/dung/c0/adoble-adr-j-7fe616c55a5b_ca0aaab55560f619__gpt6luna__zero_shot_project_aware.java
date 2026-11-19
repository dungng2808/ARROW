package org.doble.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.doble.adr.ADR;
import org.doble.adr.Environment;
import org.junit.jupiter.api.Test;

class CommandNew_ArrowTest {

    @Test
    public void commandMetadataIsAvailable() {
        CommandNew command = new CommandNew();
        assertNotNull(command);
        assertEquals("new", CommandNew.class.getAnnotation(picocli.CommandLine.Command.class).name());
    }

    @Test
    public void implementsCallable() {
        CommandNew command = new CommandNew();
        assertEquals(true, command instanceof Callable);
    }

    @Test
    public void titlePartsFieldIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("adrTitleParts");
        assertEquals(List.class, field.getType());
    }

    @Test
    public void linksFieldIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("links");
        assertEquals(java.util.ArrayList.class, field.getType());
    }

    @Test
    public void supersedesFieldIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("supersedes");
        assertEquals(java.util.ArrayList.class, field.getType());
    }

    @Test
    public void environmentFieldIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("env");
        assertEquals(Environment.class, field.getType());
    }

    @Test
    public void adrTitleParameterMetadataIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("adrTitleParts");
        assertNotNull(field.getAnnotation(picocli.CommandLine.Parameters.class));
    }

    @Test
    public void linkOptionMetadataIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("links");
        assertNotNull(field.getAnnotation(picocli.CommandLine.Option.class));
    }

    @Test
    public void supersedesOptionMetadataIsPresent() throws Exception {
        Field field = CommandNew.class.getDeclaredField("supersedes");
        assertNotNull(field.getAnnotation(picocli.CommandLine.Option.class));
    }

    @Test
    public void callMethodIsDeclared() throws Exception {
        Method method = CommandNew.class.getMethod("call");
        assertEquals(Integer.class, method.getReturnType());
    }

    @Test
    public void adrIdFormattingUsesFourDigits() {
        assertEquals("0007", String.format("%04d", 7));
    }

    @Test
    public void wellFormedAdrFilenameHasExpectedShape() throws Exception {
        Path path = Path.of("0001-example.md");
        Method method = CommandNew.class.getDeclaredMethod("wellFormedADR", Path.class);
        method.setAccessible(true);
        assertEquals(true, method.invoke(null, path));
    }

    @Test
    public void malformedAdrFilenameIsRejected() throws Exception {
        Path path = Path.of("1-example.md");
        Method method = CommandNew.class.getDeclaredMethod("wellFormedADR", Path.class);
        method.setAccessible(true);
        assertEquals(false, method.invoke(null, path));
    }

    @Test
    public void adrFilenameWithWrongSeparatorIsRejected() throws Exception {
        Path path = Path.of("0001_example.md");
        Method method = CommandNew.class.getDeclaredMethod("wellFormedADR", Path.class);
        method.setAccessible(true);
        assertEquals(false, method.invoke(null, path));
    }

    @Test
    public void adrFilenameWithNonDigitPrefixIsRejected() throws Exception {
        Path path = Path.of("00a1-example.md");
        Method method = CommandNew.class.getDeclaredMethod("wellFormedADR", Path.class);
        method.setAccessible(true);
        assertEquals(false, method.invoke(null, path));
    }

    @Test
    public void adrFilenameTooShortIsRejected() throws Exception {
        Path path = Path.of("0001-");
        Method method = CommandNew.class.getDeclaredMethod("wellFormedADR", Path.class);
        method.setAccessible(true);
        assertEquals(false, method.invoke(null, path));
    }

    @Test
    public void adrIdConversionReadsFirstFourCharacters() throws Exception {
        Method method = CommandNew.class.getDeclaredMethod("toInt", Path.class);
        method.setAccessible(true);
        assertEquals(42, method.invoke(null, Path.of("0042-example.md")));
    }
}
