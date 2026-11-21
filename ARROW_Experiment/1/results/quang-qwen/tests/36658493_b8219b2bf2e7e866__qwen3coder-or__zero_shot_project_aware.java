package com.google.uzaygezen.core;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class CompactHilbertCurve_ArrowTest {

  @Test
  public void testConstructorWithMultiDimensionalSpec() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(Lists.newArrayList(3, 2, 4));
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);
    assertEquals(spec, curve.getSpec());
  }

  @Test
  public void testConstructorWithIntArray() {
    int[] m = {3, 2, 4};
    CompactHilbertCurve curve = new CompactHilbertCurve(m);
    MultiDimensionalSpec spec = curve.getSpec();
    assertEquals(3, spec.getBitsPerDimension().size());
    assertEquals(Integer.valueOf(3), spec.getBitsPerDimension().get(0));
    assertEquals(Integer.valueOf(2), spec.getBitsPerDimension().get(1));
    assertEquals(Integer.valueOf(4), spec.getBitsPerDimension().get(2));
  }

  @Test
  public void testCopyOneBitFromEachDimension() {
    BitVector[] p = new BitVector[3];
    p[0] = BitVectorFactories.OPTIMAL.apply(3);
    p[1] = BitVectorFactories.OPTIMAL.apply(2);
    p[2] = BitVectorFactories.OPTIMAL.apply(4);
    
    p[0].set(0);
    p[0].set(2);
    p[1].set(1);
    p[2].set(3);
    
    BitVector bs = BitVectorFactories.OPTIMAL.apply(3);
    CompactHilbertCurve.copyOneBitFromEachDimension(2, p, bs);
    
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    assertTrue(bs.get(2));
  }

  @Test
  public void testClearOneBitInEachDimension() {
    BitVector[] p = new BitVector[3];
    p[0] = BitVectorFactories.OPTIMAL.apply(3);
    p[1] = BitVectorFactories.OPTIMAL.apply(2);
    p[2] = BitVectorFactories.OPTIMAL.apply(4);
    
    p[0].set(0);
    p[0].set(2);
    p[1].set(1);
    p[2].set(3);
    
    CompactHilbertCurve.clearOneBitInEachDimension(2, p);
    
    assertFalse(p[0].get(2));
    assertFalse(p[1].get(2));
    assertFalse(p[2].get(2));
  }

  @Test
  public void testCopyOneBitToEachDimensionWhereSet() {
    BitVector src = BitVectorFactories.OPTIMAL.apply(3);
    src.set(0);
    src.set(2);
    
    BitVector[] p = new BitVector[3];
    for (int i = 0; i < 3; i++) {
      p[i] = BitVectorFactories.OPTIMAL.apply(3);
    }
    
    CompactHilbertCurve.copyOneBitToEachDimensionWhereSet(src, 1, p);
    
    assertTrue(p[2].get(1));
    assertFalse(p[1].get(1));
    assertTrue(p[0].get(1));
  }
}
