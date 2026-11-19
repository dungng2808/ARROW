package org.zalando.logbook.netty;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Sequence_ArrowTest {

    @Test
    public void testConstructor() {
        Sequence sequence = new Sequence(3);
        assertNotNull(sequence);
        assertEquals(3, sequence.tasks.size());
    }

    @Test
    public void testSetAndRunEagerly() {
        Sequence sequence = new Sequence(3);
        TestRunnable task1 = new TestRunnable();
        TestRunnable task2 = new TestRunnable();
        TestRunnable task3 = new TestRunnable();

        sequence.set(0, task1);
        assertFalse(task1.ran);
        sequence.set(1, task2);
        assertFalse(task2.ran);
        sequence.set(2, task3);
        assertFalse(task3.ran);

        assertTrue(task1.ran);
        assertTrue(task2.ran);
        assertTrue(task3.ran);
    }

    @Test
    public void testSetOutOfOrder() {
        Sequence sequence = new Sequence(3);
        TestRunnable task1 = new TestRunnable();
        TestRunnable task2 = new TestRunnable();
        TestRunnable task3 = new TestRunnable();

        sequence.set(2, task3);
        assertFalse(task3.ran);
        sequence.set(1, task2);
        assertFalse(task2.ran);
        sequence.set(0, task1);
        assertTrue(task1.ran);
        assertTrue(task2.ran);
        assertTrue(task3.ran);
    }

    @Test
    public void testHasSecondTask() {
        Sequence sequence = new Sequence(3);
        assertFalse(sequence.hasSecondTask());

        sequence.set(1, () -> {});
        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void testResetNextAfterRunningAllTasks() {
        Sequence sequence = new Sequence(3);
        TestRunnable task1 = new TestRunnable();
        TestRunnable task2 = new TestRunnable();
        TestRunnable task3 = new TestRunnable();

        sequence.set(0, task1);
        sequence.set(1, task2);
        sequence.set(2, task3);

        assertTrue(task1.ran);
        assertTrue(task2.ran);
        assertTrue(task3.ran);

        // After running all tasks, next should be reset to 0
        // This is tested indirectly by ensuring subsequent calls behave correctly
        TestRunnable task4 = new TestRunnable();
        sequence.set(0, task4);
        assertTrue(task4.ran);
    }

    private static class TestRunnable implements Runnable {
        boolean ran = false;

        @Override
        public void run() {
            ran = true;
        }
    }
}
