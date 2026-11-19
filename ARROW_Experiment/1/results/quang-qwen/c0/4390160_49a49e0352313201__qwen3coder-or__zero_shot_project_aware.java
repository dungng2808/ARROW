package com.somethingsimilar.opposite_of_a_bloom_filter;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class ByteArrayFilter_ArrowTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroSize() {
        new ByteArrayFilter(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeSize() {
        new ByteArrayFilter(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithTooLargeSize() {
        new ByteArrayFilter(1 << 31);
    }

    @Test
    public void testConstructorWithValidSize() {
        ByteArrayFilter filter = new ByteArrayFilter(5);
        assertEquals(8, filter.getSize());
    }

    @Test
    public void testContainsAndAddFirstTime() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        byte[] testArray = {1, 2, 3};
        assertFalse(filter.containsAndAdd(testArray));
    }

    @Test
    public void testContainsAndAddSecondTime() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        byte[] testArray = {1, 2, 3};
        filter.containsAndAdd(testArray);
        assertTrue(filter.containsAndAdd(testArray));
    }

    @Test
    public void testContainsAndAddDifferentArraysSameContent() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        byte[] testArray1 = {1, 2, 3};
        byte[] testArray2 = {1, 2, 3};
        assertFalse(filter.containsAndAdd(testArray1));
        assertFalse(filter.containsAndAdd(testArray2));
    }

    @Test
    public void testContainsAndAddDifferentArraysDifferentContent() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        byte[] testArray1 = {1, 2, 3};
        byte[] testArray2 = {4, 5, 6};
        assertFalse(filter.containsAndAdd(testArray1));
        assertFalse(filter.containsAndAdd(testArray2));
    }

    @Test
    public void testGetSize() {
        ByteArrayFilter filter = new ByteArrayFilter(3);
        assertEquals(4, filter.getSize());
    }

    @Test
    public void testContainsAndAddWithNullArray() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        assertTrue(filter.containsAndAdd(null));
    }

    @Test
    public void testContainsAndAddWithNullArrayTwice() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        filter.containsAndAdd(null);
        assertTrue(filter.containsAndAdd(null));
    }
}
