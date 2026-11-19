package com.mitchellbosecke.pebble.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PathUtils_ArrowTest {

  @Test
  public void resolvesParentRelativePath() {
    assertThat(PathUtils.resolveRelativePath("../partials/item.html", "templates/page.html", '/'))
        .isEqualTo("partials/item.html");
  }

  @Test
  public void resolvesCurrentDirectoryRelativePath() {
    assertThat(PathUtils.resolveRelativePath("./item.html", "templates/page.html", '/'))
        .isEqualTo("templates/item.html");
  }

  @Test
  public void normalizesSeparatorsInBothPaths() {
    assertThat(PathUtils.resolveRelativePath("..\\partials\\item.html", "templates\\page.html", '/'))
        .isEqualTo("partials/item.html");
  }

  @Test
  public void resolvesAgainstDirectoryAnchor() {
    assertThat(PathUtils.resolveRelativePath("../item.html", "templates/", '/'))
        .isEqualTo("item.html");
  }

  @Test
  public void returnsNullForNonRelativePath() {
    assertThat(PathUtils.resolveRelativePath("item.html", "templates/page.html", '/')).isNull();
  }

  @Test
  public void returnsNullForEmptyRelativePath() {
    assertThat(PathUtils.resolveRelativePath("", "templates/page.html", '/')).isNull();
  }

  @Test
  public void returnsNullForNullRelativePath() {
    assertThat(PathUtils.resolveRelativePath(null, "templates/page.html", '/')).isNull();
  }

  @Test
  public void resolvesRelativePathWithEmptyAnchor() {
    assertThat(PathUtils.resolveRelativePath("../item.html", "", '/')).isEqualTo("item.html");
  }

  @Test
  public void resolvesRelativePathWithNullAnchor() {
    assertThat(PathUtils.resolveRelativePath("./item.html", null, '/')).isEqualTo("item.html");
  }

  @Test
  public void preservesLeadingSeparatorInAnchor() {
    assertThat(PathUtils.resolveRelativePath("./item.html", "/templates/page.html", '/'))
        .isEqualTo("/templates/item.html");
  }
}
