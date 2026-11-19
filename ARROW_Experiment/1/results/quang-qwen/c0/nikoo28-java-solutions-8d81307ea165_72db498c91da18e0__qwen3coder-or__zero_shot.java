package leetcode.medium;

import org.junit.Test;
import static org.junit.Assert.*;

public class OnlineStockSpan_ArrowTest {

    @Test
    public void testNextSingleElement() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        assertEquals(1, spanner.next(100));
    }

    @Test
    public void testNextIncreasingSequence() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        assertEquals(1, spanner.next(100));
        assertEquals(2, spanner.next(150));
        assertEquals(3, spanner.next(200));
    }

    @Test
    public void testNextDecreasingSequence() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        assertEquals(1, spanner.next(200));
        assertEquals(1, spanner.next(150));
        assertEquals(1, spanner.next(100));
    }

    @Test
    public void testNextMixedSequence() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        assertEquals(1, spanner.next(100));
        assertEquals(1, spanner.next(80));
        assertEquals(1, spanner.next(60));
        assertEquals(1, spanner.next(70));
        assertEquals(2, spanner.next(60));
        assertEquals(1, spanner.next(75));
        assertEquals(6, spanner.next(85));
    }

    @Test
    public void testCalculateSpansEmptyArray() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        int[] prices = {};
        int[] expected = {};
        assertArrayEquals(expected, spanner.calculateSpans(prices));
    }

    @Test
    public void testCalculateSpansSingleElement() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        int[] prices = {100};
        int[] expected = {1};
        assertArrayEquals(expected, spanner.calculateSpans(prices));
    }

    @Test
    public void testCalculateSpansAllIncreasing() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        int[] prices = {100, 150, 200};
        int[] expected = {1, 2, 3};
        assertArrayEquals(expected, spanner.calculateSpans(prices));
    }

    @Test
    public void testCalculateSpansAllDecreasing() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        int[] prices = {200, 150, 100};
        int[] expected = {1, 1, 1};
        assertArrayEquals(expected, spanner.calculateSpans(prices));
    }

    @Test
    public void testCalculateSpansMixedValues() {
        OnlineStockSpan spanner = new OnlineStockSpan();
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] expected = {1, 1, 1, 2, 1, 4, 6};
        assertArrayEquals(expected, spanner.calculateSpans(prices));
    }
}
