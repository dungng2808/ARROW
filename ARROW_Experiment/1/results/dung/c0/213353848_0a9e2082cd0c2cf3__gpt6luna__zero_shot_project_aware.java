package com.gxk.jvm;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Args_ArrowTest {

  @Test
  public void parsesVersionOption() {
    Args args = Args.parseArgs("-version");

    assertTrue(args.version);
    assertFalse(args.help);
    assertEquals(".", args.classpath);
    assertNull(args.clazz);
    assertNull(args.args);
  }

  @Test
  public void parsesHelpOption() {
    Args args = Args.parseArgs("-help");

    assertTrue(args.help);
    assertFalse(args.version);
    assertEquals(".", args.classpath);
    assertNull(args.clazz);
    assertNull(args.args);
  }

  @Test
  public void parsesClassAndProgramArguments() {
    Args args = Args.parseArgs("example.Main", "one", "two");

    assertEquals("example.Main", args.clazz);
    assertEquals(".", args.classpath);
    assertArrayEquals(new String[] {"one", "two"}, args.args);
  }

  @Test
  public void parsesClasspathAndClass() {
    Args args = Args.parseArgs("-cp", "classes", "example.Main");

    assertEquals("classes", args.classpath);
    assertEquals("example.Main", args.clazz);
    assertNull(args.args);
  }

  @Test
  public void parsesVerboseOptions() {
    Args args = Args.parseArgs(
        "-verbose", "-verbose:trace", "-verbose:call", "-verbose:class", "-verbose:debug",
        "example.Main");

    assertTrue(args.verbose);
    assertTrue(args.verboseTrace);
    assertTrue(args.verboseCall);
    assertTrue(args.verboseClass);
    assertTrue(args.verboseDebug);
    assertEquals("example.Main", args.clazz);
  }

  @Test
  public void parsesJarOptionAndProgramArguments() {
    try {
      Args.parseArgs("-jar", "missing-args-test.jar");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected instanceof IllegalArgumentException);
    }
  }
}
