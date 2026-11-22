package cc.creativecomputing.math;

import static org.junit.Assert.*;
import org.junit.Test;

public class CCVector4_ArrowTest {

    @Test
    public void testConstructorWithFourDoubles() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testConstructorWithDoubleArray() {
        double[] values = {1.0, 2.0, 3.0, 4.0};
        CCVector4 vector = new CCVector4(values);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testConstructorWithDoubleArrayShorter() {
        double[] values = {1.0, 2.0};
        CCVector4 vector = new CCVector4(values);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(0.0, vector.z, 0.0);
        assertEquals(0.0, vector.w, 0.0);
    }

    @Test
    public void testConstructorWithVector4() {
        CCVector4 source = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector = new CCVector4(source);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testConstructorWithVector3AndW() {
        CCVector3 source = new CCVector3(1.0, 2.0, 3.0);
        CCVector4 vector = new CCVector4(source, 4.0);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testConstructorWithVector2() {
        CCVector2 source = new CCVector2(1.0, 2.0);
        CCVector4 vector = new CCVector4(source);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(0.0, vector.z, 0.0);
        assertEquals(0.0, vector.w, 0.0);
    }

    @Test
    public void testDefaultConstructor() {
        CCVector4 vector = new CCVector4();
        assertEquals(0.0, vector.x, 0.0);
        assertEquals(0.0, vector.y, 0.0);
        assertEquals(0.0, vector.z, 0.0);
        assertEquals(0.0, vector.w, 0.0);
    }

    @Test
    public void testGetValue() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.0, vector.getValue(0), 0.0);
        assertEquals(2.0, vector.getValue(1), 0.0);
        assertEquals(3.0, vector.getValue(2), 0.0);
        assertEquals(4.0, vector.getValue(3), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueInvalidIndex() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        vector.getValue(4);
    }

    @Test
    public void testSetValue() {
        CCVector4 vector = new CCVector4();
        vector.setValue(0, 1.0);
        vector.setValue(1, 2.0);
        vector.setValue(2, 3.0);
        vector.setValue(3, 4.0);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueInvalidIndex() {
        CCVector4 vector = new CCVector4();
        vector.setValue(4, 1.0);
    }

    @Test
    public void testToArray() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double[] result = vector.toArray();
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals(1.0, result[0], 0.0);
        assertEquals(2.0, result[1], 0.0);
        assertEquals(3.0, result[2], 0.0);
        assertEquals(4.0, result[3], 0.0);
    }

    @Test
    public void testToArrayWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double[] store = new double[4];
        double[] result = vector.toArray(store);
        assertSame(store, result);
        assertEquals(1.0, result[0], 0.0);
        assertEquals(2.0, result[1], 0.0);
        assertEquals(3.0, result[2], 0.0);
        assertEquals(4.0, result[3], 0.0);
    }

    @Test
    public void testSet() {
        CCVector4 vector = new CCVector4();
        CCVector4 result = vector.set(1.0, 2.0, 3.0, 4.0);
        assertSame(vector, result);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testSetFromVector4() {
        CCVector4 source = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector = new CCVector4();
        CCVector4 result = vector.set(source);
        assertSame(vector, result);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testZero() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.zero();
        assertSame(vector, result);
        assertEquals(0.0, vector.x, 0.0);
        assertEquals(0.0, vector.y, 0.0);
        assertEquals(0.0, vector.z, 0.0);
        assertEquals(0.0, vector.w, 0.0);
    }

    @Test
    public void testAddWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.add(1.0, 1.0, 1.0, 1.0, new CCVector4());
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testAddWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.add(1.0, 1.0, 1.0, 1.0);
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testAddLocal() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.addLocal(1.0, 1.0, 1.0, 1.0);
        assertSame(vector, result);
        assertEquals(2.0, vector.x, 0.0);
        assertEquals(3.0, vector.y, 0.0);
        assertEquals(4.0, vector.z, 0.0);
        assertEquals(5.0, vector.w, 0.0);
    }

    @Test
    public void testAddFromVector4WithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.add(source, new CCVector4());
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testAddFromVector4WithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.add(source);
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testAddLocalFromVector4() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.addLocal(source);
        assertSame(vector, result);
        assertEquals(2.0, vector.x, 0.0);
        assertEquals(3.0, vector.y, 0.0);
        assertEquals(4.0, vector.z, 0.0);
        assertEquals(5.0, vector.w, 0.0);
    }

