package org.zalando.logbook.netty;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Sequence_ArrowTest {

    @Test
    public void hasSecondTaskIsFalseInitially() {
        Sequence sequence = new Sequence(2);

        assertFalse(sequence.hasSecondTask());
    }

    @Test
    public void hasSecondTaskReflectsSecondTaskAssignment() {
        Sequence sequence = new Sequence(2);
        sequence.set(1, new Runnable() {
            @Override
            public void run() {
            }
        });

        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void runsTasksInOrderWhenEarlierTaskIsSet() {
        Sequence sequence = new Sequence(2);
        final AtomicInteger count = new AtomicInteger();

        sequence.set(1, new Runnable() {
            @Override
            public void run() {
                count.addAndGet(10);
            }
        });
        assertEquals(0, count.get());

        sequence.set(0, new Runnable() {
            @Override
            public void run() {
                count.incrementAndGet();
            }
        });

        assertEquals(11, count.get());
        assertFalse(sequence.hasSecondTask());
    }

    @Test
    public void runsSingleTaskAndResetsForAnotherSequenceOfTasks() {
        Sequence sequence = new Sequence(1);
        final AtomicInteger count = new AtomicInteger();

        sequence.set(0, new Runnable() {
            @Override
            public void run() {
                count.incrementAndGet();
            }
        });
        sequence.set(0, new Runnable() {
            @Override
            public void run() {
                count.incrementAndGet();
            }
        });

        assertEquals(2, count.get());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void rejectsNegativeLength() {
        new Sequence(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void rejectsOutOfRangeTaskIndex() {
        Sequence sequence = new Sequence(2);
        sequence.set(2, new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}
