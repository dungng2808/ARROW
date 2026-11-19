package app.metatron.discovery.prep.parser.preparation.rule.expr;

import com.google.common.base.Function;
import java.util.Comparator;
import org.junit.Test;

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
  public void equalsAndHashCodeForEqualPairs() {
    Pair<String, Integer> first = Pair.of("left", 7);
    Pair<String, Integer> second = Pair.of("left", 7);

    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void equalsHandlesIdentityNullAndDifferentTypes() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertTrue(pair.equals(pair));
    assertFalse(pair.equals(null));
    assertFalse(pair.equals("left"));
  }

  @Test
  public void equalsDetectsDifferentValues() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertFalse(pair.equals(Pair.of("right", 7)));
    assertFalse(pair.equals(Pair.of("left", 8)));
  }

  @Test
  public void equalsAndHashCodeHandleNullValues() {
    Pair<String, Integer> first = Pair.of(null, null);
    Pair<String, Integer> second = Pair.of(null, null);

    assertTrue(first.equals(second));
    assertEquals(0, first.hashCode());
    assertEquals(first.hashCode(), second.hashCode());
    assertFalse(first.equals(Pair.of("left", null)));
    assertFalse(first.equals(Pair.of(null, 1)));
  }

  @Test
  public void toStringFormatsValues() {
    Pair<String, Integer> pair = Pair.of("left", 7);

    assertEquals("Pair{lhs=left, rhs=7}", pair.toString());
  }

  @Test
  public void lhsFunctionReturnsLeftValue() {
    Pair<String, Integer> pair = Pair.of("left", 7);
    Function<Pair<String, Integer>, String> function = Pair.lhsFn();

    assertEquals("left", function.apply(pair));
  }

  @Test
  public void rhsFunctionReturnsRightValue() {
    Pair<String, Integer> pair = Pair.of("left", 7);
    Function<Pair<String, Integer>, Integer> function = Pair.rhsFn();

    assertEquals(Integer.valueOf(7), function.apply(pair));
  }

  @Test
  public void functionsReturnNullValues() {
    Pair<String, Integer> pair = Pair.of(null, null);

    assertNull(Pair.<String, Integer>lhsFn().apply(pair));
    assertNull(Pair.<String, Integer>rhsFn().apply(pair));
  }

  @Test
  public void lhsComparatorComparesLeftValues() {
    Comparator<Pair<String, Integer>> comparator =
        Pair.lhsComparator(new Comparator<String>() {
          @Override
          public int compare(String first, String second) {
            return first.compareTo(second);
          }
        });

    assertTrue(comparator.compare(Pair.of("a", 100), Pair.of("b", 0)) < 0);
    assertEquals(0, comparator.compare(Pair.of("same", 1), Pair.of("same", 2)));
    assertTrue(comparator.compare(Pair.of("b", 0), Pair.of("a", 100)) > 0);
  }
}
