package com.google.cloud.tools.minikube.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MinikubeDockerEnvParser_ArrowTest {

  @Test
  public void parseReturnsKeyValueMap() {
    Map<String, String> expected = new HashMap<>();
    expected.put("DOCKER_HOST", "tcp://192.168.99.100:2376");
    expected.put("DOCKER_TLS_VERIFY", "1");

    assertEquals(
        expected,
        MinikubeDockerEnvParser.parse(
            Arrays.asList(
                "DOCKER_HOST=tcp://192.168.99.100:2376",
                "DOCKER_TLS_VERIFY=1")));
  }

  @Test
  public void parsePreservesAdditionalEqualsSignsInValue() {
    Map<String, String> expected = new HashMap<>();
    expected.put("TOKEN", "part1=part2");

    assertEquals(expected, MinikubeDockerEnvParser.parse(Collections.singletonList("TOKEN=part1=part2")));
  }

  @Test
  public void parseAllowsEmptyValue() {
    Map<String, String> expected = new HashMap<>();
    expected.put("EMPTY", "");

    assertEquals(expected, MinikubeDockerEnvParser.parse(Collections.singletonList("EMPTY=")));
  }

  @Test
  public void parseUsesLastValueForDuplicateKey() {
    Map<String, String> expected = new HashMap<>();
    expected.put("MODE", "second");

    assertEquals(
        expected,
        MinikubeDockerEnvParser.parse(Arrays.asList("MODE=first", "MODE=second")));
  }

  @Test
  public void parseEmptyListReturnsEmptyMap() {
    assertEquals(Collections.emptyMap(), MinikubeDockerEnvParser.parse(Collections.<String>emptyList()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseRejectsStringWithoutEqualsSign() {
    MinikubeDockerEnvParser.parse(Collections.singletonList("INVALID"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseRejectsEmptyKey() {
    MinikubeDockerEnvParser.parse(Collections.singletonList("=value"));
  }
}
