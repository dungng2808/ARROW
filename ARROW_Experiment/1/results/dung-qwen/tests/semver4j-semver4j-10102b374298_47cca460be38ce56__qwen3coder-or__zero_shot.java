package org.semver4j;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RangesList_ArrowTest {

    @Test
    public void testAddEmptyRanges() {
        RangesList rangesList = new RangesList();
        rangesList.add(Collections.emptyList());
        assertEquals(0, rangesList.get().size());
    }

    @Test
    public void testAddNonEmptyRanges() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        rangesList.add(ranges);
        assertEquals(1, rangesList.get().size());
        assertEquals(ranges, rangesList.get().get(0));
    }

    @Test
    public void testGet() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        rangesList.add(ranges);
        assertEquals(1, rangesList.get().size());
        assertEquals(ranges, rangesList.get().get(0));
    }

    @Test
    public void testIsSatisfiedByAnyTrue() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        rangesList.add(ranges);
        assertTrue(rangesList.isSatisfiedByAny());
    }

    @Test
    public void testIsSatisfiedByAnyFalse() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range("<1.0.0");
        Range range2 = new Range(">2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        rangesList.add(ranges);
        assertFalse(rangesList.isSatisfiedByAny());
    }

    @Test
    public void testIsSatisfiedByTrue() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        rangesList.add(ranges);
        Semver version = new Semver("1.5.0");
        assertTrue(rangesList.isSatisfiedBy(version));
    }

    @Test
    public void testIsSatisfiedByFalse() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        rangesList.add(ranges);
        Semver version = new Semver("3.0.0");
        assertFalse(rangesList.isSatisfiedBy(version));
    }

    @Test
    public void testToStringEmpty() {
        RangesList rangesList = new RangesList();
        assertEquals("", rangesList.toString());
    }

    @Test
    public void testToStringSingleRange() {
        RangesList rangesList = new RangesList();
        Range range = new Range(">=1.0.0");
        rangesList.add(Arrays.asList(range));
        assertEquals(">=1.0.0", rangesList.toString());
    }

    @Test
    public void testToStringMultipleRanges() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        assertEquals("(>=1.0.0 and <=2.0.0)", rangesList.toString());
    }

    @Test
    public void testToStringMultipleRangeSets() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        Range range3 = new Range(">=3.0.0");
        Range range4 = new Range("<=4.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        rangesList.add(Arrays.asList(range3, range4));
        assertEquals("(>=1.0.0 and <=2.0.0) or (>=3.0.0 and <=4.0.0)", rangesList.toString());
    }
}
