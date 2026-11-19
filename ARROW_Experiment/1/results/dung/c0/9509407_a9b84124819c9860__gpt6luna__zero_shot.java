package net.fortytwo.stream.caching;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Solution_ArrowTest {
    @Test
    public void constructorStoresBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(2, 0, bindings, 100L);

        assertSame(bindings, solution.getBindings());
    }

    @Test
    public void copyConstructorCopiesBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(2, 0, bindings, 100L);
        Solution<String> copy = new Solution<String>(original);

        assertNotSame(original, copy);
        assertSame(bindings, copy.getBindings());
    }

    @Test
    public void setBindingsReplacesBindings() {
        Solution<String> solution = new Solution<String>(1, 0, new Bindings<String>(), 0L);
        Bindings<String> replacement = new Bindings<String>();

        solution.setBindings(replacement);

        assertSame(replacement, solution.getBindings());
    }

    @Test
    public void constructorWithIndexRetainsBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 0, bindings, 0L);
        Solution<String> extended = new Solution<String>(original, 1);

        assertSame(bindings, extended.getBindings());
    }

    @Test
    public void constructorWithNewBindingsCreatesCombinedBindings() {
        Bindings<String> originalBindings = new Bindings<String>();
        Bindings<String> additionalBindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 0, originalBindings, 0L);
        Solution<String> extended = new Solution<String>(original, 1, additionalBindings);

        assertNotSame(originalBindings, extended.getBindings());
    }

    @Test
    public void composableWithDisjointSolutions() {
        Solution<String> first = new Solution<String>(2, 0, new Bindings<String>(), 0L);
        Solution<String> second = new Solution<String>(2, 1, new Bindings<String>(), 0L);

        assertTrue(first.composableWith(second, null));
    }

    @Test
    public void composableWithOverlappingSolutionsIsFalse() {
        Solution<String> first = new Solution<String>(2, 0, new Bindings<String>(), 0L);
        Solution<String> second = new Solution<String>(2, 0, new Bindings<String>(), 0L);

        assertFalse(first.composableWith(second, null));
    }
}
