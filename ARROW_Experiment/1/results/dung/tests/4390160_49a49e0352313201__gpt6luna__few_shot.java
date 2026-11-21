package com.somethingsimilar.opposite_of_a_bloom_filter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteArrayFilter_ArrowTest {

    @Test
    public void reportsArraySizeRoundedUpToPowerOfTwo() {
        ByteArrayFilter filter = new ByteArrayFilter(5);
        assertEquals(8, filter.getSize());
    }

    @Test
    public void reportsSizeForPowerOfTwoInput() {
        ByteArrayFilter filter = new ByteArrayFilter(8);
        assertEquals(8, filter.getSize());
    }

    @Test
    public void firstOccurrenceIsNotContainedAndSecondIsContained() {
        ByteArrayFilter filter = new ByteArrayFilter(16);
        byte[] id = new byte[] {1, 2, 3};

        assertFalse(filter.containsAndAdd(id));
        assertTrue(filter.containsAndAdd(id));
    }

    @Test
    public void differentIdsAreNotReportedAsContainedWhenTheyDoNotCollide() {
        ByteArrayFilter filter = new ByteArrayFilter(16);

        assertFalse(filter.containsAndAdd(new byte[] {1}));
        assertFalse(filter.containsAndAdd(new byte[] {2}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroSize() {
        new ByteArrayFilter(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeSize() {
        new ByteArrayFilter(-1);
    }
}
