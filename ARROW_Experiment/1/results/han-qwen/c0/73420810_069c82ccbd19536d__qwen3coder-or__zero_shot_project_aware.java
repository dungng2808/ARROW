package rs.exproit.util;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class RandomizedBag_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        assertNotNull(bag);
        assertEquals(0, bag.size());
    }

    @Test
    public void testSeedConstructor() {
        RandomizedBag<String> bag = new RandomizedBag<>(42L);
        assertNotNull(bag);
        assertEquals(0, bag.size());
    }

    @Test
    public void testCollectionConstructor() {
        Collection<String> collection = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<>(collection);
        assertNotNull(bag);
        assertEquals(3, bag.size());
    }

    @Test
    public void testSeedAndCollectionConstructor() {
        Collection<String> collection = Arrays.asList("x", "y", "z");
        RandomizedBag<String> bag = new RandomizedBag<>(123L, collection);
        assertNotNull(bag);
        assertEquals(3, bag.size());
    }

    @Test
    public void testAddNullElement() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        try {
            bag.add(null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testAddElements() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        assertTrue(bag.add("first"));
        assertTrue(bag.add("second"));
        assertTrue(bag.add("third"));
        assertEquals(3, bag.size());
    }

    @Test
    public void testRemoveEmptyBag() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        try {
            bag.remove();
            fail("Should have thrown NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertEquals("Cannot remove an element from an empty bag.", e.getMessage());
        }
    }

    @Test
    public void testRemoveElements() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        bag.add("first");
        bag.add("second");
        bag.add("third");

        String removed = bag.remove();
        assertNotNull(removed);
        assertEquals(2, bag.size());
    }

    @Test
    public void testIterator() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        bag.add("a");
        bag.add("b");
        bag.add("c");

        Iterator<String> iter = bag.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());

        List<String> elements = new ArrayList<>();
        while (iter.hasNext()) {
            elements.add(iter.next());
        }
        assertEquals(3, elements.size());
    }

    @Test
    public void testIteratorConcurrentModification() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        bag.add("a");
        bag.add("b");

        Iterator<String> iter = bag.iterator();
        bag.add("c"); // Modify bag during iteration

        try {
            iter.next();
            fail("Should have thrown ConcurrentModificationException");
        } catch (ConcurrentModificationException e) {
            // Expected
        }
    }

    @Test
    public void testSize() {
        RandomizedBag<String> bag = new RandomizedBag<>();
        assertEquals(0, bag.size());

        bag.add("one");
        assertEquals(1, bag.size());

        bag.add("two");
        bag.add("three");
        assertEquals(3, bag.size());
    }

    @Test
    public void testAddAndShuffleElementsInPlace() {
        Collection<String> collection = Arrays.asList("x", "y", "z");
        RandomizedBag<String> bag = new RandomizedBag<>(collection);
        assertEquals(3, bag.size());
    }

    @Test
    public void testMultipleOperations() {
        RandomizedBag<Integer> bag = new RandomizedBag<>();
        bag.add(1);
        bag.add(2);
        bag.add(3);
        assertEquals(3, bag.size());

        Integer removed = bag.remove();
        assertNotNull(removed);
        assertEquals(2, bag.size());

        bag.add(4);
        bag.add(5);
        assertEquals(4, bag.size());

        Iterator<Integer> iter = bag.iterator();
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals(4, count);
    }
}
