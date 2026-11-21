package leetcode.medium;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class OnlineStockSpan_ArrowTest {

    @Test
    public void testNextSingleElement() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int result = stockSpan.next(100);
        assertEquals(1, result);
    }

    @Test
    public void testNextMultipleElements() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        stockSpan.next(100);
        stockSpan.next(80);
        stockSpan.next(60);
        stockSpan.next(70);
        int result = stockSpan.next(60);
        assertEquals(1, result);
    }

    @Test
    public void testNextWithIncreasingPrices() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        stockSpan.next(10);
        stockSpan.next(20);
        stockSpan.next(30);
        int result = stockSpan.next(40);
        assertEquals(4, result);
    }

    @Test
    public void testCalculateSpans() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] expected = {1, 1, 1, 2, 1, 4, 6};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testCalculateSpansSingleElement() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {100};
        int[] expected = {1};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testCalculateSpansAllEqualPrices() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {50, 50, 50, 50};
        int[] expected = {1, 1, 1, 1};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testCalculateSpansDecreasingPrices() {
        OnlineStockSpan stockSpan = new OnlineStockSpan();
        int[] prices = {100, 90, 80, 70, 60};
        int[] expected = {1, 1, 1, 1, 1};
        int[] result = stockSpan.calculateSpans(prices);
        assertArrayEquals(expected, result);
    }
}
