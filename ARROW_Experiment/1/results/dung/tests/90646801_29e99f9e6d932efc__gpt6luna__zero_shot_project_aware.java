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

		LatexContext result = context.add("key", value);

		assertSame(context, result);
		assertEquals(value, context.asMap().get("key"));
	}

	@Test
	public void addReplacesExistingValueForKey() {
		LatexContext context = new LatexContext();
		context.add("key", "first");
		context.add("key", "second");

		assertEquals("second", context.asMap().get("key"));
		assertEquals(1, context.asMap().size());
	}

	@Test
	public void addAllowsNullValue() {
		LatexContext context = new LatexContext();
		context.add("key", null);

		assertTrue(context.asMap().containsKey("key"));
		assertEquals(null, context.asMap().get("key"));
	}

	@Test
	public void addRejectsNullKey() {
		LatexContext context = new LatexContext();

		try {
			context.add(null, "value");
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
			assertEquals("key must not be null", expected.getMessage());
		}
	}

	@Test
	public void keysReturnsKeysAndIsUnmodifiable() {
		LatexContext context = new LatexContext();
		context.add("alpha", 1);
		context.add("beta", 2);

		Set<String> keys = context.keys();

		assertEquals(2, keys.size());
		assertTrue(keys.contains("alpha"));
		assertTrue(keys.contains("beta"));
		try {
			keys.add("gamma");
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
			assertFalse(keys.contains("gamma"));
		}
	}

	@Test
	public void keysViewReflectsLaterChanges() {
		LatexContext context = new LatexContext();
		Set<String> keys = context.keys();

		context.add("later", "value");

		assertTrue(keys.contains("later"));
	}

	@Test
	public void asMapReturnsIndependentCopy() {
		LatexContext context = new LatexContext();
		context.add("original", "value");

		Map<String, Object> copy = context.asMap();

		assertEquals(1, copy.size());
		assertEquals("value", copy.get("original"));
		assertNotSame(copy, context.asMap());
		copy.put("copy-only", "other");
		assertFalse(context.keys().contains("copy-only"));
	}

	@Test
	public void asMapReturnsEmptyMapForNewContext() {
		LatexContext context = new LatexContext();

		assertEquals(new HashMap<String, Object>(), context.asMap());
		assertTrue(context.keys().isEmpty());
	}
}
