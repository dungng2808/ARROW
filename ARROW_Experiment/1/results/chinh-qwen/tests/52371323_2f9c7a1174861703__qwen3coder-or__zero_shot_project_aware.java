package com.spotify.metrics.core;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class MetricId_ArrowTest {

    @Test
    public void testEmptyConstructor() {
        MetricId metricId = new MetricId();
        assertNull(metricId.getKey());
        assertEquals(MetricId.EMPTY_TAGS, metricId.getTags());
    }

    @Test
    public void testStringConstructor() {
        MetricId metricId = new MetricId("test");
        assertEquals("test", metricId.getKey());
        assertEquals(MetricId.EMPTY_TAGS, metricId.getTags());
    }

    @Test
    public void testStringMapConstructor() {
        Map<String, String> tags = ImmutableMap.of("key1", "value1");
        MetricId metricId = new MetricId("test", tags);
        assertEquals("test", metricId.getKey());
        assertEquals(tags, metricId.getTags());
    }

    @Test
    public void testResolve() {
        MetricId metricId = new MetricId("test");
        MetricId resolved = metricId.resolve("sub");
        assertEquals("test.sub", resolved.getKey());
        assertEquals(MetricId.EMPTY_TAGS, resolved.getTags());
    }

    @Test
    public void testResolveWithNullPart() {
        MetricId metricId = new MetricId("test");
        MetricId resolved = metricId.resolve(null);
        assertEquals("test", resolved.getKey());
        assertEquals(MetricId.EMPTY_TAGS, resolved.getTags());
    }

    @Test
    public void testResolveWithEmptyPart() {
        MetricId metricId = new MetricId("test");
        MetricId resolved = metricId.resolve("");
        assertEquals("test", resolved.getKey());
        assertEquals(MetricId.EMPTY_TAGS, resolved.getTags());
    }

    @Test
    public void testResolveWithNullKey() {
        MetricId metricId = new MetricId((String) null);
        MetricId resolved = metricId.resolve("sub");
        assertEquals("sub", resolved.getKey());
        assertEquals(MetricId.EMPTY_TAGS, resolved.getTags());
    }

    @Test
    public void testTaggedMap() {
        MetricId metricId = new MetricId("test");
        Map<String, String> add = ImmutableMap.of("key1", "value1");
        MetricId tagged = metricId.tagged(add);
        assertEquals("test", tagged.getKey());
        assertEquals(add, tagged.getTags());
    }

    @Test
    public void testTaggedVarargs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1", "key2", "value2");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("key1", "value1", "key2", "value2"), tagged.getTags());
    }

    @Test
    public void testTaggedVarargsOddCount() {
        MetricId metricId = new MetricId("test");
        try {
            metricId.tagged("key1", "value1", "key2");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Argument count must be even", e.getMessage());
        }
    }

    @Test
    public void testTaggedVarargsNull() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged((String[]) null);
        assertEquals("test", tagged.getKey());
        assertEquals(MetricId.EMPTY_TAGS, tagged.getTags());
    }

    @Test
    public void testTaggedSinglePair() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("key1", "value1"), tagged.getTags());
    }

    @Test
    public void testTaggedTwoPairs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1", "key2", "value2");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("key1", "value1", "key2", "value2"), tagged.getTags());
    }

    @Test
    public void testTaggedThreePairs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1", "key2", "value2", "key3", "value3");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("key1", "value1", "key2", "value2", "key3", "value3"), tagged.getTags());
    }

    @Test
    public void testJoin() {
        MetricId part1 = new MetricId("test1", ImmutableMap.of("tag1", "value1"));
        MetricId part2 = new MetricId("test2", ImmutableMap.of("tag2", "value2"));
        MetricId joined = MetricId.join(part1, part2);
        assertEquals("test1.test2", joined.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2"), joined.getTags());
    }

    @Test
    public void testJoinWithNullKey() {
        MetricId part1 = new MetricId(null, ImmutableMap.of("tag1", "value1"));
        MetricId part2 = new MetricId("test2", ImmutableMap.of("tag2", "value2"));
        MetricId joined = MetricId.join(part1, part2);
        assertEquals("test2", joined.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2"), joined.getTags());
    }

    @Test
    public void testJoinWithEmptyKey() {
        MetricId part1 = new MetricId("", ImmutableMap.of("tag1", "value1"));
        MetricId part2 = new MetricId("test2", ImmutableMap.of("tag2", "value2"));
        MetricId joined = MetricId.join(part1, part2);
        assertEquals("test2", joined.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2"), joined.getTags());
    }

    @Test
    public void testBuild() {
        MetricId built = MetricId.build("test1", "test2");
        assertEquals("test1.test2", built.getKey());
        assertEquals(MetricId.EMPTY_TAGS, built.getTags());
    }

    @Test
    public void testBuildSingle() {
        MetricId built = MetricId.build("test");
        assertEquals("test", built.getKey());
        assertEquals(MetricId.EMPTY_TAGS, built.getTags());
    }

    @Test
    public void testBuildNull() {
        MetricId built = MetricId.build((String[]) null);
        assertEquals(MetricId.EMPTY, built);
    }

    @Test
    public void testBuildEmptyArray() {
        MetricId built = MetricId.build();
        assertEquals(MetricId.EMPTY, built);
    }

    @Test
    public void testToString() {
        MetricId metricId = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        String str = metricId.toString();
        assertNotNull(str);
        assertTrue(str.contains("test"));
        assertTrue(str.contains("tag1"));
        assertTrue(str.contains("value1"));
    }

    @Test
    public void testHashCode() {
        MetricId metricId1 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        assertEquals(metricId1.hashCode(), metricId2.hashCode());
    }

    @Test
    public void testEquals() {
        MetricId metricId1 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        assertEquals(metricId1, metricId2);
    }

    @Test
    public void testEqualsNull() {
        MetricId metricId = new MetricId("test");
        assertFalse(metricId.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        MetricId metricId = new MetricId("test");
        assertFalse(metricId.equals("string"));
    }

    @Test
    public void testEqualsDifferentKey() {
        MetricId metricId1 = new MetricId("test1");
        MetricId metricId2 = new MetricId("test2");
        assertNotEquals(metricId1, metricId2);
    }

    @Test
    public void testEqualsDifferentTags() {
        MetricId metricId1 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test", ImmutableMap.of("tag1", "value2"));
        assertNotEquals(metricId1, metricId2);
    }

    @Test
    public void testCompareTo() {
        MetricId metricId1 = new MetricId("test1", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test2", ImmutableMap.of("tag1", "value1"));
        assertTrue(metricId1.compareTo(metricId2) < 0);
    }

    @Test
    public void testCompareToNull() {
        MetricId metricId = new MetricId("test");
        assertTrue(metricId.compareTo(null) > 0);
    }

    @Test
    public void testCompareToSameObject() {
        MetricId metricId = new MetricId("test");
        assertEquals(0, metricId.compareTo(metricId));
    }

    @Test
    public void testCompareToSameContent() {
        MetricId metricId1 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        assertEquals(0, metricId1.compareTo(metricId2));
    }

    @Test
    public void testCompareToDifferentKey() {
        MetricId metricId1 = new MetricId("a");
        MetricId metricId2 = new MetricId("b");
        assertTrue(metricId1.compareTo(metricId2) < 0);
    }

    @Test
    public void testCompareToDifferentTags() {
        MetricId metricId1 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test", ImmutableMap.of("tag1", "value2"));
        assertTrue(metricId1.compareTo(metricId2) < 0);
    }

    @Test
    public void testGetKey() {
        MetricId metricId = new MetricId("test");
        assertEquals("test", metricId.getKey());
    }

    @Test
    public void testGetTags() {
        Map<String, String> tags = ImmutableMap.of("key1", "value1");
        MetricId metricId = new MetricId("test", tags);
        assertEquals(tags, metricId.getTags());
    }

    @Test
    public void testGetTagsReturnsUnmodifiableView() {
        MetricId metricId = new MetricId("test", ImmutableMap.of("key1", "value1"));
        Map<String, String> tags = metricId.getTags();
        try {
            tags.put("newkey", "newvalue");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}
