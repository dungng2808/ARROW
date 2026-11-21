package org.zalando.logbook.netty;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Sequence_ArrowTest {

    @Test
    public void runsTasksInOrderWhenSetSequentially() {
        Sequence sequence = new Sequence(3);
        List<Integer> results = new ArrayList<Integer>();

        sequence.set(0, () -> results.add(0));
        sequence.set(1, () -> results.add(1));
        sequence.set(2, () -> results.add(2));

        assertEquals(Arrays.asList(0, 1, 2), results);
    }

    @Test
    public void waitsForEarlierTasksBeforeRunningLaterTasks() {
        Sequence sequence = new Sequence(3);
        List<Integer> results = new ArrayList<Integer>();

        sequence.set(2, () -> results.add(2));
        sequence.set(1, () -> results.add(1));

        assertEquals(new ArrayList<Integer>(), results);

        sequence.set(0, () -> results.add(0));

        assertEquals(Arrays.asList(0, 1, 2), results);
    }

    @Test
    public void reportsWhetherSecondTaskHasBeenSet() {
        Sequence sequence = new Sequence(2);

        assertFalse(sequence.hasSecondTask());

        sequence.set(1, () -> { });

        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void resetsAfterAllTasksRun() {
        Sequence sequence = new Sequence(2);
        List<Integer> results = new ArrayList<Integer>();

        sequence.set(0, () -> results.add(0));
        sequence.set(1, () -> results.add(1));
        sequence.set(0, () -> results.add(2));
        sequence.set(1, () -> results.add(3));

        assertEquals(Arrays.asList(0, 1, 2, 3), results);
    }
}
