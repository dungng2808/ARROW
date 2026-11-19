package com.mitchellbosecke.pebble.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PathUtils_ArrowTest {

    @Test
    public void resolvesParentRelativePathUsingForwardSlashes() {
        String result = PathUtils.resolveRelativePath("../shared/file.html", "templates/pages/page.html", '/');

        assertThat(result).isEqualTo("templates/shared/file.html");
    }

    @Test
    public void resolvesCurrentDirectoryRelativePath() {
        String result = PathUtils.resolveRelativePath("./file.html", "templates/pages/page.html", '/');

        assertThat(result).isEqualTo("templates/pages/file.html");
    }

    @Test
    public void sanitizesMixedSeparatorsToExpectedSeparator() {
        String result = PathUtils.resolveRelativePath("..\\shared\\file.html", "templates\\pages\\page.html", '/');

        assertThat(result).isEqualTo("templates/shared/file.html");
    }

    @Test
    public void resolvesMultipleParentSegments() {
        String result = PathUtils.resolveRelativePath("../../shared/file.html", "templates/pages/page.html", '/');

        assertThat(result).isEqualTo("shared/file.html");
    }

    @Test
    public void returnsNullForPathThatIsNotRelative() {
        String result = PathUtils.resolveRelativePath("templates/file.html", "templates/pages/page.html", '/');

        assertThat(result).isNull();
    }

    @Test
    public void returnsNullForNullRelativePath() {
        String result = PathUtils.resolveRelativePath(null, "templates/pages/page.html", '/');

        assertThat(result).isNull();
    }

    @Test
    public void returnsNullForEmptyRelativePath() {
        String result = PathUtils.resolveRelativePath("", "templates/pages/page.html", '/');

        assertThat(result).isNull();
    }

    @Test
    public void resolvesRelativePathWithEmptyAnchor() {
        String result = PathUtils.resolveRelativePath("./file.html", "", '/');

        assertThat(result).isEqualTo("file.html");
    }

    @Test
    public void resolvesRelativePathWithNullAnchor() {
        String result = PathUtils.resolveRelativePath("../file.html", null, '/');

        assertThat(result).isEqualTo("file.html");
    }

    @Test
    public void resolvesPathUsingBackslashAsExpectedSeparator() {
        String result = PathUtils.resolveRelativePath("..\\shared\\file.html", "templates\\pages\\page.html", '\\');

        assertThat(result).isEqualTo("templates\\shared\\file.html");
    }
}
