package com.google.mu.util.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class BiIteration_ArrowTest {

  @Test
  public void testYieldPair() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    BiIteration<String, Integer> result = iteration.yield("left", 42);
    
    assertSame(iteration, result);
    assertNotNull(iteration.iterate());
  }

  @Test
  public void testYieldContinuation() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    Iteration.Continuation continuation = new Iteration.Continuation() {};
    BiIteration<String, Integer> result = iteration.yield(continuation);
    
    assertSame(iteration, result);
    assertNotNull(iteration.iterate());
  }

  @Test
  public void testIterateReturnsBiStream() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("first", 1);
    iteration.yield("second", 2);
    
    BiStream<String, Integer> stream = iteration.iterate();
    
    assertNotNull(stream);
    assertTrue(stream instanceof BiStream);
  }

  @Test
  public void testYieldWithNullLeftThrows() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    
    try {
      iteration.yield(null, 42);
      assertTrue(false); // Should not reach here
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testYieldWithNullRightThrows() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    
    try {
      iteration.yield("left", null);
      assertTrue(false); // Should not reach here
    } catch (NullPointerException e) {
      // Expected
    }
  }

  @Test
  public void testIterateTwiceThrows() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("left", 42);
    iteration.iterate(); // First call
    
    try {
      iteration.iterate(); // Second call - should throw
      assertTrue(false); // Should not reach here
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  @Test
  public void testYieldAndIterate() {
    BiIteration<String, Integer> iteration = new BiIteration<>();
    iteration.yield("a", 1);
    iteration.yield("b", 2);
    iteration.yield("c", 3);
    
    BiStream<String, Integer> stream = iteration.iterate();
    
    assertNotNull(stream);
    
    // Verify we can get entries from the stream
    Map.Entry<String, Integer> entry1 = stream.iterator().next();
    assertEquals("a", entry1.getKey());
    assertEquals(Integer.valueOf(1), entry1.getValue());
    
    Map.Entry<String, Integer> entry2 = stream.iterator().next();
    assertEquals("b", entry2.getKey());
    assertEquals(Integer.valueOf(2), entry2.getValue());
    
    Map.Entry<String, Integer> entry3 = stream.iterator().next();
    assertEquals("c", entry3.getKey());
    assertEquals(Integer.valueOf(3), entry3.getValue());
  }
}
