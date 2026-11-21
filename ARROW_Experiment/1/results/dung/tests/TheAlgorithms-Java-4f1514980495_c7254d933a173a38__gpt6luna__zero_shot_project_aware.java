package com.thealgorithms.maths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class FindMin_ArrowTest {

    @Test
    public void findsMinimumInUnsortedArray() {
        assertThat(FindMin.findMin(new int[] {4, -2, 7, 0, -5})).isEqualTo(-5);
    }

    @Test
    public void findsMinimumInSingleElementArray() {
        assertThat(FindMin.findMin(new int[] {12})).isEqualTo(12);
    }

    @Test
    public void findsMinimumWhenAllValuesAreEqual() {
        assertThat(FindMin.findMin(new int[] {3, 3, 3})).isEqualTo(3);
    }

    @Test
    public void findsIntegerMinimumValue() {
        assertThat(FindMin.findMin(new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE, 0}))
            .isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    public void throwsForEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> FindMin.findMin(new int[0]));
    }
}
