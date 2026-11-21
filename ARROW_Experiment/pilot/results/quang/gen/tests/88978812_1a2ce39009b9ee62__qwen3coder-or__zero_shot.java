package packt.java9.by.example.ch03.quick;

import org.junit.Test;
import packt.java9.by.example.ch03.AbstractSort;
import packt.java9.by.example.ch03.SortableCollection;
import packt.java9.by.example.ch03.qsort.FJQuickSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class QuickSort_ArrowTest {

    @Test
    public void testSort() {
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Create a SortableCollection with some unsorted integers
        List<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(2);
        list.add(8);
        list.add(1);
        list.add(9);
        
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public Integer get(int index) {
                return list.get(index);
            }

            @Override
            public void set(int index, Integer value) {
                list.set(index, value);
            }
        };
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Check if the list is sorted
        List<Integer> expected = new ArrayList<>();
        expected.add(1);
        expected.add(2);
        expected.add(5);
        expected.add(8);
        expected.add(9);
        
        assertEquals(expected, list);
    }

    @Test
    public void testSortEmptyList() {
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Create an empty SortableCollection
        List<Integer> list = new ArrayList<>();
        
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public Integer get(int index) {
                return list.get(index);
            }

            @Override
            public void set(int index, Integer value) {
                list.set(index, value);
            }
        };
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Check if the list is still empty
        assertTrue(list.isEmpty());
    }

    @Test
    public void testSortSingleElement() {
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Create a SortableCollection with a single element
        List<Integer> list = new ArrayList<>();
        list.add(42);
        
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public Integer get(int index) {
                return list.get(index);
            }

            @Override
            public void set(int index, Integer value) {
                list.set(index, value);
            }
        };
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Check if the list contains the same element
        assertEquals(Integer.valueOf(42), list.get(0));
    }

    @Test
    public void testSortAlreadySorted() {
        // Create a QuickSort instance
        QuickSort<Integer> quickSort = new QuickSort<>();
        
        // Create a SortableCollection with already sorted elements
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        
        SortableCollection<Integer> sortable = new SortableCollection<Integer>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public Integer get(int index) {
                return list.get(index);
            }

            @Override
            public void set(int index, Integer value) {
                list.set(index, value);
            }
        };
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Check if the list remains sorted
        List<Integer> expected = new ArrayList<>();
        expected.add(1);
        expected.add(2);
        expected.add(3);
        expected.add(4);
        expected.add(5);
        
        assertEquals(expected, list);
    }

    @Test
    public void testSortWithCustomComparator() {
        // Create a QuickSort instance with a custom comparator
        QuickSort<String> quickSort = new QuickSort<>();
        
        // Set a custom comparator that sorts strings by length
        Comparator<String> customComparator = Comparator.comparing(String::length);
        AbstractSort.setStringComparator(customComparator);
        
        // Create a SortableCollection with strings of different lengths
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("bb");
        list.add("ccc");
        list.add("d");
        
        SortableCollection<String> sortable = new SortableCollection<String>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public String get(int index) {
                return list.get(index);
            }

            @Override
            public void set(int index, String value) {
                list.set(index, value);
            }
        };
        
        // Call the sort method
        quickSort.sort(sortable);
        
        // Check if the list is sorted by length
        List<String> expected = new ArrayList<>();
        expected.add("a");
        expected.add("d");
        expected.add("bb");
        expected.add("ccc");
        
        assertEquals(expected, list);
    }
}
