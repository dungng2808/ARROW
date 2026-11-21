package de.vsfexperts.latex.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class LatexContext_ArrowTest {

    @Test
    public void addStoresValueAndReturnsSameContext() {
        LatexContext context = new LatexContext();
        Object value = new Object();

        LatexContext returned = context.add("name", value);

        assertTrue(returned == context);
        assertEquals(value, context.asMap().get("name"));
    }

    @Test
    public void addReplacesValueForExistingKey() {
        LatexContext context = new LatexContext();
        context.add("name", "first");
        context.add("name", "second");

        assertEquals("second", context.asMap().get("name"));
        assertEquals(1, context.asMap().size());
    }

    @Test
    public void keysContainsAddedKeysAndIsUnmodifiable() {
        LatexContext context = new LatexContext();
        context.add("first", 1);
        context.add("second", 2);

        assertEquals(2, context.keys().size());
        assertTrue(context.keys().contains("first"));
        assertTrue(context.keys().contains("second"));

        try {
            context.keys().remove("first");
            fail("Expected keys to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            assertTrue(context.keys().contains("first"));
        }
    }

    @Test
    public void keysViewReflectsLaterAdditions() {
        LatexContext context = new LatexContext();
        context.add("first", 1);
        java.util.Set<String> keys = context.keys();

        context.add("second", 2);

        assertTrue(keys.contains("second"));
    }

    @Test
    public void asMapReturnsIndependentCopy() {
        LatexContext context = new LatexContext();
        context.add("name", "value");

        Map<String, Object> copy = context.asMap();
        assertNotSame(copy, context.asMap());
        copy.put("other", "entry");
        copy.put("name", "changed");

        assertEquals(1, context.asMap().size());
        assertEquals("value", context.asMap().get("name"));
        assertFalse(context.keys().contains("other"));
    }

    @Test
    public void addAllowsNullValue() {
        LatexContext context = new LatexContext();

        context.add("nullable", null);

        assertTrue(context.asMap().containsKey("nullable"));
        assertEquals(null, context.asMap().get("nullable"));
    }

    @Test(expected = NullPointerException.class)
    public void addRejectsNullKey() {
        new LatexContext().add(null, "value");
    }

    @Test
    public void emptyContextHasNoKeysOrEntries() {
        LatexContext context = new LatexContext();

        assertTrue(context.keys().isEmpty());
        assertEquals(new HashMap<String, Object>(), context.asMap());
    }
}
