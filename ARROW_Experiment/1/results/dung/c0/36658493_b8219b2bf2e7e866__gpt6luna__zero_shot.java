package com.google.uzaygezen.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CompactHilbertCurve_ArrowTest {

  @Test
  public void getSpecReturnsTheSuppliedSpecification() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(new int[] {1, 2});
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);

    assertEquals(spec, curve.getSpec());
  }

  @Test
  public void indexAndInverseRoundTrip() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {2, 2});
    BitVector[] point = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(2),
        BitVectorFactories.OPTIMAL.apply(2)
    };
    point[0].set(0);
    point[1].set(1);
    BitVector index = BitVectorFactories.OPTIMAL.apply(4);

    curve.index(point, 0, index);

    BitVector[] decoded = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(2),
        BitVectorFactories.OPTIMAL.apply(2)
    };
    curve.indexInverse(index, decoded);

    assertEquals(point[0], decoded[0]);
    assertEquals(point[1], decoded[1]);
  }

  @Test
  public void indexOfOriginIsZero() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    BitVector[] point = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(1),
        BitVectorFactories.OPTIMAL.apply(1)
    };
    BitVector index = BitVectorFactories.OPTIMAL.apply(2);

    curve.index(point, 0, index);

    assertEquals(0, index.cardinality());
  }

  @Test
  public void indexRejectsWrongNumberOfCoordinates() {
    final CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 1});
    final BitVector[] point = new BitVector[] {BitVectorFactories.OPTIMAL.apply(1)};
    final BitVector index = BitVectorFactories.OPTIMAL.apply(2);

    try {
      curve.index(point, 0, index);
    } catch (IllegalArgumentException expected) {
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void indexRejectsCoordinateWiderThanItsDimension() {
    final CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1});
    final BitVector[] point = new BitVector[] {BitVectorFactories.OPTIMAL.apply(2)};
    final BitVector index = BitVectorFactories.OPTIMAL.apply(1);

    try {
      curve.index(point, 0, index);
    } catch (IllegalArgumentException expected) {
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void indexInverseClearsAndReplacesCoordinates() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1});
    BitVector index = BitVectorFactories.OPTIMAL.apply(1);
    index.set(0);
    BitVector[] point = new BitVector[] {BitVectorFactories.OPTIMAL.apply(1)};
    point[0].set(0);

    curve.indexInverse(index, point);

    assertTrue(point[0].get(0));
  }

  @Test
  public void toStringIncludesClassName() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1});

    assertTrue(curve.toString().contains("CompactHilbertCurve"));
  }
}
