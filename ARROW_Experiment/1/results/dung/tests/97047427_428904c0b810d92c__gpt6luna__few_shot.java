package com.google.cloud.tools.minikube.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MinikubeDockerEnvParser_ArrowTest {

  @Test
  public void parsesKeyValueStrings() {
    Map<String, String> expected = new HashMap<>();
    expected.put("DOCKER_HOST", "tcp://192.168.99.100:2376");
    expected.put("DOCKER_TLS_VERIFY", "1");
    expected.put("DOCKER_CERT_PATH", "/home/user/.minikube/certs");

    Map<String, String> actual =
        MinikubeDockerEnvParser.parse(
            Arrays.asList(
                "DOCKER_HOST=tcp://192.168.99.100:2376",
                "DOCKER_TLS_VERIFY=1",
                "DOCKER_CERT_PATH=/home/user/.minikube/certs"));

    assertEquals(expected, actual);
  }

  @Test
  public void preservesAdditionalEqualsSignsInValue() {
    Map<String, String> expected = new HashMap<>();
    expected.put("TOKEN", "first=second=third");

    assertEquals(
        expected, MinikubeDockerEnvParser.parse(Collections.singletonList("TOKEN=first=second=third")));
  }

  @Test
  public void parsesEmptyValue() {
    Map<String, String> expected = new HashMap<>();
    expected.put("EMPTY", "");

    assertEquals(expected, MinikubeDockerEnvParser.parse(Collections.singletonList("EMPTY=")));
  }

  @Test
  public void parsesEmptyInput() {
    assertEquals(Collections.emptyMap(), MinikubeDockerEnvParser.parse(Collections.<String>emptyList()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsStringWithoutEqualsSign() {
    MinikubeDockerEnvParser.parse(Collections.singletonList("INVALID"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsEmptyVariableName() {
    MinikubeDockerEnvParser.parse(Collections.singletonList("=value"));
  }
}
