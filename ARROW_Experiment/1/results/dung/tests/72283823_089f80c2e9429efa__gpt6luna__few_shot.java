package vkaretko.Task_5_1;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;

public class BubbleSort_ArrowTest {

    @Test
    public void sortsUnsortedArray() {
        BubbleSort sorter = new BubbleSort();
        int[] values = {5, 1, 4, 2, 8};

        int[] result = sorter.sort(values);

        assertArrayEquals(new int[]{1, 2, 4, 5, 8}, result);
        assertSame(values, result);
    }

    @Test
    public void sortsArrayWithDuplicatesAndNegativeValues() {
        BubbleSort sorter = new BubbleSort();
        int[] values = {3, -1, 3, 0, -5};

        int[] result = sorter.sort(values);

        assertArrayEquals(new int[]{-5, -1, 0, 3, 3}, result);
    }

    @Test
    public void handlesEmptyArray() {
        BubbleSort sorter = new BubbleSort();
        int[] values = {};

        int[] result = sorter.sort(values);

        assertArrayEquals(new int[]{}, result);
    }

    @Test
    public void handlesSingleElementArray() {
        BubbleSort sorter = new BubbleSort();
        int[] values = {7};

        int[] result = sorter.sort(values);

        assertArrayEquals(new int[]{7}, result);
    }
}
