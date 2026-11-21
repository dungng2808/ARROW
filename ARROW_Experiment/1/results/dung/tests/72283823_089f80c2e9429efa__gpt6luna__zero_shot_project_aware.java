package vkaretko.Task_5_1;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

public class BubbleSort_ArrowTest {

    @Test
    public void sortOrdersValuesAscending() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] values = {5, 1, 4, 2, 8};

        int[] result = bubbleSort.sort(values);

        assertArrayEquals(new int[]{1, 2, 4, 5, 8}, result);
        assertSame(values, result);
    }

    @Test
    public void sortHandlesEmptyArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] values = {};

        int[] result = bubbleSort.sort(values);

        assertArrayEquals(new int[]{}, result);
        assertSame(values, result);
    }

    @Test
    public void sortHandlesSingleElementArray() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] values = {7};

        int[] result = bubbleSort.sort(values);

        assertArrayEquals(new int[]{7}, result);
        assertSame(values, result);
    }

    @Test
    public void sortHandlesDuplicatesAndNegativeValues() {
        BubbleSort bubbleSort = new BubbleSort();
        int[] values = {3, -1, 3, 0, -1};

        int[] result = bubbleSort.sort(values);

        assertArrayEquals(new int[]{-1, -1, 0, 3, 3}, result);
        assertSame(values, result);
    }
}
