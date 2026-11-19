package org.semver4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RangesList_ArrowTest {

    @Test
    public void addStoresNonEmptyRangesAndReturnsThis() {
        RangesList rangesList = new RangesList();
        List<Range> ranges = Collections.singletonList(new Range(">=1.0.0"));

        assertSame(rangesList, rangesList.add(ranges));
        assertEquals(1, rangesList.get().size());
        assertSame(ranges, rangesList.get().get(0));
    }

    @Test
    public void addIgnoresEmptyRanges() {
        RangesList rangesList = new RangesList();

        rangesList.add(Collections.<Range>emptyList());

        assertTrue(rangesList.get().isEmpty());
    }

    @Test
    public void getReturnsMutableRangesList() {
        RangesList rangesList = new RangesList();
        List<Range> ranges = new ArrayList<>();
        ranges.add(new Range(">=1.0.0"));

        rangesList.add(ranges);
        rangesList.get().clear();

        assertTrue(rangesList.get().isEmpty());
    }

    @Test
    public void isSatisfiedByAnyChecksAllRanges() {
        RangesList rangesList = new RangesList();
        rangesList.add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));

        assertTrue(rangesList.isSatisfiedByAny());
    }

    @Test
    public void isSatisfiedByAnyIsTrueForEmptyList() {
        assertTrue(new RangesList().isSatisfiedByAny());
    }

    @Test
    public void isSatisfiedByMatchesAnySetOfRanges() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range("<1.0.0")));
        rangesList.add(Collections.singletonList(new Range(">=2.0.0")));

        assertTrue(rangesList.isSatisfiedBy(new Semver("2.0.0")));
        assertFalse(rangesList.isSatisfiedBy(new Semver("1.5.0")));
    }

    @Test
    public void isSatisfiedByRequiresEveryRangeInASet() {
        RangesList rangesList = new RangesList();
        rangesList.add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));

        assertTrue(rangesList.isSatisfiedBy(new Semver("1.5.0")));
        assertFalse(rangesList.isSatisfiedBy(new Semver("2.0.0")));
    }

    @Test
    public void isSatisfiedByIsFalseForEmptyList() {
        assertFalse(new RangesList().isSatisfiedBy(new Semver("1.0.0")));
    }

    @Test
    public void toStringFormatsSingleAndMultipleRanges() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0")));
        rangesList.add(Arrays.asList(new Range(">=2.0.0"), new Range("<3.0.0")));

        assertEquals(">=1.0.0 or (>=2.0.0 and <3.0.0)", rangesList.toString());
    }

    @Test
    public void toStringIsEmptyForNoRanges() {
        assertEquals("", new RangesList().toString());
    }

    @Test
    public void prereleaseVersionRequiresMatchingPrereleaseRange() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0-alpha")));

        assertTrue(rangesList.isSatisfiedBy(new Semver("1.0.0-beta")));
        assertFalse(rangesList.isSatisfiedBy(new Semver("1.0.1-beta")));
    }
}
