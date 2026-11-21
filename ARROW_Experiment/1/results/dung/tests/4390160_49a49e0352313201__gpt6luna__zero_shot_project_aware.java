package com.somethingsimilar.opposite_of_a_bloom_filter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteArrayFilter_ArrowTest {

  @Test
  public void constructorRoundsSizeUpToPowerOfTwo() {
    ByteArrayFilter filter = new ByteArrayFilter(5);

    assertEquals(8, filter.getSize());
  }

  @Test
  public void constructorKeepsPowerOfTwoSize() {
    ByteArrayFilter filter = new ByteArrayFilter(8);

    assertEquals(8, filter.getSize());
  }

  @Test
  public void constructorAcceptsOne() {
    ByteArrayFilter filter = new ByteArrayFilter(1);

    assertEquals(1, filter.getSize());
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorRejectsZero() {
    new ByteArrayFilter(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorRejectsNegativeSize() {
    new ByteArrayFilter(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorRejectsSizeAboveMaximum() {
    new ByteArrayFilter(1 << 30 + 1);
  }

  @Test
  public void containsAndAddReturnsFalseForFirstOccurrence() {
    ByteArrayFilter filter = new ByteArrayFilter(16);

    assertFalse(filter.containsAndAdd(new byte[] {1, 2, 3}));
  }

  @Test
  public void containsAndAddReturnsTrueForRepeatedBytes() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] id = new byte[] {1, 2, 3};

    assertFalse(filter.containsAndAdd(id));
    assertTrue(filter.containsAndAdd(new byte[] {1, 2, 3}));
  }

  @Test
  public void containsAndAddReturnsFalseForDifferentBytesAtSameIndex() {
    ByteArrayFilter filter = new ByteArrayFilter(1);

    assertFalse(filter.containsAndAdd(new byte[] {1}));
    assertFalse(filter.containsAndAdd(new byte[] {2}));
  }

  @Test
  public void containsAndAddHandlesEmptyArray() {
    ByteArrayFilter filter = new ByteArrayFilter(4);

    assertFalse(filter.containsAndAdd(new byte[0]));
    assertTrue(filter.containsAndAdd(new byte[0]));
  }
}
