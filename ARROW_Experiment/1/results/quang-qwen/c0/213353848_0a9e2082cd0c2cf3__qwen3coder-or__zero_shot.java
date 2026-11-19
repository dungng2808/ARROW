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
    Args args = Args.parseArgs("-verbose", "com.gxk.Main");
    assertTrue(args.verbose);
    assertFalse(args.verboseTrace);
    assertFalse(args.verboseCall);
    assertFalse(args.verboseClass);
    assertFalse(args.verboseDebug);
  }

  @Test
  public void testParseArgs_VerboseTrace() {
    Args args = Args.parseArgs("-verbose:trace", "com.gxk.Main");
    assertFalse(args.verbose);
    assertTrue(args.verboseTrace);
    assertFalse(args.verboseCall);
    assertFalse(args.verboseClass);
    assertFalse(args.verboseDebug);
  }

  @Test
  public void testParseArgs_VerboseCall() {
    Args args = Args.parseArgs("-verbose:call", "com.gxk.Main");
    assertFalse(args.verbose);
    assertFalse(args.verboseTrace);
    assertTrue(args.verboseCall);
    assertFalse(args.verboseClass);
    assertFalse(args.verboseDebug);
  }

  @Test
  public void testParseArgs_VerboseClass() {
    Args args = Args.parseArgs("-verbose:class", "com.gxk.Main");
    assertFalse(args.verbose);
    assertFalse(args.verboseTrace);
    assertFalse(args.verboseCall);
    assertTrue(args.verboseClass);
    assertFalse(args.verboseDebug);
  }

  @Test
  public void testParseArgs_VerboseDebug() {
    Args args = Args.parseArgs("-verbose:debug", "com.gxk.Main");
    assertFalse(args.verbose);
    assertFalse(args.verboseTrace);
    assertFalse(args.verboseCall);
    assertFalse(args.verboseClass);
    assertTrue(args.verboseDebug);
  }

  @Test
  public void testParseArgs_ColorRed() {
    Args args = Args.parseArgs("-Xcolor:red", "com.gxk.Main");
    assertEquals(Logger.ANSI_RED, Logger.fg);
  }

  @Test
  public void testParseArgs_ColorGreen() {
    Args args = Args.parseArgs("-Xcolor:green", "com.gxk.Main");
    assertEquals(Logger.ANSI_GREEN, Logger.fg);
  }

  @Test
  public void testParseArgs_ColorYellow() {
    Args args = Args.parseArgs("-Xcolor:yellow", "com.gxk.Main");
    assertEquals(Logger.ANSI_YELLOW, Logger.fg);
  }

  @Test
  public void testParseArgs_Classpath() {
    Args args = Args.parseArgs("-cp", "/tmp/classes", "com.gxk.Main");
    assertEquals("/tmp/classes", args.classpath);
    assertEquals("com.gxk.Main", args.clazz);
  }

  @Test
  public void testParseArgs_Jar() {
    Args args = Args.parseArgs("-jar", "test.jar", "com.gxk.Main");
    assertEquals("./test.jar", args.classpath);
    assertEquals("com.gxk.Main", args.clazz);
  }

  @Test
  public void testParseArgs_WithProgramArgs() {
    Args args = Args.parseArgs("-cp", "/tmp/classes", "com.gxk.Main", "arg1", "arg2");
    assertEquals("/tmp/classes", args.classpath);
    assertEquals("com.gxk.Main", args.clazz);
    assertNotNull(args.args);
    assertEquals(2, args.args.length);
    assertEquals("arg1", args.args[0]);
    assertEquals("arg2", args.args[1]);
  }

  @Test
  public void testParseArgs_MixedOptions() {
    Args args = Args.parseArgs("-verbose:class", "-cp", "/tmp/classes", "com.gxk.Main");
    assertTrue(args.verboseClass);
    assertEquals("/tmp/classes", args.classpath);
    assertEquals("com.gxk.Main", args.clazz);
  }
}
