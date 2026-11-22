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

        CCVector4 zero = new CCVector4();
        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, zero.toArray(), 0.0);
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
        double[] store = new double[4];
        assertSame(store, vector.toArray(store));
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, store, 0.0);
    }

    @Test
    public void setAndZeroReturnThis() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertSame(vector, vector.set(5.0, 6.0, 7.0, 8.0));
        assertArrayEquals(new double[] {5.0, 6.0, 7.0, 8.0}, vector.toArray(), 0.0);
        assertSame(vector, vector.zero());
        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, vector.toArray(), 0.0);
    }

    @Test
    public void arithmeticOperations() {
        CCVector4 vector = new CCVector4(8.0, 12.0, 18.0, 24.0);
        assertArrayEquals(new double[] {10.0, 15.0, 22.0, 27.0},
                vector.add(2.0, 3.0, 4.0, 3.0).toArray(), 0.0);
        assertArrayEquals(new double[] {6.0, 9.0, 14.0, 21.0},
                vector.subtract(2.0, 3.0, 4.0, 3.0).toArray(), 0.0);
        assertArrayEquals(new double[] {16.0, 36.0, 72.0, 96.0},
                vector.multiply(2.0, 3.0, 4.0, 4.0).toArray(), 0.0);
        assertArrayEquals(new double[] {4.0, 4.0, 6.0, 6.0},
                vector.divide(2.0, 3.0, 3.0, 4.0).toArray(), 0.0);
    }

    @Test
    public void arithmeticCanWriteToStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 store = new CCVector4();
        assertSame(store, vector.add(1.0, 1.0, 1.0, 1.0, store));
        assertArrayEquals(new double[] {2.0, 3.0, 4.0, 5.0}, store.toArray(), 0.0);
    }

    @Test
    public void localArithmeticMutatesAndReturnsThis() {
        CCVector4 vector = new CCVector4(8.0, 12.0, 18.0, 24.0);
        assertSame(vector, vector.addLocal(1.0, 2.0, 3.0, 4.0));
        assertArrayEquals(new double[] {9.0, 14.0, 21.0, 28.0}, vector.toArray(), 0.0);
        assertSame(vector, vector.subtractLocal(1.0, 2.0, 3.0, 4.0));
        assertArrayEquals(new double[] {8.0, 12.0, 18.0, 24.0}, vector.toArray(), 0.0);
        assertSame(vector, vector.multiplyLocal(2.0));
        assertArrayEquals(new double[] {16.0, 24.0, 36.0, 48.0}, vector.toArray(), 0.0);
        assertSame(vector, vector.divideLocal(2.0));
        assertArrayEquals(new double[] {8.0, 12.0, 18.0, 24.0}, vector.toArray(), 0.0);
    }

    @Test
    public void vectorArithmeticAndScaleAdd() {
        CCVector4 vector = new CCVector4(2.0, 4.0, 6.0, 8.0);
        CCVector4 other = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertArrayEquals(new double[] {3.0, 6.0, 9.0, 12.0}, vector.add(other).toArray(), 0.0);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.subtract(other).toArray(), 0.0);
        assertArrayEquals(new double[] {2.0, 8.0, 18.0, 32.0}, vector.multiply(other).toArray(), 0.0);
        assertArrayEquals(new double[] {2.0, 2.0, 2.0, 2.0}, vector.divide(other).toArray(), 0.0);
        assertArrayEquals(new double[] {5.0, 10.0, 15.0, 20.0}, vector.scaleAdd(2.0, other).toArray(), 0.0);
    }

    @Test
    public void negateAndNormalize() {
        CCVector4 vector = new CCVector4(1.0, -2.0, 3.0, -4.0);
        assertArrayEquals(new double[] {-1.0, 2.0, -3.0, 4.0}, vector.negate().toArray(), 0.0);

        CCVector4 normalized = new CCVector4(0.0, 3.0, 0.0, 4.0).normalize();
        assertArrayEquals(new double[] {0.0, 0.6, 0.0, 0.8}, normalized.toArray(), 1e-15);

        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, new CCVector4().normalize().toArray(), 0.0);
    }

    @Test
    public void interpolation() {
        CCVector4 begin = new CCVector4(0.0, 2.0, 4.0, 6.0);
        CCVector4 end = new CCVector4(10.0, 12.0, 14.0, 16.0);
        assertArrayEquals(new double[] {5.0, 7.0, 9.0, 11.0},
                CCVector4.lerp(begin, end, 0.5).toArray(), 0.0);
    }

    @Test
    public void lengthsDistancesAndDotProduct() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 2.0, 4.0);
        assertEquals(25.0, vector.lengthSquared(), 0.0);
        assertEquals(5.0, vector.length(), 0.0);
        assertEquals(25.0, vector.distanceSquared(0.0, 0.0, 0.0, 0.0), 0.0);
        assertEquals(5.0, vector.distance(0.0, 0.0, 0.0, 0.0), 0.0);
        assertEquals(30.0, vector.dot(2.0, 3.0, 4.0, 5.0), 0.0);
    }

    @Test
    public void validityEqualityAndHashCode() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 equalVector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertTrue(CCVector4.isValid(vector));
        assertFalse(CCVector4.isValid(null));
        assertFalse(CCVector4.isValid(new CCVector4(Double.NaN, 0.0, 0.0, 0.0)));
        assertFalse(CCVector4.isValid(new CCVector4(0.0, 0.0, Double.POSITIVE_INFINITY, 0.0)));
        assertTrue(vector.equals(equalVector));
        assertEquals(vector.hashCode(), equalVector.hashCode());
        assertFalse(vector.equals(new CCVector4()));
    }

    @Test
    public void cloneCreatesEqualIndependentVector() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 clone = vector.clone();
        assertNotSame(vector, clone);
        assertTrue(vector.equals(clone));
        clone.x = 9.0;
        assertEquals(1.0, vector.x, 0.0);
    }

    @Test
    public void stringRepresentation() {
        assertEquals("com.ardor3d.math.Vector4 [X=1.0, Y=2.0, Z=3.0, W=4.0]",
                new CCVector4(1.0, 2.0, 3.0, 4.0).toString());
    }
}
