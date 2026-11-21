package com.google.mu.util.stream;

import static com.google.common.truth.Truth.assertThat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BiIteration_ArrowTest {
  @Test
  public void yieldsPairsInOrder() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("a", 1).yield("b", 2);

    List<Map.Entry<String, Integer>> pairs = iteration.iterate().toMap().entrySet().stream()
        .collect(java.util.stream.Collectors.toList());

    assertThat(pairs).containsExactly(
        new AbstractMap.SimpleImmutableEntry<>("a", 1),
        new AbstractMap.SimpleImmutableEntry<>("b", 2)).inOrder();
  }

  @Test
  public void yieldsNoPairsWhenEmpty() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    assertThat(iteration.iterate().toMap()).isEmpty();
  }

  @Test
  public void yieldReturnsSameIteration() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    assertThat(iteration.yield("a", 1)).isSameInstanceAs(iteration);
  }

  @Test
  public void continuationCanYieldPairs() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield(() -> iteration.yield("a", 1));

    assertThat(iteration.iterate().toMap()).containsExactly("a", 1);
  }

  @Test(expected = NullPointerException.class)
  public void yieldRejectsNullLeft() {
    new BiIteration<String, Integer>().yield(null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void yieldRejectsNullRight() {
    new BiIteration<String, Integer>().yield("a", null);
  }

  @Test(expected = IllegalStateException.class)
  public void iterateCanOnlyBeCalledOnce() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.iterate();
    iteration.iterate();
  }
}
