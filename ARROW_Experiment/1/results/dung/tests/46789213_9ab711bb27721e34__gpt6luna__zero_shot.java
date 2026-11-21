package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.junit.Test;

public class MersennePrimeModulus_ArrowTest {

  @Test
  public void equalsAndHashCodeUseBitLengthAndConstant() {
    MersennePrimeModulus first = new MersennePrimeModulus(8, 3);
    MersennePrimeModulus same = new MersennePrimeModulus(8, 3);
    MersennePrimeModulus differentConstant = new MersennePrimeModulus(8, 5);
    MersennePrimeModulus differentLength = new MersennePrimeModulus(16, 3);

    assertTrue(first.equals(first));
    assertTrue(first.equals(same));
    assertEquals(first.hashCode(), same.hashCode());
    assertFalse(first.equals(differentConstant));
    assertFalse(first.equals(differentLength));
    assertFalse(first.equals(null));
    assertFalse(first.equals("not a modulus"));
  }

  @Test
  public void toStringIncludesComputedPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);

    assertEquals("MersennePrimeModulus{value=253}", modulus.toString());
  }

  @Test
  public void modReturnsValueBelowPrimeUnchanged() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);
    BigInteger value = BigInteger.valueOf(100);

    assertEquals(value, modulus.mod(value));
  }

  @Test
  public void modReturnsZeroForValueEqualToPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);

    assertEquals(BigInteger.ZERO, modulus.mod(BigInteger.valueOf(253)));
  }

  @Test
  public void modReducesValueAbovePrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);

    assertEquals(BigInteger.valueOf(2), modulus.mod(BigInteger.valueOf(255)));
  }

  @Test
  public void modReducesValueRequiringRepeatedFolding() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);

    assertEquals(BigInteger.valueOf(9), modulus.mod(BigInteger.valueOf(65536)));
  }

  @Test
  public void modHandlesZero() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);

    assertEquals(BigInteger.ZERO, modulus.mod(BigInteger.ZERO));
  }

  @Test
  public void modHandlesNegativeValueBelowPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(8, 3);
    BigInteger value = BigInteger.valueOf(-1);

    assertEquals(value, modulus.mod(value));
  }
}
