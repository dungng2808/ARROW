package com.thealgorithms.maths;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FindMin_ArrowTest {

    @Test
    public void findsMinimumInUnsortedArray() {
        assertEquals(-4, FindMin.findMin(new int[] {3, -4, 8, 1}));
    }

    @Test
    public void findsMinimumInSingleElementArray() {
        assertEquals(7, FindMin.findMin(new int[] {7}));
    }

    @Test
    public void findsMinimumAtIntegerBoundary() {
        assertEquals(Integer.MIN_VALUE, FindMin.findMin(new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE, 0}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyArray() {
        FindMin.findMin(new int[] {});
    }
}
