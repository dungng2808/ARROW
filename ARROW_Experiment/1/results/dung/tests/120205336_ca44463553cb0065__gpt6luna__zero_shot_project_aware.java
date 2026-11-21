package com.openkappa.splitmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class Slice_ArrowTest {

  @Test
  public void getReturnsDefaultForUnassignedField() {
    Slice<String, Integer> slice = new Slice<>(7);

    assertEquals(slice.get("missing"), Integer.valueOf(7));
  }

  @Test
  public void setAndGetReturnsAssignedValue() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("age", 42);

    assertEquals(slice.get("age"), Integer.valueOf(42));
  }

  @Test
  public void settingFieldReplacesItsValue() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("age", 42);
    slice.set("age", 18);

    assertEquals(slice.get("age"), Integer.valueOf(18));
  }

  @Test
  public void iteratorContainsAssignedValues() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("first", 10);
    slice.set("second", 20);

    Set<Integer> values = new HashSet<>();
    Iterator<Integer> iterator = slice.iterator();
    while (iterator.hasNext()) {
      values.add(iterator.next());
    }

    assertEquals(values, new HashSet<Integer>() {{
      add(10);
      add(20);
    }});
  }

  @Test
  public void iteratorIsEmptyWhenNoValuesAreAssigned() {
    Slice<String, Integer> slice = new Slice<>(7);

    assertFalse(slice.iterator().hasNext());
  }

  @Test
  public void iteratorDoesNotIncludeDefaultValue() {
    Slice<String, Integer> slice = new Slice<>(7);
    slice.set("assigned", 10);

    Iterator<Integer> iterator = slice.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(iterator.next(), Integer.valueOf(10));
    assertFalse(iterator.hasNext());
  }
}
