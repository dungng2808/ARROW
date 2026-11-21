package leetcode.medium;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OnlineStockSpan_ArrowTest {

    @Test
    public void testNextSingleElement() {
        OnlineStockSpan span = new OnlineStockSpan();
        assertEquals(1, span.next(100));
    }

    @Test
    public void testNextMultipleElements() {
        OnlineStockSpan span = new OnlineStockSpan();
        assertEquals(1, span.next(100));
        assertEquals(1, span.next(80));
        assertEquals(1, span.next(60));
        assertEquals(2, span.next(70));
        assertEquals(1, span.next(60));
        assertEquals(4, span.next(75));
        assertEquals(6, span.next(85));
    }

    @Test
    public void testCalculateSpansEmptyArray() {
        OnlineStockSpan span = new OnlineStockSpan();
        int[] prices = {};
        int[] result = span.calculateSpans(prices);
        assertArrayEquals(new int[]{}, result);
    }

    @Test
    public void testCalculateSpansSingleElement() {
        OnlineStockSpan span = new OnlineStockSpan();
        int[] prices = {100};
        int[] result = span.calculateSpans(prices);
        assertArrayEquals(new int[]{1}, result);
    }

    @Test
    public void testCalculateSpansMultipleElements() {
        OnlineStockSpan span = new OnlineStockSpan();
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] result = span.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 1, 1, 2, 1, 4, 6}, result);
    }

    @Test
    public void testCalculateSpansIncreasingSequence() {
        OnlineStockSpan span = new OnlineStockSpan();
        int[] prices = {1, 2, 3, 4, 5};
        int[] result = span.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result);
    }

    @Test
    public void testCalculateSpansDecreasingSequence() {
        OnlineStockSpan span = new OnlineStockSpan();
        int[] prices = {5, 4, 3, 2, 1};
        int[] result = span.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 1, 1, 1, 1}, result);
    }

    @Test
    public void testNextAndCalculateSpansTogether() {
        OnlineStockSpan span = new OnlineStockSpan();
        assertEquals(1, span.next(100));
        int[] prices = {80, 60, 70, 60, 75, 85};
        int[] result = span.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 1, 2, 1, 4, 6}, result);
    }
}
