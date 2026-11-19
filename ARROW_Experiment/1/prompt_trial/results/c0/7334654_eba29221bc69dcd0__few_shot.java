package com.mitchellbosecke.pebble.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathUtils_ArrowTest {

    @Test
    public void resolvesParentRelativePathUsingExpectedSeparator() {
        assertEquals("templates/base.html",
                PathUtils.resolveRelativePath("../base.html", "templates/pages/home.html", '/'));
    }

    @Test
    public void resolvesCurrentDirectoryRelativePath() {
        assertEquals("templates/pages/child.html",
                PathUtils.resolveRelativePath("./child.html", "templates/pages/home.html", '/'));
    }

    @Test
    public void normalizesSeparatorsInBothPaths() {
        assertEquals("templates/base.html",
                PathUtils.resolveRelativePath("..\\base.html", "templates\\pages\\home.html", '/'));
    }

    @Test
    public void returnsNullForPathThatIsNotRelative() {
        assertNull(PathUtils.resolveRelativePath("templates/base.html", "templates/pages/home.html", '/'));
    }

    @Test
    public void returnsNullForNullRelativePath() {
        assertNull(PathUtils.resolveRelativePath(null, "templates/pages/home.html", '/'));
    }

    @Test
    public void returnsNullForEmptyRelativePath() {
        assertNull(PathUtils.resolveRelativePath("", "templates/pages/home.html", '/'));
    }

    @Test
    public void resolvesRelativePathWithEmptyAnchor() {
        assertEquals("base.html", PathUtils.resolveRelativePath("../base.html", "", '/'));
    }
}
