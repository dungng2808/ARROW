package cc.creativecomputing.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CCVector4_ArrowTest {

    @Test
    public void constructorsAndArrayConversion() {
        CCVector4 vector = new CCVector4(new double[] {1.0, 2.0, 3.0, 4.0});
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.toArray(), 0.0);

        CCVector4 shortVector = new CCVector4(new double[] {5.0, 6.0});
        assertArrayEquals(new double[] {5.0, 6.0, 0.0, 0.0}, shortVector.toArray(), 0.0);

        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, new CCVector4().toArray(), 0.0);
    }

    @Test
    public void indexedAccessAndMutation() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.0, vector.getValue(0), 0.0);
        assertEquals(2.0, vector.getValue(1), 0.0);
        assertEquals(3.0, vector.getValue(2), 0.0);
        assertEquals(4.0, vector.getValue(3), 0.0);

        vector.setValue(2, 9.0);
        assertEquals(9.0, vector.z, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getValueRejectsInvalidIndex() {
        new CCVector4().getValue(4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValueRejectsInvalidIndex() {
        new CCVector4().setValue(-1, 2.0);
    }

    @Test
    public void toArrayUsesProvidedStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double[] store = new double[5];
        assertSame(store, vector.toArray(store));
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0, 0.0}, store, 0.0);
    }

    @Test
    public void setAndZero() {
        CCVector4 vector = new CCVector4();
        assertSame(vector, vector.set(1.0, 2.0, 3.0, 4.0));
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.toArray(), 0.0);
        assertSame(vector, vector.zero());
        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, vector.toArray(), 0.0);
    }

    @Test
    public void addAndSubtract() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertArrayEquals(new double[] {6.0, 8.0, 10.0, 12.0}, vector.add(5.0, 6.0, 7.0, 8.0).toArray(), 0.0);
        assertArrayEquals(new double[] {-4.0, -4.0, -4.0, -4.0}, vector.subtract(5.0, 6.0, 7.0, 8.0).toArray(), 0.0);
    }

    @Test
    public void localArithmeticMutatesAndReturnsThis() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertSame(vector, vector.addLocal(1.0, 1.0, 1.0, 1.0));
        assertArrayEquals(new double[] {2.0, 3.0, 4.0, 5.0}, vector.toArray(), 0.0);

        assertSame(vector, vector.subtractLocal(1.0, 1.0, 1.0, 1.0));
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.toArray(), 0.0);
    }

    @Test
    public void multiplyAndDivide() {
        CCVector4 vector = new CCVector4(2.0, 4.0, 6.0, 8.0);
        assertArrayEquals(new double[] {4.0, 12.0, 24.0, 40.0}, vector.multiply(2.0, 3.0, 4.0, 5.0).toArray(), 0.0);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.divide(2.0).toArray(), 0.0);
    }

    @Test
    public void scaleAddAndNegate() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(2.0, 3.0, 4.0, 5.0);
        assertArrayEquals(new double[] {4.0, 7.0, 10.0, 13.0}, vector.scaleAdd(2.0, add).toArray(), 0.0);
        assertArrayEquals(new double[] {-1.0, -2.0, -3.0, -4.0}, vector.negate().toArray(), 0.0);
    }

    @Test
    public void normalizeAndZeroNormalize() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 2.0, 4.0);
        assertArrayEquals(new double[] {1.0 / 5.0, 2.0 / 5.0, 2.0 / 5.0, 4.0 / 5.0}, vector.normalize().toArray(), 1.0e-12);
        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, new CCVector4().normalize().toArray(), 0.0);
    }

    @Test
    public void interpolation() {
        CCVector4 begin = new CCVector4(0.0, 2.0, 4.0, 6.0);
        CCVector4 end = new CCVector4(10.0, 12.0, 14.0, 16.0);
        assertArrayEquals(new double[] {5.0, 7.0, 9.0, 11.0}, CCVector4.lerp(begin, end, 0.5).toArray(), 0.0);
    }

    @Test
    public void lengthsDistancesAndDotProduct() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 2.0, 4.0);
        assertEquals(25.0, vector.lengthSquared(), 0.0);
        assertEquals(5.0, vector.length(), 0.0);
        assertEquals(4.0, vector.distanceSquared(1.0, 2.0, 2.0, 2.0), 0.0);
        assertEquals(2.0, vector.distance(1.0, 2.0, 2.0, 2.0), 0.0);
        assertEquals(30.0, vector.dot(2.0, 3.0, 4.0, 5.0), 0.0);
    }

    @Test
    public void validity() {
        assertTrue(CCVector4.isValid(new CCVector4(1.0, 2.0, 3.0, 4.0)));
        assertFalse(CCVector4.isValid(null));
        assertFalse(CCVector4.isValid(new CCVector4(Double.NaN, 0.0, 0.0, 0.0)));
        assertFalse(CCVector4.isValid(new CCVector4(0.0, 0.0, Double.POSITIVE_INFINITY, 0.0)));
    }

    @Test
    public void equalityHashAndClone() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 copy = vector.clone();
        assertNotSame(vector, copy);
        assertTrue(vector.equals(copy));
        assertEquals(vector.hashCode(), copy.hashCode());
        assertFalse(vector.equals(new CCVector4(1.0, 2.0, 3.0, 5.0)));
    }

    @Test
    public void stringRepresentation() {
        assertEquals("com.ardor3d.math.Vector4 [X=1.0, Y=2.0, Z=3.0, W=4.0]",
                new CCVector4(1.0, 2.0, 3.0, 4.0).toString());
    }
}
