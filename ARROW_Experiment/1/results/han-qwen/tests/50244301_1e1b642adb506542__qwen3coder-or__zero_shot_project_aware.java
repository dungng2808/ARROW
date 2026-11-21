package com.vladsch.flexmark.experimental.util.sequence.managed;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.SubSequence;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.function.Function;

import static org.junit.Assert.*;

public class BaseSequenceManager_ArrowTest {

    @Test
    public void testGetBaseSequenceWithNullCallTypes() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String input = "hello";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return new SubSequence(s, 0, s.length());
            }
        };

        BasedSequence result = manager.getBaseSequence(input, null, factory);
        assertNotNull(result);
        assertEquals(input, result.getBase());
    }

    @Test
    public void testGetBaseSequenceWithExistingBase() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String input = "test";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return new SubSequence(s, 0, s.length());
            }
        };

        // First call to create and store
        BasedSequence firstResult = manager.getBaseSequence(input, null, factory);
        assertNotNull(firstResult);

        // Second call should return the same instance
        int[] callTypes = new int[1];
        BasedSequence secondResult = manager.getBaseSequence(input, callTypes, factory);
        assertSame(firstResult, secondResult);
        assertEquals(0, callTypes[0]); // Should be 0 since it was found in map
    }

    @Test
    public void testGetBaseSequenceWithNewBase() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String input = "new";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return new SubSequence(s, 0, s.length());
            }
        };

        int[] callTypes = new int[1];
        BasedSequence result = manager.getBaseSequence(input, callTypes, factory);
        assertNotNull(result);
        assertEquals(input, result.getBase());
        assertEquals(20, callTypes[0]); // Should be 20 since it was constructed and added
    }

    @Test
    public void testGetBaseSequenceWithWeakReferenceCleanup() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String input = "cleanup";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return new SubSequence(s, 0, s.length());
            }
        };

        // Create and store
        BasedSequence firstResult = manager.getBaseSequence(input, null, factory);
        assertNotNull(firstResult);

        // Force garbage collection by clearing the reference manually
        // This simulates weak reference cleanup
        manager.baseMap.clear();

        // Now call again - should create a new one
        int[] callTypes = new int[1];
        BasedSequence secondResult = manager.getBaseSequence(input, callTypes, factory);
        assertNotNull(secondResult);
        assertEquals(input, secondResult.getBase());
        assertEquals(20, callTypes[0]); // Should be 20 since it was constructed and added
    }

    @Test
    public void testGetBaseSequenceWithSetSearch() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String input = "search";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return new SubSequence(s, 0, s.length());
            }
        };

        // Create and store in set
        BasedSequence firstResult = manager.getBaseSequence(input, null, factory);
        assertNotNull(firstResult);

        // Call again - should find it in set
        int[] callTypes = new int[1];
        BasedSequence secondResult = manager.getBaseSequence(input, callTypes, factory);
        assertSame(firstResult, secondResult);
        assertEquals(10, callTypes[0]); // Should be 10 since it was found in set
    }
}
