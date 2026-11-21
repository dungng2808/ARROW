package com.google.uzaygezen.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CompactHilbertCurve_ArrowTest {

  @Test
  public void constructorPreservesSpec() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(new int[] {1, 2});
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);
    assertEquals(spec, curve.getSpec());
  }

  @Test
  public void arrayConstructorCreatesExpectedSpec() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1, 2});
    assertEquals(new MultiDimensionalSpec(new int[] {1, 2}), curve.getSpec());
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

    assertEquals(BitVectorFactories.OPTIMAL.apply(2), index);
  }

  @Test
  public void indexWithMinimumLevelLeavesLowerBitsClear() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {2, 2});
    BitVector[] point = new BitVector[] {
        BitVectorFactories.OPTIMAL.apply(2),
        BitVectorFactories.OPTIMAL.apply(2)
    };
    point[0].set(1);
    point[1].set(1);
    BitVector index = BitVectorFactories.OPTIMAL.apply(4);

    curve.index(point, 1, index);

    assertEquals(4, index.length());
    assertEquals(0, index.get(0) ? 1 : 0);
    assertEquals(0, index.get(1) ? 1 : 0);
  }

  @Test
  public void toStringIsNotNull() {
    CompactHilbertCurve curve = new CompactHilbertCurve(new int[] {1});
    assertNotNull(curve.toString());
  }
}
