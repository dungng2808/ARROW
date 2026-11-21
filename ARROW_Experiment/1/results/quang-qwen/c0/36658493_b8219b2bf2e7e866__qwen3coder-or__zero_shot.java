package com.google.uzaygezen.core;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class CompactHilbertCurve_ArrowTest {

  @Test
  public void testConstructorWithMultiDimensionalSpec() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(Lists.newArrayList(2, 3));
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);
    assertNotNull(curve);
    assertEquals(spec, curve.getSpec());
  }

  @Test
  public void testConstructorWithIntArray() {
    int[] m = {2, 3};
    CompactHilbertCurve curve = new CompactHilbertCurve(m);
    assertNotNull(curve);
    assertEquals(2, curve.getSpec().getBitsPerDimension().size());
  }

  @Test
  public void testGetSpec() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(Lists.newArrayList(1, 2, 3));
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);
    assertEquals(spec, curve.getSpec());
  }

  @Test
  public void testCopyOneBitFromEachDimension() {
    BitVector[] p = new BitVector[2];
    p[0] = BitVectorFactories.OPTIMAL.apply(3);
    p[1] = BitVectorFactories.OPTIMAL.apply(3);
    
    p[0].set(1);
    p[1].set(2);
    
    BitVector bs = BitVectorFactories.OPTIMAL.apply(2);
    CompactHilbertCurve.copyOneBitFromEachDimension(1, p, bs);
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    
    bs.clear();
    CompactHilbertCurve.copyOneBitFromEachDimension(2, p, bs);
    assertFalse(bs.get(0));
    assertTrue(bs.get(1));
  }

  @Test
  public void testClearOneBitInEachDimension() {
    BitVector[] p = new BitVector[2];
    p[0] = BitVectorFactories.OPTIMAL.apply(3);
    p[1] = BitVectorFactories.OPTIMAL.apply(3);
    
    p[0].set(1);
    p[1].set(2);
    
    CompactHilbertCurve.clearOneBitInEachDimension(1, p);
    assertFalse(p[0].get(1));
    assertTrue(p[1].get(2));
    
    CompactHilbertCurve.clearOneBitInEachDimension(2, p);
    assertFalse(p[1].get(2));
  }

  @Test
  public void testCopyOneBitToEachDimensionWhereSet() {
    BitVector src = BitVectorFactories.OPTIMAL.apply(3);
    src.set(0);
    src.set(2);
    
    BitVector[] p = new BitVector[3];
    for (int i = 0; i < 3; i++) {
      p[i] = BitVectorFactories.OPTIMAL.apply(2);
    }
    
    CompactHilbertCurve.copyOneBitToEachDimensionWhereSet(src, 1, p);
    
    assertTrue(p[2].get(1));
    assertFalse(p[1].get(1));
    assertTrue(p[0].get(1));
  }
}
