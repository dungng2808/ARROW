package dk.alexandra.fresco.framework.builder.numeric.field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import org.junit.Test;

public class MersennePrimeModulus_ArrowTest {

  @Test
  public void testConstructor() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(128, 1);
    assertThat(modulus.getBigInteger(), is(BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE)));
  }

  @Test
  public void testModSmallValue() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(128, 1);
    BigInteger value = BigInteger.valueOf(100);
    assertThat(modulus.mod(value), is(value));
  }

  @Test
  public void testModEqualToPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(128, 1);
    BigInteger value = modulus.getBigInteger();
    assertThat(modulus.mod(value), is(BigInteger.ZERO));
  }

  @Test
  public void testModLargerThanPrime() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(128, 1);
    BigInteger value = modulus.getBigInteger().add(BigInteger.ONE);
    assertThat(modulus.mod(value), is(BigInteger.ONE));
  }

  @Test
  public void testModWithConstant() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(128, 3);
    BigInteger value = BigInteger.valueOf(2).pow(130);
    BigInteger result = modulus.mod(value);
    // Value is 2^130, which when reduced should be 2^2 * 3 = 12
    assertThat(result, is(BigInteger.valueOf(12)));
  }

  @Test
  public void testEquals() {
    MersennePrimeModulus m1 = new MersennePrimeModulus(128, 1);
    MersennePrimeModulus m2 = new MersennePrimeModulus(128, 1);
    assertThat(m1.equals(m2), is(true));
  }

  @Test
  public void testNotEquals() {
    MersennePrimeModulus m1 = new MersennePrimeModulus(128, 1);
    MersennePrimeModulus m2 = new MersennePrimeModulus(128, 2);
    assertThat(m1.equals(m2), is(false));
  }

  @Test
  public void testHashCode() {
    MersennePrimeModulus m1 = new MersennePrimeModulus(128, 1);
    MersennePrimeModulus m2 = new MersennePrimeModulus(128, 1);
    assertThat(m1.hashCode(), is(m2.hashCode()));
  }

  @Test
  public void testToString() {
    MersennePrimeModulus modulus = new MersennePrimeModulus(128, 1);
    assertThat(modulus.toString(), is("MersennePrimeModulus{value=" + modulus.getBigInteger() + '}'));
  }
}
