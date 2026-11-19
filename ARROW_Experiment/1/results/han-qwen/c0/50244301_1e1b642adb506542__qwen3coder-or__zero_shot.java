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
        final String testString = "hello";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return BasedSequence.of(s);
            }
        };

        BasedSequence result1 = manager.getBaseSequence(testString, null, factory);
        BasedSequence result2 = manager.getBaseSequence(testString, null, factory);

        assertEquals(result1, result2);
    }

    @Test
    public void testGetBaseSequenceWithCallTypes() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String testString = "world";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return BasedSequence.of(s);
            }
        };

        int[] callTypes = new int[1];
        BasedSequence result1 = manager.getBaseSequence(testString, callTypes, factory);
        int callType1 = callTypes[0];

        BasedSequence result2 = manager.getBaseSequence(testString, callTypes, factory);
        int callType2 = callTypes[0];

        assertEquals(result1, result2);
        assertTrue(callType1 >= 20); // Should be at least 20 since it's a new creation
        assertTrue(callType2 >= 20); // Should be at least 20 since it's a new creation
    }

    @Test
    public void testGetBaseSequenceReturnsSameInstanceForSameObject() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String testString = "test";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return BasedSequence.of(s);
            }
        };

        BasedSequence result1 = manager.getBaseSequence(testString, null, factory);
        BasedSequence result2 = manager.getBaseSequence(testString, null, factory);

        assertSame(result1, result2);
    }

    @Test
    public void testGetBaseSequenceDifferentObjects() {
        BaseSequenceManager manager = new BaseSequenceManager();
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return BasedSequence.of(s);
            }
        };

        BasedSequence result1 = manager.getBaseSequence("first", null, factory);
        BasedSequence result2 = manager.getBaseSequence("second", null, factory);

        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
    }

    @Test
    public void testGetBaseSequenceWithSubSequence() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String testString = "subsequence";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return SubSequence.of(BasedSequence.of(s), 0, s.length());
            }
        };

        BasedSequence result1 = manager.getBaseSequence(testString, null, factory);
        BasedSequence result2 = manager.getBaseSequence(testString, null, factory);

        assertSame(result1, result2);
    }

    @Test
    public void testGetBaseSequenceAfterWeakReferenceGc() {
        BaseSequenceManager manager = new BaseSequenceManager();
        final String testString = "gc_test";
        Function<String, BasedSequence> factory = new Function<String, BasedSequence>() {
            @Override
            public BasedSequence apply(String s) {
                return BasedSequence.of(s);
            }
        };

        BasedSequence result1 = manager.getBaseSequence(testString, null, factory);
        // Force GC by creating many objects
        for (int i = 0; i < 1000; i++) {
            new String("temp" + i);
        }

        BasedSequence result2 = manager.getBaseSequence(testString, null, factory);

        // Since we're using weak references, the behavior might vary depending on GC,
        // but we ensure it doesn't crash
        assertNotNull(result1);
        assertNotNull(result2);
    }
}
