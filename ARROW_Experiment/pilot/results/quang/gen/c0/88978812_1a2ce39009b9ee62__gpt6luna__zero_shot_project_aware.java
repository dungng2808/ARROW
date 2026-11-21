package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.SortableCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuickSort_ArrowTest {

    @Test
    public void sortOrdersCollection() {
        final List<Integer> values = new ArrayList<Integer>(Arrays.asList(4, 1, 3, 2));
        SortableCollection<Integer> collection = new SortableCollection<Integer>() {
            @Override
            public int size() {
                return values.size();
            }

            @Override
            public Integer get(int index) {
                return values.get(index);
            }

            @Override
            public void swap(int first, int second) {
                Integer value = values.get(first);
                values.set(first, values.get(second));
                values.set(second, value);
            }
        };

        new QuickSort<Integer>().sort(collection);

        assertEquals(Arrays.asList(1, 2, 3, 4), values);
    }
}
