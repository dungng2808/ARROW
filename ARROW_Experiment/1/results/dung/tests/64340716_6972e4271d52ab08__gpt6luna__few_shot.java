package io.katharsis.queryspec;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SortSpec_ArrowTest {

    @Test
    public void ascCreatesAscendingSortSpec() {
        SortSpec sortSpec = SortSpec.asc(Arrays.asList("name"));

        assertEquals(Direction.ASC, sortSpec.getDirection());
        assertEquals("name ASC", sortSpec.toString());
    }

    @Test
    public void descCreatesDescendingSortSpec() {
        SortSpec sortSpec = SortSpec.desc(Arrays.asList("name"));

        assertEquals(Direction.DESC, sortSpec.getDirection());
        assertEquals("name DESC", sortSpec.toString());
    }

    @Test
    public void constructorPreservesPathAndDirection() {
        SortSpec sortSpec = new SortSpec(Arrays.asList("author", "name"), Direction.ASC);

        assertEquals(Direction.ASC, sortSpec.getDirection());
        assertEquals("author.name ASC", sortSpec.toString());
    }

    @Test
    public void reverseChangesDirectionAndPreservesPath() {
        SortSpec sortSpec = SortSpec.asc(Arrays.asList("name"));

        SortSpec reversed = sortSpec.reverse();

        assertEquals(Direction.DESC, reversed.getDirection());
        assertEquals("name DESC", reversed.toString());
    }

    @Test
    public void reverseOfDescendingChangesToAscending() {
        SortSpec sortSpec = SortSpec.desc(Arrays.asList("name"));

        assertEquals(Direction.ASC, sortSpec.reverse().getDirection());
    }

    @Test
    public void equalSortSpecsHaveEqualHashCodes() {
        SortSpec first = SortSpec.asc(Arrays.asList("author", "name"));
        SortSpec second = new SortSpec(Arrays.asList("author", "name"), Direction.ASC);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void differentDirectionsAreNotEqual() {
        SortSpec ascending = SortSpec.asc(Arrays.asList("name"));
        SortSpec descending = SortSpec.desc(Arrays.asList("name"));

        assertFalse(ascending.equals(descending));
        assertNotEquals(ascending, descending);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullPath() {
        new SortSpec(null, Direction.ASC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyPath() {
        new SortSpec(Collections.<String>emptyList(), Direction.ASC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullDirection() {
        new SortSpec(Arrays.asList("name"), null);
    }
}
