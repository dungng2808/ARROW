package com.vladsch.flexmark.experimental.util.sequence.managed;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.SubSequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BaseSequenceManager_ArrowTest {
    @Test
    public void createsAndCachesBaseSequence() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String base = "alpha";
        int[] callTypes = { -1 };

        BasedSequence first = manager.getBaseSequence(base, callTypes, value -> SubSequence.of(value));
        assertSame(base, first.getBase());
        assertSame(first, first.getBaseSequence());
        assertEquals(20, callTypes[0]);

        callTypes[0] = -1;
        BasedSequence second = manager.getBaseSequence(base, callTypes, value -> {
            throw new AssertionError("Factory should not be called for a cached base");
        });
        assertSame(first, second);
        assertEquals(0, callTypes[0]);
    }

    @Test
    public void reusesEquivalentBaseSequenceForEqualObjects() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String firstBase = new String("same");
        String equivalentBase = new String("same");
        int[] callTypes = { -1 };

        BasedSequence first = manager.getBaseSequence(firstBase, callTypes, value -> SubSequence.of(value));
        assertEquals(20, callTypes[0]);

        callTypes[0] = -1;
        BasedSequence second = manager.getBaseSequence(equivalentBase, callTypes, value -> {
            throw new AssertionError("Factory should not be called for an equivalent base");
        });
        assertSame(first, second);
        assertEquals(10, callTypes[0]);
    }
}
