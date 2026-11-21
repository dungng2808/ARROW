package com.openkappa.splitmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Slice_ArrowTest {

  @Test
  public void getReturnsDefaultForUnassignedField() {
    Slice<String, Integer> slice = new Slice<>(7);

    assertEquals(Integer.valueOf(7), slice.get("missing"));
  }

  @Test
  public void getReturnsAssignedValue() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("field", 12);

    assertEquals(Integer.valueOf(12), slice.get("field"));
  }

  @Test
  public void setReplacesPreviouslyAssignedValue() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("field", 12);
    slice.set("field", 19);

    assertEquals(Integer.valueOf(19), slice.get("field"));
  }

  @Test
  public void getReturnsNullWhenNullWasAssigned() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("field", null);

    assertNull(slice.get("field"));
  }

  @Test
  public void getReturnsNullDefaultForUnassignedField() {
    Slice<String, Integer> slice = new Slice<>(null);

    assertNull(slice.get("missing"));
  }

  @Test
  public void iteratorContainsAssignedValues() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("first", 12);
    slice.set("second", 19);

    List<Integer> values = new ArrayList<>();
    for (Integer value : slice) {
      values.add(value);
    }

    assertEquals(2, values.size());
    assertTrue(values.containsAll(Arrays.asList(12, 19)));
  }

  @Test
  public void iteratorIsEmptyWhenNoValuesAreAssigned() {
    Slice<String, Integer> slice = new Slice<>(7);
    Iterator<Integer> iterator = slice.iterator();

    assertFalse(iterator.hasNext());
  }
}
