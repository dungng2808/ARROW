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
    public void testEmptyMetricId() {
        MetricId metricId = new MetricId();
        assertNull(metricId.getKey());
        assertTrue(metricId.getTags().isEmpty());
    }

    @Test
    public void testMetricIdWithKey() {
        MetricId metricId = new MetricId("testKey");
        assertEquals("testKey", metricId.getKey());
        assertTrue(metricId.getTags().isEmpty());
    }

    @Test
    public void testMetricIdWithKeyAndTags() {
        Map<String, String> tags = ImmutableMap.of("tag1", "value1", "tag2", "value2");
        MetricId metricId = new MetricId("testKey", tags);
        assertEquals("testKey", metricId.getKey());
        assertEquals(tags, metricId.getTags());
    }

    @Test
    public void testResolve() {
        MetricId metricId = new MetricId("parent");
        MetricId resolved = metricId.resolve("child");
        assertEquals("parent.child", resolved.getKey());
        assertEquals(metricId.getTags(), resolved.getTags());
    }

    @Test
    public void testResolveWithNullPart() {
        MetricId metricId = new MetricId("parent");
        MetricId resolved = metricId.resolve(null);
        assertEquals("parent", resolved.getKey());
        assertEquals(metricId.getTags(), resolved.getTags());
    }

    @Test
    public void testResolveWithEmptyPart() {
        MetricId metricId = new MetricId("parent");
        MetricId resolved = metricId.resolve("");
        assertEquals("parent", resolved.getKey());
        assertEquals(metricId.getTags(), resolved.getTags());
    }

    @Test
    public void testResolveWithNullKey() {
        MetricId metricId = new MetricId((String) null);
        MetricId resolved = metricId.resolve("child");
        assertEquals("child", resolved.getKey());
        assertEquals(metricId.getTags(), resolved.getTags());
    }

    @Test
    public void testTaggedWithMap() {
        MetricId metricId = new MetricId("test");
        Map<String, String> addTags = ImmutableMap.of("newTag", "newValue");
        MetricId tagged = metricId.tagged(addTags);
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("newTag", "newValue"), tagged.getTags());
    }

    @Test
    public void testTaggedWithVariadicArgs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("tag1", "value1", "tag2", "value2");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2"), tagged.getTags());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTaggedWithOddNumberOfArgs() {
        MetricId metricId = new MetricId("test");
        metricId.tagged("tag1", "value1", "tag2");
    }

    @Test
    public void testTaggedWithSinglePair() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("tag1", "value1");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1"), tagged.getTags());
    }

    @Test
    public void testTaggedWithTwoPairs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("tag1", "value1", "tag2", "value2");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2"), tagged.getTags());
    }

    @Test
    public void testTaggedWithThreePairs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("tag1", "value1", "tag2", "value2", "tag3", "value3");
        assertEquals("test", tagged.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2", "tag3", "value3"), tagged.getTags());
    }

    @Test
    public void testJoin() {
        MetricId part1 = new MetricId("part1", ImmutableMap.of("tag1", "value1"));
        MetricId part2 = new MetricId("part2", ImmutableMap.of("tag2", "value2"));
        MetricId joined = MetricId.join(part1, part2);
        assertEquals("part1.part2", joined.getKey());
        assertEquals(ImmutableMap.of("tag1", "value1", "tag2", "value2"), joined.getTags());
    }

    @Test
    public void testJoinWithNullParts() {
        MetricId joined = MetricId.join((MetricId[]) null);
        assertEquals(MetricId.EMPTY, joined);
    }

    @Test
    public void testBuildWithNoParts() {
        MetricId built = MetricId.build();
        assertEquals(MetricId.EMPTY, built);
    }

    @Test
    public void testBuildWithSinglePart() {
        MetricId built = MetricId.build("single");
        assertEquals("single", built.getKey());
        assertTrue(built.getTags().isEmpty());
    }

    @Test
    public void testBuildWithMultipleParts() {
        MetricId built = MetricId.build("part1", "part2", "part3");
        assertEquals("part1.part2.part3", built.getKey());
        assertTrue(built.getTags().isEmpty());
    }

    @Test
    public void testBuildWithNullPart() {
        MetricId built = MetricId.build("part1", null, "part2");
        assertEquals("part1.part2", built.getKey());
        assertTrue(built.getTags().isEmpty());
    }

    @Test
    public void testBuildWithEmptyPart() {
        MetricId built = MetricId.build("part1", "", "part2");
        assertEquals("part1.part2", built.getKey());
        assertTrue(built.getTags().isEmpty());
    }

    @Test
    public void testToString() {
        MetricId metricId = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        String toString = metricId.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test"));
        assertTrue(toString.contains("tag1"));
        assertTrue(toString.contains("value1"));
    }

    @Test
    public void testEqualsAndHashCode() {
        MetricId metricId1 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("test", ImmutableMap.of("tag1", "value1"));
        MetricId metricId3 = new MetricId("different", ImmutableMap.of("tag1", "value1"));

        assertEquals(metricId1, metricId2);
        assertEquals(metricId1.hashCode(), metricId2.hashCode());
        assertNotEquals(metricId1, metricId3);
    }

    @Test
    public void testCompareTo() {
        MetricId metricId1 = new MetricId("a", ImmutableMap.of("tag1", "value1"));
        MetricId metricId2 = new MetricId("b", ImmutableMap.of("tag1", "value1"));
        MetricId metricId3 = new MetricId("a", ImmutableMap.of("tag1", "value2"));

        assertTrue(metricId1.compareTo(metricId2) < 0);
        assertTrue(metricId2.compareTo(metricId1) > 0);
        assertTrue(metricId1.compareTo(metricId3) < 0);
        assertTrue(metricId3.compareTo(metricId1) > 0);
        assertEquals(0, metricId1.compareTo(metricId1));
    }

    @Test
    public void testCompareToWithNull() {
        MetricId metricId = new MetricId("test");
        assertTrue(metricId.compareTo(null) > 0);
    }

    @Test
    public void testCompareToWithSameInstance() {
        MetricId metricId = new MetricId("test");
        assertEquals(0, metricId.compareTo(metricId));
    }
}
