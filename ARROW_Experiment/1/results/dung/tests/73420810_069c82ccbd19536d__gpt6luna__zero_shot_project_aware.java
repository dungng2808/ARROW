package rs.exproit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;

public class RandomizedBag_ArrowTest {

    @Test
    public void emptyBagHasSizeZero() {
        RandomizedBag<String> bag = new RandomizedBag<String>();
        assertEquals(0, bag.size());
        assertTrue(bag.isEmpty());
    }

    @Test
    public void seededBagPreservesElementsAndDuplicates() {
        RandomizedBag<String> bag = new RandomizedBag<String>(17L,
                Arrays.asList("a", "b", "a"));

        assertEquals(3, bag.size());
        assertTrue(bag.contains("a"));
        assertTrue(bag.contains("b"));
    }

    @Test
    public void addAllowsDuplicatesAndReturnsTrue() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);

        assertTrue(bag.add("x"));
        assertTrue(bag.add("x"));
        assertEquals(2, bag.size());
    }

    @Test(expected = NullPointerException.class)
    public void addRejectsNull() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.add(null);
    }

    @Test
    public void removeReturnsAnElementAndDecreasesSize() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L,
                Arrays.asList("a", "b", "c"));

        String removed = bag.remove();

        assertTrue(Arrays.asList("a", "b", "c").contains(removed));
        assertEquals(2, bag.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void removeFromEmptyBagThrows() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.remove();
    }

    @Test
    public void iteratorContainsAllElements() {
        RandomizedBag<String> bag = new RandomizedBag<String>(2L,
                Arrays.asList("a", "b", "c"));
        Iterator<String> iterator = bag.iterator();
        ArrayList<String> values = new ArrayList<String>();

        while (iterator.hasNext()) {
            values.add(iterator.next());
        }

        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void collectionConstructorCreatesIndependentBag() {
        ArrayList<String> source = new ArrayList<String>(Arrays.asList("a", "b"));
        RandomizedBag<String> bag = new RandomizedBag<String>(source);
        source.add("c");

        assertEquals(2, bag.size());
    }
}
