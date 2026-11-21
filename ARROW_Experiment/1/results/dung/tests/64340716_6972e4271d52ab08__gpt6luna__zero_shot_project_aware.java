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
	public void ascCreatesAscendingSort() {
		SortSpec sortSpec = SortSpec.asc(Arrays.asList("name"));

		assertEquals(Direction.ASC, sortSpec.getDirection());
		assertEquals("name ASC", sortSpec.toString());
	}

	@Test
	public void descCreatesDescendingSort() {
		SortSpec sortSpec = SortSpec.desc(Arrays.asList("name"));

		assertEquals(Direction.DESC, sortSpec.getDirection());
		assertEquals("name DESC", sortSpec.toString());
	}

	@Test
	public void reverseChangesAscendingToDescending() {
		SortSpec sortSpec = SortSpec.asc(Arrays.asList("name"));

		assertEquals(Direction.DESC, sortSpec.reverse().getDirection());
		assertEquals("name DESC", sortSpec.reverse().toString());
	}

	@Test
	public void reverseChangesDescendingToAscending() {
		SortSpec sortSpec = SortSpec.desc(Arrays.asList("name"));

		assertEquals(Direction.ASC, sortSpec.reverse().getDirection());
		assertEquals("name ASC", sortSpec.reverse().toString());
	}

	@Test
	public void toStringJoinsPathSegments() {
		SortSpec sortSpec = SortSpec.asc(Arrays.asList("author", "name"));

		assertEquals("author.name ASC", sortSpec.toString());
	}

	@Test
	public void equalSortSpecsHaveEqualHashCodes() {
		SortSpec first = SortSpec.asc(Arrays.asList("author", "name"));
		SortSpec second = SortSpec.asc(Arrays.asList("author", "name"));

		assertTrue(first.equals(second));
		assertEquals(first.hashCode(), second.hashCode());
	}

	@Test
	public void differentDirectionsAreNotEqual() {
		SortSpec ascending = SortSpec.asc(Arrays.asList("name"));
		SortSpec descending = SortSpec.desc(Arrays.asList("name"));

		assertFalse(ascending.equals(descending));
	}

	@Test
	public void differentPathsAreNotEqual() {
		SortSpec first = SortSpec.asc(Arrays.asList("first"));
		SortSpec second = SortSpec.asc(Arrays.asList("second"));

		assertFalse(first.equals(second));
	}

	@Test
	public void sortSpecIsNotEqualToNullOrAnotherType() {
		SortSpec sortSpec = SortSpec.asc(Arrays.asList("name"));

		assertFalse(sortSpec.equals(null));
		assertFalse(sortSpec.equals("name"));
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
