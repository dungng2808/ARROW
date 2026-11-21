package vkaretko.Task_5_1;

import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

public class BubbleSort_ArrowTest {

    @Test
    public void whenUnsortedArrayThenSorted() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {64, 34, 25, 12, 22, 11, 90};
        int[] expected = {11, 12, 22, 25, 34, 64, 90};
        int[] result = bubbleSort.sort(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void whenEmptyArrayThenReturnEmptyArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {};
        int[] expected = {};
        int[] result = bubbleSort.sort(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void whenSingleElementArrayThenReturnSameArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {42};
        int[] expected = {42};
        int[] result = bubbleSort.sort(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void whenAlreadySortedArrayThenReturnSameArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {1, 2, 3, 4, 5};
        int[] expected = {1, 2, 3, 4, 5};
        int[] result = bubbleSort.sort(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void whenReverseSortedArrayThenReturnSortedArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {5, 4, 3, 2, 1};
        int[] expected = {1, 2, 3, 4, 5};
        int[] result = bubbleSort.sort(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void whenArrayWithDuplicatesThenSorted() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] input = {3, 1, 4, 1, 5, 9, 2, 6, 5};
        int[] expected = {1, 1, 2, 3, 4, 5, 5, 6, 9};
        int[] result = bubbleSort.sort(input);
        assertArrayEquals(expected, result);
    }
}
