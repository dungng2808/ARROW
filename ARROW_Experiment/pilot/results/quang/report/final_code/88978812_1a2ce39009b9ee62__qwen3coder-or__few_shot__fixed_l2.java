package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.AbstractSort;
import packt.java9.by.example.ch03.SortableCollection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public class QuickSort_ArrowTest {

    @Test
    public void testSort() throws Exception {
        // Create a SortableCollection instance
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            private Integer[] data = {5, 2, 8, 1, 9};
            
            @Override
            public int size() {
                return data.length;
            }
            
            @Override
            public Integer get(int index) {
                return data[index];
            }
            
            @Override
            public void set(int index, Integer value) {
                data[index] = value;
            }
        };
        
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Set up the comparator using reflection since it's protected
        Field comparatorField = AbstractSort.class.getDeclaredField("comparator");
        comparatorField.setAccessible(true);
        comparatorField.set(quickSort, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        
        // Set up the swapper using reflection since it's protected
        Field swapperField = AbstractSort.class.getDeclaredField("swapper");
        swapperField.setAccessible(true);
        swapperField.set(quickSort, new AbstractSort.Swapper<Integer>() {
            @Override
            public void swap(SortableCollection<Integer> collection, int i, int j) {
                Integer temp = collection.get(i);
                collection.set(i, collection.get(j));
                collection.set(j, temp);
            }
        });
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Verify the result
        Integer[] expected = {1, 2, 5, 8, 9};
        Integer[] actual = new Integer[sortable.size()];
        for (int i = 0; i < sortable.size(); i++) {
            actual[i] = sortable.get(i);
        }
        
        // Check if the sorting worked correctly
        assert Arrays.equals(expected, actual);
    }
}
