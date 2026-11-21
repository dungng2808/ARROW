package com.google.uzaygezen.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompactHilbertCurve_ArrowTest {

  @Test
  public void specIsReturned() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    assertEquals(2, curve.getSpec().sumBitsPerDimension());
    assertEquals(1, curve.getSpec().maxBitsPerDimension());
  }

  @Test
  public void indexAndInverseRoundTrip() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    BitVector[] point = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(1),
        BitVectorFactories.OPTIMAL.apply(1)
    };
    point[0].set(0);
    BitVector index = BitVectorFactories.OPTIMAL.apply(2);

    curve.index(point, 0, index);

    BitVector[] decoded = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(1),
        BitVectorFactories.OPTIMAL.apply(1)
    };
    curve.indexInverse(index, decoded);

    assertEquals(point[0], decoded[0]);
    assertEquals(point[1], decoded[1]);
  }

  @Test
  public void indexAtMinimumLevelClearsOutput() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    BitVector[] point = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(1),
        BitVectorFactories.OPTIMAL.apply(1)
    };
    BitVector index = BitVectorFactories.OPTIMAL.apply(2);
    index.set(0);
    index.set(1);

    curve.index(point, 1, index);

    assertFalse(index.get(0));
    assertFalse(index.get(1));
  }

  @Test
  public void indexRejectsWrongNumberOfCoordinates() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    BitVector index = BitVectorFactories.OPTIMAL.apply(2);

    try {
      curve.index(new BitVector[] {BitVectorFactories.OPTIMAL.apply(1)}, 0, index);
    } catch (IllegalArgumentException expected) {
      assertTrue(true);
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void indexRejectsCoordinateWiderThanDimension() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    BitVector index = BitVectorFactories.OPTIMAL.apply(2);

    try {
      curve.index(new BitVector[] {
          BitVectorFactories.OPTIMAL.apply(2),
          BitVectorFactories.OPTIMAL.apply(1)
      }, 0, index);
    } catch (IllegalArgumentException expected) {
      assertTrue(true);
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void toStringIsNotNull() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    assertTrue(curve.toString() != null);
  }
}
