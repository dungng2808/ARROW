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
    public void addReturnsThisAndStoresNonEmptyRanges() {
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
    public void getReturnsMutableBackingList() {
        RangesList rangesList = new RangesList();
        List<List<Range>> expected = new ArrayList<List<Range>>();

        assertSame(rangesList.get(), rangesList.get());
        rangesList.get().add(Collections.<Range>emptyList());
        expected.add(Collections.<Range>emptyList());
        assertEquals(expected, rangesList.get());
    }

    @Test
    public void isSatisfiedByAnyIsTrueForEmptyList() {
        assertTrue(new RangesList().isSatisfiedByAny());
    }

    @Test
    public void isSatisfiedByAnyChecksAllRanges() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0")));
        rangesList.add(Collections.singletonList(new Range("<0.0.0")));

        assertFalse(rangesList.isSatisfiedByAny());
    }

    @Test
    public void isSatisfiedByAnyIsTrueWhenEveryRangeCanMatch() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0")));
        rangesList.add(Collections.singletonList(new Range("<2.0.0")));

        assertTrue(rangesList.isSatisfiedByAny());
    }

    @Test
    public void isSatisfiedByMatchesAnySetOfRanges() {
        RangesList rangesList = new RangesList();
        rangesList.add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));
        rangesList.add(Collections.singletonList(new Range(">=3.0.0")));

        assertTrue(rangesList.isSatisfiedBy(new Semver("3.0.0")));
        assertFalse(rangesList.isSatisfiedBy(new Semver("2.5.0")));
    }

    @Test
    public void isSatisfiedByRequiresEveryRangeInASet() {
        RangesList rangesList = new RangesList();
        rangesList.add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));

        assertFalse(rangesList.isSatisfiedBy(new Semver("2.0.0")));
    }

    @Test
    public void isSatisfiedByRejectsPrereleaseWithoutMatchingPrereleaseRange() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0")));

        assertFalse(rangesList.isSatisfiedBy(new Semver("1.0.0-alpha")));
    }

    @Test
    public void isSatisfiedByAllowsPrereleaseWhenRangeHasPrereleaseAtSameCoreVersion() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0-alpha")));

        assertTrue(rangesList.isSatisfiedBy(new Semver("1.0.0-beta")));
    }

    @Test
    public void toStringFormatsEmptyAndSingleRangeSets() {
        assertEquals("", new RangesList().toString());

        RangesList rangesList = new RangesList();
        rangesList.add(Collections.singletonList(new Range(">=1.0.0")));
        rangesList.add(Collections.singletonList(new Range("<2.0.0")));

        assertEquals(">=1.0.0 or <2.0.0", rangesList.toString());
    }

    @Test
    public void toStringFormatsMultipleRangesWithinSets() {
        RangesList rangesList = new RangesList();
        rangesList.add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));
        rangesList.add(Arrays.asList(new Range(">=3.0.0"), new Range("<4.0.0")));

        assertEquals("(>=1.0.0 and <2.0.0) or (>=3.0.0 and <4.0.0)", rangesList.toString());
    }

    @Test
    public void toStringRemovesParenthesesAroundOnlySet() {
        RangesList rangesList = new RangesList();
        rangesList.add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));

        assertEquals(">=1.0.0 and <2.0.0", rangesList.toString());
    }
}
