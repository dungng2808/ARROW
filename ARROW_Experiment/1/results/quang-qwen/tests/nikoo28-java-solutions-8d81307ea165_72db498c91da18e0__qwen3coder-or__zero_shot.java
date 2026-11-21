package leetcode.medium;

import org.junit.Test;
import static org.junit.Assert.*;

public class OnlineStockSpan_ArrowTest {

    @Test
    public void testNextSingleElement() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int result = stockSpan.next(100);
        assertEquals(1, result);
    }

    @Test
    public void testNextIncreasingSequence() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        stockSpan.next(100);
        int result = stockSpan.next(101);
        assertEquals(2, result);
    }

    @Test
    public void testNextDecreasingSequence() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        stockSpan.next(101);
        int result = stockSpan.next(100);
        assertEquals(1, result);
    }

    @Test
    public void testNextMixedSequence() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        stockSpan.next(100);
        stockSpan.next(80);
        stockSpan.next(60);
        stockSpan.next(70);
        int result = stockSpan.next(60);
        assertEquals(1, result);
    }

    @Test
    public void testCalculateSpansEmptyArray() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {};
        int[] result = stockSpan.calculateSpans(prices);
        assertEquals(0, result.length);
    }

    @Test
    public void testCalculateSpansSingleElement() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {100};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(new int[]{1}, result);
    }

    @Test
    public void testCalculateSpansIncreasingSequence() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {100, 101, 102, 103};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 2, 3, 4}, result);
    }

    @Test
    public void testCalculateSpansDecreasingSequence() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {103, 102, 101, 100};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 1, 1, 1}, result);
    }

    @Test
    public void testCalculateSpansMixedSequence() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(new int[]{1, 1, 1, 2, 1, 4, 6}, result);
    }
}
