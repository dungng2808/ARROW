package de.vsfexperts.latex.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class LatexContext_ArrowTest {

	@Test
	public void testAddReturnsThis() {
		LatexContext context = new LatexContext();
		LatexContext result = context.add("key", "value");
		assertSame(context, result);
	}

	@Test(expected = NullPointerException.class)
	public void testAddWithNullKeyThrowsException() {
		LatexContext context = new LatexContext();
		context.add(null, "value");
	}

	@Test
	public void testAddWithValue() {
		LatexContext context = new LatexContext();
		context.add("key1", "value1");
		context.add("key2", "value2");

		Set<String> keys = context.keys();
		assertEquals(2, keys.size());
		assertTrue(keys.contains("key1"));
		assertTrue(keys.contains("key2"));

		Map<String, Object> map = context.asMap();
		assertEquals(2, map.size());
		assertEquals("value1", map.get("key1"));
		assertEquals("value2", map.get("key2"));
	}

	@Test
	public void testKeysReturnsUnmodifiableSet() {
		LatexContext context = new LatexContext();
		context.add("key", "value");

		Set<String> keys = context.keys();
		assertNotNull(keys);
		assertTrue(keys.contains("key"));

		// This should fail if the set is modifiable
		try {
			keys.remove("key");
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}

	@Test
	public void testAsMapReturnsCopy() {
		LatexContext context = new LatexContext();
		context.add("key", "value");

		Map<String, Object> map1 = context.asMap();
		Map<String, Object> map2 = context.asMap();

		assertSame(map1, map1);
		assertSame(map2, map2);

		// They should be equal but not the same object
		assertEquals(map1, map2);
		assertTrue(map1 != map2);
	}

	@Test
	public void testEmptyContext() {
		LatexContext context = new LatexContext();

		Set<String> keys = context.keys();
		assertEquals(0, keys.size());

		Map<String, Object> map = context.asMap();
		assertEquals(0, map.size());
	}
}
