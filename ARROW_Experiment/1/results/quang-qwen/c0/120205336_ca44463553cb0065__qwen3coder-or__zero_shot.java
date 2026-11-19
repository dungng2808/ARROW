package com.openkappa.splitmap;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;

public class Slice_ArrowTest {

  @Test
  public void testConstructor() {
    Slice<String, Integer> slice = new Slice<>(0);
    assertNotNull(slice);
  }

  @Test
  public void testGetWithDefaultValue() {
    Slice<String, Integer> slice = new Slice<>(0);
    Integer result = slice.get("nonExistentField");
    assertEquals(Integer.valueOf(0), result);
  }

  @Test
  public void testGetWithSetField() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 42);
    Integer result = slice.get("field1");
    assertEquals(Integer.valueOf(42), result);
  }

  @Test
  public void testGetWithNullDefaultValue() {
    Slice<String, Integer> slice = new Slice<>((Integer) null);
    Integer result = slice.get("anyField");
    assertNull(result);
  }

  @Test
  public void testSetAndGet() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 100);
    Integer result = slice.get("field1");
    assertEquals(Integer.valueOf(100), result);
  }

  @Test
  public void testIterator() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 10);
    slice.set("field2", 20);
    
    Iterator<Integer> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    
    // We can't easily verify the exact contents since we don't know the iteration order,
    // but we can at least ensure it's not empty and iterable
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    assertEquals(2, count);
  }

  @Test
  public void testIteratorEmptySlice() {
    Slice<String, Integer> slice = new Slice<>(0);
    Iterator<Integer> iterator = slice.iterator();
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testMultipleSets() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 10);
    slice.set("field2", 20);
    slice.set("field1", 30); // overwrite
    
    assertEquals(Integer.valueOf(30), slice.get("field1"));
    assertEquals(Integer.valueOf(20), slice.get("field2"));
  }
}
