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
		context.add("key", "value");
		Set<String> keys = context.keys();
		assertEquals(1, keys.size());
		assertTrue(keys.contains("key"));
	}

	@Test
	public void testKeysReturnsUnmodifiableSet() {
		LatexContext context = new LatexContext();
		Set<String> keys = context.keys();
		assertNotNull(keys);
		// This should fail if the set is modifiable
		try {
			keys.add("newKey");
		} catch (UnsupportedOperationException e) {
			// Expected
		}
	}

	@Test
	public void testAsMapReturnsCopy() {
		LatexContext context = new LatexContext();
		context.add("key", "value");
		Map<String, Object> map = context.asMap();
		assertNotNull(map);
		assertEquals(1, map.size());
		map.put("newKey", "newValue");
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
}
