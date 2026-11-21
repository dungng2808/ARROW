package rs.exproit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RandomizedBag_ArrowTest {

    @Test
    public void newBagIsEmpty() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        assertTrue(bag.isEmpty());
        assertEquals(0, bag.size());
    }

    @Test
    public void addAllowsDuplicatesAndPreservesAllElements() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        assertTrue(bag.add("item"));
        assertTrue(bag.add("item"));
        assertEquals(2, bag.size());

        List<String> values = new ArrayList<String>();
        for (String value : bag) {
            values.add(value);
        }
        assertEquals(Arrays.asList("item", "item"), values);
    }

    @Test
    public void constructorCopiesCollectionElements() {
        List<String> source = Arrays.asList("a", "b", "c");
        RandomizedBag<String> bag = new RandomizedBag<String>(1L, source);

        assertEquals(3, bag.size());
        List<String> values = new ArrayList<String>();
        for (String value : bag) {
            values.add(value);
        }
        assertEquals(3, values.size());
        assertTrue(values.containsAll(source));
    }

    @Test
    public void iteratorReturnsAllElements() {
        RandomizedBag<Integer> bag = new RandomizedBag<Integer>(2L);
        bag.add(1);
        bag.add(2);
        bag.add(3);

        Iterator<Integer> iterator = bag.iterator();
        List<Integer> values = new ArrayList<Integer>();
        while (iterator.hasNext()) {
            values.add(iterator.next());
        }

        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void removeReturnsAndDeletesAnElement() {
        RandomizedBag<String> bag = new RandomizedBag<String>(3L);
        bag.add("only");
        assertEquals("only", bag.remove());
        assertTrue(bag.isEmpty());
        assertEquals(0, bag.size());
    }

    @Test
    public void removeFromEmptyBagThrows() {
        RandomizedBag<String> bag = new RandomizedBag<String>(4L);
        try {
            bag.remove();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void addNullThrows() {
        RandomizedBag<String> bag = new RandomizedBag<String>(5L);
        try {
            bag.add(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals(null, expected.getMessage());
        }
    }
}
