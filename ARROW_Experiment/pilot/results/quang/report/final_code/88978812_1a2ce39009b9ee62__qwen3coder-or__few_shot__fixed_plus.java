package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.AbstractSort;
import packt.java9.by.example.ch03.SortableCollection;

import java.util.Arrays;
import java.util.Comparator;

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
                return null;
            }

            @Override
            public void set(int index, Integer value) {
            }
        };
        
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Mock the comparator and swapper from the parent class
        Comparator<Integer> mockComparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return 0;
            }
        };
        
        // Use the correct approach to create a Swapper instance
        AbstractSort.Swapper<Integer> mockSwapper = new AbstractSort.Swapper<Integer>() {
            @Override
            public void swap(SortableCollection<Integer> collection, int i, int j) {
            }
        };
        
        // Set the comparator and swapper using reflection since they're protected
        try {
            java.lang.reflect.Field comparatorField = AbstractSort.class.getDeclaredField("comparator");
            comparatorField.setAccessible(true);
            comparatorField.set(quickSort, mockComparator);
            
            java.lang.reflect.Field swapperField = AbstractSort.class.getDeclaredField("swapper");
            swapperField.setAccessible(true);
            swapperField.set(quickSort, mockSwapper);
        } catch (Exception e) {
            // Should not happen in normal circumstances
            throw new RuntimeException(e);
        }
        
        // Call the sort method
        quickSort.sort(sortable);
    }
}
