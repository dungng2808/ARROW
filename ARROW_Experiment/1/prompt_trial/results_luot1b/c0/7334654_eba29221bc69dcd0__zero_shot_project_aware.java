package com.mitchellbosecke.pebble.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PathUtils_ArrowTest {

  @Test
  public void resolvesParentRelativePathWithForwardSlashes() {
    assertThat(PathUtils.resolveRelativePath("../shared/file.html", "templates/pages/page.html", '/'))
        .isEqualTo("templates/shared/file.html");
  }

  @Test
  public void resolvesCurrentDirectoryRelativePath() {
    assertThat(PathUtils.resolveRelativePath("./child.html", "templates/page.html", '/'))
        .isEqualTo("templates/child.html");
  }

  @Test
  public void normalizesSeparatorsToExpectedSeparator() {
    assertThat(PathUtils.resolveRelativePath("..\\shared\\file.html", "templates\\pages\\page.html", '/'))
        .isEqualTo("templates/shared/file.html");
  }

  @Test
  public void resolvesRelativePathWithBackslashSeparator() {
    assertThat(PathUtils.resolveRelativePath("..\\shared\\file.html", "templates\\pages\\page.html", '\\'))
        .isEqualTo("templates\\shared\\file.html");
  }

  @Test
  public void returnsNullForNullRelativePath() {
    assertThat(PathUtils.resolveRelativePath(null, "templates/page.html", '/')).isNull();
  }

  @Test
  public void returnsNullForEmptyRelativePath() {
    assertThat(PathUtils.resolveRelativePath("", "templates/page.html", '/')).isNull();
  }

  @Test
  public void returnsNullForNonRelativePath() {
    assertThat(PathUtils.resolveRelativePath("templates/file.html", "templates/page.html", '/'))
        .isNull();
  }

  @Test
  public void resolvesRelativePathAgainstNullAnchor() {
    assertThat(PathUtils.resolveRelativePath("../file.html", null, '/')).isEqualTo("file.html");
  }

  @Test
  public void resolvesRelativePathAgainstEmptyAnchor() {
    assertThat(PathUtils.resolveRelativePath("./file.html", "", '/')).isEqualTo("file.html");
  }

  @Test
  public void resolvesParentBeyondAnchorWithoutAddingSegments() {
    assertThat(PathUtils.resolveRelativePath("../../file.html", "page.html", '/'))
        .isEqualTo("file.html");
  }

  @Test
  public void preservesTrailingAnchorSeparator() {
    assertThat(PathUtils.resolveRelativePath("../file.html", "templates/pages/", '/'))
        .isEqualTo("templates/file.html");
  }

  @Test
  public void resolvesDotSegmentsWithoutChangingAnchorDirectory() {
    assertThat(PathUtils.resolveRelativePath("././file.html", "templates/pages/", '/'))
        .isEqualTo("templates/pages/file.html");
  }
}
