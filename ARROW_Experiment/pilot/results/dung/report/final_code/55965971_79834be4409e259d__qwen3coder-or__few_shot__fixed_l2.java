package com.brianway.learning.algorithms.leetcode.easy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveDuplicatesfromSortedArray_ArrowTest {

    @Test
    public void testRemoveDuplicates() {
        RemoveDuplicatesfromSortedArray solution = new RemoveDuplicatesfromSortedArray().new RemoveDuplicatesfromSortedArray0();
        
        // Test case 1: Empty array
        int[] nums1 = {};
        int result1 = solution.removeDuplicates(nums1);
        assertEquals(0, result1);
        
        // Test case 2: Single element array
        int[] nums2 = {1};
        int result2 = solution.removeDuplicates(nums2);
        assertEquals(1, result2);
        
        // Test case 3: Two elements with same value
        int[] nums3 = {1, 1};
        int result3 = solution.removeDuplicates(nums3);
        assertEquals(1, result3);
        
        // Test case 4: Two elements with different values
        int[] nums4 = {1, 2};
        int result4 = solution.removeDuplicates(nums4);
        assertEquals(2, result4);
        
        // Test case 5: Multiple duplicates
        int[] nums5 = {1, 1, 2, 2, 3, 3, 4};
        int result5 = solution.removeDuplicates(nums5);
        assertEquals(4, result5);
        
        // Test case 6: All same elements
        int[] nums6 = {1, 1, 1, 1, 1};
        int result6 = solution.removeDuplicates(nums6);
        assertEquals(1, result6);
        
        // Test case 7: Normal case with multiple distinct elements
        int[] nums7 = {1, 2, 3, 4, 5};
        int result7 = solution.removeDuplicates(nums7);
        assertEquals(5, result7);
    }
}
