package com.vladsch.flexmark.experimental.util.sequence.managed;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.SubSequence;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BaseSequenceManager_ArrowTest {

    @Test
    public void createsAndReturnsBaseSequence() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String base = "alpha";
        int[] callTypes = {-1};

        BasedSequence result = manager.getBaseSequence(
                base,
                callTypes,
                value -> SubSequence.of(value));

        assertSame(base, result.getBase());
        assertSame(result, result.getBaseSequence());
        assertEquals(20, callTypes[0]);
    }

    @Test
    public void returnsCachedBaseSequence() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String base = "alpha";
        int[] callTypes = {-1};

        BasedSequence first = manager.getBaseSequence(base, null, value -> SubSequence.of(value));
        BasedSequence second = manager.getBaseSequence(base, callTypes, value -> SubSequence.of(value));

        assertSame(first, second);
        assertEquals(0, callTypes[0]);
    }

    @Test
    public void reusesEquivalentBaseSequenceForEqualObjects() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String firstBase = new String("alpha");
        String equivalentBase = new String("alpha");

        BasedSequence first = manager.getBaseSequence(firstBase, null, value -> SubSequence.of(value));
        int[] callTypes = {-1};
        BasedSequence second = manager.getBaseSequence(
                equivalentBase,
                callTypes,
                value -> SubSequence.of(value));

        assertSame(first, second);
        assertEquals(10, callTypes[0]);
    }
}
