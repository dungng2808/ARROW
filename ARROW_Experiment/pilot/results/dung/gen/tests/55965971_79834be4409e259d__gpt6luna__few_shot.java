package com.brianway.learning.algorithms.leetcode.easy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveDuplicatesfromSortedArray_ArrowTest {

    @Test
    public void removeDuplicatesOnBaseClassReturnsZero() {
        RemoveDuplicatesfromSortedArray subject = new RemoveDuplicatesfromSortedArray();
        assertEquals(0, subject.removeDuplicates(new int[] {1, 1, 2}));
    }

    @Test
    public void removeDuplicatesOnSubclassReturnsDistinctCountAndCompactsArray() {
        RemoveDuplicatesfromSortedArray subject =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 2, 2, 3};

        assertEquals(3, subject.removeDuplicates(nums));
        assertEquals(1, nums[0]);
        assertEquals(2, nums[1]);
        assertEquals(3, nums[2]);
    }

    @Test
    public void removeDuplicatesOnSubclassHandlesEmptyArray() {
        RemoveDuplicatesfromSortedArray subject =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        assertEquals(0, subject.removeDuplicates(new int[0]));
    }

    @Test
    public void removeDuplicatesOnSubclassHandlesSingleElementArray() {
        RemoveDuplicatesfromSortedArray subject =
                new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        assertEquals(1, subject.removeDuplicates(new int[] {7}));
    }
}
