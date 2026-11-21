package com.thealgorithms.maths;

import org.junit.Test;
import static org.junit.Assert.*;

public class FindMin_ArrowTest {

    @Test
    public void testFindMinNormalCase() {
        int[] array = {5, 2, 8, 1, 9};
        int expected = 1;
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinWithNegativeNumbers() {
        int[] array = {-5, -2, -8, -1, -9};
        int expected = -9;
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinSingleElement() {
        int[] array = {42};
        int expected = 42;
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinDuplicateMinimum() {
        int[] array = {3, 1, 4, 1, 5};
        int expected = 1;
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindMinEmptyArray() {
        int[] array = {};
        FindMin.findMin(array);
    }

    @Test
    public void testFindMinWithMaxValue() {
        int[] array = {Integer.MAX_VALUE, 100, 50};
        int expected = 50;
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinWithMinValue() {
        int[] array = {Integer.MIN_VALUE, 100, 50};
        int expected = Integer.MIN_VALUE;
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }
}
