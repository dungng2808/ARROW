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
        String text = "alpha";
        int[] callTypes = { -1 };

        BasedSequence first = manager.getBaseSequence(text, callTypes, value -> SubSequence.of(value));
        assertSame(first, first.getBaseSequence());
        assertSame(text, first.getBase());
        assertEquals(20, callTypes[0]);

        callTypes[0] = -1;
        BasedSequence second = manager.getBaseSequence(text, callTypes, value -> {
            throw new AssertionError("Factory should not be called for a cached base");
        });

        assertSame(first, second);
        assertEquals(0, callTypes[0]);
    }

    @Test
    public void reusesEquivalentBaseForEqualObject() {
        BaseSequenceManager manager = new BaseSequenceManager();
        String firstText = new String("same");
        String secondText = new String("same");
        int[] callTypes = { -1 };

        BasedSequence first = manager.getBaseSequence(firstText, callTypes, value -> SubSequence.of(value));
        assertEquals(20, callTypes[0]);

        callTypes[0] = -1;
        BasedSequence second = manager.getBaseSequence(secondText, callTypes, value -> {
            throw new AssertionError("Factory should not be called for an equivalent base");
        });

        assertSame(first, second);
        assertEquals(10, callTypes[0]);
    }
}
