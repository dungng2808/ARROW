package com.openkappa.splitmap;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.util.Iterator;
import java.util.Map;

public class Slice_ArrowTest {

  @Test
  public void testConstructor() {
    String defaultValue = "default";
    Slice<String, String> slice = new Slice<>(defaultValue);
    assertNotNull(slice);
  }

  @Test
  public void testGetWithDefaultValue() {
    String defaultValue = "default";
    Slice<String, String> slice = new Slice<>(defaultValue);
    String result = slice.get("field");
    assertEquals(result, defaultValue);
  }

  @Test
  public void testGetWithSetField() {
    String defaultValue = "default";
    Slice<String, String> slice = new Slice<>(defaultValue);
    slice.set("field", "value");
    String result = slice.get("field");
    assertEquals(result, "value");
  }

  @Test
  public void testSetAndGet() {
    String defaultValue = null;
    Slice<String, String> slice = new Slice<>(defaultValue);
    slice.set("field", "value");
    String result = slice.get("field");
    assertEquals(result, "value");
  }

  @Test
  public void testIterator() {
    String defaultValue = "default";
    Slice<String, String> slice = new Slice<>(defaultValue);
    slice.set("field1", "value1");
    slice.set("field2", "value2");
    
    Iterator<String> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    assertNotNull(iterator.next());
    assertTrue(iterator.hasNext());
    assertNotNull(iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testIteratorEmptySlice() {
    String defaultValue = "default";
    Slice<String, String> slice = new Slice<>(defaultValue);
    
    Iterator<String> iterator = slice.iterator();
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testGetWithNullDefaultValue() {
    Slice<String, String> slice = new Slice<>((String) null);
    String result = slice.get("field");
    assertNull(result);
  }

  @Test
  public void testGetWithNullValueSet() {
    Slice<String, String> slice = new Slice<>("default");
    slice.set("field", null);
    String result = slice.get("field");
    assertNull(result);
  }
}
