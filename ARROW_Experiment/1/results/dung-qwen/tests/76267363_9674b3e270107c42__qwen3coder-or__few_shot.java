package io.github.jhipster.service.filter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class Filter_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        Filter<String> filter = new Filter<>();
        assertNull(filter.getEquals());
        assertNull(filter.getNotEquals());
        assertNull(filter.getSpecified());
        assertNull(filter.getIn());
        assertNull(filter.getNotIn());
    }

    @Test
    public void testCopyConstructor() {
        Filter<String> original = new Filter<>();
        original.setEquals("test");
        original.setNotEquals("notTest");
        original.setSpecified(true);
        List<String> inList = new ArrayList<>();
        inList.add("a");
        inList.add("b");
        original.setIn(inList);
        List<String> notInList = new ArrayList<>();
        notInList.add("c");
        notInList.add("d");
        original.setNotIn(notInList);

        Filter<String> copy = new Filter<>(original);

        assertEquals(original.getEquals(), copy.getEquals());
        assertEquals(original.getNotEquals(), copy.getNotEquals());
        assertEquals(original.getSpecified(), copy.getSpecified());
        assertEquals(original.getIn(), copy.getIn());
        assertEquals(original.getNotIn(), copy.getNotIn());

        // Ensure deep copy for lists
        assertNotSame(original.getIn(), copy.getIn());
        assertNotSame(original.getNotIn(), copy.getNotIn());
    }

    @Test
    public void testCopyMethod() {
        Filter<String> original = new Filter<>();
        original.setEquals("test");

        Filter<String> copy = original.copy();

        assertEquals(original.getEquals(), copy.getEquals());
        assertNotSame(original, copy);
    }

    @Test
    public void testSettersAndGetters() {
        Filter<Integer> filter = new Filter<>();

        // Test equals setter/getter
        filter.setEquals(42);
        assertEquals(Integer.valueOf(42), filter.getEquals());

        // Test notEquals setter/getter
        filter.setNotEquals(24);
        assertEquals(Integer.valueOf(24), filter.getNotEquals());

        // Test specified setter/getter
        filter.setSpecified(false);
        assertEquals(Boolean.FALSE, filter.getSpecified());

        // Test in setter/getter
        List<Integer> inList = new ArrayList<>();
        inList.add(1);
        inList.add(2);
        filter.setIn(inList);
        assertEquals(inList, filter.getIn());

        // Test notIn setter/getter
        List<Integer> notInList = new ArrayList<>();
        notInList.add(3);
        notInList.add(4);
        filter.setNotIn(notInList);
        assertEquals(notInList, filter.getNotIn());
    }

    @Test
    public void testEqualsAndHashCode() {
        Filter<String> filter1 = new Filter<>();
        filter1.setEquals("test");
        filter1.setSpecified(true);

        Filter<String> filter2 = new Filter<>();
        filter2.setEquals("test");
        filter2.setSpecified(true);

        Filter<String> filter3 = new Filter<>();
        filter3.setEquals("different");
        filter3.setSpecified(false);

        // Test equality
        assertEquals(filter1, filter2);
        assertNotEquals(filter1, filter3);
        assertNotEquals(filter1, null);
        assertEquals(filter1, filter1);

        // Test hash code consistency
        assertEquals(filter1.hashCode(), filter2.hashCode());
        assertNotEquals(filter1.hashCode(), filter3.hashCode());
    }

    @Test
    public void testToString() {
        Filter<String> filter = new Filter<>();
        filter.setEquals("test");
        filter.setSpecified(true);

        String result = filter.toString();
        assertNotNull(result);
        assertTrue(result.contains("Filter"));
        assertTrue(result.contains("equals=test"));
        assertTrue(result.contains("specified=true"));
    }

    @Test
    public void testGetFilterName() {
        Filter<String> filter = new Filter<>();
        String name = filter.getFilterName();
        assertEquals("Filter", name);
    }
}
