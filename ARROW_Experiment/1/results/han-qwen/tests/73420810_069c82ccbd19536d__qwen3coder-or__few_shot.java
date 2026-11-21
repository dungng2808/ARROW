package rs.exproit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

public class RandomizedBag_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertNotNull(bag);
        assertEquals(0, bag.size());
    }

    @Test
    public void testSeedConstructor() {
        RandomizedBag<String> bag = new RandomizedBag<String>(12345L);
        assertNotNull(bag);
        assertEquals(0, bag.size());
    }

    @Test
    public void testCollectionConstructor() {
        List<String> elements = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>(elements);
        assertNotNull(bag);
        assertEquals(3, bag.size());
    }

    @Test
    public void testSeedAndCollectionConstructor() {
        List<String> elements = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>(12345L, elements);
        assertNotNull(bag);
        assertEquals(3, bag.size());
    }

    @Test
    public void testAdd() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertTrue(bag.add("first"));
        assertTrue(bag.add("second"));
        assertEquals(2, bag.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullElement() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add(null);
    }

    @Test
    public void testRemove() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("first");
        bag.add("second");
        bag.add("third");
        
        String removed = bag.remove();
        assertNotNull(removed);
        assertEquals(2, bag.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveEmptyBag() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.remove();
    }

    @Test
    public void testIterator() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("first");
        bag.add("second");
        bag.add("third");
        
        Iterator<String> iter = bag.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
    }

    @Test
    public void testIteratorEmptyBag() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        Iterator<String> iter = bag.iterator();
        assertFalse(iter.hasNext());
    }

    @Test
    public void testSize() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertEquals(0, bag.size());
        
        bag.add("first");
        assertEquals(1, bag.size());
        
        bag.add("second");
        assertEquals(2, bag.size());
    }

    @Test
    public void testAddAndShuffleElementsInPlace() {
        List<String> elements = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.addAndshuffleElementsInPlace(elements);
        assertEquals(3, bag.size());
    }

    @Test
    public void testMultipleOperations() {
        RandomizedBag<Integer> bag = new RandomizedBag<Integer>();
        bag.add(1);
        bag.add(2);
        bag.add(3);
        bag.add(4);
        bag.add(5);
        
        assertEquals(5, bag.size());
        
        Integer removed = bag.remove();
        assertNotNull(removed);
        assertEquals(4, bag.size());
        
        Iterator<Integer> iter = bag.iterator();
        assertTrue(iter.hasNext());
        while (iter.hasNext()) {
            iter.next();
        }
    }
}
