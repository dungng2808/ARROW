package com.google.cloud.tools.minikube.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MinikubeDockerEnvParser_ArrowTest {

  @Test
  public void testParse_validInput() {
    List<String> input = new ArrayList<>();
    input.add("VAR1=VALUE1");
    input.add("VAR2=VALUE2");
    input.add("VAR3=VALUE3");

    Map<String, String> result = MinikubeDockerEnvParser.parse(input);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("VALUE1", result.get("VAR1"));
    assertEquals("VALUE2", result.get("VAR2"));
    assertEquals("VALUE3", result.get("VAR3"));
  }

  @Test
  public void testParse_emptyList() {
    List<String> input = new ArrayList<>();

    Map<String, String> result = MinikubeDockerEnvParser.parse(input);

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testParse_singleKeyValuePair() {
    List<String> input = new ArrayList<>();
    input.add("SINGLE=VALUE");

    Map<String, String> result = MinikubeDockerEnvParser.parse(input);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("VALUE", result.get("SINGLE"));
  }

  @Test
  public void testParse_keyWithEqualsInValue() {
    List<String> input = new ArrayList<>();
    input.add("VAR=VALUE=WITH=EQUALS");

    Map<String, String> result = MinikubeDockerEnvParser.parse(input);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("VALUE=WITH=EQUALS", result.get("VAR"));
  }

  @Test
  public void testParse_emptyKey() {
    List<String> input = new ArrayList<>();
    input.add("=VALUE");

    try {
      MinikubeDockerEnvParser.parse(input);
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "Error while parsing minikube's Docker environment: encountered empty environment variable name",
          e.getMessage());
    }
  }

  @Test
  public void testParse_invalidFormat() {
    List<String> input = new ArrayList<>();
    input.add("INVALIDFORMAT");

    try {
      MinikubeDockerEnvParser.parse(input);
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "Error while parsing minikube's Docker environment: environment variable string not in KEY=VALUE format",
          e.getMessage());
    }
  }

  @Test
  public void testParse_mixedValidAndInvalid() {
    List<String> input = new ArrayList<>();
    input.add("VALID=VALUE");
    input.add("INVALID");
    input.add("ANOTHER=VALUE");

    try {
      MinikubeDockerEnvParser.parse(input);
      fail("Expected IllegalArgumentException was not thrown");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "Error while parsing minikube's Docker environment: environment variable string not in KEY=VALUE format",
          e.getMessage());
    }
  }
}
