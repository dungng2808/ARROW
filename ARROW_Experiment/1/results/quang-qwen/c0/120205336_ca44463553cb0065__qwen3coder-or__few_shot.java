package com.openkappa.splitmap;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

public class Slice_ArrowTest {

  @Test
  public void testSliceCreation() {
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
  public void testGetWithSetFieldValue() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 42);
    Integer value = slice.get("field1");
    assertEquals(Integer.valueOf(42), value);
  }

  @Test
  public void testGetWithSetAndDefault() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 42);
    Integer value1 = slice.get("field1");
    Integer value2 = slice.get("field2");
    assertEquals(Integer.valueOf(42), value1);
    assertEquals(Integer.valueOf(0), value2);
  }

  @Test
  public void testIterator() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 10);
    slice.set("field2", 20);
    
    Iterator<Integer> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    
    // Collect values to verify they are the ones we set
    int count = 0;
    while (iterator.hasNext()) {
      Integer value = iterator.next();
      assertTrue(value == 10 || value == 20);
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
  public void testDefaultValueNull() {
    Slice<String, Object> slice = new Slice<>((Object) null);
    Object value = slice.get("anyField");
    assertNull(value);
  }

  @Test
  public void testSetNullValue() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", null);
    Integer value = slice.get("field1");
    assertNull(value);
  }
}
