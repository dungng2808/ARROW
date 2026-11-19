package io.github.jhipster.service.filter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
        original.setIn(Arrays.asList("a", "b"));
        original.setNotIn(Arrays.asList("c", "d"));

        Filter<String> copy = new Filter<>(original);

        assertEquals(original.getEquals(), copy.getEquals());
        assertEquals(original.getNotEquals(), copy.getNotEquals());
        assertEquals(original.getSpecified(), copy.getSpecified());
        assertEquals(original.getIn(), copy.getIn());
        assertEquals(original.getNotIn(), copy.getNotIn());
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
    public void testSetAndGetEquals() {
        Filter<String> filter = new Filter<>();
        filter.setEquals("testValue");
        assertEquals("testValue", filter.getEquals());
    }

    @Test
    public void testSetAndGetNotEquals() {
        Filter<String> filter = new Filter<>();
        filter.setNotEquals("testValue");
        assertEquals("testValue", filter.getNotEquals());
    }

    @Test
    public void testSetAndGetSpecified() {
        Filter<String> filter = new Filter<>();
        filter.setSpecified(true);
        assertEquals(true, filter.getSpecified());
    }

    @Test
    public void testSetAndGetIn() {
        Filter<String> filter = new Filter<>();
        List<String> inList = Arrays.asList("a", "b");
        filter.setIn(inList);
        assertEquals(inList, filter.getIn());
    }

    @Test
    public void testSetAndGetNotIn() {
        Filter<String> filter = new Filter<>();
        List<String> notInList = Arrays.asList("c", "d");
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

        assertTrue(filter1.equals(filter2));
        assertTrue(filter2.equals(filter1));
        assertEquals(filter1.hashCode(), filter2.hashCode());

        assertFalse(filter1.equals(filter3));
        assertFalse(filter3.equals(filter1));
    }

    @Test
    public void testEqualsWithNulls() {
        Filter<String> filter1 = new Filter<>();
        Filter<String> filter2 = new Filter<>();

        assertTrue(filter1.equals(filter2));
        assertEquals(filter1.hashCode(), filter2.hashCode());
    }

    @Test
    public void testToString() {
        Filter<String> filter = new Filter<>();
        filter.setEquals("test");
        filter.setSpecified(true);

        String result = filter.toString();
        assertTrue(result.contains("Filter ["));
        assertTrue(result.contains("equals=test"));
        assertTrue(result.contains("specified=true"));
    }

    @Test
    public void testToStringWithNullValues() {
        Filter<String> filter = new Filter<>();
        String result = filter.toString();
        assertTrue(result.contains("Filter ["));
        assertFalse(result.contains("equals="));
        assertFalse(result.contains("notEquals="));
        assertFalse(result.contains("specified="));
        assertFalse(result.contains("in="));
        assertFalse(result.contains("notIn="));
    }

    @Test
    public void testGetFilterName() {
        Filter<String> filter = new Filter<>();
        assertEquals("Filter", filter.getFilterName());
    }

    @Test
    public void testInListIsCopied() {
        Filter<String> filter = new Filter<>();
        List<String> originalList = new ArrayList<>();
        originalList.add("item1");
        filter.setIn(originalList);

        // Modify original list
        originalList.add("item2");

        // Check that the filter's list is independent
        assertEquals(1, filter.getIn().size());
        assertEquals("item1", filter.getIn().get(0));
    }

    @Test
    public void testNotInListIsCopied() {
        Filter<String> filter = new Filter<>();
        List<String> originalList = new ArrayList<>();
        originalList.add("item1");
        filter.setNotIn(originalList);

        // Modify original list
        originalList.add("item2");

        // Check that the filter's list is independent
        assertEquals(1, filter.getNotIn().size());
        assertEquals("item1", filter.getNotIn().get(0));
    }
}
