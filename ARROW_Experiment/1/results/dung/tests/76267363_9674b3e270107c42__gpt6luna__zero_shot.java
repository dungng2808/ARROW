package io.github.jhipster.service.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Filter_ArrowTest {

    @Test
    public void defaultConstructorInitializesFieldsToNull() {
        Filter<String> filter = new Filter<String>();

        assertEquals(null, filter.getEquals());
        assertEquals(null, filter.getNotEquals());
        assertEquals(null, filter.getSpecified());
        assertEquals(null, filter.getIn());
        assertEquals(null, filter.getNotIn());
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
    public void settersCanResetValuesToNull() {
        Filter<String> filter = new Filter<String>()
                .setEquals("value")
                .setNotEquals("other")
                .setSpecified(Boolean.FALSE)
                .setIn(Arrays.asList("one"))
                .setNotIn(Arrays.asList("two"));

        filter.setEquals(null);
        filter.setNotEquals(null);
        filter.setSpecified(null);
        filter.setIn(null);
        filter.setNotIn(null);

        assertEquals(null, filter.getEquals());
        assertEquals(null, filter.getNotEquals());
        assertEquals(null, filter.getSpecified());
        assertEquals(null, filter.getIn());
        assertEquals(null, filter.getNotIn());
    }

    @Test
    public void copyDuplicatesValuesAndLists() {
        List<String> included = new ArrayList<String>(Arrays.asList("one", "two"));
        List<String> excluded = new ArrayList<String>(Arrays.asList("three"));
        Filter<String> original = new Filter<String>()
                .setEquals("equal")
                .setNotEquals("different")
                .setSpecified(Boolean.FALSE)
                .setIn(included)
                .setNotIn(excluded);

        Filter<String> copy = original.copy();

        assertEquals(original, copy);
        assertNotSame(original, copy);
        assertNotSame(included, copy.getIn());
        assertNotSame(excluded, copy.getNotIn());

        included.add("added");
        excluded.clear();

        assertEquals(Arrays.asList("one", "two"), copy.getIn());
        assertEquals(Arrays.asList("three"), copy.getNotIn());
    }

    @Test
    public void copyPreservesNullLists() {
        Filter<String> copy = new Filter<String>().copy();

        assertEquals(null, copy.getIn());
        assertEquals(null, copy.getNotIn());
    }

    @Test
    public void copyConstructorCopiesValuesAndLists() {
        List<String> included = new ArrayList<String>(Arrays.asList("one"));
        List<String> excluded = new ArrayList<String>(Arrays.asList("two"));
        Filter<String> original = new Filter<String>()
                .setEquals("equal")
                .setNotEquals("different")
                .setSpecified(Boolean.TRUE)
                .setIn(included)
                .setNotIn(excluded);

        Filter<String> copy = new Filter<String>(original);

        assertEquals(original, copy);
        assertNotSame(included, copy.getIn());
        assertNotSame(excluded, copy.getNotIn());
    }

    @Test
    public void equalsAndHashCodeUseAllFields() {
        Filter<String> first = new Filter<String>()
                .setEquals("equal")
                .setNotEquals("different")
                .setSpecified(Boolean.TRUE)
                .setIn(Arrays.asList("one", "two"))
                .setNotIn(Arrays.asList("three"));
        Filter<String> same = new Filter<String>()
                .setEquals("equal")
                .setNotEquals("different")
                .setSpecified(Boolean.TRUE)
                .setIn(Arrays.asList("one", "two"))
                .setNotIn(Arrays.asList("three"));

        assertTrue(first.equals(first));
        assertTrue(first.equals(same));
        assertTrue(same.equals(first));
        assertEquals(first.hashCode(), same.hashCode());

        same.setNotIn(Arrays.asList("changed"));
        assertFalse(first.equals(same));
    }

    @Test
    public void equalsRejectsNullAndDifferentClass() {
        Filter<String> filter = new Filter<String>();

        assertFalse(filter.equals(null));
        assertFalse(filter.equals("not a filter"));
    }

    @Test
    public void toStringIncludesSetFieldsInOrder() {
        Filter<String> filter = new Filter<String>()
                .setEquals("a")
                .setNotEquals("b")
                .setSpecified(Boolean.TRUE)
                .setIn(Arrays.asList("c", "d"))
                .setNotIn(Arrays.asList("e"));

        assertEquals("Filter [equals=a, notEquals=b, specified=true, in=[c, d], notIn=[e]]", filter.toString());
    }

    @Test
    public void toStringOmitsUnsetFields() {
        Filter<String> filter = new Filter<String>();

        assertEquals("Filter []", filter.toString());
    }

    @Test
    public void toStringIncludesEmptyListsAndFalseSpecified() {
        Filter<String> filter = new Filter<String>()
                .setSpecified(Boolean.FALSE)
                .setIn(new ArrayList<String>())
                .setNotIn(new ArrayList<String>());

        assertEquals("Filter [specified=false, in=[], notIn=[]]", filter.toString());
    }
}
