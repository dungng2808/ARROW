package vkaretko.Task_5_1;

import org.junit.Test;
import static org.junit.Assert.*;

public class BubbleSort_ArrowTest {

    @Test
    public void testSortNormalArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortAlreadySortedArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {1, 2, 3, 4, 5};
        int[] expected = {1, 2, 3, 4, 5};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortReverseSortedArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {5, 4, 3, 2, 1};
        int[] expected = {1, 2, 3, 4, 5};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortSingleElementArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {42};
        int[] expected = {42};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortEmptyArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {};
        int[] expected = {};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortTwoElements() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {2, 1};
        int[] expected = {1, 2};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortDuplicateElements() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {3, 1, 4, 1, 5, 9, 2, 6, 5};
        int[] expected = {1, 1, 2, 3, 4, 5, 5, 6, 9};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }
}
