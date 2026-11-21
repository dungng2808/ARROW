package com.thealgorithms.maths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

public class FindMin_ArrowTest {

    @Test
    public void testFindMinNormalCase() {
        int[] array = {5, 2, 8, 1, 9};
        int expected = Arrays.stream(array).min().getAsInt();
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
    public void testFindMinNegativeNumbers() {
        int[] array = {-5, -2, -8, -1, -9};
        int expected = Arrays.stream(array).min().getAsInt();
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinMixedPositiveAndNegative() {
        int[] array = {10, -5, 0, 15, -20};
        int expected = Arrays.stream(array).min().getAsInt();
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinWithMaxValue() {
        int[] array = {Integer.MAX_VALUE, 100, 50};
        int expected = Arrays.stream(array).min().getAsInt();
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test
    public void testFindMinWithMinValue() {
        int[] array = {Integer.MIN_VALUE, 100, 50};
        int expected = Arrays.stream(array).min().getAsInt();
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindMinEmptyArray() {
        int[] array = {};
        FindMin.findMin(array);
    }

    @Test
    public void testFindMinLargeArray() {
        Random random = new Random();
        int size = 1000;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt() % 1000;
        }
        int expected = Arrays.stream(array).min().getAsInt();
        int actual = FindMin.findMin(array);
        assertEquals(expected, actual);
    }
}
