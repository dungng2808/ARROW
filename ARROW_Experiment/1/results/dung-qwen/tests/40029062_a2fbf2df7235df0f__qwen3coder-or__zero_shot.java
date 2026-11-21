package org.sonatype.nexus.common.app;

import java.util.Comparator;

import org.junit.Test;
import static org.junit.Assert.*;

public class VersionComparator_ArrowTest {

    @Test
    public void testCompareBothVersionLike() {
        Comparator<String> comparator = VersionComparator.INSTANCE;
        assertEquals(0, comparator.compare("1.0", "1.0"));
        assertTrue(comparator.compare("1.0", "2.0") < 0);
        assertTrue(comparator.compare("2.0", "1.0") > 0);
        assertTrue(comparator.compare("1.0-beta4", "1.0") > 0);
        assertTrue(comparator.compare("1.0", "1.0-beta4") < 0);
    }

    @Test
    public void testCompareBothNonVersionLike() {
        Comparator<String> comparator = VersionComparator.INSTANCE;
        assertEquals(0, comparator.compare("latest", "latest"));
        assertTrue(comparator.compare("beta5", "release") > 0);
        assertTrue(comparator.compare("release", "beta5") < 0);
        assertTrue(comparator.compare("ABCDEF", "0xFF") > 0);
    }

    @Test
    public void testCompareFirstVersionLikeSecondNot() {
        Comparator<String> comparator = VersionComparator.INSTANCE;
        assertTrue(comparator.compare("1.0", "latest") > 0);
        assertTrue(comparator.compare("2.0.0", "beta5") > 0);
    }

    @Test
    public void testCompareFirstNotVersionLikeSecondVersionLike() {
        Comparator<String> comparator = VersionComparator.INSTANCE;
        assertTrue(comparator.compare("latest", "1.0") < 0);
        assertTrue(comparator.compare("beta5", "2.0.0") < 0);
    }

    @Test
    public void testCompareNullValues() {
        Comparator<String> comparator = VersionComparator.INSTANCE;
        // Both null should be equal
        assertEquals(0, comparator.compare(null, null));
        // Null vs non-null - null should come first
        assertTrue(comparator.compare(null, "1.0") < 0);
        assertTrue(comparator.compare("1.0", null) > 0);
    }

    @Test
    public void testIsVersionLike() {
        assertFalse(VersionComparator.INSTANCE.compare("latest", "release") == 0);
        assertTrue(VersionComparator.INSTANCE.compare("1.0", "1.0") == 0);
    }

    @Test
    public void testVersionMethod() {
        assertNotNull(VersionComparator.version("1.0"));
        assertNotNull(VersionComparator.version("2.0.0"));
        assertNotNull(VersionComparator.version("1.0-beta4"));
        try {
            VersionComparator.version("invalid.version");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
