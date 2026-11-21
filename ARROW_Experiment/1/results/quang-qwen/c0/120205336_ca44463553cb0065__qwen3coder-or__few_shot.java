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
  public void testGetDefaultValue() {
    Slice<String, Integer> slice = new Slice<>(42);
    Integer value = slice.get("nonExistentField");
    assertEquals(Integer.valueOf(42), value);
  }

  @Test
  public void testGetSetValue() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 100);
    Integer value = slice.get("field1");
    assertEquals(Integer.valueOf(100), value);
  }

  @Test
  public void testGetWithOverriddenValue() {
    Slice<String, Integer> slice = new Slice<>(50);
    slice.set("field1", 200);
    Integer value = slice.get("field1");
    assertEquals(Integer.valueOf(200), value);
    Integer defaultValue = slice.get("field2");
    assertEquals(Integer.valueOf(50), defaultValue);
  }

  @Test
  public void testIterator() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("field1", 10);
    slice.set("field2", 20);
    
    Iterator<Integer> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    
    // Collect values to verify they are returned correctly
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
  public void testNullDefaultValue() {
    Slice<String, String> slice = new Slice<>((String) null);
    String value = slice.get("anyField");
    assertNull(value);
  }

  @Test
  public void testNullValueSet() {
    Slice<String, String> slice = new Slice<>("default");
    slice.set("field1", null);
    String value = slice.get("field1");
    assertNull(value);
  }
}
