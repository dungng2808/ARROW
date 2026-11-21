package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import static org.junit.Assert.*;
import packt.java9.by.example.ch03.SortableCollection;

public class QuickSort_ArrowTest {
    @Test
    public void sortHandlesNullSortableByThrowing() {
        QuickSort<Integer> sort = new QuickSort<Integer>();
        try {
            sort.sort(null);
            fail("Expected an exception");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }
}