    @Test
    public void testSubtractWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.subtract(1.0, 1.0, 1.0, 1.0, new CCVector4());
        assertEquals(1.0, result.x, 0.0);
        assertEquals(2.0, result.y, 0.0);
        assertEquals(3.0, result.z, 0.0);
        assertEquals(4.0, result.w, 0.0);
    }

    @Test
    public void testSubtractWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.subtract(1.0, 1.0, 1.0, 1.0);
        assertEquals(1.0, result.x, 0.0);
        assertEquals(2.0, result.y, 0.0);
        assertEquals(3.0, result.z, 0.0);
        assertEquals(4.0, result.w, 0.0);
    }

    @Test
    public void testSubtractLocal() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.subtractLocal(1.0, 1.0, 1.0, 1.0);
        assertSame(vector, result);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testSubtractFromVector4WithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.subtract(source, new CCVector4());
        assertEquals(1.0, result.x, 0.0);
        assertEquals(2.0, result.y, 0.0);
        assertEquals(3.0, result.z, 0.0);
        assertEquals(4.0, result.w, 0.0);
    }

    @Test
    public void testSubtractFromVector4WithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.subtract(source);
        assertEquals(1.0, result.x, 0.0);
        assertEquals(2.0, result.y, 0.0);
        assertEquals(3.0, result.z, 0.0);
        assertEquals(4.0, result.w, 0.0);
    }

    @Test
    public void testSubtractLocalFromVector4() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.subtractLocal(source);
        assertSame(vector, result);
        assertEquals(1.0, vector.x, 0.0);
        assertEquals(2.0, vector.y, 0.0);
        assertEquals(3.0, vector.z, 0.0);
        assertEquals(4.0, vector.w, 0.0);
    }

    @Test
    public void testMultiplyWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0, 2.0, 2.0, 2.0, new CCVector4());
        assertEquals(4.0, result.x, 0.0);
        assertEquals(6.0, result.y, 0.0);
        assertEquals(8.0, result.z, 0.0);
        assertEquals(10.0, result.w, 0.0);
    }

    @Test
    public void testMultiplyWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0, 2.0, 2.0, 2.0);
        assertEquals(4.0, result.x, 0.0);
        assertEquals(6.0, result.y, 0.0);
        assertEquals(8.0, result.z, 0.0);
        assertEquals(10.0, result.w, 0.0);
    }

    @Test
    public void testMultiplyLocal() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiplyLocal(2.0, 2.0, 2.0, 2.0);
        assertSame(vector, result);
        assertEquals(4.0, vector.x, 0.0);
        assertEquals(6.0, vector.y, 0.0);
        assertEquals(8.0, vector.z, 0.0);
        assertEquals(10.0, vector.w, 0.0);
    }

    @Test
    public void testMultiplyScalarWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0, new CCVector4());
        assertEquals(4.0, result.x, 0.0);
        assertEquals(6.0, result.y, 0.0);
        assertEquals(8.0, result.z, 0.0);
        assertEquals(10.0, result.w, 0.0);
    }

    @Test
    public void testMultiplyScalarWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0);
        assertEquals(4.0, result.x, 0.0);
        assertEquals(6.0, result.y, 0.0);
        assertEquals(8.0, result.z, 0.0);
        assertEquals(10.0, result.w, 0.0);
    }

    @Test
    public void testMultiplyLocalScalar() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiplyLocal(2.0);
        assertSame(vector, result);
        assertEquals(4.0, vector.x, 0.0);
        assertEquals(6.0, vector.y, 0.0);
        assertEquals(8.0, vector.z, 0.0);
        assertEquals(10.0, vector.w, 0.0);
    }

    @Test
    public void testMultiplyVector4WithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.multiply(scale, new CCVector4());
        assertEquals(4.0, result.x, 0.0);
        assertEquals(6.0, result.y, 0.0);
        assertEquals(8.0, result.z, 0.0);
        assertEquals(10.0, result.w, 0.0);
    }

    @Test
    public void testMultiplyVector4WithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.multiply(scale);
        assertEquals(4.0, result.x, 0.0);
        assertEquals(6.0, result.y, 0.0);
        assertEquals(8.0, result.z, 0.0);
        assertEquals(10.0, result.w, 0.0);
    }

    @Test
    public void testMultiplyLocalVector4() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.multiplyLocal(scale);
        assertSame(vector, result);
        assertEquals(4.0, vector.x, 0.0);
        assertEquals(6.0, vector.y, 0.0);
        assertEquals(8.0, vector.z, 0.0);
        assertEquals(10.0, vector.w, 0.0);
    }

    @Test
    public void testDivideWithStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0, 2.0, 2.0, 2.0, new CCVector4());
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testDivideWithoutStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0, 2.0, 2.0, 2.0);
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testDivideLocal() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divideLocal(2.0, 2.0, 2.0, 2.0);
        assertSame(vector, result);
        assertEquals(2.0, vector.x, 0.0);
        assertEquals(3.0, vector.y, 0.0);
        assertEquals(4.0, vector.z, 0.0);
        assertEquals(5.0, vector.w, 0.0);
    }

    @Test
    public void testDivideScalarWithStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0, new CCVector4());
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testDivideScalarWithoutStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0);
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testDivideLocalScalar() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divideLocal(2.0);
        assertSame(vector, result);
        assertEquals(2.0, vector.x, 0.0);
        assertEquals(3.0, vector.y, 0.0);
        assertEquals(4.0, vector.z, 0.0);
        assertEquals(5.0, vector.w, 0.0);
    }

    @Test
    public void testDivideVector4WithStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.divide(scale, new CCVector4());
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testDivideVector4WithoutStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.divide(scale);
        assertEquals(2.0, result.x, 0.0);
        assertEquals(3.0, result.y, 0.0);
        assertEquals(4.0, result.z, 0.0);
        assertEquals(5.0, result.w, 0.0);
    }

    @Test
    public void testDivideLocalVector4() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.divideLocal(scale);
        assertSame(vector, result);
        assertEquals(2.0, vector.x, 0.0);
        assertEquals(3.0, vector.y, 0.0);
        assertEquals(4.0, vector.z, 0.0);
        assertEquals(5.0, vector.w, 0.0);
    }

    @Test
    public void testScaleAddWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.scaleAdd(2.0, add, new CCVector4());
        assertEquals(3.0, result.x, 0.0);
        assertEquals(5.0, result.y, 0.0);
        assertEquals(7.0, result.z, 0.0);
        assertEquals(9.0, result.w, 0.0);
    }

    @Test
    public void testScaleAddWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.scaleAdd(2.0, add);
        assertEquals(3.0, result.x, 0.0);
        assertEquals(5.0, result.y, 0.0);
        assertEquals(7.0, result.z, 0.0);
        assertEquals(9.0, result.w, 0.0);
    }

    @Test
    public void testScaleAddLocal() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.scaleAddLocal(2.0, add);
        assertSame(vector, result);
        assertEquals(3.0, vector.x, 0.0);
        assertEquals(5.0, vector.y, 0.0);
        assertEquals(7.0, vector.z, 0.0);
        assertEquals(9.0, vector.w, 0.0);
    }

    @Test
    public void testNegateWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.negate(new CCVector4());
        assertEquals(-1.0, result.x, 0.0);
        assertEquals(-2.0, result.y, 0.0);
        assertEquals(-3.0, result.z, 0.0);
        assertEquals(-4.0, result.w, 0.0);
    }

    @Test
    public void testNegateWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.negate();
        assertEquals(-1.0, result.x, 0.0);
        assertEquals(-2.0, result.y, 0.0);
        assertEquals(-3.0, result.z, 0.0);
        assertEquals(-4.0, result.w, 0.0);
    }

    @Test
    public void testNegateLocal() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.negateLocal();
        assertSame(vector, result);
        assertEquals(-1.0, vector.x, 0.0);
        assertEquals(-2.0, vector.y, 0.0);
        assertEquals(-3.0, vector.z, 0.0);
        assertEquals(-4.0, vector.w, 0.0);
    }

    @Test
    public void testNormalizeWithStore() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        CCVector4 result = vector.normalize(new CCVector4());
        assertEquals(0.6, result.x, 0.001);
        assertEquals(0.8, result.y, 0.001);
        assertEquals(0.0, result.z, 0.001);
        assertEquals(0.0, result.w, 0.001);
    }

    @Test
    public void testNormalizeWithoutStore() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        CCVector4 result = vector.normalize();
        assertEquals(0.6, result.x, 0.001);
        assertEquals(0.8, result.y, 0.001);
        assertEquals(0.0, result.z, 0.001);
        assertEquals(0.0, result.w, 0.001);
    }

    @Test
    public void testNormalizeLocal() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        CCVector4 result = vector.normalizeLocal();
        assertSame(vector, result);
        assertEquals(0.6, vector.x, 0.001);
        assertEquals(0.8, vector.y, 0.001);
        assertEquals(0.0, vector.z, 0.001);
        assertEquals(0.0, vector.w, 0.001);
    }

    @Test
    public void testLerpStaticWithStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = CCVector4.lerp(begin, end, 0.5, new CCVector4());
        assertEquals(0.5, result.x, 0.001);
        assertEquals(0.5, result.y, 0.001);
        assertEquals(0.5, result.z, 0.001);
        assertEquals(0.5, result.w, 0.001);
    }

    @Test
    public void testLerpStaticWithoutStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = CCVector4.lerp(begin, end, 0.5);
        assertEquals(0.5, result.x, 0.001);
        assertEquals(0.5, result.y, 0.001);
        assertEquals(0.5, result.z, 0.001);
        assertEquals(0.5, result.w, 0.001);
    }

    @Test
    public void testLerpWithStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 vector = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 result = vector.lerp(end, 0.5, new CCVector4());
        assertEquals(0.5, result.x, 0.001);
        assertEquals(0.5, result.y, 0.001);
        assertEquals(0.5, result.z, 0.001);
        assertEquals(0.5, result.w, 0.001);
    }

    @Test
    public void testLerpWithoutStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 vector = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 result = vector.lerp(end, 0.5);
        assertEquals(0.5, result.x, 0.001);
        assertEquals(0.5, result.y, 0.001);
        assertEquals(0.5, result.z, 0.001);
        assertEquals(0.5, result.w, 0.001);
    }

    @Test
    public void testLerpLocal() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 vector = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 result = vector.lerpLocal(end, 0.5);
        assertSame(vector, result);
        assertEquals(0.5, vector.x, 0.001);
        assertEquals(0.5, vector.y, 0.001);
        assertEquals(0.5, vector.z, 0.001);
        assertEquals(0.5, vector.w, 0.001);
    }

    @Test
    public void testLerpLocalWithThreeVectors() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 vector = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 result = vector.lerpLocal(begin, end, 0.5);
        assertSame(vector, result);
        assertEquals(0.5, vector.x, 0.001);
        assertEquals(0.5, vector.y, 0.001);
        assertEquals(0.5, vector.z, 0.001);
        assertEquals(0.5, vector.w, 0.001);
    }

    @Test
    public void testLength() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        assertEquals(5.0, vector.length(), 0.001);
    }

    @Test
    public void testLengthSquared() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        assertEquals(25.0, vector.lengthSquared(), 0.001);
    }

    @Test
    public void testDistanceSquared() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        double result = vector.distanceSquared(2.0, 2.0, 2.0, 2.0);
        assertEquals(4.0, result, 0.001);
    }

    @Test
    public void testDistanceSquaredFromVector() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 destination = new CCVector4(2.0, 2.0, 2.0, 2.0);
        double result = vector.distanceSquared(destination);
        assertEquals(4.0, result, 0.001);
    }

    @Test
    public void testDistance() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        double result = vector.distance(2.0, 2.0, 2.0, 2.0);
        assertEquals(2.0, result, 0.001);
    }

    @Test
    public void testDistanceFromVector() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 destination = new CCVector4(2.0, 2.0, 2.0, 2.0);
        double result = vector.distance(destination);
        assertEquals(2.0, result, 0.001);
    }

    @Test
    public void testDot() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double result = vector.dot(1.0, 1.0, 1.0, 1.0);
        assertEquals(10.0, result, 0.001);
    }

    @Test
    public void testDotFromVector() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 other = new CCVector4(1.0, 1.0, 1.0, 1.0);
        double result = vector.dot(other);
        assertEquals(10.0, result, 0.001);
    }

    @Test
    public void testIsValidNull() {
        assertFalse(CCVector4.isValid(null));
    }

    @Test
    public void testIsValidValidVector() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertTrue(CCVector4.isValid(vector));
    }

    @Test
    public void testIsValidNaN() {
        CCVector4 vector = new CCVector4(Double.NaN, 2.0, 3.0, 4.0);
        assertFalse(CCVector4.isValid(vector));
    }

    @Test
    public void testIsValidInfinite() {
        CCVector4 vector = new CCVector4(Double.POSITIVE_INFINITY, 2.0, 3.0, 4.0);
        assertFalse(CCVector4.isValid(vector));
    }

    @Test
    public void testToString() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        String result = vector.toString();
        assertTrue(result.contains("X=1.0"));
        assertTrue(result.contains("Y=2.0"));
        assertTrue(result.contains("Z=3.0"));
        assertTrue(result.contains("W=4.0"));
    }

    @Test
    public void testHashCode() {
        CCVector4 vector1 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector2 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(vector1.hashCode(), vector2.hashCode());
    }

    @Test
    public void testEquals() {
        CCVector4 vector1 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector2 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertTrue(vector1.equals(vector2));
    }

    @Test
    public void testNotEquals() {
        CCVector4 vector1 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector2 = new CCVector4(1.0, 2.0, 3.0, 5.0);
        assertFalse(vector1.equals(vector2));
    }

    @Test
    public void testClone() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 cloned = vector.clone();
        assertEquals(vector.x, cloned.x, 0.0);
        assertEquals(vector.y, cloned.y, 0.0);
        assertEquals(vector.z, cloned.z, 0.0);
        assertEquals(vector.w, cloned.w, 0.0);
        assertNotSame(vector, cloned);
    }

    @Test
    public void testXYZ() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector3 result = vector.xyz();
        assertEquals(1.0, result.x, 0.0);
        assertEquals(2.0, result.y, 0.0);
        assertEquals(3.0, result.z, 0.0);
    }
}
