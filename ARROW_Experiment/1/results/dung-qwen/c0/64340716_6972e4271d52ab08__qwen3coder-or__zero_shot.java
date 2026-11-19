package io.katharsis.queryspec;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SortSpec_ArrowTest {

	@Test
	public void testConstructorWithNullPath() {
		try {
			new SortSpec(null, SortSpec.Direction.ASC);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Parameters may not be empty", e.getMessage());
		}
	}

	@Test
	public void testConstructorWithEmptyPath() {
		try {
			new SortSpec(Arrays.asList(), SortSpec.Direction.ASC);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Parameters may not be empty", e.getMessage());
		}
	}

	@Test
	public void testConstructorWithNullDirection() {
		try {
			new SortSpec(Arrays.asList("attribute"), null);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertEquals("Parameters may not be empty", e.getMessage());
		}
	}

	@Test
	public void testConstructorValid() {
		List<String> path = Arrays.asList("attribute1", "attribute2");
		SortSpec sortSpec = new SortSpec(path, SortSpec.Direction.ASC);
		
		assertEquals(path, sortSpec.getAttributePath());
		assertEquals(SortSpec.Direction.ASC, sortSpec.getDirection());
	}

	@Test
	public void testAscFactoryMethod() {
		List<String> path = Arrays.asList("attribute");
		SortSpec sortSpec = SortSpec.asc(path);
		
		assertEquals(path, sortSpec.getAttributePath());
		assertEquals(SortSpec.Direction.ASC, sortSpec.getDirection());
	}

	@Test
	public void testDescFactoryMethod() {
		List<String> path = Arrays.asList("attribute");
		SortSpec sortSpec = SortSpec.desc(path);
		
		assertEquals(path, sortSpec.getAttributePath());
		assertEquals(SortSpec.Direction.DESC, sortSpec.getDirection());
	}

	@Test
	public void testReverse() {
		List<String> path = Arrays.asList("attribute");
		SortSpec sortSpec = new SortSpec(path, SortSpec.Direction.ASC);
		SortSpec reversed = sortSpec.reverse();
		
		assertEquals(path, reversed.getAttributePath());
		assertEquals(SortSpec.Direction.DESC, reversed.getDirection());
		
		SortSpec reversedAgain = reversed.reverse();
		assertEquals(path, reversedAgain.getAttributePath());
		assertEquals(SortSpec.Direction.ASC, reversedAgain.getDirection());
	}

	@Test
	public void testHashCode() {
		List<String> path = Arrays.asList("attribute");
		SortSpec sortSpec1 = new SortSpec(path, SortSpec.Direction.ASC);
		SortSpec sortSpec2 = new SortSpec(path, SortSpec.Direction.ASC);
		
		assertEquals(sortSpec1.hashCode(), sortSpec2.hashCode());
	}

	@Test
	public void testEquals() {
		List<String> path = Arrays.asList("attribute");
		SortSpec sortSpec1 = new SortSpec(path, SortSpec.Direction.ASC);
		SortSpec sortSpec2 = new SortSpec(path, SortSpec.Direction.ASC);
		SortSpec sortSpec3 = new SortSpec(path, SortSpec.Direction.DESC);
		
		assertEquals(sortSpec1, sortSpec2);
		assertNotEquals(sortSpec1, sortSpec3);
		assertNotEquals(sortSpec1, null);
		assertNotEquals(sortSpec1, new Object());
	}

	@Test
	public void testToString() {
		List<String> path = Arrays.asList("attr1", "attr2");
		SortSpec sortSpec = new SortSpec(path, SortSpec.Direction.ASC);
		
		String expected = "attr1.attr2 ASC";
		assertEquals(expected, sortSpec.toString());
	}
}
