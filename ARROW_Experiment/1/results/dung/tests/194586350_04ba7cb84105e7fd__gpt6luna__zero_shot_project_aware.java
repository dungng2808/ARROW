package app.metatron.discovery.prep.parser.preparation.rule.expr;

import com.google.common.base.Function;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Pair_ArrowTest {

  @Test
  public void ofCreatesPairWithValues() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertEquals("left", pair.lhs);
    assertEquals(Integer.valueOf(7), pair.rhs);
  }

  @Test
  public void constructorAllowsNullValues() {
    Pair<String, Integer> pair = new Pair<String, Integer>(null, null);

    assertNull(pair.lhs);
    assertNull(pair.rhs);
  }

  @Test
  public void equalsRecognizesSameInstance() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertTrue(pair.equals(pair));
  }

  @Test
  public void equalsRecognizesEqualPairs() {
    Pair<String, Integer> first = Pair.of("left", 7);
    Pair<String, Integer> second = Pair.of("left", 7);

    assertTrue(first.equals(second));
    assertTrue(second.equals(first));
  }

  @Test
  public void equalsRejectsNullAndDifferentClass() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertFalse(pair.equals(null));
    assertFalse(pair.equals("left"));
  }

  @Test
  public void equalsRejectsDifferentLeftOrRightValues() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertFalse(pair.equals(Pair.of("other", 7)));
    assertFalse(pair.equals(Pair.of("left", 8)));
  }

  @Test
  public void equalsHandlesNullComponents() {
    Pair<String, Integer> bothNull = Pair.of(null, null);
    Pair<String, Integer> sameNulls = Pair.of(null, null);

    assertTrue(bothNull.equals(sameNulls));
    assertFalse(bothNull.equals(Pair.of("left", null)));
    assertFalse(bothNull.equals(Pair.of(null, 7)));
  }

  @Test
  public void hashCodeUsesComponentHashCodes() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertEquals(31 * "left".hashCode() + Integer.valueOf(7).hashCode(), pair.hashCode());
  }

  @Test
  public void hashCodeHandlesNullComponents() {
    Pair<String, Integer> pair = Pair.of(null, 7);

    assertEquals(Integer.valueOf(7).hashCode(), pair.hashCode());
    assertEquals(0, Pair.of(null, null).hashCode());
  }

  @Test
  public void toStringFormatsComponents() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertEquals("Pair{lhs=left, rhs=7}", pair.toString());
  }

  @Test
  public void toStringFormatsNullComponents() {
    Pair<String, Integer> pair = Pair.of(null, null);

    assertEquals("Pair{lhs=null, rhs=null}", pair.toString());
  }

  @Test
  public void lhsFunctionReturnsLeftValue() {
    Pair<String, Integer> pair = Pair.of("left", 7);
    Function<Pair<String, Integer>, String> function = Pair.lhsFn();

    assertEquals("left", function.apply(pair));
  }

  @Test
  public void lhsFunctionReturnsNullLeftValue() {
    Pair<String, Integer> pair = Pair.of(null, 7);
    Function<Pair<String, Integer>, String> function = Pair.lhsFn();

    assertNull(function.apply(pair));
  }

  @Test
  public void rhsFunctionReturnsRightValue() {
    Pair<String, Integer> pair = Pair.of("left", 7);
    Function<Pair<String, Integer>, Integer> function = Pair.rhsFn();

    assertEquals(Integer.valueOf(7), function.apply(pair));
  }

  @Test
  public void rhsFunctionReturnsNullRightValue() {
    Pair<String, Integer> pair = Pair.of("left", null);
    Function<Pair<String, Integer>, Integer> function = Pair.rhsFn();

    assertNull(function.apply(pair));
  }

  @Test
  public void lhsComparatorComparesLeftValues() {
    Comparator<String> strings = new Comparator<String>() {
      @Override
      public int compare(String first, String second) {
        return first.compareTo(second);
      }
    };
    Comparator<Pair<String, ?>> comparator = Pair.lhsComparator(strings);
    Pair<String, Integer> first = Pair.of("alpha", 100);
    Pair<String, String> second = Pair.of("beta", "ignored");

    assertEquals(-1, comparator.compare(first, second));
    assertEquals(1, comparator.compare(second, first));
    assertEquals(0, comparator.compare(first, Pair.of("alpha", 200)));
  }
}
