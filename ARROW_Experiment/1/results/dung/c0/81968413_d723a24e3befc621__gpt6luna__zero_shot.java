package com.google.mu.util.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BiIteration_ArrowTest {
  @Test
  public void yieldsPairsInOrder() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("a", 1).yield("b", 2);

    List<Map.Entry<String, Integer>> pairs = iteration.iterate().toList();

    assertEquals(
        Arrays.asList(
            new java.util.AbstractMap.SimpleImmutableEntry<>("a", 1),
            new java.util.AbstractMap.SimpleImmutableEntry<>("b", 2)),
        pairs);
  }

  @Test
  public void yieldsNoPairs() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    assertEquals(Collections.emptyList(), iteration.iterate().toList());
  }

  @Test
  public void yieldReturnsSameIteration() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    assertSame(iteration, iteration.yield("a", 1));
  }

  @Test
  public void continuationCanYieldPairs() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield((Continuation) () -> iteration.yield("a", 1));

    assertEquals(
        Collections.singletonList(
            new java.util.AbstractMap.SimpleImmutableEntry<>("a", 1)),
        iteration.iterate().toList());
  }

  @Test
  public void iterateCanOnlyBeCalledOnce() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.iterate();

    try {
      iteration.iterate();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected.
    }
  }

  @Test(expected = NullPointerException.class)
  public void yieldRejectsNullLeft() {
    new BiIteration<String, Integer>().yield(null, 1);
  }

  @Test(expected = NullPointerException.class)
  public void yieldRejectsNullRight() {
    new BiIteration<String, Integer>().yield("a", null);
  }
}
