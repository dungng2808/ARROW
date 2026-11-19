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
        RandomizedBag<String> bag = new RandomizedBag<String>(42L);
        assertNotNull(bag);
        assertEquals(0, bag.size());
    }

    @Test
    public void testCollectionConstructor() {
        Collection<String> collection = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>(collection);
        assertNotNull(bag);
        assertEquals(3, bag.size());
    }

    @Test
    public void testSeedAndCollectionConstructor() {
        Collection<String> collection = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>(42L, collection);
        assertNotNull(bag);
        assertEquals(3, bag.size());
    }

    @Test
    public void testAdd() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertTrue(bag.add("a"));
        assertEquals(1, bag.size());
        assertTrue(bag.add("b"));
        assertEquals(2, bag.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add(null);
    }

    @Test
    public void testRemove() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("a");
        bag.add("b");
        bag.add("c");
        
        String removed = bag.remove();
        assertNotNull(removed);
        assertEquals(2, bag.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveEmpty() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.remove();
    }

    @Test
    public void testIterator() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("a");
        bag.add("b");
        bag.add("c");
        
        Iterator<String> iter = bag.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
    }

    @Test
    public void testIteratorNext() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("a");
        bag.add("b");
        bag.add("c");
        
        Iterator<String> iter = bag.iterator();
        assertNotNull(iter.next());
        assertNotNull(iter.next());
        assertNotNull(iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testSize() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertEquals(0, bag.size());
        bag.add("a");
        assertEquals(1, bag.size());
        bag.add("b");
        assertEquals(2, bag.size());
    }

    @Test
    public void testIsEmpty() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertTrue(bag.isEmpty());
        bag.add("a");
        assertFalse(bag.isEmpty());
    }

    @Test
    public void testAddAndShuffleElementsInPlace() {
        Collection<String> collection = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.addAndshuffleElementsInPlace(collection);
        assertEquals(3, bag.size());
    }

    @Test
    public void testIteratorConcurrentModification() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("a");
        bag.add("b");
        bag.add("c");
        
        Iterator<String> iter = bag.iterator();
        bag.add("d"); // Modify bag while iterating
        
        try {
            iter.next();
            fail("Expected ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
            // Expected
        }
    }

    @Test
    public void testMultipleIterations() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        bag.add("a");
        bag.add("b");
        bag.add("c");
        
        Iterator<String> iter1 = bag.iterator();
        Iterator<String> iter2 = bag.iterator();
        
        assertTrue(iter1.hasNext());
        assertTrue(iter2.hasNext());
    }

    @Test
    public void testAddWithSeed() {
        RandomizedBag<String> bag1 = new RandomizedBag<String>(123L);
        RandomizedBag<String> bag2 = new RandomizedBag<String>(123L);
        
        bag1.add("a");
        bag1.add("b");
        bag2.add("a");
        bag2.add("b");
        
        assertEquals(bag1.size(), bag2.size());
    }

    @Test
    public void testRemoveOrdering() {
        RandomizedBag<Integer> bag = new RandomizedBag<Integer>();
        bag.add(1);
        bag.add(2);
        bag.add(3);
        bag.add(4);
        bag.add(5);
        
        // Remove all elements
        List<Integer> removed = new ArrayList<Integer>();
        while (!bag.isEmpty()) {
            removed.add(bag.remove());
        }
        
        assertEquals(5, removed.size());
        // Check that we got all elements
        assertTrue(removed.contains(1));
        assertTrue(removed.contains(2));
        assertTrue(removed.contains(3));
        assertTrue(removed.contains(4));
        assertTrue(removed.contains(5));
    }
}
