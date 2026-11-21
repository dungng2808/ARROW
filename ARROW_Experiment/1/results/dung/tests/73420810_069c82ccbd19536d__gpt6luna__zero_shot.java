package rs.exproit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;
import static org.junit.Assert.*;

public class RandomizedBag_ArrowTest {

    @Test
    public void addAndSize() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        assertTrue(bag.add("alpha"));
        assertEquals(1, bag.size());
        assertTrue(bag.add("beta"));
        assertEquals(2, bag.size());
    }

    @Test
    public void addAllowsDuplicates() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.add("same");
        bag.add("same");
        assertEquals(2, bag.size());
    }

    @Test(expected = NullPointerException.class)
    public void addRejectsNull() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.add(null);
    }

    @Test
    public void removeReturnsAnAddedElementAndDecreasesSize() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.add("alpha");
        bag.add("beta");
        String removed = bag.remove();
        assertTrue(Arrays.asList("alpha", "beta").contains(removed));
        assertEquals(1, bag.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void removeFromEmptyBagThrows() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.remove();
    }

    @Test
    public void iteratorContainsAllElements() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L);
        bag.add("alpha");
        bag.add("beta");
        bag.add("gamma");

        Iterator<String> iterator = bag.iterator();
        ArrayList<String> actual = new ArrayList<String>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }

        assertEquals(3, actual.size());
        assertTrue(actual.contains("alpha"));
        assertTrue(actual.contains("beta"));
        assertTrue(actual.contains("gamma"));
    }

    @Test
    public void collectionConstructorCopiesElements() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L, Arrays.asList("alpha", "beta"));
        assertEquals(2, bag.size());
        assertTrue(Arrays.asList("alpha", "beta").contains(bag.remove()));
        assertEquals(1, bag.size());
    }

    @Test
    public void emptyCollectionConstructorCreatesEmptyBag() {
        RandomizedBag<String> bag = new RandomizedBag<String>(1L, new ArrayList<String>());
        assertEquals(0, bag.size());
    }
}
