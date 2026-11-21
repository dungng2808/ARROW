package Sort;

import org.junit.Test;
import static org.junit.Assert.*;

public class Insert_ArrowTest {

    @Test
    public void testSort_NormalCase() {
        int[] array = {5, 2, 4, 6, 1, 3};
        Insert.Sort(array);
        int[] expected = {1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, array);
    }

    @Test
    public void testSort_EmptyArray() {
        int[] array = {};
        Insert.Sort(array);
        int[] expected = {};
        assertArrayEquals(expected, array);
    }

    @Test
    public void testSort_SingleElement() {
        int[] array = {1};
        Insert.Sort(array);
        int[] expected = {1};
        assertArrayEquals(expected, array);
    }

    @Test
    public void testSort_AlreadySorted() {
        int[] array = {1, 2, 3, 4, 5};
        Insert.Sort(array);
        int[] expected = {1, 2, 3, 4, 5};
        assertArrayEquals(expected, array);
    }

    @Test
    public void testSort_ReverseSorted() {
        int[] array = {5, 4, 3, 2, 1};
        Insert.Sort(array);
        int[] expected = {1, 2, 3, 4, 5};
        assertArrayEquals(expected, array);
    }

    @Test
    public void testSort_Duplicates() {
        int[] array = {3, 1, 4, 1, 5, 9, 2, 6, 5};
        Insert.Sort(array);
        int[] expected = {1, 1, 2, 3, 4, 5, 5, 6, 9};
        assertArrayEquals(expected, array);
    }
}
