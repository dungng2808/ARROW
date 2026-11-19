package com.thealgorithms.maths;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FindMin_ArrowTest {

    @Test
    public void findMinReturnsSmallestValue() {
        assertEquals(-4, FindMin.findMin(new int[] {3, -4, 8, 0}));
    }

    @Test
    public void findMinReturnsOnlyValue() {
        assertEquals(7, FindMin.findMin(new int[] {7}));
    }

    @Test
    public void findMinHandlesRepeatedMinimum() {
        assertEquals(-2, FindMin.findMin(new int[] {-2, 5, -2, 1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findMinRejectsEmptyArray() {
        FindMin.findMin(new int[0]);
    }
}
