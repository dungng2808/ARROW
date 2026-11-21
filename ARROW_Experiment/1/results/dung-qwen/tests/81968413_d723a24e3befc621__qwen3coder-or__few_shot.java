package com.google.mu.util.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class BiIteration_ArrowTest {

  @Test
  public void testYieldPair() {
    BiIteration<String, Integer> biIteration = new BiIteration<>();
    biIteration.yield("key", 1);
    biIteration.yield("key2", 2);
    
    BiStream<String, Integer> stream = biIteration.iterate();
    assertNotNull(stream);
  }

  @Test
  public void testYieldPairWithNullLeft() {
    BiIteration<String, Integer> biIteration = new BiIteration<>();
    
    try {
      biIteration.yield(null, 1);
    } catch (NullPointerException e) {
      // Expected
      return;
    }
    assertTrue(false); // Should not reach here
  }

  @Test
  public void testYieldPairWithNullRight() {
    BiIteration<String, Integer> biIteration = new BiIteration<>();
    
    try {
      biIteration.yield("key", null);
    } catch (NullPointerException e) {
      // Expected
      return;
    }
    assertTrue(false); // Should not reach here
  }

  @Test
  public void testIterateReturnsBiStream() {
    BiIteration<String, Integer> biIteration = new BiIteration<>();
    biIteration.yield("key", 1);
    
    BiStream<String, Integer> stream = biIteration.iterate();
    assertNotNull(stream);
    assertTrue(stream instanceof BiStream);
  }

  @Test
  public void testMultipleYields() {
    BiIteration<String, Integer> biIteration = new BiIteration<>();
    biIteration.yield("a", 1);
    biIteration.yield("b", 2);
    biIteration.yield("c", 3);
    
    BiStream<String, Integer> stream = biIteration.iterate();
    assertNotNull(stream);
  }

  @Test
  public void testEmptyIteration() {
    BiIteration<String, Integer> biIteration = new BiIteration<>();
    
    BiStream<String, Integer> stream = biIteration.iterate();
    assertNotNull(stream);
  }
}
