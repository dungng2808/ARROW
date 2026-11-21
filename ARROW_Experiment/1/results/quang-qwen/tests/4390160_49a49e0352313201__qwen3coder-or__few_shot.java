package com.somethingsimilar.opposite_of_a_bloom_filter;

import static org.junit.Assert.*;

import org.junit.Test;

public class ByteArrayFilter_ArrowTest {

    @Test
    public void testConstructorValidSize() {
        ByteArrayFilter filter = new ByteArrayFilter(10);
        assertEquals(16, filter.getSize());
    }

    @Test
    public void testConstructorPowerOfTwoSize() {
        ByteArrayFilter filter = new ByteArrayFilter(16);
        assertEquals(16, filter.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroSize() {
        new ByteArrayFilter(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNegativeSize() {
        new ByteArrayFilter(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTooLargeSize() {
        new ByteArrayFilter(1 << 31);
    }

    @Test
    public void testContainsAndAddFirstTime() {
        ByteArrayFilter filter = new ByteArrayFilter(10);
        byte[] id = {1, 2, 3};
        assertFalse(filter.containsAndAdd(id));
    }

    @Test
    public void testContainsAndAddSecondTime() {
        ByteArrayFilter filter = new ByteArrayFilter(10);
        byte[] id = {1, 2, 3};
        filter.containsAndAdd(id);
        assertTrue(filter.containsAndAdd(id));
    }

    @Test
    public void testContainsAndAddDifferentArraysSameContent() {
        ByteArrayFilter filter = new ByteArrayFilter(10);
        byte[] id1 = {1, 2, 3};
        byte[] id2 = {1, 2, 3};
        assertFalse(filter.containsAndAdd(id1));
        assertTrue(filter.containsAndAdd(id2));
    }

    @Test
    public void testContainsAndAddCollision() {
        ByteArrayFilter filter = new ByteArrayFilter(2);
        byte[] id1 = {1, 2, 3};
        byte[] id2 = {4, 5, 6};
        // Force collision by making both hash to same index
        // This is a bit tricky to test reliably, but we'll test that it doesn't crash
        assertFalse(filter.containsAndAdd(id1));
        assertFalse(filter.containsAndAdd(id2));
    }

    @Test
    public void testGetSize() {
        ByteArrayFilter filter = new ByteArrayFilter(5);
        assertEquals(8, filter.getSize());
    }
}
