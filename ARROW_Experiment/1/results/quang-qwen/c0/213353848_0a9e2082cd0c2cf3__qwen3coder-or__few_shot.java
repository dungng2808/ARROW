package com.gxk.jvm;

import org.junit.Test;
import static org.junit.Assert.*;

public class Args_ArrowTest {

  @Test
  public void testParseArgs_Version() {
    Args args = Args.parseArgs("-version");
    assertTrue(args.version);
    assertFalse(args.help);
    assertFalse(args.verbose);
  }

  @Test
  public void testParseArgs_Help() {
    Args args = Args.parseArgs("-help");
    assertFalse(args.version);
    assertTrue(args.help);
    assertFalse(args.verbose);
  }

  @Test
  public void testParseArgs_Verbose() {
    Args args = Args.parseArgs("-verbose", "com.example.Main");
    assertTrue(args.verbose);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_VerboseTrace() {
    Args args = Args.parseArgs("-verbose:trace", "com.example.Main");
    assertTrue(args.verboseTrace);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_VerboseCall() {
    Args args = Args.parseArgs("-verbose:call", "com.example.Main");
    assertTrue(args.verboseCall);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_VerboseClass() {
    Args args = Args.parseArgs("-verbose:class", "com.example.Main");
    assertTrue(args.verboseClass);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_VerboseDebug() {
    Args args = Args.parseArgs("-verbose:debug", "com.example.Main");
    assertTrue(args.verboseDebug);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_ColorRed() {
    Args args = Args.parseArgs("-Xcolor:red", "com.example.Main");
    assertEquals(Logger.ANSI_RED, Logger.fg);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_ColorGreen() {
    Args args = Args.parseArgs("-Xcolor:green", "com.example.Main");
    assertEquals(Logger.ANSI_GREEN, Logger.fg);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_ColorYellow() {
    Args args = Args.parseArgs("-Xcolor:yellow", "com.example.Main");
    assertEquals(Logger.ANSI_YELLOW, Logger.fg);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_Classpath() {
    Args args = Args.parseArgs("-cp", "/path/to/classes", "com.example.Main");
    assertEquals("/path/to/classes", args.classpath);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_Jar() {
    Args args = Args.parseArgs("-jar", "app.jar", "com.example.Main");
    assertEquals("./app.jar", args.classpath);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_WithProgramArgs() {
    Args args = Args.parseArgs("-cp", "/path/to/classes", "com.example.Main", "arg1", "arg2");
    assertEquals("/path/to/classes", args.classpath);
    assertEquals("com.example.Main", args.clazz);
    assertNotNull(args.args);
    assertEquals(2, args.args.length);
    assertEquals("arg1", args.args[0]);
    assertEquals("arg2", args.args[1]);
  }

  @Test
  public void testParseArgs_DefaultClasspath() {
    Args args = Args.parseArgs("com.example.Main");
    assertEquals(".", args.classpath);
    assertEquals("com.example.Main", args.clazz);
  }

  @Test
  public void testParseArgs_MultipleVerboseFlags() {
    Args args = Args.parseArgs("-verbose:class", "-verbose:debug", "com.example.Main");
    assertTrue(args.verboseClass);
    assertTrue(args.verboseDebug);
    assertEquals("com.example.Main", args.clazz);
  }
}
