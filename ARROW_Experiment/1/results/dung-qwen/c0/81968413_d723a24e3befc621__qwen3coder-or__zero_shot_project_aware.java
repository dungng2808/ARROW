package com.google.mu.util.stream;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.mu.util.stream.Iteration.Continuation;

public class BiIteration_ArrowTest {

  @Test
  public void testYield() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    BiIteration<String, Integer> result = iteration.yield("a", 1);
    assertSame(iteration, result);
    
    BiStream<String, Integer> stream = iteration.iterate();
    List<Map.Entry<String, Integer>> entries = stream.toList();
    assertThat(entries).containsExactly(new AbstractMap.SimpleImmutableEntry<>("a", 1));
  }

  @Test
  public void testYieldMultiple() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("a", 1).yield("b", 2).yield("c", 3);
    
    BiStream<String, Integer> stream = iteration.iterate();
    List<Map.Entry<String, Integer>> entries = stream.toList();
    assertThat(entries).containsExactly(
        new AbstractMap.SimpleImmutableEntry<>("a", 1),
        new AbstractMap.SimpleImmutableEntry<>("b", 2),
        new AbstractMap.SimpleImmutableEntry<>("c", 3));
  }

  @Test
  public void testYieldWithNullLeft() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    try {
      iteration.yield(null, 1);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test
  public void testYieldWithNullRight() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    try {
      iteration.yield("a", null);
      fail("Expected NullPointerException");
    } catch (NullPointerException expected) {
      // Expected
    }
  }

  @Test
  public void testYieldContinuation() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    Continuation continuation = () -> {
      return singletonList(new AbstractMap.SimpleImmutableEntry<>("x", 10));
    };
    iteration.yield(continuation);
    
    BiStream<String, Integer> stream = iteration.iterate();
    List<Map.Entry<String, Integer>> entries = stream.toList();
    assertThat(entries).containsExactly(new AbstractMap.SimpleImmutableEntry<>("x", 10));
  }

  @Test
  public void testIterateTwiceThrows() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("a", 1);
    iteration.iterate(); // First call
    
    try {
      iteration.iterate(); // Second call - should throw
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected
    }
  }

  @Test
  public void testEmptyIteration() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    BiStream<String, Integer> stream = iteration.iterate();
    List<Map.Entry<String, Integer>> entries = stream.toList();
    assertThat(entries).isEmpty();
  }

  @Test
  public void testIterateReturnsBiStream() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("a", 1);
    BiStream<String, Integer> stream = iteration.iterate();
    
    assertTrue(stream instanceof BiStream);
    assertThat(stream.toList()).containsExactly(new AbstractMap.SimpleImmutableEntry<>("a", 1));
  }
}
