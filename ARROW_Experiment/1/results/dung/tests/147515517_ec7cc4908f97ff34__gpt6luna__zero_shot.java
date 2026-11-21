package Sort;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class Insert_ArrowTest {

    @Test
    public void sortsUnsortedArray() {
        int[] array = {5, 2, 4, 1, 3};

        Insert.Sort(array);

        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, array);
    }

    @Test
    public void sortsArrayWithDuplicates() {
        int[] array = {3, 1, 3, 2, 1};

        Insert.Sort(array);

        assertArrayEquals(new int[]{1, 1, 2, 3, 3}, array);
    }

    @Test
    public void leavesEmptyArrayUnchanged() {
        int[] array = {};

        Insert.Sort(array);

        assertArrayEquals(new int[]{}, array);
    }

    @Test
    public void leavesSingleElementArrayUnchanged() {
        int[] array = {7};

        Insert.Sort(array);

        assertArrayEquals(new int[]{7}, array);
    }

    @Test
    public void leavesAlreadySortedArrayUnchanged() {
        int[] array = {1, 2, 3, 4};

        Insert.Sort(array);

        assertArrayEquals(new int[]{1, 2, 3, 4}, array);
    }

    @Test
    public void sortsArrayContainingNegativeValues() {
        int[] array = {0, -3, 2, -1};

        Insert.Sort(array);

        assertArrayEquals(new int[]{-3, -1, 0, 2}, array);
    }
}
