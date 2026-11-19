package org.sonatype.nexus.common.app;

import org.eclipse.aether.version.Version;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VersionComparator_ArrowTest
{
  @Test
  public void comparesVersionLikeStringsSemantically() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(-1, comparator.compare("1.9", "1.10"));
    assertEquals(1, comparator.compare("2.0", "1.99"));
    assertEquals(0, comparator.compare("1.0", "1.0.0"));
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
    assertEquals(1, comparator.compare("release", "beta"));
    assertEquals(0, comparator.compare("latest", "latest"));
  }

  @Test
  public void versionParsesVersionString() {
    Version version = VersionComparator.version("1.2.3");

    assertNotNull(version);
    assertEquals(0, version.compareTo(VersionComparator.version("1.2.3")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void versionRejectsNull() {
    VersionComparator.version(null);
  }
}
