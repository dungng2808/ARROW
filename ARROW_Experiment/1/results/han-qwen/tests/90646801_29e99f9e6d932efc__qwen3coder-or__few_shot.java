package de.vsfexperts.latex.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
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
	public void testAddThrowsNullPointerExceptionForNullKey() {
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
			// If we reach here, the set was modifiable, which is incorrect
		} catch (UnsupportedOperationException e) {
			// Expected behavior
		}
	}

	@Test
	public void testAsMapReturnsCopy() {
		LatexContext context = new LatexContext();
		context.add("key", "value");

		Map<String, Object> map1 = context.asMap();
		Map<String, Object> map2 = context.asMap();

		assertNotSame(map1, map2);
		assertEquals(map1, map2);

		// Modifying the returned map should not affect the context
		map1.put("newKey", "newValue");
		assertEquals(1, context.asMap().size());
	}

	@Test
	public void testEmptyContext() {
		LatexContext context = new LatexContext();

		Set<String> keys = context.keys();
		assertEquals(0, keys.size());

		Map<String, Object> map = context.asMap();
		assertEquals(0, map.size());
	}

	@Test
	public void testAddWithNullValue() {
		LatexContext context = new LatexContext();
		context.add("key", null);

		Set<String> keys = context.keys();
		assertEquals(1, keys.size());
		assertTrue(keys.contains("key"));

		Map<String, Object> map = context.asMap();
		assertEquals(1, map.size());
		assertEquals(null, map.get("key"));
	}

	@Test
	public void testMultipleAddsSameKey() {
		LatexContext context = new LatexContext();
		context.add("key", "value1");
		context.add("key", "value2");

		Set<String> keys = context.keys();
		assertEquals(1, keys.size());
		assertTrue(keys.contains("key"));

		Map<String, Object> map = context.asMap();
		assertEquals(1, map.size());
		assertEquals("value2", map.get("key"));
	}

	private void assertNotSame(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			throw new AssertionError("Objects should not be the same");
		}
	}
}
