package io.katharsis.queryspec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SortSpec_ArrowTest {

    @Test
    public void constructorStoresDirection() {
        SortSpec spec = new SortSpec(Arrays.asList("author", "name"), Direction.ASC);

        assertEquals(Direction.ASC, spec.getDirection());
    }

    @Test
    public void ascCreatesAscendingSort() {
        SortSpec spec = SortSpec.asc(Arrays.asList("title"));

        assertEquals(Direction.ASC, spec.getDirection());
        assertEquals("title ASC", spec.toString());
    }

    @Test
    public void descCreatesDescendingSort() {
        SortSpec spec = SortSpec.desc(Arrays.asList("title"));

        assertEquals(Direction.DESC, spec.getDirection());
        assertEquals("title DESC", spec.toString());
    }

    @Test
    public void reverseChangesAscendingToDescending() {
        SortSpec spec = SortSpec.asc(Arrays.asList("author", "name"));

        SortSpec reversed = spec.reverse();

        assertEquals(Direction.DESC, reversed.getDirection());
        assertEquals("author.name DESC", reversed.toString());
    }

    @Test
    public void reverseChangesDescendingToAscending() {
        SortSpec spec = SortSpec.desc(Arrays.asList("author", "name"));

        SortSpec reversed = spec.reverse();

        assertEquals(Direction.ASC, reversed.getDirection());
        assertEquals("author.name ASC", reversed.toString());
    }

    @Test
    public void equalSpecsAreEqualAndHaveSameHashCode() {
        SortSpec first = SortSpec.asc(Arrays.asList("author", "name"));
        SortSpec second = SortSpec.asc(Arrays.asList("author", "name"));

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentDirectionsAreNotEqual() {
        SortSpec ascending = SortSpec.asc(Arrays.asList("title"));
        SortSpec descending = SortSpec.desc(Arrays.asList("title"));

        assertFalse(ascending.equals(descending));
    }

    @Test
    public void differentPathsAreNotEqual() {
        SortSpec first = SortSpec.asc(Arrays.asList("title"));
        SortSpec second = SortSpec.asc(Arrays.asList("name"));

        assertFalse(first.equals(second));
    }

    @Test
    public void specIsNotEqualToNull() {
        SortSpec spec = SortSpec.asc(Arrays.asList("title"));

        assertFalse(spec.equals(null));
    }

    @Test
    public void specIsNotEqualToDifferentClass() {
        SortSpec spec = SortSpec.asc(Arrays.asList("title"));

        assertFalse(spec.equals("title ASC"));
    }

    @Test
    public void constructorRejectsNullPath() {
        try {
            new SortSpec(null, Direction.ASC);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void constructorRejectsEmptyPath() {
        try {
            new SortSpec(Collections.<String>emptyList(), Direction.ASC);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void constructorRejectsNullDirection() {
        try {
            new SortSpec(Arrays.asList("title"), null);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
