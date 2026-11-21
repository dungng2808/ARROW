package org.zalando.logbook.netty;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class Sequence_ArrowTest {

    @Test
    public void testConstructor() {
        Sequence sequence = new Sequence(3);
        assertNotNull(sequence);
        assertEquals(3, sequence.tasks.size());
    }

    @Test
    public void testSetAndRunEagerly() {
        AtomicInteger counter = new AtomicInteger(0);
        Sequence sequence = new Sequence(3);

        Runnable task1 = () -> counter.incrementAndGet();
        Runnable task2 = () -> counter.incrementAndGet();
        Runnable task3 = () -> counter.incrementAndGet();

        sequence.set(0, task1);
        assertEquals(1, counter.get());

        sequence.set(1, task2);
        assertEquals(2, counter.get());

        sequence.set(2, task3);
        assertEquals(3, counter.get());
    }

    @Test
    public void testHasSecondTask() {
        Sequence sequence = new Sequence(3);

        assertFalse(sequence.hasSecondTask());

        Runnable task = () -> {};
        sequence.set(1, task);
        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void testRunEagerlyWithNullTask() {
        AtomicInteger counter = new AtomicInteger(0);
        Sequence sequence = new Sequence(3);

        Runnable task1 = () -> counter.incrementAndGet();
        sequence.set(0, task1);
        assertEquals(1, counter.get());

        sequence.set(1, null);
        assertEquals(1, counter.get());

        Runnable task2 = () -> counter.incrementAndGet();
        sequence.set(2, task2);
        assertEquals(1, counter.get());
    }

    @Test
    public void testResetNextAfterCompletion() {
        AtomicInteger counter = new AtomicInteger(0);
        Sequence sequence = new Sequence(2);

        Runnable task1 = () -> counter.incrementAndGet();
        Runnable task2 = () -> counter.incrementAndGet();

        sequence.set(0, task1);
        assertEquals(1, counter.get());

        sequence.set(1, task2);
        assertEquals(2, counter.get());

        // After completion, next should be reset to 0
        assertFalse(sequence.hasSecondTask());
    }
}
