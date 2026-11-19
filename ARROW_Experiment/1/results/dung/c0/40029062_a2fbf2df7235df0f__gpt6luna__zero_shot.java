package org.sonatype.nexus.common.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VersionComparator_ArrowTest
{
  @Test
  public void comparesVersionNumbersNumerically() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(-1, comparator.compare("1.9", "1.10"));
  }

  @Test
  public void comparesVersionQualifiers() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(-1, comparator.compare("1.0-alpha", "1.0"));
  }

  @Test
  public void sortsNonVersionStringsBeforeVersions() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(-1, comparator.compare("latest", "1.0"));
  }

  @Test
  public void comparesNonVersionStringsLexicographically() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(-1, comparator.compare("ABC", "DEF"));
  }

  @Test
  public void comparesVersionLikeStringsCaseInsensitively() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(0, comparator.compare("1.0-ALPHA", "1.0-alpha"));
  }

  @Test
  public void exposesComparatorInstance() {
    assertEquals(-1, VersionComparator.INSTANCE.compare("latest", "1.0"));
  }

  @Test
  public void parsesVersion() {
    assertNotNull(VersionComparator.version("1.2.3"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsNullVersionWhenParsing() {
    VersionComparator.version(null);
  }
}
