package org.semver4j;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RangesList_ArrowTest {

    @Test
    public void testAddEmptyList() {
        RangesList rangesList = new RangesList();
        RangesList result = rangesList.add(Collections.emptyList());
        
        assertSame(rangesList, result);
        assertTrue(rangesList.get().isEmpty());
    }

    @Test
    public void testAddNonEmptyList() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        List<Range> ranges = Arrays.asList(range1, range2);
        
        RangesList result = rangesList.add(ranges);
        
        assertSame(rangesList, result);
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
        
        List<List<Range>> result = rangesList.get();
        
        assertEquals(1, result.size());
        assertEquals(ranges, result.get(0));
    }

    @Test
    public void testIsSatisfiedByAnyTrue() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        
        assertTrue(rangesList.isSatisfiedByAny());
    }

    @Test
    public void testIsSatisfiedByAnyFalse() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range("<1.0.0");
        Range range2 = new Range(">2.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        
        assertFalse(rangesList.isSatisfiedByAny());
    }

    @Test
    public void testIsSatisfiedByTrue() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        
        Semver version = new Semver("1.5.0");
        assertTrue(rangesList.isSatisfiedBy(version));
    }

    @Test
    public void testIsSatisfiedByFalse() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        
        Semver version = new Semver("3.0.0");
        assertFalse(rangesList.isSatisfiedBy(version));
    }

    @Test
    public void testToString() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        
        String result = rangesList.toString();
        assertEquals("(>=1.0.0 and <=2.0.0)", result);
    }

    @Test
    public void testToStringMultipleRanges() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        Range range2 = new Range("<=2.0.0");
        Range range3 = new Range(">=3.0.0");
        Range range4 = new Range("<=4.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        rangesList.add(Arrays.asList(range3, range4));
        
        String result = rangesList.toString();
        assertEquals("(>=1.0.0 and <=2.0.0) or (>=3.0.0 and <=4.0.0)", result);
    }

    @Test
    public void testToStringSingleRange() {
        RangesList rangesList = new RangesList();
        Range range1 = new Range(">=1.0.0");
        rangesList.add(Collections.singletonList(range1));
        
        String result = rangesList.toString();
        assertEquals(">=1.0.0", result);
    }

    @Test
    public void testToStringEmpty() {
        RangesList rangesList = new RangesList();
        
        String result = rangesList.toString();
        assertEquals("", result);
    }
}
