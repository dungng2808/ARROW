package cc.creativecomputing.math;

import junit.framework.TestCase;

public class CCVector4_ArrowTest extends TestCase {

    public void testConstructorWithFourParameters() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testConstructorWithDoubleArray() {
        double[] values = {1.0, 2.0, 3.0, 4.0};
        CCVector4 vector = new CCVector4(values);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testConstructorWithDoubleArrayShorter() {
        double[] values = {1.0, 2.0};
        CCVector4 vector = new CCVector4(values);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(0.0, vector.z);
        assertEquals(0.0, vector.w);
    }

    public void testConstructorWithVector4() {
        CCVector4 source = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector = new CCVector4(source);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testConstructorWithVector3AndW() {
        CCVector3 source = new CCVector3(1.0, 2.0, 3.0);
        CCVector4 vector = new CCVector4(source, 4.0);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testConstructorWithVector2() {
        CCVector2 source = new CCVector2(1.0, 2.0);
        CCVector4 vector = new CCVector4(source);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(0.0, vector.z);
        assertEquals(0.0, vector.w);
    }

    public void testDefaultConstructor() {
        CCVector4 vector = new CCVector4();
        assertEquals(0.0, vector.x);
        assertEquals(0.0, vector.y);
        assertEquals(0.0, vector.z);
        assertEquals(0.0, vector.w);
    }

    public void testGetValue() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.0, vector.getValue(0));
        assertEquals(2.0, vector.getValue(1));
        assertEquals(3.0, vector.getValue(2));
        assertEquals(4.0, vector.getValue(3));
    }

    public void testGetValueInvalidIndex() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        try {
            vector.getValue(4);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testSetValue() {
        CCVector4 vector = new CCVector4();
        vector.setValue(0, 1.0);
        vector.setValue(1, 2.0);
        vector.setValue(2, 3.0);
        vector.setValue(3, 4.0);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testSetValueInvalidIndex() {
        CCVector4 vector = new CCVector4();
        try {
            vector.setValue(4, 1.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testToArray() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double[] result = vector.toArray();
        assertEquals(1.0, result[0]);
        assertEquals(2.0, result[1]);
        assertEquals(3.0, result[2]);
        assertEquals(4.0, result[3]);
    }

    public void testToArrayWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double[] store = new double[4];
        double[] result = vector.toArray(store);
        assertSame(store, result);
        assertEquals(1.0, result[0]);
        assertEquals(2.0, result[1]);
        assertEquals(3.0, result[2]);
        assertEquals(4.0, result[3]);
    }

    public void testSet() {
        CCVector4 vector = new CCVector4();
        CCVector4 result = vector.set(1.0, 2.0, 3.0, 4.0);
        assertSame(vector, result);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testSetFromVector() {
        CCVector4 source = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector = new CCVector4();
        CCVector4 result = vector.set(source);
        assertSame(vector, result);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testZero() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.zero();
        assertSame(vector, result);
        assertEquals(0.0, vector.x);
        assertEquals(0.0, vector.y);
        assertEquals(0.0, vector.z);
        assertEquals(0.0, vector.w);
    }

    public void testAddWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.add(1.0, 1.0, 1.0, 1.0, new CCVector4());
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testAddWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.add(1.0, 1.0, 1.0, 1.0);
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testAddLocal() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.addLocal(1.0, 1.0, 1.0, 1.0);
        assertSame(vector, result);
        assertEquals(2.0, vector.x);
        assertEquals(3.0, vector.y);
        assertEquals(4.0, vector.z);
        assertEquals(5.0, vector.w);
    }

    public void testAddFromVectorWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.add(source, new CCVector4());
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testAddFromVectorWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.add(source);
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testAddLocalFromVector() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.addLocal(source);
        assertSame(vector, result);
        assertEquals(2.0, vector.x);
        assertEquals(3.0, vector.y);
        assertEquals(4.0, vector.z);
        assertEquals(5.0, vector.w);
    }

    public void testSubtractWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.subtract(1.0, 1.0, 1.0, 1.0, new CCVector4());
        assertEquals(1.0, result.x);
        assertEquals(2.0, result.y);
        assertEquals(3.0, result.z);
        assertEquals(4.0, result.w);
    }

    public void testSubtractWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.subtract(1.0, 1.0, 1.0, 1.0);
        assertEquals(1.0, result.x);
        assertEquals(2.0, result.y);
        assertEquals(3.0, result.z);
        assertEquals(4.0, result.w);
    }

    public void testSubtractLocal() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.subtractLocal(1.0, 1.0, 1.0, 1.0);
        assertSame(vector, result);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testSubtractFromVectorWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.subtract(source, new CCVector4());
        assertEquals(1.0, result.x);
        assertEquals(2.0, result.y);
        assertEquals(3.0, result.z);
        assertEquals(4.0, result.w);
    }

