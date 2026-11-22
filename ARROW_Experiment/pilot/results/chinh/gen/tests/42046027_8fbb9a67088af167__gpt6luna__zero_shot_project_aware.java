package cc.creativecomputing.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class CCVector4_ArrowTest {

    @Test
    public void constructorAndArrayConversion() {
        CCVector4 vector = new CCVector4(new double[] {1.0, 2.0, 3.0, 4.0});
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.toArray(), 0.0);
    }

    @Test
    public void arrayConstructorFillsMissingComponentsWithZero() {
        CCVector4 vector = new CCVector4(new double[] {5.0, 6.0});
        assertArrayEquals(new double[] {5.0, 6.0, 0.0, 0.0}, vector.toArray(), 0.0);
    }

    @Test
    public void getAndSetValueByIndex() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        vector.setValue(2, 9.0);
        assertEquals(9.0, vector.getValue(2), 0.0);
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

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void toArrayRejectsShortStore() {
        new CCVector4(1.0, 2.0, 3.0, 4.0).toArray(new double[3]);
    }

    @Test
    public void setAndZero() {
        CCVector4 vector = new CCVector4();
        assertSame(vector, vector.set(1.0, 2.0, 3.0, 4.0));
        assertSame(vector, vector.zero());
        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, vector.toArray(), 0.0);
    }

    @Test
    public void addReturnsExpectedVectorWithoutChangingReceiver() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.add(4.0, 3.0, 2.0, 1.0);
        assertArrayEquals(new double[] {5.0, 5.0, 5.0, 5.0}, result.toArray(), 0.0);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.toArray(), 0.0);
    }

    @Test
    public void addLocalMutatesReceiver() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertSame(vector, vector.addLocal(1.0, 2.0, 3.0, 4.0));
        assertArrayEquals(new double[] {2.0, 4.0, 6.0, 8.0}, vector.toArray(), 0.0);
    }

    @Test
    public void subtractReturnsExpectedVector() {
        CCVector4 result = new CCVector4(8.0, 7.0, 6.0, 5.0).subtract(1.0, 2.0, 3.0, 4.0);
        assertArrayEquals(new double[] {7.0, 5.0, 3.0, 1.0}, result.toArray(), 0.0);
    }

    @Test
    public void multiplyAndDivideByScalar() {
        CCVector4 vector = new CCVector4(2.0, 4.0, 6.0, 8.0);
        assertArrayEquals(new double[] {6.0, 12.0, 18.0, 24.0}, vector.multiply(3.0).toArray(), 0.0);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, vector.divide(2.0).toArray(), 0.0);
    }

    @Test
    public void componentwiseMultiplyAndDivide() {
        CCVector4 vector = new CCVector4(8.0, 12.0, 18.0, 20.0);
        assertArrayEquals(new double[] {16.0, 36.0, 72.0, 100.0}, vector.multiply(2.0, 3.0, 4.0, 5.0).toArray(), 0.0);
        assertArrayEquals(new double[] {4.0, 4.0, 3.0, 2.0}, vector.divide(2.0, 3.0, 6.0, 10.0).toArray(), 0.0);
    }

    @Test
    public void scaleAdd() {
        CCVector4 result = new CCVector4(1.0, 2.0, 3.0, 4.0).scaleAdd(2.0, new CCVector4(5.0, 6.0, 7.0, 8.0));
        assertArrayEquals(new double[] {7.0, 10.0, 13.0, 16.0}, result.toArray(), 0.0);
    }

    @Test
    public void negateAndNormalize() {
        CCVector4 vector = new CCVector4(1.0, -2.0, 2.0, 0.0);
        assertArrayEquals(new double[] {-1.0, 2.0, -2.0, 0.0}, vector.negate().toArray(), 0.0);
        CCVector4 normalized = vector.normalize();
        assertArrayEquals(new double[] {1.0 / 3.0, -2.0 / 3.0, 2.0 / 3.0, 0.0}, normalized.toArray(), 1e-15);
    }

    @Test
    public void normalizeZeroVectorReturnsZero() {
        assertArrayEquals(new double[] {0.0, 0.0, 0.0, 0.0}, new CCVector4().normalize().toArray(), 0.0);
    }

    @Test
    public void lerpInterpolatesComponents() {
        CCVector4 result = CCVector4.lerp(
            new CCVector4(0.0, 2.0, 4.0, 6.0),
            new CCVector4(10.0, 12.0, 14.0, 16.0),
            0.25
        );
        assertArrayEquals(new double[] {2.5, 4.5, 6.5, 8.5}, result.toArray(), 0.0);
    }

    @Test
    public void lengthAndDistances() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 2.0, 4.0);
        assertEquals(25.0, vector.lengthSquared(), 0.0);
        assertEquals(5.0, vector.length(), 0.0);
        assertEquals(4.0, vector.distanceSquared(1.0, 2.0, 2.0, 2.0), 0.0);
        assertEquals(2.0, vector.distance(1.0, 2.0, 2.0, 2.0), 0.0);
    }

    @Test
    public void dotProduct() {
        assertEquals(70.0, new CCVector4(1.0, 2.0, 3.0, 4.0).dot(5.0, 6.0, 7.0, 8.0), 0.0);
    }

    @Test
    public void validityChecksNullFiniteAndNonFiniteVectors() {
        assertFalse(CCVector4.isValid(null));
        assertTrue(CCVector4.isValid(new CCVector4(1.0, 2.0, 3.0, 4.0)));
        assertFalse(CCVector4.isValid(new CCVector4(Double.NaN, 0.0, 0.0, 0.0)));
        assertFalse(CCVector4.isValid(new CCVector4(0.0, 0.0, Double.POSITIVE_INFINITY, 0.0)));
    }

    @Test
    public void equalityHashAndClone() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 copy = vector.clone();
        assertEquals(vector, copy);
        assertEquals(vector.hashCode(), copy.hashCode());
        assertNotSame(vector, copy);
        assertFalse(vector.equals(new CCVector4(1.0, 2.0, 3.0, 5.0)));
    }

    @Test
    public void stringRepresentation() {
        assertEquals("com.ardor3d.math.Vector4 [X=1.0, Y=2.0, Z=3.0, W=4.0]",
            new CCVector4(1.0, 2.0, 3.0, 4.0).toString());
    }
}
