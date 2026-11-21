package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.SortableCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuickSort_ArrowTest {

    @Test
    public void sortOrdersElements() {
        final List<Integer> values = new ArrayList<Integer>(Arrays.asList(4, 1, 3, 2));
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return values.size();
            }

            public Integer get(int index) {
                return values.get(index);
            }

            public void set(int index, Integer value) {
                values.set(index, value);
            }
        };

        QuickSort<Integer> sort = new QuickSort<Integer>();
        sort.setComparator(new Comparator<Integer>() {
            public int compare(Integer left, Integer right) {
                return left.compareTo(right);
            }
        });
        sort.sort(sortable);

        assertEquals(Arrays.asList(1, 2, 3, 4), values);
    }
}
