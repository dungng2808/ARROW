package io.katharsis.queryspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SortSpec_ArrowTest {

	@Test
	public void testConstructor() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec = new SortSpec(path, Direction.ASC);
		
		assertEquals(Direction.ASC, sortSpec.getDirection());
		assertEquals(path, sortSpec.getAttributePath());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullPath() {
		new SortSpec(null, Direction.ASC);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyPath() {
		new SortSpec(Arrays.asList(), Direction.ASC);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullDirection() {
		new SortSpec(Arrays.asList("attribute"), null);
	}

	@Test
	public void testAsc() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec = SortSpec.asc(path);
		
		assertEquals(Direction.ASC, sortSpec.getDirection());
		assertEquals(path, sortSpec.getAttributePath());
	}

	@Test
	public void testDesc() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec = SortSpec.desc(path);
		
		assertEquals(Direction.DESC, sortSpec.getDirection());
		assertEquals(path, sortSpec.getAttributePath());
	}

	@Test
	public void testReverse() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec = new SortSpec(path, Direction.ASC);
		SortSpec reversed = sortSpec.reverse();
		
		assertEquals(Direction.DESC, reversed.getDirection());
		assertEquals(path, reversed.getAttributePath());
		
		SortSpec reversedTwice = reversed.reverse();
		assertEquals(Direction.ASC, reversedTwice.getDirection());
		assertEquals(path, reversedTwice.getAttributePath());
	}

	@Test
	public void testHashCode() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec1 = new SortSpec(path, Direction.ASC);
		SortSpec sortSpec2 = new SortSpec(path, Direction.ASC);
		SortSpec sortSpec3 = new SortSpec(path, Direction.DESC);
		
		assertEquals(sortSpec1.hashCode(), sortSpec2.hashCode());
		assertNotEquals(sortSpec1.hashCode(), sortSpec3.hashCode());
	}

	@Test
	public void testEquals() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec1 = new SortSpec(path, Direction.ASC);
		SortSpec sortSpec2 = new SortSpec(path, Direction.ASC);
		SortSpec sortSpec3 = new SortSpec(path, Direction.DESC);
		SortSpec sortSpec4 = new SortSpec(Arrays.asList("attribute1"), Direction.ASC);
		
		assertTrue(sortSpec1.equals(sortSpec2));
		assertFalse(sortSpec1.equals(sortSpec3));
		assertFalse(sortSpec1.equals(sortSpec4));
		assertTrue(sortSpec1.equals(sortSpec1));
		assertFalse(sortSpec1.equals(new Object()));
		assertFalse(sortSpec1.equals(null));
	}

	@Test
	public void testToString() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec = new SortSpec(path, Direction.ASC);
		
		String expected = "attribute1.attribute2 ASC";
		assertEquals(expected, sortSpec.toString());
	}
}
