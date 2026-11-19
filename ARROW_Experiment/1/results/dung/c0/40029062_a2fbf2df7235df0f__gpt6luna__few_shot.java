package org.sonatype.nexus.common.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VersionComparator_ArrowTest {

  @Test
  public void comparesVersionStringsByVersionOrder() {
    VersionComparator comparator = new VersionComparator();
    assertEquals(-1, comparator.compare("1.2", "1.10"));
    assertEquals(0, comparator.compare("2.0", "2.0.0"));
  }

  @Test
  public void sortsNonVersionStringsBeforeVersionStrings() {
    VersionComparator comparator = new VersionComparator();
    assertEquals(-1, comparator.compare("latest", "1.0"));
    assertEquals(1, comparator.compare("1.0", "latest"));
  }

  @Test
  public void comparesNonVersionStringsLexicographically() {
    VersionComparator comparator = new VersionComparator();
    assertEquals(-1, comparator.compare("ABC", "DEF"));
    assertEquals(0, comparator.compare("release", "release"));
  }

  @Test
  public void parsesVersionString() {
    assertNotNull(VersionComparator.version("1.2.3"));
  }

  @Test
  public void instanceComparatorUsesVersionOrdering() {
    assertEquals(-1, VersionComparator.INSTANCE.compare("1.0", "2.0"));
  }
}
