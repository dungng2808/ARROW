package com.brianway.learning.algorithms.leetcode.easy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveDuplicatesfromSortedArray_ArrowTest {
    @Test
    public void removeDuplicatesOnBaseClassReturnsZero() {
        RemoveDuplicatesfromSortedArray array = new RemoveDuplicatesfromSortedArray();
        assertEquals(0, array.removeDuplicates(new int[]{1, 1, 2}));
    }

    @Test
    public void removeDuplicatesOnNestedClassReturnsLengthForEmptyArray() {
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 array =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        assertEquals(0, array.removeDuplicates(new int[]{}));
    }

    @Test
    public void removeDuplicatesOnNestedClassReturnsLengthForSingleElement() {
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 array =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        assertEquals(1, array.removeDuplicates(new int[]{7}));
    }

    @Test
    public void removeDuplicatesOnNestedClassCompactsSortedArray() {
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 array =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 2, 2, 3};
        assertEquals(3, array.removeDuplicates(nums));
        assertEquals(1, nums[0]);
        assertEquals(2, nums[1]);
        assertEquals(3, nums[2]);
    }

    @Test
    public void removeDuplicatesOnNestedClassHandlesAllEqualValues() {
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 array =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {4, 4, 4};
        assertEquals(1, array.removeDuplicates(nums));
        assertEquals(4, nums[0]);
    }
}
