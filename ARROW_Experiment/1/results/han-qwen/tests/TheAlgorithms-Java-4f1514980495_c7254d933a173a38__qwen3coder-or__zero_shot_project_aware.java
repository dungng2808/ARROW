package com.thealgorithms.maths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

public class FindMin_ArrowTest {

    @Test
    public void testFindMinNormalCase() {
        int[] array = {5, 2, 8, 1, 9};
        int result = FindMin.findMin(array);
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void testFindMinWithNegativeNumbers() {
        int[] array = {-5, -2, -8, -1, -9};
        int result = FindMin.findMin(array);
        assertThat(result).isEqualTo(-9);
    }

    @Test
    public void testFindMinWithMixedNumbers() {
        int[] array = {10, -5, 0, 15, -10};
        int result = FindMin.findMin(array);
        assertThat(result).isEqualTo(-10);
    }

    @Test
    public void testFindMinSingleElement() {
        int[] array = {42};
        int result = FindMin.findMin(array);
        assertThat(result).isEqualTo(42);
    }

    @Test
    public void testFindMinEmptyArray() {
        int[] array = {};
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> FindMin.findMin(array))
                .withMessage("array must be non-empty.");
    }

    @Test
    public void testFindMinWithAllSameElements() {
        int[] array = {5, 5, 5, 5};
        int result = FindMin.findMin(array);
        assertThat(result).isEqualTo(5);
    }

    @Test
    public void testFindMinWithLargeNumbers() {
        int[] array = {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int result = FindMin.findMin(array);
        assertThat(result).isEqualTo(Integer.MIN_VALUE);
    }
}
