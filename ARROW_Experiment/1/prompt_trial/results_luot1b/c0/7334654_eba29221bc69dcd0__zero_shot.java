package com.mitchellbosecke.pebble.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathUtils_ArrowTest {

  @Test
  public void resolvesParentRelativePathWithForwardSlashes() {
    assertEquals("templates/base.html",
        PathUtils.resolveRelativePath("../base.html", "templates/pages/page.html", '/'));
  }

  @Test
  public void resolvesCurrentDirectoryRelativePath() {
    assertEquals("templates/pages/child.html",
        PathUtils.resolveRelativePath("./child.html", "templates/pages/page.html", '/'));
  }

  @Test
  public void sanitizesMixedSeparatorsToExpectedSeparator() {
    assertEquals("templates/base.html",
        PathUtils.resolveRelativePath("..\\base.html", "templates\\pages\\page.html", '/'));
  }

  @Test
  public void returnsNullForNullRelativePath() {
    assertNull(PathUtils.resolveRelativePath(null, "templates/page.html", '/'));
  }

  @Test
  public void returnsNullForEmptyRelativePath() {
    assertNull(PathUtils.resolveRelativePath("", "templates/page.html", '/'));
  }

  @Test
  public void returnsNullForNonRelativePath() {
    assertNull(PathUtils.resolveRelativePath("templates/base.html", "templates/page.html", '/'));
  }

  @Test
  public void resolvesRelativePathWithEmptyAnchor() {
    assertEquals("base.html", PathUtils.resolveRelativePath("../base.html", "", '/'));
  }

  @Test
  public void resolvesRelativePathWithNullAnchor() {
    assertEquals("base.html", PathUtils.resolveRelativePath("../base.html", null, '/'));
  }

  @Test
  public void resolvesToEmptyPathWhenParentRemovesOnlyAnchorSegment() {
    assertEquals("", PathUtils.resolveRelativePath("../base.html", "page.html", '/'));
  }

  @Test
  public void preservesTrailingSeparatorInRelativePath() {
    assertEquals("templates/pages/",
        PathUtils.resolveRelativePath("./", "templates/pages/page.html", '/'));
  }
}
