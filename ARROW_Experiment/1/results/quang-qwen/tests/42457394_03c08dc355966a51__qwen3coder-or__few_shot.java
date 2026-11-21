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
    public void testSetWhenIndexIsNext() {
        Sequence sequence = new Sequence(3);
        Runnable task1 = () -> {};
        Runnable task2 = () -> {};
        
        sequence.set(0, task1);
        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void testSetWhenIndexIsNotNext() {
        Sequence sequence = new Sequence(3);
        Runnable task1 = () -> {};
        Runnable task2 = () -> {};
        
        sequence.set(1, task2);
        assertFalse(sequence.hasSecondTask());
    }

    @Test
    public void testHasSecondTaskWhenNull() {
        Sequence sequence = new Sequence(3);
        assertFalse(sequence.hasSecondTask());
    }

    @Test
    public void testHasSecondTaskWhenNotNull() {
        Sequence sequence = new Sequence(3);
        Runnable task = () -> {};
        sequence.set(1, task);
        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void testRunEagerlyWithAllTasksSet() {
        Sequence sequence = new Sequence(3);
        StringBuilder sb = new StringBuilder();
        Runnable task1 = () -> sb.append("1");
        Runnable task2 = () -> sb.append("2");
        Runnable task3 = () -> sb.append("3");
        
        sequence.set(0, task1);
        sequence.set(1, task2);
        sequence.set(2, task3);
        
        // This should trigger runEagerly since index 0 is next
        // But since we're calling set on index 0, it will run immediately
        // We need to manually call runEagerly to test it properly
        
        // Actually, let's test the behavior correctly:
        // Set tasks in order, so they get executed
        sequence.set(0, task1);
        sequence.set(1, task2);
        sequence.set(2, task3);
        
        // After setting all tasks, the sequence should be reset to 0
        assertEquals(0, sequence.next);
    }

    @Test
    public void testSetAndRunEagerly() {
        Sequence sequence = new Sequence(3);
        StringBuilder sb = new StringBuilder();
        Runnable task1 = () -> sb.append("A");
        Runnable task2 = () -> sb.append("B");
        Runnable task3 = () -> sb.append("C");
        
        sequence.set(0, task1);
        assertEquals("A", sb.toString());
        assertEquals(1, sequence.next);
        
        sequence.set(1, task2);
        assertEquals("AB", sb.toString());
        assertEquals(2, sequence.next);
        
        sequence.set(2, task3);
        assertEquals("ABC", sb.toString());
        assertEquals(0, sequence.next);
    }

    @Test
    public void testSetOutOfOrder() {
        Sequence sequence = new Sequence(3);
        StringBuilder sb = new StringBuilder();
        Runnable task1 = () -> sb.append("1");
        Runnable task2 = () -> sb.append("2");
        Runnable task3 = () -> sb.append("3");
        
        sequence.set(2, task3);
        assertEquals("", sb.toString());
        assertEquals(0, sequence.next);
        
        sequence.set(1, task2);
        assertEquals("", sb.toString());
        assertEquals(0, sequence.next);
        
        sequence.set(0, task1);
        assertEquals("123", sb.toString());
        assertEquals(0, sequence.next);
    }
}
