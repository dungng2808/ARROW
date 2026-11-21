package com.brianway.learning.algorithms.leetcode.easy;

import org.junit.Test;
import static org.junit.Assert.*;

public class RemoveDuplicatesfromSortedArray_ArrowTest {

    @Test
    public void testRemoveDuplicatesEmptyArray() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {};
        int result = solution.removeDuplicates(nums);
        assertEquals(0, result);
    }

    @Test
    public void testRemoveDuplicatesSingleElement() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1};
        int result = solution.removeDuplicates(nums);
        assertEquals(1, result);
    }

    @Test
    public void testRemoveDuplicatesTwoElementsNoDuplicates() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 2};
        int result = solution.removeDuplicates(nums);
        assertEquals(2, result);
    }

    @Test
    public void testRemoveDuplicatesTwoElementsWithDuplicates() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1};
        int result = solution.removeDuplicates(nums);
        assertEquals(1, result);
    }

    @Test
    public void testRemoveDuplicatesMultipleElementsNoDuplicates() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 2, 3, 4, 5};
        int result = solution.removeDuplicates(nums);
        assertEquals(5, result);
    }

    @Test
    public void testRemoveDuplicatesMultipleElementsWithDuplicates() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5};
        int result = solution.removeDuplicates(nums);
        assertEquals(5, result);
    }

    @Test
    public void testRemoveDuplicatesComplexCase() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {0, 0, 1, 1, 1, 2, 2, 3, 3, 4};
        int result = solution.removeDuplicates(nums);
        assertEquals(5, result);
    }

    @Test
    public void testRemoveDuplicatesAllSameElements() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {1, 1, 1, 1, 1};
        int result = solution.removeDuplicates(nums);
        assertEquals(1, result);
    }

    @Test
    public void testRemoveDuplicatesWithNegativeNumbers() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        int[] nums = {-3, -1, 0, 1, 3, 3, 3};
        int result = solution.removeDuplicates(nums);
        assertEquals(5, result);
    }
}
