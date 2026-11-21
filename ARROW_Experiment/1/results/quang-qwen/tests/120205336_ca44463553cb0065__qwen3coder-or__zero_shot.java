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
    Integer value = slice.get("nonExistentField");
    assertEquals(Integer.valueOf(0), value);
  }

  @Test
  public void testGetWithSetField() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 42);
    Integer value = slice.get("field1");
    assertEquals(Integer.valueOf(42), value);
  }

  @Test
  public void testGetWithNullDefaultValue() {
    Slice<String, Integer> slice = new Slice<>((Integer) null);
    Integer value = slice.get("anyField");
    assertNull(value);
  }

  @Test
  public void testSetAndGet() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 100);
    slice.set("field2", 200);
    
    assertEquals(Integer.valueOf(100), slice.get("field1"));
    assertEquals(Integer.valueOf(200), slice.get("field2"));
    assertEquals(Integer.valueOf(0), slice.get("field3"));
  }

  @Test
  public void testIterator() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 10);
    slice.set("field2", 20);
    
    Iterator<Integer> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    
    // Collect values to verify contents
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
  public void testIteratorWithNullValues() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", null);
    slice.set("field2", 42);
    
    Iterator<Integer> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    
    // Verify we can iterate through all values
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    assertEquals(2, count);
  }
}
