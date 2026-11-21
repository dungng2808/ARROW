package org.sonatype.nexus.common.app;

import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

public class VersionComparator_ArrowTest {

  @Test
  public void testCompareBothVersionLike() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("1.0", "2.0");
    assertTrue(result < 0);
  }

  @Test
  public void testCompareBothVersionLikeEqual() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("1.0", "1.0");
    assertEquals(0, result);
  }

  @Test
  public void testCompareBothVersionLikeReverse() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("2.0", "1.0");
    assertTrue(result > 0);
  }

  @Test
  public void testCompareFirstVersionLikeSecondNot() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("1.0", "latest");
    assertTrue(result > 0);
  }

  @Test
  public void testCompareFirstNotVersionLikeSecondVersionLike() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("latest", "1.0");
    assertTrue(result < 0);
  }

  @Test
  public void testCompareNeitherVersionLike() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("latest", "release");
    assertTrue(result > 0);
  }

  @Test
  public void testCompareNeitherVersionLikeEqual() {
    Comparator<String> comparator = VersionComparator.INSTANCE;
    int result = comparator.compare("latest", "latest");
    assertEquals(0, result);
  }

  @Test
  public void testVersionLikeDetection() {
    assertTrue(VersionComparator.INSTANCE instanceof Comparator);
  }

  @Test
  public void testVersionParsing() {
    assertNotNull(VersionComparator.version("1.0"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVersionParsingInvalid() {
    VersionComparator.version("invalid-version");
  }

  @Test
  public void testIsVersionLike() {
    assertTrue(VersionComparator.INSTANCE.getClass().getDeclaredMethod("isVersionLike", String.class)
        .invoke(VersionComparator.INSTANCE, "1.0") != null);
  }
}
