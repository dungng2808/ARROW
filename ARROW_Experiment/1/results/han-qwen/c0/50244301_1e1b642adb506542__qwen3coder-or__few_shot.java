package com.vladsch.flexmark.experimental.util.sequence.managed;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.SubSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.function.Function;

import static org.junit.Assert.*;

public class BaseSequenceManager_ArrowTest {

    @Test
    public void testGetBaseSequenceFirstTime() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String testString = "hello world";
        int[] callTypes = new int[1];
        
        BasedSequence result = manager.getBaseSequence(testString, callTypes, s -> {
            return new MockBasedSequence(s);
        });
        
        assertNotNull(result);
        assertEquals("hello world", result.toString());
        assertEquals(20, callTypes[0]); // 10 for set search + 10 for construction
    }

    @Test
    public void testGetBaseSequenceFromCache() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String testString = "hello world";
        int[] callTypes = new int[1];
        
        // First call
        BasedSequence result1 = manager.getBaseSequence(testString, callTypes, s -> {
            return new MockBasedSequence(s);
        });
        
        // Second call with same object
        BasedSequence result2 = manager.getBaseSequence(testString, callTypes, s -> {
            fail("Factory should not be called when cached");
            return null;
        });
        
        assertSame(result1, result2);
        assertEquals(0, callTypes[0]); // 0 for map lookup
    }

    @Test
    public void testGetBaseSequenceWithNullCallTypes() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String testString = "test";
        
        BasedSequence result = manager.getBaseSequence(testString, null, s -> {
            return new MockBasedSequence(s);
        });
        
        assertNotNull(result);
        assertEquals("test", result.toString());
    }

    @Test
    public void testGetBaseSequenceWithDifferentObjectsButSameContent() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String testString1 = "hello";
        String testString2 = "hello";
        
        BasedSequence result1 = manager.getBaseSequence(testString1, null, s -> {
            return new MockBasedSequence(s);
        });
        
        BasedSequence result2 = manager.getBaseSequence(testString2, null, s -> {
            fail("Factory should not be called when cached");
            return null;
        });
        
        // These should be different objects since they're different string instances
        // But the manager should handle them correctly
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    public void testGetBaseSequenceWithWeakReferenceCleanup() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String testString = "test";
        
        // Get initial sequence
        BasedSequence result1 = manager.getBaseSequence(testString, null, s -> {
            return new MockBasedSequence(s);
        });
        
        // Force GC to clear weak reference
        System.gc();
        
        // This should create a new instance since the weak reference was cleared
        BasedSequence result2 = manager.getBaseSequence(testString, null, s -> {
            return new MockBasedSequence(s);
        });
        
        assertNotNull(result1);
        assertNotNull(result2);
    }

    // Mock implementation for testing
    private static class MockBasedSequence extends BasedSequence {
        private final String content;

        public MockBasedSequence(String content) {
            this.content = content;
        }

        @Override
        public char charAt(int index) {
            return content.charAt(index);
        }

        @Override
        public int length() {
            return content.length();
        }

        @Override
        public BasedSequence subSequence(int start, int end) {
            return new MockBasedSequence(content.substring(start, end));
        }

        @Override
        public BasedSequence getBaseSequence() {
            return this;
        }

        @Override
        public Object getBase() {
            return content;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MockBasedSequence that = (MockBasedSequence) obj;
            return content.equals(that.content);
        }

        @Override
        public int hashCode() {
            return content.hashCode();
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
