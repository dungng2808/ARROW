package vkaretko.Task_5_1;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class BubbleSort_ArrowTest {

    @Test
    public void testSort() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {5, 2, 8, 1, 9};
        int[] expected = {1, 2, 5, 8, 9};
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
    public void testSortSingleElement() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {42};
        int[] expected = {42};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortAlreadySorted() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {1, 2, 3, 4, 5};
        int[] expected = {1, 2, 3, 4, 5};
        int[] actual = bubbleSort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testSortReverseSorted() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {5, 4, 3, 2, 1};
        int[] expected = {1, 2, 3, 4, 5};
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
