package dk.alexandra.fresco.framework.builder.numeric.field;

import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MersennePrimeModulus_ArrowTest {

  @Test
  public void modulusStringShowsComputedPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 1);
    assertEquals("MersennePrimeModulus{value=255}", modulus.toString());
  }

  @Test
  public void equalModuliHaveEqualHashCodes() {
    MersennePrimeModulus first = new MersennePrimeModulus(8, 1);
    MersennePrimeModulus second = new MersennePrimeModulus(8, 1);
    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void differentParametersAreNotEqual() {
    MersennePrimeModulus first = new MersennePrimeModulus(8, 1);
    MersennePrimeModulus second = new MersennePrimeModulus(8, 3);
    assertNotEquals(first, second);
  }

  @Test
  public void modulusIsNotEqualToNullOrAnotherClass() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 1);
    assertFalse(modulus.equals(null));
    assertFalse(modulus.equals("modulus"));
  }

  @Test
  public void modulusReturnsValuesBelowPrimeUnchanged() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 1);
    BigInteger value = BigInteger.valueOf(100);
    assertEquals(value, modulus.mod(value));
  }

  @Test
  public void modulusMapsPrimeToZero() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 1);
    assertEquals(BigInteger.ZERO, modulus.mod(BigInteger.valueOf(255)));
  }

  @Test
  public void modulusReducesValuesAbovePrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 1);
    assertEquals(BigInteger.valueOf(1), modulus.mod(BigInteger.valueOf(256)));
    assertEquals(BigInteger.valueOf(44), modulus.mod(BigInteger.valueOf(300)));
  }
}
