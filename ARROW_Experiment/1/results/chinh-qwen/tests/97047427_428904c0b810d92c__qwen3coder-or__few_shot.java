package com.google.cloud.tools.minikube.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MinikubeDockerEnvParser_ArrowTest {

  @Test
  public void testParse_emptyList() {
    List<String> keyValueStrings = Arrays.asList();
    Map<String, String> result = MinikubeDockerEnvParser.parse(keyValueStrings);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testParse_singleKeyValuePair() {
    List<String> keyValueStrings = Arrays.asList("VAR1=value1");
    Map<String, String> result = MinikubeDockerEnvParser.parse(keyValueStrings);
    Map<String, String> expected = new HashMap<>();
    expected.put("VAR1", "value1");
    assertEquals(expected, result);
  }

  @Test
  public void testParse_multipleKeyValues() {
    List<String> keyValueStrings =
        Arrays.asList("VAR1=value1", "VAR2=value2", "VAR3=value3");
    Map<String, String> result = MinikubeDockerEnvParser.parse(keyValueStrings);
    Map<String, String> expected = new HashMap<>();
    expected.put("VAR1", "value1");
    expected.put("VAR2", "value2");
    expected.put("VAR3", "value3");
    assertEquals(expected, result);
  }

  @Test
  public void testParse_keyWithValueContainingEquals() {
    List<String> keyValueStrings = Arrays.asList("VAR1=value1=value2");
    Map<String, String> result = MinikubeDockerEnvParser.parse(keyValueStrings);
    Map<String, String> expected = new HashMap<>();
    expected.put("VAR1", "value1=value2");
    assertEquals(expected, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_invalidFormat_noEquals() {
    List<String> keyValueStrings = Arrays.asList("VAR1");
    MinikubeDockerEnvParser.parse(keyValueStrings);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_invalidFormat_emptyKey() {
    List<String> keyValueStrings = Arrays.asList("=value1");
    MinikubeDockerEnvParser.parse(keyValueStrings);
  }
}
