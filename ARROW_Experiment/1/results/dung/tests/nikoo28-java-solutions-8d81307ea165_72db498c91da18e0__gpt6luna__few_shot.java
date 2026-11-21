package leetcode.medium;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OnlineStockSpan_ArrowTest {

  @Test
  public void nextReturnsSpanForIncreasingAndRepeatedPrices() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    assertEquals(1, stockSpan.next(100));
    assertEquals(1, stockSpan.next(80));
    assertEquals(1, stockSpan.next(60));
    assertEquals(2, stockSpan.next(70));
    assertEquals(1, stockSpan.next(60));
    assertEquals(4, stockSpan.next(75));
    assertEquals(6, stockSpan.next(85));
  }

  @Test
  public void nextCountsEqualPricesInSpan() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    assertEquals(1, stockSpan.next(50));
    assertEquals(2, stockSpan.next(50));
    assertEquals(3, stockSpan.next(50));
  }

  @Test
  public void calculateSpansForMixedPrices() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    assertArrayEquals(
        new int[] {1, 1, 1, 2, 1, 4, 6},
        stockSpan.calculateSpans(new int[] {100, 80, 60, 70, 60, 75, 85}));
  }

  @Test
  public void calculateSpansForIncreasingPrices() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    assertArrayEquals(
        new int[] {1, 2, 3, 4},
        stockSpan.calculateSpans(new int[] {10, 20, 30, 40}));
  }

  @Test
  public void calculateSpansForDecreasingPrices() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    assertArrayEquals(
        new int[] {1, 1, 1, 1},
        stockSpan.calculateSpans(new int[] {40, 30, 20, 10}));
  }
}