    public void testSubtractFromVectorWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.subtract(source);
        assertEquals(1.0, result.x);
        assertEquals(2.0, result.y);
        assertEquals(3.0, result.z);
        assertEquals(4.0, result.w);
    }

    public void testSubtractLocalFromVector() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 source = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.subtractLocal(source);
        assertSame(vector, result);
        assertEquals(1.0, vector.x);
        assertEquals(2.0, vector.y);
        assertEquals(3.0, vector.z);
        assertEquals(4.0, vector.w);
    }

    public void testMultiplyWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0, 2.0, 2.0, 2.0, new CCVector4());
        assertEquals(4.0, result.x);
        assertEquals(6.0, result.y);
        assertEquals(8.0, result.z);
        assertEquals(10.0, result.w);
    }

    public void testMultiplyWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0, 2.0, 2.0, 2.0);
        assertEquals(4.0, result.x);
        assertEquals(6.0, result.y);
        assertEquals(8.0, result.z);
        assertEquals(10.0, result.w);
    }

    public void testMultiplyLocal() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiplyLocal(2.0, 2.0, 2.0, 2.0);
        assertSame(vector, result);
        assertEquals(4.0, vector.x);
        assertEquals(6.0, vector.y);
        assertEquals(8.0, vector.z);
        assertEquals(10.0, vector.w);
    }

    public void testMultiplyScalarWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0, new CCVector4());
        assertEquals(4.0, result.x);
        assertEquals(6.0, result.y);
        assertEquals(8.0, result.z);
        assertEquals(10.0, result.w);
    }

    public void testMultiplyScalarWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiply(2.0);
        assertEquals(4.0, result.x);
        assertEquals(6.0, result.y);
        assertEquals(8.0, result.z);
        assertEquals(10.0, result.w);
    }

    public void testMultiplyScalarLocal() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 result = vector.multiplyLocal(2.0);
        assertSame(vector, result);
        assertEquals(4.0, vector.x);
        assertEquals(6.0, vector.y);
        assertEquals(8.0, vector.z);
        assertEquals(10.0, vector.w);
    }

    public void testMultiplyVectorWithStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.multiply(scale, new CCVector4());
        assertEquals(4.0, result.x);
        assertEquals(6.0, result.y);
        assertEquals(8.0, result.z);
        assertEquals(10.0, result.w);
    }

    public void testMultiplyVectorWithoutStore() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.multiply(scale);
        assertEquals(4.0, result.x);
        assertEquals(6.0, result.y);
        assertEquals(8.0, result.z);
        assertEquals(10.0, result.w);
    }

    public void testMultiplyVectorLocal() {
        CCVector4 vector = new CCVector4(2.0, 3.0, 4.0, 5.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.multiplyLocal(scale);
        assertSame(vector, result);
        assertEquals(4.0, vector.x);
        assertEquals(6.0, vector.y);
        assertEquals(8.0, vector.z);
        assertEquals(10.0, vector.w);
    }

    public void testDivideWithStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0, 2.0, 2.0, 2.0, new CCVector4());
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testDivideWithoutStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0, 2.0, 2.0, 2.0);
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testDivideLocal() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divideLocal(2.0, 2.0, 2.0, 2.0);
        assertSame(vector, result);
        assertEquals(2.0, vector.x);
        assertEquals(3.0, vector.y);
        assertEquals(4.0, vector.z);
        assertEquals(5.0, vector.w);
    }

    public void testDivideScalarWithStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0, new CCVector4());
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testDivideScalarWithoutStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divide(2.0);
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testDivideScalarLocal() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 result = vector.divideLocal(2.0);
        assertSame(vector, result);
        assertEquals(2.0, vector.x);
        assertEquals(3.0, vector.y);
        assertEquals(4.0, vector.z);
        assertEquals(5.0, vector.w);
    }

    public void testDivideVectorWithStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.divide(scale, new CCVector4());
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testDivideVectorWithoutStore() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.divide(scale);
        assertEquals(2.0, result.x);
        assertEquals(3.0, result.y);
        assertEquals(4.0, result.z);
        assertEquals(5.0, result.w);
    }

    public void testDivideVectorLocal() {
        CCVector4 vector = new CCVector4(4.0, 6.0, 8.0, 10.0);
        CCVector4 scale = new CCVector4(2.0, 2.0, 2.0, 2.0);
        CCVector4 result = vector.divideLocal(scale);
        assertSame(vector, result);
        assertEquals(2.0, vector.x);
        assertEquals(3.0, vector.y);
        assertEquals(4.0, vector.z);
        assertEquals(5.0, vector.w);
    }

    public void testScaleAddWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.scaleAdd(2.0, add, new CCVector4());
        assertEquals(3.0, result.x);
        assertEquals(5.0, result.y);
        assertEquals(7.0, result.z);
        assertEquals(9.0, result.w);
    }

    public void testScaleAddWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.scaleAdd(2.0, add);
        assertEquals(3.0, result.x);
        assertEquals(5.0, result.y);
        assertEquals(7.0, result.z);
        assertEquals(9.0, result.w);
    }

    public void testScaleAddLocal() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 add = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = vector.scaleAddLocal(2.0, add);
        assertSame(vector, result);
        assertEquals(3.0, vector.x);
        assertEquals(5.0, vector.y);
        assertEquals(7.0, vector.z);
        assertEquals(9.0, vector.w);
    }

    public void testNegateWithStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.negate(new CCVector4());
        assertEquals(-1.0, result.x);
        assertEquals(-2.0, result.y);
        assertEquals(-3.0, result.z);
        assertEquals(-4.0, result.w);
    }

    public void testNegateWithoutStore() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.negate();
        assertEquals(-1.0, result.x);
        assertEquals(-2.0, result.y);
        assertEquals(-3.0, result.z);
        assertEquals(-4.0, result.w);
    }

    public void testNegateLocal() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 result = vector.negateLocal();
        assertSame(vector, result);
        assertEquals(-1.0, vector.x);
        assertEquals(-2.0, vector.y);
        assertEquals(-3.0, vector.z);
        assertEquals(-4.0, vector.w);
    }

    public void testNormalizeWithStore() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        CCVector4 result = vector.normalize(new CCVector4());
        assertEquals(0.6, result.x, 0.0001);
        assertEquals(0.8, result.y, 0.0001);
        assertEquals(0.0, result.z);
        assertEquals(0.0, result.w);
    }

    public void testNormalizeWithoutStore() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        CCVector4 result = vector.normalize();
        assertEquals(0.6, result.x, 0.0001);
        assertEquals(0.8, result.y, 0.0001);
        assertEquals(0.0, result.z);
        assertEquals(0.0, result.w);
    }

    public void testNormalizeLocal() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        CCVector4 result = vector.normalizeLocal();
        assertSame(vector, result);
        assertEquals(0.6, vector.x, 0.0001);
        assertEquals(0.8, vector.y, 0.0001);
        assertEquals(0.0, vector.z);
        assertEquals(0.0, vector.w);
    }

    public void testLerpStaticWithStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = CCVector4.lerp(begin, end, 0.5, new CCVector4());
        assertEquals(0.5, result.x);
        assertEquals(0.5, result.y);
        assertEquals(0.5, result.z);
        assertEquals(0.5, result.w);
    }

    public void testLerpStaticWithoutStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = CCVector4.lerp(begin, end, 0.5);
        assertEquals(0.5, result.x);
        assertEquals(0.5, result.y);
        assertEquals(0.5, result.z);
        assertEquals(0.5, result.w);
    }

    public void testLerpWithStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = begin.lerp(end, 0.5, new CCVector4());
        assertEquals(0.5, result.x);
        assertEquals(0.5, result.y);
        assertEquals(0.5, result.z);
        assertEquals(0.5, result.w);
    }

    public void testLerpWithoutStore() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = begin.lerp(end, 0.5);
        assertEquals(0.5, result.x);
        assertEquals(0.5, result.y);
        assertEquals(0.5, result.z);
        assertEquals(0.5, result.w);
    }

    public void testLerpLocal() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = begin.lerpLocal(end, 0.5);
        assertSame(begin, result);
        assertEquals(0.5, begin.x);
        assertEquals(0.5, begin.y);
        assertEquals(0.5, begin.z);
        assertEquals(0.5, begin.w);
    }

    public void testLerpLocalStatic() {
        CCVector4 begin = new CCVector4(0.0, 0.0, 0.0, 0.0);
        CCVector4 end = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 result = begin.lerpLocal(begin, end, 0.5);
        assertSame(begin, result);
        assertEquals(0.5, begin.x);
        assertEquals(0.5, begin.y);
        assertEquals(0.5, begin.z);
        assertEquals(0.5, begin.w);
    }

    public void testLength() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        assertEquals(5.0, vector.length(), 0.0001);
    }

    public void testLengthSquared() {
        CCVector4 vector = new CCVector4(3.0, 4.0, 0.0, 0.0);
        assertEquals(25.0, vector.lengthSquared());
    }

    public void testDistanceSquared() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        double distance = vector.distanceSquared(2.0, 2.0, 2.0, 2.0);
        assertEquals(4.0, distance);
    }

    public void testDistanceSquaredFromVector() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 destination = new CCVector4(2.0, 2.0, 2.0, 2.0);
        double distance = vector.distanceSquared(destination);
        assertEquals(4.0, distance);
    }

    public void testDistance() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        double distance = vector.distance(2.0, 2.0, 2.0, 2.0);
        assertEquals(2.0, distance, 0.0001);
    }

    public void testDistanceFromVector() {
        CCVector4 vector = new CCVector4(1.0, 1.0, 1.0, 1.0);
        CCVector4 destination = new CCVector4(2.0, 2.0, 2.0, 2.0);
        double distance = vector.distance(destination);
        assertEquals(2.0, distance, 0.0001);
    }

    public void testDot() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        double dot = vector.dot(2.0, 2.0, 2.0, 2.0);
        assertEquals(20.0, dot);
    }

    public void testDotFromVector() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 other = new CCVector4(2.0, 2.0, 2.0, 2.0);
        double dot = vector.dot(other);
        assertEquals(20.0, dot);
    }

    public void testIsValidValid() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertTrue(CCVector4.isValid(vector));
    }

    public void testIsValidNull() {
        assertFalse(CCVector4.isValid(null));
    }

    public void testIsValidNaN() {
        CCVector4 vector = new CCVector4(Double.NaN, 2.0, 3.0, 4.0);
        assertFalse(CCVector4.isValid(vector));
    }

    public void testIsValidInfinite() {
        CCVector4 vector = new CCVector4(Double.POSITIVE_INFINITY, 2.0, 3.0, 4.0);
        assertFalse(CCVector4.isValid(vector));
    }

    public void testToString() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        String result = vector.toString();
        assertTrue(result.contains("X=1.0"));
        assertTrue(result.contains("Y=2.0"));
        assertTrue(result.contains("Z=3.0"));
        assertTrue(result.contains("W=4.0"));
    }

    public void testHashCode() {
        CCVector4 vector1 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector2 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertEquals(vector1.hashCode(), vector2.hashCode());
    }

    public void testEquals() {
        CCVector4 vector1 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector2 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        assertTrue(vector1.equals(vector2));
    }

    public void testNotEquals() {
        CCVector4 vector1 = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 vector2 = new CCVector4(1.0, 2.0, 3.0, 5.0);
        assertFalse(vector1.equals(vector2));
    }

    public void testClone() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector4 cloned = vector.clone();
        assertEquals(vector.x, cloned.x);
        assertEquals(vector.y, cloned.y);
        assertEquals(vector.z, cloned.z);
        assertEquals(vector.w, cloned.w);
        assertNotSame(vector, cloned);
    }

    public void testXYZ() {
        CCVector4 vector = new CCVector4(1.0, 2.0, 3.0, 4.0);
        CCVector3 xyz = vector.xyz();
        assertEquals(1.0, xyz.x);
        assertEquals(2.0, xyz.y);
        assertEquals(3.0, xyz.z);
    }
}
