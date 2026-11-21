package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.AbstractSort;
import packt.java9.by.example.ch03.SortableCollection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class QuickSort_ArrowTest {

    @Test
    public void testSort() throws Exception {
        // Create a mock SortableCollection
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            @Override
            public int size() {
                return 5;
            }

            @Override
            public Integer get(int index) {
                return index;
            }

            @Override
            public void set(int index, Integer value) {
                // No-op
            }
        };
        
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Mock the comparator and swapper from the parent class
        Comparator<Integer> mockComparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
        
        // Use the correct approach to create a mock swapper
        AbstractSort.Swapper<Integer> mockSwapper = new AbstractSort.Swapper<Integer>() {
            @Override
            public void swap(SortableCollection<Integer> collection, int i, int j) {
                // No-op
            }
        };
        
        // Set the comparator and swapper using reflection since they're protected
        Field comparatorField = AbstractSort.class.getDeclaredField("comparator");
        comparatorField.setAccessible(true);
        comparatorField.set(quickSort, mockComparator);
        
        Field swapperField = AbstractSort.class.getDeclaredField("swapper");
        swapperField.setAccessible(true);
        swapperField.set(quickSort, mockSwapper);
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Verify that the sorting worked correctly
        assertEquals(0, sortable.get(0).intValue());
        assertEquals(1, sortable.get(1).intValue());
        assertEquals(2, sortable.get(2).intValue());
        assertEquals(3, sortable.get(3).intValue());
        assertEquals(4, sortable.get(4).intValue());
    }
}
