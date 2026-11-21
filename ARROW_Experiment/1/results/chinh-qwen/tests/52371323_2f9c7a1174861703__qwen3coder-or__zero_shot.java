package com.spotify.metrics.core;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
        Map<String, String> tags = new TreeMap<>();
        tags.put("key1", "value1");
        MetricId metricId = new MetricId("test", tags);
        assertEquals("test", metricId.getKey());
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
    public void testTaggedMap() {
        MetricId metricId = new MetricId("test");
        Map<String, String> add = new TreeMap<>();
        add.put("key1", "value1");
        MetricId tagged = metricId.tagged(add);
        assertEquals("test", tagged.getKey());
        assertEquals(add, tagged.getTags());
    }

    @Test
    public void testTaggedVariadic() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1", "key2", "value2");
        assertEquals("test", tagged.getKey());
        Map<String, String> expectedTags = new TreeMap<>();
        expectedTags.put("key1", "value1");
        expectedTags.put("key2", "value2");
        assertEquals(expectedTags, tagged.getTags());
    }

    @Test
    public void testTaggedSinglePair() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1");
        assertEquals("test", tagged.getKey());
        Map<String, String> expectedTags = new TreeMap<>();
        expectedTags.put("key1", "value1");
        assertEquals(expectedTags, tagged.getTags());
    }

    @Test
    public void testTaggedTwoPairs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1", "key2", "value2");
        assertEquals("test", tagged.getKey());
        Map<String, String> expectedTags = new TreeMap<>();
        expectedTags.put("key1", "value1");
        expectedTags.put("key2", "value2");
        assertEquals(expectedTags, tagged.getTags());
    }

    @Test
    public void testTaggedThreePairs() {
        MetricId metricId = new MetricId("test");
        MetricId tagged = metricId.tagged("key1", "value1", "key2", "value2", "key3", "value3");
        assertEquals("test", tagged.getKey());
        Map<String, String> expectedTags = new TreeMap<>();
        expectedTags.put("key1", "value1");
        expectedTags.put("key2", "value2");
        expectedTags.put("key3", "value3");
        assertEquals(expectedTags, tagged.getTags());
    }

    @Test
    public void testJoin() {
        MetricId part1 = new MetricId("part1", Collections.singletonMap("tag1", "value1"));
        MetricId part2 = new MetricId("part2", Collections.singletonMap("tag2", "value2"));
        MetricId joined = MetricId.join(part1, part2);
        assertEquals("part1.part2", joined.getKey());
        Map<String, String> expectedTags = new TreeMap<>();
        expectedTags.put("tag1", "value1");
        expectedTags.put("tag2", "value2");
        assertEquals(expectedTags, joined.getTags());
    }

    @Test
    public void testJoinWithNullParts() {
        MetricId joined = MetricId.join((MetricId[]) null);
        assertEquals(MetricId.EMPTY, joined);
    }

    @Test
    public void testBuild() {
        MetricId built = MetricId.build("part1", "part2");
        assertEquals("part1.part2", built.getKey());
        assertEquals(MetricId.EMPTY_TAGS, built.getTags());
    }

    @Test
    public void testBuildSinglePart() {
        MetricId built = MetricId.build("single");
        assertEquals("single", built.getKey());
        assertEquals(MetricId.EMPTY_TAGS, built.getTags());
    }

    @Test
    public void testBuildNoParts() {
        MetricId built = MetricId.build();
        assertEquals(MetricId.EMPTY, built);
    }

    @Test
    public void testToString() {
        MetricId metricId = new MetricId("test", Collections.singletonMap("key", "value"));
        String result = metricId.toString();
        assertNotNull(result);
    }

    @Test
    public void testEquals() {
        MetricId metricId1 = new MetricId("test", Collections.singletonMap("key", "value"));
        MetricId metricId2 = new MetricId("test", Collections.singletonMap("key", "value"));
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
        assertFalse(metricId.equals("not a metric id"));
    }

    @Test
    public void testEqualsSameObject() {
        MetricId metricId = new MetricId("test");
        assertEquals(metricId, metricId);
    }

    @Test
    public void testCompareTo() {
        MetricId metricId1 = new MetricId("a");
        MetricId metricId2 = new MetricId("b");
        assertTrue(metricId1.compareTo(metricId2) < 0);
    }

    @Test
    public void testCompareToNull() {
        MetricId metricId = new MetricId("test");
        assertTrue(metricId.compareTo(null) > 0);
    }

    @Test
    public void testCompareToSame() {
        MetricId metricId1 = new MetricId("test");
        MetricId metricId2 = new MetricId("test");
        assertEquals(0, metricId1.compareTo(metricId2));
    }

    @Test
    public void testHashCode() {
        MetricId metricId = new MetricId("test", Collections.singletonMap("key", "value"));
        int hash = metricId.hashCode();
        assertTrue(hash != 0);
    }

    @Test
    public void testExtendKey() {
        MetricId metricId = new MetricId("parent");
        String extended = metricId.getClass().getDeclaredMethod("extendKey", String.class)
            .invoke(metricId, "child").toString();
        assertEquals("parent.child", extended);
    }
}
