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
  public void getReturnsDefaultValueForUnsetField() {
    Slice<String, Integer> slice = new Slice<>(7);

    assertEquals(Integer.valueOf(7), slice.get("missing"));
  }

  @Test
  public void getReturnsValueSetForField() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("field", 12);

    assertEquals(Integer.valueOf(12), slice.get("field"));
  }

  @Test
  public void setNullValueOverridesDefaultValue() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("field", null);

    assertNull(slice.get("field"));
  }

  @Test
  public void iteratorContainsSetValues() {
    Slice<String, Integer> slice = new Slice<>(0);
    slice.set("first", 3);
    slice.set("second", 5);

    List<Integer> values = new ArrayList<>();
    for (Integer value : slice) {
      values.add(value);
    }

    assertEquals(2, values.size());
    assertTrue(values.containsAll(Arrays.asList(3, 5)));
  }

  @Test
  public void iteratorIsEmptyWhenNoValuesAreSet() {
    Slice<String, Integer> slice = new Slice<>(0);
    Iterator<Integer> iterator = slice.iterator();

    assertFalse(iterator.hasNext());
  }
}
