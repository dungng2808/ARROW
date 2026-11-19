package dk.alexandra.fresco.framework.builder.numeric.field;

import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class MersennePrimeModulus_ArrowTest {

    @Test
    public void testConstructor() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(128, 1);
        assertEquals(128, modulus.bitLength);
        assertEquals(BigInteger.valueOf(1), modulus.constant);
        assertEquals(BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE), modulus.precomputedBitMask);
        assertEquals(BigInteger.ONE.shiftLeft(128).subtract(BigInteger.valueOf(1)), modulus.prime);
    }

    @Test
    public void testGetBigInteger() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(64, 3);
        assertEquals(BigInteger.ONE.shiftLeft(64).subtract(BigInteger.valueOf(3)), modulus.getBigInteger());
    }

    @Test
    public void testEquals() {
        MersennePrimeModulus m1 = new MersennePrimeModulus(128, 1);
        MersennePrimeModulus m2 = new MersennePrimeModulus(128, 1);
        MersennePrimeModulus m3 = new MersennePrimeModulus(256, 1);
        MersennePrimeModulus m4 = new MersennePrimeModulus(128, 2);

        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertNotEquals(m1, m4);
        assertNotEquals(m1, null);
        assertNotEquals(m1, new Object());
    }

    @Test
    public void testHashCode() {
        MersennePrimeModulus m1 = new MersennePrimeModulus(128, 1);
        MersennePrimeModulus m2 = new MersennePrimeModulus(128, 1);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void testToString() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(64, 3);
        String expected = "MersennePrimeModulus{value=" + modulus.prime + "}";
        assertEquals(expected, modulus.toString());
    }

    @Test
    public void testModLessThanPrime() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(64, 3);
        BigInteger value = BigInteger.valueOf(100);
        assertEquals(value, modulus.mod(value));
    }

    @Test
    public void testModEqualToPrime() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(64, 3);
        assertEquals(BigInteger.ZERO, modulus.mod(modulus.prime));
    }

    @Test
    public void testModGreaterThanPrime() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(64, 3);
        BigInteger value = modulus.prime.add(BigInteger.ONE);
        assertEquals(BigInteger.ONE, modulus.mod(value));
    }

    @Test
    public void testModLargeValue() {
        MersennePrimeModulus modulus = new MersennePrimeModulus(64, 3);
        BigInteger value = modulus.prime.multiply(BigInteger.valueOf(10)).add(BigInteger.valueOf(500));
        BigInteger expected = value.mod(modulus.prime);
        assertEquals(expected, modulus.mod(value));
    }
}
