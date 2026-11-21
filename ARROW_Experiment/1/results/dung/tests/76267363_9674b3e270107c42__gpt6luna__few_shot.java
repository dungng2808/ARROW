package io.github.jhipster.service.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Filter_ArrowTest {

    @Test
    public void defaultFilterHasNullFieldsAndExpectedString() {
        Filter<String> filter = new Filter<String>();

        assertNull(filter.getEquals());
        assertNull(filter.getNotEquals());
        assertNull(filter.getSpecified());
        assertNull(filter.getIn());
        assertNull(filter.getNotIn());
        assertEquals("Filter []", filter.toString());
    }

    @Test
    public void settersStoreValuesAndReturnSameFilter() {
        Filter<String> filter = new Filter<String>();
        List<String> included = Arrays.asList("one", "two");
        List<String> excluded = Arrays.asList("three");

        assertSame(filter, filter.setEquals("equal"));
        assertSame(filter, filter.setNotEquals("different"));
        assertSame(filter, filter.setSpecified(Boolean.TRUE));
        assertSame(filter, filter.setIn(included));
        assertSame(filter, filter.setNotIn(excluded));

        assertEquals("equal", filter.getEquals());
        assertEquals("different", filter.getNotEquals());
        assertEquals(Boolean.TRUE, filter.getSpecified());
        assertSame(included, filter.getIn());
        assertSame(excluded, filter.getNotIn());
    }

    @Test
    public void toStringIncludesSetFields() {
        Filter<String> filter = new Filter<String>()
                .setEquals("a")
                .setNotEquals("b")
                .setSpecified(Boolean.FALSE)
                .setIn(Arrays.asList("c", "d"))
                .setNotIn(Arrays.asList("e"));

        assertEquals("Filter [equals=a, notEquals=b, specified=false, in=[c, d], notIn=[e]]",
                filter.toString());
    }

    @Test
    public void copyPreservesValuesAndCopiesLists() {
        List<String> included = new ArrayList<String>(Arrays.asList("one", "two"));
        List<String> excluded = new ArrayList<String>(Arrays.asList("three"));
        Filter<String> original = new Filter<String>()
                .setEquals("equal")
                .setNotEquals("different")
                .setSpecified(Boolean.TRUE)
                .setIn(included)
                .setNotIn(excluded);

        Filter<String> copy = original.copy();

        assertEquals(original, copy);
        assertNotEquals(original, new Filter<String>());
        assertNotSameLists(original, copy);

        copy.getIn().add("four");
        copy.getNotIn().add("five");
        assertEquals(Arrays.asList("one", "two"), original.getIn());
        assertEquals(Arrays.asList("three"), original.getNotIn());
    }

    @Test
    public void copyPreservesNullLists() {
        Filter<String> copy = new Filter<String>(new Filter<String>());

        assertNull(copy.getIn());
        assertNull(copy.getNotIn());
        assertEquals(new Filter<String>(), copy);
    }

    @Test
    public void equalityAndHashCodeUseAllFields() {
        Filter<String> first = new Filter<String>()
                .setEquals("a")
                .setNotEquals("b")
                .setSpecified(Boolean.TRUE)
                .setIn(Arrays.asList("c"))
                .setNotIn(Arrays.asList("d"));
        Filter<String> second = new Filter<String>()
                .setEquals("a")
                .setNotEquals("b")
                .setSpecified(Boolean.TRUE)
                .setIn(Arrays.asList("c"))
                .setNotIn(Arrays.asList("d"));

        assertTrue(first.equals(first));
        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(null));
        assertFalse(first.equals("not a filter"));
        assertFalse(first.equals(new Filter<String>().setEquals("other")));
    }

    private void assertNotSameLists(Filter<String> first, Filter<String> second) {
        assertNotNull(first.getIn());
        assertNotNull(second.getIn());
        assertFalse(first.getIn() == second.getIn());
        assertNotNull(first.getNotIn());
        assertNotNull(second.getNotIn());
        assertFalse(first.getNotIn() == second.getNotIn());
    }
}
