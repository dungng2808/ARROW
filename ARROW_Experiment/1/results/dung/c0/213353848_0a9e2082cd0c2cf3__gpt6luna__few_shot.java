package com.gxk.jvm;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
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
    assertNull(args.clazz);
    assertNull(args.args);
  }

  @Test
  public void parsesHelpOption() {
    Args args = Args.parseArgs("-help");
    assertTrue(args.help);
    assertFalse(args.version);
    assertNull(args.clazz);
  }

  @Test
  public void parsesClassAndProgramArguments() {
    Args args = Args.parseArgs("example.Main", "one", "two");
    assertEquals(".", args.classpath);
    assertEquals("example.Main", args.clazz);
    assertArrayEquals(new String[] {"one", "two"}, args.args);
  }

  @Test
  public void parsesClasspathAndClassArguments() {
    Args args = Args.parseArgs("-cp", "classes", "example.Main", "value");
    assertEquals("classes", args.classpath);
    assertEquals("example.Main", args.clazz);
    assertArrayEquals(new String[] {"value"}, args.args);
  }

  @Test
  public void parsesVerboseOptions() {
    Args args =
        Args.parseArgs(
            "-verbose",
            "-verbose:trace",
            "-verbose:call",
            "-verbose:class",
            "-verbose:debug",
            "example.Main");
    assertTrue(args.verbose);
    assertTrue(args.verboseTrace);
    assertTrue(args.verboseCall);
    assertTrue(args.verboseClass);
    assertTrue(args.verboseDebug);
    assertEquals("example.Main", args.clazz);
  }

  @Test
  public void parsesJarMainClassAndArguments() throws Exception {
    File jar = File.createTempFile("args-test", ".jar");
    try {
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "example.Main");
      try (JarOutputStream output =
          new JarOutputStream(new FileOutputStream(jar), manifest)) {
        // The manifest is written by JarOutputStream.
      }

      Args args = Args.parseArgs("-jar", jar.getName(), "first", "second");
      assertEquals("." + File.pathSeparator + jar.getName(), args.classpath);
      assertEquals("example/Main", args.clazz);
      assertArrayEquals(new String[] {"first", "second"}, args.args);
    } finally {
      jar.delete();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsJarWithoutManifest() throws Exception {
    File jar = File.createTempFile("args-empty", ".jar");
    try {
      try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jar))) {
        // No manifest entry.
      }
      Args.parseArgs("-jar", jar.getName());
    } finally {
      jar.delete();
    }
  }
}
