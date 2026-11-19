package org.zalando.logbook.netty;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sequence_ArrowTest {

    @Test
    public void runsTasksInOrderWhenSetSequentially() {
        Sequence sequence = new Sequence(2);
        List<Integer> order = new ArrayList<Integer>();

        sequence.set(0, () -> order.add(0));
        sequence.set(1, () -> order.add(1));

        assertEquals(Arrays.asList(0, 1), order);
    }

    @Test
    public void waitsForEarlierTaskBeforeRunningLaterTask() {
        Sequence sequence = new Sequence(2);
        List<Integer> order = new ArrayList<Integer>();

        sequence.set(1, () -> order.add(1));
        assertEquals(new ArrayList<Integer>(), order);

        sequence.set(0, () -> order.add(0));

        assertEquals(Arrays.asList(0, 1), order);
    }

    @Test
    public void reportsWhetherSecondTaskHasBeenSet() {
        Sequence sequence = new Sequence(2);

        assertFalse(sequence.hasSecondTask());

        sequence.set(1, () -> {
        });

        assertTrue(sequence.hasSecondTask());
    }

    @Test
    public void resetsAfterRunningAllTasks() {
        Sequence sequence = new Sequence(1);
        List<Integer> order = new ArrayList<Integer>();

        sequence.set(0, () -> order.add(1));
        sequence.set(0, () -> order.add(2));

        assertEquals(Arrays.asList(1, 2), order);
    }
}
