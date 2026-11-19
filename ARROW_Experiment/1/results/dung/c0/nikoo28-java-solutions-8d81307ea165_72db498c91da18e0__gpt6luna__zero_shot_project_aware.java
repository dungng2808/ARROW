package leetcode.medium;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OnlineStockSpan_ArrowTest {

  @Test
  public void nextReturnsSpanForIncreasingAndDecreasingPrices() {
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
  public void nextIncludesPreviousEqualPrices() {
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
  public void calculateSpansIncludesEqualPrices() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    assertArrayEquals(
        new int[] {1, 2, 3},
        stockSpan.calculateSpans(new int[] {50, 50, 50}));
  }

  @Test
  public void calculateSpansForEmptyArrayThrowsArrayIndexOutOfBoundsException() {
    OnlineStockSpan stockSpan = new OnlineStockSpan();

    org.junit.jupiter.api.Assertions.assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> stockSpan.calculateSpans(new int[0]));
  }
}
