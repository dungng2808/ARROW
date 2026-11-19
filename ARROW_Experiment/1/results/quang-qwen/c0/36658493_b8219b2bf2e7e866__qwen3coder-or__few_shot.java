package com.google.uzaygezen.core;

import com.google.common.primitives.Ints;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompactHilbertCurve_ArrowTest {

    @Test
    public void testConstructorWithMultiDimensionalSpec() {
        MultiDimensionalSpec spec = new MultiDimensionalSpec(Ints.asList(2, 3));
        CompactHilbertCurve curve = new CompactHilbertCurve(spec);
        assertNotNull(curve);
        assertEquals(spec, curve.getSpec());
    }

    @Test
    public void testConstructorWithIntArray() {
        int[] m = {2, 3};
        CompactHilbertCurve curve = new CompactHilbertCurve(m);
        assertNotNull(curve);
        assertNotNull(curve.getSpec());
    }

    @Test
    public void testCopyOneBitFromEachDimension() {
        BitVector[] p = new BitVector[2];
        p[0] = BitVectorFactories.OPTIMAL.apply(2);
        p[1] = BitVectorFactories.OPTIMAL.apply(2);
        
        p[0].set(0);
        p[1].set(1);
        
        BitVector bs = BitVectorFactories.OPTIMAL.apply(2);
        CompactHilbertCurve.copyOneBitFromEachDimension(0, p, bs);
        assertTrue(bs.get(0));
        assertFalse(bs.get(1));
        
        bs.clear();
        CompactHilbertCurve.copyOneBitFromEachDimension(1, p, bs);
        assertFalse(bs.get(0));
        assertTrue(bs.get(1));
    }

    @Test
    public void testClearOneBitInEachDimension() {
        BitVector[] p = new BitVector[2];
        p[0] = BitVectorFactories.OPTIMAL.apply(2);
        p[1] = BitVectorFactories.OPTIMAL.apply(2);
        
        p[0].set(0);
        p[1].set(1);
        
        CompactHilbertCurve.clearOneBitInEachDimension(0, p);
        assertFalse(p[0].get(0));
        assertTrue(p[1].get(1));
        
        CompactHilbertCurve.clearOneBitInEachDimension(1, p);
        assertFalse(p[0].get(0));
        assertFalse(p[1].get(1));
    }

    @Test
    public void testCopyOneBitToEachDimensionWhereSet() {
        BitVector src = BitVectorFactories.OPTIMAL.apply(2);
        src.set(0);
        src.set(1);
        
        BitVector[] p = new BitVector[2];
        p[0] = BitVectorFactories.OPTIMAL.apply(2);
        p[1] = BitVectorFactories.OPTIMAL.apply(2);
        
        CompactHilbertCurve.copyOneBitToEachDimensionWhereSet(src, 0, p);
        assertTrue(p[1].get(0)); // reversed order
        assertTrue(p[0].get(0));
    }
}
