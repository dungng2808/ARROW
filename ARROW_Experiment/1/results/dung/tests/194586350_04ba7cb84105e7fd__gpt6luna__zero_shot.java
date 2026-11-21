package app.metatron.discovery.prep.parser.preparation.rule.expr;

import com.google.common.base.Function;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Pair_ArrowTest {

  @Test
  public void constructorStoresValues() {
    Pair<String, Integer> pair = new Pair<>("left", 7);

    assertEquals("left", pair.lhs);
    assertEquals(Integer.valueOf(7), pair.rhs);
  }

  @Test
  public void ofCreatesPairWithValues() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertEquals("left", pair.lhs);
    assertEquals(Integer.valueOf(7), pair.rhs);
  }

  @Test
  public void equalsRecognizesSameAndEquivalentPairs() {
    Pair<String, Integer> pair = new Pair<>("left", 7);
    Pair<String, Integer> equivalent = new Pair<>("left", 7);

    assertTrue(pair.equals(pair));
    assertTrue(pair.equals(equivalent));
  }

  @Test
  public void equalsRejectsDifferentValuesAndTypes() {
    Pair<String, Integer> pair = new Pair<>("left", 7);

    assertFalse(pair.equals(new Pair<>("right", 7)));
    assertFalse(pair.equals(new Pair<>("left", 8)));
    assertFalse(pair.equals(null));
    assertFalse(pair.equals("left"));
  }

  @Test
  public void equalsHandlesNullComponents() {
    Pair<String, Integer> bothNull = new Pair<>(null, null);
    Pair<String, Integer> equivalent = new Pair<>(null, null);
    Pair<String, Integer> differentLeft = new Pair<>("left", null);
    Pair<String, Integer> differentRight = new Pair<>(null, 7);

    assertTrue(bothNull.equals(equivalent));
    assertFalse(bothNull.equals(differentLeft));
    assertFalse(bothNull.equals(differentRight));
  }

  @Test
  public void hashCodeUsesComponentHashCodes() {
    Pair<String, Integer> pair = new Pair<>("left", 7);
    int expected = 31 * "left".hashCode() + Integer.valueOf(7).hashCode();

    assertEquals(expected, pair.hashCode());
  }

  @Test
  public void hashCodeHandlesNullComponents() {
    Pair<String, Integer> pair = new Pair<>(null, 7);

    assertEquals(Integer.valueOf(7).hashCode(), pair.hashCode());
    assertEquals(0, new Pair<>(null, null).hashCode());
  }

  @Test
  public void toStringFormatsComponents() {
    Pair<String, Integer> pair = new Pair<>("left", 7);

    assertEquals("Pair{lhs=left, rhs=7}", pair.toString());
  }

  @Test
  public void toStringFormatsNullComponents() {
    Pair<String, Integer> pair = new Pair<>(null, null);

    assertEquals("Pair{lhs=null, rhs=null}", pair.toString());
  }

  @Test
  public void lhsFunctionReturnsLeftComponent() {
    Pair<String, Integer> pair = new Pair<>("left", 7);
    Function<Pair<String, Integer>, String> function = Pair.lhsFn();

    assertEquals("left", function.apply(pair));
  }

  @Test
  public void lhsFunctionReturnsNullLeftComponent() {
    Pair<String, Integer> pair = new Pair<>(null, 7);
    Function<Pair<String, Integer>, String> function = Pair.lhsFn();

    assertNull(function.apply(pair));
  }

  @Test
  public void rhsFunctionReturnsRightComponent() {
    Pair<String, Integer> pair = new Pair<>("left", 7);
    Function<Pair<String, Integer>, Integer> function = Pair.rhsFn();

    assertEquals(Integer.valueOf(7), function.apply(pair));
  }

  @Test
  public void rhsFunctionReturnsNullRightComponent() {
    Pair<String, Integer> pair = new Pair<>("left", null);
    Function<Pair<String, Integer>, Integer> function = Pair.rhsFn();

    assertNull(function.apply(pair));
  }

  @Test
  public void lhsComparatorComparesLeftComponents() {
    Comparator<Pair<String, ?>> comparator =
        Pair.lhsComparator(new Comparator<String>() {
          @Override
          public int compare(String first, String second) {
            return first.compareTo(second);
          }
        });
    Pair<String, Integer> first = new Pair<>("alpha", 100);
    Pair<String, Integer> second = new Pair<>("beta", 1);

    assertTrue(comparator.compare(first, second) < 0);
    assertTrue(comparator.compare(second, first) > 0);
    assertEquals(0, comparator.compare(first, new Pair<String, Integer>("alpha", 200)));
  }
}
