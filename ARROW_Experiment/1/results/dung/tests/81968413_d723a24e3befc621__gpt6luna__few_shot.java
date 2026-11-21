package com.google.mu.util.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class BiIteration_ArrowTest {

  @Test
  public void yieldsPairsInOrder() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("first", 1).yield("second", 2);

    List<String> pairs = new ArrayList<>();
    iteration.iterate().forEach((left, right) -> pairs.add(left + ":" + right));

    assertEquals(Arrays.asList("first:1", "second:2"), pairs);
  }

  @Test
  public void yieldReturnsSameIteration() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    assertSame(iteration, iteration.yield("left", 1));
  }

  @Test
  public void emptyIterationProducesNoPairs() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    List<String> pairs = new ArrayList<>();

    iteration.iterate().forEach((left, right) -> pairs.add(left + ":" + right));

    assertTrue(pairs.isEmpty());
  }

  @Test
  public void iterateCanOnlyBeCalledOnce() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.iterate();

    try {
      iteration.iterate();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertFalse(expected.getMessage() == null);
    }
  }

  @Test
  public void rejectsNullLeftValue() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    try {
      iteration.yield(null, 1);
      fail("Expected a null-check exception");
    } catch (RuntimeException expected) {
      assertTrue(expected instanceof NullPointerException);
    }
  }

  @Test
  public void rejectsNullRightValue() {
    BiIteration<String, Integer> iteration = new BiIteration<>();

    try {
      iteration.yield("left", null);
      fail("Expected a null-check exception");
    } catch (RuntimeException expected) {
      assertTrue(expected instanceof NullPointerException);
    }
  }
}
