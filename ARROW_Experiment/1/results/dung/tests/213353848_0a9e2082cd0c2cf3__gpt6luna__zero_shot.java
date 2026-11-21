package com.gxk.jvm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Args_ArrowTest {

  @Test
  public void parsesVersionOption() {
    Args result = Args.parseArgs("-version");

    assertTrue(result.version);
    assertFalse(result.help);
    assertEquals(".", result.classpath);
    assertNull(result.clazz);
    assertNull(result.args);
  }

  @Test
  public void parsesHelpOption() {
    Args result = Args.parseArgs("-help");

    assertTrue(result.help);
    assertFalse(result.version);
    assertEquals(".", result.classpath);
    assertNull(result.clazz);
    assertNull(result.args);
  }

  @Test
  public void parsesClassAndProgramArguments() {
    Args result = Args.parseArgs("example.Main", "first", "second");

    assertEquals("example.Main", result.clazz);
    assertEquals(".", result.classpath);
    assertArrayEquals(new String[] {"first", "second"}, result.args);
  }

  @Test
  public void parsesClasspathAndClass() {
    Args result = Args.parseArgs("-cp", "lib/*", "example.Main");

    assertEquals("lib/*", result.classpath);
    assertEquals("example.Main", result.clazz);
    assertNull(result.args);
  }

  @Test
  public void parsesVerboseOptions() {
    Args result = Args.parseArgs(
        "-verbose", "-verbose:trace", "-verbose:call", "-verbose:class", "-verbose:debug",
        "example.Main");

    assertTrue(result.verbose);
    assertTrue(result.verboseTrace);
    assertTrue(result.verboseCall);
    assertTrue(result.verboseClass);
    assertTrue(result.verboseDebug);
    assertEquals("example.Main", result.clazz);
  }

  @Test
  public void parsesJarManifestMainClass() throws Exception {
    java.io.File jar = java.io.File.createTempFile("args-test", ".jar");
    try {
      java.util.jar.Manifest manifest = new java.util.jar.Manifest();
      manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
      manifest.getMainAttributes().putValue("Main-Class", "example.Main");
      java.util.jar.JarOutputStream output =
          new java.util.jar.JarOutputStream(new java.io.FileOutputStream(jar), manifest);
      output.close();

      String relativePath = jar.getName();
      Args result = Args.parseArgs("-jar", relativePath, "first");

      assertEquals("." + java.io.File.separator + relativePath, result.classpath);
      assertEquals("example/Main", result.clazz);
      assertArrayEquals(new String[] {"first"}, result.args);
    } finally {
      jar.delete();
    }
  }
}
