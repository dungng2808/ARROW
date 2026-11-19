package net.fortytwo.stream.caching;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Solution_ArrowTest {

    @Test
    public void constructorStoresBindings() {
        Bindings<String> bindings = new Bindings<>();
        Solution<String> solution = new Solution<>(2, 0, bindings, 100L);

        assertSame(bindings, solution.getBindings());
    }

    @Test
    public void copyConstructorPreservesBindings() {
        Bindings<String> bindings = new Bindings<>();
        Solution<String> original = new Solution<>(2, 0, bindings, 100L);
        Solution<String> copy = new Solution<>(original);

        assertSame(bindings, copy.getBindings());
    }

    @Test
    public void setBindingsReplacesBindings() {
        Solution<String> solution = new Solution<>(1, 0, new Bindings<String>(), 0L);
        Bindings<String> replacement = new Bindings<>();

        solution.setBindings(replacement);

        assertSame(replacement, solution.getBindings());
    }

    @Test
    public void constructorWithAdditionalIndexPreservesBindings() {
        Bindings<String> bindings = new Bindings<>();
        Solution<String> original = new Solution<>(2, 0, bindings, 0L);
        Solution<String> extended = new Solution<>(original, 1);

        assertSame(bindings, extended.getBindings());
    }

    @Test
    public void constructorWithNewBindingsCreatesBindings() {
        Solution<String> original = new Solution<>(1, 0, new Bindings<String>(), 0L);
        Solution<String> extended = new Solution<>(original, 1, new Bindings<String>());

        assertNotNull(extended.getBindings());
    }

    @Test
    public void composableWithDisjointPatternsAndEmptyBindings() {
        Bindings<String> firstBindings = new Bindings<>();
        Bindings<String> secondBindings = new Bindings<>();
        Solution<String> first = new Solution<>(2, 0, firstBindings, 0L);
        Solution<String> second = new Solution<>(2, 1, secondBindings, 0L);

        assertTrue(first.composableWith(second, null));
    }

    @Test
    public void composableWithOverlappingPatternsIsFalse() {
        Bindings<String> bindings = new Bindings<>();
        Solution<String> first = new Solution<>(2, 0, bindings, 0L);
        Solution<String> second = new Solution<>(2, 0, bindings, 0L);

        assertFalse(first.composableWith(second, null));
    }
}
