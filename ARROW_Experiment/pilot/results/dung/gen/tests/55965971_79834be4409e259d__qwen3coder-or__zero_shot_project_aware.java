package com.brianway.learning.algorithms.leetcode.easy;

import org.junit.Test;
import static org.junit.Assert.*;

public class RemoveDuplicatesfromSortedArray_ArrowTest {

    @Test
    public void testRemoveDuplicates_EmptyArray() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {};
        int result = solution.removeDuplicates(nums);
        assertEquals(0, result);
    }

    @Test
    public void testRemoveDuplicates_SingleElement() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {1};
        int result = solution.removeDuplicates(nums);
        assertEquals(1, result);
    }

    @Test
    public void testRemoveDuplicates_TwoElementsDistinct() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 2};
        int result = solution.removeDuplicates(nums);
        assertEquals(2, result);
    }

    @Test
    public void testRemoveDuplicates_TwoElementsSame() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1};
        int result = solution.removeDuplicates(nums);
        assertEquals(1, result);
    }

    @Test
    public void testRemoveDuplicates_MultipleDuplicates() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 2, 2, 3, 3, 4};
        int result = solution.removeDuplicates(nums);
        assertEquals(4, result);
    }

    @Test
    public void testRemoveDuplicates_WithZeros() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4};
        int result = solution.removeDuplicates(nums);
        assertEquals(5, result);
    }

    @Test
    public void testRemoveDuplicates_AllSameElements() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 1, 1, 1};
        int result = solution.removeDuplicates(nums);
        assertEquals(1, result);
    }

    @Test
    public void testRemoveDuplicates_NormalCase() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray.RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 2, 3, 4, 5};
        int result = solution.removeDuplicates(nums);
        assertEquals(5, result);
    }
}
