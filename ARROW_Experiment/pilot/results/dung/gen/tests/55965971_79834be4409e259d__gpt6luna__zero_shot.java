package com.brianway.learning.algorithms.leetcode.easy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveDuplicatesfromSortedArray_ArrowTest {
    @Test
    public void removeDuplicatesBaseImplementationReturnsZero() {
        RemoveDuplicatesfromSortedArray instance = new RemoveDuplicatesfromSortedArray();
        assertEquals(0, instance.removeDuplicates(new int[]{1, 1, 2}));
    }

    @Test
    public void removeDuplicatesSubclassReturnsDistinctCountAndCompactsArray() {
        RemoveDuplicatesfromSortedArray outer = new RemoveDuplicatesfromSortedArray();
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 instance =
                outer.new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 2, 2, 3};

        assertEquals(3, instance.removeDuplicates(nums));
        assertEquals(1, nums[0]);
        assertEquals(2, nums[1]);
        assertEquals(3, nums[2]);
    }

    @Test
    public void removeDuplicatesSubclassHandlesEmptyArray() {
        RemoveDuplicatesfromSortedArray outer = new RemoveDuplicatesfromSortedArray();
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 instance =
                outer.new RemoveDuplicatesfromSortedArray0();

        assertEquals(0, instance.removeDuplicates(new int[]{}));
    }

    @Test
    public void removeDuplicatesSubclassHandlesSingleElementArray() {
        RemoveDuplicatesfromSortedArray outer = new RemoveDuplicatesfromSortedArray();
        RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0 instance =
                outer.new RemoveDuplicatesfromSortedArray0();

        assertEquals(1, instance.removeDuplicates(new int[]{7}));
    }
}
