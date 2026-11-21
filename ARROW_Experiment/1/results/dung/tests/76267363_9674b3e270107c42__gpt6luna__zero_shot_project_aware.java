package io.github.jhipster.service.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class Filter_ArrowTest {

    @Test
    public void defaultConstructorInitializesFieldsToNull() {
        Filter<String> filter = new Filter<>();

        assertEquals(null, filter.getEquals());
        assertEquals(null, filter.getNotEquals());
        assertEquals(null, filter.getSpecified());
        assertEquals(null, filter.getIn());
        assertEquals(null, filter.getNotIn());
    }

    @Test
    public void settersStoreValuesAndReturnSameFilter() {
        Filter<String> filter = new Filter<>();
        assertSame(filter, filter.setEquals("equal"));
        assertSame(filter, filter.setNotEquals("different"));
        assertSame(filter, filter.setSpecified(Boolean.TRUE));
        assertSame(filter, filter.setIn(Arrays.asList("one", "two")));
        assertSame(filter, filter.setNotIn(Collections.singletonList("three")));

        assertEquals("equal", filter.getEquals());
        assertEquals("different", filter.getNotEquals());
        assertEquals(Boolean.TRUE, filter.getSpecified());
        assertEquals(Arrays.asList("one", "two"), filter.getIn());
        assertEquals(Collections.singletonList("three"), filter.getNotIn());
    }

    @Test
    public void settersAcceptNullValues() {
        Filter<String> filter = new Filter<>();
        filter.setEquals("value").setNotEquals("other").setSpecified(Boolean.FALSE)
            .setIn(Collections.singletonList("in")).setNotIn(Collections.singletonList("notIn"));

        filter.setEquals(null).setNotEquals(null).setSpecified(null).setIn(null).setNotIn(null);

        assertEquals(null, filter.getEquals());
        assertEquals(null, filter.getNotEquals());
        assertEquals(null, filter.getSpecified());
        assertEquals(null, filter.getIn());
        assertEquals(null, filter.getNotIn());
    }

    @Test
    public void copyConstructorCopiesValuesAndCreatesIndependentLists() {
        ArrayList<String> in = new ArrayList<>(Arrays.asList("a", "b"));
        ArrayList<String> notIn = new ArrayList<>(Collections.singletonList("c"));
        Filter<String> original = new Filter<String>()
            .setEquals("equal")
            .setNotEquals("notEqual")
            .setSpecified(Boolean.FALSE)
            .setIn(in)
            .setNotIn(notIn);

        Filter<String> copy = new Filter<>(original);

        assertEquals("equal", copy.getEquals());
        assertEquals("notEqual", copy.getNotEquals());
        assertEquals(Boolean.FALSE, copy.getSpecified());
        assertEquals(Arrays.asList("a", "b"), copy.getIn());
        assertEquals(Collections.singletonList("c"), copy.getNotIn());
        assertNotSame(in, copy.getIn());
        assertNotSame(notIn, copy.getNotIn());

        in.add("later");
        notIn.clear();
        assertEquals(Arrays.asList("a", "b"), copy.getIn());
        assertEquals(Collections.singletonList("c"), copy.getNotIn());
    }

    @Test
    public void copyConstructorPreservesNullLists() {
        Filter<String> copy = new Filter<>(new Filter<String>());

        assertEquals(null, copy.getIn());
        assertEquals(null, copy.getNotIn());
    }

    @Test
    public void copyReturnsEqualIndependentFilter() {
        Filter<String> original = new Filter<String>()
            .setEquals("value")
            .setIn(new ArrayList<>(Arrays.asList("a", "b")));
        Filter<String> copy = original.copy();

        assertNotSame(original, copy);
        assertEquals(original, copy);
        assertNotSame(original.getIn(), copy.getIn());
    }

    @Test
    public void equalsRecognizesSameAndEquivalentFilters() {
        Filter<String> first = new Filter<String>()
            .setEquals("value")
            .setNotEquals("other")
            .setSpecified(Boolean.TRUE)
            .setIn(Arrays.asList("a", "b"))
            .setNotIn(Collections.singletonList("c"));
        Filter<String> second = new Filter<String>()
            .setEquals("value")
            .setNotEquals("other")
            .setSpecified(Boolean.TRUE)
            .setIn(Arrays.asList("a", "b"))
            .setNotIn(Collections.singletonList("c"));

        assertTrue(first.equals(first));
        assertTrue(first.equals(second));
        assertTrue(second.equals(first));
    }

    @Test
    public void equalsRejectsNullDifferentClassAndDifferentValues() {
        Filter<String> filter = new Filter<String>().setEquals("value");

        assertFalse(filter.equals(null));
        assertFalse(filter.equals("value"));
        assertFalse(filter.equals(new Filter<String>().setEquals("different")));
    }

    @Test
    public void hashCodeMatchesForEqualFilters() {
        Filter<String> first = new Filter<String>()
            .setEquals("value")
            .setNotEquals("other")
            .setSpecified(Boolean.FALSE)
            .setIn(Arrays.asList("a", "b"))
            .setNotIn(Collections.singletonList("c"));
        Filter<String> second = new Filter<String>()
            .setEquals("value")
            .setNotEquals("other")
            .setSpecified(Boolean.FALSE)
            .setIn(Arrays.asList("a", "b"))
            .setNotIn(Collections.singletonList("c"));

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void hashCodeChangesWhenFilterValueChanges() {
        Filter<String> first = new Filter<String>().setEquals("first");
        Filter<String> second = new Filter<String>().setEquals("second");

        assertNotEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toStringIncludesOnlySetFieldsInOrder() {
        Filter<String> filter = new Filter<String>()
            .setEquals("a")
            .setNotEquals("b")
            .setSpecified(Boolean.TRUE)
            .setIn(Arrays.asList("c", "d"))
            .setNotIn(Collections.singletonList("e"));

        assertEquals("Filter [equals=a, notEquals=b, specified=true, in=[c, d], notIn=[e]]", filter.toString());
    }

    @Test
    public void toStringForEmptyFilterHasNoFieldEntries() {
        Filter<String> filter = new Filter<>();

        assertEquals("Filter []", filter.toString());
    }
}
