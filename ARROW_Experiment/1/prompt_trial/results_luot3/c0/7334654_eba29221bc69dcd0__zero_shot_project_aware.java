package com.mitchellbosecke.pebble.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PathUtils_ArrowTest {

  @Test
  public void resolvesParentRelativePath() {
    String result = PathUtils.resolveRelativePath("../partials/item.html", "templates/page.html", '/');

    assertThat(result).isEqualTo("partials/item.html");
  }

  @Test
  public void resolvesCurrentDirectoryRelativePath() {
    String result = PathUtils.resolveRelativePath("./item.html", "templates/page.html", '/');

    assertThat(result).isEqualTo("templates/item.html");
  }

  @Test
  public void normalizesSeparatorsToExpectedSeparator() {
    String result = PathUtils.resolveRelativePath("..\\partials\\item.html", "templates\\page.html", '/');

    assertThat(result).isEqualTo("partials/item.html");
  }

  @Test
  public void resolvesMultipleParentSegments() {
    String result = PathUtils.resolveRelativePath("../../item.html", "one/two/page.html", '/');

    assertThat(result).isEqualTo("item.html");
  }

  @Test
  public void returnsNullForNullRelativePath() {
    String result = PathUtils.resolveRelativePath(null, "templates/page.html", '/');

    assertThat(result).isNull();
  }

  @Test
  public void returnsNullForEmptyRelativePath() {
    String result = PathUtils.resolveRelativePath("", "templates/page.html", '/');

    assertThat(result).isNull();
  }

  @Test
  public void returnsNullForNonRelativePath() {
    String result = PathUtils.resolveRelativePath("item.html", "templates/page.html", '/');

    assertThat(result).isNull();
  }

  @Test
  public void resolvesRelativePathWithNullAnchor() {
    String result = PathUtils.resolveRelativePath("./item.html", null, '/');

    assertThat(result).isEqualTo("item.html");
  }

  @Test
  public void resolvesRelativePathWithEmptyAnchor() {
    String result = PathUtils.resolveRelativePath("../item.html", "", '/');

    assertThat(result).isEqualTo("item.html");
  }
}
