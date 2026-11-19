package com.google.uzaygezen.core;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class CompactHilbertCurve_ArrowTest {

  @Test
  public void testConstructorWithMultiDimensionalSpec() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(Lists.newArrayList(2, 3));
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);
    assertEquals(spec, curve.getSpec());
  }

  @Test
  public void testConstructorWithIntArray() {
    int[] m = {2, 3};
    CompactHilbertCurve curve = new CompactHilbertCurve(m);
    MultiDimensionalSpec spec = curve.getSpec();
    assertEquals(2, spec.getBitsPerDimension().size());
    assertEquals(Integer.valueOf(2), spec.getBitsPerDimension().get(0));
    assertEquals(Integer.valueOf(3), spec.getBitsPerDimension().get(1));
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
    
    BitVector[] p = new BitVector[2];
    p[0] = BitVectorFactories.OPTIMAL.apply(3);
    p[1] = BitVectorFactories.OPTIMAL.apply(3);
    
    CompactHilbertCurve.copyOneBitToEachDimensionWhereSet(src, 1, p);
    
    assertTrue(p[1].get(1)); // bit 0 in src maps to bit 1 in p[1]
    assertTrue(p[0].get(1)); // bit 2 in src maps to bit 1 in p[0]
  }

  @Test
  public void testGetSpec() {
    MultiDimensionalSpec spec = new MultiDimensionalSpec(Lists.newArrayList(2, 3));
    CompactHilbertCurve curve = new CompactHilbertCurve(spec);
    assertEquals(spec, curve.getSpec());
  }
}
