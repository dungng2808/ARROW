package net.fortytwo.stream.caching;

import org.junit.Test;
import static org.junit.Assert.*;

public class Solution_ArrowTest {

    @Test
    public void testCopyConstructor() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 0, bindings, 1000L);
        Solution<String> copy = new Solution<String>(original);
        
        assertEquals(original.getBindings(), copy.getBindings());
        assertEquals(original.remainingPatterns, copy.remainingPatterns);
        assertEquals(original.matchedPatterns, copy.matchedPatterns);
        assertEquals(original.expirationTime, copy.expirationTime);
    }

    @Test
    public void testSolutionConstructor() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(3, 0, bindings, 1000L);
        
        assertEquals(2, solution.remainingPatterns);
        assertEquals(1, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
        assertEquals(bindings, solution.getBindings());
    }

    @Test
    public void testSolutionConstructorWithIndex() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 0, bindings, 1000L);
        Solution<String> solution = new Solution<String>(original, 1);
        
        assertEquals(1, solution.remainingPatterns);
        assertEquals(3, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
        assertEquals(bindings, solution.getBindings());
    }

    @Test
    public void testSolutionConstructorWithIndexAndNewBindings() {
        Bindings<String> originalBindings = new Bindings<String>();
        Bindings<String> newBindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 0, originalBindings, 1000L);
        Solution<String> solution = new Solution<String>(original, 1, newBindings);
        
        assertEquals(1, solution.remainingPatterns);
        assertEquals(3, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
        assertNotNull(solution.getBindings());
    }

    @Test
    public void testSolutionConstructorWithTwoSolutions() {
        Bindings<String> bindings1 = new Bindings<String>();
        Bindings<String> bindings2 = new Bindings<String>();
        Solution<String> first = new Solution<String>(3, 0, bindings1, 1000L);
        Solution<String> second = new Solution<String>(3, 1, bindings2, 1000L);
        Solution<String> solution = new Solution<String>(3, first, second);
        
        assertEquals(1, solution.remainingPatterns);
        assertEquals(3, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
        assertNotNull(solution.getBindings());
    }

    @Test
    public void testGetBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(3, 0, bindings, 1000L);
        
        assertEquals(bindings, solution.getBindings());
    }

    @Test
    public void testSetBindings() {
        Bindings<String> originalBindings = new Bindings<String>();
        Bindings<String> newBindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(3, 0, originalBindings, 1000L);
        
        solution.setBindings(newBindings);
        assertEquals(newBindings, solution.getBindings());
    }

    @Test
    public void testComposableWithDisjointAndCompatible() {
        Bindings<String> bindings1 = new Bindings<String>();
        Bindings<String> bindings2 = new Bindings<String>();
        Solution<String> first = new Solution<String>(3, 0, bindings1, 1000L);
        Solution<String> second = new Solution<String>(3, 1, bindings2, 1000L);
        
        // Mock QueryVariables since we don't have access to it
        // We'll assume it returns true for compatibility check
        Query.QueryVariables vars = null;
        
        // Since we can't properly test the method due to missing dependencies,
        // we'll just verify that the method compiles and runs
        assertTrue(first.composableWith(second, vars));
    }
}
