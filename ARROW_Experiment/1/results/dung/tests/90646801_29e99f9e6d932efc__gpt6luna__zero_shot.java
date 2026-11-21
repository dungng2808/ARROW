package de.vsfexperts.latex.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class LatexContext_ArrowTest {

	@Test
	public void addStoresValueAndReturnsSameContext() {
		LatexContext context = new LatexContext();
		Object value = new Object();

		LatexContext returned = context.add("name", value);

		assertSame(context, returned);
		assertSame(value, context.asMap().get("name"));
	}

	@Test
	public void addReplacesExistingValueForKey() {
		LatexContext context = new LatexContext();
		context.add("name", "first");
		context.add("name", "second");

		assertEquals("second", context.asMap().get("name"));
		assertEquals(1, context.asMap().size());
	}

	@Test
	public void addAllowsNullValue() {
		LatexContext context = new LatexContext();
		context.add("name", null);

		assertTrue(context.asMap().containsKey("name"));
		assertEquals(null, context.asMap().get("name"));
	}

	@Test
	public void addRejectsNullKey() {
		LatexContext context = new LatexContext();

		try {
			context.add(null, "value");
			fail("Expected a null key to be rejected");
		} catch (NullPointerException expected) {
			assertEquals("key must not be null", expected.getMessage());
		}
	}

	@Test
	public void keysReturnsKeysAndIsUnmodifiable() {
		LatexContext context = new LatexContext();
		context.add("first", 1);
		context.add("second", 2);

		Set<String> keys = context.keys();

		assertEquals(2, keys.size());
		assertTrue(keys.contains("first"));
		assertTrue(keys.contains("second"));
		try {
			keys.add("third");
			fail("Expected keys to be unmodifiable");
		} catch (UnsupportedOperationException expected) {
			assertFalse(keys.contains("third"));
		}
	}

	@Test
	public void keysReflectsLaterContextChanges() {
		LatexContext context = new LatexContext();
		Set<String> keys = context.keys();

		context.add("later", "value");

		assertTrue(keys.contains("later"));
	}

	@Test
	public void asMapReturnsCopyOfContext() {
		LatexContext context = new LatexContext();
		context.add("name", "value");

		Map<String, Object> map = context.asMap();

		assertEquals(1, map.size());
		assertEquals("value", map.get("name"));
		assertNotSame(map, context.asMap());
		map.put("other", "entry");
		assertFalse(context.asMap().containsKey("other"));
	}

	@Test
	public void asMapReturnsEmptyMapForEmptyContext() {
		LatexContext context = new LatexContext();

		assertEquals(new HashMap<String, Object>(), context.asMap());
	}
}
