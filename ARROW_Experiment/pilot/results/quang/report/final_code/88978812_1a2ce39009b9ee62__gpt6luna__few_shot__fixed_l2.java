package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.SortableCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuickSort_ArrowTest {

    @Test
    public void sortOrdersElements() {
        List<Integer> values = new ArrayList<Integer>(Arrays.asList(4, 1, 3, 2));
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return values.size();
            }

            public Integer get(int index) {
                return values.get(index);
            }
        };

        QuickSort<Integer> sort = new QuickSort<Integer>() {
            {
                comparator = Integer::compareTo;
                swapper = (first, second) -> {
                    Integer value = values.get(first);
                    values.set(first, values.get(second));
                    values.set(second, value);
                };
            }
        };
        sort.sort(sortable);

        assertEquals(Arrays.asList(1, 2, 3, 4), values);
    }
}
