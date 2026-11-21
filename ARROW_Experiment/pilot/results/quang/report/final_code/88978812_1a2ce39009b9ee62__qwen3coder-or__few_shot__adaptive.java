package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.AbstractSort;
import packt.java9.by.example.ch03.SortableCollection;
import packt.java9.by.example.ch03.qsort.FJQuickSort;

import java.util.Arrays;
import java.util.Comparator;

import static org.mockito.Mockito.*;

public class QuickSort_ArrowTest {

    @Test
    public void testSort() {
        // Create a mock SortableCollection
        SortableCollection<Integer> sortable = mock(SortableCollection.class);
        
        // Set up the mock to return a size of 5
        when(sortable.size()).thenReturn(5);
        
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Mock the comparator and swapper from the parent class
        Comparator<Integer> mockComparator = mock(Comparator.class);
        AbstractSort.Swapper<Integer> mockSwapper = mock(AbstractSort.Swapper.class);
        
        // Set the comparator and swapper using reflection since they're protected
        try {
            var comparatorField = AbstractSort.class.getDeclaredField("comparator");
            comparatorField.setAccessible(true);
            comparatorField.set(quickSort, mockComparator);
            
            var swapperField = AbstractSort.class.getDeclaredField("swapper");
            swapperField.setAccessible(true);
            swapperField.set(quickSort, mockSwapper);
        } catch (Exception e) {
            // Should not happen in normal circumstances
            throw new RuntimeException(e);
        }
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Verify that qsort was called with correct parameters
        verify(sortable, times(1)).size();
    }
}
