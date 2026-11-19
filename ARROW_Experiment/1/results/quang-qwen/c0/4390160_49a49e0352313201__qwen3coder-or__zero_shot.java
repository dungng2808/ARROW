package com.somethingsimilar.opposite_of_a_bloom_filter;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class ByteArrayFilter_ArrowTest {

    @Test
    public void testConstructorValidSize() {
        ByteArrayFilter filter = new ByteArrayFilter(16);
        assertEquals(16, filter.getSize());
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
        assertTrue(filter.containsAndAdd(testArray2));
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
    public void testContainsAndAddWithNull() {
        ByteArrayFilter filter = new ByteArrayFilter(16);
        assertNull(filter.containsAndAdd(null));
    }

    @Test
    public void testGetSize() {
        ByteArrayFilter filter = new ByteArrayFilter(10);
        assertEquals(16, filter.getSize());
    }

    @Test
    public void testContainsAndAddWithEmptyArray() {
        ByteArrayFilter filter = new ByteArrayFilter(16);
        byte[] emptyArray = {};
        assertFalse(filter.containsAndAdd(emptyArray));
        assertTrue(filter.containsAndAdd(emptyArray));
    }

    @Test
    public void testContainsAndAddWithLargeArray() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        byte[] largeArray = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            largeArray[i] = (byte) i;
        }
        assertFalse(filter.containsAndAdd(largeArray));
        assertTrue(filter.containsAndAdd(largeArray));
    }
}
