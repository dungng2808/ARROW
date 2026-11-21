package net.fortytwo.stream.caching;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
    public void copyConstructorPreservesBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(2, 0, bindings, 100L);
        Solution<String> copy = new Solution<String>(original);

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
    public void constructorWithAdditionalIndexPreservesBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 0, bindings, 0L);
        Solution<String> extended = new Solution<String>(original, 1);

        assertSame(bindings, extended.getBindings());
    }

    @Test
    public void constructorWithAdditionalBindingsProducesBindings() {
        Solution<String> original = new Solution<String>(3, 0, new Bindings<String>(), 0L);
        Solution<String> extended = new Solution<String>(original, 1, new Bindings<String>());

        assertNotNull(extended.getBindings());
    }

    @Test
    public void combiningSolutionsProducesBindings() {
        Solution<String> first = new Solution<String>(2, 0, new Bindings<String>(), 0L);
        Solution<String> second = new Solution<String>(2, 1, new Bindings<String>(), 0L);
        Solution<String> combined = new Solution<String>(2, first, second);

        assertNotNull(combined.getBindings());
    }

    @Test
    public void composableWithDisjointSolutionsAndEmptyBindings() {
        Solution<String> first = new Solution<String>(2, 0, new Bindings<String>(), 0L);
        Solution<String> second = new Solution<String>(2, 1, new Bindings<String>(), 0L);

        assertTrue(first.composableWith(second, null));
    }

    @Test
    public void composableWithOverlappingPatternsIsFalse() {
        Solution<String> first = new Solution<String>(2, 0, new Bindings<String>(), 0L);
        Solution<String> overlapping = new Solution<String>(first);

        assertFalse(first.composableWith(overlapping, null));
    }
}
