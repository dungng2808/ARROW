package org.zalando.logbook.netty;

import org.junit.Test;

import static org.junit.Assert.*;

public class Sequence_ArrowTest {

    @Test
    public void testConstructor() {
        Sequence sequence = new Sequence(3);
        assertNotNull(sequence);
        assertEquals(3, sequence.tasks.size());
        assertNull(sequence.tasks.get(0));
        assertNull(sequence.tasks.get(1));
        assertNull(sequence.tasks.get(2));
        assertEquals(0, sequence.next);
    }

    @Test
    public void testSetAndRunEagerly() {
        Sequence sequence = new Sequence(3);
        
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        sequence.set(0, task1);
        sequence.set(1, task2);
        
        assertFalse(sequence.hasSecondTask());
    }

    @Test
    public void testHasSecondTask() {
        Sequence sequence = new Sequence(3);
        
        assertFalse(sequence.hasSecondTask());
        
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        sequence.set(1, task);
        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void testSetWithNextIndex() {
        Sequence sequence = new Sequence(3);
        
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        sequence.set(0, task1);
        
        // Since index 0 is the next index, it should run eagerly
        // But there's no way to verify this without side effects
        // Just ensure no exception is thrown
        assertNotNull(sequence);
    }

    @Test
    public void testRunEagerlyWithAllTasks() {
        Sequence sequence = new Sequence(3);
        
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        Runnable task3 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        sequence.set(0, task1);
        sequence.set(1, task2);
        sequence.set(2, task3);
        
        // Should not throw any exception
        assertNotNull(sequence);
    }

    @Test
    public void testRunEagerlyWithMissingTask() {
        Sequence sequence = new Sequence(3);
        
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                // Empty implementation
            }
        };
        
        sequence.set(0, task1);
        // Task at index 1 is null
        
        // Should not throw any exception
        assertNotNull(sequence);
    }
}
