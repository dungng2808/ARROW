package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.junit.Test;

public class MersennePrimeModulus_ArrowTest {

  @Test
  public void modReturnsValuesBelowPrimeUnchanged() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);
    BigInteger value = BigInteger.valueOf(7);

    assertEquals(value, modulus.mod(value));
  }

  @Test
  public void modReturnsZeroForPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);

    assertEquals(BigInteger.ZERO, modulus.mod(BigInteger.valueOf(15)));
  }

  @Test
  public void modReducesValuesAbovePrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);

    assertEquals(BigInteger.valueOf(1), modulus.mod(BigInteger.valueOf(16)));
    assertEquals(BigInteger.valueOf(2), modulus.mod(BigInteger.valueOf(17)));
  }

  @Test
  public void modReducesLargeValuesUsingRepeatedFolding() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);

    // 16^3 + 1 is congruent to 2 modulo 15.
    assertEquals(BigInteger.valueOf(2), modulus.mod(BigInteger.valueOf(4097)));
  }

  @Test
  public void equalsRecognizesSameInstanceAndEquivalentValues() {
    MersennePrimeModulus first = new MersennePrimeModulus(4, 1);
    MersennePrimeModulus second = new MersennePrimeModulus(4, 1);

    assertTrue(first.equals(first));
    assertTrue(first.equals(second));
  }

  @Test
  public void equalsRejectsNullDifferentClassAndDifferentParameters() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);

    assertFalse(modulus.equals(null));
    assertFalse(modulus.equals("not a modulus"));
    assertFalse(modulus.equals(new MersennePrimeModulus(5, 1)));
    assertFalse(modulus.equals(new MersennePrimeModulus(4, 2)));
  }

  @Test
  public void hashCodeMatchesForEquivalentValues() {
    MersennePrimeModulus first = new MersennePrimeModulus(4, 1);
    MersennePrimeModulus second = new MersennePrimeModulus(4, 1);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void toStringIncludesPrimeValue() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);

    assertEquals("MersennePrimeModulus{value=15}", modulus.toString());
  }

  @Test
  public void getBigIntegerReturnsPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(4, 1);

    assertEquals(BigInteger.valueOf(15), modulus.getBigInteger());
  }

  @Test
  public void constructorRejectsNonDivisibleBitLength() {
    try {
      new MersennePrimeModulus(3, 1);
    } catch (RuntimeException e) {
      assertNotNull(e);
      return;
    }
    throw new AssertionError("Expected constructor to reject a non-divisible bit length");
  }
}
