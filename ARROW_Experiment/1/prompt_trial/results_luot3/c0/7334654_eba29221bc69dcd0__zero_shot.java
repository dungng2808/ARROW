package com.mitchellbosecke.pebble.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PathUtils_ArrowTest {

  @Test
  public void resolvesParentRelativePath() {
    assertEquals("templates/base.peb", PathUtils.resolveRelativePath("../base.peb",
        "templates/pages/page.peb", '/'));
  }

  @Test
  public void resolvesCurrentDirectoryRelativePath() {
    assertEquals("templates/pages/child.peb", PathUtils.resolveRelativePath("./child.peb",
        "templates/pages/page.peb", '/'));
  }

  @Test
  public void normalizesSeparators() {
    assertEquals("templates/base.peb", PathUtils.resolveRelativePath("..\\base.peb",
        "templates\\pages\\page.peb", '/'));
  }

  @Test
  public void returnsNullForNullRelativePath() {
    assertNull(PathUtils.resolveRelativePath(null, "templates/page.peb", '/'));
  }

  @Test
  public void returnsNullForEmptyRelativePath() {
    assertNull(PathUtils.resolveRelativePath("", "templates/page.peb", '/'));
  }

  @Test
  public void returnsNullForNonRelativePath() {
    assertNull(PathUtils.resolveRelativePath("templates/base.peb", "templates/page.peb", '/'));
  }

  @Test
  public void resolvesRelativePathWithEmptyAnchor() {
    assertEquals("child.peb", PathUtils.resolveRelativePath("./child.peb", "", '/'));
  }

  @Test
  public void resolvesRelativePathWithNullAnchor() {
    assertEquals("child.peb", PathUtils.resolveRelativePath("./child.peb", null, '/'));
  }

  @Test
  public void resolvesParentBeyondAnchor() {
    assertEquals("base.peb", PathUtils.resolveRelativePath("../../base.peb",
        "templates/pages/page.peb", '/'));
  }

  @Test
  public void preservesTrailingSeparatorInRelativePath() {
    assertEquals("templates/pages", PathUtils.resolveRelativePath("./", "templates/pages/page.peb",
        '/'));
  }
}
