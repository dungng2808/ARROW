package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.SortableCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QuickSort_ArrowTest {

    @Test
    public void testSort() {
        // Create a list of integers to sort
        List<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(2);
        list.add(8);
        list.add(1);
        list.add(9);

        // Create a SortableCollection from the list
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return list.size();
            }

            public Integer get(int index) {
                return list.get(index);
            }

            public void set(int index, Integer value) {
                list.set(index, value);
            }

            public Comparator<Integer> getComparator() {
                return Integer::compareTo;
            }
        };

        // Create the QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();

        // Call the sort method
        quickSort.sort(sortable);

        // Check that the list is sorted
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(5), list.get(2));
        assertEquals(Integer.valueOf(8), list.get(3));
        assertEquals(Integer.valueOf(9), list.get(4));
    }

    @Test
    public void testSortEmptyList() {
        // Create an empty list
        List<Integer> list = new ArrayList<>();

        // Create a SortableCollection from the list
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return list.size();
            }

            public Integer get(int index) {
                return list.get(index);
            }

            public void set(int index, Integer value) {
                list.set(index, value);
            }

            public Comparator<Integer> getComparator() {
                return Integer::compareTo;
            }
        };

        // Create the QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();

        // Call the sort method
        quickSort.sort(sortable);

        // Check that the list is still empty
        assertEquals(0, list.size());
    }

    @Test
    public void testSortSingleElement() {
        // Create a list with one element
        List<Integer> list = new ArrayList<>();
        list.add(42);

        // Create a SortableCollection from the list
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return list.size();
            }

            public Integer get(int index) {
                return list.get(index);
            }

            public void set(int index, Integer value) {
                list.set(index, value);
            }

            public Comparator<Integer> getComparator() {
                return Integer::compareTo;
            }
        };

        // Create the QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();

        // Call the sort method
        quickSort.sort(sortable);

        // Check that the list still has one element
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(42), list.get(0));
    }

    @Test
    public void testSortAlreadySorted() {
        // Create a list that is already sorted
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);

        // Create a SortableCollection from the list
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return list.size();
            }

            public Integer get(int index) {
                return list.get(index);
            }

            public void set(int index, Integer value) {
                list.set(index, value);
            }

            public Comparator<Integer> getComparator() {
                return Integer::compareTo;
            }
        };

        // Create the QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();

        // Call the sort method
        quickSort.sort(sortable);

        // Check that the list is still sorted
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
        assertEquals(Integer.valueOf(4), list.get(3));
        assertEquals(Integer.valueOf(5), list.get(4));
    }

    @Test
    public void testSortReverseSorted() {
        // Create a list that is reverse sorted
        List<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(4);
        list.add(3);
        list.add(2);
        list.add(1);

        // Create a SortableCollection from the list
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            public int size() {
                return list.size();
            }

            public Integer get(int index) {
                return list.get(index);
            }

            public void set(int index, Integer value) {
                list.set(index, value);
            }

            public Comparator<Integer> getComparator() {
                return Integer::compareTo;
            }
        };

        // Create the QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();

        // Call the sort method
        quickSort.sort(sortable);

        // Check that the list is now sorted
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
        assertEquals(Integer.valueOf(4), list.get(3));
        assertEquals(Integer.valueOf(5), list.get(4));
    }
}
