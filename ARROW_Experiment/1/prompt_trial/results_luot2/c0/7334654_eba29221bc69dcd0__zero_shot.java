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
  public void returnsNullForPathNotStartingWithDotSegment() {
    assertNull(PathUtils.resolveRelativePath("child.html", "templates/page.html", '/'));
  }

  @Test
  public void resolvesRelativePathAgainstEmptyAnchor() {
    assertEquals("child.html", PathUtils.resolveRelativePath("./child.html", "", '/'));
  }

  @Test
  public void parentTraversalBeyondAnchorProducesRemainingPath() {
    assertEquals("base.html", PathUtils.resolveRelativePath("../../base.html", "templates/page.html", '/'));
  }
}
