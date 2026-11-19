package com.mitchellbosecke.pebble.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathUtils_ArrowTest {

    @Test
    public void resolvesParentRelativePathUsingForwardSlashes() {
        assertEquals("templates/base.peb", PathUtils.resolveRelativePath("../base.peb",
                "templates/pages/current.peb", '/'));
    }

    @Test
    public void resolvesCurrentDirectoryRelativePath() {
        assertEquals("templates/pages/child.peb", PathUtils.resolveRelativePath("./child.peb",
                "templates/pages/current.peb", '/'));
    }

    @Test
    public void sanitizesMixedSeparators() {
        assertEquals("templates/base.peb", PathUtils.resolveRelativePath("..\\base.peb",
                "templates\\pages\\current.peb", '/'));
    }

    @Test
    public void returnsNullForNullRelativePath() {
        assertNull(PathUtils.resolveRelativePath(null, "templates/current.peb", '/'));
    }

    @Test
    public void returnsNullForEmptyRelativePath() {
        assertNull(PathUtils.resolveRelativePath("", "templates/current.peb", '/'));
    }

    @Test
    public void returnsNullForNonRelativePath() {
        assertNull(PathUtils.resolveRelativePath("templates/child.peb",
                "templates/current.peb", '/'));
    }

    @Test
    public void resolvesRelativePathWithEmptyAnchor() {
        assertEquals("child.peb", PathUtils.resolveRelativePath("./child.peb", "", '/'));
    }

    @Test
    public void resolvesParentPathBeyondEmptyAnchor() {
        assertEquals("child.peb", PathUtils.resolveRelativePath("../child.peb", "", '/'));
    }

    @Test
    public void resolvesPathWithBackslashSeparator() {
        assertEquals("templates\\base.peb", PathUtils.resolveRelativePath("..\\base.peb",
                "templates\\pages\\current.peb", '\\'));
    }
}
