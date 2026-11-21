package com.somethingsimilar.opposite_of_a_bloom_filter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ByteArrayFilter_ArrowTest {

  @Test
  public void testConstructorValidSize() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    assertEquals(16, filter.getSize());
  }

  @Test
  public void testConstructorRoundsUpToPowerOfTwo() {
    ByteArrayFilter filter = new ByteArrayFilter(15);
    assertEquals(16, filter.getSize());
  }

  @Test
  public void testConstructorRoundsUpToPowerOfTwo2() {
    ByteArrayFilter filter = new ByteArrayFilter(17);
    assertEquals(32, filter.getSize());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorInvalidSizeZero() {
    new ByteArrayFilter(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorInvalidSizeNegative() {
    new ByteArrayFilter(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorInvalidSizeTooLarge() {
    new ByteArrayFilter(1 << 31);
  }

  @Test
  public void testContainsAndAddFirstTime() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] testArray = {1, 2, 3};
    assertFalse(filter.containsAndAdd(testArray));
  }

  @Test
  public void testContainsAndAddSecondTime() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] testArray = {1, 2, 3};
    filter.containsAndAdd(testArray);
    assertTrue(filter.containsAndAdd(testArray));
  }

  @Test
  public void testContainsAndAddDifferentArraysSameContent() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] testArray1 = {1, 2, 3};
    byte[] testArray2 = {1, 2, 3};
    assertFalse(filter.containsAndAdd(testArray1));
    assertFalse(filter.containsAndAdd(testArray2));
  }

  @Test
  public void testContainsAndAddDifferentArraysDifferentContent() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] testArray1 = {1, 2, 3};
    byte[] testArray2 = {4, 5, 6};
    assertFalse(filter.containsAndAdd(testArray1));
    assertFalse(filter.containsAndAdd(testArray2));
  }

  @Test
  public void testContainsAndAddWithCollision() {
    ByteArrayFilter filter = new ByteArrayFilter(2);
    byte[] testArray1 = {1, 2, 3};
    byte[] testArray2 = {4, 5, 6};
    // Force collision by making sure both hash to same index
    // This is a bit tricky to test reliably, but we can at least verify
    // that the method doesn't crash and behaves reasonably
    assertFalse(filter.containsAndAdd(testArray1));
    assertFalse(filter.containsAndAdd(testArray2));
  }

  @Test
  public void testGetSize() {
    ByteArrayFilter filter = new ByteArrayFilter(8);
    assertEquals(8, filter.getSize());
  }

  @Test
  public void testContainsAndAddEmptyArray() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] emptyArray = {};
    assertFalse(filter.containsAndAdd(emptyArray));
    assertTrue(filter.containsAndAdd(emptyArray));
  }

  @Test
  public void testContainsAndAddLargeArray() {
    ByteArrayFilter filter = new ByteArrayFilter(16);
    byte[] largeArray = new byte[1000];
    for (int i = 0; i < 1000; i++) {
      largeArray[i] = (byte) i;
    }
    assertFalse(filter.containsAndAdd(largeArray));
    assertTrue(filter.containsAndAdd(largeArray));
  }
}
