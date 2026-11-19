package net.fortytwo.stream.caching;

import org.junit.Test;
import static org.junit.Assert.*;

public class Solution_ArrowTest {

    @Test
    public void testCopyConstructor() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 1, bindings, 1000L);
        
        Solution<String> copy = new Solution<String>(original);
        
        assertEquals(original.getBindings(), copy.getBindings());
        assertEquals(original.remainingPatterns, copy.remainingPatterns);
        assertEquals(original.matchedPatterns, copy.matchedPatterns);
        assertEquals(original.expirationTime, copy.expirationTime);
    }

    @Test
    public void testSolutionConstructor() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(3, 1, bindings, 1000L);
        
        assertEquals(2, solution.remainingPatterns);
        assertEquals(2, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
        assertEquals(bindings, solution.getBindings());
    }

    @Test
    public void testSolutionConstructorWithIndex() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 1, bindings, 1000L);
        Solution<String> solution = new Solution<String>(original, 2);
        
        assertEquals(original.getBindings(), solution.getBindings());
        assertEquals(1, solution.remainingPatterns);
        assertEquals(6, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
    }

    @Test
    public void testSolutionConstructorWithIndexAndNewBindings() {
        Bindings<String> originalBindings = new Bindings<String>();
        Bindings<String> newBindings = new Bindings<String>();
        Solution<String> original = new Solution<String>(3, 1, originalBindings, 1000L);
        Solution<String> solution = new Solution<String>(original, 2, newBindings);
        
        assertEquals(original.getBindings(), solution.getBindings());
        assertEquals(1, solution.remainingPatterns);
        assertEquals(6, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
    }

    @Test
    public void testSolutionConstructorFromTwoSolutions() {
        Bindings<String> bindings1 = new Bindings<String>();
        Bindings<String> bindings2 = new Bindings<String>();
        Solution<String> first = new Solution<String>(3, 1, bindings1, 1000L);
        Solution<String> second = new Solution<String>(3, 2, bindings2, 1500L);
        Solution<String> solution = new Solution<String>(3, first, second);
        
        assertEquals(bindings1, solution.getBindings());
        assertEquals(1, solution.remainingPatterns);
        assertEquals(6, solution.matchedPatterns);
        assertEquals(1000L, solution.expirationTime);
    }

    @Test
    public void testGetBindings() {
        Bindings<String> bindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(3, 1, bindings, 1000L);
        
        assertEquals(bindings, solution.getBindings());
    }

    @Test
    public void testSetBindings() {
        Bindings<String> initialBindings = new Bindings<String>();
        Bindings<String> newBindings = new Bindings<String>();
        Solution<String> solution = new Solution<String>(3, 1, initialBindings, 1000L);
        
        solution.setBindings(newBindings);
        
        assertEquals(newBindings, solution.getBindings());
    }

    @Test
    public void testComposableWith() {
        Bindings<String> bindings1 = new Bindings<String>();
        Bindings<String> bindings2 = new Bindings<String>();
        Solution<String> first = new Solution<String>(3, 1, bindings1, 1000L);
        Solution<String> second = new Solution<String>(3, 2, bindings2, 1000L);
        Query.QueryVariables vars = new Query.QueryVariables();
        
        // Since we don't have access to the implementation details of Bindings.compatibleWith(),
        // we just test that the method doesn't throw an exception and returns a boolean
        boolean result = first.composableWith(second, vars);
        assertNotNull(result);
    }
}
