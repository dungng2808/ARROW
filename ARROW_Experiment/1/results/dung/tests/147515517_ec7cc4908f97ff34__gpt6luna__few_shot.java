package Sort;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class Insert_ArrowTest {

    @Test
    public void sortsUnsortedArray() {
        int[] array = {5, 2, 4, 1, 3};

        Insert.Sort(array);

        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, array);
    }

    @Test
    public void leavesSortedArrayUnchanged() {
        int[] array = {1, 2, 3, 4};

        Insert.Sort(array);

        assertArrayEquals(new int[]{1, 2, 3, 4}, array);
    }

    @Test
    public void sortsArrayWithDuplicatesAndNegativeValues() {
        int[] array = {3, -1, 3, 0, -1};

        Insert.Sort(array);

        assertArrayEquals(new int[]{-1, -1, 0, 3, 3}, array);
    }

    @Test
    public void handlesEmptyArray() {
        int[] array = {};

        Insert.Sort(array);

        assertArrayEquals(new int[]{}, array);
    }

    @Test
    public void handlesSingleElementArray() {
        int[] array = {7};

        Insert.Sort(array);

        assertArrayEquals(new int[]{7}, array);
    }
}
